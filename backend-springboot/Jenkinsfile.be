pipeline{
    agent any
    
    option{
        disableConcurrentBuilds()
        timestamps()
    }
    enviroment{
        IMAGE = 'ralsei-coach-house-be:latest'
        CONTAINNER = ralsei-be
        PORT = 8000
    }
    stage{

        stage(checkout){
            steps{
                checkout scm
            }
        }
        stage('Build Maven'){
            steps{
                dir('backend-springboot'){
                    sh 'mvn clean package -DskipTests'
                }
            }
        }
        stage('Docker Build'){
            steps{
                dir('backend-springboot'){
                    sh 'docker build -t $IMAGE .'
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sh '''
                    docker rm -f ${CONTAINER} 2>/dev/null || true

                    docker run -d \
                        --name ${CONTAINER} \
                        --restart unless-stopped \
                        -p ${PORT}:8080 \
                        ${IMAGE}
                '''
            }
        }
        post{
            success{
                echo 'Deployment Completed Successfully!'
            }
            failure{
                echo 'Deployment Failed at stage: Deploy'
            }
            cleanup{
                sh 'docker system prune -f || true'
            }
        }

    }
