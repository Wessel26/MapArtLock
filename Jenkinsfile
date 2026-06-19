pipeline {
    agent any

    tools {
        jdk 'JDK-17'
        maven 'Maven'
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Archive plugin jar') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
}
