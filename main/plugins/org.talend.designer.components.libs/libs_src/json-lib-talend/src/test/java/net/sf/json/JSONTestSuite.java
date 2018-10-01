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

import junit.framework.TestSuite;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class JSONTestSuite extends TestSuite {
   public static TestSuite suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.setName( "json-lib" );

      suite.addTest( net.sf.json.AllTests.suite() );
      suite.addTest( net.sf.json.regexp.AllTests.suite() );
      suite.addTest( net.sf.json.filters.AllTests.suite() );
      suite.addTest( net.sf.json.processors.AllTests.suite() );
      suite.addTest( net.sf.json.util.AllTests.suite() );
      suite.addTest( net.sf.json.xml.AllTests.suite() );
      suite.addTest( net.sf.json.test.AllTests.suite() );

      return suite;
   }
}