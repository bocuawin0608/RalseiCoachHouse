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
            post {
                always {
                    // Publishes structured JUnit data for the analytics API;
                    // this runs even when Maven reports failed tests.
                    junit allowEmptyResults: true, testResults: 'backend-springboot/target/surefire-reports/TEST-*.xml'
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
                        // Jenkins uses POSIX sh. Wait for a real HTTP response
                        // before k6 starts; a closed port must fail immediately.
                        sh '''
                            set -eu
                            max_attempts=30
                            attempt=1
                            echo "Waiting for backend to be reachable on port 8000..."
                            while [ "$attempt" -le "$max_attempts" ]; do
                              if curl --fail --silent --show-error --max-time 5 \
                                'http://127.0.0.1:8000/api/v1/trips/home?date=2026-09-24&page=0&size=1' \
                                > /dev/null; then
                                echo "Backend is ready."
                                exit 0
                              fi
                              echo "Backend is not ready (attempt $attempt/$max_attempts)."
                              attempt=$((attempt + 1))
                              sleep 3
                            done

                            echo "Backend did not become reachable on port 8000; skipping k6." >&2
                            docker ps -a --filter 'name=ralsei-be' >&2 || true
                            docker logs --tail 100 ralsei-be >&2 || true
                            exit 1
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
            script {
                def reportFile = "jenkins-build-${env.BUILD_NUMBER}-analytics.pdf"
                def reportStatus = currentBuild.currentResult ?: 'UNKNOWN'
                if (reportStatus == 'FAILURE') {
                    reportStatus = 'FAILED'
                }
                sh """#!/usr/bin/env sh
                    set -eu
                    # The next commands consume an API token from a protected
                    # agent file. Do not expose expanded values in Jenkins logs.
                    set +x
                    report_config="\${JENKINS_REPORT_CONFIG:-/etc/nhaxetuanmv-jenkins-report.env}"
                    if [ ! -r "\$report_config" ]; then
                      echo "WARNING: Jenkins report configuration is not readable: \$report_config"
                      exit 0
                    fi
                    . "\$report_config"
                    ./report.sh -u "\$JENKINS_API_URL" -j "\$JOB_NAME" -b "\$BUILD_NUMBER" \
                      -usr "\$JENKINS_API_USER" -t "\$JENKINS_API_TOKEN" -o "${reportFile}" -s "${reportStatus}" || {
                        echo "WARNING: Jenkins analytics PDF could not be generated."
                        exit 0
                      }
                    ./email.sh "\$JOB_NAME" "\$BUILD_NUMBER" "\$BUILD_URL" \
                      "${reportFile}" "Pipeline Analytics" "${reportStatus}" || \
                      echo "WARNING: Jenkins analytics email could not be sent."
                """
            }
            archiveArtifacts artifacts: 'backend-springboot/semgrep-report.json,jenkins-build-*-analytics.pdf', fingerprint: true, allowEmptyArchive: true
            cleanWs()
        }
    }
}
