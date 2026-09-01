pipeline {
    agent any

    triggers {
        githubPush()
        pollSCM('H/15 * * * *')
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '30'))
        timeout(time: 1, unit: 'HOURS')
        timestamps()
    }

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk'
        PATH = "${JAVA_HOME}/bin:${PATH}"
        AWS_REGION = 'us-east-1'
        ECR_REGISTRY = credentials('aws-ecr-registry')
        DOCKER_REGISTRY = credentials('docker-registry-credentials')
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out code from repository..."
                    checkout scm
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    echo "Building application with Maven..."
                    sh 'mvn clean package -DskipTests -U'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    echo "Running unit tests..."
                    sh 'mvn test'
                }
            }
        }

        stage('Code Quality') {
            steps {
                script {
                    echo "Running SonarQube analysis..."
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn sonar:sonar -Dsonar.projectKey=booking-engine'
                    }
                }
            }
        }

        stage('Security Scan') {
            steps {
                script {
                    echo "Running security scans..."
                    sh 'mvn dependency-check:check'
                }
            }
        }

        stage('Build Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    echo "Building Docker image..."
                    sh 'docker build -t booking-engine:${BUILD_NUMBER} .'
                    sh 'docker tag booking-engine:${BUILD_NUMBER} ${ECR_REGISTRY}/booking-engine:${BUILD_NUMBER}'
                    sh 'docker tag booking-engine:${BUILD_NUMBER} ${ECR_REGISTRY}/booking-engine:latest'
                }
            }
        }

        stage('Push to ECR') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    echo "Pushing Docker image to ECR..."
                    sh '''
                        aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        docker push ${ECR_REGISTRY}/booking-engine:${BUILD_NUMBER}
                        docker push ${ECR_REGISTRY}/booking-engine:latest
                    '''
                }
            }
        }

        stage('Deploy to Dev') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    echo "Deploying to development environment..."
                    sh '''
                        aws eks update-kubeconfig --region ${AWS_REGION} --name booking-engine-cluster
                        kubectl set image deployment/booking-engine booking-engine=${ECR_REGISTRY}/booking-engine:${BUILD_NUMBER} -n dev
                        kubectl rollout status deployment/booking-engine -n dev --timeout=5m
                    '''
                }
            }
        }

        stage('Deploy to Prod') {
            when {
                branch 'main'
            }
            steps {
                input 'Deploy to Production?'
                script {
                    echo "Deploying to production environment..."
                    sh '''
                        aws eks update-kubeconfig --region ${AWS_REGION} --name booking-engine-cluster
                        kubectl set image deployment/booking-engine booking-engine=${ECR_REGISTRY}/booking-engine:${BUILD_NUMBER} -n production
                        kubectl rollout status deployment/booking-engine -n production --timeout=5m
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                echo "Cleaning up..."
                cleanWs()
            }
        }
        failure {
            script {
                echo "Build failed! Sending notifications..."
                // Add notification logic here (email, Slack, etc.)
            }
        }
        success {
            script {
                echo "Build successful!"
                // Add notification logic here
            }
        }
    }
}
