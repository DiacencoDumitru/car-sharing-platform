FROM maven:3.9-eclipse-temurin-17-focal AS builder
WORKDIR /app

COPY pom.xml .
COPY spotbugs-exclude.xml .
COPY shared/api-contracts/pom.xml shared/api-contracts/
COPY shared/common-utils/pom.xml shared/common-utils/
COPY services/eureka-server/pom.xml services/eureka-server/
COPY services/user-service/pom.xml services/user-service/
COPY services/car-service/pom.xml services/car-service/
COPY services/booking-service/pom.xml services/booking-service/
COPY services/dispute-service/pom.xml services/dispute-service/
COPY services/notification-service/pom.xml services/notification-service/
COPY services/api-gateway/pom.xml services/api-gateway/

RUN mvn clean install -N

RUN mvn dependency:go-offline

COPY shared/api-contracts/src shared/api-contracts/src
COPY shared/common-utils/src shared/common-utils/src
COPY services/notification-service/src services/notification-service/src

RUN mvn clean install -pl shared/api-contracts,shared/common-utils -DskipTests
RUN mvn -pl services/notification-service -DskipTests package

FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/services/notification-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java","-jar","app.jar"]
