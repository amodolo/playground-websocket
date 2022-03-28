FROM maven:3.8-eclipse-temurin-11 AS build
WORKDIR /tmp
COPY src ./src
COPY pom.xml .
RUN mvn clean package -DskipTests

FROM tomcat:8.5-jre11-temurin-focal
WORKDIR $CATALINA_HOME/webapps/playground
COPY --from=build /tmp/target/playground ./