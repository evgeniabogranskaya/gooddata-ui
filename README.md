# Customer Facing Audit Log

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

### Manual tests

```
echo '{"domain": "default", "test":2, "userId": "876ec68f5630b38de65852ed5d6236ff", "realTimeOccurrence":"2017-02-06T15:53:02.030Z"}' >> /mnt/log/cfal/test.log
echo '{"domain": "default", "test":3, "userId": "015ba7e9b00ff2f1d1af252cf5bd29fb", "realTimeOccurrence":"2017-02-06T15:53:02.030Z"}' >> /mnt/log/cfal/test.log

curl localhost:8080/cfal-restapi/gdc/account/profile/015ba7e9b00ff2f1d1af252cf5bd29fb/events -H "X-GDC-PUBLIC-USER-ID: 015ba7e9b00ff2f1d1af252cf5bd29fb"
curl localhost:8080/cfal-restapi/gdc/account/profile/015ba7e9b00ff2f1d1af252cf5bd29fb/events -H "X-GDC-PUBLIC-USER-ID: 876ec68f5630b38de65852ed5d6236ff"
curl localhost:8080/cfal-restapi/gdc/domains/default/events -H "X-GDC-PUBLIC-USER-ID: 876ec68f5630b38de65852ed5d6236ff"
```


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
```

## Update the PI

```
mvn verify
rsync -av --del --rsync-path="sudo rsync" cfal-restapi/target/cfal-restapi/ $INSTANCE:/usr/share/tomcat/webapps/cfal-restapi/
```

