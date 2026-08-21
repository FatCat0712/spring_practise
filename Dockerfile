FROM openjdk:21-ea

ARG FILE_JAR=target/tayjava-sample-code-0.0.1-SNAPSHOT.jar

ADD ${FILE_JAR} api-service.jar

LABEL authors="sonhai"

ENTRYPOINT ["java", "-jar", "api-service.jar"]

EXPOSE 80