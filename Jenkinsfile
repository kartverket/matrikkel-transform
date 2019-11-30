pipeline {
    agent any
    tools {
        jdk 'Java 8 Latest'
    }
    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }
    stages{
        stage('Bygg og test') {
            steps {
                bat "./gradlew --info clean assemble test"
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}
