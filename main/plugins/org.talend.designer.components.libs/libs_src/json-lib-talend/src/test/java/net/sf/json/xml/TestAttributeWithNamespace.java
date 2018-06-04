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

import junit.framework.TestCase;
import net.sf.json.JSONObject;
import net.sf.json.test.JSONAssert;


/**
 * @author Tobias Sodergren <tobias.sodergren@infomaker.se>
 */
public class TestAttributeWithNamespace extends TestCase {

   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestAttributeWithNamespace.class );
   }

   public TestAttributeWithNamespace( String name ) {
      super( name );
   }

   public void testShouldCreateCorrectJson() throws Exception {

      XMLSerializer reader = new XMLSerializer();

      JSONObject actual = (JSONObject) reader.read( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Document DOMVersion=\"8.0\" Self=\"d\">" +
            "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 5.3-c011 66.145661, 2012/02/06-14:56:27\">" +
            "</x:xmpmeta>" +
            "</Document>" );

      JSONObject expected = new JSONObject()
            .element( "@DOMVersion", "8.0" )
            .element( "@Self", "d" )
            .element( "x:xmpmeta",
                  new JSONObject()
                        .element( "@xmlns:x", "adobe:ns:meta/" )
                        .element( "@x:xmptk", "Adobe XMP Core 5.3-c011 66.145661, 2012/02/06-14:56:27" ) );

      JSONAssert.assertEquals( expected, actual );
   }
}