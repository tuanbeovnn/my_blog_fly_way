node {
  def WORKSPACE = "/var/lib/jenkins/workspace/springboot-deploy"
  def dockerImageTag = "backend_app_blog_v2${env.BUILD_NUMBER}"
  try {
    cleanWs()
    stage('Clone Repo') {
      git url: 'https://gitlab.com/tuanbeovnn/blog_v2.git',
        credentialsId: 'backend_app_blog_v2',
        branch: 'main'
    }

    stage('Testing') {
       withMaven(maven: 'maven') {
        sh "mvn test"

       }
    }
    stage('Build docker') {
      //dockerImage = docker.build("backend_app_blog:${env.BUILD_NUMBER}")
      sh "whoami"
      sh "DOCKER_BUILDKIT=1 docker build -t backend_app_blog_v2:${env.BUILD_NUMBER} ."
      //sh "docker build -t backend_app_blog:${env.BUILD_NUMBER} ."
    }
    stage('Deploy docker') {
      echo "Docker Image Tag Name: ${dockerImageTag}"
      sh "docker stop backend_app_blog_v2 || true && docker rm backend_app_blog_v2 || true"
      sh "docker run --name backend_app_blog_v2 -d -p 9090:8080 tuanquangnguyen1710/backend_app_blog_v2:backend_app_blog${env.BUILD_NUMBER}"
    }
  } catch (e) {
    currentBuild.result = 'FAILURE'
    throw e
  }
}
