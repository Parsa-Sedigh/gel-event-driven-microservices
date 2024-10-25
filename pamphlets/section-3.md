`## 5-001 Introduction to Spring boot

## 6-002 Creating the base Spring boot project

start.spring.io

`mvnw` is maven wrapper file and it allows you to run maven project without having maven installed and present on the
$PATH.
It downloads the correct maven version if it's not found on the system which is helpful for interoperability.

Remove the root src folder as the sources will be under sub-modules. So delete src and target folders.

Now let's make the pom.xml file a base pom file for future modules:

1. since we don't have any source files in the base project, the <packaging> shouldn't be jar but should be pom. That
   implies that
   we will use this pom as a base configuration and not for creating a runnable jar.
2. add <dependencyManagement> and move all deps of <dependencies> in there. This will define all deps in this base pom
   file without
   really downloading them. Any submodule that wants to include a dep, will simply override and use a <dependencies>
   section in the sub-module
   itself.
3. Define the versions in base pom file so that submodules will just include a dep without specifying the version and
   all the submodules
   will get the same version of dep. Do this in <properties> of base pom. For example, define `spring-boot.version`
   property in
   <properties> section and reference it from deps and plugins. We do all these changes to manage the deps with versions
   in a single place
   in the base pom.xml file and use them without hassle in the submodules and microservices.
4. in <build>, move <plugins> to <pluginManagement>. Again, any service(submodule) that wants to include a plugin, will
   simply create
   a <plugin> section in the module without setting the version, but by setting any property or goal for that task
   specifically.

Note: <dependencyManagement> is for dep management, plugin management and version tracing.

spring-boot-maven-plugin: it can create executable archive files such as jar or war files that contain all app's deps
and can be run
with a single java -jar command. This behavior comes by just including the plugin itself because it's pre-configured to
create the
target runnable jar. In addition, this plugin helps to run spring boot apps, generate build information and start your
spring boot app
prior to running integration tests and finally it helps to create docker images with the build-image goal which comes
with
spring boot 2.3.0 release.

So: Make it runnable, start the app, create build info, create docker images.

maven-compiler-plugin: by default, maven uses java 1.6 version for source and target settings and since we wanna use a
newer java version,
we use this plugin to set this property. Note that we won't put this in <pluginManagement> section, as we need this
plugin application-wide.
So we need to define it in <plugins> section to be used in submodules without needing to override.
So this plugin sets the java compiler version for a maven project.

Note: Starting with java 9, instead of <source> and <target>, we need to use <release>

## 7-003 The very first microservice

Twitter-to-kafka service: Responsible for get the data from twitter and put it in kafka.

We don't need to specify versions for the deps in submodules that have defined versions in base pom.xml .

Note: <scope>provided</scope> : use for compile-only tools which are not required at runtime.

Since this service won't be triggered by a client and need to start reading data from twitter when the app starts,
we need to find a way to trigger the reading logic. There are a couple of opts:

1. we can use @PostConstruct on a method like init(). That method will be called once after the spring bean created. And
   by default,
   spring beans are created once because they are created as singletons. However, we can change change this behavior
   with @Scope().
   @Scope("request") will create a new spring bean for each req. With this annotation, how many times the init method of
   TwitterToKafkaServiceApplication
   will be called then? It will be called in each req separately because the method will run after each obj creation. So
   @Scope("request")
   is not a good option for an application general initialization job. Although this behavior is only valid for example
   for a controller,
   where a req will cause to create a new bean, we still look for a better alternative for initialization.

```java

@SpringBootApplication
@Scope("request")
public class TwitterToKafkaServiceApplication {
    @PostConstruct
    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }
}
```

2. implementing ApplicationListener interface and overriding onApplicationEvent() . This method is an application
   listener method, so
   it will run only once for sure, therefore we can use it.

```java

