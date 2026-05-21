pipeline {
    agent any

    stages {

        stage('Maven Build') {
            steps {
                bat 'bat '"C:\\Users\\abhiram\\Downloads\\apache-maven-3.9.11-bin\\apache-maven-3.9.11\\bin\\mvn.cmd" clean install'
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