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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONObjectEqualsHashCodeCompareTo extends TestCase {
   private static JSONObject strings;
   private static Map values = new HashMap();
   private static JSONObject values1;
   private static JSONObject values2;
   private static JSONObject values3;

   static{
      values.put( "JSONObject.null.1", new JSONObject( true ) );
      values.put( "JSONObject.null.2", new JSONObject( true ) );
      values.put( "int.1", Integer.valueOf( "1" ) );
      values.put( "int.2", Integer.valueOf( "2" ) );
      values.put( "long.1", Long.valueOf( "1" ) );
      values.put( "long.2", Long.valueOf( "2" ) );
      values.put( "string.1", "1" );
      values.put( "string.2", "2" );
      values.put( "boolean.1", Boolean.TRUE );
      values.put( "boolean.2", Boolean.FALSE );

      strings = new JSONObject().element( "int", "1" )
            .element( "long", "1" )
            .element( "boolean", "true" )
            .element( "string", "string" )
            .element( "func", "function(){ return this; }" )
            .element( "array", "[1,2,3]" );
      values.put( "JSONObject.strings", strings );
      values1 = new JSONObject().element( "int", Integer.valueOf( "1" ) )
            .element( "long", Long.valueOf( "1" ) )
            .element( "boolean", Boolean.TRUE )
            .element( "string", "string" )
            .element( "func", new JSONFunction( "return this;" ) )
            .element( "array", JSONArray.fromObject( new int[] { 1, 2, 3 } ) );
      values.put( "JSONObject.values.1", values1 );
      values2 = new JSONObject().element( "int", Integer.valueOf( "1" ) )
            .element( "long", Long.valueOf( "1" ) )
            .element( "boolean", Boolean.TRUE )
            .element( "string", "string" );
      values.put( "JSONObject.values.2", values2 );
      values3 = new JSONObject().element( "int", Integer.valueOf( "2" ) )
            .element( "long", Long.valueOf( "2" ) )
            .element( "boolean", Boolean.FALSE )
            .element( "string", "string2" );
      values.put( "JSONObject.values.3", values3 );
   }

   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONObjectEqualsHashCodeCompareTo.class );
   }

   public TestJSONObjectEqualsHashCodeCompareTo( String name ) {
      super( name );
   }

   public void testCompareTo_different_size() {
      assertEquals( -1, values2.compareTo( strings ) );
      assertEquals( 1, strings.compareTo( values2 ) );
   }

   public void testCompareTo_null() {
      assertEquals( -1, strings.compareTo( null ) );
   }

   public void testCompareTo_object() {
      assertEquals( -1, strings.compareTo( new Object() ) );
   }

   public void testCompareTo_same_array() {
      assertEquals( 0, strings.compareTo( strings ) );
   }

   public void testCompareTo_same_size_different_values() {
      assertEquals( -1, values2.compareTo( values3 ) );
   }

   public void testCompareTo_same_size_similar_values() {
      assertEquals( 0, strings.compareTo( values1 ) );
   }

   public void testEquals_different_key_same_size() {
      JSONObject a = new JSONObject().element( "key1", "string" );
      JSONObject b = new JSONObject().element( "key2", "json" );
      assertFalse( a.equals( b ) );
      assertFalse( b.equals( a ) );
   }

   public void testEquals_different_sizes() {
      assertFalse( values.get( "JSONObject.values.1" )
            .equals( values.get( "JSONObject.values.2" ) ) );
   }

   public void testEquals_nullObject_other() {
      assertFalse( values.get( "JSONObject.null.1" )
            .equals( values.get( "JSONObject.strings" ) ) );
   }

   public void testEquals_nullObjects_different() {
      assertTrue( values.get( "JSONObject.null.1" )
            .equals( values.get( "JSONObject.null.2" ) ) );
   }

   public void testEquals_other_nullObject() {
      assertFalse( values.get( "JSONObject.strings" )
            .equals( values.get( "JSONObject.null.1" ) ) );
   }

   public void testEquals_same() {
      assertTrue( values.get( "JSONObject.null.1" )
            .equals( values.get( "JSONObject.null.1" ) ) );
   }

   public void testEquals_same_key_different_value() {
      JSONObject a = new JSONObject().element( "key", "string" );
      JSONObject b = new JSONObject().element( "key", "json" );
      assertFalse( a.equals( b ) );
      assertFalse( b.equals( a ) );
   }

   public void testEquals_strings_values() {
      assertTrue( values.get( "JSONObject.strings" )
            .equals( values.get( "JSONObject.values.1" ) ) );
   }

   public void testEquals_to_null() {
      assertFalse( values.get( "JSONObject.null.1" )
            .equals( null ) );
   }

   public void testEquals_to_other() {
      assertFalse( values.get( "JSONObject.null.1" )
            .equals( new Object() ) );
   }

   public void testEquals_values_strings() {
      assertTrue( values.get( "JSONObject.values.1" )
            .equals( values.get( "JSONObject.strings" ) ) );
   }

   public void testHashCode_different_size() {
      assertFalse( values.get( "JSONObject.values.1" )
            .hashCode() == values.get( "JSONObject.values.2" )
            .hashCode() );
   }

   public void testHashCode_nullObject_other() {
      assertFalse( values.get( "JSONObject.null.1" )
            .hashCode() == values.get( "JSONObject.strings" )
            .hashCode() );
   }

   public void testHashCode_nullObjects_different() {
      assertTrue( values.get( "JSONObject.null.1" )
            .hashCode() == values.get( "JSONObject.null.2" )
            .hashCode() );
   }

   public void testHashCode_other_nullObject() {
      assertFalse( values.get( "JSONObject.strings" )
            .hashCode() == values.get( "JSONObject.null.1" )
            .hashCode() );
   }

   public void testHashCode_same() {
      assertTrue( values.get( "JSONObject.null.1" )
            .hashCode() == values.get( "JSONObject.null.1" )
            .hashCode() );
   }

   public void testHashCode_same_key_different_value() {
      JSONObject a = new JSONObject().element( "key", "string" );
      JSONObject b = new JSONObject().element( "key", "json" );
      assertFalse( a.hashCode() == b.hashCode() );
   }

   public void testHashCode_strings_values() {
      assertTrue( values.get( "JSONObject.strings" )
            .hashCode() == values.get( "JSONObject.values.1" )
            .hashCode() );
   }

   public void testHashCode_to_other() {
      assertFalse( values.get( "JSONObject.null.1" )
            .hashCode() == new Object().hashCode() );
   }

   public void testHashCode_values_strings() {
      assertTrue( values.get( "JSONObject.values.1" )
            .hashCode() == values.get( "JSONObject.strings" )
            .hashCode() );
   }
}