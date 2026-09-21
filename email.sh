#!/usr/bin/env sh

# Sends a MIME multipart Jenkins PDF report through SMTP. Credentials live in a
# protected file on the build agent; no raw email text is piped to an SMTP tool.
set -eu

usage() {
    cat <<'EOF'
Usage: email.sh JOB_NAME BUILD_NUMBER BUILD_URL REPORT_FILE [STAGE] [STATUS]

SMTP configuration is loaded from CI_EMAIL_CONFIG (default:
/etc/nhaxetuanmv-ci-email.env). The file must define SMTP_HOST, SMTP_USER,
and SMTP_PASSWORD and must not be committed to source control.
Recipients: BACKEND_TEAM_EMAILS, DEVOPS_TEAM_EMAILS, QA_TEAM_EMAILS,
            SECURITY_TEAM_EMAILS, PIPELINE_REPORT_EMAILS (comma/semicolon-separated);
            MAIL_TO is fallback.
Optional: SMTP_PORT (587), MAIL_FROM (SMTP_USER), BUILD_DURATION
EOF
}

if [ "${1:-}" = "--help" ] || [ "${1:-}" = "-h" ]; then usage; exit 0; fi

JOB_NAME="${1:?JOB_NAME is required}"
BUILD_NUMBER="${2:?BUILD_NUMBER is required}"
BUILD_URL="${3:?BUILD_URL is required}"
REPORT_FILE="${4:-}"
FAILED_STAGE="${5:-${FAILED_STAGE:-Unknown}}"
BUILD_STATUS="${6:-${BUILD_STATUS:-FAILED}}"

CI_EMAIL_CONFIG="${CI_EMAIL_CONFIG:-/etc/nhaxetuanmv-ci-email.env}"
if [ ! -r "$CI_EMAIL_CONFIG" ]; then
    printf '%s\n' "SMTP configuration is not readable: $CI_EMAIL_CONFIG" >&2
    printf '%s\n' "Provision the external SMTP secret on the Jenkins agent or mount it into the agent container." >&2
    exit 2
fi

# This file is operator-managed, outside the repository, and permissioned to
# the CI agent user (recommended mode: 0600).
# shellcheck disable=SC1090
. "$CI_EMAIL_CONFIG"

: "${SMTP_HOST:?SMTP_HOST is required}"
: "${SMTP_USER:?SMTP_USER is required}"
: "${SMTP_PASSWORD:?SMTP_PASSWORD is required}"

SMTP_PORT="${SMTP_PORT:-587}"
MAIL_FROM="${MAIL_FROM:-$SMTP_USER}"
# Test fallback. Configure role-specific recipient variables in the external
# SMTP configuration file before using this pipeline for the wider team.
MAIL_TO="${MAIL_TO:-doanngocduc2006@gmail.com}"

recipients_for_stage() {
    case "$1" in
        'Pipeline Analytics') printf '%s' "${PIPELINE_REPORT_EMAILS:-${MAIL_TO:-}}" ;;
        'Checkout'|'Backend Unit Test'|'Check fucked up code'|'Build Maven') printf '%s' "${BACKEND_TEAM_EMAILS:-}" ;;
        'Security testing') printf '%s' "${SECURITY_TEAM_EMAILS:-}${SECURITY_TEAM_EMAILS:+,}${DEVOPS_TEAM_EMAILS:-}" ;;
        'Docker Build'|'Deploy') printf '%s' "${DEVOPS_TEAM_EMAILS:-}" ;;
        'Performance Testing') printf '%s' "${BACKEND_TEAM_EMAILS:-}${BACKEND_TEAM_EMAILS:+,}${QA_TEAM_EMAILS:-}${QA_TEAM_EMAILS:+,}${DEVOPS_TEAM_EMAILS:-}" ;;
        *) printf '%s' "${DEVOPS_TEAM_EMAILS:-}" ;;
    esac
}

RECIPIENTS="$(recipients_for_stage "$FAILED_STAGE")"
RECIPIENTS="${RECIPIENTS:-${MAIL_TO:-}}"
[ -n "$RECIPIENTS" ] || { printf '%s\n' "No recipients configured for failed stage: $FAILED_STAGE" >&2; exit 2; }

if [ -z "$REPORT_FILE" ] || [ ! -s "$REPORT_FILE" ]; then
    printf '%s\n' "PDF report is missing or empty; refusing to send an incomplete email: ${REPORT_FILE:-<unset>}" >&2
    exit 2
fi

PYTHON_BIN="${REPORT_PYTHON:-python3}"
command -v "$PYTHON_BIN" >/dev/null || { printf '%s\n' "Python interpreter is not available: $PYTHON_BIN" >&2; exit 69; }
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

# Keep the password out of the process argument list. jenkins_reporter.py reads
# these values only from the process environment.
export SMTP_HOST SMTP_PORT SMTP_PASSWORD
exec "$PYTHON_BIN" "$SCRIPT_DIR/jenkins_reporter.py" send-email \
    --pdf "$REPORT_FILE" \
    --job-name "$JOB_NAME" \
    --build-number "$BUILD_NUMBER" \
    --status "$BUILD_STATUS" \
    --failed-stage "$FAILED_STAGE" \
    --build-url "$BUILD_URL" \
    --duration "${BUILD_DURATION:-Not available}" \
    --recipients "$RECIPIENTS" \
    --sender-email "$MAIL_FROM"
