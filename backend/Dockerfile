# Multi-stage build для Spring Boot приложения
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости (кэшируется если pom.xml не изменился)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем приложение
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ - используем Debian-based образ для лучшей совместимости с ARM64
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Устанавливаем необходимые инструменты для healthcheck
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/*

# Создаем пользователя для запуска приложения
RUN groupadd -r spring && useradd -r -g spring spring

# Создаем директорию для логов ДО переключения на пользователя spring
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

# Копируем собранный JAR из stage build
COPY --from=build /app/target/*.jar app.jar

# Меняем владельца JAR файла
RUN chown spring:spring app.jar

# Переключаемся на пользователя spring
USER spring:spring

# Открываем порт
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
