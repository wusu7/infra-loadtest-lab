pipeline {
    // Jenkins agent가 있는 임의의 실행 노드에서 파이프라인을 수행합니다.
    agent any

    stages {
        // Jenkins workspace에 GitHub 저장소의 main 브랜치를 가져옵니다.
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/wusu7/infra-loadtest-lab.git'
            }
        }

        // Jenkins 서버에 k6가 설치되어 있고 실행 가능한지 먼저 확인합니다.
        stage('Check k6') {
            steps {
                sh 'k6 version'
            }
        }

        // 외부 샘플 사이트로 아주 짧은 smoke test를 실행해 k6 기본 동작을 확인합니다.
        stage('Run k6 Smoke Test') {
            steps {
                sh '''
                    mkdir -p results
                    k6 run k6/smoke-test.js \
                      --summary-export results/smoke-summary.json
                '''
            }
        }

        // Target 서버의 Spring Boot API를 호출해 Redis incr와 Kafka publish 흐름을 검증합니다.
        stage('Run Redis Kafka API Load Test') {
            steps {
                sh '''
                    mkdir -p results
                    K6_PROMETHEUS_RW_SERVER_URL=http://172.31.37.123:9090/api/v1/write \
                    K6_PROMETHEUS_RW_TREND_AS_NATIVE_HISTOGRAM=true \
                    K6_PROMETHEUS_RW_PUSH_INTERVAL=5s \
                    k6 run k6/api-load-test.js \
                      -e BASE_URL=http://172.31.37.123:8080 \
                      -o experimental-prometheus-rw \
                      --tag test=redis-kafka-api \
                      --tag run_id=${BUILD_NUMBER} \
                      --stage 30s:100 \
                      --stage 1m:300 \
                      --stage 2m:500 \
                      --stage 2m:700 \
                      --stage 2m:700 \
                      --stage 1m:200 \
                      --stage 30s:0 \
                      --summary-export results/api-load-summary.json
                '''
            }
        }
    }

    post {
        // 성공/실패와 관계없이 결과 JSON이 있으면 Jenkins artifact로 보관합니다.
        always {
            script {
                if (fileExists('results')) {
                    archiveArtifacts artifacts: 'results/*.json', fingerprint: true
                } else {
                    echo 'No result files to archive.'
                }
            }
        }
    }
}
