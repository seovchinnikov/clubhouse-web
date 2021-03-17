FROM openjdk:11.0.10-jre-slim-buster
RUN apt-get update && apt-get install -y curl
# EXPOSE 8080/tcp

ARG APP_JAR_FILE
ENV JAVA_TOOL_OPTIONS="-Xmx1024m -Xss512k -XX:MetaspaceSize=64m"

RUN mkdir -p /apps

COPY ${APP_JAR_FILE} /apps/app.jar

ENTRYPOINT ["java", "-jar", "/apps/app.jar"]