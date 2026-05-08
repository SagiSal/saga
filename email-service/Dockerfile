FROM maven:3.9.5-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY email-service/pom.xml email-service/pom.xml
COPY common/src common/src
COPY order-service/src order-service/src
COPY inventory-service/src inventory-service/src
COPY payment-service/src payment-service/src
COPY email-service/src email-service/src
RUN mvn -pl email-service -am -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/email-service/target/email-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
