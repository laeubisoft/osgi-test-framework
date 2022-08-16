package my.api.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.framework.launch.Framework;
import org.osgi.test.common.annotation.InjectService;

import de.laeubisoft.osgi.junit5.framework.annotations.EmbeddedFramework;
import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle;
import de.laeubisoft.osgi.junit5.framework.annotations.composites.UseFelixServiceComponentRuntime;
import de.laeubisoft.osgi.junit5.framework.extension.FrameworkExtension;
import de.laeubisoft.osgi.junit5.framework.services.FrameworkEvents;
import my.api.HelloWorld;

@WithBundle("api-bundle")
@WithBundle(value = "impl-bundle", start = true)
@UseFelixServiceComponentRuntime
public class MyImplTest {

	@InjectService
	HelloWorld helloWorld;

	@BeforeAll
	public static void beforeTest(@EmbeddedFramework Framework framework,
			@InjectService FrameworkEvents frameworkEvents) {
		FrameworkExtension.printBundles(framework, System.out::println);
		FrameworkExtension.printComponents(framework, System.out::println);
		frameworkEvents.assertErrorFree();
	}

	@Test
	public void testService() {
		assertEquals("Hello World", helloWorld.sayHello());
	}

}
