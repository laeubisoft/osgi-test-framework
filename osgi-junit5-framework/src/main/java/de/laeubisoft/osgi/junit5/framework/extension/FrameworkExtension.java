/*******************************************************************************
 * Copyright (c) Läubisoft GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package de.laeubisoft.osgi.junit5.framework.extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.PreconditionViolationException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;

import de.laeubisoft.osgi.junit5.framework.annotations.EmbeddedFramework;

/**
 * The {@link FrameworkExtension} allows to start a so called <a href=
 * "http://docs.osgi.org/specification/osgi.core/8.0.0/framework.connect.html">Connect-Framework</a>
 * that is directly feed from the classpath of your test, this makes it suitable
 * for test-cases where you have a small set of bundles (even though the number
 * is not limited in any way), want to run different configurations or run
 * directly from your current module (e.g. the usual maven setup where the
 * test-code is next to your code and executed by maven-surefire as part of that
 * build).
 */
public class FrameworkExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

	private JUnit5ConnectFramework connect;

	FrameworkExtension() {
	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		this.connect = getConnectFramework(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		JUnit5FrameworkUtilHelper.threadHelper.set(connect);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		JUnit5FrameworkUtilHelper.threadHelper.set(null);
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		return parameterContext.isAnnotated(EmbeddedFramework.class)
				&& parameterContext.getParameter().getType() == Framework.class;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return connect.framework;
	}

	private static JUnit5ConnectFramework getConnectFramework(ExtensionContext context) {
		Namespace namespace = Namespace.create(FrameworkExtension.class, context.getUniqueId());
		Store store = context.getStore(namespace);
		return store.getOrComputeIfAbsent("JUnit5ConnectFramework", key -> {
			try {
				return new JUnit5ConnectFramework(context.getRequiredTestClass(), context.getUniqueId());
			} catch (Exception e) {
				throw new PreconditionViolationException("problem starting framework: " + e, e);
			}
		}, JUnit5ConnectFramework.class);
	}

	/**
	 * Prints the current Framework bundles and their state to the given log
	 * consumer
	 * 
	 * @param framework the framework for printing
	 * @param log       for each line, this consumer will receive a string
	 */
	public static void printBundles(Framework framework, Consumer<String> log) {
		Bundle[] bundles = framework.getBundleContext().getBundles();
		log.accept("============ Framework Bundles ==================");
		Comparator<Bundle> bySymbolicName = Comparator.comparing(Bundle::getSymbolicName,
				String.CASE_INSENSITIVE_ORDER);
		Comparator<Bundle> byState = Comparator.comparingInt(Bundle::getState);
		Arrays.stream(bundles).sorted(byState.thenComparing(bySymbolicName)).forEachOrdered(bundle -> {
			log.accept(toBundleState(bundle.getState()) + " | " + bundle.getSymbolicName() + " (" + bundle.getVersion()
					+ ")");
		});
	}

	/**
	 * Prints the current Framework bundles, registered services and components to
	 * the given log consumer
	 * 
	 * @param framework the framework for printing
	 * @param log       for each line, this consumer will receive a string
	 */
	public void printFrameworkState(Framework framework, Consumer<String> log) {
		printBundles(framework, log);
		printComponents(framework, log);
		printServices(framework, log);
	}

	/**
	 * Prints the current Framework registered services to the given log consumer
	 *
	 * @param framework the framework for printing
	 * @param log       for each line, this consumer will receive a string
	 */
	public static void printServices(Framework framework, Consumer<String> log) {
		log.accept("============ Registered Services ==================");
		Arrays.stream(framework.getBundleContext().getBundles()).map(bundle -> bundle.getRegisteredServices())
				.filter(Objects::nonNull).flatMap(Arrays::stream).forEach(reference -> {
					Object service = reference.getProperty(Constants.OBJECTCLASS);
					if (service instanceof Object[]) {
						Object[] objects = (Object[]) service;
						if (objects.length == 1) {
							service = objects[0];
						} else {
							service = Arrays.toString(objects);
						}
					}
					log.accept(service + " registered by " + reference.getBundle().getSymbolicName() + " | "
							+ reference.getProperties());
				});
	}

	/**
	 * Prints the current Framework bundles and their state to the given log
	 * consumer
	 *
	 * @param framework the framework for printing
	 * @param log       for each line, this consumer will receive a string
	 */
	public static void printComponents(Framework framework, Consumer<String> log) {
		BundleContext bc = framework.getBundleContext();
		try {
			ServiceReference<?>[] serviceReferences = bc
					.getAllServiceReferences(ServiceComponentRuntime.class.getName(), null);
			if (serviceReferences == null) {
				log.accept("No service component runtime installed (or started) in this framework!");
				return;
			}
			for (ServiceReference<?> serviceReference : serviceReferences) {
				Object service = bc.getService(serviceReference);
				if (service instanceof ServiceComponentRuntime) {
					ServiceComponentRuntime componentRuntime = (ServiceComponentRuntime) service;
					log.accept("============ Framework Components ==================");
					Collection<ComponentDescriptionDTO> descriptionDTOs = componentRuntime
							.getComponentDescriptionDTOs();
					Comparator<ComponentConfigurationDTO> byComponentName = Comparator
							.comparing(dto -> dto.description.name, String.CASE_INSENSITIVE_ORDER);
					Comparator<ComponentConfigurationDTO> byComponentState = Comparator.comparingInt(dto -> dto.state);
					descriptionDTOs.stream()
							.flatMap(dto -> componentRuntime.getComponentConfigurationDTOs(dto).stream())
							.sorted(byComponentState.thenComparing(byComponentName)).forEachOrdered(dto -> {
								if (dto.state == ComponentConfigurationDTO.FAILED_ACTIVATION) {
									log.accept(toComponentState(dto.state) + " | " + dto.description.name + " | "
											+ dto.failure);
								} else {
									log.accept(toComponentState(dto.state) + " | " + dto.description.name);
								}
								for (int i = 0; i < dto.unsatisfiedReferences.length; i++) {
									UnsatisfiedReferenceDTO ref = dto.unsatisfiedReferences[i];
									log.accept("\t" + ref.name + " is missing");
								}
							});
				} else {
					log.accept("The service component runtime " + service
							+ " and the test-probe do not share the same classspace for "
							+ ServiceComponentRuntime.class.getName() + "!");
				}
				if (service != null) {
					bc.ungetService(serviceReference);
				}
			}
		} catch (InvalidSyntaxException e) {
		}
	}

	private static String toComponentState(int state) {
		switch (state) {
		case ComponentConfigurationDTO.ACTIVE:
			return "ACTIVE ";
		case ComponentConfigurationDTO.FAILED_ACTIVATION:
			return "FAILED ";
		case ComponentConfigurationDTO.SATISFIED:
			return "SATISFIED ";
		case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION:
		case ComponentConfigurationDTO.UNSATISFIED_REFERENCE:
			return "UNSATISFIED";
		default:
			return String.valueOf(state);
		}
	}

	private static String toBundleState(int state) {
		switch (state) {
		case Bundle.ACTIVE:
			return "ACTIVE   ";
		case Bundle.INSTALLED:
			return "INSTALLED";
		case Bundle.RESOLVED:
			return "RESOLVED ";
		case Bundle.STARTING:
			return "STARTING ";
		case Bundle.STOPPING:
			return "STOPPING ";
		default:
			return String.valueOf(state);
		}
	}

}
