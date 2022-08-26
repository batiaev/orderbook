FROM openjdk:17-alpine

ARG APP_NAME=orderbook
ARG JAR_FILE=./build/libs/${APP_NAME}-0.0.1-SNAPSHOT-all.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","app.jar"]
