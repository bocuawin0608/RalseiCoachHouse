#!/usr/bin/env sh

# Sends a CI/CD failure notice to the team responsible for the failed stage.
# SMTP credentials must be supplied by Jenkins, never committed to this repo.
set -eu

usage() {
    cat <<'EOF'
Usage: email.sh JOB_NAME BUILD_NUMBER BUILD_URL LOG_FILE [FAILED_STAGE]

Required: SMTP_HOST, SMTP_USER, SMTP_PASSWORD
Recipients: BACKEND_TEAM_EMAILS, DEVOPS_TEAM_EMAILS, QA_TEAM_EMAILS,
            SECURITY_TEAM_EMAILS (comma/semicolon-separated); MAIL_TO is fallback.
Optional: SMTP_PORT (587), MAIL_FROM (SMTP_USER), FAILURE_SUMMARY, FAILED_STAGE
EOF
}

if [ "${1:-}" = "--help" ] || [ "${1:-}" = "-h" ]; then usage; exit 0; fi

JOB_NAME="${1:?JOB_NAME is required}"
BUILD_NUMBER="${2:?BUILD_NUMBER is required}"
BUILD_URL="${3:?BUILD_URL is required}"
LOG_FILE="${4:-}"
FAILED_STAGE="${5:-${FAILED_STAGE:-Unknown}}"

: "${SMTP_HOST:?SMTP_HOST is required}"
: "${SMTP_USER:?SMTP_USER is required}"
: "${SMTP_PASSWORD:?SMTP_PASSWORD is required}"

SMTP_PORT="${SMTP_PORT:-587}"
MAIL_FROM="${MAIL_FROM:-$SMTP_USER}"
# Test fallback. Configure role-specific recipient variables in Jenkins before
# using this pipeline for the wider team.
MAIL_TO="${MAIL_TO:-doanngocduc2006@gmail.com}"

role_for_stage() {
    case "$1" in
        'Checkout'|'Backend Unit Test'|'Check fucked up code'|'Build Maven') printf '%s' 'Backend engineering' ;;
        'Security testing') printf '%s' 'Security and DevOps' ;;
        'Docker Build'|'Deploy') printf '%s' 'DevOps' ;;
        'Performance Testing') printf '%s' 'Backend engineering, QA and DevOps' ;;
        *) printf '%s' 'DevOps (triage)' ;;
    esac
}

recipients_for_stage() {
    case "$1" in
        'Checkout'|'Backend Unit Test'|'Check fucked up code'|'Build Maven') printf '%s' "${BACKEND_TEAM_EMAILS:-}" ;;
        'Security testing') printf '%s' "${SECURITY_TEAM_EMAILS:-}${SECURITY_TEAM_EMAILS:+,}${DEVOPS_TEAM_EMAILS:-}" ;;
        'Docker Build'|'Deploy') printf '%s' "${DEVOPS_TEAM_EMAILS:-}" ;;
        'Performance Testing') printf '%s' "${BACKEND_TEAM_EMAILS:-}${BACKEND_TEAM_EMAILS:+,}${QA_TEAM_EMAILS:-}${QA_TEAM_EMAILS:+,}${DEVOPS_TEAM_EMAILS:-}" ;;
        *) printf '%s' "${DEVOPS_TEAM_EMAILS:-}" ;;
    esac
}

ROLE="$(role_for_stage "$FAILED_STAGE")"
RECIPIENTS="$(recipients_for_stage "$FAILED_STAGE")"
RECIPIENTS="${RECIPIENTS:-${MAIL_TO:-}}"
[ -n "$RECIPIENTS" ] || { printf '%s\n' "No recipients configured for failed stage: $FAILED_STAGE" >&2; exit 2; }

FAILURE_SUMMARY="${FAILURE_SUMMARY:-See the attached Jenkins report and console log for the failing command and stack trace.}"
SUBJECT="[CI/CD FAILED] ${JOB_NAME} #${BUILD_NUMBER} — ${FAILED_STAGE}"
BODY=$(cat <<EOF
CI/CD pipeline failed and requires action.

Project       : ${JOB_NAME}
Build         : #${BUILD_NUMBER}
Failed stage  : ${FAILED_STAGE}
Responsible   : ${ROLE}
Status        : FAILED

Failure summary:
${FAILURE_SUMMARY}

Jenkins build:
${BUILD_URL}
EOF
)

set -- --fail --silent --show-error \
    --url "smtp://${SMTP_HOST}:${SMTP_PORT}" \
    --ssl-reqd \
    --user "${SMTP_USER}:${SMTP_PASSWORD}" \
    --mail-from "${MAIL_FROM}"

# curl needs one --mail-rcpt per recipient. Permit both common separators.
OLD_IFS=$IFS
IFS=',;'
set -f
for recipient in $RECIPIENTS; do
    recipient=$(printf '%s' "$recipient" | tr -d '[:space:]')
    [ -n "$recipient" ] && set -- "$@" --mail-rcpt "$recipient"
done
set +f
IFS=$OLD_IFS

set -- "$@" \
    --form-string "from=${MAIL_FROM}" \
    --form-string "to=${RECIPIENTS}" \
    --form-string "subject=${SUBJECT}" \
    --form-string "body=${BODY}"

if [ -n "$LOG_FILE" ] && [ -f "$LOG_FILE" ]; then
    set -- "$@" --form "attachment=@${LOG_FILE};filename=jenkins-build-${BUILD_NUMBER}.log"
fi

curl "$@"
