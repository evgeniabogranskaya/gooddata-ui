# FluentD Mongo Performance Test

Generates records and fetch them into the fluentd via tcp socket plugin. Records are then passed into Mongo DB. 
Script should be run on the host of the mongo database and fluentd host with out_mongo plugin. Records are generated 
for given range of domains randomly. Script also able to create indexes directly in mongo database.

## Run
Using python2.7

Default generation
```
python fluentd_perf_test_client.py
```
or with index creation
```
python fluentd_perf_test_client.py -i
```
help
```
python fluentd_perf_test_client.py -h
```

## Stop
Script is listenning SIGTERM or SIGINT signals in order to be stopped.

## Port and host configuration
Must be change in the script

Mongo host and port
```
mongo_host = 'localhost'
mongo_port = 27017
```
FluentD host and port
```
fluentd_host = 'localhost'
fluentd_port = 24224
```
