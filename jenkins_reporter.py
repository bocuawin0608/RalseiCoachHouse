#!/usr/bin/env python3
"""Collect Jenkins Pipeline data and create a structured PDF build report."""
from __future__ import annotations

import argparse
import re
import sys
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path
from typing import Any
from urllib.parse import quote

import requests
from jinja2 import Environment, FileSystemLoader, select_autoescape
from weasyprint import CSS, HTML

TIMEOUT = (10, 30)
LOG_SNIPPET_LINES = 24
ERROR_PATTERNS = [
    ("Out of memory", re.compile(r"(?:outofmemoryerror|java heap space|killed process)", re.I)),
    ("Timeout", re.compile(r"(?:timeout|timed out|deadline exceeded)", re.I)),
    ("Connection failure", re.compile(r"(?:connection refused|connectexception|unknownhost|connection reset)", re.I)),
    ("Test failure", re.compile(r"(?:tests run:.*failures: [1-9]|there (?:were|was) failing tests|assertionerror)", re.I)),
    ("Compilation failure", re.compile(r"(?:compilation failure|cannot find symbol|failed to compile)", re.I)),
    ("Build command failure", re.compile(r"(?:script returned exit code|\[ERROR\].*(?:failed|failure)|error:)", re.I)),
]


class JenkinsApiError(RuntimeError):
    pass


def job_url(base: str, job_name: str, build: int | None = None) -> str:
    parts = ["job", *[quote(part, safe="") for part in job_name.strip("/").split("/") if part]]
    if not parts[1:]:
        raise JenkinsApiError("JOB_NAME must not be empty")
    path = "/".join(parts)
    return f"{base.rstrip('/')}/{path}" + (f"/{build}" if build is not None else "")


class JenkinsClient:
    def __init__(self, base: str, user: str, token: str):
        self.base = base.rstrip("/")
        self.session = requests.Session()
        self.session.auth = (user, token)
        self.session.headers.update({"Accept": "application/json"})

    def get_json(self, url: str, required: bool = True) -> dict[str, Any] | list[Any] | None:
        try:
            response = self.session.get(url, timeout=TIMEOUT)
        except requests.RequestException as exc:
            raise JenkinsApiError(f"Cannot reach Jenkins: {exc}") from exc
        if response.status_code == 404 and not required:
            return None
        if response.status_code in (401, 403):
            raise JenkinsApiError("Jenkins authentication was rejected. Check user and API token permissions.")
        if not response.ok:
            raise JenkinsApiError(f"Jenkins API returned HTTP {response.status_code} for {url}")
        try:
            return response.json()
        except ValueError as exc:
            raise JenkinsApiError(f"Jenkins returned invalid JSON for {url}") from exc

    def get_text(self, url: str) -> str:
        try:
            response = self.session.get(url, timeout=TIMEOUT)
        except requests.RequestException as exc:
            raise JenkinsApiError(f"Cannot retrieve console log: {exc}") from exc
        if response.status_code in (401, 403):
            raise JenkinsApiError("Jenkins authentication was rejected while reading console log.")
        if not response.ok:
            return f"Console log unavailable (HTTP {response.status_code})."
        return response.text


def duration_ms(start: Any, end: Any) -> int:
    try:
        return max(0, int(end or 0) - int(start or 0))
    except (TypeError, ValueError):
        return 0


def format_duration(milliseconds: int) -> str:
    seconds = max(0, round(milliseconds / 1000))
    minutes, seconds = divmod(seconds, 60)
    hours, minutes = divmod(minutes, 60)
    return f"{hours}h {minutes}m {seconds}s" if hours else (f"{minutes}m {seconds}s" if minutes else f"{seconds}s")


def format_timestamp(milliseconds: Any) -> str:
    try:
        return datetime.fromtimestamp(int(milliseconds) / 1000, tz=timezone.utc).astimezone().strftime("%Y-%m-%d %H:%M:%S %Z")
    except (TypeError, ValueError, OSError):
        return "Not available"


def normalized_status(value: Any) -> str:
    value = str(value or "UNKNOWN").upper()
    return {"SUCCESS": "SUCCESS", "FAILURE": "FAILED", "FAILED": "FAILED", "ABORTED": "ABORTED", "IN_PROGRESS": "RUNNING"}.get(value, value)


def extract_log_analysis(log: str) -> dict[str, Any]:
    lines = log.splitlines()
    matches: list[tuple[int, str]] = []
    categories: list[str] = []
    for index, line in enumerate(lines):
        for category, pattern in ERROR_PATTERNS:
            if pattern.search(line):
                matches.append((index, line.strip()))
                categories.append(category)
                break
    if not matches:
        return {"root_cause": "No recognized failure signature was found in the Jenkins console log.", "snippet": "No error snippet available.", "categories": []}
    index, line = matches[-1]
    start, end = max(0, index - 5), min(len(lines), index + LOG_SNIPPET_LINES)
    return {"root_cause": line[:500], "snippet": "\n".join(lines[start:end]), "categories": list(dict.fromkeys(categories))}


def stage_log_slice(console_log: str, stage_name: str) -> str:
    """Best-effort stage isolation from Declarative Pipeline console markers."""
    lines = console_log.splitlines()
    markers = [index for index, line in enumerate(lines) if stage_name in line and ("[Pipeline]" in line or "stage" in line.lower())]
    if not markers:
        return console_log
    start = markers[-1]
    end = next((index for index in range(start + 1, len(lines)) if "[Pipeline]" in lines[index] and "stage" in lines[index].lower()), len(lines))
    return "\n".join(lines[start:end])


