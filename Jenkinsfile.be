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
                checkout scm
            }
        }

        stage('Backend Unit Test') {
            steps {
                echo 'unit test backend-springboot stage'
                dir('backend-springboot') {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Check fucked up code') {
            steps {
                echo 'check fucked up code stage'
                dir('backend-springboot') {
                    sh 'mvn checkstyle:check'
                }
            }
        }

        stage('Security testing') {
            steps {
                dir('backend-springboot') {
                    sh 'semgrep scan --config=auto --json --output semgrep-report.json || true'
                }
            }
        }

        stage('Build Maven') {
            steps {
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
                dir('backend-springboot') {
                    sh "docker build -t ${IMAGE} ."
                }
            }
        }

        stage('Deploy') {
            steps {
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
                dir('backend-springboot') {
                    script {
                        // Đợi app sống thật sự trên cổng 8000 (đã map ra ngoài) trước khi gọi k6
                        sh '''
                            echo "Waiting for backend to be up..."
                            for i in {1..30}; do
                              if curl -s http://127.0.0.1:8000/api/v1/trips/home?date=2026-09-24 > /dev/null; then
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
        }

        failure {
            echo 'Pipeline Failed!'
        }

        always {
            archiveArtifacts artifacts: 'backend-springboot/semgrep-report.json', fingerprint: true
            cleanWs()
        }
    }
}