pipeline {
  agent any
  stages {
    stage('build and unit test') {
      steps {
        sh 'sh ./mvnw install'
      }
    }
  }
}