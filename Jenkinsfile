pipeline {
  agent any

  parameters {
    choice(name: "DEPLOY_ENV", choices: ["dev", "uat", "prod"], description: "Target environment")
    string(name: "VERSION_TAG", defaultValue: "${BUILD_NUMBER}", description: "Docker image version")
    booleanParam(name: "SKIP_TESTS", defaultValue: false, description: "Skip unit tests (use only for hotfixes)")
    booleanParam(name: "SKIP_SONAR", defaultValue: false, description: "Skip SonarQube scan")
  }

  environment {
    AWS_REGION   = "ap-south-1"
    ECR_REGISTRY = "453764757326.dkr.ecr.ap-south-1.amazonaws.com" 
    ECR_REPO     = "jmstechops/backend"
    IMAGE_TAG    = "${params.DEPLOY_ENV}-${params.VERSION_TAG}"
    SONAR_HOST   = "http://13.126.234.166:9000"
}
tools {
    maven "maven3"  
  }
  options {
    timeout(time: 30, unit: "MINUTES")
    buildDiscarder(logRotator(numToKeepStr: "10"))
    disableConcurrentBuilds()  // Prevent parallel builds causing conflicts
  }

  stages {
    stage("Checkout") {
      steps {
         git branch: 'master',
         credentialsId: 'github',
          url:'https://github.com/Kishore-SCM/spring3-mvc-maven-xml-hello-world.git'
        checkout scm
        echo "Building ${IMAGE_TAG} from commit ${GIT_COMMIT.take(7)}"
      }
    }

    stage("Maven Build") {
      steps {
        sh "mvn clean package ${params.SKIP_TESTS ? '-DskipTests=true' : ''} -B"
      }
    }

    stage("Unit Tests") {
      when { expression { !params.SKIP_TESTS } }
      steps {
        sh "mvn test -B"
      }
      post {
        always {
          junit "target/surefire-reports/*.xml"
          publishHTML(target: [reportName: "Test Report", reportDir: "target/site", reportFiles: "index.html"])
        }
      }
    }

    stage("SonarQube Scan") {
      when { expression { !params.SKIP_SONAR } }
      steps {
        withSonarQubeEnv("sonarqube") {
          sh "mvn sonar:sonar -Dsonar.projectKey=jmstechops-backend"
        }
      }
    }

    stage("Quality Gate") {
      when { expression { !params.SKIP_SONAR } }
      steps {
        timeout(time: 5, unit: "MINUTES") {
          waitForQualityGate abortPipeline: true
          // This BLOCKS the pipeline until Sonar analysis is complete
          // abortPipeline: true means FAIL the build if quality gate fails
        }
      }
    }

    stage("Docker Build") {
      steps {
        sh "docker build -t ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG} ."
      }
    }

    stage("Push to ECR") {
      steps {
        withAWS(credentials: "aws-credentials", region: "${AWS_REGION}") {
          sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"
          sh "docker push ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}"
        }
      }
    }
  }

  post {
    success { notifySlack("SUCCESS", "#00AA00") }
    failure  { notifySlack("FAILURE", "#AA0000") }
    always   { sh "docker rmi ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG} || true" }
  }
}

def notifySlack(status, color) {
  slackSend(channel: "#devops-alerts",
    color: color,
    message: "${status}: ${JOB_NAME} #${BUILD_NUMBER} | ${IMAGE_TAG} | ${BUILD_URL}")
}

