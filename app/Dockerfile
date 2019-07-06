FROM openjdk:8-jdk-alpine

# Add the defacto "wait for it" script from GitHub raw content.
ADD https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh wait-for-it.sh

# TODO: determine if `/tmp` is needed/wanted
VOLUME /tmp

COPY target/app*.jar /app.jar

RUN apk add --update bash && rm -rf /var/cache/apk/*
RUN bash -c 'touch /app.jar'
RUN bash -c 'chmod 777 /wait-for-it.sh'

ENV JAVA_OPTS ""
ENV SPRING_PROFILES_ACTIVE docker

EXPOSE 8081
EXPOSE 8000

ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar
