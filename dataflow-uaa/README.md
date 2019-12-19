UAA dataflow sample

```
$ ./build-all
$ docker-compose -f docker-compose-oauth.yml pull
$ docker-compose -f docker-compose-oauth.yml up --force-recreate
$ docker-compose -f docker-compose-oauth.yml down
```
There is user `janne` with pw `janne` as full access, and `guest` with pw `guest` as view access.


```
$ uaac target http://localhost:8080/uaa
$ uaac token client get uaa_admin -s fdejwhatgumb99x1ras3
```

Or using dataflow/skipper jars directly with uaa

```
java -jar <dataflow/skipper jar> --spring.config.additional-location <dataflow-uaa.yml/skipper-uaa.yml>
```
