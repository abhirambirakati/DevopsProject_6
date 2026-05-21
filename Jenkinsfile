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

        stage('Deploy Container') {
            steps {

                bat 'docker stop voltstore-container || exit 0'

                bat 'docker rm voltstore-container || exit 0'

                bat 'docker run -d --name voltstore-container -p 9091:9091 voltstore-app'

            }
        }

    }
}