@SpringBootApplication
public class TwitterToKafkaServiceApplication implements ApplicationListener {
    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

    }
}
```

3. implementing CommandLineRunner and overriding the run() method. This is also a perfect option for application
   initialization logic.
   The only difference with the previous way is the parameters.

```java

@SpringBootApplication
public class TwitterToKafkaServiceApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
    }
}
```

Note: @PostConstruct executes after dependency injection to perform initialization.

Since we don't need to use the ApplicationEvent, we choose the third option.

There are also other ways to start initialization logic like using @EventListener, but we stick to CommandLineRunner for
now as it will
do the job for us.

application.yml or application.properties is used to bind external properties into an application at runtime.

Lombok: creates code like getter, setter and ... and then update the class with these methods during **compilation** and
the
resulting bytecode that will be run by jvm, will actually include these methods created by lombok.

So lombok updates the class with additional methods during compilation.

With springboot, we got logback and slf4j deps automatically, so we could only add a logback.xml to customize logging
behavior.

If you specify a lower log level, it will also log the higher levels but not the lower levels than the current one.
That means trace level will print all the log messages defined in the code. However, if you see the level as error,
all the logs except error, will be ignored.

trace > debug > info > warn > error.

Run `mvn clean install` to install the deps and to see if we have a successful build.

There are two ways to inject an obj into another:

- field injection: using @Autowired
- constructor injection: using constructor

There are some disadvantages of using field injection:

1. you don't need to use @Autowired with constructor injection, so you can inject an obj while you are writing a simple
   java code
   without adding any annotation

Advantages of using constructor injection:

1. allows to be immutable, since you can define the property as final. And immutable objects helps to create more robust
   and
   thread-safe apps
2. forces that the obj is created with the required dep as the **constructor forces it**
3. we don't use reflection with this kind of injection unlike field injection. We know that reflection makes the app
   code to run slower
   since it involves types that are dynamic result at runtime.

So: Prefer constructor injection over field injection because it favors immutability & forces object creation with the
injected
obj & no reflection & no @Autowired.

## 8-004 Streaming tweets with Twitter4j The command component in CQRS & Event sourcing

All classes that annotated with @Component, will be scanned and loaded as spring bean at runtime.

Note: Spring beans are created with singleton scope by default. That means there will be a single instance for each bean
and when
you inject this bean, it will use the same singleton instance, instead of creating a new instance.

Note: If you define a spring bean with prototype scope instead, then for each injection of bean, spring will create a
new instance.
So it won't be a singleton anymore.

Note: @Singleton: same instance is shared each time the obj is injected.

Note: @Prototype: a new obj is created each time the obj is injected.

Note: Spring will not call method with @PreDestroy, when a bean is in prototype scope. So @PreDestroy is not called with
prototype scope

`mvn clean install`: cleans the files generated by maven(clean) & build and install the project into local repo(
install).

The twitter-to-kafka microservice will be used as the command component in CQRS pattern. We call the twitter tweets as
events and we put
the tweets(events) from source-data(twitter) to a kafka topic(event-store).

Instead of directly reading data from kafka-topic, we put the data in elastic search and read from there. This is more
efficient.
So the write and read parts build differently in CQRS pattern to separate the concerns and do the ops effectively. This
will lead
to an eventual consistency as opposed to strong consistency. Because there will be a small delay between write and read
ops but this is
not that important if you don't do real-time operations and having write and read parts separately will allow you to
implement and use
the different parts much more effectively for scaling.

Eventual consistency: a consistency model used in distributed computing to achieve high availability that informally
guarantees that,
if no new updates are made to a given data item, eventually all accesses to that item will return the last updated
value.

## 9-005 Streaming tweets with Twitter Api V2

For now, to use twitter v2 api, we can't use twitter4j altogether, instead create a custom twitter client and trigger
twitter4j listener class.

## 10-006 Adding mock twitter stream as an alternative

Note: Use custom exception class, so that you can customize the exception and message. With this, we can distinguish the
exceptions that are
thrown at runtime.

Since we programmed interface instead of concrete classes, we can easily **switch** the implementations using
config vars and @ConditionalOnExpression.

@ConditionalOnProperty: conditionally create a spring bean with a config variable.

Using config vars in application.yml, we can change the configs at compile time(for example deciding which
implementation class to use
at **compile time**) and spring will load the correct bean at **runtime**.

## 11-007 Introducing Apache KafkaEvent sourcing, topics, partitions, producer & consumer

we use kafka as event store in our project.

### Kafka basics

- low latency & high throughput
- holds the data in an immutable, append-only structure called topics. We call this data as logs or events. It's
  immutability ensures that
  you will have all history of data feeds.
- kafka is fast, resilient, scalable and has high throughput
- it's resilient as it relies on file system(instead of memory) for storing and caching messages. It keeps all messages
  on disk and use replicas.
- resilient and fault-tolerant by replication. It keeps a configurable amount of replicas to prevent data loss in case
  of a failure.
- it's fast because it relies on disk caching and **memory-mapped** files of the underlying OS instead of garbage
  collector eligible JVM memory.
  A memory mapped file contains the contents of a file in virtual memory. The mapping between a file and memory space
  enables an application to
  modify the file by reading and writing directly to the memory. Accessing memory mapped files is faster than using
  direct read & write ops as it
  operators on memory. So memory mapped files are faster and have lower I/O latency than using a direct disk access. In
  addition to that,
  in most operating systems, the memory region assigned to a mapped file is in kernel's page cache. That means no copies
  will be created in
  the user memory space and it will directly operate on disk cache, which will be faster.
  Page cache consists of physical pages in RAM, corresponds to physical blocks on disc. So although kafka uses disk to
  store the data
  for resiliency, it actually provides a strong caching mechanism by using disk caching which actually means it works
  directly on memory of
  the underlying OS most of the time.
- scale by partitions. Kafka has a natural scaling capability thanks to the partitions inside each topic. So you can
  scale by just increasing
  the partition number.
- ordered inside partition. Ordering is only guaranteed in a single partition. You can put the related data to the same
  partition by using a
  partitioning strategy. So we use partitioning strategy to insert related data into the same partition.
- kafka as an event store: a great match for event-driven microservices

For high availability, it's better to hold the brokers on different servers.

Kafka producer:

- sends data to kafka cluster
- thread safe for multi-threading. So in most cases, using a single producer might be enough for the system.

The replication factor can be as much as the broker number to get higher resiliency. But OFC if the replication factor
increases,
the perf cost is increase a bit as well because with multiple replications, it will require approvals from all of the
replicas to
finalize a data to be produced on a kafka topic.

Each partition can only be assigned to a single consumer. So a partition can only have one consumer. On the other hand,
it's possible to read(consume) multiple partitions from a single consumer.

Yes, we can use a single consumer to consume all the partitions. But we can add more consumers up to the number of
partition number.

You can use threads in a single process, or processes on a single machine

**Kafka consumer concurrency level:** threads -> processes -> machines. We can use threads in a single process or
processes on a
single machine or even processes on different physical machines. These are the concurrency levels.

### Kafka producer basics

A producer holds a buffer of unsent records per partition.

- batch.size: the more the batch, the more throughout.
- linger.ms: used to add a delay on producer for higher throughput. default is 0, so no delay by default! So setting
  this to 10,
  will cause 10ms delay for each send on the producer.
- max.in.flight.requests.per.connection: used to limit the in-flight reqs number on producer

linger.ms tradeoff: increasing it will increase the throughput, but it will also cause a delay as the producer will wait
more before
sending the data. So delay vs throughput tradeoff.

multiple broker nodes: to achieve high availability and resiliency.

Another trade-off between performance and resiliency:

- ack=all: used for better resiliency.
- ack=1: producer will wait confirmation only from the target broker.
- ack=0: no resiliency. Producer will not wait for any confirmations.

Note: By default, there is no compression on producer.

By using compressed data, we can send more data at once through the network and increase the throughput.

Snapp: Fast compressor with a less compression

End to end compression: better in performance as compression is done once.

Note: If you set `retry` config more than 0, there will be a possibility that the order of messages will be lost.
Because any failed data,
can be inserted in the second try and it will be inserted after the records that naturally comes after it. To prevent
this, you can use
max.in.flight.requests.per.connection = 1 (if retires > 0) , so you can only send one **record** at the time. But OFC
this will affect the
perf, because you can't use batching in this case. So you need to decide if ordering is important.

Another tradeoff: retires tradeoff. Ordering vs performance

DefaultPartitioner uses round-robin approach to distribute the load on the partitions. That means, it will send data to
available
partitions one by one in rational order.

Round-robin: send data to each partition equally in rational order.

## 12-008 Adding common config module

When a svc gets data from config server, the config data will be hold on config data classes and it makes sense to
create a config data module and hold all config classes there.

So the config module holds all java config classes of all services.

We wanna keep the config classes in a single module instead of holding them in each microservice.

@ComponentScan(basePackages = "com.microservices.demo") is required to allow finding the spring beans in other modules.
When a spring boot app starts, by default it scans the packages starting from the package directory that the spring boot
app main class resides in.
In twitter-to-kafka-service module, the package that the application class scans,
is `com.microservices.demo.twitter.to.kafka.service`.

When we work with multiple modules, there will be some spring beans that reside in different packages, for example the
TwitterToKafkaServiceConfigData class is in com.microservices.demo.config package which is not in the package of
application class of TwitterToKafkaServiceApplication which is
com.microservices.demo.twitter.to.kafka.service which is the default scan. So we have to use @ComponentScan() with
`basePackages = "com.microservices.demo"` . So spring will find all packages in all modules because we use
com.microservices.demo as the
base group for our project. Every module has a package structure that starts with com.microservices.demo .

## 13-009 Running Apache Kafka cluster with docker Kafka, Zookeeper and Schema Registry

If you don't set a bridge network, a default network will be created. And on the **default** bridge network, containers
can only access
each other by IP addresses. But with a user-defined bridge network, containers can be resolved using hostnames which is
more handy instead
of IP addresses, because IP has a dynamic structure and can change in time.

Zookeeper is required to hold metadata for a cluster and play a role in determining leader election and cluster health.
KIP 500 project will remove the zookeeper from kafka. The leader election will be done inside the kafka cluster itself,
where the leader itself is
responsible for the cluster health.

Schema registry is used to register a schema for a kafka topic and it will check the producers and consumers each time,
to force them to
use a registered schema so that only the allowed(valid) schema will be used. Producers and consumers check schema with
id and
cache the result before sending and receiving data. So it's actually a one-time req to the schema registry and then
subsequent checks are
done by using cache.

Schema registry also allows backward and forward compatibility. So schema can evolve without breaking changes.

Why we have defined 3 kafka brokers?

It's to accomplish the concept of quorum which indicates the minimum number of members necessary to conduct a business
in a group. And in
kafka terms, this will prevent split brain issue, which can occur when a network split occurs. It prevents creating more
than one network in
a group of kafka brokers such that with three brokers, a network must have two nodes at least. This way we can be sure
that **only**
one network can be created because there can only be a one group with two nodes. Note that the other group will only
have one node,
which is not enough to create a network.

Quorum: set the minimum number of brokers to create a network - prevent split brain.

In docker-compose directory:

```shell
docker compose -f common.yml -f kafka_cluster.yml up

