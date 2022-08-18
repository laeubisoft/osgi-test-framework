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
package de.laeubisoft.osgi.junit5.framework.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

import de.laeubisoft.osgi.junit5.framework.extension.FrameworkExtension;

/**
 * Could be used to configure an extra framework property, example:
 *
 * <pre>
 * &#64;WithFrameworkProperty(property = Constants.FRAMEWORK_BEGINNING_STARTLEVEL, value = "6")
 * class MyTests {
 *
 * }
 * </pre>
 */
@Inherited
@Target({
	ElementType.TYPE
})
@Retention(RUNTIME)
@ExtendWith(FrameworkExtension.class)
@Documented
@Repeatable(WithFrameworkProperties.class)
public @interface WithFrameworkProperty {
	/**
	 * @return the name of the property to set on the framework
	 */
	String property();

	/**
	 * @return the value to set as a framework property
	 */
	String value();
}
