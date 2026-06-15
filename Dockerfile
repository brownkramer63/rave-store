FROM maven:3.9.9-eclipse-temurin-11 AS builder

#  Create a directory /app in the Container
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -Dmaven.test.skip=true package

FROM eclipse-temurin:11-jre
COPY --from=builder /app/target/cataclysm-solutions.*jar /cataclysm-solutions.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","cataclysm-solutions.jar"]
