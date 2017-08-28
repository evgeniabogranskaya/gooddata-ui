# What?

This directory contains means how to ease development on the user machine.

## Mongo & C4 mock
As all you need for querying CFAL restapi is MongoDB and C4 (mock), we've prepared a simple way how to setup your environment to get both of these services running. Simply use
```bash
docker-compose up
```
to boot necessary services. Once this finished, you can run CFAL restapi from command-line / IDE and enjoy happy development. Once you're done, you can kill both C4 mock and MongoDB using
```bash
docker-compose down
```

## C4 mock

One of the things you need to have for proper CFAL API testing is C4, as we're using it from our app (as it contains information about domains/users).

Current mock contains information about user with id `876ec68f5630b38de65852ed5d6236ff` and about `default` domain (where that user is domain admin).

### Docker
```bash
docker build -t c4-mock c4Mock/ && docker run -p 668:8080 c4-mock
```

### Bash
```bash
curl -O http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/2.7.1/wiremock-standalone-2.7.1.jar
sudo java -jar wiremock-standalone-2.7.1.jar --port 668 --root-dir c4Mock
```

### SSH tunnel to PI
In some situations you might prefer connection to existing C4, so in that case, you can you following command to get SSH tunnel to your favourite PI
```bash
ssh -L 668:localhost:668 -N -v stg3-c3
```
