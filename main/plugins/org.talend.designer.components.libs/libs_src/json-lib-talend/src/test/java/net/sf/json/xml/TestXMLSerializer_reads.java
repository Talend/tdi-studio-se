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
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.test.JSONAssert;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestXMLSerializer_reads extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestXMLSerializer_reads.class );
   }

   private XMLSerializer xmlSerializer;

   public TestXMLSerializer_reads( String testName ) {
      super( testName );
   }

   public void testNullObjectArray() {
      String xml = "<a><e class=\"object\" null=\"true\"/><e class=\"object\" null=\"true\"/></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[null,null]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testNullObjectArray_noTypeHintsCompatibility() {
      String xml = "<a><e json_class=\"object\" json_null=\"true\"/><e json_class=\"object\" json_null=\"true\"/></a>";
      xmlSerializer.setTypeHintsCompatibility( false );
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[null,null]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testRead_nullObject() {
      String xml = "<o/>";
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testRead_nullObject_2() {
      String xml = "<o null=\"true\"/>";
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testRead_nullObject_2_noTypeHintsCompatibility() {
      String xml = "<o json_null=\"true\"/>";
      xmlSerializer.setTypeHintsCompatibility( false );
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testRead_nullObject_3() {
      String xml = "<o class=\"object\"/>";
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testRead_nullObject_3_noTypeHintsCompatibility() {
      String xml = "<o json_class=\"object\"/>";
      xmlSerializer.setTypeHintsCompatibility( false );
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testRead_nullObject_4() {
      String xml = "<o type=\"string\"/>";
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testRead_nullObject_4_noTypeHintsCompatibility() {
      String xml = "<o json_type=\"string\"/>";
      xmlSerializer.setTypeHintsCompatibility( false );
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testRead_nullObject_5() {
      String xml = "<o class=\"object\" type=\"string\"/>";
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testRead_nullObject_5_noTypeHintsCompatibility() {
      String xml = "<o json_class=\"object\" json_type=\"string\"/>";
      xmlSerializer.setTypeHintsCompatibility( false );
      JSON actual = xmlSerializer.read( xml );
      Assertions.assertEquals( JSONNull.getInstance(), actual );
   }

   public void testReadArray_forceTopLevelObject() {
      String xml = "<a>json</a>";
      xmlSerializer.setForceTopLevelObject( true );
      JSON actual = xmlSerializer.read( xml );
      JSON expected = new JSONObject().element( "a", new JSONArray().element( "json" ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadArray_withText() {
      String xml = "<a>json</a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = new JSONArray().element( "json" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadBooleanArray_withDefaultType() {
      String xml = "<a type=\"boolean\"><e>true</e><e>false</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[true,false]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadBooleanArray_withoutDefaultType() {
      String xml = "<a><e type=\"boolean\">true</e><e type=\"boolean\">false</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[true,false]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadFloatArray_withDefaultType() {
      String xml = "<a type=\"float\"><e>1.1</e><e>2.2</e><e>3.3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1.1,2.2,3.3]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadFloatArray_withoutDefaultType() {
      String xml = "<a><e type=\"float\">1.1</e><e type=\"float\">2.2</e><e type=\"float\">3.3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1.1,2.2,3.3]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadFunctionObject() {
      String xml = "<o><func params=\"a\" ><![CDATA[return a;]]></func></o>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONObject.fromObject( "{func:function(a){ return a; }}" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadFunctionObject_noTypeHintsCompatibility() {
      String xml = "<o><func json_params=\"a\" ><![CDATA[return a;]]></func></o>";
      xmlSerializer.setTypeHintsCompatibility( false );
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONObject.fromObject( "{func:function(a){ return a; }}" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadFunctionObject_withDefaultType() {
      String xml = "<o type=\"function\"><func params=\"a\"><![CDATA[return a;]]></func></o>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONObject.fromObject( "{func:function(a){ return a; }}" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadFunctionObject_withoutDefaultType() {
      String xml = "<o><func params=\"a\" type=\"function\"><![CDATA[return a;]]></func></o>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONObject.fromObject( "{func:function(a){ return a; }}" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadIntegerArray_withDefaultType() {
      String xml = "<a type=\"integer\"><e>1</e><e>2</e><e>3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1,2,3]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadIntegerArray_withoutDefaultType() {
      String xml = "<a><e type=\"integer\">1</e><e type=\"integer\">2</e><e type=\"integer\">3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1,2,3]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMixedArray_withDefaultType() {
      String xml = "<a type=\"string\"><e type=\"boolean\">true</e><e type=\"number\">2.2</e><e>3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[true,2.2,\"3\"]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMixedArray_withoutDefaultType() {
      String xml = "<a><e type=\"boolean\">true</e><e type=\"number\">2.2</e><e type=\"string\">3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[true,2.2,\"3\"]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiBooleanArray_withDefaultType() {
      String xml = "<a type=\"boolean\"><e>true</e><e>false</e><e class=\"array\"><e>false</e><e>true</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[true,false,[false,true]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiBooleanArray_withoutDefaultType() {
      String xml = "<a><e type=\"boolean\">true</e><e type=\"boolean\">false</e><e class=\"array\"><e type=\"boolean\">false</e><e type=\"boolean\">true</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[true,false,[false,true]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiFloatArray_withDefaultType() {
      String xml = "<a type=\"float\"><e>1.1</e><e>2.2</e><e>3.3</e><e class=\"array\"><e>1.1</e><e>2.2</e><e>3.3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1.1,2.2,3.3,[1.1,2.2,3.3]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiFloatArray_withoutDefaultType() {
      String xml = "<a><e type=\"float\">1.1</e><e type=\"float\">2.2</e><e type=\"float\">3.3</e><e class=\"array\"><e type=\"float\">1.1</e><e type=\"float\">2.2</e><e type=\"float\">3.3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1.1,2.2,3.3,[1.1,2.2,3.3]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiIntegerArray_withDefaultType() {
      String xml = "<a type=\"integer\"><e>1</e><e>2</e><e>3</e><e class=\"array\"><e>1</e><e>2</e><e>3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1,2,3,[1,2,3]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiIntegerArray_withoutDefaultType() {
      String xml = "<a><e type=\"integer\">1</e><e type=\"integer\">2</e><e type=\"integer\">3</e><e class=\"array\"><e type=\"integer\">1</e><e type=\"integer\">2</e><e type=\"integer\">3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1,2,3,[1,2,3]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiMixedArray_withDefaultType() {
      String xml = "<a type=\"string\"><e type=\"boolean\">true</e><e type=\"number\">2.2</e><e>3</e><e class=\"array\"><e type=\"boolean\">true</e><e type=\"number\">2.2</e><e>3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[true,2.2,\"3\",[true,2.2,\"3\"]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiMixedArray_withoutDefaultType() {
      String xml = "<a><e type=\"boolean\">true</e><e type=\"number\">2.2</e><e type=\"string\">3</e><e class=\"array\"><e type=\"boolean\">true</e><e type=\"number\">2.2</e><e type=\"string\">3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[true,2.2,\"3\",[true,2.2,\"3\"]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiNumberArray_withDefaultType() {
      String xml = "<a type=\"number\"><e>1.1</e><e>2.2</e><e>3</e><e class=\"array\"><e>1.1</e><e>2.2</e><e>3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1.1,2.2,3,[1.1,2.2,3]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiNumberArray_withoutDefaultType() {
      String xml = "<a><e type=\"number\">1.1</e><e type=\"number\">2.2</e><e type=\"number\">3</e><e class=\"array\"><e type=\"number\">1.1</e><e type=\"number\">2.2</e><e type=\"number\">3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1.1,2.2,3,[1.1,2.2,3]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiStringArray_withDefaultType() {
      String xml = "<a type=\"string\"><e>1.1</e><e>2.2</e><e>3</e><e class=\"array\"><e>1.1</e><e>2.2</e><e>3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[\"1.1\",\"2.2\",\"3\",[\"1.1\",\"2.2\",\"3\"]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadMultiStringArray_withoutDefaultType() {
      String xml = "<a><e type=\"string\">1.1</e><e type=\"string\">2.2</e><e type=\"string\">3</e><e class=\"array\"><e type=\"string\">1.1</e><e type=\"string\">2.2</e><e type=\"string\">3</e></e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[\"1.1\",\"2.2\",\"3\",[\"1.1\",\"2.2\",\"3\"]]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadNestedObject() {
      String xml = "<o><name>json</name><nested class=\"object\"><id type=\"number\">1</id></nested></o>";
      JSONObject actual = (JSONObject) xmlSerializer.read( xml );
      JSONObject expected = JSONObject.fromObject( "{name:\"json\",nested:{id:1}}" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadNumberArray_withDefaultType() {
      String xml = "<a type=\"number\"><e>1.1</e><e>2.2</e><e>3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1.1,2.2,3]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadNumberArray_withoutDefaultType() {
      String xml = "<a><e type=\"number\">1.1</e><e type=\"number\">2.2</e><e type=\"number\">3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[1.1,2.2,3]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadObject_withAttributes() {
      String xml = "<o string=\"json\" number=\"1\"/>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = new JSONObject().element( "@string", "json" ).element( "@number", "1" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadObject_withAttributes_noTypeHintsEnabled() {
      String xml = "<o class=\"Java.class\" type=\"object\" string=\"json\" number=\"1\"/>";
      xmlSerializer.setTypeHintsEnabled( false );
      JSON actual = xmlSerializer.read( xml );
      JSON expected = new JSONObject().element( "@class", "Java.class" ).element( "@type", "object" ).element(
            "@string", "json" ).element( "@number", "1" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadObject_withAttributes_noTypeHintsEnabled_noTypeHintsCompatibility() {
      String xml = "<o json_class=\"Java.class\" json_type=\"object\" string=\"json\" number=\"1\"/>";
      xmlSerializer.setTypeHintsEnabled( false );
      xmlSerializer.setTypeHintsCompatibility( false );
      JSON actual = xmlSerializer.read( xml );
      JSON expected = new JSONObject().element( "@json_class", "Java.class" ).element( "@json_type", "object" )
            .element( "@string", "json" ).element( "@number", "1" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadObject_withText() {
      String xml = "<o>first<string>json</string>\n</o>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = new JSONObject().element( "string", "json" ).element( "#text", "first" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadObject_withText_2() {
      String xml = "<o>first<string>json</string>second</o>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = new JSONObject().element( "string", "json" ).element( "#text",
            new JSONArray().element( "first" ).element( "second" ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadObjectFullTypes() {
      String xml = "<o><int type=\"integer\">1</int>" + "<decimal type=\"float\">2.0</decimal>"
            + "<number type=\"number\">3.1416</number>" + "<bool type=\"boolean\">true</bool>"
            + "<string>json</string>" + "<func type=\"function\" params=\"a\" ><![CDATA[return a;]]></func></o>";
      JSONObject actual = (JSONObject) xmlSerializer.read( xml );
      JSONObject expected = JSONObject.fromObject( "{func:function(a){ return a; }}" );
      expected.element( "int", new Integer( 1 ) );
      expected.element( "decimal", new Double( 2.0 ) );
      expected.element( "number", new Double( 3.1416 ) );
      expected.element( "bool", Boolean.TRUE );
      expected.element( "string", "json" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadSimpleObject_withDefaultType() {
      String xml = "<o type=\"number\"><bool type=\"boolean\">true</bool><int>1</int><double>2.2</double><string type=\"string\">json</string><numbers class=\"array\"><e>4.44</e><e>5</e></numbers></o>";
      JSONObject actual = (JSONObject) xmlSerializer.read( xml );
      JSONObject expected = JSONObject.fromObject( "{bool:true,int:1,double:2.2,string:'json',numbers:[4.44,5]}" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadStringArray_withDefaultType() {
      String xml = "<a type=\"string\"><e>1.1</e><e>2.2</e><e>3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[\"1.1\",\"2.2\",\"3\"]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadStringArray_withoutDefaultType() {
      String xml = "<a><e type=\"string\">1.1</e><e type=\"string\">2.2</e><e type=\"string\">3</e></a>";
      JSON actual = xmlSerializer.read( xml );
      JSON expected = JSONArray.fromObject( "[\"1.1\",\"2.2\",\"3\"]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadWithNamespace_array() {
      String xml = "<a xmlns=\"http://json.org/json/1.0\" xmlns:ns=\"http://www.w3.org/2001/XMLSchema-instance\"><ns:string>json</ns:string><ns:string>1</ns:string></a>";
      JSON actual = xmlSerializer.read( xml );
      JSONObject expected = new JSONObject().element( "@xmlns", "http://json.org/json/1.0" ).element( "@xmlns:ns",
            "http://www.w3.org/2001/XMLSchema-instance" ).element( "ns:string", "json" ).accumulate( "ns:string", "1" );
      Assertions.assertEquals( expected, actual );
   }

   public void testReadWithNamespace_object() {
      String xml = "<o xmlns=\"http://json.org/json/1.0\" xmlns:ns=\"http://www.w3.org/2001/XMLSchema-instance\"><ns:string>json</ns:string><ns:number>1</ns:number></o>";
      JSON actual = xmlSerializer.read( xml );
      JSONObject expected = new JSONObject().element( "@xmlns", "http://json.org/json/1.0" ).element( "@xmlns:ns",
            "http://www.w3.org/2001/XMLSchema-instance" ).element( "ns:string", "json" ).element( "ns:number", "1" );
      Assertions.assertEquals( expected, actual );
   }

   public void testRemoveNameSpacePrefixFromElements() throws Exception {
      xmlSerializer.setRemoveNamespacePrefixFromElements( true );

      JSONObject json = (JSONObject) xmlSerializer.readFromFile( "net/sf/json/xml/delicious.xml" );
      assertFalse( json.getJSONObject( "item" ).has( "@rdf:about" ) );
      assertTrue( json.getJSONObject( "item" ).has( "@about" ) );
   }

   public void testSkipNamespaces() throws Exception {
      xmlSerializer.setSkipNamespaces( true );

      JSONObject json = (JSONObject) xmlSerializer.readFromFile( "net/sf/json/xml/delicious.xml" );
      assertFalse( json.getJSONObject( "item" ).has( "@xmlns" ) );
   }

   public void testTrimSpaces() throws Exception {
      JSONObject json = (JSONObject) xmlSerializer.readFromFile( "net/sf/json/xml/delicious.xml" );
      String link = json.getJSONObject( "item" ).getString( "link" );
      assertTrue( link.startsWith( " " ) );
      assertTrue( link.endsWith( " " ) );

      xmlSerializer.setTrimSpaces( true );
      json = (JSONObject) xmlSerializer.readFromFile( "net/sf/json/xml/delicious.xml" );
      link = json.getJSONObject( "item" ).getString( "link" );
      assertFalse( link.startsWith( " " ) );
      assertFalse( link.endsWith( " " ) );
   }

   public void testXmlConversionRules() {
      String xml = "<span class=\"vevent\">" + "<a class=\"url\" href=\"http://www.web2con.com/\">"
            + "<span class=\"summary\">Web 2.0 Conference</span>"
            + "<abbr class=\"dtstart\" title=\"2005-10-05\">October 5</abbr>"
            + "<abbr class=\"dtend\" title=\"2005-10-08\">7</abbr>"
            + "<span class=\"location\">Argent Hotel, San Francisco, CA</span>" + "</a>" + "</span>";

      xmlSerializer.setTypeHintsCompatibility( false );
      xmlSerializer.setForceTopLevelObject( true );
      JSONObject actual = (JSONObject) xmlSerializer.read( xml );
      assertNotNull( actual );
      JSONObject expected = new JSONObject()
            .element(
                  "span",
                  new JSONObject()
                        .element( "@class", "vevent" )
                        .element(
                              "a",
                              new JSONObject()
                                    .element( "@class", "url" )
                                    .element( "@href", "http://www.web2con.com/" )
                                    .element( "span",
                                          JSONObject.fromObject( "{'@class':'summary','#text':'Web 2.0 Conference'}" ) )
                                    .element(
                                          "abbr",
                                          JSONObject
                                                .fromObject( "{'@class':'dtstart','@title': '2005-10-05','#text':'October 5'}" ) )
                                    .accumulate(
                                          "abbr",
                                          JSONObject
                                                .fromObject( "{'@class':'dtend','@title': '2005-10-08','#text':'7'}" ) )
                                    .accumulate(
                                          "span",
                                          JSONObject
                                                .fromObject( "{'@class':'location','#text':'Argent Hotel, San Francisco, CA'}" ) ) ) );
      JSONAssert.assertEquals( expected, actual );
   }

   protected void setUp() throws Exception {
      super.setUp();
      xmlSerializer = new XMLSerializer();
   }
}