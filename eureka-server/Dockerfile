FROM maven:3.9-eclipse-temurin-17-focal AS builder
WORKDIR /app

COPY pom.xml .
COPY ./eureka-server/pom.xml ./eureka-server/
COPY ./user-service/pom.xml ./user-service/
COPY ./car-service/pom.xml ./car-service/
COPY ./booking-service/pom.xml ./booking-service/
COPY ./dispute-service/pom.xml ./dispute-service/
COPY ./common-utils/pom.xml ./common-utils/
COPY ./api-contracts/pom.xml ./api-contracts/

RUN mvn clean install -N

RUN mvn dependency:go-offline

COPY ./eureka-server/src ./eureka-server/src
COPY ./user-service/src ./user-service/src
COPY ./car-service/src ./car-service/src
COPY ./booking-service/src ./booking-service/src
COPY ./dispute-service/src ./dispute-service/src
COPY ./common-utils/src ./common-utils/src
COPY ./api-contracts/src ./api-contracts/src

RUN mvn clean package -pl eureka-server -DskipTests

FROM eclipse-temurin:17-jre-focal
WORKDIR /app

COPY --from=builder /app/eureka-server/target/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["java","-jar","app.jar"]