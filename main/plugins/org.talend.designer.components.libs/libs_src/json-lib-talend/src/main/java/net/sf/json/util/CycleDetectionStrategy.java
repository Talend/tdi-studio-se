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

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 * Base class for cycle detection in a hierarchy.<br>
 * The JSON spec forbides cycles in a hierarchy and most parsers will raise and
 * error when a cycle is detected. This class defines a contract for handling
 * those cycles and two base implementations:
 * <ul>
 * <li>STRICT - will throw a JSONException if a cycle is found.</li>
 * <li>LENIENT - will return an empty array or null object if a cycle is found.</li>
 * </ul>
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class CycleDetectionStrategy {
   public static final JSONArray IGNORE_PROPERTY_ARR = new JSONArray();
   public static final JSONObject IGNORE_PROPERTY_OBJ = new JSONObject();

   /** Returns empty array and null object */
   public static final CycleDetectionStrategy LENIENT = new LenientCycleDetectionStrategy();
   /**
    * Returns a special object (IGNORE_PROPERTY_OBJ) that indicates the entire
    * property should be ignored
    */
   public static final CycleDetectionStrategy NOPROP = new LenientNoRefCycleDetectionStrategy();
   /** Throws a JSONException */
   public static final CycleDetectionStrategy STRICT = new StrictCycleDetectionStrategy();

   /**
    * Handle a repeated reference<br>
    * Must return a valid JSONArray or null.
    *
    * @param reference the repeated reference.
    */
   public abstract JSONArray handleRepeatedReferenceAsArray( Object reference );

   /**
    * Handle a repeated reference<br>
    * Must return a valid JSONObject or null.
    *
    * @param reference the repeated reference.
    */
   public abstract JSONObject handleRepeatedReferenceAsObject( Object reference );

   private static final class LenientCycleDetectionStrategy extends CycleDetectionStrategy {
      public JSONArray handleRepeatedReferenceAsArray( Object reference ) {
         return new JSONArray();
      }

      public JSONObject handleRepeatedReferenceAsObject( Object reference ) {
         return new JSONObject( true );
      }
   }

   /**
    * A cycle detection strategy that prevents any mention of the possible
    * conflict from appearing.
    *
    * @author small
    */
   private static final class LenientNoRefCycleDetectionStrategy extends CycleDetectionStrategy {
      public JSONArray handleRepeatedReferenceAsArray( Object reference ) {
         return IGNORE_PROPERTY_ARR;
      }

      public JSONObject handleRepeatedReferenceAsObject( Object reference ) {
         return IGNORE_PROPERTY_OBJ;
      }
   }

   private static final class StrictCycleDetectionStrategy extends CycleDetectionStrategy {
      public JSONArray handleRepeatedReferenceAsArray( Object reference ) {
         throw new JSONException( "There is a cycle in the hierarchy!" );
      }

      public JSONObject handleRepeatedReferenceAsObject( Object reference ) {
         throw new JSONException( "There is a cycle in the hierarchy!" );
      }
   }
}