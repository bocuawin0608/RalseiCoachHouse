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
        GITOPS_REPO_URL = 'http://localhost/bocuawin0608/ralsei-gitops-config.git'
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
                        withCredentials([usernamePassword(credentialsId: 'gitops-credentials', usernameVariable: 'GITOPS_USERNAME', passwordVariable: 'GITOPS_TOKEN')]) {
                            sh '''
                                set -eu
                                rm -rf "$WORKSPACE/gitops"
                                git clone "http://${GITOPS_USERNAME}:${GITOPS_TOKEN}@172.18.0.2/bocuawin0608/ralsei-gitops-config.git" "$WORKSPACE/gitops"
                                cd "$WORKSPACE/gitops"
                                git checkout "$GITOPS_DEFAULT_BRANCH"
                                git config user.name "Jenkins CI"
                                git config user.email "jenkins@ralsei.local"

                                cd "$OVERLAY_PATH"
                                curl -s "https://raw.githubusercontent.com/kubernetes-sigs/kustomize/master/hack/install_kustomize.sh" | bash
                                ./kustomize edit set image ralsei/ralsei-coach-house-be="$IMAGE_TAG"
                                rm kustomize
                                cd ../../../

                                git add .
                                if git diff --cached --quiet; then
                                    echo "No GitOps change required."
                                    exit 0
                                fi

                                git commit -m "chore(ci): promote $IMAGE_TAG to $TARGET_ENV"
                                git push origin HEAD:"$GITOPS_DEFAULT_BRANCH"
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                withCredentials([
                string(
                    credentialsId: 'jenkins-env',
                    variable: 'JENKINS_ENV'
                )
            ]) {
                    sh '''
                    printf '%s\\n' "$JENKINS_ENV" > jenkins.env

                    source jenkins.env

                    curl -X POST \
                      -H 'Content-type: application/json' \
                      --data '{"text":"CI pipeline succeeded for fe-staff"}' \
                      "$SLACK_WEBHOOK_URL"

                    rm -f jenkins.env
                '''
            }
            }
        }

        failure {
            script {
                withCredentials([
                string(
                    credentialsId: 'jenkins-env',
                    variable: 'JENKINS_ENV'
                )
            ]) {
                    sh '''
                    printf '%s\\n' "$JENKINS_ENV" > jenkins.env

                    source jenkins.env

                    curl -X POST \
                      -H 'Content-type: application/json' \
                      --data '{"text":"CI pipeline failed for fe-staff"}' \
                      "$SLACK_WEBHOOK_URL"

                    rm -f jenkins.env
                '''
            }
            }
        }

        always {
            cleanWs()
        }
    }
}
