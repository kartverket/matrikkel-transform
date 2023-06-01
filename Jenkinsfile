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
        stage("Publish GitHub") {
            when {
                branch 'master'
            }
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'matrikkel-pat-github',
                        usernameVariable: 'GITHUB_USER',
                        passwordVariable: 'GITHUB_TOKEN')
                ]) {
                    sh "./gradlew ${gradleOptions} publish --init-script gradle/scripts/mavenPublishGitHub.gradle"
                }
            }
        }
        stage("Publish Nexus") {
            when {
                branch 'master'
            }
            steps {
                withCredentials([usernamePassword(
                        credentialsId: 'nexusdeploy',
                        usernameVariable: 'DEPLOYUSER_USR',
                        passwordVariable: 'DEPLOYUSER_PSW')
                ]) {
                    sh "./gradlew ${gradleOptions} publish --init-script gradle/scripts/mavenPublish.gradle "
                }
            }
        }
    }
    post {
        always {
            junit "build/test-results/test/*.xml"
        }
    }
}
