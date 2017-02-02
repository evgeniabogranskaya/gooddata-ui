# gdc-cfal
Customer Facing Audit Log

# How To
## Deploy a PI
Use [EL 7 PI deploy job](https://ci.intgdc.com/job/Deploy%20a%20developer%20instance%20via%20foreman%20(el7)/build?delay=0sec), set HARDWARE_TYPE to (at least) c2r10e200 and HIERA_OVERRIDES to 
```
fluentd::enabled: true

fluentd::plugins::out_forward_mongo::servers:
  hosts: ["localhost"]
```
