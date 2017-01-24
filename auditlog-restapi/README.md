Audit log rest api
==================


How to run it localy
--------------------

As we need to create `WAR` packages for our Tomcat deployment, we needed to set scope of the `spring-boot-starter-tomcat` to `provided`,
leaving us without obvious option to run application using `main` method from `com.gooddata.cfal.restapi.AuditlogApplication`.

One of the ways around it is to use ```mvn spring-boot:run``` to run this app.