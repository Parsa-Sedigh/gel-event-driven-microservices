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

## 22-002 Creating Spring Cloud Config Server as a Microservice
## 23-003 Using a common logback file for all microservices
## 24-004 Changing twitter-to-kafka-service to work with config server
## 25-005 lecture-23-run-project
## 26-005 Using remote GitHub repository
## 27-006 Adding security to config server and encrypt passwords
## 28-007 Using Jasypt to encrypt sensitive data
## 29-008 Using JCE to encrypt sensitive data
## 30-009 JCE vs Jasypt
## 31-010 Containerization of config server by creating the docker image