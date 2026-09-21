#!/usr/bin/env bash

# Jenkins build analytics PDF wrapper. No secrets are written to disk or echoed.
set -Eeuo pipefail

usage() {
  cat <<'EOF'
Usage: report.sh -u JENKINS_URL -j JOB_NAME -b BUILD_NUMBER -usr USER -t TOKEN -o OUTPUT_PDF_PATH [-s STATUS]

Example:
  ./report.sh -u http://jenkins.local:8080 -j backend-pipeline -b 142 \
    -usr admin -t 'api-token' -o Build_142_Report.pdf
EOF
}

JENKINS_URL='' JOB_NAME='' BUILD_NUMBER='' JENKINS_USER='' JENKINS_TOKEN='' OUTPUT_PDF='' STATUS_OVERRIDE=''
while (($#)); do
  case "$1" in
    -u) JENKINS_URL="${2:?Missing value for -u}"; shift 2 ;;
    -j) JOB_NAME="${2:?Missing value for -j}"; shift 2 ;;
    -b) BUILD_NUMBER="${2:?Missing value for -b}"; shift 2 ;;
    -usr|--user) JENKINS_USER="${2:?Missing value for $1}"; shift 2 ;;
    -t|--token) JENKINS_TOKEN="${2:?Missing value for $1}"; shift 2 ;;
    -o|--output) OUTPUT_PDF="${2:?Missing value for $1}"; shift 2 ;;
    -s|--status) STATUS_OVERRIDE="${2:?Missing value for $1}"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) printf 'Unknown option: %s\n' "$1" >&2; usage >&2; exit 64 ;;
  esac
done

for name in JENKINS_URL JOB_NAME BUILD_NUMBER JENKINS_USER JENKINS_TOKEN OUTPUT_PDF; do
  [[ -n "${!name}" ]] || { printf 'Missing required option: %s\n' "$name" >&2; usage >&2; exit 64; }
done
[[ "$BUILD_NUMBER" =~ ^[0-9]+$ ]] || { echo 'BUILD_NUMBER must be numeric.' >&2; exit 64; }
PYTHON_BIN="${REPORT_PYTHON:-python3}"
command -v "$PYTHON_BIN" >/dev/null || { echo "Python interpreter is not available: $PYTHON_BIN" >&2; exit 69; }
command -v curl >/dev/null || { echo 'curl is required.' >&2; exit 69; }

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
"$PYTHON_BIN" - <<'PY' || { echo "Install dependencies with: $PYTHON_BIN -m pip install -r requirements.txt" >&2; exit 69; }
import importlib.util
missing = [module for module in ('requests', 'jinja2', 'weasyprint') if importlib.util.find_spec(module) is None]
if missing:
    raise SystemExit('Missing Python modules: ' + ', '.join(missing))
PY

mkdir -p "$(dirname -- "$OUTPUT_PDF")"
command=("$PYTHON_BIN" "$SCRIPT_DIR/jenkins_reporter.py" \
  --jenkins-url "$JENKINS_URL" --job-name "$JOB_NAME" --build-number "$BUILD_NUMBER" \
  --user "$JENKINS_USER" --token "$JENKINS_TOKEN" --output "$OUTPUT_PDF" \
  --template "$SCRIPT_DIR/template.html" --stylesheet "$SCRIPT_DIR/style.css")
[[ -z "$STATUS_OVERRIDE" ]] || command+=(--status-override "$STATUS_OVERRIDE")
exec "${command[@]}"
