pipeline {
    agent any
    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }
    stages{
        stage('Bygg og test') {
            sh "./gradlew --info clean assemble test"
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}
