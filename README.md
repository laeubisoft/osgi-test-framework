# osgi-test-framework
This is an extension for the [osgi-test library](https://github.com/osgi/osgi-test) that provides an easy way to fire up an embedded framework.

## Setup an embedded Testframework with `FrameworkExtension`

`org.osgi.test` itself assumes running inside an OSGI Framework and you can setup one 
using [BND](https://github.com/bndtools/bnd), [Tycho](https://github.com/eclipse/tycho/), [Pax Exam](https://github.com/ops4j/org.ops4j.pax.exam2) or other techniques but this often requires carefull setup 
and often mean all your tests run in the same setup or you spread them over different 
test-artifacts.

With the `FrameworkExtension` it is possible to start a so called [Connect-Framework](http://docs.osgi.org/specification/osgi.core/8.0.0/framework.connect.html) 
that is directly feed from the classpath of your test, this makes it suitable for test-cases 
where you have a small set of bundles (even though the number is not limited in any 
way), want to run different configurations or run directly from your current module 
(e.g. the usual maven setup where the test-code is next to your code and executed by 
maven-surefire as part of that build).

First you need to consider some things:

- you are responsible for setting up what makes your Framework, the good news is that 
you often do not need setup all bundles, just those required for your test
- all bundles must be on the classpath of your test, either as a jar or as a folder
- even though your test will see a full Framework and thus can register services, use 
declarative services and so on, all bundles share the same classloader. This has advantages 
(e.g. your test can easily interact with all code in the framework) but also limit 
the usage of some OSGi feature, e.g. you can't use the same bundle in different versions
- because of this, lazy activation of bundles do not work and they will always be activated 
beforehands

### Configure the framework

The framework is confugured using a builder, lets assume you only need your test and 
will setup everything else using `org.osgi.test` (e.g. installing other bundles, register 
services, see below) then you can use this and you are done.

```java
@RegisterExtension
static FrameworkExtension framework = FrameworkExtension.builder()
		.build();
```

### adding a bundle from the classpath

Often one wants additional stuff, so you can load it from the classpath of your test 
(e.g. adding it as a maven dependency):

```java
static FrameworkExtension framework = FrameworkExtension.builder()
		.withBundle("org.xerial.sqlite-jdbc")
		.build();
```

you simply pass the bundle name of the bundle and you are done.

If you like, you can even mark your bundle as beeing started:
```java
static FrameworkExtension framework = FrameworkExtension.builder()
		.withBundle("org.xerial.sqlite-jdbc", true)
		.build();
```

### Inspect the state of the framework

The extension provides some usefull methods to give you insights into the state
of your framework:

- `framework.printBundles(System.out::println)` will print an overview of all installed 
bundles and their state
- `framework.printServices(System.out::println)` will print an overview of all installed 
services
- `framework.printComponents(System.out::println)` will print an overview of all declarative 
  services components and their state
- `framework.printFrameworkState(System.out::println)` will print bundes, services 
and components
-  `framework.getFrameworkEvents` return the `FrameworkEvents` for this framework that 
could be used to inspect any `FrameworkEvent`s that occuring while starting or running 
the embedded framework.

### Export additional packages

In most cases your bundles under test will require additional packages (e.g. from APIs) 
and you can of course simply provide the API bundle as well to fullfill this requirements:

```java
static FrameworkExtension framework = FrameworkExtension.builder()
		.withBundle("org.xerial.sqlite-jdbc", true)
		.withBundle("org.osgi.service.jdbc")
		.build();
```
depending how good you shape your system (and how good shaped the libraries are you 
are using), this can become a headache as those bundles might require additional packages 
or capabilities.
As an alternative you can export arbitrary packages from your test-probe, this could 
also be used to make some things aviable from within your test-probe even though it 
is not a bundle at all:

```java
static FrameworkExtension framework = FrameworkExtension.builder()
		.withBundle("org.xerial.sqlite-jdbc", true)
		.exportPackage("org.osgi.service.jdbc", "1.0.0")
		.build();
```

### Further testing support

For further support of testing OSGi items itself, take a look at https://github.com/osgi/osgi-test/tree/main/org.osgi.test.junit5
