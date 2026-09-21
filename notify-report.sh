#!/usr/bin/env sh

# Generate the final Jenkins analytics PDF, then notify through the external
# SMTP configuration. Reporting/email problems must not change CI build status.
set -u

STATUS="${1:?STATUS is required}"
OUTPUT_FILE="jenkins-build-${BUILD_NUMBER:?BUILD_NUMBER is required}-analytics.pdf"
REPORT_FILE=''
REPORT_CONFIG="${JENKINS_REPORT_CONFIG:-/etc/nhaxetuanmv-jenkins-report.env}"
REPORT_READY=false

# Do not echo API-token-expanded commands in the Jenkins console.
set +x

if [ ! -r "$REPORT_CONFIG" ]; then
    printf '%s\n' "REPORT ERROR: API configuration is not readable: $REPORT_CONFIG" >&2
elif ! . "$REPORT_CONFIG"; then
    printf '%s\n' 'REPORT ERROR: Unable to load API configuration.' >&2
elif [ -z "${JENKINS_API_URL:-}" ] || [ -z "${JENKINS_API_USER:-}" ] || [ -z "${JENKINS_API_TOKEN:-}" ]; then
    printf '%s\n' 'REPORT ERROR: JENKINS_API_URL, JENKINS_API_USER, and JENKINS_API_TOKEN are required.' >&2
else
    [ -z "${REPORT_PYTHON:-}" ] || export REPORT_PYTHON
    printf '%s\n' "REPORT: Creating $OUTPUT_FILE from Jenkins API data..."
    if ./report.sh -u "$JENKINS_API_URL" -j "$JOB_NAME" -b "$BUILD_NUMBER" \
        -usr "$JENKINS_API_USER" -t "$JENKINS_API_TOKEN" -o "$OUTPUT_FILE" \
        --build-url "$BUILD_URL" -s "$STATUS" && [ -s "$OUTPUT_FILE" ]; then
        REPORT_READY=true
        REPORT_FILE="$OUTPUT_FILE"
        printf '%s\n' "REPORT: PDF created successfully: $REPORT_FILE"
    else
        printf '%s\n' 'REPORT ERROR: PDF generation failed; review the report.sh output above.' >&2
    fi
fi

printf '%s\n' "EMAIL: Sending $STATUS notification (PDF attached: $REPORT_READY)..."
if ./email.sh "$JOB_NAME" "$BUILD_NUMBER" "$BUILD_URL" "$REPORT_FILE" 'Pipeline Analytics' "$STATUS"; then
    printf '%s\n' 'EMAIL: Notification sent successfully.'
else
    status=$?
    printf '%s\n' "EMAIL ERROR: Notification failed (exit $status). See the SMTP error above." >&2
fi

# Notifications are observability; they must never overwrite the CI result.
exit 0
