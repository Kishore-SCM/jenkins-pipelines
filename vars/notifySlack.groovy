def call(String status, String color, String channel = "#devops-alerts") {
  slackSend(
    channel: channel,
    color:   color,
    message: [
      "*${status}*: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
      "Branch: ${env.GIT_BRANCH}",
      "Commit: ${env.GIT_COMMIT?.take(7)}",
      "View: <${env.BUILD_URL}|Open in Jenkins>"
    ].join(" | ")
  )
}

