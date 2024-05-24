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