# to see if containers are running
docker ps
```

CLI tool called kafkacat to monitor kafka cluster and topics.

To use it on windows, it's better to use kafkacat docker image. Note that to use kafkacat with the docker image, use
`--network=host` option in `docker run`, because when we use kafkacat inside a docker container, to be able to reach to
the host machine that
is localhost in our case, we need to start the container by including it in the host machine's network.

The kafka-admin submodule will create and verify kafka topics programmatically.

The kafka-producer submodule will include spring-kafka to write kafka producer implementation.

## 14-010 Creating kafka-model module

avro: strict schema and efficient byte serialization.

avro-maven-plugin: creates java classes from avro schema file.

To create the java classes from avro files:

```shell
mvn clean install
```

## 15-011 Creating kafka-admin module - Part 1 Configuration and dependencies

We need to check(verify) if the topics are created and schema server is running, prior to running microservices. This
will be required, because when you run all components in a single docker compose file, if kafka or schema registry or
any other dep is not running, your app will fail at startup and won't continue to work as expected. So to make your
services more resilient, you will need to add some checks prior to running services. These checks are at kafka-admin
module. Also apart from these programmatic check, we will add more checks in the compose file.

spring retry: automatically retry a failed op.

Spring-aop is required to be able to use spring-retry.

ExponentialBackOffPolicy: increases wait time for each retry attempt

kafka AdminClient: manage and inspect brokers, topics and configurations.

acks: all - wait ack from all replicas | 1: wait only current broker's ack | 0: no ack

## 16-012 Creating kafka-admin module - Part 2 Creating Kafka topics programmatically

## 17-013 Creating kafka-producer module Configuration of Kafka producer

## 18-014 Creating kafka-producer module Produce events to store in Kafka event store

Kafka template: A thread-safe template for executing high level producer ops.

**Listenable future**: Register callback methods for handling events when the response return.

## 19-015 Integrate Kafka modules with Microservice Use Kafka as event store for service

- Kafka-producer module: uses spring-kafka to write kafka producer implementation.
- kafka-admin module: create and verify kafka topics programmatically and check if topics are created and schema
  registry is up and running

Check if the kafka cluster is ready:

```shell
kafkacat -L -b localhost:<broker port>
```

Then run `TwitterToKafkaServiceApplication` with `enable-mock-tweets: false`.

Examine kafka cluster:

```shell
kafkacat -L -b localhost:19092

