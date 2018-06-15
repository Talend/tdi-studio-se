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

package net.sf.json.sample;

import net.sf.json.JSONException;
import net.sf.json.util.JsonEventListener;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class JsonEventAdpater implements JsonEventListener {
   private int arrayEnd = 0;
   private int arrayStart = 0;
   private int elementAdded = 0;
   private int error = 0;
   private int objectEnd = 0;
   private int objectStart = 0;
   private int propertySet = 0;
   private int warning = 0;

   public int getArrayEnd() {
      return arrayEnd;
   }

   public int getArrayStart() {
      return arrayStart;
   }

   public int getElementAdded() {
      return elementAdded;
   }

   public int getError() {
      return error;
   }

   public int getObjectEnd() {
      return objectEnd;
   }

   public int getObjectStart() {
      return objectStart;
   }

   public int getPropertySet() {
      return propertySet;
   }

   public int getWarning() {
      return warning;
   }

   public void onArrayEnd() {
      arrayEnd++;
   }

   public void onArrayStart() {
      arrayStart++;
   }

   public void onElementAdded( int index, Object element ) {
      elementAdded++;
   }

   public void onError( JSONException jsone ) {
      error++;
   }

   public void onObjectEnd() {
      objectEnd++;
   }

   public void onObjectStart() {
      objectStart++;
   }

   public void onPropertySet( String key, Object value, boolean accumulated ) {
      propertySet++;
   }

   public void onWarning( String warning ) {
      this.warning++;
   }

   public void reset() {
      objectStart = 0;
      objectEnd = 0;
      arrayStart = 0;
      arrayEnd = 0;
      error = 0;
      warning = 0;
      propertySet = 0;
      elementAdded = 0;
   }
}