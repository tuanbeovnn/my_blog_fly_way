node("master") {
  def WORKSPACE = "/var/lib/jenkins/workspace/springboot-deploy"
  def currentVersionFile = "${WORKSPACE}/current_blog_version.txt"
  def dockerImageTag = ""

  try {
    cleanWs()

    stage('Clone Repo') {
      git url: 'https://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@github.com/tuanbeovnn/my_blog_fly_way.git',
        credentialsId: 'blogs',
        branch: 'dev'
    }

    stage('Run Checkstyle') {
      sh '''
        mvn checkstyle:check
      '''
    }

    // Uncomment this stage if you want to run tests in the future
    // stage('Testing') {
    //   sh '''
    //     mvn test
    //   '''
    // }

    stage('Calculate Docker Tag') {
      // Read the current version file or initialize to V.1.1.0
      if (fileExists(currentVersionFile)) {
        dockerImageTag = readFile(currentVersionFile).trim()
      } else {
        dockerImageTag = "V.1.1.0"
      }

      echo "Current Docker Tag: ${dockerImageTag}"

      // Split the current version into parts
      def versionParts = dockerImageTag.replace("V.", "").split("\\.")
      def major = versionParts[0].toInteger()
      def minor = versionParts[1].toInteger()
      def patch = versionParts[2].toInteger()

      // Increment the patch version
      patch++

      // If the patch exceeds 5, reset it and increment the minor version
      if (patch > 5) {
        patch = 0
        minor++
      }

      // Construct the new version
      dockerImageTag = "V.${major}.${minor}.${patch}"
      echo "New Docker Tag: ${dockerImageTag}"

      // Save the new version to the file
      writeFile(file: currentVersionFile, text: dockerImageTag)
    }

    stage('Build Docker') {
      sh "DOCKER_BUILDKIT=1 docker build -t my_blogs:${dockerImageTag} ."
    }

    stage('Deploy Docker') {
      echo "Docker Image Tag Name: ${dockerImageTag}"
      sh "docker stop my_blogs || true && docker rm my_blogs || true"
      sh "docker run --name my_blogs -d -p 9091:8080 my_blogs:${dockerImageTag}"
    }

    stage('Cleanup Old Images') {
      echo "Cleaning up old Docker images..."
      sh """
        docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" | grep "my_blogs:" | sort -r | awk 'NR>1 {print \$2}' | xargs -r docker rmi -f || true
      """
    }
  } catch (e) {
    currentBuild.result = 'FAILURE'
    throw e
  }
}