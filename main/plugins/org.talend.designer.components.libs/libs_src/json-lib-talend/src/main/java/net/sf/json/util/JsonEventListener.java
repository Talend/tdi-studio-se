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

package net.sf.json.util;

import net.sf.json.JSONException;

/**
 * Defines the contract to handle JsonEvents when building an object or array.
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public interface JsonEventListener {

   /**
    * Ttriggered when reaching the end of an array.
    */
   void onArrayEnd();

   /**
    * triggered when the start of an array is encountered.
    */
   void onArrayStart();

   /**
    * Triggered when an element has been added to the current array.
    *
    * @param index the index where the element was added
    * @param element the added element
    */
   void onElementAdded( int index, Object element );

   /**
    * Triggered when an exception is thrown.
    *
    * @param jsone the thrown exception
    */
   void onError( JSONException jsone );

   /**
    * triggered when reaching the end of an object.
    */
   void onObjectEnd();

   /**
    * Triggered when the start of an object is encountered.
    */
   void onObjectStart();

   /**
    * Triggered when a property is set on an object
    *
    * @param key the name of the property
    * @param value the value of the property
    * @param accumulated if the value has been accumulated over 'key'
    */
   void onPropertySet( String key, Object value, boolean accumulated );

   /**
    * Triggered when a warning is encountered.
    *
    * @param warning the warning message
    */
   void onWarning( String warning );
}