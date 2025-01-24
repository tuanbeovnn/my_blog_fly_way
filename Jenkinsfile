pipeline {
    agent { label 'master' }

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['dev', 'prod'], description: 'Select the environment for deployment')
    }

    environment {
        WORKSPACE = "/var/lib/jenkins/workspace/springboot-deploy"
        SPRING_CONFIG_FILE = "application-${params.ENVIRONMENT}.yaml"
        CONTAINER_NAME = "my_blogs_${params.ENVIRONMENT}"
    }

    stages {
        stage('Set Environment-Specific Variables') {
            steps {
                script {
                    // Set port dynamically based on the environment
                    if (params.ENVIRONMENT == 'dev') {
                        env.CONTAINER_PORT = '9095'
                    } else if (params.ENVIRONMENT == 'prod') {
                        env.CONTAINER_PORT = '9091'
                    }
                    echo "Environment: ${params.ENVIRONMENT}, Port: ${env.CONTAINER_PORT}"
                }
            }
        }

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Clone Repo') {
            steps {
                script {
                    // Determine the branch to clone based on the environment
                    if (params.ENVIRONMENT == 'prod') {
                        echo "Cloning the 'main' branch"
                        git url: 'https://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@github.com/tuanbeovnn/my_blog_fly_way.git',
                            credentialsId: 'blogs',
                            branch: 'main'
                    } else if (params.ENVIRONMENT == 'dev') {
                        echo "Cloning the 'dev' branch"
                        git url: 'https://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@github.com/tuanbeovnn/my_blog_fly_way.git',
                            credentialsId: 'blogs',
                            branch: 'dev'
                    } else {
                        error("Unknown environment: ${params.ENVIRONMENT}. Please select 'dev' or 'prod'.")
                    }
                }
            }
        }

        stage('Run Checkstyle') {
            steps {
                sh 'mvn checkstyle:check'
            }
        }

        stage('Calculate Docker Tag') {
            steps {
                script {
                    // Define the path to the version file
                    def currentVersionFile = "${env.WORKSPACE}/current_blog_version_${params.ENVIRONMENT}.txt"

                    // Initialize or read the current version file
                    if (fileExists(currentVersionFile)) {
                        dockerImageTag = readFile(currentVersionFile).trim()
                    } else {
                        dockerImageTag = "V.1.1.0"
                    }

                    echo "Current Docker Tag: ${dockerImageTag}"

                    // Increment the version number
                    def versionParts = dockerImageTag.replace("V.", "").split("\\.")
                    def major = versionParts[0].toInteger()
                    def minor = versionParts[1].toInteger()
                    def patch = versionParts[2].toInteger()

                    patch++
                    if (patch > 5) {
                        patch = 0
                        minor++
                    }

                    dockerImageTag = "V.${major}.${minor}.${patch}"
                    echo "New Docker Tag: ${dockerImageTag}"

                    // Save the new version back to the file
                    writeFile(file: currentVersionFile, text: dockerImageTag)
                }
            }
        }

        stage('Build Docker') {
            steps {
                script {
                    sh """
                    DOCKER_BUILDKIT=1 docker build -t my_blogs:${params.ENVIRONMENT}-${dockerImageTag} .
                    """
                }
            }
        }

        stage('Deploy Docker') {
            steps {
                script {
                    // Inject credentials securely
                    withCredentials([usernamePassword(credentialsId: 'db-credentials-id', usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')]) {
                        echo "Deploying to environment: ${params.ENVIRONMENT}"
                        echo "Using configuration file: ${env.SPRING_CONFIG_FILE}"

                        // Stop and remove the existing container
                        sh """
                        docker stop ${env.CONTAINER_NAME} || true && docker rm ${env.CONTAINER_NAME} || true
                        """

                        // Run the Docker container with injected environment variables
                        sh """
                        docker run --name ${env.CONTAINER_NAME} -d -p ${env.CONTAINER_PORT}:8080 \\
                            -v ${env.WORKSPACE}/${env.SPRING_CONFIG_FILE}:/app/config/application.yaml \\
                            -e spring.profiles.active=${params.ENVIRONMENT} \\
                            -e DB_USERNAME=${DB_USERNAME} \\
                            -e DB_PASSWORD=${DB_PASSWORD} \\
                            my_blogs:${params.ENVIRONMENT}-${dockerImageTag}
                        """
                    }
                }
            }
        }

        stage('Cleanup Old Images') {
            steps {
                script {
                    echo "Cleaning up old Docker images for environment: ${params.ENVIRONMENT}..."
                    sh """
                    docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" | grep "my_blogs:${params.ENVIRONMENT}" | sort -r | awk 'NR>1 {print \$2}' | xargs -r docker rmi -f || true
                    """
                }
            }
        }
    }

    post {
        failure {
            echo "Build failed!"
        }
        success {
            echo "Build and deployment succeeded!"
        }
    }
}