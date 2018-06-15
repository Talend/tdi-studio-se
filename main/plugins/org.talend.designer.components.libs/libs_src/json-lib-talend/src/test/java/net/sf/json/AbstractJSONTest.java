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

import java.io.StringWriter;

import junit.framework.TestCase;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class AbstractJSONTest extends TestCase {
   public AbstractJSONTest( String name ) {
      super( name );
   }

   public void testIsArray() {
      boolean isArray = ((Boolean) getIsArrayExpectations()[0]).booleanValue();
      JSON json = (JSON) getIsArrayExpectations()[1];
      assertEquals( isArray, json.isArray() );
   }

   public void testToString() {
      String expected = (String) getToStringExpectations1()[0];
      JSON json = (JSON) getToStringExpectations1()[1];
      assertEquals( expected, json.toString() );
   }

   public void testToString_indentFactor() {
      String expected = (String) getToStringExpectations2()[0];
      JSON json = (JSON) getToStringExpectations2()[1];
      assertEquals( expected, json.toString( getIndentFactor() ) );
   }

   public void testToString_indentFactor_indent() {
      String expected = (String) getToStringExpectations3()[0];
      JSON json = (JSON) getToStringExpectations3()[1];
      assertEquals( expected, json.toString( getIndentFactor(), getIndent() ) );
   }

   public void testWrite() throws Exception {
      StringWriter w = new StringWriter();
      String expected = (String) getWriteExpectations()[0];
      JSON json = (JSON) getWriteExpectations()[1];
      json.write( w );
      assertEquals( expected, w.getBuffer()
            .toString() );
   }

   protected abstract int getIndent();

   protected abstract int getIndentFactor();

   protected abstract Object[] getIsArrayExpectations();

   protected abstract Object[] getToStringExpectations1();

   protected abstract Object[] getToStringExpectations2();

   protected abstract Object[] getToStringExpectations3();

   protected abstract Object[] getWriteExpectations();
}