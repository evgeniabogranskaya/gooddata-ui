# Customer Facing Audit Log

High level documentation:
[Customer Facing Audit Log](https://confluence.intgdc.com/display/plat/CFAL+-+Customer+Facing+Audit+Log)
including
[Event Catalog](https://confluence.intgdc.com/display/plat/CFAL+Event+Catalog),
[Operations Guide](https://confluence.intgdc.com/display/plat/CFAL+Operations+Guide),
[User Help](https://help.gooddata.com/display/doc/Auditing+Platform+Events),
[API Reference](https://help.gooddata.com/display/developer/API+Reference#/reference/audit-events).

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

### Run REST API Locally from IntelliJ IDEA

Sometime it might be useful to be able to run CFAL RestAPI directly from IDE. However because of current setup of the dependencies, you cannot simply run `com.gooddata.cfal.restapi.AuditlogApplication` (in case you try this, you'll get load of `ClassNotFoundeEception`s).

You can use Maven profile `local-development` to fix this and run CFAL REST API from IDE. 

### Developer's machine recommended setup

To ease you a first struggle with this app, these are recommended options you should have in `~/.spring-boot-devtools.properties`.

These will enable output to CONSOLE (instead of SYSLOG) and disable Graphite monitoring (so you won't have console filled with)
Graphite logger complaining about Graphite endpoints inaccessibility.

```
gdc.logging.appender=CONSOLE
monitoring.graphite.reporting-enabled=false
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

## CFAL SDK Monitoring

List of monitored metrics:

* `cfal.queue.size` - metric of `ConcurrentAuditLogService` representing size of internal queue 
* `cfal.queue.rejected.count` - metric of `ConcurrentAuditLogService` representing count of rejected items, because the queue was full
* `cfal.write.error.count` - metric representing count of events unable to be written to the log file
* `cfal.rotate.error.count` - metric representing how many times log file could not be rotated
* `cfal.log.call.count` - metric representing how many times `AuditLogService.logEvent` was called

CFAL SDK uses codehale metrics for monitoring. Registering `AuditLogService` and `AuditLogEventWriter` to application container (as beans)
and having properly setup graphite monitoring in component should be sufficient to start monitoring CFAL metrics.

## Enable CFAL for new component

1. Add Puppet/Hiera configuration

   Define new source log file for affected component node types (eg. `hieradata/type/cl_msf_restapi.yaml`, but don't forget to include it in `hieradata/type/rat.yaml`):
    ```
    ---
    fluentd::cfal_sources:
      msfrestapi:
        user: tomcat
    ```
    CFAL (Fluentd) will create and monitor `/mnt/log/cfal/msfrestapi.log` after that.

    Fluentd itself is enabled on all cluster and RAT.

2. Add SELinux rules

   There's a SELinux type `gdc_cfal_log_t` defined for accessing `/mnt/log/cfal/` logs called `gdc_cfal_log_t`, see [gdc-selinux](https://github.com/gooddata/gdc-selinux).

3. Add SGManager rules allowing to reach `cfal` subcluster from component's subcluster

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
`pollTimeoutMinutes`    | `5`                     | `-DpollTimeoutMinutes=5`
`sshKey`                | `~/.ssh/id_rsa`         | `-DsshKey=/path/to/key`
`sshKeyPass`            | _none_                  | `-DsshKeyPass=mykeypass`
`sshUser`               | OS user                 | `-DsshUser=martin.caslavsky`

#### Test using SSH

Skip them with `-DexcludeGroups=ssh`


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

