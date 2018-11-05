FROM registry.becopay.com/devops/java-docker:latest
MAINTAINER jeus
ADD target/*.jar invoice.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/invoice.jar"]
EXPOSE 9193