def collect_tests(payload: Any) -> dict[str, Any]:
    if not isinstance(payload, dict):
        return {"total": 0, "passed": 0, "failed": 0, "skipped": 0, "suites": [], "failures": []}
    total = int(payload.get("totalCount") or 0)
    failed = int(payload.get("failCount") or 0)
    skipped = int(payload.get("skipCount") or 0)
    suites, failures = [], []
    for suite in payload.get("suites") or []:
        name = suite.get("name") or "Unnamed suite"
        cases = suite.get("cases") or []
        suite_failed = sum(1 for case in cases if str(case.get("status", "")).upper() not in ("PASSED", "SKIPPED"))
        suites.append({"name": name, "total": len(cases), "failed": suite_failed})
        for case in cases:
            if str(case.get("status", "")).upper() in ("PASSED", "SKIPPED"):
                continue
            failures.append({"suite": name, "name": case.get("name") or "Unnamed test", "error": (case.get("errorDetails") or case.get("errorStackTrace") or "No stack trace supplied by Jenkins")[:2500]})
    return {"total": total, "passed": max(0, total - failed - skipped), "failed": failed, "skipped": skipped, "suites": suites, "failures": failures[:20]}


def collect_report(args: argparse.Namespace) -> dict[str, Any]:
    client = JenkinsClient(args.jenkins_url, args.user, args.token)
    build_url = job_url(args.jenkins_url, args.job_name, args.build_number)
    build = client.get_json(f"{build_url}/api/json?tree=displayName,fullDisplayName,result,timestamp,duration,building,description,actions[causes[shortDescription]]") or {}
    console_log = client.get_text(f"{build_url}/consoleText")
    pipeline = client.get_json(f"{build_url}/wfapi/describe", required=False) or {}
    if not pipeline:
        runs = client.get_json(f"{job_url(args.jenkins_url, args.job_name)}/wfapi/runs", required=False) or []
        if isinstance(runs, list):
            pipeline = next((run for run in runs if str(run.get("id")) == str(args.build_number)), {})
    if not isinstance(pipeline, dict):
        pipeline = {}
    raw_stages = pipeline.get("stages") or []
    stages = []
    for stage in raw_stages:
        status = normalized_status(stage.get("status"))
        stage_duration = duration_ms(stage.get("startTimeMillis"), stage.get("endTimeMillis"))
        stage_name = stage.get("name") or "Unnamed stage"
        stage_analysis = extract_log_analysis(stage_log_slice(console_log, stage_name)) if status in ("FAILED", "ABORTED") else None
        stages.append({"name": stage_name, "status": status, "duration": format_duration(stage_duration), "duration_ms": stage_duration,
                       "error_summary": stage_analysis["root_cause"] if stage_analysis else "—"})
    if not stages:
        stages.append({"name": "Pipeline stages unavailable", "status": "UNKNOWN", "duration": "—", "duration_ms": 0, "error_summary": "Pipeline Stage View API is unavailable or the Pipeline: REST API plugin is not installed."})
    test_payload = client.get_json(f"{build_url}/testReport/api/json", required=False)
    tests = collect_tests(test_payload)
    analysis = extract_log_analysis(console_log)
    result = normalized_status(args.status_override or build.get("result") or ("RUNNING" if build.get("building") else "UNKNOWN"))
    causes = [cause.get("shortDescription") for action in build.get("actions") or [] for cause in (action or {}).get("causes") or [] if cause.get("shortDescription")]
    completed = [stage for stage in stages if stage["status"] != "UNKNOWN"]
    succeeded = sum(1 for stage in completed if stage["status"] == "SUCCESS")
    return {
        "project_name": args.job_name.split("/")[-1], "job_name": args.job_name, "build_number": args.build_number,
        "build_url": build_url, "status": result, "trigger": "; ".join(causes) or "Not available",
        "timestamp": format_timestamp(build.get("timestamp")), "total_duration": format_duration(int(build.get("duration") or 0)),
        "total_stages": len(stages), "success_rate": round((succeeded / len(completed) * 100) if completed else 0, 1),
        "stages": stages, "tests": tests, "analysis": analysis,
        "generated_at": datetime.now().astimezone().strftime("%Y-%m-%d %H:%M:%S %Z"),
        "environment": f"Jenkins API: {args.jenkins_url.rstrip('/')}",
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jenkins-url", required=True); parser.add_argument("--job-name", required=True)
    parser.add_argument("--build-number", required=True, type=int); parser.add_argument("--user", required=True)
    parser.add_argument("--token", required=True); parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--status-override", choices=["SUCCESS", "FAILED", "ABORTED", "UNSTABLE", "UNKNOWN"])
    parser.add_argument("--template", required=True, type=Path); parser.add_argument("--stylesheet", required=True, type=Path)
    args = parser.parse_args()
    try:
        report = collect_report(args)
        environment = Environment(loader=FileSystemLoader(str(args.template.parent)), autoescape=select_autoescape(["html"]))
        html = environment.get_template(args.template.name).render(report=report)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        HTML(string=html, base_url=str(args.template.parent)).write_pdf(str(args.output), stylesheets=[CSS(filename=str(args.stylesheet))])
        print(f"Jenkins PDF report created: {args.output}")
        return 0
    except JenkinsApiError as exc:
        print(f"Report generation failed: {exc}", file=sys.stderr)
    except (OSError, requests.RequestException) as exc:
        print(f"Report generation failed: {exc}", file=sys.stderr)
    return 2


if __name__ == "__main__":
    raise SystemExit(main())
