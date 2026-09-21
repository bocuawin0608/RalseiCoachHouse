pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    environment {
        IMAGE = 'ralsei-coach-house-be:latest'
        CONTAINER = 'ralsei-be'
        PORT = '8000'
        VERSION = '0.0.${BUILD_NUMBER}' // Sửa lại cú pháp string interpolation cho đúng
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
                echo 'unit test backend-springboot stage'
                dir('backend-springboot') {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Check fucked up code') {
            steps {
                script { env.FAILED_STAGE = 'Check fucked up code' }
                echo 'check fucked up code stage'
                dir('backend-springboot') {
                    sh 'mvn checkstyle:check'
                }
            }
        }

        stage('Security testing') {
            steps {
                script { env.FAILED_STAGE = 'Security testing' }
                dir('backend-springboot') {
                    sh 'semgrep scan --config=auto --json --output semgrep-report.json || true'
                }
            }
        }

        stage('Build Maven') {
            steps {
                script { env.FAILED_STAGE = 'Build Maven' }
                dir('backend-springboot') {
                    sh '''
                        if [ -f ./mvnw ]; then
                            chmod +x ./mvnw
                            ./mvnw clean package -DskipTests
                        else
                            mvn clean package -DskipTests
                        fi
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                script { env.FAILED_STAGE = 'Docker Build' }
                dir('backend-springboot') {
                    sh "docker build -t ${IMAGE} ."
                }
            }
        }

        stage('Deploy') {
            steps {
                script { env.FAILED_STAGE = 'Deploy' }
                sh """
                    docker rm -f ${CONTAINER} 2>/dev/null || true

                    docker run -d \
                        --name ${CONTAINER} \
                        --restart unless-stopped \
                        -p ${PORT}:8080 \
                        ${IMAGE}
                """
            }
        }

        stage('Performance Testing') {
            steps {
                script { env.FAILED_STAGE = 'Performance Testing' }
                dir('backend-springboot') {
                    script {
                        // Đợi app sống thật sự trên cổng 8000 (đã map ra ngoài) trước khi gọi k6
                        sh '''
                            echo "Waiting for backend to be up..."
                            for i in {1..30}; do
                              if curl -s http://localhost:8000/api/v1/trips/home?date=2026-09-24 > /dev/null; then
                                echo "Backend is up!"
                                break
                              fi
                              sleep 3
                            done
                        '''

                        // Chạy k6 với BASE_URL trỏ đúng vào cổng 8000
                        sh 'k6 run -e BASE_URL=http://127.0.0.1:8000/api -e K6_PROFILE=load load-test.js'
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment and Load Test Completed Successfully!'
            script {
                echo "External SMTP configuration: ${env.CI_EMAIL_CONFIG ?: '/etc/nhaxetuanmv-ci-email.env'}"
                writeFile file: 'ci-success-report.log', text: """\
Pipeline: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}
Status: SUCCESS
Build URL: ${env.BUILD_URL}
"""
                sh '''#!/usr/bin/env sh
                    ./email.sh "$JOB_NAME" "$BUILD_NUMBER" "$BUILD_URL" \
                      ci-success-report.log "Pipeline completed" SUCCESS || \
                      echo "WARNING: CI success email could not be sent."
                '''
            }
        }

        failure {
            echo 'Pipeline Failed!'
            script {
                echo "External SMTP configuration: ${env.CI_EMAIL_CONFIG ?: '/etc/nhaxetuanmv-ci-email.env'}"
                writeFile file: 'ci-failure-report.log', text: """\
Pipeline: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}
Failed stage: ${env.FAILED_STAGE ?: 'Unknown'}
Build URL: ${env.BUILD_URL}

Open the Jenkins console for the failed command and complete stack trace.
"""

                echo """
EMAIL NOTIFICATION TEST
Pipeline       : ${env.JOB_NAME}
Build          : #${env.BUILD_NUMBER}
Failed stage   : ${env.FAILED_STAGE ?: 'Unknown'}
Recipient route: email.sh will select the responsible role for this stage
Jenkins URL    : ${env.BUILD_URL}
"""

                sh '''#!/usr/bin/env sh
                    ./email.sh "$JOB_NAME" "$BUILD_NUMBER" "$BUILD_URL" \
                      ci-failure-report.log "$FAILED_STAGE" FAILED || \
                      echo "WARNING: CI failure email could not be sent."
                '''
            }
        }

        always {
            archiveArtifacts artifacts: 'backend-springboot/semgrep-report.json', fingerprint: true
            cleanWs()
        }
    }
}
