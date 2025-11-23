pipeline {
    agent any
    
    tools {
        maven 'Maven'
        jdk 'JDK-11'
    }
    
    environment {
        SONAR_TOKEN = credentials('sonar-token')
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Clonant repositori...'
                checkout scm
            }
        }
        
        stage('Build & Tests') {
            steps {
                echo 'Compilant i executant tests...'
                bat 'mvn clean verify'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Coverage') {
            steps {
                echo 'Generant informe de cobertura...'
                bat 'mvn jacoco:report'
            }
            post {
                always {
                    publishHTML([
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'Jacoco Coverage Report'
                    ])
                    archiveArtifacts artifacts: 'target/site/jacoco/**/*', fingerprint: true
                }
            }
        }
        
        stage('SonarQube Scan') {
            steps {
                echo 'Analitzant qualitat del codi...'
                withSonarQubeEnv('SonarQube') {
                    bat "sonar-scanner -Dsonar.projectKey=petclinic-backend -Dsonar.login=${SONAR_TOKEN}"
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline executat amb èxit!'
        }
        failure {
            echo 'Pipeline ha fallat. Revisa els logs.'
        }
    }
}
