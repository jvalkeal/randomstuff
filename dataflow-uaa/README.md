UAA dataflow sample

```
$ ./build-all
$ docker-compose -f docker-compose-oauth.yml pull
$ docker-compose -f docker-compose-oauth.yml up --force-recreate
$ docker-compose -f docker-compose-oauth.yml down
```
There is one user `janne` with pw `janne`.

