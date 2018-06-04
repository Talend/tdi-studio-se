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

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJavaIdentifierTransformer extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJavaIdentifierTransformer.class );
   }

   public TestJavaIdentifierTransformer( String testName ) {
      super( testName );
   }

   public void testCamelCase() {
      JavaIdentifierTransformer jit = JavaIdentifierTransformer.CAMEL_CASE;
      assertEquals( "camelCase", jit.transformToJavaIdentifier( "camel case" ) );
      assertEquals( "camelCase", jit.transformToJavaIdentifier( "@camel case" ) );
      assertEquals( "$camelCase", jit.transformToJavaIdentifier( "$camel case" ) );
      assertEquals( "camelCase", jit.transformToJavaIdentifier( "camel@case" ) );
      assertEquals( "camelCase", jit.transformToJavaIdentifier( "camel @case" ) );
      assertEquals( "camelCase", jit.transformToJavaIdentifier( "camel@@case" ) );
      assertEquals( "camelCase", jit.transformToJavaIdentifier( "camel@ @case" ) );
   }

   public void testUnderscore() {
      JavaIdentifierTransformer jit = JavaIdentifierTransformer.UNDERSCORE;
      assertEquals( "under_score", jit.transformToJavaIdentifier( "under score" ) );
      assertEquals( "under_score", jit.transformToJavaIdentifier( "@under score" ) );
      assertEquals( "$under_score", jit.transformToJavaIdentifier( "$under score" ) );
      assertEquals( "under_score", jit.transformToJavaIdentifier( "under@score" ) );
      assertEquals( "under_score", jit.transformToJavaIdentifier( "under score" ) );
      assertEquals( "under_score", jit.transformToJavaIdentifier( "under@@score" ) );
      assertEquals( "under_score", jit.transformToJavaIdentifier( "under@ @score" ) );
      assertEquals( "under_score", jit.transformToJavaIdentifier( "under score " ) );
   }

   public void testWhitespace() {
      JavaIdentifierTransformer jit = JavaIdentifierTransformer.WHITESPACE;
      assertEquals( "whitespace", jit.transformToJavaIdentifier( "white space" ) );
      assertEquals( "whitespace", jit.transformToJavaIdentifier( "@white space" ) );
      assertEquals( "$whitespace", jit.transformToJavaIdentifier( "$white space" ) );
      assertEquals( "whitespace", jit.transformToJavaIdentifier( "white@space" ) );
      assertEquals( "whitespace", jit.transformToJavaIdentifier( "white@@space" ) );
      assertEquals( "whitespace", jit.transformToJavaIdentifier( "white@ @space" ) );
      assertEquals( "whitespace", jit.transformToJavaIdentifier( "white space " ) );
   }
}