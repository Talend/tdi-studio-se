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
public class TestJSONArrayEqualsHashCodeCompareTo extends TestCase {
   private static JSONArray strings;
   private static Map values = new HashMap();
   private static JSONArray values1;
   private static JSONArray values2;
   private static JSONArray values3;

   static{
      values.put( "int.1", Integer.valueOf( "1" ) );
      values.put( "int.2", Integer.valueOf( "2" ) );
      values.put( "long.1", Long.valueOf( "1" ) );
      values.put( "long.2", Long.valueOf( "2" ) );
      values.put( "string.1", "1" );
      values.put( "string.2", "2" );
      values.put( "boolean.1", Boolean.TRUE );
      values.put( "boolean.2", Boolean.FALSE );

      strings = new JSONArray().element( "1" )
            .element( "1" )
            .element( "true" )
            .element( "string" )
            .element( "function(){ return this; }" )
            .element( "[1,2,3]" );
      values.put( "JSONArray.strings", strings );
      values1 = new JSONArray().element( Integer.valueOf( "1" ) )
            .element( Long.valueOf( "1" ) )
            .element( Boolean.TRUE )
            .element( "string" )
            .element( new JSONFunction( "return this;" ) )
            .element( JSONArray.fromObject( new int[] { 1, 2, 3 } ) );
      values.put( "JSONArray.values.1", values1 );
      values2 = new JSONArray().element( Integer.valueOf( "1" ) )
            .element( Long.valueOf( "1" ) )
            .element( Boolean.TRUE )
            .element( "string" );
      values.put( "JSONObject.values.2", values2 );
      values3 = new JSONArray().element( Integer.valueOf( "2" ) )
            .element( Long.valueOf( "2" ) )
            .element( Boolean.FALSE )
            .element( "string2" );
      values.put( "JSONObject.values.3", values3 );
   }

   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONArrayEqualsHashCodeCompareTo.class );
   }

   public TestJSONArrayEqualsHashCodeCompareTo( String name ) {
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

   public void testEquals_different_elements_same_size() {
      assertFalse( values2.equals( values3 ) );
      assertFalse( values3.equals( values2 ) );
   }

   public void testEquals_null() {
      assertFalse( strings.equals( null ) );
   }

   public void testEquals_object() {
      assertFalse( strings.equals( new Object() ) );
   }

   public void testEquals_same_object() {
      assertTrue( strings.equals( strings ) );
   }

   public void testEquals_same_size_similar_values() {
      assertTrue( strings.equals( values1 ) );
   }

   public void testHashCode_different_elements_same_size() {
      assertFalse( values2.hashCode() == values3.hashCode() );
   }
}