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

import java.io.StringWriter;

import junit.framework.TestCase;
import net.sf.json.JSONFunction;
import net.sf.json.JSONObject;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONBuilder extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONBuilder.class );
   }

   public TestJSONBuilder( String testName ) {
      super( testName );
   }

   public void testCreateArray() {
      StringWriter w = new StringWriter();
      new JSONBuilder( w ).array()
            .value( true )
            .value( 1.1d )
            .value( 2L )
            .value( "text" )
            .endArray();
      assertEquals( "[true,1.1,2,\"text\"]", w.toString() );
   }

   public void testCreateEmptyArray() {
      StringWriter w = new StringWriter();
      new JSONBuilder( w ).array()
            .endArray();
      assertEquals( "[]", w.toString() );
   }

   public void testCreateEmptyArrayWithNullObjects() {
      StringWriter w = new StringWriter();
      new JSONBuilder( w ).array()
            .value( null )
            .value( null )
            .endArray();
      assertEquals( "[null,null]", w.toString() );
   }

   public void testCreateEmptyObject() {
      StringWriter w = new StringWriter();
      new JSONBuilder( w ).object()
            .endObject();
      assertEquals( "{}", w.toString() );
   }

   public void testCreateFunctionArray() {
      StringWriter w = new StringWriter();
      new JSONBuilder( w ).array()
            .value( new JSONFunction( "var a = 1;" ) )
            .value( new JSONFunction( "var b = 2;" ) )
            .endArray();
      assertEquals( "[function(){ var a = 1; },function(){ var b = 2; }]", w.toString() );
   }

   public void testCreateSimpleObject() {
      StringWriter w = new StringWriter();
      new JSONBuilder( w ).object()
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
      JSONObject jsonObj = JSONObject.fromObject( w.toString() );
      assertEquals( Boolean.TRUE, jsonObj.get( "bool" ) );
      assertEquals( new Double( 1.1d ), jsonObj.get( "numDouble" ) );
      assertEquals( new Long( 2 ).longValue(), ((Number) jsonObj.get( "numInt" )).longValue() );
      assertEquals( "text", jsonObj.get( "text" ) );
      assertTrue( JSONUtils.isFunction( jsonObj.get( "func" ) ) );
      assertEquals( "function(){ var a = 1; }", jsonObj.get( "func" )
            .toString() );
   }
}