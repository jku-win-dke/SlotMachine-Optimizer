FROM adoptopenjdk/openjdk16:jre16u-alpine-nightly
LABEL maintainer=“jku”
WORKDIR /app
COPY target/optimizer-0.5.jar /app/optimizer.jar
ENTRYPOINT ["java","-jar","optimizer.jar"]