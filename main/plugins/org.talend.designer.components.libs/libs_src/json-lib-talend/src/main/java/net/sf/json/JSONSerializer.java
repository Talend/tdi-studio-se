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

package net.sf.json;

import net.sf.json.util.JSONTokener;
import net.sf.json.util.JSONUtils;

/**
 * Transforms java objects into JSON and back.<br>
 * Transformation from java to JSON is pretty straightforward, but the other way
 * around needs certain configuration, otherwise the java objects produced will
 * be DynaBeans and Lists, because the JSON notation does not carry any
 * information on java classes.<br>
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class JSONSerializer {
   /**
    * Transform a JSON value to a java object.<br>
    * Depending on the configured values for conversion this will return a
    * DynaBean, a bean, a List, or and array.
    *
    * @param json a JSON value
    * @return depends on the nature of the source object (JSONObject, JSONArray,
    *         JSONNull).
    */
   public static Object toJava( JSON json ) {
      return toJava( json, new JsonConfig() );
   }

   /**
    * Transform a JSON value to a java object.<br>
    * Depending on the configured values for conversion this will return a
    * DynaBean, a bean, a List, or and array.
    *
    * @param json a JSON value
    * @param jsonConfig additional configuration
    * @return depends on the nature of the source object (JSONObject, JSONArray,
    *         JSONNull) and the configured rootClass, classMap and arrayMode
    */
   public static Object toJava( JSON json, JsonConfig jsonConfig ) {
      if( JSONUtils.isNull( json ) ){
         return null;
      }

      Object object = null;

      if( json instanceof JSONArray ){
         if( jsonConfig.getArrayMode() == JsonConfig.MODE_OBJECT_ARRAY ){
            object = JSONArray.toArray( (JSONArray) json, jsonConfig );
         }else{
            object = JSONArray.toCollection( (JSONArray) json, jsonConfig );
         }
      }else{
         object = JSONObject.toBean( (JSONObject) json, jsonConfig );
      }

      return object;
   }

   /**
    * Creates a JSONObject, JSONArray or a JSONNull from object.<br>
    * Accepts JSON formatted strings, Maps, arrays, Collections, DynaBeans and
    * JavaBeans.
    *
    * @param object any java Object
    * @throws JSONException if the object can not be converted
    */
   public static JSON toJSON( Object object ) {
      return toJSON( object, new JsonConfig() );
   }

   /**
    * Creates a JSONObject, JSONArray or a JSONNull from object.<br>
    * Accepts JSON formatted strings, Maps, arrays, Collections, DynaBeans and
    * JavaBeans.
    *
    * @param object any java Object
    * @param jsonConfig additional configuration
    * @throws JSONException if the object can not be converted
    */
   public static JSON toJSON( Object object, JsonConfig jsonConfig ) {
      JSON json = null;
      if( object == null ){
         json = JSONNull.getInstance();
      }else if( object instanceof JSONString ){
         json = toJSON( (JSONString) object, jsonConfig );
      }else if( object instanceof String ){
         json = toJSON( (String) object, jsonConfig );
      }else if( JSONUtils.isArray( object ) ){
         json = JSONArray.fromObject( object, jsonConfig );
      }else{
         try{
            json = JSONObject.fromObject( object, jsonConfig );
         }catch( JSONException e ){
            if( object instanceof JSONTokener ){
               ((JSONTokener) object).reset();
            }
            json = JSONArray.fromObject( object, jsonConfig );
         }
      }

      return json;
   }

   /**
    * Creates a JSONObject, JSONArray or a JSONNull from a JSONString.
    *
    * @throws JSONException if the string is not a valid JSON string
    */
   private static JSON toJSON( JSONString string, JsonConfig jsonConfig ) {
      return toJSON( string.toJSONString(), jsonConfig );
   }

   /**
    * Creates a JSONObject, JSONArray or a JSONNull from a JSONString.
    *
    * @throws JSONException if the string is not a valid JSON string
    */
   private static JSON toJSON( String string, JsonConfig jsonConfig ) {
      JSON json = null;
      if( string.startsWith( "[" ) ){
         json = JSONArray.fromObject( string, jsonConfig );
      }else if( string.startsWith( "{" ) ){
         json = JSONObject.fromObject( string, jsonConfig );
      }else if( "null".equals( string ) ){
         json = JSONNull.getInstance();
      }else{
         throw new JSONException( "Invalid JSON String" );
      }

      return json;
   }
}