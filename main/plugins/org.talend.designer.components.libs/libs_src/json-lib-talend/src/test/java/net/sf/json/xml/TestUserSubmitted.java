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
import net.sf.json.Assertions;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.test.JSONAssert;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestUserSubmitted extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestUserSubmitted.class );
   }

   public TestUserSubmitted( String name ) {
      super( name );
   }

   public void testReadFromXMLToJSON_1735732() throws Exception {
      // bug 1735732

      XMLSerializer xmlSerializer = new XMLSerializer();

      JSONObject actual = (JSONObject) xmlSerializer.readFromFile( "net/sf/json/xml/1735732.xml" );
      JSONObject expected = new JSONObject().element( "@xmlns:ns2",
            "http://schemas.foo.com/HelloWorld" )
            .element( "item", new JSONObject().element( "age", "2 5" )
                  .element( "name", "emp1" ) )
            .accumulate( "item", new JSONObject().element( "age", "2" )
                  .element( "name", "emp2" ) );
      JSONAssert.assertEquals( expected, actual );
   }

   public void testReadFromXMLToJSON_1739066() throws Exception {
      // bug 1739066

      XMLSerializer xmlSerializer = new XMLSerializer();
      xmlSerializer.setTrimSpaces( true );

      JSONObject actual = (JSONObject) xmlSerializer.readFromFile( "net/sf/json/xml/1739066.xml" );
      JSONObject expected = new JSONObject().element( "Address", "http://localhost:0/te stToString" )
            .element( "@xmlns", "http://www.w3.org/2005/08/addressing" )
            .element(
                  "Metadata",
                  new JSONObject().element( "@xmlns:wsa-wsdl",
                        "http://www.w3.org/2006/01/wsdl-instance" )
                        .element( "@wsa-wsdl:wsdlLocation", "file:///b.wsdl" )
                        .element(
                              "ns3:InterfaceName",
                              new JSONObject().element( "@xmlns:tns", "http://com.iona.cxf/GreetMe" )
                                    .element( "@xmlns:ns3",
                                          "http://www.w3.org/2005/02/addressing/wsdl" )
                                    .element( "#text", "tns:GreetMePortType" ) )
                        .element(
                              "ns3:ServiceName",
                              new JSONObject().element( "@EndpointName", "GreetMePort" )
                                    .element( "@xmlns:tns", "http://com.iona.cxf/GreetMe" )
                                    .element( "@xmlns:ns3",
                                          "http://www.w3.org/2005/02/addressing/wsdl" )
                                    .element( "#text", "tns:GreetMeService" ) ) );
      JSONAssert.assertEquals( expected, actual );
   }

   public void testIgnoreWhitespaceWhileReading() {
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<ARCXML version=\"1.1\">\n"
            + "<RESPONSE>\n"
            + "<FEATURES>\n"
            + "<FEATURE>\n"
            + "<ENVELOPE minx=\"-178.216552734375\" miny=\"18.9254779815674\" maxx=\"179.775939941406\" maxy=\"71.3514404296875\"/>\n"
            + "<FIELDS>\n"
            + "<FIELD name=\"FIPS_CNTRY\" value=\"US\" />\n"
            + "<FIELD name=\"GMI_CNTRY\" value=\"USA\" />\n"
            + "<FIELD name=\"ISO_2DIGIT\" value=\"US\" />\n"
            + "<FIELD name=\"ISO_3DIGIT\" value=\"USA\" />\n"
            + "<FIELD name=\"CNTRY_NAME\" value=\"United States\" />\n"
            + "<FIELD name=\"LONG_NAME\" value=\"United States\" />\n"
            + "<FIELD name=\"SOVEREIGN\" value=\"United States\" />\n"
            + "<FIELD name=\"POP_CNTRY\" value=\"258833000\" />\n"
            + "<FIELD name=\"CURR_TYPE\" value=\"US Dollar\" />\n"
            + "<FIELD name=\"CURR_CODE\" value=\"USD\" />\n"
            + "<FIELD name=\"LANDLOCKED\" value=\"N\" />\n"
            + "<FIELD name=\"SQKM\" value=\"9449365\" />\n"
            + "<FIELD name=\"SQMI\" value=\"3648399.75\" />\n"
            + "<FIELD name=\"COLORMAP\" value=\"5\" />\n"
            + "<FIELD name=\"#SHAPE#\" value=\"[Geometry]\" />\n"
            + "<FIELD name=\"#ID#\" value=\"234\" />\n"
            + "</FIELDS>\n"
            + "</FEATURE>\n"
            + "<FEATURECOUNT count=\"1\" hasmore=\"false\" />\n"
            + "<ENVELOPE minx=\"-178.216552734375\" miny=\"18.9254779815674\" maxx=\"179.775939941406\" maxy=\"71.3514404296875\"/>\n"
            + "</FEATURES>\n" + "</RESPONSE>\n" + "</ARCXML>\n";

      XMLSerializer xmlSerializer = new XMLSerializer();
      xmlSerializer.setSkipWhitespace( true );
      JSON json1 = xmlSerializer.read( xml );

      xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<ARCXML version=\"1.1\">"
            + "<RESPONSE>"
            + "<FEATURES>"
            + "<FEATURE>"
            + "<ENVELOPE minx=\"-178.216552734375\" miny=\"18.9254779815674\" maxx=\"179.775939941406\" maxy=\"71.3514404296875\"/>"
            + "<FIELDS>"
            + "<FIELD name=\"FIPS_CNTRY\" value=\"US\" />"
            + "<FIELD name=\"GMI_CNTRY\" value=\"USA\" />"
            + "<FIELD name=\"ISO_2DIGIT\" value=\"US\" />"
            + "<FIELD name=\"ISO_3DIGIT\" value=\"USA\" />"
            + "<FIELD name=\"CNTRY_NAME\" value=\"United States\" />"
            + "<FIELD name=\"LONG_NAME\" value=\"United States\" />"
            + "<FIELD name=\"SOVEREIGN\" value=\"United States\" />"
            + "<FIELD name=\"POP_CNTRY\" value=\"258833000\" />"
            + "<FIELD name=\"CURR_TYPE\" value=\"US Dollar\" />"
            + "<FIELD name=\"CURR_CODE\" value=\"USD\" />"
            + "<FIELD name=\"LANDLOCKED\" value=\"N\" />"
            + "<FIELD name=\"SQKM\" value=\"9449365\" />"
            + "<FIELD name=\"SQMI\" value=\"3648399.75\" />"
            + "<FIELD name=\"COLORMAP\" value=\"5\" />"
            + "<FIELD name=\"#SHAPE#\" value=\"[Geometry]\" />"
            + "<FIELD name=\"#ID#\" value=\"234\" />"
            + "</FIELDS>"
            + "</FEATURE>"
            + "<FEATURECOUNT count=\"1\" hasmore=\"false\" />"
            + "<ENVELOPE minx=\"-178.216552734375\" miny=\"18.9254779815674\" maxx=\"179.775939941406\" maxy=\"71.3514404296875\"/>"
            + "</FEATURES>" + "</RESPONSE>" + "</ARCXML>";
      JSON json2 = xmlSerializer.read( xml );
      Assertions.assertEquals( json2, json1 );
   }

   public void testXMLRoundtrip() {
      String json = "{\"entries\": [ { \"credits\": \"p1\", \"id\": 1, \"status\": true, \"text\": \"1\" }, { \"credits\": \"p2\", \"id\": 2, \"status\": true, \"text\": \"2\" } ]}";
      JSONObject json1 = JSONObject.fromObject( json );
      XMLSerializer xmlSerializer = new XMLSerializer();
      String xml =  xmlSerializer.write(json1);
      JSONObject json2 = (JSONObject) xmlSerializer.read( xml );
      JSONAssert.assertEquals( json1, json2 );
      assertTrue(json1.getJSONArray( "entries" ).getJSONObject( 0 ).get( "id" ) instanceof Integer );
      assertTrue(json2.getJSONArray( "entries" ).getJSONObject( 0 ).get( "id" ) instanceof Integer );
   }
   
   public void testXMLWithArraySingleElement() {
      String testXML =
                        "    <rate>" +
                        "      <rateBreakdown>\n" +
                        "        <rate>\n" +
                        "          <date>\n" +
                        "            <day>15</day>\n" +
                        "            <month>1</month>\n" +
                        "            <year>2007</year>\n" +
                        "          </date>\n" +
                        "          <amount>109.74</amount>\n" +
                        "        </rate>\n" +
                        "      </rateBreakdown>" +
                        "      <totalAmount>219.48</totalAmount>\n" +
                        "    </rate>";

      JSON expected = JSONSerializer.toJSON("{\"rate\":{\"rateBreakdown\":[{\"amount\":\"109.74\",\"date\":{\"month\":\"1\",\"day\":\"15\",\"year\":\"2007\"}}],\"totalAmount\":\"219.48\"}}");

      // rate.rateBreakdown.rate should be a single entry array.

      JSONObject actual = convertXML( testXML, "rate" );
      assertNotNull( actual );
      Assertions.assertEquals( expected, actual );
   }

   public void testXMLWithoutArray() {
      String testXML =
                        "    <rate>" +
                        "      <rateBreakdown>\n" +
                        "        <rate>\n" +
                        "          <date>\n" +
                        "            <day>15</day>\n" +
                        "            <month>1</month>\n" +
                        "            <year>2007</year>\n" +
                        "          </date>\n" +
                        "          <amount>109.74</amount>\n" +
                        "        </rate>\n" +
                        "      </rateBreakdown>" +
                        "      <totalAmount>219.48</totalAmount>\n" +
                        "    </rate>";

      JSON expected = JSONSerializer.toJSON("{\"rate\":{\"rateBreakdown\":{\"rate\":{\"amount\":\"109.74\",\"date\":{\"month\":\"1\",\"day\":\"15\",\"year\":\"2007\"}}},\"totalAmount\":\"219.48\"}}");

      // rate.rateBreakdown.rate should be a single entry array.

      JSONObject actual = convertXML( testXML );
      assertNotNull( actual );
      Assertions.assertEquals( expected, actual );
   }

   private JSONObject convertXML( String testXML) {
      JSON jsonElement = getSerializer().read( testXML );
      return (JSONObject) jsonElement;
   }

   private JSONObject convertXML( String testXML, String arrayName ) {
      final XMLSerializer xmlSerializer = getSerializer();
      xmlSerializer.setArrayName( arrayName );
      JSON jsonElement = xmlSerializer.read( testXML );
      return (JSONObject) jsonElement;
   }

   private XMLSerializer getSerializer() {
      XMLSerializer xmlSerializer = new XMLSerializer();
      xmlSerializer.setSkipWhitespace( true );
      xmlSerializer.setForceTopLevelObject( true );
      return xmlSerializer;
   }
}