# Customer Facing Audit Log

## Developer's machine recommended setup

To ease you a first struggle with this app, these are recommended options you should have in `~/.spring-boot-devtools.properties`.

These will enable output to CONSOLE (instead of SYSLOG) and disable Graphite monitoring (so you won't have console filled with)
Graphite logger complaining about Graphite endpoints inaccessibility.

```
gdc.logging.appender=CONSOLE
monitoring.graphite.reporting-enabled=false
```

## Build
```
mvn package
```

## Tests

### Unit tests
```
mvn test
```

### Mocked tests
```
mvn verify
```

### Acceptance Tests
```
mvn -am -pl cfal-test -P at verify
```

parameter | default             | example
----------|---------------------|--------
`host`    | `localhost`         | `-Dhost=mcl-cfal.na.intgdc.com`
`user`    | `bear@gooddata.com` | `-Duser=bear@gooddata.com`
`pass`    | `jindrisska`        | `-Dpass=jindrisska`

### Manual tests

```
echo '{"domain": "default", "test":2, "userLogin": "bear@gooddata.com", "occurred":"2017-02-06T15:53:02.030Z"}' >> /mnt/log/cfal/test.log
echo '{"domain": "default", "test":3, "userLogin": "jane@gooddata.com", "occurred":"2017-02-06T15:53:02.030Z"}' >> /mnt/log/cfal/test.log

curl localhost:8080/cfal-restapi/gdc/account/profile/015ba7e9b00ff2f1d1af252cf5bd29fb/auditEvents -H "X-GDC-PUBLIC-USER-ID: 015ba7e9b00ff2f1d1af252cf5bd29fb"
curl localhost:8080/cfal-restapi/gdc/account/profile/015ba7e9b00ff2f1d1af252cf5bd29fb/auditEvents -H "X-GDC-PUBLIC-USER-ID: 876ec68f5630b38de65852ed5d6236ff"
curl localhost:8080/cfal-restapi/gdc/domains/default/auditEvents -H "X-GDC-PUBLIC-USER-ID: 876ec68f5630b38de65852ed5d6236ff"
```

### Mutation tests

These tests measure coverage by slightly changing existing source code and watching for tests failures.

More information about whole theory behind it: http://pitest.org/

These tests are being executed in `verify` phase automatically. Expected number of killed mutants is 70% (it's set in the `pom.xml`).
You can check reports in `target/pit-reports/{date_time_of_execution}` directory.

However if you want to skip these tests, you can do it by simply using `-P !mutation-tests`, so for example:
```
mvn clean verify -P '!mutation-tests'
```

## REST API Security

Actuator endpoints (except `/info`) are secured by basic-auth.

   user   |     password        |
----------|---------------------|
  `cfal`  |      `cfal`         |
  
### Example call of health endpoint
`
curl localhost:8080/cfal-restapi/health -u cfal:cfal
`

## Deploy a PI
Use [EL 7 PI deploy job](https://ci.intgdc.com/job/Deploy%20a%20developer%20instance%20via%20foreman%20(el7)/build?delay=0sec)

parameter | value
----------|-------
`HARDWARE_TYPE` | `c2r10e200` (at least)
`TYPE`          | `rat`
`EXTRA`         | `rat_cfal=1`

`HIERA_OVERRIDES`:
```
fluentd::enabled: true

fluentd::plugins::out_forward_mongo::servers:
  hosts: ["localhost"]

cfg_cfal_enabled: true
```

## Update the PI

```
mvn verify
rsync -av --del --rsync-path="sudo rsync" cfal-restapi/target/cfal-restapi/ $INSTANCE:/usr/share/tomcat/webapps/cfal-restapi/
```

