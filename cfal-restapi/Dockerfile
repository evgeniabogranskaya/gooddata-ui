FROM docker-registry.na.intgdc.com/gooddata/gdc-java8:latest

LABEL name="CFAL restapi"
LABEL maintainer="Scrum Team HoneyBadgers <honeybadgers@gooddata.com>"

ADD target/cfal-restapi.jar cfal-restapi.jar

ENTRYPOINT ["java","-jar","/cfal-restapi.jar"]

EXPOSE 8080
