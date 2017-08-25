# Kubernetes

##How to push docker image to docker registry

Following commands will build `cfal-restapi`, prepare Docker image and push it to Docker registry, from where Helm can access it and install it to K8S cluster.
```bash
mvn clean package -T1C -DskipTests -Pk8s -am -pl cfal-restapi

docker build -t docker-registry.na.intgdc.com/gooddata/cfal-restapi:latest ./cfal-restapi/ 
docker push docker-registry.na.intgdc.com/gooddata/cfal-restapi:latest
```

##How to install it to K8S cluster

This sequence will delete old CFAL installation and install it from scratch. It's not the best thing you can do, but ... well ... 
```bash
cd k8s
helm_wrapper.sh del --purge cfal
helm_wrapper.sh install cfal/ --name cfal --namespace=cfal
```
