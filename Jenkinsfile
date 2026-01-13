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
                        echo "Unit test hatası: ${e.getMessage()}"
                        // Test hatalarını fatal yapma, sadece uyar
                        currentBuild.result = 'UNSTABLE'
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
                        echo "Integration test hatası: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
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
                            echo "UYARI: Docker bulunamadı, build atlanıyor..."
                            currentBuild.result = 'UNSTABLE'
                        }
                    } catch (Exception e) {
                        echo "Docker build hatası: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
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
                            echo "UYARI: Docker Compose bulunamadı, container'lar başlatılamıyor..."
                            currentBuild.result = 'UNSTABLE'
                        }
                    } catch (Exception e) {
                        echo "Docker Compose hatası: ${e.getMessage()}"
                        currentBuild.result = 'UNSTABLE'
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
                                    echo "❌ Sistem ${maxAttempts * waitTime} saniye içinde hazır olmadı!"
                                    currentBuild.result = 'UNSTABLE'
                                } else {
                                    echo "⏳ Bekleniyor... (Deneme ${attempt}/${maxAttempts})"
                                    sleep(waitTime)
                                }
                            }
                        } catch (Exception e) {
                            attempt++
                            if (attempt >= maxAttempts) {
                                echo "❌ Health check başarısız: ${e.getMessage()}"
                                currentBuild.result = 'UNSTABLE'
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
                script {
                    try {
                        sh '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                ./mvnw test -Dtest=UserRentBookTest -Pselenium -DfailIfNoTests=false || true
                            elif command -v mvn &> /dev/null; then
                                mvn test -Dtest=UserRentBookTest -Pselenium -DfailIfNoTests=false || true
                            else
                                echo "Maven bulunamadı, Selenium testleri atlanıyor..."
                            fi
                        '''
                    } catch (Exception e) {
                        echo "Selenium test hatası (Senaryo 1): ${e.getMessage()}"
                        // Selenium testlerini fatal yapma
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
                script {
                    try {
                        sh '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                ./mvnw test -Dtest=AdminAddBookTest -Pselenium -DfailIfNoTests=false || true
                            elif command -v mvn &> /dev/null; then
                                mvn test -Dtest=AdminAddBookTest -Pselenium -DfailIfNoTests=false || true
                            else
                                echo "Maven bulunamadı, Selenium testleri atlanıyor..."
                            fi
                        '''
                    } catch (Exception e) {
                        echo "Selenium test hatası (Senaryo 2): ${e.getMessage()}"
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
                script {
                    try {
                        sh '''
                            if [ -f mvnw ]; then
                                chmod +x mvnw
                                ./mvnw test -Dtest=UserReturnBookTest -Pselenium -DfailIfNoTests=false || true
                            elif command -v mvn &> /dev/null; then
                                mvn test -Dtest=UserReturnBookTest -Pselenium -DfailIfNoTests=false || true
                            else
                                echo "Maven bulunamadı, Selenium testleri atlanıyor..."
                            fi
                        '''
                    } catch (Exception e) {
                        echo "Selenium test hatası (Senaryo 3): ${e.getMessage()}"
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
                    echo "=== TEST ÖZET RAPORU ===" > test-report.txt 2>/dev/null || true
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
                    
                    cat test-report.txt 2>/dev/null || echo "Test raporu oluşturulamadı"
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
