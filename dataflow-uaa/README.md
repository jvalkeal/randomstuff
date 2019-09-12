UAA dataflow sample

```
$ ./build-all
$ docker-compose -f docker-compose-oauth.yml pull
$ docker-compose -f docker-compose-oauth.yml up --force-recreate
$ docker-compose -f docker-compose-oauth.yml down
```
There is one user `janne` with pw `janne`.


```
$ uaac target http://localhost:8080/uaa
$ uaac token client get uaa_admin -s fdejwhatgumb99x1ras3
```

