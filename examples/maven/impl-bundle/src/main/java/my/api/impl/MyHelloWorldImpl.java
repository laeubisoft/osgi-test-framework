package my.api.impl;

import org.osgi.service.component.annotations.Component;

import my.api.HelloWorld;

@Component(service = HelloWorld.class)
public class MyHelloWorldImpl implements HelloWorld {

	@Override
	public String sayHello() {
		return "Hello World";
	}

}
