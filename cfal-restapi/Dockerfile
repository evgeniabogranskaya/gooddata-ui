FROM docker-registry.na.intgdc.com/gooddata/gdc-java8:latest

LABEL name="CFAL restapi"
LABEL maintainer="Scrum Team Smurfs <smurfs@gooddata.com>"

ADD target/cfal-restapi.war cfal-restapi.war

ENTRYPOINT ["java","-jar","/cfal-restapi.war"]

EXPOSE 8080
