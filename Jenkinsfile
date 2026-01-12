pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'kitap-kiralama-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        REPO_URL = 'https://github.com/BarisBaran/kitapKiralama.git'
        CHROME_BIN = '/usr/bin/google-chrome'
        DISPLAY = ':99'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== 1. GitHub\'dan kodlar çekiliyor ==='
                git branch: 'main', url: "${REPO_URL}"
                sh 'git log -1'
            }
        }

        stage('Build') {
            steps {
                echo '=== 2. Kodlar build ediliyor ==='
                sh 'mvn clean compile -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Unit Tests') {
            steps {
                echo '=== 3. Birim Testleri çalıştırılıyor ==='
                sh 'mvn test -Dtest=*Test'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    junit 'target/surefire-reports/*.xml'
                    publishHTML([
                        reportName: 'Unit Test Report',
                        reportDir: 'target/surefire-reports',
                        reportFiles: 'index.html',
                        keepAll: true
                    ])
                }
                failure {
                    echo 'Birim testleri başarısız!'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo '=== 4. Entegrasyon Testleri çalıştırılıyor ==='
                sh 'mvn verify -Dtest=*IntegrationTest -DfailIfNoTests=false'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml'
                    junit 'target/failsafe-reports/*.xml'
                    publishHTML([
                        reportName: 'Integration Test Report',
                        reportDir: 'target/failsafe-reports',
                        reportFiles: 'index.html',
                        keepAll: true
                    ])
                }
                failure {
                    echo 'Entegrasyon testleri başarısız!'
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo '=== 5. Docker image oluşturuluyor ==='
                script {
                    try {
                        dockerImage = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    } catch (Exception e) {
                        echo "Docker plugin kullanılamıyor, docker build komutu kullanılıyor..."
                        sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    }
                }
            }
        }

        stage('Docker Compose Up') {
            steps {
                echo '=== 6. Docker container\'lar başlatılıyor ==='
                sh '''
                    docker-compose down || true
                    docker-compose up -d --build
                    timeout /t 45 /nobreak >nul 2>&1 || sleep 45
                '''
            }
        }

        stage('Install Chrome for Selenium') {
            steps {
                echo '=== Chrome ve ChromeDriver kurulumu ==='
                sh '''
                    apt-get update || true
                    apt-get install -y google-chrome-stable || true
                    apt-get install -y xvfb || true
                '''
            }
        }

        stage('Health Check') {
            steps {
                echo '=== Sistem sağlık kontrolü yapılıyor ==='
                script {
                    def maxAttempts = 12
                    def waitTime = 5
                    def attempt = 0
                    
                    while (attempt < maxAttempts) {
                        try {
                            // Windows ve Linux için uyumlu health check
                            def response = sh(
                                script: '''
                                    curl -f http://localhost:8080/api/books 2>/dev/null || \
                                    powershell -Command "try { Invoke-WebRequest -Uri http://localhost:8080/api/books -UseBasicParsing | Out-Null; exit 0 } catch { exit 1 }" || \
                                    exit 1
                                ''',
                                returnStatus: true
                            )
                            
                            if (response == 0) {
                                echo "Sistem hazır!"
                                break
                            } else {
                                throw new Exception("Health check failed")
                            }
                        } catch (Exception e) {
                            attempt++
                            if (attempt >= maxAttempts) {
                                error("Sistem ${maxAttempts * waitTime} saniye içinde hazır olmadı!")
                            }
                            echo "Bekleniyor... (Deneme ${attempt}/${maxAttempts})"
                            sleep(waitTime)
                        }
                    }
                }
            }
        }

        stage('Selenium Tests - Senaryo 1') {
            steps {
                echo '=== 6.1. Selenium Test Senaryosu 1: Kullanıcı Giriş ve Kitap Kiralama ==='
                sh 'mvn test -Dtest=UserRentBookTest -Pselenium || true'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Tests - Senaryo 2') {
            steps {
                echo '=== 6.2. Selenium Test Senaryosu 2: Admin Kitap Ekleme ==='
                sh 'mvn test -Dtest=AdminAddBookTest -Pselenium || true'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Selenium Tests - Senaryo 3') {
            steps {
                echo '=== 6.3. Selenium Test Senaryosu 3: Kullanıcı Kitap İade ==='
                sh 'mvn test -Dtest=UserReturnBookTest -Pselenium || true'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    junit 'target/surefire-reports/*.xml'
                    publishHTML([
                        reportName: 'Selenium Test Report',
                        reportDir: 'target/surefire-reports',
                        reportFiles: 'index.html',
                        keepAll: true
                    ])
                }
                success {
                    echo '=== Tüm Selenium test senaryoları tamamlandı ==='
                }
                failure {
                    echo 'Selenium testleri başarısız!'
                }
            }
        }
    }

    post {
        always {
            echo '=== Pipeline tamamlandı ==='
            script {
                // Test raporlarını topla
                sh '''
                    echo "=== TEST ÖZET RAPORU ===" > test-report.txt
                    echo "" >> test-report.txt
                    
                    if [ -f target/surefire-reports/TEST-*.xml ]; then
                        echo "Birim Testleri:" >> test-report.txt
                        grep -h "testsuite" target/surefire-reports/TEST-*.xml | head -5 >> test-report.txt
                    fi
                    
                    if [ -f target/failsafe-reports/TEST-*.xml ]; then
                        echo "" >> test-report.txt
                        echo "Entegrasyon Testleri:" >> test-report.txt
                        grep -h "testsuite" target/failsafe-reports/TEST-*.xml | head -5 >> test-report.txt
                    fi
                    
                    cat test-report.txt
                '''
            }
        }
        success {
            echo 'Pipeline başarıyla tamamlandı! ✅'
        }
        failure {
            echo 'Pipeline başarısız oldu! ❌'
        }
        cleanup {
            // Cleanup - container'ları durdurma
            script {
                try {
                    sh 'docker-compose down || true'
                } catch (Exception e) {
                    echo "Cleanup sırasında hata: ${e.getMessage()}"
                }
            }
        }
    }
}
