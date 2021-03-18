FROM adoptopenjdk/openjdk15:jre15u-alpine-nightly
LABEL maintainer=“jku”
WORKDIR /app
COPY target/optimizer-0.0.1.jar /app/optimizer.jar
ENTRYPOINT ["java","-jar","optimizer.jar"]