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

if [ -r "$REPORT_CONFIG" ] && ! . "$REPORT_CONFIG"; then
    printf '%s\n' 'REPORT ERROR: Unable to load API configuration.' >&2
else
    # Values injected by Jenkins Credentials take precedence over the legacy
    # agent file. The username/token must never be committed or printed.
    API_URL="${JENKINS_REPORT_API_URL:-${JENKINS_API_URL:-${JENKINS_URL:-}}}"
    API_USER="${JENKINS_REPORT_API_USER:-${JENKINS_API_USER:-}}"
    API_TOKEN="${JENKINS_REPORT_API_TOKEN:-${JENKINS_API_TOKEN:-}}"
    if [ -z "$API_URL" ] || [ -z "$API_USER" ] || [ -z "$API_TOKEN" ]; then
        printf '%s\n' 'REPORT ERROR: Bind the jenkins-report-api Jenkins credential (or provide the legacy API configuration file).' >&2
    else
        [ -z "${REPORT_PYTHON:-}" ] || export REPORT_PYTHON
        export JENKINS_REPORTER_TOKEN="$API_TOKEN"
        printf '%s\n' "REPORT: Creating $OUTPUT_FILE from Jenkins API data..."
        if ./report.sh -u "$API_URL" -j "$JOB_NAME" -b "$BUILD_NUMBER" \
            -usr "$API_USER" -o "$OUTPUT_FILE" \
            --build-url "$BUILD_URL" -s "$STATUS" && [ -s "$OUTPUT_FILE" ]; then
            REPORT_READY=true
            REPORT_FILE="$OUTPUT_FILE"
            printf '%s\n' "REPORT: PDF created successfully: $REPORT_FILE"
        else
            printf '%s\n' 'REPORT ERROR: PDF generation failed; review the report.sh output above.' >&2
        fi
    fi
fi

if [ "$REPORT_READY" = true ]; then
    printf '%s\n' "EMAIL: Sending $STATUS notification with verified PDF attachment..."
    if ./email.sh "$JOB_NAME" "$BUILD_NUMBER" "$BUILD_URL" "$REPORT_FILE" 'Pipeline Analytics' "$STATUS"; then
        printf '%s\n' 'EMAIL: Notification sent successfully.'
    else
        status=$?
        printf '%s\n' "EMAIL ERROR: Notification failed (exit $status). See the SMTP error above." >&2
    fi
else
    printf '%s\n' 'EMAIL: Not sent because no verified PDF report was generated.' >&2
fi

# Notifications are observability; they must never overwrite the CI result.
exit 0
