# Customer Facing Audit Log

## Developer's machine recommended setup

To ease you a first struggle with this app, these are recommended options you should have in `~/.spring-boot-devtools.properties`.

These will enable output to CONSOLE (instead of SYSLOG) and disable Graphite monitoring (so you won't have console filled with)
Graphite logger complaining about Graphite endpoints inaccessibility.

```
gdc.logging.appender=CONSOLE
monitoring.graphite.reporting-enabled=false
```
## CFAL Event Catalog

All audited events are listed in [confluence](https://confluence.intgdc.com/display/plat/CFAL+Event+Catalog).

## Build
```
mvn package
```

## Run REST API Locally
```
mvn install -am -pl cfal-dto
cd cfal-restapi
mvn spring-boot:run
curl localhost:8080/info
```

## Release CFAL Java SDK
```
mvn release:prepare -DautoVersionSubmodules=true -DpushChanges=false
git push origin --tags
mvn release:perform
git checkout -b sdk
git push origin HEAD
```
and open pull request like https://github.com/gooddata/gdc-cfal/pull/80

## Enable CFAL for new component

Define new source log file for affected component node types (eg. `hieradata/type/cl_msf_restapi.yaml`, but don't forget to include it in `hieradata/type/rat.yaml`):
```
---
fluentd::cfal_sources:
  msfrestapi:
    user: tomcat
```
CFAL (Fluentd) will create and monitor `/mnt/log/cfal/msfrestapi.log` after that.

Fluentd itself is enabled on all cluster and RAT.

There's a SELinux type `gdc_cfal_log_t` defined for accessing `/mnt/log/cfal/` logs called `gdc_cfal_log_t`, see [gdc-selinux](https://github.com/gooddata/gdc-selinux).

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

parameter               | default                 | example
------------------------|-------------------------|--------
`host`                  | `localhost`             | `-Dhost=mcl-cfal.na.intgdc.com`
`user`                  | `bear@gooddata.com`     | `-Duser=bear@gooddata.com`
`pass`                  | `jindrisska`            | `-Dpass=jindrisska`
`projectId`             | `FoodMartDemo`          | `-DprojectId=FoodMartDemo`
`projectToken`          | _none_                  | `-DprojectToken=pgroup2`
`keepProject`           | `true` for FoodMartDemo | `-DprojectToken=pgroup2`
`datawarehouseToken`    | `vertica`               | `-DdatawarehouseToken=vertica`

### Manual tests

```
echo '{"domainId": "default", "test": 2, "userLogin": "bear@gooddata.com", "occurred": "2017-02-06T15:53:02.030Z", "userIp": "127.0.0.1", "success": true, "type": "STANDARD_LOGIN"}' >> /mnt/log/cfal/test.log
echo '{"domainId": "default", "test": 3, "userLogin": "jane@gooddata.com", "occurred": "2017-02-06T15:53:02.030Z", "userIp": "127.0.0.1", "success": true, "type": "STANDARD_LOGIN"}' >> /mnt/log/cfal/test.log

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
Use [EL 7 PI deploy job](https://ci.intgdc.com/job/deploy-instance-el7/build?delay=0sec)

parameter | value
----------|-------
`HARDWARE_TYPE` | `c2r10e200` (at least)
`TYPE`          | `rat`
`EXTRA`         | `rat_cfal=1,rat_msf_csv=0,rat_hds=1,components=msf`

`HIERA_OVERRIDES`:
```
cfg_cfal_enabled: true
cfg_rpm_vertica_version_el7: "%{hiera('vertica72_latest_el7')}"
cfg_rpm_vertica_odbc_version_el7: "%{hiera('vertica72_latest_el7')}"
cfg_rpm_gdc_vertica_udx_rfc4180csvparser_version_el7: "%{hiera('vertica72_udx_latest_el7')}"
cfg_rpm_gdc_vertica_udx_gdccsvparser_version_el7: "%{hiera('vertica72_udx_latest_el7')}"
```

Configuration for pipeline instance in [ci-infra](https://github.com/gooddata/ci-infra/blob/master/jenkins/jobs/ci/cfal-project.yaml).

## Update the PI

```
mvn verify
rsync -av --del --rsync-path="sudo rsync" cfal-restapi/target/cfal-restapi/ $INSTANCE:/usr/share/tomcat/webapps/cfal-restapi/
```

