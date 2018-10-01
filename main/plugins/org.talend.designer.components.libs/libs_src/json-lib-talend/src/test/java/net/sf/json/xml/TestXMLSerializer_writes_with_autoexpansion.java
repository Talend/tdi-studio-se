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

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.custommonkey.xmlunit.XMLTestCase;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestXMLSerializer_writes_with_autoexpansion extends XMLTestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestXMLSerializer_writes_with_autoexpansion.class );
   }

   private XMLSerializer xmlSerializer;

   public TestXMLSerializer_writes_with_autoexpansion( String testName ) {
      super( testName );
   }

   public void testWriteBooleanArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[true,false]" );
      String expected = "<a><e type=\"boolean\">true</e><e type=\"boolean\">false</e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteFunctionArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[function(a){ return a; }]" );
      String expected = "<a><e type=\"function\" params=\"a\"><![CDATA[return a;]]></e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteFunctionArray_noTypeHintsCompatibility() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[function(a){ return a; }]" );
      xmlSerializer.setTypeHintsCompatibility( false );
      String expected = "<a><e json_type=\"function\" json_params=\"a\"><![CDATA[return a;]]></e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteJSONArray_collapseProperties() throws Exception {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "duplicated", "json1" );
      jsonObject.accumulate( "duplicated", "json2" );
      jsonObject.getJSONArray( "duplicated" )
            .setExpandElements( true );
      JSONArray jsonArray = new JSONArray().element( jsonObject );
      String expected = "<a><e class=\"object\"><duplicated type=\"string\">json1</duplicated><duplicated type=\"string\">json2</duplicated></e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteMultiBooleanArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[true,false,[true,false]]" );
      String expected = "<a><e type=\"boolean\">true</e><e type=\"boolean\">false</e><e class=\"array\"><e type=\"boolean\">true</e><e type=\"boolean\">false</e></e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteMultiNumberArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[1.1,2,[3,4.4]]" );
      String expected = "<a><e type=\"number\">1.1</e><e type=\"number\">2</e><e class=\"array\"><e type=\"number\">3</e><e type=\"number\">4.4</e></e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteMultiStringArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "['1.1','2',['3','4.4']]" );
      String expected = "<a><e type=\"string\">1.1</e><e type=\"string\">2</e><e class=\"array\"><e type=\"string\">3</e><e type=\"string\">4.4</e></e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteNullObjectArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[null,null]" );
      String expected = "<a><e class=\"object\" null=\"true\"/><e class=\"object\" null=\"true\"/></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteNullObjectArray_noTypeHintsCompatibility() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[null,null]" );
      String expected = "<a><e json_class=\"object\" json_null=\"true\"/><e json_class=\"object\" json_null=\"true\"/></a>";
      xmlSerializer.setTypeHintsCompatibility( false );
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteNumberArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[1.1,2]" );
      String expected = "<a><e type=\"number\">1.1</e><e type=\"number\">2</e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteObject_full_types() throws Exception {
      JSONObject jsonObject = JSONObject.fromObject( "{\"string\":\"json\",\"int\":1,\"bool\":true,\"array\":[1.1,2],\"nested_null\":null,\"nested\":{\"name\":\"json\"},\"func\":function(a){ return a; }}" );
      String expected = "<o><string type=\"string\">json</string>" + "<int type=\"number\">1</int>"
            + "<bool type=\"boolean\">true</bool>"
            + "<array class=\"array\"><e type=\"number\">1.1</e><e type=\"number\">2</e></array>"
            + "<nested_null class=\"object\" null=\"true\"/>"
            + "<nested class=\"object\"><name type=\"string\">json</name></nested>"
            + "<func type=\"function\" params=\"a\"><![CDATA[return a;]]></func>" + "</o>";
      String xml = xmlSerializer.write( jsonObject );
      assertXMLEqual( expected, xml );
   }

   public void testWriteObjectArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "[{\"name\":\"json\"}]" );
      String expected = "<a><e class=\"object\"><name type=\"string\">json</name></e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   public void testWriteStringArray() throws Exception {
      JSONArray jsonArray = JSONArray.fromObject( "['1','2']" );
      String expected = "<a><e type=\"string\">1</e><e type=\"string\">2</e></a>";
      String xml = xmlSerializer.write( jsonArray );
      assertXMLEqual( expected, xml );
   }

   protected void setUp() throws Exception {
      super.setUp();
      xmlSerializer = new XMLSerializer();
      xmlSerializer.setPerformAutoExpansion(true);
   }
}