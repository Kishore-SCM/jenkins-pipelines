def call(String projectKey) {
  stage("SonarQube Scan") {
    withSonarQubeEnv("sonarqube") {
      sh "mvn sonar:sonar -Dsonar.projectKey=${projectKey} -B"
    }
  }
  stage("Quality Gate") {
    timeout(time: 5, unit: "MINUTES") {
      waitForQualityGate abortPipeline: true
    }
  }
}

