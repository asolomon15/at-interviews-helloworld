pipeline{
    agent any
    parameters {
        string(name: 'BUILD_ENV', defaultValue: 'DEV') // DEV/PROD varaibles passed in from the jenkins jobs
    }
    stages {

        // Perform the git clone of the hello world appliation 
        stage ("Git Clone") {
            steps {
                cleanWs()
                sh'"git" clone git@github.com:asolomon15/at-interviews-helloworld.git .'
            }
        }
        
        // Perform a setup and to determine which type of build this is. 
        stage('Setup') {
            steps {
                sh '"echo" THIS IS A ${BUILD_ENV} BUILD'
                sh '"aws" --profile at-interviews sts get-caller-identity'
                sh '"env"'
            }
        }
        
        // Perform the Docker build. 
        stage('Build') {
            environment {
                COMMIT_ID = """${sh(
                    script: 'git rev-parse --verify --short HEAD',
                    returnStdout: true
                )}"""
            }
            steps {
                sh '"echo" this is the variable ${COMMIT_ID}'
                sh '"docker" build --no-cache --build-arg GIT_COMMIT=${COMMIT_ID} -t helloworld:${BUILD_ENV}_${COMMIT_ID} -t 310228935478.dkr.ecr.us-west-2.amazonaws.com/helloworld:${BUILD_ENV}_${COMMIT_ID} .'
                sh '"env"'
            }
        }
        
        // Push the docker image to the docker repository in AWS
        stage('Push Image') {
            environment {
                COMMIT_ID = """${sh(
                    script: 'git rev-parse --verify --short HEAD',
                    returnStdout: true
                )}"""
            }
            steps {
                sh '"echo" Login to Elastic Container Repository'
                sh '"aws" ecr get-login-password --region us-west-2 | docker login --username AWS --password-stdin 310228935478.dkr.ecr.us-west-2.amazonaws.com'
                sh '"aws" eks update-kubeconfig --region us-west-2 --name at-interviews-cluster'
                sh '"echo" Pushing image to Repository'
                sh '"env"'
                sh '"docker" push 310228935478.dkr.ecr.us-west-2.amazonaws.com/helloworld:${BUILD_ENV}_${COMMIT_ID}'
            }
        }
        
        // Deploy the image Using Helm 
        stage('Deploy Image') {
            environment {
                COMMIT_ID = """${sh(
                    script: 'git rev-parse --verify --short HEAD',
                    returnStdout: true
                )}"""
            }
            steps {
                sh '"echo" Deploy image using Helm and Kubernetes'
                sh '"helm" upgrade --install --namespace $(whoami) --create-namespace helloworld --set image.tag=${BUILD_ENV}_${COMMIT_ID} helm/helloworld'
            }
        }
        
        // Start up Kubernetes 
        stage("Start up environment") {
            steps{
                sh '"kubectl" --namespace antoinesolomon get ingress'
                sh '"kubectl" --namespace antoinesolomon describe ingress helloworld'
            }
        }
    }
}
