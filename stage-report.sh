#!/usr/bin/env sh

# Creates a one-page PDF CI report from the artifacts available after a stage.
# It deliberately uses Ghostscript rather than a browser or remote service, so
# report generation stays local to the Jenkins agent.
set -eu

STAGE_NAME="${1:?STAGE_NAME is required}"
STAGE_STATUS="${2:?STAGE_STATUS is required}"
JOB_NAME="${3:?JOB_NAME is required}"
BUILD_NUMBER="${4:?BUILD_NUMBER is required}"
BUILD_URL="${5:?BUILD_URL is required}"
OUTPUT_FILE="${6:?OUTPUT_FILE is required}"

command -v gs >/dev/null 2>&1 || {
    printf '%s\n' 'Ghostscript (gs) is required to generate the PDF report.' >&2
    exit 2
}

safe_text() {
    # Escape PostScript control characters and remove newlines from values.
    printf '%s' "$1" | tr '\n\r' '  ' | sed 's/\\/\\\\/g; s/(/\\(/g; s/)/\\)/g'
}

test_summary() {
    reports_dir='backend-springboot/target/surefire-reports'
    [ -d "$reports_dir" ] || { printf '%s' 'Not available'; return; }
    awk '
        match($0, /tests="[0-9]+"/) { value = substr($0, RSTART + 7, RLENGTH - 8); tests += value }
        match($0, /failures="[0-9]+"/) { value = substr($0, RSTART + 10, RLENGTH - 11); failures += value }
        match($0, /errors="[0-9]+"/) { value = substr($0, RSTART + 8, RLENGTH - 9); errors += value }
        END { printf "%d tests, %d failures, %d errors", tests, failures, errors }
    ' "$reports_dir"/TEST-*.xml 2>/dev/null || printf '%s' 'Not available'
}

coverage_summary() {
    csv='backend-springboot/target/site/jacoco/jacoco.csv'
    [ -f "$csv" ] || { printf '%s' 'Not available'; return; }
    awk -F, '
        NR > 1 { missed += $4; covered += $5 }
        END {
            total = missed + covered
            if (total == 0) print "Not available"
            else printf "Instruction coverage: %.2f%% (%d / %d covered)", (covered * 100 / total), covered, total
        }
    ' "$csv"
}

stage_details() {
    case "$STAGE_NAME" in
        'Backend Unit Test')
            printf '%s\n' "Unit tests: $(test_summary)"
            printf '%s\n' "Coverage: $(coverage_summary)"
            ;;
        'Check fucked up code')
            printf '%s\n' 'Checkstyle analysis completed; see Jenkins console for rule details.'
            ;;
        'Security testing')
            if [ -f backend-springboot/semgrep-report.json ]; then
                findings=$(grep -o '"check_id"' backend-springboot/semgrep-report.json | wc -l | tr -d ' ')
                printf '%s\n' "Semgrep findings reported: ${findings}"
            else
                printf '%s\n' 'Semgrep report was not produced.'
            fi
            ;;
        'Docker Build')
            printf '%s\n' 'Docker image build completed.'
            ;;
        'Deploy')
            printf '%s\n' 'Deployment command completed; readiness is verified by the performance stage.'
            ;;
        'Performance Testing')
            printf '%s\n' 'k6 performance test completed. See the Jenkins console for thresholds and latency metrics.'
            ;;
        *)
            printf '%s\n' 'Stage execution completed. See the Jenkins console for details.'
            ;;
    esac
}

mkdir -p "$(dirname "$OUTPUT_FILE")"
TEMP_PS="${OUTPUT_FILE%.pdf}.ps"
TEMP_DETAILS="${OUTPUT_FILE%.pdf}.details"
trap 'rm -f "$TEMP_PS" "$TEMP_DETAILS"' EXIT HUP INT TERM
stage_details > "$TEMP_DETAILS"

{
    printf '%%!PS-Adobe-3.0\n'
    printf '/Helvetica findfont 11 scalefont setfont\n'
    printf '72 760 moveto (CI/CD Stage Report) show\n'
    printf '/Helvetica findfont 9 scalefont setfont\n'
    line=730
    report_line() {
        printf '72 %s moveto (%s) show\n' "$line" "$(safe_text "$1")"
        line=$((line - 22))
    }
    report_line "Project: $JOB_NAME"
    report_line "Build: #$BUILD_NUMBER"
    report_line "Stage: $STAGE_NAME"
    report_line "Status: $STAGE_STATUS"
    report_line ""
    while IFS= read -r detail; do report_line "$detail"; done < "$TEMP_DETAILS"
    report_line ""
    report_line "Jenkins: $BUILD_URL"
    printf 'showpage\n'
} > "$TEMP_PS"

gs -q -dBATCH -dNOPAUSE -sDEVICE=pdfwrite -sOutputFile="$OUTPUT_FILE" "$TEMP_PS"
printf '%s\n' "PDF stage report created: $OUTPUT_FILE"
