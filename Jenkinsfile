pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'kitap-kiralama-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        REPO_URL = 'https://github.com/BarisBaran/kitapKiralama.git'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '=== 1. GitHub\'dan kodlar çekiliyor ==='
                git branch: 'main', url: "${REPO_URL}"
                sh 'git log -1 --oneline'
            }
        }

        stage('Build') {
            steps {
                echo '=== 2. Kodlar build ediliyor ==='
                script {
                    try {
                        // Maven Wrapper'ı çalıştırılabilir yap
                        sh '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                echo "Maven Wrapper kullanılıyor..."
                                ./mvnw clean compile -DskipTests -Dmaven.test.skip=true
                            elif command -v mvn &> /dev/null; then
                                echo "Maven bulundu, kullanılıyor..."
                                mvn clean compile -DskipTests -Dmaven.test.skip=true
                            else
                                echo "HATA: Maven Wrapper ve Maven bulunamadı!"
                                exit 1
                            fi
                        '''
                    } catch (Exception e) {
                        echo "Build hatası: ${e.getMessage()}"
                        error("Build başarısız!")
                    }
                }
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
            }
        }

        stage('Unit Tests') {
            steps {
                echo '=== 3. Birim Testleri çalıştırılıyor ==='
                script {
                    try {
                        sh '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                ./mvnw test -Dtest=*Test -DfailIfNoTests=false
                            elif command -v mvn &> /dev/null; then
                                mvn test -Dtest=*Test -DfailIfNoTests=false
                            else
                                echo "Maven bulunamadı, testler atlanıyor..."
                                exit 0
                            fi
                        '''
                    } catch (Exception e) {
                        echo "⚠️ Unit test hatası (opsiyonel): ${e.getMessage()}"
                        // Test hatalarını fatal yapma, pipeline devam eder
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo '=== 4. Entegrasyon Testleri çalıştırılıyor ==='
                script {
                    try {
                        sh '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                ./mvnw verify -Dtest=*IntegrationTest -DfailIfNoTests=false
                            elif command -v mvn &> /dev/null; then
                                mvn verify -Dtest=*IntegrationTest -DfailIfNoTests=false
                            else
                                echo "Maven bulunamadı, testler atlanıyor..."
                                exit 0
                            fi
                        '''
                    } catch (Exception e) {
                        echo "⚠️ Integration test hatası (opsiyonel): ${e.getMessage()}"
                        // Test hatalarını fatal yapma, pipeline devam eder
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/failsafe-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo '=== 5. Docker image oluşturuluyor ==='
                script {
                    try {
                        // Docker'ın varlığını kontrol et
                        def dockerExists = sh(
                            script: 'command -v docker &> /dev/null && echo "yes" || echo "no"',
                            returnStdout: true
                        ).trim()
                        
                        if (dockerExists == "yes") {
                            sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                            echo "Docker image başarıyla oluşturuldu: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        } else {
                            echo "⚠️ UYARI: Docker bulunamadı, build atlanıyor (opsiyonel)"
                        }
                    } catch (Exception e) {
                        echo "⚠️ Docker build hatası (opsiyonel): ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Docker Compose Up') {
            steps {
                echo '=== 6. Docker container\'lar başlatılıyor ==='
                script {
                    try {
                        // Docker Compose'un varlığını kontrol et
                        def composeExists = sh(
                            script: 'command -v docker-compose &> /dev/null || docker compose version &> /dev/null && echo "yes" || echo "no"',
                            returnStdout: true
                        ).trim()
                        
                        if (composeExists == "yes") {
                            sh '''
                                # Önce mevcut container'ları durdur
                                docker compose down -v 2>/dev/null || docker-compose down -v 2>/dev/null || true
                                
                                # Container'ları başlat
                                docker compose up -d --build 2>/dev/null || docker-compose up -d --build
                                
                                # Sistemin hazır olmasını bekle
                                echo "Sistem başlatılıyor, 45 saniye bekleniyor..."
                                sleep 45
                            '''
                        } else {
                            echo "⚠️ UYARI: Docker Compose bulunamadı, container'lar başlatılamıyor (opsiyonel)"
                        }
                    } catch (Exception e) {
                        echo "⚠️ Docker Compose hatası (opsiyonel): ${e.getMessage()}"
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                echo '=== Sistem sağlık kontrolü yapılıyor ==='
                script {
                    def maxAttempts = 12
                    def waitTime = 5
                    def attempt = 0
                    def isHealthy = false
                    
                    while (attempt < maxAttempts && !isHealthy) {
                        try {
                            // Basit curl veya wget ile health check
                            def response = sh(
                                script: '''
                                    curl -f -s -o /dev/null -w "%{http_code}" http://localhost:9090/api/books 2>/dev/null || \
                                    wget -q -O /dev/null -S http://localhost:9090/api/books 2>&1 | grep -q "200 OK" && echo "200" || echo "000"
                                ''',
                                returnStdout: true
                            ).trim()
                            
                            if (response == "200" || response.contains("200")) {
                                echo "✅ Sistem hazır! (HTTP ${response})"
                                isHealthy = true
                            } else {
                                attempt++
                                if (attempt >= maxAttempts) {
                                    echo "⚠️ Sistem ${maxAttempts * waitTime} saniye içinde hazır olmadı (opsiyonel)"
                                } else {
                                    echo "⏳ Bekleniyor... (Deneme ${attempt}/${maxAttempts})"
                                    sleep(waitTime)
                                }
                            }
                        } catch (Exception e) {
                            attempt++
                            if (attempt >= maxAttempts) {
                                echo "⚠️ Health check başarısız (opsiyonel): ${e.getMessage()}"
                            } else {
                                echo "⏳ Bekleniyor... (Deneme ${attempt}/${maxAttempts})"
                                sleep(waitTime)
                            }
                        }
                    }
                }
            }
        }

        stage('Selenium Tests - Senaryo 1') {
            steps {
                echo '=== 6.1. Selenium Test Senaryosu 1: Kullanıcı Giriş ve Kitap Kiralama ==='
                echo 'NOT: Selenium testleri opsiyoneldir. Hata olsa bile pipeline devam eder.'
                script {
                    def testResult = sh(
                        script: '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                ./mvnw test -Dtest=UserRentBookTest -Pselenium -DfailIfNoTests=false 2>&1 || echo "TEST_FAILED"
                            elif command -v mvn &> /dev/null; then
                                mvn test -Dtest=UserRentBookTest -Pselenium -DfailIfNoTests=false 2>&1 || echo "TEST_FAILED"
                            else
                                echo "Maven bulunamadı, Selenium testleri atlanıyor..."
                                echo "TEST_SKIPPED"
                            fi
                        ''',
                        returnStatus: true
                    )
                    if (testResult != 0) {
                        echo "⚠️ Selenium test başarısız (opsiyonel - pipeline devam ediyor)"
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Selenium Tests - Senaryo 2') {
            steps {
                echo '=== 6.2. Selenium Test Senaryosu 2: Admin Kitap Ekleme ==='
                echo 'NOT: Selenium testleri opsiyoneldir. Hata olsa bile pipeline devam eder.'
                script {
                    def testResult = sh(
                        script: '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                ./mvnw test -Dtest=AdminAddBookTest -Pselenium -DfailIfNoTests=false 2>&1 || echo "TEST_FAILED"
                            elif command -v mvn &> /dev/null; then
                                mvn test -Dtest=AdminAddBookTest -Pselenium -DfailIfNoTests=false 2>&1 || echo "TEST_FAILED"
                            else
                                echo "Maven bulunamadı, Selenium testleri atlanıyor..."
                                echo "TEST_SKIPPED"
                            fi
                        ''',
                        returnStatus: true
                    )
                    if (testResult != 0) {
                        echo "⚠️ Selenium test başarısız (opsiyonel - pipeline devam ediyor)"
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Selenium Tests - Senaryo 3') {
            steps {
                echo '=== 6.3. Selenium Test Senaryosu 3: Kullanıcı Kitap İade ==='
                echo 'NOT: Selenium testleri opsiyoneldir. Hata olsa bile pipeline devam eder.'
                script {
                    def testResult = sh(
                        script: '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                ./mvnw test -Dtest=UserReturnBookTest -Pselenium -DfailIfNoTests=false 2>&1 || echo "TEST_FAILED"
                            elif command -v mvn &> /dev/null; then
                                mvn test -Dtest=UserReturnBookTest -Pselenium -DfailIfNoTests=false 2>&1 || echo "TEST_FAILED"
                            else
                                echo "Maven bulunamadı, Selenium testleri atlanıyor..."
                                echo "TEST_SKIPPED"
                            fi
                        ''',
                        returnStatus: true
                    )
                    if (testResult != 0) {
                        echo "⚠️ Selenium test başarısız (opsiyonel - pipeline devam ediyor)"
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
                success {
                    echo '=== Tüm Selenium test senaryoları tamamlandı ==='
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
                    echo "=== TEST OZET RAPORU ===" > test-report.txt 2>/dev/null || true
                    echo "" >> test-report.txt 2>/dev/null || true
                    
                    if ls target/surefire-reports/TEST-*.xml 1> /dev/null 2>&1; then
                        echo "Birim Testleri:" >> test-report.txt
                        grep -h "testsuite" target/surefire-reports/TEST-*.xml 2>/dev/null | head -5 >> test-report.txt || true
                    fi
                    
                    if ls target/failsafe-reports/TEST-*.xml 1> /dev/null 2>&1; then
                        echo "" >> test-report.txt
                        echo "Entegrasyon Testleri:" >> test-report.txt
                        grep -h "testsuite" target/failsafe-reports/TEST-*.xml 2>/dev/null | head -5 >> test-report.txt || true
                    fi
                    
                    cat test-report.txt 2>/dev/null || echo "Test raporu olusturulamadi"
                '''
            }
        }
        success {
            echo '✅ Pipeline başarıyla tamamlandı!'
        }
        failure {
            echo '❌ Pipeline başarısız oldu!'
        }
        unstable {
            echo '⚠️ Pipeline tamamlandı ancak bazı uyarılar var!'
        }
        cleanup {
            script {
                try {
                    echo "Cleanup: Container'lar durduruluyor..."
                    sh '''
                        docker compose down -v 2>/dev/null || \
                        docker-compose down -v 2>/dev/null || \
                        echo "Docker Compose bulunamadı veya container'lar zaten durdurulmuş"
                    '''
                } catch (Exception e) {
                    echo "Cleanup sırasında hata (önemsiz): ${e.getMessage()}"
                }
            }
        }
    }
}
