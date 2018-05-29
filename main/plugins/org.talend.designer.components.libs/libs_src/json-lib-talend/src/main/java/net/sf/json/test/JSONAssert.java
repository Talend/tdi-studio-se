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

package net.sf.json.test;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import junit.framework.Assert;
import net.sf.ezmorph.Morpher;
import net.sf.ezmorph.object.IdentityObjectMorpher;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONFunction;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.util.JSONUtils;

/**
 * Provides assertions on equality for JSON strings and JSON types.
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class JSONAssert extends Assert {
   /**
    * Asserts that two JSON values are equal.
    */
   public static void assertEquals( JSON expected, JSON actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONArrays are equal.
    */
   public static void assertEquals( JSONArray expected, JSONArray actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONArrays are equal.
    */
   public static void assertEquals( JSONArray expected, String actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONFunctions are equal.
    */
   public static void assertEquals( JSONFunction expected, String actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONNulls are equal.
    */
   public static void assertEquals( JSONNull expected, String actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONObjects are equal.
    */
   public static void assertEquals( JSONObject expected, JSONObject actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONObjects are equal.
    */
   public static void assertEquals( JSONObject expected, String actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSON values are equal.
    */
   public static void assertEquals( String message, JSON expected, JSON actual ) {
      String header = message == null ? "" : message + ": ";
      if( expected == null ){
         fail( header + "expected was null" );
      }
      if( actual == null ){
         fail( header + "actual was null" );
      }
      if( expected == actual || expected.equals( actual ) ){
         return;
      }

      if( expected instanceof JSONArray ){
         if( actual instanceof JSONArray ){
            assertEquals( header, (JSONArray) expected, (JSONArray) actual );
         }else{
            fail( header + "actual is not a JSONArray" );
         }
      }else if( expected instanceof JSONObject ){
         if( actual instanceof JSONObject ){
            assertEquals( header, (JSONObject) expected, (JSONObject) actual );
         }else{
            fail( header + "actual is not a JSONObject" );
         }
      }else if( expected instanceof JSONNull ){
         if( actual instanceof JSONNull ){
            return;
         }else{
            fail( header + "actual is not a JSONNull" );
         }
      }
   }

   /**
    * Asserts that two JSONArrays are equal.
    */
   public static void assertEquals( String expected, JSONArray actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONArrays are equal.
    */
   public static void assertEquals( String message, JSONArray expected, JSONArray actual ) {
      String header = message == null ? "" : message + ": ";
      if( expected == null ){
         fail( header + "expected array was null" );
      }
      if( actual == null ){
         fail( header + "actual array was null" );
      }
      if( expected == actual || expected.equals( actual ) ){
         return;
      }
      if( actual.size() != expected.size() ){
         fail( header + "arrays sizes differed, expected.length()=" + expected.size()
               + " actual.length()=" + actual.size() );
      }

      int max = expected.size();
      for( int i = 0; i < max; i++ ){
         Object o1 = expected.get( i );
         Object o2 = actual.get( i );

         // handle nulls
         if( JSONNull.getInstance()
               .equals( o1 ) ){
            if( JSONNull.getInstance()
                  .equals( o2 ) ){
               continue;
            }else{
               fail( header + "arrays first differed at element [" + i + "];" );
            }
         }else{
            if( JSONNull.getInstance()
                  .equals( o2 ) ){
               fail( header + "arrays first differed at element [" + i + "];" );
            }
         }

         if( o1 instanceof JSONArray && o2 instanceof JSONArray ){
            JSONArray e = (JSONArray) o1;
            JSONArray a = (JSONArray) o2;
            assertEquals( header + "arrays first differed at element " + i + ";", e, a );
         }else{
            if( o1 instanceof String && o2 instanceof JSONFunction ){
               assertEquals( header + "arrays first differed at element [" + i + "];", (String) o1,
                     (JSONFunction) o2 );
            }else if( o1 instanceof JSONFunction && o2 instanceof String ){
               assertEquals( header + "arrays first differed at element [" + i + "];",
                     (JSONFunction) o1, (String) o2 );
            }else if( o1 instanceof JSONObject && o2 instanceof JSONObject ){
               assertEquals( header + "arrays first differed at element [" + i + "];",
                     (JSONObject) o1, (JSONObject) o2 );
            }else if( o1 instanceof JSONArray && o2 instanceof JSONArray ){
               assertEquals( header + "arrays first differed at element [" + i + "];",
                     (JSONArray) o1, (JSONArray) o2 );
            }else if( o1 instanceof JSONFunction && o2 instanceof JSONFunction ){
               assertEquals( header + "arrays first differed at element [" + i + "];",
                     (JSONFunction) o1, (JSONFunction) o2 );
            }else{
               if( o1 instanceof String ){
                  assertEquals( header + "arrays first differed at element [" + i + "];",
                        (String) o1, String.valueOf( o2 ) );
               }else if( o2 instanceof String ){
                  assertEquals( header + "arrays first differed at element [" + i + "];",
                        String.valueOf( o1 ), (String) o2 );
               }else{
                  Morpher m1 = JSONUtils.getMorpherRegistry()
                        .getMorpherFor( o1.getClass() );
                  Morpher m2 = JSONUtils.getMorpherRegistry()
                        .getMorpherFor( o2.getClass() );
                  if( m1 != null && m1 != IdentityObjectMorpher.getInstance() ){
                     assertEquals( header + "arrays first differed at element [" + i + "];", o1,
                           JSONUtils.getMorpherRegistry()
                                 .morph( o1.getClass(), o2 ) );
                  }else if( m2 != null && m2 != IdentityObjectMorpher.getInstance() ){
                     assertEquals( header + "arrays first differed at element [" + i + "];",
                           JSONUtils.getMorpherRegistry()
                                 .morph( o1.getClass(), o1 ), o2 );
                  }else{
                     assertEquals( header + "arrays first differed at element [" + i + "];", o1, o2 );
                  }
               }
            }
         }
      }
   }

   /**
    * Asserts that two JSONArrays are equal.
    */
   public static void assertEquals( String message, JSONArray expected, String actual ) {
      try{
         assertEquals( message, expected, JSONArray.fromObject( actual ) );
      }catch( JSONException e ){
         String header = message == null ? "" : message + ": ";
         fail( header + "actual is not a JSONArray" );
      }
   }

   /**
    * Asserts that two JSONFunctions are equal.
    */
   public static void assertEquals( String expected, JSONFunction actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONFunctions are equal.
    */
   public static void assertEquals( String message, JSONFunction expected, String actual ) {
      String header = message == null ? "" : message + ": ";
      if( expected == null ){
         fail( header + "expected function was null" );
      }
      if( actual == null ){
         fail( header + "actual string was null" );
      }

      try{
         assertEquals( header, expected, JSONFunction.parse( actual ) );
      }catch( JSONException jsone ){
         fail( header + "'" + actual + "' is not a function" );
      }
   }

   /**
    * Asserts that two JSONNulls are equal.
    */
   public static void assertEquals( String expected, JSONNull actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONNulls are equal.
    */
   public static void assertEquals( String message, JSONNull expected, String actual ) {
      String header = message == null ? "" : message + ": ";
      if( actual == null ){
         fail( header + "actual string was null" );
      }else if( expected == null ){
         assertEquals( header, "null", actual );
      }else{
         assertEquals( header, expected.toString(), actual );
      }
   }

   /**
    * Asserts that two JSONObjects are equal.
    */
   public static void assertEquals( String expected, JSONObject actual ) {
      assertEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSONObjects are equal.
    */
   public static void assertEquals( String message, JSONObject expected, JSONObject actual ) {
      String header = message == null ? "" : message + ": ";
      if( expected == null ){
         fail( header + "expected object was null" );
      }
      if( actual == null ){
         fail( header + "actual object was null" );
      }
      if( expected == actual /* || expected.equals( actual ) */){
         return;
      }

      if( expected.isNullObject() ){
         if( actual.isNullObject() ){
            return;
         }else{
            fail( header + "actual is not a null JSONObject" );
         }
      }else{
         if( actual.isNullObject() ){
            fail( header + "actual is a null JSONObject" );
         }
      }

      if( expected.names().size() != actual.names().size() ){
         fail( header + missingAndUnexpectedNames( expected, actual ));
      }
      
      for( Iterator keys = expected.keys(); keys.hasNext(); ){
         String key = (String) keys.next();
         Object o1 = expected.opt( key );
         Object o2 = actual.opt( key );

         if( JSONNull.getInstance()
               .equals( o1 ) ){
            if( JSONNull.getInstance()
                  .equals( o2 ) ){
               continue;
            }else{
               fail( header + "objects differed at key [" + key + "];" );
            }
         }else{
            if( JSONNull.getInstance()
                  .equals( o2 ) ){
               fail( header + "objects differed at key [" + key + "];" );
            }
         }

         if( o1 instanceof String && o2 instanceof JSONFunction ){
            assertEquals( header + "objects differed at key [" + key + "];", (String) o1,
                  (JSONFunction) o2 );
         }else if( o1 instanceof JSONFunction && o2 instanceof String ){
            assertEquals( header + "objects differed at key [" + key + "];", (JSONFunction) o1,
                  (String) o2 );
         }else if( o1 instanceof JSONObject && o2 instanceof JSONObject ){
            assertEquals( header + "objects differed at key [" + key + "];", (JSONObject) o1,
                  (JSONObject) o2 );
         }else if( o1 instanceof JSONArray && o2 instanceof JSONArray ){
            assertEquals( header + "objects differed at key [" + key + "];", (JSONArray) o1,
                  (JSONArray) o2 );
         }else if( o1 instanceof JSONFunction && o2 instanceof JSONFunction ){
            assertEquals( header + "objects differed at key [" + key + "];", (JSONFunction) o1,
                  (JSONFunction) o2 );
         }else{
            if( o1 instanceof String ){
               assertEquals( header + "objects differed at key [" + key + "];", (String) o1,
                     String.valueOf( o2 ) );
            }else if( o2 instanceof String ){
               assertEquals( header + "objects differed at key [" + key + "];",
                     String.valueOf( o1 ), (String) o2 );
            }else{
               Morpher m1 = JSONUtils.getMorpherRegistry()
                     .getMorpherFor( o1.getClass() );
               Morpher m2 = JSONUtils.getMorpherRegistry()
                     .getMorpherFor( o2.getClass() );
               if( m1 != null && m1 != IdentityObjectMorpher.getInstance() ){
                  assertEquals( header + "objects differed at key [" + key + "];", o1,
                        JSONUtils.getMorpherRegistry()
                              .morph( o1.getClass(), o2 ) );
               }else if( m2 != null && m2 != IdentityObjectMorpher.getInstance() ){
                  assertEquals( header + "objects differed at key [" + key + "];",
                        JSONUtils.getMorpherRegistry()
                              .morph( o1.getClass(), o1 ), o2 );
               }else{
                  assertEquals( header + "objects differed at key [" + key + "];", o1, o2 );
               }
            }
         }
      }
   }

   private static String missingAndUnexpectedNames(JSONObject expected,
         JSONObject actual) {
      String missingExpectedNames = missingExpectedNames( expected, actual );
      String unexpectedNames = unexpectedNames( expected, actual );
      if( missingExpectedNames != null && unexpectedNames != null ){
         return missingExpectedNames + ", " + unexpectedNames;
      }else if( missingExpectedNames != null ){
         return missingExpectedNames;
      }else{
         return unexpectedNames;
      }
   }

   private static String missingExpectedNames(JSONObject expected,
         JSONObject actual) {
      SortedSet keysInExpectedButNotInActual = new TreeSet( expected.keySet() );
      keysInExpectedButNotInActual.removeAll( actual.keySet() );
      if ( keysInExpectedButNotInActual.isEmpty() ) {
         return null;
      } else {
         return "missing expected names: [" + StringUtils.join( keysInExpectedButNotInActual, ", " ) + "]";
      }
   }

   private static String unexpectedNames(JSONObject expected, JSONObject actual) {
      SortedSet keysInActualButNotInExpected = new TreeSet( actual.keySet() );
      keysInActualButNotInExpected.removeAll( expected.keySet() );
      if ( keysInActualButNotInExpected.isEmpty() ) {
         return null;
      } else {
         return "unexpected names: [" + StringUtils.join( keysInActualButNotInExpected, ", " ) + "]";
      }
   }

   /**
    * Asserts that two JSONObjects are equal.
    */
   public static void assertEquals( String message, JSONObject expected, String actual ) {
      try{
         assertEquals( message, expected, JSONObject.fromObject( actual ) );
      }catch( JSONException e ){
         String header = message == null ? "" : message + ": ";
         fail( header + "actual is not a JSONObject" );
      }
   }

   /**
    * Asserts that two JSONArrays are equal.
    */
   public static void assertEquals( String message, String expected, JSONArray actual ) {
      try{
         assertEquals( message, JSONArray.fromObject( expected ), actual );
      }catch( JSONException e ){
         String header = message == null ? "" : message + ": ";
         fail( header + "expected is not a JSONArray" );
      }
   }

   /**
    * Asserts that two JSONFunctions are equal.
    */
   public static void assertEquals( String message, String expected, JSONFunction actual ) {
      String header = message == null ? "" : message + ": ";
      if( expected == null ){
         fail( header + "expected string was null" );
      }
      if( actual == null ){
         fail( header + "actual function was null" );
      }

      try{
         assertEquals( header, JSONFunction.parse( expected ), actual );
      }catch( JSONException jsone ){
         fail( header + "'" + expected + "' is not a function" );
      }
   }

   /**
    * Asserts that two JSONNulls are equal.
    */
   public static void assertEquals( String message, String expected, JSONNull actual ) {
      String header = message == null ? "" : message + ": ";
      if( expected == null ){
         fail( header + "expected was null" );
      }else if( actual == null ){
         assertEquals( header, expected, "null" );
      }else{
         assertEquals( header, expected, actual.toString() );
      }
   }

   /**
    * Asserts that two JSONObjects are equal.
    */
   public static void assertEquals( String message, String expected, JSONObject actual ) {
      try{
         assertEquals( message, JSONObject.fromObject( expected ), actual );
      }catch( JSONException e ){
         String header = message == null ? "" : message + ": ";
         fail( header + "expected is not a JSONObject" );
      }
   }

   /**
    * Asserts that two JSON strings are equal.
    */
   public static void assertJsonEquals( String expected, String actual ) {
      assertJsonEquals( null, expected, actual );
   }

   /**
    * Asserts that two JSON strings are equal.
    */
   public static void assertJsonEquals( String message, String expected, String actual ) {
      String header = message == null ? "" : message + ": ";
      if( expected == null ){
         fail( header + "expected was null" );
      }
      if( actual == null ){
         fail( header + "actual was null" );
      }

      JSON json1 = null;
      JSON json2 = null;
      try{
         json1 = JSONSerializer.toJSON( expected );
      }catch( JSONException jsone ){
         fail( header + "expected is not a valid JSON string" );
      }
      try{
         json2 = JSONSerializer.toJSON( actual );
      }catch( JSONException jsone ){
         fail( header + "actual is not a valid JSON string" );
      }
      assertEquals( header, json1, json2 );
   }

   /**
    * Asserts that a JSON value is not null.<br>
    * Fails if:
    * <ul>
    * <li>JSONNull.getInstance().equals( json )</li>
    * <li>((JSONObject) json).isNullObject()</li>
    * </ul>
    */
   public static void assertNotNull( JSON json ) {
      assertNotNull( null, json );
   }

   /**
    * Asserts that a JSON value is not null.<br>
    * Fails if:
    * <ul>
    * <li>JSONNull.getInstance().equals( json )</li>
    * <li>((JSONObject) json).isNullObject()</li>
    * </ul>
    */
   public static void assertNotNull( String message, JSON json ) {
      String header = message == null ? "" : message + ": ";
      if( json instanceof JSONObject ){
         assertFalse( header + "Object is null", ((JSONObject) json).isNullObject() );
      }else if( JSONNull.getInstance()
            .equals( json ) ){
         fail( header + "Object is null" );
      }
   }

   /**
    * Asserts that a JSON value is null.<br>
    * Fails if:
    * <ul>
    * <li>!JSONNull.getInstance().equals( json )</li>
    * <li>!((JSONObject) json).isNullObject()</li>
    * </ul>
    */
   public static void assertNull( JSON json ) {
      assertNull( null, json );
   }

   /**
    * Asserts that a JSON value is null.<br>
    * Fails if:
    * <ul>
    * <li>!JSONNull.getInstance().equals( json )</li>
    * <li>!((JSONObject) json).isNullObject()</li>
    * </ul>
    */
   public static void assertNull( String message, JSON json ) {
      String header = message == null ? "" : message + ": ";
      if( json instanceof JSONObject ){
         assertTrue( header + "Object is not null", ((JSONObject) json).isNullObject() );
      }else if( !JSONNull.getInstance()
            .equals( json ) ){
         fail( header + "Object is not null" );
      }
   }
}