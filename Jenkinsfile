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
    environment {
        gradleOptions = "--no-daemon --info"
    }
    stages{
        stage('Clean') {
            steps {
                bat "./gradlew ${gradleOptions} clean"
            }
        }
        stage('Assemble') {
            steps {
                bat "./gradlew ${gradleOptions} assemble"
            }
        }
        stage('Test') {
            steps {
                bat "./gradlew ${gradleOptions} -DignoreFailures=true test"
            }
        }
    }
    post {
        always {
            junit "build/test-results/test/*.xml"
        }
    }
}
