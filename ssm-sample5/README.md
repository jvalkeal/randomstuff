# Statemachine Locking Demo

Create database:
```shell
$ docker stop test-postgres && docker rm test-postgres
$ docker run --name test-postgres -d -p 5432:5432 -e POSTGRES_PASSWORD=spring -e POSTGRES_USER=spring -e POSTGRES_DB=sm postgres:10
$ docker exec -it test-postgres psql -U spring -d sm
```

Start once to create lock table(then kill process):
```shell
$ java -jar target/smlock-0.0.1-SNAPSHOT.jar --spring.profiles.active=postgres --spring.datasource.initializationMode=always
```

Then start multiple instances:
```shell
$ INSTANCE_INDEX=0 java -jar target/smlock-0.0.1-SNAPSHOT.jar --spring.profiles.active=postgres --server.port=8081
$ INSTANCE_INDEX=1 java -jar target/smlock-0.0.1-SNAPSHOT.jar --spring.profiles.active=postgres --server.port=8082
```

From instance 2, request states:
```shell
14:05 $ http :8082/state
HTTP/1.1 200
Connection: keep-alive
Content-Length: 325
Content-Type: text/plain;charset=UTF-8
Date: Sun, 07 Mar 2021 14:05:32 GMT
Keep-Alive: timeout=60

ObjectState [getIds()=[S1], getClass()=class org.springframework.statemachine.state.ObjectState, hashCode()=1653433525, toString()=AbstractState [id=S1, pseudoState=org.springframework.statemachine.state.DefaultPseudoState@46f9b0b4, deferred=[], entryActions=[], exitActions=[], stateActions=[], regions=[], submachine=null]]

```
From instance 1, switch between states:
```shell
$ http POST :8081/event?id=E1
$ http POST :8081/event?id=E2
```

Transition from S1 to S2 contains sleep, so while that happens, instance 2 should not be able to lock machine:

```shell
$ $ http :8082/state
HTTP/1.1 423
Connection: keep-alive
Content-Type: application/json
Date: Sun, 07 Mar 2021 14:33:52 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "error": "Locked",
    "message": "Unable to get locked machine test",
    "path": "/state",
    "status": 423,
    "timestamp": "2021-03-07T14:33:52.881+00:00"
}
```
