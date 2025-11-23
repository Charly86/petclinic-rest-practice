pipeline {
    agent any

    tools {
        maven 'my-maven' 
        jdk 'my-jdk'
    }

    environment {
        SONAR_SERVER_NAME = 'sonar-server'
    }

    stages {
        // 1. Baixar el codi del repositori [cite: 57]
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // 2. Compilar i executar tests [cite: 58]
        stage('Build & Tests') {
            steps {
                sh 'mvn clean verify'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        // 3. Generar i Arxivar Cobertura [cite: 59]
        stage('Coverage Report') {
            steps {
                sh 'mvn jacoco:report'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/site/jacoco/**/*', allowEmptyArchive: true
                    jacoco execPattern: '**/target/jacoco.exec',
                           classPattern: '**/target/classes',
                           sourcePattern: 'src/main/java',
                           inclusionPattern: '**/*.class'
                }
            }
        }

        // 4. SonarQube Scan [cite: 60]
        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv(SONAR_SERVER_NAME) {
                        sh 'mvn sonar:sonar'
                    }
                }
            }
        }

        // 5. Quality Gate (Valorat positivament) [cite: 66]
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
