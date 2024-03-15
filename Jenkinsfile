node("master") {
  def WORKSPACE = "/var/lib/jenkins/workspace/springboot-deploy"
  def dockerImageTag = "my_blogs${env.BUILD_NUMBER}"

  try {
    cleanWs()

    stage('Clone Repo') {
      git url: 'https://gitlab.com/tuanbeovnn/blog_v2.git',
        credentialsId: 'blogs',
        branch: 'main'
    }

    stage('Run check style') {
       sh '''
         export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/
         cp /.env src/main/resources/
         mvn checkstyle:check
       '''
    }

    stage('Testing') {
      sh '''
        export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64/
        mvn test
      '''
    }

    stage('Build docker') {
      sh "whoami"
      sh "DOCKER_BUILDKIT=1 docker build -t my_blogs:${env.BUILD_NUMBER} ."
    }

    stage('Deploy docker') {
      echo "Docker Image Tag Name: ${dockerImageTag}"
      sh "docker stop my_blogs || true && docker rm my_blogs || true"
      sh "docker run --name my_blogs -d -p 9091:8080 my_blogs:${env.BUILD_NUMBER}"
    }
  } catch (e) {
    currentBuild.result = 'FAILURE'
    throw e
  }
}