FROM maven:3.9-eclipse-temurin-17-focal AS builder
WORKDIR /app

COPY pom.xml .
COPY ./api-contracts/pom.xml ./api-contracts/
COPY ./common-utils/pom.xml ./common-utils/
COPY ./notification-service/pom.xml ./notification-service/

RUN mvn clean install -pl api-contracts,common-utils -DskipTests

COPY ./api-contracts/src ./api-contracts/src
COPY ./common-utils/src ./common-utils/src
COPY ./notification-service/src ./notification-service/src

RUN mvn -pl notification-service -DskipTests package

FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/notification-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","app.jar"]

