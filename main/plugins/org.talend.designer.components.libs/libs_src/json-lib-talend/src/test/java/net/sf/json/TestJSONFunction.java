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

import net.sf.json.test.JSONAssert;
import junit.framework.TestCase;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONFunction extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONFunction.class );
   }

   public TestJSONFunction( String name ) {
      super( name );
   }

   public void testEquals() {
      JSONFunction expected = new JSONFunction( new String[] { "a" }, "return a;" );
      assertFalse( expected.equals( null ) );
      assertFalse( expected.equals( new Object() ) );
      assertFalse( expected.equals( "" ) );
      assertFalse( expected.equals( new JSONFunction( "return a;" ) ) );
      assertFalse( expected.equals( new JSONFunction( new String[] { "a" }, "return b;" ) ) );
      assertTrue( expected.equals( new JSONFunction( new String[] { "a" }, "return a;" ) ) );
   }

   public void testHashCode() {
      JSONFunction a = new JSONFunction( new String[] { "a,b" }, "return a+b;" );
      JSONFunction b = new JSONFunction( new String[] { "a,b" }, "return a+b;" );
      assertTrue( a.hashCode() == b.hashCode() );
   }

   public void testParse_String() {
      assertEquals( "function(){ return a; }", JSONFunction.parse( "function(){ return a; }" ).toString() );
   }

   public void testParse_String_withWhiteSpacechars() {
      assertEquals( "function(){ return a; }", JSONFunction.parse( "function() { return a; }" ).toString() );
      assertEquals( "function(){ return a; }", JSONFunction.parse( "function()  { return a; }" ).toString() );
      assertEquals( "function(){ return a; }", JSONFunction.parse( "function()\n{ return a; }" ).toString() );
      assertEquals( "function(){ return a; }", JSONFunction.parse( "function()\t{ return a; }" ).toString() );
   }
   
   public void testParse_withSingleArg(){
      JSONFunction expected = new JSONFunction(new String[]{"a"},"return this");
      JSONFunction actual = JSONFunction.parse( "function(a){ return this}" );
      JSONAssert.assertEquals( expected, actual );
   }
  
   public void testParse_withMultipleArgs(){
      JSONFunction expected = new JSONFunction(new String[]{"a","b"},"return this");
      JSONAssert.assertEquals( expected, JSONFunction.parse( "function(a,b){ return this }" ) );
      JSONAssert.assertEquals( expected, JSONFunction.parse( "function( a,b ){ return this }" ) );
      JSONAssert.assertEquals( expected, JSONFunction.parse( "function( a,b){ return this }" ) );
      JSONAssert.assertEquals( expected, JSONFunction.parse( "function(a, b ){ return this }" ) );
      JSONAssert.assertEquals( expected, JSONFunction.parse( "function( a, b ){ return this }" ) );
      JSONAssert.assertEquals( expected, JSONFunction.parse( "function ( a, b ){ return this }" ) );
      JSONAssert.assertEquals( expected, JSONFunction.parse( "function ( a, b ) { return this }" ) );
   }
}