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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.json.sample.ArrayJSONStringBean;
import net.sf.json.sample.BeanA;
import net.sf.json.sample.MappingBean;
import net.sf.json.sample.ObjectJSONStringBean;
import net.sf.json.sample.ValueBean;
import net.sf.json.util.JSONTokener;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONSerializer extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONSerializer.class );
   }

   private JsonConfig jsonConfig;

   public TestJSONSerializer( String name ) {
      super( name );
   }

   public void testToJava_JSONArray_1() {
      setName( "JSONArray('[]') -&gt; ToJava[default]" );
      JSONArray jsonArray = JSONArray.fromObject( "[]" );
      Object java = JSONSerializer.toJava( jsonArray );
      assertNotNull( java );
      assertTrue( java instanceof List );
      List list = (List) java;
      assertEquals( 0, list.size() );
   }

   public void testToJava_JSONArray_2() {
      setName( "JSONArray('[]') -&gt; ToJava[arrayMode:OBJECT_ARRAY]" );
      JSONArray jsonArray = JSONArray.fromObject( "[]" );
      jsonConfig.setArrayMode( JsonConfig.MODE_OBJECT_ARRAY );
      Object java = JSONSerializer.toJava( jsonArray, jsonConfig );
      assertNotNull( java );
      assertTrue( Object[].class.isAssignableFrom( java.getClass() ) );
      Object[] array = (Object[]) java;
      assertEquals( 0, array.length );
   }

   public void testToJava_JSONNull_1() {
      setName( "JSONNull -&gt; ToJava[default]" );
      Object java = JSONSerializer.toJava( JSONNull.getInstance() );
      assertNull( java );
   }

   public void testToJava_JSONObject_1() {
      setName( "JSONObject(null:true) -&gt; ToJava[default]" );
      Object java = JSONSerializer.toJava( new JSONObject( true ) );
      assertNull( java );
   }

   public void testToJava_JSONObject_2() throws Exception {
      setName( "JSONObject -&gt; ToJava[default]" );
      String json = "{name=\"json\",bool:true,int:1,double:2.2,func:function(a){ return a; },array:[1,2]}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      Object bean = JSONSerializer.toJava( jsonObject );
      assertNotNull( bean );
      assertTrue( bean instanceof DynaBean );
      assertEquals( jsonObject.get( "name" ), PropertyUtils.getProperty( bean, "name" ) );
      assertEquals( jsonObject.get( "bool" ), PropertyUtils.getProperty( bean, "bool" ) );
      assertEquals( jsonObject.get( "int" ), PropertyUtils.getProperty( bean, "int" ) );
      assertEquals( jsonObject.get( "double" ), PropertyUtils.getProperty( bean, "double" ) );
      assertEquals( jsonObject.get( "func" ), PropertyUtils.getProperty( bean, "func" ) );
      List expected = (List) JSONArray.toCollection( jsonObject.getJSONArray( "array" ) );
      Assertions.assertEquals( expected, (List) PropertyUtils.getProperty( bean, "array" ) );
   }

   public void testToJava_JSONObject_3() throws Exception {
      setName( "JSONObject -&gt; ToJava[rootClass:BeanA]" );
      String json = "{bool:true,integer:1,string:\"json\"}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      jsonConfig.setRootClass( BeanA.class );
      Object java = JSONSerializer.toJava( jsonObject, jsonConfig );
      assertNotNull( java );
      assertTrue( java instanceof BeanA );
      BeanA bean = (BeanA) java;
      assertEquals( jsonObject.get( "bool" ), Boolean.valueOf( bean.isBool() ) );
      assertEquals( jsonObject.get( "integer" ), new Integer( bean.getInteger() ) );
      assertEquals( jsonObject.get( "string" ), bean.getString() );
   }

   public void testToJava_JSONObject_4() {
      setName( "JSONObject -&gt; ToJava[rootClass:BeanA,classMap]" );

      MappingBean mappingBean = new MappingBean();
      ValueBean beanA = new ValueBean();
      beanA.setValue( 90000 );
      ValueBean beanB = new ValueBean();
      beanB.setValue( 91000 );
      mappingBean.addAttribute( "beanA", beanA );
      mappingBean.addAttribute( "beanB", beanB );
      Map classMap = new HashMap();
      classMap.put( "bean.*", ValueBean.class );

      JSONObject jsonObject = JSONObject.fromObject( mappingBean );
      jsonConfig.setRootClass( MappingBean.class );
      jsonConfig.setClassMap( classMap );
      Object java = JSONSerializer.toJava( jsonObject, jsonConfig );
      assertNotNull( java );
      assertTrue( java instanceof MappingBean );
      MappingBean mappingBean2 = (MappingBean) java;

      Object ba = mappingBean2.getAttributes()
            .get( "beanA" );
      Object bb = mappingBean2.getAttributes()
            .get( "beanB" );
      assertTrue( ba instanceof ValueBean );
      assertTrue( bb instanceof ValueBean );
      assertEquals( beanA.getValue(), ((ValueBean) ba).getValue() );
      assertEquals( beanB.getValue(), ((ValueBean) bb).getValue() );
   }

   public void testToJava_JSONObject_and_reset() throws Exception {
      String json = "{bool:true,integer:1,string:\"json\"}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      jsonConfig.setRootClass( BeanA.class );
      Object java = JSONSerializer.toJava( jsonObject, jsonConfig );
      assertNotNull( java );
      assertTrue( java instanceof BeanA );
      BeanA bean = (BeanA) java;
      assertEquals( jsonObject.get( "bool" ), Boolean.valueOf( bean.isBool() ) );
      assertEquals( jsonObject.get( "integer" ), new Integer( bean.getInteger() ) );
      assertEquals( jsonObject.get( "string" ), bean.getString() );
      jsonConfig.reset();
      java = JSONSerializer.toJava( jsonObject, jsonConfig );
      assertTrue( java instanceof DynaBean );
      assertEquals( jsonObject.get( "bool" ), PropertyUtils.getProperty( java, "bool" ) );
      assertEquals( jsonObject.get( "integer" ), PropertyUtils.getProperty( java, "integer" ) );
      assertEquals( jsonObject.get( "string" ), PropertyUtils.getProperty( java, "string" ) );
   }

   public void testToJSON_JSONString_array() {
      ArrayJSONStringBean bean = new ArrayJSONStringBean();
      bean.setValue( "'json','json'" );
      JSON json = JSONSerializer.toJSON( bean );
      assertNotNull( json );
      assertTrue( json instanceof JSONArray );
      Assertions.assertEquals( JSONArray.fromObject( "['json','json']" ), (JSONArray) json );
   }

   public void testToJSON_JSONString_null() {
      JSON json = JSONSerializer.toJSON( (JSONString) null );
      assertNotNull( json );
      assertTrue( JSONNull.getInstance()
            .equals( json ) );
   }

   public void testToJSON_JSONString_object() {
      ObjectJSONStringBean bean = new ObjectJSONStringBean();
      bean.setName( "json" );
      JSON json = JSONSerializer.toJSON( bean );
      assertNotNull( json );
      assertTrue( json instanceof JSONObject );
      Assertions.assertEquals( JSONObject.fromObject( "{\"name\":\"json\"}" ), (JSONObject) json );
   }

   public void testToJSON_Object_array() {
      JSON json = JSONSerializer.toJSON( new int[] { 1, 2 } );
      assertNotNull( json );
      assertTrue( json instanceof JSONArray );
      Assertions.assertEquals( JSONArray.fromObject( "[1,2]" ), (JSONArray) json );
   }

   public void testToJSON_Object_JSONTokener_array() {
      JSON json = JSONSerializer.toJSON( new JSONTokener( "[1,2]" ) );
      assertNotNull( json );
      assertTrue( json instanceof JSONArray );
      Assertions.assertEquals( JSONArray.fromObject( "[1,2]" ), (JSONArray) json );
   }

   public void testToJSON_Object_null() {
      JSON json = JSONSerializer.toJSON( (Object) null );
      assertNotNull( json );
      assertTrue( JSONNull.getInstance()
            .equals( json ) );
   }

   public void testToJSON_Object_object() {
      JSON json = JSONSerializer.toJSON( new BeanA() );
      assertNotNull( json );
      assertTrue( json instanceof JSONObject );
      Assertions.assertEquals( JSONObject.fromObject( new BeanA() ), (JSONObject) json );
   }

   public void testToJSON_String_array() {
      JSON json = JSONSerializer.toJSON( "['json','json']" );
      assertNotNull( json );
      assertTrue( json instanceof JSONArray );
      Assertions.assertEquals( JSONArray.fromObject( "['json','json']" ), (JSONArray) json );
   }

   public void testToJSON_String_invalid() {
      try{
         JSONSerializer.toJSON( "garbage" );
         fail( "Should have thrown a JSONException" );
      }catch( JSONException expected ){
         // ok
      }
   }

   public void testToJSON_String_null() {
      JSON json = JSONSerializer.toJSON( (String) null );
      assertNotNull( json );
      assertTrue( JSONNull.getInstance()
            .equals( json ) );
   }

   public void testToJSON_String_null_literal() {
      JSON json = JSONSerializer.toJSON( "null" );
      assertNotNull( json );
      assertTrue( JSONNull.getInstance()
            .equals( json ) );
   }

   public void testToJSON_String_object() {
      JSON json = JSONSerializer.toJSON( "{'name':'json'}" );
      assertNotNull( json );
      assertTrue( json instanceof JSONObject );
      Assertions.assertEquals( JSONObject.fromObject( "{\"name\":\"json\"}" ), (JSONObject) json );
   }

   protected void setUp() throws Exception {
      jsonConfig = new JsonConfig();
   }
}