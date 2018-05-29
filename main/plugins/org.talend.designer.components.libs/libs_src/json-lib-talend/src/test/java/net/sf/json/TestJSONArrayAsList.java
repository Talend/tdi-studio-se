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

import java.util.List;

import junit.framework.TestCase;
import net.sf.json.test.JSONAssert;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONArrayAsList extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONArrayAsList.class );
   }

   private JSONArray jsonArray;

   public TestJSONArrayAsList( String name ) {
      super( name );
   }

   public void testAdd() {
      assertEquals( 5, jsonArray.size() );
      jsonArray.add( "value" );
      assertEquals( 6, jsonArray.size() );
   }

   public void testAdd_index_value() {
      assertEquals( 5, jsonArray.size() );
      Object first = jsonArray.get( 0 );
      jsonArray.add( 0, "value" );
      assertEquals( 6, jsonArray.size() );
      assertEquals( "value", jsonArray.get( 0 ) );
      assertEquals( first, jsonArray.get( 1 ) );
   }

   public void testAddAll() {
      JSONArray array = new JSONArray();
      array.addAll( jsonArray );
      JSONAssert.assertEquals( jsonArray, array );
   }

   public void testAddAll_index_value() {
      JSONArray array = new JSONArray().element( "value" );
      array.addAll( 0, jsonArray );
      assertEquals( 6, array.size() );
      assertEquals( "value", array.get( 5 ) );
   }

   public void testClear() {
      assertEquals( 5, jsonArray.size() );
      jsonArray.clear();
      assertEquals( 0, jsonArray.size() );
   }

   public void testContains() {
      assertTrue( jsonArray.contains( "1" ) );
      assertFalse( jsonArray.contains( "2" ) );
   }

   public void testContainsAll() {
      assertTrue( jsonArray.containsAll( jsonArray ) );
   }

   public void testIndexOf() {
      jsonArray.element( "1" );
      assertEquals( 0, jsonArray.indexOf( "1" ) );
   }

   public void testIsEmpty() {
      assertFalse( jsonArray.isEmpty() );
   }

   public void testLastIndexOf() {
      jsonArray.element( "1" );
      assertEquals( 5, jsonArray.lastIndexOf( "1" ) );
   }

   public void testRemove() {
      assertEquals( 5, jsonArray.size() );
      jsonArray.remove( "string" );
      assertEquals( 4, jsonArray.size() );
      assertTrue( !jsonArray.contains( "string" ) );
   }

   public void testRemove_index() {
      assertEquals( 5, jsonArray.size() );
      jsonArray.remove( 2 );
      assertEquals( 4, jsonArray.size() );
      assertTrue( !jsonArray.contains( "string" ) );
   }

   public void testRemoveAll() {
      assertEquals( 5, jsonArray.size() );
      jsonArray.removeAll( jsonArray );
      assertEquals( 0, jsonArray.size() );
   }

   public void testRetainAll() {
      assertEquals( 5, jsonArray.size() );
      jsonArray.retainAll( jsonArray );
      assertEquals( 5, jsonArray.size() );
   }

   public void testSubList() {
      List actual = jsonArray.subList( 0, 3 );
      JSONArray expected = new JSONArray().element( "1" )
            .element( "true" )
            .element( "string" );
      JSONAssert.assertEquals( expected, JSONArray.fromObject( actual ) );
   }

   /*
    * public void testToArray() { } public void testToArray_array() { }
    */

   protected void setUp() throws Exception {
      jsonArray = new JSONArray().element( "1" )
            .element( "true" )
            .element( "string" )
            .element( "function(){ return this; }" )
            .element( "[1,2,3]" );
   }
}