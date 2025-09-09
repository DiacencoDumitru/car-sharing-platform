FROM maven:3.9-eclipse-temurin-17-focal AS builder
WORKDIR /app

COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY user-service/pom.xml user-service/
COPY car-service/pom.xml car-service/
COPY booking-service/pom.xml booking-service/
COPY dispute-service/pom.xml dispute-service/
COPY common-utils/pom.xml common-utils/
COPY api-contracts/pom.xml api-contracts/
COPY api-gateway/pom.xml api-gateway/

RUN mvn -B -q -pl api-contracts,common-utils,api-gateway -am dependency:go-offline

COPY api-contracts/src api-contracts/src
COPY common-utils/src common-utils/src
COPY api-gateway/src api-gateway/src

RUN mvn -B -q -pl api-contracts,common-utils -am -DskipTests clean install
RUN mvn -B -q -pl api-gateway -am -DskipTests clean package

FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=builder /app/api-gateway/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","app.jar"]
