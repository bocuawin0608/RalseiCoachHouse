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
                    // Lệnh package của Maven tự động chạy test. 
                    // Chạy 1 lần duy nhất, lấy cả kết quả test lẫn file .jar cuối cùng.
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

        stage('Docker Build') {
            steps {
                script { env.FAILED_STAGE = 'Docker Build' }
                dir(env.BACKEND_DIR) {
                    // Đã thêm dấu chấm (.) để định vị build context
                    sh '''
                        set -eu
                        docker build --network=host --pull -t "$IMAGE_TAG" .                    '''
                }
            }
        }

        stage('Validate Kubernetes Manifests') {
            steps {
                script { env.FAILED_STAGE = 'Validate Kubernetes Manifests' }
                sh '''
                    set -eu
                    if [ ! -d "$K8S_MANIFEST_DIR" ]; then
                        printf '%s\n' "ERROR: Kubernetes manifests directory is missing." >&2
                        exit 1
                    fi
                    
                    # Logic kiểm tra secret của cậu được giữ nguyên vì nó hợp lệ
                    if ! kubectl get secret --namespace "$K8S_NAMESPACE" "$K8S_RUNTIME_SECRET" > /dev/null; then
                       printf '%s\n' "ERROR: Required runtime Secret is missing: $K8S_RUNTIME_SECRET" >&2
                       exit 1
                    fi
                '''
            }
        }

        stage('Deploy to Environment') {
            // Đổi tên vì Kind không bao giờ là Production
            steps {
                script { env.FAILED_STAGE = 'Deploy to Environment' }
                dir(env.K8S_MANIFEST_DIR) {
                    sh '''
                        set -eu
                        
                        kind load docker-image "$IMAGE_TAG" --name "$KIND_CLUSTER"
                        kubectl apply --namespace "$K8S_NAMESPACE" -f .
                        kubectl set image --namespace "$K8S_NAMESPACE" "deployment/$K8S_DEPLOYMENT" \
                            "$K8S_CONTAINER=$IMAGE_TAG"
                            
                        if ! kubectl rollout status --namespace "$K8S_NAMESPACE" "deployment/$K8S_DEPLOYMENT" \
                            --timeout=6m; then
                            printf '%s\n' "ERROR: Deployment failed." >&2
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
                        ./notify-report.sh SUCCESS || true
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo "Pipeline failed in stage: ${env.FAILED_STAGE ?: 'unknown'}"
            withCredentials([usernamePassword(
                credentialsId: 'jenkins-report-api',
                usernameVariable: 'JENKINS_REPORT_API_USER',
                passwordVariable: 'JENKINS_REPORT_API_TOKEN'
            )]) {
                sh '''
                    set -eu
                    ./notify-report.sh FAILED || true
                '''
            }
        }
        always {
            archiveArtifacts artifacts: 'backend-springboot/semgrep-report.json,jenkins-build-*-analytics.pdf', fingerprint: true, allowEmptyArchive: true
            cleanWs()
        }
    }
}