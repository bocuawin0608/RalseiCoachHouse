pipeline {
    agent any

    options {
        disableConcurrentBuilds()
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changes = sh(
                        script: '''
                            git diff --name-only HEAD~1 HEAD
                        ''',
                        returnStdout: true
                    ).trim()

                    env.BE_CHANGED = changes.split('\n').any {
                        it.startsWith('backend-springboot/')
                    }.toString()

                    env.FE_CHANGED = changes.split('\n').any {
                        it.startsWith('frontend-react/')
                    }.toString()

                    env.STAFF_CHANGED = changes.split('\n').any {
                        it.startsWith('frontend-react-staff/')
                    }.toString()

                    echo "Backend changed: ${env.BE_CHANGED}"
                    echo "Customer FE changed: ${env.FE_CHANGED}"
                    echo "Staff FE changed: ${env.STAFF_CHANGED}"
                }
            }
        }

        stage('Trigger Services') {
            steps {
                script {
                    def jobs = [:]

                    if (env.BE_CHANGED == 'true') {
                        jobs['Backend'] = {
                            build job: 'ralsei-coach-house-be',
                                  wait: true,
                                  propagate: true
                        }
                    }

                    if (env.FE_CHANGED == 'true') {
                        jobs['Customer FE'] = {
                            build job: 'ralsei-coach-house-fe',
                                  wait: true,
                                  propagate: true
                        }
                    }

                    if (env.STAFF_CHANGED == 'true') {
                        jobs['Staff FE'] = {
                            build job: 'ralsei-coach-house-fe-staff',
                                  wait: true,
                                  propagate: true
                        }
                    }

                    if (jobs.isEmpty()) {
                        echo 'No application changes detected.'
                    } else {
                        parallel jobs
                    }
                }
            }
        }
    }
}