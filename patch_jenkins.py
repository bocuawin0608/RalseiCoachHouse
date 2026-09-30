import re

with open('Jenkinsfile.be', 'r') as f:
    content = f.read()

# Replace the fragile replacement block
pattern = re.compile(r'DEPLOYMENT_FILE="\$\(find "\$WORKSPACE/gitops/\$GITOPS_OVERLAY_PATH" -type f[^\n]+\n[^\n]+\n[^\n]+\n[^\n]+\n[^\n]+\n[^\n]+\n[^\n]+\n[^\n]+\n[^\n]+')
replacement = """(cd "$WORKSPACE/gitops/$GITOPS_OVERLAY_PATH" && kustomize edit set image ralsei/ralsei-coach-house-be="$IMAGE_TAG")"""

new_content = content.replace('''DEPLOYMENT_FILE="$(find "$WORKSPACE/gitops/$GITOPS_OVERLAY_PATH" -type f \\( -name '*.yaml' -o -name '*.yml' \\) | head -n 1)"
                            if [ -z "$DEPLOYMENT_FILE" ]; then
                                printf '%s\\n' "ERROR: No deployment manifest found in $GITOPS_OVERLAY_PATH" >&2
                                exit 1
                            fi

                            if command -v yq >/dev/null 2>&1; then
                                yq e -i '.spec.template.spec.containers[0].image = strenv(IMAGE_TAG)' "$DEPLOYMENT_FILE"
                            else
                                sed -i -E "s#(image:\\s*).+?#\\\\1${IMAGE_TAG}#g" "$DEPLOYMENT_FILE"
                            fi''', replacement)

with open('Jenkinsfile.be', 'w') as f:
    f.write(new_content)
