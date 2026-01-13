# Git ve Docker Compose Komutları

## Git Push Komutları

```powershell
# Tüm değişiklikleri ekle
git add .

# Commit et
git commit -m "Jenkins ve Docker Compose scriptleri eklendi"

# GitHub'a push et
git push origin main
```

**Tek satır:**
```powershell
git add . && git commit -m "Güncellemeler" && git push origin main
```

## Docker Compose Komutları

```powershell
# Container'ları başlat
docker compose up -d

# Container'ları durdur
docker compose down

# Container'ları yeniden başlat
docker compose restart

# Logları görüntüle
docker compose logs -f

# Container durumunu kontrol et
docker compose ps
```

**Tek satır başlatma:**
```powershell
docker compose up -d --build
```

## Jenkins Komutları

```powershell
# Jenkins'i başlat (Port 8081)
docker run -d --name jenkins --restart=unless-stopped -p 8081:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock jenkins/jenkins:lts

# Admin şifresini al
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Jenkins logları
docker logs jenkins -f

# Jenkins'i durdur
docker stop jenkins

# Jenkins'i kaldır
docker rm jenkins
```
