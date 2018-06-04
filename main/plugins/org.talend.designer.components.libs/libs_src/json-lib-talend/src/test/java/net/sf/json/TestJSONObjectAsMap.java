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
public class TestJSONObjectAsMap extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONObjectAsMap.class );
   }

   private JSONObject jsonObject;

   public TestJSONObjectAsMap( String name ) {
      super( name );
   }

   public void testClear() {
      assertEquals( 6, jsonObject.size() );
      jsonObject.clear();
      assertEquals( 0, jsonObject.size() );
   }

   public void testContainsKey() {
      assertTrue( jsonObject.containsKey( "func" ) );
      assertFalse( jsonObject.containsKey( "bogus" ) );
   }

   public void testContainsValue() {
      assertTrue( jsonObject.containsValue( "string" ) );
   }

   public void testIsEmpty() {
      assertFalse( jsonObject.isEmpty() );
   }

   public void testPut() {
      String key = "key";
      Object value = "value";
      jsonObject.put( key, value );
      assertEquals( value, jsonObject.get( key ) );
   }

   public void testPutAll() {
      JSONObject json = new JSONObject();
      Map map = new HashMap();
      map.put( "key", "value" );
      json.putAll( map );
      assertEquals( 1, json.size() );
      assertEquals( "value", json.get( "key" ) );
      map.put( "key", "value2" );
      json.putAll( map );
      assertEquals( 1, json.size() );
      assertEquals( "value2", json.get( "key" ) );
   }

   public void testRemove() {
      assertTrue( jsonObject.has( "func" ) );
      jsonObject.remove( "func" );
      assertFalse( jsonObject.has( "func" ) );
   }

   protected void setUp() throws Exception {
      jsonObject = new JSONObject().element( "int", "1" )
            .element( "long", "1" )
            .element( "boolean", "true" )
            .element( "string", "string" )
            .element( "func", "function(){ return this; }" )
            .element( "array", "[1,2,3]" );
   }
}