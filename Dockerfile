# Multi-stage build için Dockerfile
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Maven bağımlılıklarını kopyala (cache için)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kaynak kodunu kopyala
COPY src ./src

# Uygulamayı build et (test derlemesini de atla)
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Build edilen JAR dosyasını kopyala
COPY --from=build /app/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/books || exit 1

# Port
EXPOSE 8080

# Uygulamayı çalıştır
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