# Note: Using kafkacat with -C flag, makes it consumer
# kafkacat consumer: kafkacat -C -b host:port -t topic_name
kafkacat -C -b localhost:19092 -t twitter-topic
```

Then run the app with `enable-mock-tweets: true`.

## 20-016 Containerization of microservice with docker image Run all with docker compose

To create the docker img, we use `spring-boot:build-image` property. It creates a docker image for a spring boot app.
Spring boot provides this property through the spring-boot-maven plugin.

If you run `mvn install` from root, it will compile and build the project and then it will create the docker image of
the twitter-to-kafka-service.

Note: when we run `mvn install`, it will also run the context load tests and currently when the context loads, it will
try to reach to kafka in the test and since we don't have any kafka mock running on test, we should first run a kafka
cluster locally before running `mvn install` or we can use:
```shell
mvn install -DskipTests
```

to skip context load test, so we can install deps and create docker img.

Spring boot `build-image` follows layerd approach to prevent two overheads:
1. prevents creating a single fat jar
2. if there's an update, there will be no need to update the whole jar. Thanks to using caching. This is achieved with the help of
cloud native buildpacks. 

So docker img is created with a layerd approach and layering is designed to separate the code based on how likely it is to change
between application builds. Library code is less likely to change between builds so it is placed in it's over layers to allow tooling
to reuse the layers from cache. App code on the other end, is more likely to change between builds, so it is isolated on a separate layer.
So by reusing layers during image creation and caching the results, we get faster builds.

To run all the components:

```shell
docker compose -f common.yml -f kafka_cluster.yml -f services.yml up
docker ps
kafkacat -L -b localhost:19092
docker logs -f container_id
```

However, we can use the .env file to set the yml files we wanna run, by setting the `COMPOSE_FILE` env and then we can only run:
```shell
# in docker-compose directory:
docker compose up
``` 
to start all containers we've listed in the `COMPOSE_FILE` env.