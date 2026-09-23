pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        BACKEND_DIR = 'backend-springboot'
        K8S_MANIFEST_DIR = 'backend-springboot/k8s'
        K8S_NAMESPACE = 'default'
        K8S_DEPLOYMENT = 'ralsei-be'
        K8S_CONTAINER = 'ralsei-be'
        K8S_RUNTIME_SECRET = 'ralsei-be-runtime'
        KIND_CLUSTER = 'local'
        IMAGE = 'ralsei-coach-house-be'
        VERSION = "0.0.${BUILD_NUMBER}"
        IMAGE_TAG = "${IMAGE}:${VERSION}"
    }

    stages {
        stage('Checkout') {
            steps {
                script { env.FAILED_STAGE = 'Checkout' }
                checkout scm
            }
        }

        stage('Backend Unit Test') {
            steps {
                script { env.FAILED_STAGE = 'Backend Unit Test' }
                dir(env.BACKEND_DIR) {
                    sh '''
                        set -eu
                        chmod +x ./mvnw
                        ./mvnw clean test
                    '''
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'backend-springboot/target/surefire-reports/TEST-*.xml'
                }
            }
        }

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
                        semgrep scan --config=auto --json --output semgrep-report.json
                    '''
                }
            }
        }

        stage('Build Maven') {
            steps {
                script { env.FAILED_STAGE = 'Build Maven' }
                dir(env.BACKEND_DIR) {
                    sh '''
                        set -eu
                        chmod +x ./mvnw
                        ./mvnw clean package -DskipTests
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                script { env.FAILED_STAGE = 'Docker Build' }
                dir(env.BACKEND_DIR) {
                    sh '''
                        set -eu
                        docker build --pull -t "$IMAGE_TAG" .
                    '''
                }
            }
        }

        stage('Validate Kubernetes Manifests') {
            steps {
                script { env.FAILED_STAGE = 'Validate Kubernetes Manifests' }
                sh '''
                    set -eu

                    if [ ! -d "$K8S_MANIFEST_DIR" ]; then
                        printf '%s\\n' "ERROR: Kubernetes manifests directory is missing: $K8S_MANIFEST_DIR" >&2
                        printf '%s\\n' 'Expected a workspace-relative directory containing deployment manifests.' >&2
                        printf '%s\\n' 'Available top-level workspace directories:' >&2
                        find . -mindepth 1 -maxdepth 1 -type d -print | sort >&2
                        exit 1
                    fi

                    manifest_found=false
                    for manifest in "$K8S_MANIFEST_DIR"/*.yaml "$K8S_MANIFEST_DIR"/*.yml; do
                        if [ -f "$manifest" ]; then
                            manifest_found=true
                            break
                        fi
                    done

                    if [ "$manifest_found" != true ]; then
                        printf '%s\\n' "ERROR: No .yaml or .yml manifests found directly in $K8S_MANIFEST_DIR" >&2
                        exit 1
                    fi

                    # A failed Secret lookup can mean either that the Secret is
                    # absent or that kubectl is pointed at an invalid API server.
                    # Check the API endpoint first so the latter is not reported as
                    # a missing Secret.
                    if ! kubectl get --raw='/api' --request-timeout=10s > /dev/null; then
                        printf '%s\\n' "ERROR: Kubernetes API is unavailable or the active kubeconfig context is invalid." >&2
                        printf '%s\\n' "Active context: $(kubectl config current-context 2>/dev/null || printf '%s' '<none>')" >&2
                        printf '%s\\n' "Repair the Kind cluster/kubeconfig, then rerun the deployment." >&2
                        exit 1
                    fi

                    if ! secret_name=$(kubectl get secret --namespace "$K8S_NAMESPACE" "$K8S_RUNTIME_SECRET" \
                        --ignore-not-found --output=name --request-timeout=10s); then
                        printf '%s\\n' "ERROR: Could not query runtime Secret $K8S_RUNTIME_SECRET in namespace $K8S_NAMESPACE." >&2
                        printf '%s\\n' 'The Kubernetes API did not complete the Secret lookup; inspect the cluster and kubeconfig.' >&2
                        exit 1
                    fi

                    if [ -z "$secret_name" ]; then
                        printf '%s\\n' "ERROR: Required runtime Secret is missing: $K8S_RUNTIME_SECRET" >&2
                        printf '%s\\n' "Create it in namespace $K8S_NAMESPACE before deployment; see $K8S_MANIFEST_DIR/README.md." >&2
                        exit 1
                    fi
                '''
            }
        }

        stage('Production Deployment') {
            steps {
                script { env.FAILED_STAGE = 'Production Deployment' }
                dir(env.K8S_MANIFEST_DIR) {
                    sh '''
                        set -eu

                        kind load docker-image "$IMAGE_TAG" --name "$KIND_CLUSTER"
                        kubectl apply --namespace "$K8S_NAMESPACE" -f .
                        kubectl set image --namespace "$K8S_NAMESPACE" "deployment/$K8S_DEPLOYMENT" \\
                            "$K8S_CONTAINER=$IMAGE_TAG"
                        kubectl rollout status --namespace "$K8S_NAMESPACE" "deployment/$K8S_DEPLOYMENT" \\
                            --timeout=120s
                    '''
                }
            }
        }

        stage('Send CI Report') {
            steps {
                script { env.FAILED_STAGE = 'Send CI Report' }
                withCredentials([usernamePassword(
                    credentialsId: 'jenkins-report-api',
                    usernameVariable: 'JENKINS_REPORT_API_USER',
                    passwordVariable: 'JENKINS_REPORT_API_TOKEN'
                )]) {
                    sh '''
                        set -eu
                        ./notify-report.sh SUCCESS
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'Kubernetes deployment and CI reporting completed successfully.'
        }

        failure {
            echo "Pipeline failed in stage: ${env.FAILED_STAGE ?: 'unknown'}"
            withCredentials([usernamePassword(
                credentialsId: 'jenkins-report-api',
                usernameVariable: 'JENKINS_REPORT_API_USER',
                passwordVariable: 'JENKINS_REPORT_API_TOKEN'
            )]) {
                sh '''
                    kind export kubeconfig --name local
                    kubectl cluster-info

                    set -eu
                    ./notify-report.sh FAILED
                '''
            }
        }

        always {
            archiveArtifacts artifacts: 'backend-springboot/semgrep-report.json,jenkins-build-*-analytics.pdf', fingerprint: true, allowEmptyArchive: true
            cleanWs()
        }
    }
}
