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
        DB_IMAGE = 'ralsei-db'
        VERSION = "0.0.${BUILD_NUMBER}"
        IMAGE_TAG = "${IMAGE}:${VERSION}"
        DB_IMAGE_TAG = "${DB_IMAGE}:${VERSION}"
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

        stage('Build Database Image') {
            steps {
                script { env.FAILED_STAGE = 'Build Database Image' }
                dir(env.BACKEND_DIR + '/db') {
                    sh '''
                        set -eu
                        docker build -t "$DB_IMAGE_TAG" .
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

                   if ! kubectl get secret --namespace "$K8S_NAMESPACE" "$K8S_RUNTIME_SECRET" > /dev/null; then
                       printf '%s\\n' "ERROR: Required runtime Secret is missing: $K8S_RUNTIME_SECRET" >&2
                       printf '%s\\n' "Create it in namespace $K8S_NAMESPACE before deployment; see $K8S_MANIFEST_DIR/README.md." >&2
                       exit 1
                   fi

                    # Do not deploy template values such as <host>. `envFrom`
                    # would otherwise pass them to Spring as real configuration,
                    # causing a crash loop that is only visible after deployment.
                    for secret_key in SPRING_DATASOURCE_URL SPRING_DATASOURCE_USERNAME \
                        SPRING_DATASOURCE_PASSWORD SPRING_DATA_REDIS_HOST JWT_SECRET \
                        SEPAY_API_TOKEN GOONG_API_KEY MAIL_USERNAME MAIL_PASSWORD MAIL_FROM; do
                        encoded_value=$(kubectl get secret --namespace "$K8S_NAMESPACE" "$K8S_RUNTIME_SECRET" \
                            --output="jsonpath={.data.${secret_key}}")
                        if [ -z "$encoded_value" ]; then
                            printf '%s\\n' "ERROR: Runtime Secret $K8S_RUNTIME_SECRET is missing required key: $secret_key" >&2
                            exit 1
                        fi

                        if ! secret_value=$(printf '%s' "$encoded_value" | base64 --decode); then
                            printf '%s\\n' "ERROR: Runtime Secret $K8S_RUNTIME_SECRET contains an unreadable value for key: $secret_key" >&2
                            exit 1
                        fi

                        case "$secret_value" in
                            *'<'*'>'*)
                                printf '%s\\n' "ERROR: Runtime Secret $K8S_RUNTIME_SECRET still contains a placeholder for key: $secret_key" >&2
                                printf '%s\\n' "Replace template values in backend-springboot/k8s/runtime-secret.env and re-apply the Secret." >&2
                                exit 1
                                ;;
                        esac
                    done
                '''
            }
        }

        stage('Production Deployment') {
            steps {
                script { env.FAILED_STAGE = 'Production Deployment' }
                dir(env.K8S_MANIFEST_DIR) {
                    sh '''
                        set -eu

                        kind load docker-image "$DB_IMAGE_TAG" --name "$KIND_CLUSTER"
                        kind load docker-image "$IMAGE_TAG" --name "$KIND_CLUSTER"
                        kubectl apply --namespace "$K8S_NAMESPACE" -f .
                        kubectl set image --namespace "$K8S_NAMESPACE" "deployment/ralsei-mssql" \
                            "ralsei-mssql=$DB_IMAGE_TAG"
                        kubectl rollout status --namespace "$K8S_NAMESPACE" "deployment/ralsei-mssql" \
                            --timeout=6m || true
                        kubectl set image --namespace "$K8S_NAMESPACE" "deployment/$K8S_DEPLOYMENT" \\
                            "$K8S_CONTAINER=$IMAGE_TAG"
                        # The startup probe permits up to five minutes for Spring Boot
                        # to initialize. The rollout timeout must not be shorter than
                        # the application's own allowed startup window.
                        if ! kubectl rollout status --namespace "$K8S_NAMESPACE" "deployment/$K8S_DEPLOYMENT" \\
                            --timeout=6m; then
                            printf '%s\\n' "ERROR: Deployment $K8S_DEPLOYMENT did not become ready. Pod diagnostics follow:" >&2
                            kubectl get pods --namespace "$K8S_NAMESPACE" \\
                                --selector=app.kubernetes.io/name="$K8S_DEPLOYMENT" -o wide >&2 || true
                            kubectl describe deployment --namespace "$K8S_NAMESPACE" "$K8S_DEPLOYMENT" >&2 || true

                            pod_names=$(kubectl get pods --namespace "$K8S_NAMESPACE" \\
                                --selector=app.kubernetes.io/name="$K8S_DEPLOYMENT" \\
                                --output=jsonpath='{range .items[*]}{.metadata.name}{" "}{end}' || true)
                            for pod_name in $pod_names; do
                                printf '%s\\n' "--- Describe pod: $pod_name ---" >&2
                                kubectl describe pod --namespace "$K8S_NAMESPACE" "$pod_name" >&2 || true
                                printf '%s\\n' "--- Current logs: $pod_name ---" >&2
                                kubectl logs --namespace "$K8S_NAMESPACE" "$pod_name" \\
                                    --all-containers --tail=200 >&2 || true
                                printf '%s\\n' "--- Previous logs: $pod_name ---" >&2
                                kubectl logs --namespace "$K8S_NAMESPACE" "$pod_name" \\
                                    --all-containers --previous --tail=200 >&2 || true
                            done
                            exit 1
                        fi
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
