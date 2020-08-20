/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.studio.model.parameter;

/**
 * Property types constants, which are used in SimplePropertyDefinition
 */
public final class PropertyTypes {

    public static final String ARRAY = "ARRAY";

    public static final String BOOLEAN = "BOOLEAN";

    public static final String ENUM = "ENUM";

    public static final String OBJECT = "OBJECT";

    public static final String STRING = "STRING";

    private PropertyTypes() {
        new AssertionError();
    }
}
