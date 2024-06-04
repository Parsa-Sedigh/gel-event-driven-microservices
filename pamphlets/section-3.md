## 5-001 Introduction to Spring boot

## 6-002 Creating the base Spring boot project
start.spring.io

`mvnw` is maven wrapper file and it allows you to run maven project without having maven installed and present on the $PATH.
It downloads the correct maven version if it's not found on the system which is helpful for interoperability.

Remove the root src folder as the sources will be under sub-modules. So delete src and target folders.

Now let's make the pom.xml file a base pom file for future modules:
1. since we don't have any source files in the base project, the <packaging> shouldn't be jar but should be pom. That implies that
we will use this pom as a base configuration and not for creating a runnable jar.
2. add <dependencyManagement> and move all deps of <dependencies> in there. This will define all deps in this base pom file without
really downloading them. Any submodule that wants to include a dep, will simply override and use a <dependencies> section in the sub-module
itself.
3. Define the versions in base pom file so that submodules will just include a dep without specifying the version and all the submodules
will get the same version of dep. Do this in <properties> of base pom. For example, define `spring-boot.version` property in
<properties> section and reference it from deps and plugins. We do all these changes to manage the deps with versions in a single place
in the base pom.xml file and use them without hassle in the submodules and microservices.
4. in <build>, move <plugins> to <pluginManagement>. Again, any service(submodule) that wants to include a plugin, will simply create
a <plugin> section in the module without setting the version, but by setting any property or goal for that task specifically.

Note: <dependencyManagement> is for dep management, plugin management and version tracing.

spring-boot-maven-plugin: it can create executable archive files such as jar or war files that contain all app's deps and can be run
with a single java -jar command. This behavior comes by just including the plugin itself because it's pre-configured to create the 
target runnable jar. In addition, this plugin helps to run spring boot apps, generate build information and start your spring boot app
prior to running integration tests and finally it helps to create docker images with the build-image goal which comes with 
spring boot 2.3.0 release.

So: Make it runnable, start the app, create build info, create docker images.

maven-compiler-plugin: by default, maven uses java 1.6 version for source and target settings and since we wanna use a newer java version,
we use this plugin to set this property. Note that we won't put this in <pluginManagement> section, as we need this plugin application-wide.
So we need to define it in <plugins> section to be used in submodules without needing to override.
So this plugin sets the java compiler version for a maven project.

Note: Starting with java 9, instead of <source> and <target>, we need to use <release>

## 7-003 The very first microservice
Twitter-to-kafka service: Responsible for get the data from twitter and put it in kafka.

We don't need to specify versions for the deps in submodules that have defined versions in base pom.xml .

Note: <scope>provided</scope> : use for compile-only tools which are not required at runtime.

Since this service won't be triggered by a client and need to start reading data from twitter when the app starts,
we need to find a way to trigger the reading logic. There are a couple of opts:
1. we can use @PostConstruct on a method like init(). That method will be called once after the spring bean created. And by default,
spring beans are created once because they are created as singletons. However, we can change change this behavior with @Scope().
@Scope("request") will create a new spring bean for each req. With this annotation, how many times the init method of TwitterToKafkaServiceApplication
will be called then? It will be called in each req separately because the method will run after each obj creation. So @Scope("request")
is not a good option for an application general initialization job. Although this behavior is only valid for example for a controller,
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
2. implementing ApplicationListener interface and overriding onApplicationEvent() . This method is an application listener method, so
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
3. implementing CommandLineRunner and overriding the run() method. This is also a perfect option for application initialization logic.
The only difference with the previous way is the parameters.
```java
@SpringBootApplication
public class TwitterToKafkaServiceApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {}
}
```

Note: @PostConstruct executes after dependency injection to perform initialization.

Since we don't need to use the ApplicationEvent, we choose the third option.

There are also other ways to start initialization logic like using @EventListener, but we stick to CommandLineRunner for now as it will
do the job for us.

application.yml or application.properties is used to bind external properties into an application at runtime.

Lombok: creates code like getter, setter and ... and then update the class with these methods during **compilation** and the 
resulting bytecode that will be run by jvm, will actually include these methods created by lombok.

So lombok updates the class with additional methods during compilation.

With springboot, we got logback and slf4j deps automatically, so we could only add a logback.xml to customize logging behavior.

If you specify a lower log level, it will also log the higher levels but not the lower levels than the current one. 
That means trace level will print all the log messages defined in the code. However, if you see the level as error,
all the logs except error, will be ignored.

trace > debug > info > warn > error.

Run `mvn clean install` to install the deps and to see if we have a successful build.

There are two ways to inject an obj into another:
- field injection: using @Autowired
- constructor injection: using constructor

There are some disadvantages of using field injection:
1. you don't need to use @Autowired with constructor injection, so you can inject an obj while you are writing a simple java code
without adding any annotation

Advantages of using constructor injection:
1. allows to be immutable, since you can define the property as final. And immutable objects helps to create more robust and
thread-safe apps
2. forces that the obj is created with the required dep as the **constructor forces it**
3. we don't use reflection with this kind of injection unlike field injection. We know that reflection makes the app code to run slower
since it involves types that are dynamic result at runtime.

So: Prefer constructor injection over field injection because it favors immutability & forces object creation with the injected
obj & no reflection & no @Autowired.

## 8-004 Streaming tweets with Twitter4j The command component in CQRS & Event sourcing
All classes that annotated with @Component, will be scanned and loaded as spring bean at runtime.

Note: Spring beans are created with singleton scope by default. That means there will be a single instance for each bean and when
you inject this bean, it will use the same singleton instance, instead of creating a new instance.

Note: If you define a spring bean with prototype scope instead, then for each injection of bean, spring will create a new instance.
So it won't be a singleton anymore.

Note: @Singleton: same instance is shared each time the obj is injected.

Note: @Prototype: a new obj is created each time the obj is injected.

Note: Spring will not call method with @PreDestroy, when a bean is in prototype scope. So @PreDestroy is not called with prototype scope

`mvn clean install`: cleans the files generated by maven(clean) & build and install the project into local repo(install).

The twitter-to-kafka microservice will be used as the command component in CQRS pattern. We call the twitter tweets as events and we put
the tweets(events) from source-data(twitter) to a kafka topic(event-store).

Instead of directly reading data from kafka-topic, we put the data in elastic search and read from there. This is more efficient.
So the write and read parts build differently in CQRS pattern to separate the concerns and do the ops effectively. This will lead
to an eventual consistency as opposed to strong consistency. Because there will be a small delay between write and read ops but this is
not that important if you don't do real-time operations and having write and read parts separately will allow you to implement and use
the different parts much more effectively for scaling.

Eventual consistency: a consistency model used in distributed computing to achieve high availability that informally guarantees that,
if no new updates are made to a given data item, eventually all accesses to that item will return the last updated value.

## 9-005 Streaming tweets with Twitter Api V2
