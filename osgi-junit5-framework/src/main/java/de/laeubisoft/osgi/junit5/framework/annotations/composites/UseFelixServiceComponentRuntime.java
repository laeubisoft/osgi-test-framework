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
package de.laeubisoft.osgi.junit5.framework.annotations.composites;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import de.laeubisoft.osgi.junit5.framework.annotations.WithBundle;

/**
 * This annotation enables the Felix Service Component Runtime for a test
 */
@Inherited
@Target({
	ElementType.TYPE
})
@Retention(RUNTIME)
@Documented
@WithBundle(value = "org.apache.felix.scr", start = true, isolated = true)
@WithBundle("org.osgi.util.promise")
@WithBundle("org.osgi.util.function")
@WithBundle("org.osgi.service.component")
public @interface UseFelixServiceComponentRuntime {

}
