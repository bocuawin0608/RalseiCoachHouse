pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        BACKEND_DIR = 'backend-springboot'
        REGISTRY_URL = 'docker.io'
        IMAGE_REPO = 'ralsei/ralsei-coach-house-be'
        VERSION = "0.0.${BUILD_NUMBER}"
        IMAGE_TAG = "${IMAGE_REPO}:${VERSION}"
        GITOPS_REPO_URL = 'https://github.com/ralsei/ralsei-gitops-config.git'
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
                    steps {
                        script { env.FAILED_STAGE = 'Security Testing' }
                        dir(env.BACKEND_DIR) {
                            sh '''
                                set -eu
                                semgrep scan --config=auto --json --output semgrep-report.json || true
                            '''
                        }
                    }
                }
            }
        }

        stage('Build & Unit Test') {
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
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_REGISTRY_USER',
                    passwordVariable: 'DOCKER_REGISTRY_PASSWORD'
                )]) {
                    dir(env.BACKEND_DIR) {
                        sh '''
                            set -eu
                            echo "$DOCKER_REGISTRY_PASSWORD" | docker login "$REGISTRY_URL" -u "$DOCKER_REGISTRY_USER" --password-stdin
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
                        if [ ! -d "$BACKEND_DIR/k8s" ]; then
                            printf '%s\\n' "ERROR: Kubernetes manifests directory is missing." >&2
                            exit 1
                        fi

                        find "$BACKEND_DIR/k8s" -type f \\( -name '*.yaml' -o -name '*.yml' \\) | sort | while IFS= read -r file; do
                            if [ ! -s "$file" ]; then
                                printf '%s\\n' "ERROR: Empty manifest file detected: $file" >&2
                                exit 1
                            fi
                        done

                        printf '%s\\n' "Kubernetes manifests validation passed."
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
                script {
                    env.FAILED_STAGE = 'GitOps CD Promotion'

                    def branchName = env.BRANCH_NAME ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    def targetEnv = ''
                    def overlayPath = ''

                    if (branchName == 'develop') {
                        targetEnv = 'dev'
                        overlayPath = 'k8s/overlays/dev'
                    } else if (branchName.startsWith('release/')) {
                        targetEnv = 'staging'
                        overlayPath = 'k8s/overlays/staging'
                    } else if (branchName == 'main') {
                        targetEnv = 'prod'
                        overlayPath = 'k8s/overlays/prod'
                    } else {
                        echo "Skipping GitOps promotion for branch '${branchName}'. Only develop, release/* and main are supported."
                        return
                    }

                    env.TARGET_ENV = targetEnv
                    env.GITOPS_OVERLAY_PATH = overlayPath

                    if (targetEnv == 'prod') {
                        input(
                            id: 'prod-approval',
                            message: "Approve production GitOps promotion for build #${env.BUILD_NUMBER} on branch ${branchName}.",
                            ok: 'Approve Production'
                        )
                    }

                    withCredentials([usernamePassword(
                        credentialsId: 'gitops-credentials',
                        usernameVariable: 'GITOPS_USERNAME',
                        passwordVariable: 'GITOPS_TOKEN'
                    )]) {
                        sh '''
                            set -eu
                            rm -rf "$WORKSPACE/gitops"
                            git clone "https://${GITOPS_USERNAME}:${GITOPS_TOKEN}@github.com/ralsei/ralsei-gitops-config.git" "$WORKSPACE/gitops"
                            git -C "$WORKSPACE/gitops" checkout "$GITOPS_DEFAULT_BRANCH"
                            git -C "$WORKSPACE/gitops" config user.name "Jenkins CI"
                            git -C "$WORKSPACE/gitops" config user.email "jenkins@ralsei.local"

                            (cd "$WORKSPACE/gitops/$GITOPS_OVERLAY_PATH" && kustomize edit set image ralsei/ralsei-coach-house-be="$IMAGE_TAG")

                            git -C "$WORKSPACE/gitops" add .
                            if git -C "$WORKSPACE/gitops" diff --cached --quiet; then
                                printf '%s\\n' "No GitOps manifest change required for ${TARGET_ENV}."
                                exit 0
                            fi

                            git -C "$WORKSPACE/gitops" commit -m "chore(ci): promote ${IMAGE_TAG} to ${TARGET_ENV}"
                            git -C "$WORKSPACE/gitops" push "https://${GITOPS_USERNAME}:${GITOPS_TOKEN}@github.com/ralsei/ralsei-gitops-config.git" HEAD:"${GITOPS_DEFAULT_BRANCH}"
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                def buildNumber = env.BUILD_NUMBER ?: 'unknown'
                def branchName = env.BRANCH_NAME ?: 'unknown'
                def stageName = env.FAILED_STAGE ?: 'All CI stages completed'

                withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK_URL')]) {
                    sh """
                        set -eu
                        curl -fsS -X POST -H 'Content-Type: application/json' \
                            --data '{"text":"✅ CI pipeline succeeded\\nBuild #: ${buildNumber}\\nBranch: ${branchName}\\nStage: ${stageName}"}' \
                            "${SLACK_WEBHOOK_URL}"
                    """
                }
            }
        }

        failure {
            script {
                def buildNumber = env.BUILD_NUMBER ?: 'unknown'
                def branchName = env.BRANCH_NAME ?: 'unknown'
                def stageName = env.FAILED_STAGE ?: 'unknown'

                withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK_URL')]) {
                    sh """
                        set -eu
                        curl -fsS -X POST -H 'Content-Type: application/json' \
                            --data '{"text":"❌ CI pipeline failed\\nBuild #: ${buildNumber}\\nBranch: ${branchName}\\nStage: ${stageName}"}' \
                            "${SLACK_WEBHOOK_URL}"
                    """
                }
            }
        }

        always {
            archiveArtifacts artifacts: 'backend-springboot/semgrep-report.json', fingerprint: true, allowEmptyArchive: true
            cleanWs()
        }
    }
}