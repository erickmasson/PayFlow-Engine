# 1 Build da aplicaçãÃo
FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# 2 Execução
FROM amazoncorretto:21-alpine
WORKDIR /app

# usuário sem privilégios de root
RUN addgroup -S payflow && adduser -S payflow -G payflow
USER payflow:payflow

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]