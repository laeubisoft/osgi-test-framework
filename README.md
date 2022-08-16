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

The framework is configured using annotations described below.
Just in cases where you only need your test and will setup everything else using `org.osgi.test` (e.g. installing other bundles, register 
services, see below) then you can still use this standalone.

```java
@ExtendWith(FrameworkExtension.class)
public class MyImplTest {

		... your test code here ...
}
```

### Adding a bundle from the classpath

Often one wants additional stuff, so you can load it from the classpath of your test 
(e.g. adding it as a maven dependency):

```java
@WithBundle("api-bundle")
@WithBundle("impl-bundle")
public class MyImplTest {
		... your test code here ...
}
```

you simply pass the bundle name of the bundle and you are done.

If you like, you can even mark your bundle as beeing started (usually desired if the 
contain activators or DS components):
```java
@WithBundle("api-bundle")
@WithBundle(value = "impl-bundle", start = true)
public class MyImplTest {
		... your test code here ...
}
```

### Export additional packages

In most cases your bundles under test will require additional packages (e.g. from APIs) 
and you can of course simply provide the API bundle as well to fullfill this requirements 
but depending how good you shape your system (and how good shaped the libraries are you 
are using), this can become a headache as those bundles might require additional packages 
or capabilities. You maybe simply want to restrict the set of bundles to the absoloute 
minimum.

As an alternative you can export arbitrary packages from your test-probe, this could 
also be used to make some things aviable from within your test-probe even though
it is not a bundle at all, the package will simply be feed from your test classpath:
```java
@WithExportedPackage("my.extra.package;version=\"1.0.0\"")
public class MyImplTest {
		... your test code here ...
}
```

### Set additional Framework Properties

An OSGi framework is configured using framework properties, you can define additional 
ones in the follwoing way:
```java
@WithFrameworkProperty(property = Constants.FRAMEWORK_BEGINNING_STARTLEVEL, value = "6")
public class MyImplTest {
		... your test code here ...
}
```

### Access the framework itself

For advanced use cases you can get the running framework injected into your test:

```java
public class MyImplTest {
	
	@InjectFramework
	Framework framework;
	
		... your test code here ...
}
```



### Inspect the state of the framework

Sometimes things getting wrong, or you even want to assert that something has gone 
wrong or simply need to inspect the framework state.

The extension provides some usefull methods to give you insights into the state
of your framework.

#### Check Framework events

The framework register a service `FrameworkEvents` you can fetch to assert certain 
things about framework events:

```java
	@InjectService //this is from the osgi.test library!
	FrameworkEvents frameworkEvents;
	

	@BeforeEach
	public void checkService() {
		frameworkEvents.assertErrorFree();
	}

		... your test code here ...
}

#### Print out information about a Framework

The `FrameworkExtension` provides some useful methods to query the framework state
```java
public class MyImplTest {

	@BeforeEach
	public void printFrameworkInfo(@InjectFramework	Framework framework) {
		FrameworkExtension.printBundles(framework, System.out::println);
	}
	
		... your test code here ...
}
```
### Composite Annotations

There are some common tasks and configuration that might be repetive to many tests.
For this the extension already provides some common composite annotations.

#### Enable the Felix Service component runtime

```java
@UseFelixServiceComponentRuntime
public class MyImplTest {
	
		... your test code here ...
}
```


### Further testing support

For further support of testing OSGi items itself, take a look at https://github.com/osgi/osgi-test/tree/main/org.osgi.test.junit5
