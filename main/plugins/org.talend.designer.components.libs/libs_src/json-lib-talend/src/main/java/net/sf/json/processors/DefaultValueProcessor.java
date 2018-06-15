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

/**
 * Defines the default value for a type when its value is null.
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public interface DefaultValueProcessor {
   /**
    * Returns an appropriate default value for a type.
    *
    * @param type
    * @return the default value for instances of type
    */
   Object getDefaultValue( Class type );
}