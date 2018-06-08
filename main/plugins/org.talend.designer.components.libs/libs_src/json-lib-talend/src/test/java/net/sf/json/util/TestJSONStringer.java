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

import junit.framework.TestCase;
import net.sf.json.JSONFunction;
import net.sf.json.JSONObject;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONStringer extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONStringer.class );
   }

   public TestJSONStringer( String testName ) {
      super( testName );
   }

   public void testCreateArray() {
      JSONBuilder b = new JSONStringer().array()
            .value( true )
            .value( 1.1d )
            .value( 2L )
            .value( "text" )
            .endArray();
      assertEquals( "[true,1.1,2,\"text\"]", b.toString() );
   }

   public void testCreateEmptyArray() {
      JSONBuilder b = new JSONStringer().array()
            .endArray();
      assertEquals( "[]", b.toString() );
   }

   public void testCreateEmptyArrayWithNullObjects() {
      JSONBuilder b = new JSONStringer().array()
            .value( null )
            .value( null )
            .endArray();
      assertEquals( "[null,null]", b.toString() );
   }

   public void testCreateEmptyObject() {
      JSONBuilder b = new JSONStringer().object()
            .endObject();
      assertEquals( "{}", b.toString() );
   }

   public void testCreateFunctionArray() {
      JSONBuilder b = new JSONStringer().array()
            .value( new JSONFunction( "var a = 1;" ) )
            .value( new JSONFunction( "var b = 2;" ) )
            .endArray();
      assertEquals( "[function(){ var a = 1; },function(){ var b = 2; }]", b.toString() );
   }

   public void testCreateSimpleObject() {
      JSONBuilder b = new JSONStringer().object()
            .key( "bool" )
            .value( true )
            .key( "numDouble" )
            .value( 1.1d )
            .key( "numInt" )
            .value( 2 )
            .key( "text" )
            .value( "text" )
            .key( "func" )
            .value( new JSONFunction( "var a = 1;" ) )
            .endObject();
      JSONObject jsonObj = JSONObject.fromObject( b.toString() );
      assertEquals( Boolean.TRUE, jsonObj.get( "bool" ) );
      assertEquals( new Double( 1.1d ), jsonObj.get( "numDouble" ) );
      assertEquals( new Long( 2 ).longValue(), ((Number) jsonObj.get( "numInt" )).longValue() );
      assertEquals( "text", jsonObj.get( "text" ) );
      assertTrue( JSONUtils.isFunction( jsonObj.get( "func" ) ) );
      assertEquals( "function(){ var a = 1; }", jsonObj.get( "func" )
            .toString() );
   }
}