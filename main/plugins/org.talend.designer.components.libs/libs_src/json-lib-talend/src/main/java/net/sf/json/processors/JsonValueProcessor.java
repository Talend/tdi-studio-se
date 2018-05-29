/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.json.processors;

import net.sf.json.JSONException;
import net.sf.json.JsonConfig;

/**
 * Base interface for custom serialization per property.
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public interface JsonValueProcessor {
   /**
    * Processes the value an returns a suitable JSON value.
    *
    * @param value the input value
    * @return a valid JSON value that represents the input value
    * @throws JSONException if an error occurs during transformation
    */
   Object processArrayValue( Object value, JsonConfig jsonConfig );

   /**
    * Processes the value an returns a suitable JSON value.
    *
    * @param key the name of the property
    * @param value the value of the property
    * @return a valid JSON value that represents the input property
    * @throws JSONException if an error occurs during transformation
    */
   Object processObjectValue( String key, Object value, JsonConfig jsonConfig );
}