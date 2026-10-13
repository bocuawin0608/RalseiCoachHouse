pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '15')) // Đừng để rác log nuốt chửng ổ cứng
    }

    environment {
        BACKEND_DIR = 'backend-springboot'
        REGISTRY_URL = 'docker.io'
        IMAGE_REPO = 'bocuawin0608/ralsei-coach-house-be'
        VERSION = "0.0.${BUILD_NUMBER}"
        IMAGE_TAG = "${IMAGE_REPO}:${VERSION}"
        GITOPS_REPO_URL = 'https://github.com/bocuawin0608/ralsei-git-ops.git'
        GITOPS_DEFAULT_BRANCH = 'main'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Static Analysis') {
            parallel {
                stage('Code Style Check') {
                    agent {
                        docker {
                            image 'eclipse-temurin:17-jdk-jammy'
                            // Ánh xạ thư mục .m2 của host vào /maven_cache trong container
                            // Sử dụng MAVEN_OPTS để ép Maven ghi đè đường dẫn kho chứa cục bộ
                            args '-v $HOME/.m2:/maven_cache -e MAVEN_OPTS="-Dmaven.repo.local=/maven_cache" -e HOME=/tmp --dns 8.8.8.8'
                        }
                    }
                    steps {
                        script { env.FAILED_STAGE = 'Code Style Check' }
                        dir(env.BACKEND_DIR) {
                            sh '''
                                set -eu
                                chmod +x ./mvnw
                                ./mvnw checkstyle:check
                            '''
                        }
                    }
                }

                stage('Security Testing') {
                    agent {
                        docker {
                            image 'returntocorp/semgrep'
                            args '--entrypoint="" -e HOME=/tmp --dns 8.8.8.8 -e GITHUB_WORKSPACE=/tmp'
                        }
                    }
                    steps {
                        script { env.FAILED_STAGE = 'Security Testing' }
                        dir(env.BACKEND_DIR) {
                            sh 'semgrep scan --config=auto --json --output semgrep-report.json || true'
                        }
                    }
                }
            }
        }

        stage('Build & Unit Test') {
            agent {
                docker {
                    reuseNode true
                    image 'eclipse-temurin:17-jdk-jammy'
                    args '-v $HOME/.m2:/maven_cache -e MAVEN_OPTS="-Dmaven.repo.local=/maven_cache" -e HOME=/tmp --dns 8.8.8.8' // Cứu rỗi băng thông và CPU nhờ Cache
                }
            }
            steps {
                script { env.FAILED_STAGE = 'Build & Unit Test' }
                dir(env.BACKEND_DIR) {
                    sh '''
                        set -eu
                        chmod +x ./mvnw
                        ./mvnw clean package
                    '''
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'backend-springboot/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Docker Build & Push') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'release/*'
                }
            }
            steps {
                script { env.FAILED_STAGE = 'Docker Build & Push' }
                withCredentials([usernamePassword(
                    credentialsId: 'deploy',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    dir(env.BACKEND_DIR) {
                        sh '''
                            set -eu
                            cp target/*.war app.war
                            echo "$DOCKER_PASS" | docker login "$REGISTRY_URL" -u "$DOCKER_USER" --password-stdin
                            docker build --network=host --pull -t "$IMAGE_TAG" .
                            docker push "$IMAGE_TAG"
                        '''
                    }
                }
            }
        }

        stage('Validate Kubernetes Manifests') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'release/*'
                }
            }
            steps {
                script { env.FAILED_STAGE = 'Validate Kubernetes Manifests' }
                sh '''
                    set -eu
                    echo "Kích hoạt Kubeconform để rà quét schema thực thụ..."
                    # Quét toàn bộ thư mục k8s, chặn đứng mọi YAML rác
                    docker run --rm --dns 8.8.8.8 -v "${WORKSPACE}:/workspace" -w /workspace ghcr.io/yannh/kubeconform:latest -summary -strict backend-springboot/k8s/
                '''
            }
        }

        stage('GitOps CD Promotion') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'release/*'
                }
            }
            steps {
                // (Giữ nguyên logic phân nhánh và kustomize của anh, nó ổn)
                script {
                    env.FAILED_STAGE = 'GitOps CD Promotion'
                    def branchName = env.BRANCH_NAME ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    def targetEnv = branchName == 'develop' ? 'dev' : (branchName == 'main' ? 'prod' : 'staging')
                    def overlayPath = "k8s/overlays/${targetEnv}"

                    if (targetEnv == 'prod') {
                        input id: 'prod-approval', message: 'Approve production promotion?', ok: 'Deploy to Prod'
                    }

                    withEnv(["OVERLAY_PATH=${overlayPath}", "TARGET_ENV=${targetEnv}"]) {
                        try {
                            withCredentials([
                                usernamePassword(
                                    credentialsId: 'gitops-credentials',
                                    usernameVariable: 'GITOPS_USERNAME',
                                    passwordVariable: 'GITOPS_TOKEN'
                                )
                            ]) {
                                sh '''
                                    set -eu

                                    AUTH_DIR=$(mktemp -d "${WORKSPACE}/.git-auth-XXXXXX")
                                    chmod 700 "$AUTH_DIR"
                                    ASKPASS_SCRIPT="$AUTH_DIR/askpass.sh"

                                    cat <<'EOF' > "$ASKPASS_SCRIPT"
#!/bin/sh
case "$1" in
    *Username*|*username*) exec printf '%s\n' "$GITOPS_USERNAME" ;;
    *Password*|*password*) exec printf '%s\n' "$GITOPS_TOKEN" ;;
    *) exec printf '%s\n' "$GITOPS_TOKEN" ;;
esac
EOF
                                    chmod 700 "$ASKPASS_SCRIPT"

                                    cleanup() {
                                        rm -rf "$AUTH_DIR"
                                    }
                                    trap cleanup EXIT INT TERM

                                    export GIT_ASKPASS="$ASKPASS_SCRIPT"
                                    export GIT_TERMINAL_PROMPT=0

                                    rm -rf "$WORKSPACE/gitops"
                                    git clone "$GITOPS_REPO_URL" "$WORKSPACE/gitops"
                                    cd "$WORKSPACE/gitops"
                                    git checkout "$GITOPS_DEFAULT_BRANCH"
                                    git config user.name "Jenkins CI"
                                    git config user.email "jenkins@ralsei.local"

                                    cd "$WORKSPACE/gitops/$OVERLAY_PATH"
                                    if command -v kustomize >/dev/null 2>&1; then
                                        KUSTOMIZE_BIN="kustomize"
                                    else
                                        curl -sSfL "https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh" | bash
                                        KUSTOMIZE_BIN="./kustomize"
                                    fi
                                    "$KUSTOMIZE_BIN" edit set image ralsei/ralsei-coach-house-be="$IMAGE_TAG"
                                    rm -f ./kustomize

                                    cd "$WORKSPACE/gitops"
                                    git add .
                                    if ! git diff --cached --quiet; then
                                        git commit -m "chore(ci): promote $IMAGE_TAG to $TARGET_ENV"
                                        git push origin HEAD:"$GITOPS_DEFAULT_BRANCH"
                                    else
                                        echo "No GitOps change required."
                                    fi

                                    if command -v kind >/dev/null 2>&1 && kind get clusters 2>/dev/null | grep -q "^local$"; then
                                        echo "Loading image $IMAGE_TAG into local Kind cluster..."
                                        kind load docker-image "$IMAGE_TAG" --name local || true

                                        KUBETMP=$(mktemp "${WORKSPACE}/.kube-XXXXXX")
                                        kind get kubeconfig --name local > "$KUBETMP"
                                        export KUBECONFIG="$KUBETMP"

                                        echo "Applying Kustomize overlay to cluster..."
                                        kubectl apply -k "$WORKSPACE/gitops/$OVERLAY_PATH"

                                        echo "Verifying deployment rollout..."
                                        if ! kubectl -n "ralsei-${TARGET_ENV}" rollout status deployment/backend --timeout=120s; then
                                            echo "Rollout failed! Initiating rollback..."
                                            kubectl -n "ralsei-${TARGET_ENV}" rollout undo deployment/backend || true
                                            rm -f "$KUBETMP"
                                            exit 1
                                        fi

                                        echo "Verifying backend via Kong Gateway..."
                                        sleep 3
                                        curl -fsS -H "Host: nhaxetuanmv.local" http://172.21.0.2:32080/ >/dev/null
                                        echo "Backend Kong gateway health check passed."

                                        rm -f "$KUBETMP"
                                        echo "Deployment rollout completed and verified successfully."
                                    fi
                                '''
                            }
                        } finally {
                            sh(script: 'rm -rf "${WORKSPACE}"/.git-auth-* 2>/dev/null || true', returnStatus: true)
                        }
                    }
                }
            }
        }
    }

    post {
        // Slack notifications disabled until 'slack-webhook-url' credential is configured.
        // success {
        //     script {
        //         try {
        //             withCredentials([
        //                 string(
        //                     credentialsId: 'slack-webhook-url',
        //                     variable: 'SLACK_WEBHOOK_URL'
        //                 )
        //             ]) {
        //                 def branchName = env.BRANCH_NAME ?: 'unknown'
        //                 def buildNum = env.BUILD_NUMBER ?: 'unknown'
        //                 def message = "[SUCCESS] CI pipeline succeeded for backend (Build #${buildNum}, Branch: ${branchName})"
        //                 def payload = groovy.json.JsonOutput.toJson([text: message])
        //
        //                 withEnv(["SLACK_PAYLOAD=${payload}"]) {
        //                     sh(
        //                         script: '''
        //                             set +x
        //                             if [ -n "${SLACK_WEBHOOK_URL:-}" ]; then
        //                                 curl -sS -X POST \
        //                                     -H 'Content-type: application/json' \
        //                                     --data "$SLACK_PAYLOAD" \
        //                                     "$SLACK_WEBHOOK_URL" > /dev/null || echo "Warning: Slack notification delivery failed."
        //                             else
        //                                 echo "Notice: SLACK_WEBHOOK_URL is empty; skipping Slack notification."
        //                             fi
        //                         ''',
        //                         returnStatus: true
        //                     )
        //                 }
        //             }
        //         } catch (Exception e) {
        //             echo "Notice: Slack success notification skipped or failed: ${e.message}"
        //         }
        //     }
        // }
        //
        // failure {
        //     script {
        //         try {
        //             withCredentials([
        //                 string(
        //                     credentialsId: 'slack-webhook-url',
        //                     variable: 'SLACK_WEBHOOK_URL'
        //                 )
        //             ]) {
        //                 def branchName = env.BRANCH_NAME ?: 'unknown'
        //                 def buildNum = env.BUILD_NUMBER ?: 'unknown'
        //                 def failedStage = env.FAILED_STAGE ?: 'unknown'
        //                 def message = "[FAILURE] CI pipeline failed for backend (Build #${buildNum}, Branch: ${branchName}, Stage: ${failedStage})"
        //                 def payload = groovy.json.JsonOutput.toJson([text: message])
        //
        //                 withEnv(["SLACK_PAYLOAD=${payload}"]) {
        //                     sh(
        //                         script: '''
        //                             set +x
        //                             if [ -n "${SLACK_WEBHOOK_URL:-}" ]; then
        //                                 curl -sS -X POST \
        //                                     -H 'Content-type: application/json' \
        //                                     --data "$SLACK_PAYLOAD" \
        //                                     "$SLACK_WEBHOOK_URL" > /dev/null || echo "Warning: Slack notification delivery failed."
        //                             else
        //                                 echo "Notice: SLACK_WEBHOOK_URL is empty; skipping Slack notification."
        //                             fi
        //                         ''',
        //                         returnStatus: true
        //                     )
        //                 }
        //             }
        //         } catch (Exception e) {
        //             echo "Notice: Slack failure notification skipped or failed: ${e.message}"
        //         }
        //     }
        // }

        always {
            cleanWs()
        }
    }
}
