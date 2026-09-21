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
                    set -u
                   # The next commands consume an API token from a protected
                   # agent file. Do not expose expanded values in Jenkins logs.
                   set +x
                   report_config="\${JENKINS_REPORT_CONFIG:-/etc/nhaxetuanmv-jenkins-report.env}"
                    report_file="${reportFile}"
                    report_generated=false
                   if [ ! -r "\$report_config" ]; then
                      echo "REPORT ERROR: API configuration is not readable: \$report_config"
                    else
                      . "\$report_config"
                      if [ -z "\${JENKINS_API_URL:-}" ] || [ -z "\${JENKINS_API_USER:-}" ] || [ -z "\${JENKINS_API_TOKEN:-}" ]; then
                        echo "REPORT ERROR: JENKINS_API_URL, JENKINS_API_USER, and JENKINS_API_TOKEN are required."
                      else
                      [ -z "\${REPORT_PYTHON:-}" ] || export REPORT_PYTHON
                      echo "REPORT: Collecting Jenkins build data and creating ${reportFile}..."
                      if ./report.sh -u "\$JENKINS_API_URL" -j "\$JOB_NAME" -b "\$BUILD_NUMBER" \
                        -usr "\$JENKINS_API_USER" -t "\$JENKINS_API_TOKEN" -o "\$report_file" -s "${reportStatus}"; then
                        if [ -s "\$report_file" ]; then
                          report_generated=true
                          echo "REPORT: PDF created successfully: \$report_file"
                        else
                          echo "REPORT ERROR: Generator returned success but no PDF was created."
                        fi
                      else
                        echo "REPORT ERROR: PDF generation failed; see the report.sh output above."
                      fi
                      fi
                    fi

                    if [ "\$report_generated" = false ]; then
                      # Still send an alert when the reporting stack is broken.
                      # email.sh omits a missing attachment safely.
                      report_file=""
                    fi
                    echo "EMAIL: Sending ${reportStatus} notification (PDF attached: \$report_generated)..."
                    if ./email.sh "\$JOB_NAME" "\$BUILD_NUMBER" "\$BUILD_URL" \
                      "\$report_file" "Pipeline Analytics" "${reportStatus}"; then
                      echo "EMAIL: Notification sent successfully."
                    else
                      email_status=\$?
                      echo "EMAIL ERROR: Notification failed (exit \$email_status). See the SMTP error above."
                    fi
               """
            }
            archiveArtifacts artifacts: 'backend-springboot/semgrep-report.json,jenkins-build-*-analytics.pdf', fingerprint: true, allowEmptyArchive: true
            cleanWs()
        }
    }
}
