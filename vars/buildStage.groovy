def call(Map config = [:]) {
  stage("Maven Build") {
    sh "mvn clean package ${config.skipTests ? '-DskipTests=true' : ''} -B"
    archiveArtifacts artifacts: "target/*.jar", fingerprint: true
  }
}

