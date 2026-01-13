#!/bin/bash
# Jenkins Sıfırdan Kurulum ve Başlatma Scripti
# Linux/Mac için Bash Script

echo "=== Jenkins Sıfırdan Kurulum ==="

# 1. Docker'ın çalıştığını kontrol et
echo ""
echo "1. Docker kontrol ediliyor..."
if docker version > /dev/null 2>&1; then
    echo "   ✅ Docker çalışıyor"
else
    echo "   ❌ Docker çalışmıyor! Lütfen Docker'ı başlatın."
    exit 1
fi

# 2. Mevcut Jenkins container'ını durdur ve kaldır
echo ""
echo "2. Eski Jenkins container'ları temizleniyor..."
docker stop jenkins 2>/dev/null
docker rm jenkins 2>/dev/null
docker stop kitap-kiralama-jenkins 2>/dev/null
docker rm kitap-kiralama-jenkins 2>/dev/null
echo "   ✅ Temizlik tamamlandı"

# 3. Jenkins volume'unu kontrol et (opsiyonel)
echo ""
echo "3. Jenkins volume kontrol ediliyor..."
JENKINS_VOLUME=$(docker volume ls -q | grep jenkins)
if [ ! -z "$JENKINS_VOLUME" ]; then
    echo "   ⚠️  Mevcut Jenkins volume bulundu: $JENKINS_VOLUME"
    read -p "   Jenkins verilerini silmek istiyor musunuz? (y/n): " response
    if [ "$response" = "y" ] || [ "$response" = "Y" ]; then
        docker volume rm $JENKINS_VOLUME 2>/dev/null
        echo "   ✅ Volume silindi"
    else
        echo "   ℹ️  Volume korunuyor (veriler saklanacak)"
    fi
fi

# 4. Jenkins container'ını başlat
echo ""
echo "4. Jenkins container'ı başlatılıyor..."
docker run -d \
    --name jenkins \
    --restart=unless-stopped \
    -p 8080:8080 \
    -p 50000:50000 \
    -v jenkins_home:/var/jenkins_home \
    -v /var/run/docker.sock:/var/run/docker.sock \
    jenkins/jenkins:lts

if [ $? -eq 0 ]; then
    echo "   ✅ Jenkins container başlatıldı"
else
    echo "   ❌ Jenkins container başlatılamadı!"
    exit 1
fi

# 5. Jenkins'in hazır olmasını bekle
echo ""
echo "5. Jenkins başlatılıyor, lütfen bekleyin..."
MAX_ATTEMPTS=30
ATTEMPT=0
IS_READY=false

while [ $ATTEMPT -lt $MAX_ATTEMPTS ] && [ "$IS_READY" = false ]; do
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
    
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "200\|403"; then
        IS_READY=true
        echo "   ✅ Jenkins hazır! ($ATTEMPT. deneme)"
    else
        echo "   ⏳ Bekleniyor... ($ATTEMPT/$MAX_ATTEMPTS)"
    fi
done

if [ "$IS_READY" = false ]; then
    echo "   ⚠️  Jenkins başlatma zaman aşımı, ancak container çalışıyor olabilir"
fi

# 6. Initial Admin Password'i göster
echo ""
echo "6. Jenkins Admin Şifresi alınıyor..."
sleep 5
PASSWORD=$(docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null)

if [ ! -z "$PASSWORD" ]; then
    echo ""
    echo "═══════════════════════════════════════════════════════════"
    echo "   JENKINS BAŞLATILDI!"
    echo "═══════════════════════════════════════════════════════════"
    echo ""
    echo "   🌐 Jenkins URL: http://localhost:8080"
    echo ""
    echo "   🔑 Initial Admin Password: $PASSWORD"
    echo ""
    echo "   📝 Bu şifreyi kopyalayın ve Jenkins kurulumunda kullanın!"
    echo "═══════════════════════════════════════════════════════════"
    echo ""
else
    echo "   ⚠️  Şifre henüz hazır değil, birkaç saniye sonra tekrar deneyin:"
    echo "   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword"
fi

# 7. Container durumunu göster
echo ""
echo "7. Container durumu:"
docker ps --filter "name=jenkins" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo ""
echo "✅ Jenkins kurulumu tamamlandı!"
echo ""
echo "📌 Sonraki adımlar:"
echo "   1. Tarayıcıda http://localhost:8080 adresine gidin"
echo "   2. Yukarıdaki şifreyi girin"
echo "   3. 'Install suggested plugins' seçeneğini seçin"
echo "   4. Admin kullanıcı oluşturun"
echo "   5. Jenkins'i kullanmaya başlayın!"
