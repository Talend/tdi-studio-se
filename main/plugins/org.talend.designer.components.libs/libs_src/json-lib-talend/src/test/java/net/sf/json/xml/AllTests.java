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

package net.sf.json.xml;

import junit.framework.TestSuite;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class AllTests extends TestSuite {
   public static TestSuite suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.setName( "xml" );

      suite.addTest( new TestSuite( TestXMLSerializer_reads.class ) );
      suite.addTest( new TestSuite( TestXMLSerializer_writes.class ) );
      suite.addTest( new TestSuite( TestUserSubmitted.class ) );
      suite.addTest( new TestSuite( TestAttributeWithNamespace.class ) );
      suite.addTest( new TestSuite( TestArrayAutoExpansion.class ) );
      suite.addTest( new TestSuite( TestXMLSerializer_writes_with_autoexpansion.class ) );
      suite.addTest( new TestSuite( TestXmlContainingTypeAttribute.class ) );
      suite.addTest( new TestSuite( TestXmlContainingCData.class ) );
      suite.addTest( new TestSuite( TestArrayAutoExpansion.class ) );
      suite.addTest( new TestSuite( TestXmlWithEntity.class ) );
      suite.addTest( new TestSuite( TestIdmlParsing.class ) );

      return suite;
   }
}