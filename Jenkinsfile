pipeline {
    agent any

    stages {

        stage('Maven Build') {
            steps {
                bat 'mvn clean install'
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker build -t voltstore-app .'
            }
        }
    }
}