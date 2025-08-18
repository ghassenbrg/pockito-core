pipeline {
  agent any

  environment {
    IMAGE_REPO = 'ghassenbrg/pockito-core'
    MAVEN_LOCAL = '.m2/repository'
  }

  options {
    timestamps()
    skipDefaultCheckout(true) // we do an explicit checkout stage
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Set Vars') {
      steps {
        script {
          // Sanitize branch for Docker tag (letters, digits, underscore, dot, hyphen)
          def safeBranch = (env.BRANCH_NAME ?: 'master').replaceAll('[^a-zA-Z0-9_.-]', '-')
          env.IMAGE_TAG = (safeBranch in ['master','main']) ? 'latest' : safeBranch
          echo "Docker image will be: ${env.IMAGE_REPO}:${env.IMAGE_TAG}"
        }
      }
    }

    stage('Build') {
      steps {
        // Ensure mvnw is executable; fall back to system mvn if wrapper missing
        sh 'chmod +x mvnw || true'
        sh "./mvnw -v || mvn -v"
        sh "./mvnw clean compile -Dmaven.repo.local=${MAVEN_LOCAL} || mvn clean compile -Dmaven.repo.local=${MAVEN_LOCAL}"
      }
    }

    stage('Test') {
      steps {
        sh "./mvnw test -Dmaven.repo.local=${MAVEN_LOCAL} || mvn test -Dmaven.repo.local=${MAVEN_LOCAL}"
      }
      post {
        always {
          junit '**/target/surefire-reports/*.xml'
        }
      }
    }

    stage('Package') {
      steps {
        sh "./mvnw -DskipTests package -Dmaven.repo.local=${MAVEN_LOCAL} || mvn -DskipTests package -Dmaven.repo.local=${MAVEN_LOCAL}"
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    stage('Docker Build') {
      steps {
        sh "docker build -t ${IMAGE_REPO}:${IMAGE_TAG} ."
      }
    }

    stage('Docker Push') {
      when {
        anyOf { branch 'master'; branch 'main'; branch 'develop' }
      }
      steps {
        script {
          // Use your existing String credential (token only)
          withCredentials([string(credentialsId: 'dockerhub-token', variable: 'DOCKERHUB_TOKEN')]) {
            def hubUser = env.DOCKER_HUB_USERNAME ?: 'ghassenbrg'
            sh "echo \"$DOCKERHUB_TOKEN\" | docker login -u ${hubUser} --password-stdin"

            sh "docker push ${IMAGE_REPO}:${IMAGE_TAG}"

            if (env.BRANCH_NAME in ['master','main']) {
              sh "docker tag ${IMAGE_REPO}:${IMAGE_TAG} ${IMAGE_REPO}:latest"
              sh "docker push ${IMAGE_REPO}:latest"
            }
          }
        }
      }
      post {
        always {
          sh 'docker logout || true'
        }
      }
    }
  }

  post {
    always {
      // Don’t fail the build if docker prune needs root or nothing to prune
      sh 'docker system prune -f || true'
      // Avoid cleanWs() plugin dependency
      deleteDir()
    }
    success {
      echo "Pipeline completed successfully for branch: ${env.BRANCH_NAME}"
    }
    failure {
      echo "Pipeline failed for branch: ${env.BRANCH_NAME}"
    }
  }
}
