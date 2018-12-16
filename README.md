This microservice provides metrics for your RHPAM (kie-server) instance.
Currently it's compatible with RHPAM 7.1.1.

It utilizes microprofile-jaxrs, microprofile-metrics and microprofile-config parts of the Eclipse Microprofile Spec

List of metrics:

| Metric Name   | Metric description |
| ------------- | ------------- |
| totalActiveProcessInstancesCount  | Returns number of total active process instances across all containers. Returns -1 in case of failed initialization with kie-server  |

This microservice reacts to following configuration properties:

| Property name  | Description  | Default Value
| ------------- | ------------- | ------------- |
| kie.server.url | self-explanatory  |http://localhost:8280/kie-server/services/rest/server |
| kie.server.username | self-explanatory | anton |   
| kie.server.password | self-explanatory | password1!|

These properties can be passed as:
 - System property
 - Environment variable
 - src/main/resources/META-INF/microprofile-config.properties

Example usage:

Provided you have a running kie-server instance, you can make use of this microservice as follows:

```bash
$ mvn clean package
$ java -Dswarm.port.offset=100 -Dkie.server.url=http://HOST:PORT/kie-server/services/rest/server -Dkie.server.username=someUser -Dkie.server.password=somePassword! -jar target/rhpam-metrics-thorntail.jar
$ curl -X GET  http://localhost:8180/metrics/application  -H 'Accept: application/json' 
```

Sample output:
```json
{
    "totalActiveProcessInstancesCount": 6
}
```

If your kie-server has not been online at the time of deploying this microservice, you can reconnect at any time by executing:

```bash
$ curl -X GET http://localhost:8180/appMetrics/reconnect  -H 'Accept: application/json' 
```
