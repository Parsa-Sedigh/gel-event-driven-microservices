# Section 04 - Externalizing configuration with Spring Cloud Config Server

## 21-001 Creating Config Server Repository
Externalized configuration microservices pattern enables working with same code in different environments. This pattern
is one of the topics in 12 factor application methodology.

12 factor app provides shared vocabulary for common problems in software-as-a-service.

Create `config-server-repository` directory.

config-client.yml is for general application configuration logic.

We create different yml files for each of the microservices.

We wanna put all the things in `application.yml` of microservices in `config-client-<microservice name>.yml` in config-server.
We wanna use the config **externally**, we don't want to keep the config in the service itself.

Note: application.yml defined in the service, overrides the config in the config server repo. Because we have enabled overriding of these
in config-client.yml .

Note: config-server will need a git repo to be able to fetch the data from this(config-server-repository) folder.
In other words, the config-server microservice fetches the data from the config-server-repository.

The bootstrap.yml is required in case of loading config in bootstrap phase which has priority over application.yml .

## 22-002 Creating Spring Cloud Config Server as a Microservice
## 23-003 Using a common logback file for all microservices
Common logback file. Share appends and pass service specific parameters. Create a `logback-common.xml` file in app-config-data module.
With this approach, we can change or add appenders in a single centralized place and it will immediately all the services that include
that common-logback.xml

Since we put the logback-common.xml in resources folder of app-config-data module, it will end up in the classpath. So we can include
this using a `<include resource="..." />` without setting any path for `resource`. So we only need to set the name of the logback-common.xml
since logback will scan the classpath starting from the top directory.

## 24-004 Changing twitter-to-kafka-service to work with config server
Note: Fetching config data from config-server should be before loading the application config, so we need to use
bootstrap configuration in bootstrap.yml not application config in application.yml .

Note: Now when we run
```shell
mvn clean install
```
it requires the config server to run the tests. So run config-server before running mvn clean install for twitter-to-kafka service.
This is required because mvn install cmd will load the context from twitter-to-kafka service and since we defiend a context load test
there, it requires the config-server as it will try to fetch data from config-server. But again you can use mvn clean install -DskipTests
to skip the context load test in case you don't want to run config-server.

## 25-005 lecture-23-run-project
## 26-005 Using remote GitHub repository
To enable high availability for config server and to manage and scale better, we use a remote github repo.

Since it's a private repo, we need to provide username and password.

Here, we need to do a change in using the password for github account. We need to use a token instead of our github account password directly.

change default branch from master to main by using `spring.cloud.config.server.git.default-label`. So set it to main.

When config server is running, you can go to `localhost:8888/config-client/twitter_to_kafka`

## 27-006 Adding security to config server and encrypt passwords
Add basic auth to config server by adding spring security dep. Having this dep, automatically enables basic auth.

We wanna encrypt the passwords. One is remote github pass and the other is local config server pass. For that we create two git branches.
One for jasypt encryption and the other for JCE(java cryptography extension encryption).

These are for encrypting secrets.

## 28-007 Using Jasypt to encrypt sensitive data
Java simplified encryption

jasytp: provides basic encryption capabilities

Note: Since we added the playground as a submodule in our project, it will be added automatically to the <modules> section of 
root pom. Remove it from there because we won't use it as a submodule for our app. It will just be a playground module to run some
jasypt tests.

Secure jasypt key: Make it parameterized to obtain at startup securely.

Note: We don't want to hardcode the encryptor key at application.yml, we wanna pass this key when the app starts. We can do this using
startup parameters.

So run this to set the env:
```shell
export JASYPT_ENCRYPTOR_PASSWORD='<...>'
```

Note: export command only sets env vars temporarily. To make it permanent, go to ~/.bash_profile or ... and set it there.
Then restart intellij so it could pick it up. You can also pass it using `program arguments` in intellij:
`-Djasypt.encryptor.password='<...>'`.

Note: We can remove jasypt encryptor password from application.yml because spring boot will automatically pick up this env var
from system.

## 29-008 Using JCE to encrypt sensitive data
## 30-009 JCE vs Jasypt
## 31-010 Containerization of config server by creating the docker image