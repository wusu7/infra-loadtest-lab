pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/wusu7/infra-loadtest-lab.git'
            }
        }

        stage('Check k6') {
            steps {
                sh 'k6 version'
            }
        }

        stage('Run k6 Smoke Test') {
            steps {
                sh '''
                mkdir -p results
                k6 run k6/smoke-test.js \
                  --summary-export results/smoke-summary.json
                '''
            }
        }
    }

    post {
    always {
        script {
            if (fileExists('results/smoke-summary.json')) {
                archiveArtifacts artifacts: 'results/*.json', fingerprint: true
            } else {
                echo 'No result files to archive.'
            }
        }
    }
}