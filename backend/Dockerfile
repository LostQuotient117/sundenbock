# Basis-Image mit OpenJDK 25
FROM openjdk:25-slim

# Maven installieren
ARG MAVEN_VERSION=3.9.4
RUN apt-get update && apt-get install -y curl unzip \
    && curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.zip -o maven.zip \
    && unzip maven.zip -d /opt \
    && rm maven.zip \
    && ln -s /opt/apache-maven-$MAVEN_VERSION /opt/maven

ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

WORKDIR /build