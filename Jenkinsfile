pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'ghassenbrg/pockito-core:1.0.0-SNAPSHOT'
        DOCKER_TAG = "${env.BRANCH_NAME == 'master' ? 'latest' : env.BRANCH_NAME}"
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                script {
                    if (fileExists('mvnw')) {
                        sh './mvnw clean compile -Dmaven.repo.local=.m2/repository'
                    } else {
                        sh 'mvn clean compile -Dmaven.repo.local=.m2/repository'
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    if (fileExists('mvnw')) {
                        sh './mvnw test -Dmaven.repo.local=.m2/repository'
                    } else {
                        sh 'mvn test -Dmaven.repo.local=.m2/repository'
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    if (fileExists('mvnw')) {
                        sh './mvnw package -DskipTests -Dmaven.repo.local=.m2/repository'
                    } else {
                        sh 'mvn package -DskipTests -Dmaven.repo.local=.m2/repository'
                    }
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    // Build Docker image
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }
        
        stage('Docker Push') {
            when {
                anyOf {
                    branch 'master'
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    // Use Docker Hub token for authentication
                    withCredentials([string(credentialsId: 'dockerhub-token', variable: 'DOCKERHUB_TOKEN')]) {
                        sh "echo \"$DOCKERHUB_TOKEN\" | docker login -u ${env.DOCKER_HUB_USERNAME ?: 'ghassenbrg'} --password-stdin"
                        
                        // Tag and push the image
                        sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        
                        // If it's master branch, also tag as latest
                        if (env.BRANCH_NAME == 'master') {
                            sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                            sh "docker push ${DOCKER_IMAGE}:latest"
                        }
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Clean up Docker images to save space
            sh 'docker system prune -f'
            
            // Clean up workspace
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully for branch: ${env.BRANCH_NAME}"
        }
        failure {
            echo "Pipeline failed for branch: ${env.BRANCH_NAME}"
        }
    }
}
