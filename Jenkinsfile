pipeline {
    agent {
        node {
            label "matrikkel-utils"
        }
    }
    tools {
        jdk 'Java 11 Latest'
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
                sh "./gradlew ${gradleOptions} clean"
            }
        }
        stage('Assemble') {
            steps {
                sh "./gradlew ${gradleOptions} assemble"
            }
        }
        stage('Test') {
            steps {
                sh "./gradlew ${gradleOptions} -DignoreFailures=true test"
            }
        }
    }
    post {
        always {
            junit "build/test-results/test/*.xml"
        }
    }
}
