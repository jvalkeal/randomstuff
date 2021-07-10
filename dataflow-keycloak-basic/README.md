# Keycloak dataflow sample
This is a simple configuration example for making dataflow and skipper
to use `keycloak`.

NOTE: Keycloak configuration and use cases are quite opinionated
      meaning this sample should not be taken as is as it's a config
      which works end to end with minimal settings.

## Start Services via Docker Compose
Using embedded db.
```
docker-compose -f docker-compose.yml up --force-recreate
```

This will start `rabbitmq`, `skipper`, `dataflow`, `keycloak` and import container setting up keycloak.

## About Keycload Import Configs

There is user `admin` with password `admin` created.

In this docker compose we use tool https://github.com/adorsys/keycloak-config-cli to import configs into keycloak
so that we can get things running without manual configuration.

## Access dataflow
Open `http://localhost:9393/dashboard` which should initiate login via keycloak server.

## Run Compose Task
Settings for composed task in this run is:
```
app.composed-task-runner.oauth2-client-credentials-client-id=dataflow
app.composed-task-runner.oauth2-client-credentials-client-secret=secret
app.composed-task-runner.oauth2-client-credentials-scopes=openid,dataflow.view,dataflow.deploy,dataflow.destroy,dataflow.manage,dataflow.modify,dataflow.schedule,dataflow.create
app.composed-task-runner.oauth2-client-credentials-token-uri=http://keycloak-server:8080/auth/realms/dataflow/protocol/openid-connect/token
```

Here's a full recording with launching composed task using 1min token expire with custom settings.

![Recording](images/scdf-keycloak-task-short-token-lifespan.gif)
