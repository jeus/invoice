FROM registry.becopay.com/devops/java-docker:latest
MAINTAINER
ADD target/*.jar invoice.jar
ENTRYPOINT ["/usr/bin/java"]
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/invoice.jar"]
VOLUME /var/lib/spring-cloud/config-repo
EXPOSE 9193