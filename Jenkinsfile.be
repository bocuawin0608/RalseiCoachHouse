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
        VERSION = '0.0.{BUILD_NUMBER}'
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
                sh 'mvn clean test' 
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
    }

    post {
        success {
            echo 'Deployment Completed Successfully!'
        }

        failure {
            echo 'Deployment Failed!'
        }

        always {
            cleanWs()
        }
    }
}
