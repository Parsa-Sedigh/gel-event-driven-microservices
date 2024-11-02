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
To use jce, first we need to install spring-boot-commandline utility. To use spring-boot-commandline utility, first we need to install
sdkman or brew. If you chose sdkman, run:
```shell
sdk install springboot
ls ~/.sdkman/candidates
cd ~/.sdkman/candidates/springboot
spring install org.springframework.cloud:spring-cloud-cli:<installed version>.RELEASE

# to encrypt the password
spring encrypt PLAIN_TEXT --key KEY
```

Then put the result string into the cloud.config.password in this format: `{cipher}<encrypted password>`. This way spring understands
this pass is encrypted using JCE.

**Note: Each time, the encrypt command will give different result because it uses a random salt value for secret key generation.**

Also encrypt the github password as well.

We can implement /encrypt and /decrypt endpoints to use config server for encryption.
Then make a POST call to `localhost:8888/encrypt` and in body, put the password you wanna encrypt in raw text format.
You can also call `localhost:8888/decrypt` and put the encrypted password in the body.

## 30-009 JCE vs Jasypt
JCE is preferred way for encrypting secrets.

Jasypt default output is in base64 encoding, but can also be set to hexadecimal. But JCE default output is hexadecimal.

In Jasypt using PBKDF2(password based key derivation function 2) is not possible to create a secret key. In Jasypt we can only
use PBEKeySpec without salt and iteration count. All of the PBE algos use a one-way hashing func. However in Jasypt PBE implementation,
salt is used in encryption process directly and not in creating the secret key. However, with PBKDF2, secret key is created using
a salt and then with an iteration count, to end up with a slow hashing algo.

By using a slow hash func, PBKDF2 makes it difficult to use brute-force attacks and by increasing iteration count,
this difficulty can be increased further.

So PBKDF2 reduces vulnerabilities to brute-force attacks by using salt and iteration count.

Jasypt approach might be a little bit faster especially with cached single salt where only a single salt is used and cached for later usages.
But using a PBEKeySpec without salt and iteration count is less secure and more prone to rainbow attacks, especially if the key is not
strong enough.

Rainbow attacks: type of attack that uses a hash table to crack passwords by comparison.

Rainbow attacks hold the plain text passwords and hash pairs in a pre-computed rainbow table and use brute-force to try to match the hashed value
with an input.

![](img/section-4/30-1.png)

A real rainbow table is more complicated like first matching the first part of a hash value and then it goes to one level deeper to
continue searching.

JCE uses AesBytesEncryptor and PBKDF2 which is PBEKeySpec with salt and iteration count.

So Jasypt is easier to use but JCE is more secure.

**Note: The encrypted values in configs, will be decrypted at startup by config-server and sent to services in unencrypted format.
So it's important to use ssl/tls enabled communication in production env.**

**Note: Here we're using symmetric encryption which means there's a single key for both encryption and decryption.**
There is a better approach which is using asymmetric encryption like RSA where encryption is done by a public key
while decryption is done by a private key. It's more secure as it uses 2 keys: public key is shared but private key kept as confidential.

Asymmetric vs symmetric
Asymmetric:
- more secure as it has 2 keys. msg is encrypted with a public key and can only be decrypted using the corresponding private key.
So without knowing the private key, an attacker can't decrypt the encrypted val.
- this approach is slower because it has more complex mathematical logic and it uses two different keys for encryption and decryption.
The natural tradeoff is between having more secure or faster systems.
- enables using digital signature which enables checking data integrity. So apart from confidentiality, asymmetric approach provides
**data integrity which will help to identify if the data in transit is changed or tampered.**
- an example algo that uses this approach is **RSA** which is used in SSL and TLS solutions.

symmetric: 
- this approach has a single key which makes it faster than asymmetric but less secure than asymmetric. So the same key is used for both encryption and decryption.
- it provides confidentiality to share msgs in encrypted formats
- since we only have a single key, it will be a challenge to share this key. But in asymmetric approach we always keep the private key as a secret
while we can share the public key with everyone.
- **AES** algo uses this encryption

We will use JCE and decrypt the secrets in the config-server and send to microservices. For prod, enable SSL/TLS in communication between config server and microservices.

## 31-010 Containerization of config server by creating the docker image
What happens when a microservice tries to reach to config-server and config-server is not up yet?

A: We need to set health check to config-service and wait until it's ready. We can use depends_on in docker compose file. However, we need a more
sophisticated approach. Because depends_on only checks if the container is up and running not the app inside of container is really running or not.
For this, we wrote check-config-server-started.sh .

Then run:
```shell
chmod +x check-config-server-started.sh
```