# Keycloak dataflow samples
These are a simple configuration examples for making dataflow and skipper
to use `keycloak`.

NOTE: Keycloak configuration and use cases are quite opinionated
      meaning this sample should not be taken as is as it's a config
      which works end to end with minimal settings.

## Sample 1
Meant to show that short token lifetime works when `composed-task-runner` is configured correctly.

### Start Services via Docker Compose
Using embedded db.
```
docker-compose -f docker-compose-1.yml up --force-recreate
```

This will start `rabbitmq`, `skipper`, `dataflow`, `keycloak` and import container setting up keycloak.

### About Keycload Import Configs

There is user `admin` with password `admin` created.

In this docker compose we use tool https://github.com/adorsys/keycloak-config-cli to import configs into keycloak
so that we can get things running without manual configuration.

### Access dataflow
Open `http://localhost:9393/dashboard` which should initiate login via keycloak server.

### Run Compose Task
Settings for composed task in this run is:
```
app.composed-task-runner.oauth2-client-credentials-client-id=dataflow
app.composed-task-runner.oauth2-client-credentials-client-secret=secret
app.composed-task-runner.oauth2-client-credentials-scopes=openid,dataflow.view,dataflow.deploy,dataflow.destroy,dataflow.manage,dataflow.modify,dataflow.schedule,dataflow.create
app.composed-task-runner.oauth2-client-credentials-token-uri=http://keycloak-server:8080/auth/realms/dataflow/protocol/openid-connect/token
```

Here's a full recording with launching composed task using 1min token expire with custom settings.

![Recording](images/scdf-keycloak-task-short-token-lifespan.gif)

## Sample 2
Meant to show that _client id_ with colons works and shell can connect to environment.
As we're only interested client layer, `skipper` doesn't enable auth.

### Start Services via Docker Compose
Using embedded db.
```
docker-compose -f docker-compose-2.yml up --force-recreate
```

This will start `rabbitmq`, `skipper`, `dataflow`, `keycloak` and import container setting up keycloak.

### About Keycload Import Configs

There is user `admin` with password `admin` created.

In this docker compose we use tool https://github.com/adorsys/keycloak-config-cli to import configs into keycloak
so that we can get things running without manual configuration.

We do not use `issuer-uri` config to discover keycloak settings because then we'd get into trouble
with hostnames as then you'd need to modify `/etc/hosts` and this allow us to access using localhost.

### Access dataflow
Open `http://localhost:9393/dashboard` which should initiate login via keycloak server.

### Access with shell

```
java -jar spring-cloud-dataflow-shell-2.9.0-SNAPSHOT.jar \
  --spring.config.additional-location=shell-2.yml \
  --dataflow.username=admin \
  --dataflow.password=admin
```

```
  ____                              ____ _                __
 / ___| _ __  _ __(_)_ __   __ _   / ___| | ___  _   _  __| |
 \___ \| '_ \| '__| | '_ \ / _` | | |   | |/ _ \| | | |/ _` |
  ___) | |_) | |  | | | | | (_| | | |___| | (_) | |_| | (_| |
 |____/| .__/|_|  |_|_| |_|\__, |  \____|_|\___/ \__,_|\__,_|
  ____ |_|    _          __|___/                 __________
 |  _ \  __ _| |_ __ _  |  ___| | _____      __  \ \ \ \ \ \
 | | | |/ _` | __/ _` | | |_  | |/ _ \ \ /\ / /   \ \ \ \ \ \
 | |_| | (_| | || (_| | |  _| | | (_) \ V  V /    / / / / / /
 |____/ \__,_|\__\__,_| |_|   |_|\___/ \_/\_/    /_/_/_/_/_/

2.9.0-SNAPSHOT

Welcome to the Spring Cloud Data Flow shell. For assistance hit TAB or type "help".
Successfully targeted http://localhost:9393/
dataflow:>
```
