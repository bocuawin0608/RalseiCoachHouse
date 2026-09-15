
pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    triggers {
        gitlab(
            triggerOnPush: true,
            triggerOnMergeRequest: false,
            triggerOnNoteRequest: false,
            triggerOnPipelineEvent: false,
            triggerOnAcceptedMergeRequest: false,
            triggerOnClosedMergeRequest: false,
            triggerOnApprovedMergeRequest: false,
            triggerOnBuildStatusChanged: false,
            branchFilterType: 'NameBasedFilter',
            includeBranchesSpec: 'main'
        )
    }

    environment {
        IMAGE = 'ralsei-coach-house-be:latest'
        CONTAINER = 'ralsei-be'
        PORT = '8000'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Maven') {
            steps {
                dir('backend-springboot') {
                    sh './mvnw clean package -DskipTests || mvn clean package -DskipTests'
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
