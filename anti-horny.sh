#!/bin/bash
# Anti-Horny Failure Handler & Rickroll Script
# Triggered when test execution or build stages fail in CI/CD or local dev.

EXIT_CODE="${1:-1}"
FAILURE_MESSAGE="${2:-Test execution failed! Anti-Horny Protocol Activated.}"
LOG_FILE="${3:-}"

echo "================================================================="
echo "               🚨 ANTI-HORNY FAILURE DETECTED 🚨                  "
echo "================================================================="
echo ""
echo "❌ Error Details:"
echo "   $FAILURE_MESSAGE"
echo ""

if [ -n "$LOG_FILE" ] && [ -f "$LOG_FILE" ]; then
    echo "📄 Last 30 lines of log ($LOG_FILE):"
    echo "-----------------------------------------------------------------"
    tail -n 30 "$LOG_FILE"
    echo "-----------------------------------------------------------------"
fi

echo ""
echo "🎵 Activating Rickroll Protocol..."

RICKROLL_URL="https://www.youtube.com/watch?v=dQw4w9WgXcQ"

# Attempt to open browser if graphical environment is present
if [ -n "$DISPLAY" ] || [ -n "$WAYLAND_DISPLAY" ]; then
    if command -v xdg-open >/dev/null 2>&1; then
        xdg-open "$RICKROLL_URL" >/dev/null 2>&1 &
    elif command -v sensible-browser >/dev/null 2>&1; then
        sensible-browser "$RICKROLL_URL" >/dev/null 2>&1 &
    elif command -v python3 >/dev/null 2>&1; then
        python3 -c "import webbrowser; webbrowser.open('$RICKROLL_URL')" >/dev/null 2>&1 &
    fi
fi

# Fallback: ASCII Rickroll in terminal
if command -v curl >/dev/null 2>&1; then
    echo "📺 Playing ASCII Rickroll in terminal..."
    curl -sL https://raw.githubusercontent.com/keroserene/rickroll/master/roll.sh | bash || true
fi

echo "================================================================="
echo "Fix your tests before trying again!"
echo "================================================================="

exit "$EXIT_CODE"
