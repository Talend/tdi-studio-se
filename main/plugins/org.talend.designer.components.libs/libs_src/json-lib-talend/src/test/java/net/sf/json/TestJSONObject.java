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

import junit.framework.TestCase;
import net.sf.ezmorph.MorphUtils;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.ezmorph.bean.MorphDynaClass;
import net.sf.ezmorph.test.ArrayAssertions;
import net.sf.json.processors.DefaultValueProcessor;
import net.sf.json.processors.DefaultValueProcessorMatcher;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.sample.BeanA;
import net.sf.json.sample.BeanB;
import net.sf.json.sample.BeanC;
import net.sf.json.sample.BeanD;
import net.sf.json.sample.BeanFoo;
import net.sf.json.sample.BeanWithFunc;
import net.sf.json.sample.ChildBean;
import net.sf.json.sample.ClassBean;
import net.sf.json.sample.EmptyBean;
import net.sf.json.sample.JavaIdentifierBean;
import net.sf.json.sample.ListingBean;
import net.sf.json.sample.MappingBean;
import net.sf.json.sample.NumberBean;
import net.sf.json.sample.ObjectBean;
import net.sf.json.sample.ObjectJSONStringBean;
import net.sf.json.sample.ParentBean;
import net.sf.json.sample.PrimitiveBean;
import net.sf.json.sample.PropertyBean;
import net.sf.json.sample.SetBean;
import net.sf.json.sample.TransientBean;
import net.sf.json.sample.ValueBean;
import net.sf.json.util.CycleDetectionStrategy;
import net.sf.json.util.JSONTokener;
import net.sf.json.util.JSONUtils;
import net.sf.json.util.JavaIdentifierTransformer;
import net.sf.json.util.PropertyExclusionClassMatcher;
import net.sf.json.util.PropertyFilter;
import net.sf.json.util.PropertySetStrategy;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONObject extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONObject.class );
   }

   private JsonConfig jsonConfig;

   public TestJSONObject( String testName ) {
      super( testName );
   }

   public void testAccumulate() {
      JSONObject json = new JSONObject();
      json.accumulate( "key", "1" );
      Assertions.assertEquals( 1, json.getInt( "key" ) );
      json.accumulate( "key", "2" );
      Assertions.assertEquals( JSONArray.fromObject( "['1','2']" ), json.getJSONArray( "key" ) );
      json.accumulate( "key", "3" );
      Assertions.assertEquals( JSONArray.fromObject( "['1','2','3']" ), json.getJSONArray( "key" ) );
   }

   public void testAccumulate__nullObject() {
      try{
         new JSONObject( true ).accumulate( "key", "value" );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // ok
      }
   }

   public void testConstructor_Object__nullJSONObject() {
      JSONObject jsonObject = JSONObject.fromObject( (JSONObject) null );
      assertTrue( jsonObject.isNullObject() );
   }

   public void testConstructor_Object_String_Array__nullObject() {
      jsonConfig.setExcludes( new String[] { "bool", "integer" } );
      JSONObject jsonObject = JSONObject.fromObject( (Object) null, jsonConfig );
      assertTrue( jsonObject.isNullObject() );
   }

   public void testCycleDetection_beans_noprop() {
      jsonConfig.setCycleDetectionStrategy( CycleDetectionStrategy.NOPROP );
      ParentBean parent = new ParentBean();
      parent.setChild( new ChildBean() );

      JSONObject actual = JSONObject.fromObject( parent, jsonConfig );
      JSONObject expected = new JSONObject().element( "value", 0 )
            .element( "child", new JSONObject().element( "value", 0 ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testCycleDetection_beans_null() {
      jsonConfig.setCycleDetectionStrategy( CycleDetectionStrategy.LENIENT );
      ParentBean parent = new ParentBean();
      parent.setChild( new ChildBean() );

      JSONObject actual = JSONObject.fromObject( parent, jsonConfig );
      JSONObject expected = new JSONObject().element( "value", 0 )
            .element( "child", new JSONObject().element( "value", 0 )
                  .element( "parent", new JSONObject( true ) ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testCycleDetection_beans_strict() {
      ParentBean parent = new ParentBean();
      parent.setChild( new ChildBean() );
      try{
         JSONObject.fromObject( parent );
         fail( "A JSONException was expected" );
      }catch( JSONException expected ){
         assertTrue( expected.getMessage()
               .endsWith( "There is a cycle in the hierarchy!" ) );
      }
   }

   public void testDiscard() {
      JSONObject jsonObject = new JSONObject().element( "int", "1" )
            .element( "long", "1" )
            .element( "boolean", "true" )
            .element( "string", "string" )
            .element( "func", "function(){ return this; }" )
            .element( "array", "[1,2,3]" );
      assertEquals( 6, jsonObject.size() );
      jsonObject.discard( "int" )
            .discard( "func" );
      assertEquals( 4, jsonObject.size() );
      assertFalse( jsonObject.has( "int" ) );
      assertFalse( jsonObject.has( "func" ) );
   }

   public void testElement__duplicateProperty() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "duplicated", "json1" );
      jsonObject.element( "duplicated", "json2" );
      Object o = jsonObject.get( "duplicated" );
      assertFalse( o instanceof JSONArray );
      assertEquals( "json2", o );
   }

   public void testElement__duplicateProperty_2() {
      JSONObject jsonObject = JSONObject.fromObject( "{'duplicated':'json1','duplicated':'json2'}" );
      Object o = jsonObject.get( "duplicated" );
      assertTrue( o instanceof JSONArray );
      assertEquals( new JSONArray().element( "json1" )
            .element( "json2" ), o );
   }

   public void testElement_Bean() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "bean", new ObjectBean() );
      JSONObject actual = jsonObject.getJSONObject( "bean" );
      Assertions.assertTrue( !actual.has( "class" ) );
   }

   public void testElement_Bean_exclusions() {
      JSONObject jsonObject = new JSONObject();
      jsonConfig.setExcludes( new String[] { "pexcluded" } );
      jsonObject.element( "bean", new ObjectBean(), jsonConfig );
      JSONObject actual = jsonObject.getJSONObject( "bean" );
      Assertions.assertTrue( !actual.has( "class" ) );
      Assertions.assertTrue( !actual.has( "pexcluded" ) );
   }

   public void testElement_Bean_exclusions_ignoreDefault() {
      JSONObject jsonObject = new JSONObject();
      jsonConfig.setExcludes( new String[] { "pexcluded" } );
      jsonConfig.setIgnoreDefaultExcludes( true );
      jsonObject.element( "bean", new ObjectBean(), jsonConfig );
      JSONObject actual = jsonObject.getJSONObject( "bean" );
      //see : https://github.com/apache/commons-beanutils/compare/BEANUTILS_1_9_3...commons-beanutils-1.9.4#diff-dbeaa70d3a09169fb5a1e42e5b319037R34-R43
      //Assertions.assertTrue( actual.has( "class" ) );
      Assertions.assertTrue( !actual.has( "pexcluded" ) );
   }

   public void testElement_boolean() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "bool", true );
      assertTrue( jsonObject.getBoolean( "bool" ) );
   }

   public void testElement_Boolean() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "bool", Boolean.TRUE );
      Assertions.assertTrue( jsonObject.getBoolean( "bool" ) );
   }

   public void testElement_Class() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "class", Object.class );
      assertEquals( "java.lang.Object", jsonObject.get( "class" ) );
   }

   public void testElement_Collection() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "list", Collections.EMPTY_LIST );
      Assertions.assertEquals( new JSONArray(), jsonObject.getJSONArray( "list" ) );
   }

   public void testElement_Collection2() {
      List list = new ArrayList();
      list.add( new ObjectBean() );
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "list", list );
      JSONObject actual = jsonObject.getJSONArray( "list" )
            .getJSONObject( 0 );
      Assertions.assertTrue( !actual.has( "class" ) );
   }

   public void testElement_Collection2_exclusions() {
      List list = new ArrayList();
      list.add( new ObjectBean() );
      JSONObject jsonObject = new JSONObject();
      jsonConfig.setExcludes( new String[] { "pexcluded" } );
      jsonObject.element( "list", list, jsonConfig );
      JSONObject actual = jsonObject.getJSONArray( "list" )
            .getJSONObject( 0 );
      Assertions.assertTrue( !actual.has( "class" ) );
      Assertions.assertTrue( !actual.has( "pexcluded" ) );
   }

   public void testElement_Collection2_exclusions_ignoreDefault() {
      List list = new ArrayList();
      list.add( new ObjectBean() );
      jsonConfig.setExcludes( new String[] { "pexcluded" } );
      jsonConfig.setIgnoreDefaultExcludes( true );
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "list", list, jsonConfig );
      JSONObject actual = jsonObject.getJSONArray( "list" )
            .getJSONObject( 0 );
      //see : https://github.com/apache/commons-beanutils/compare/BEANUTILS_1_9_3...commons-beanutils-1.9.4#diff-dbeaa70d3a09169fb5a1e42e5b319037R34-R43
      //Assertions.assertTrue( actual.has( "class" ) );
      Assertions.assertTrue( !actual.has( "pexcluded" ) );
   }

   public void testElement_double() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "double", 1d );
      assertEquals( 1d, jsonObject.getDouble( "double" ), 0d );
   }

   public void testElement_int() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "int", 1 );
      assertEquals( 1, jsonObject.getInt( "int" ) );
   }

   public void testElement_JSON() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "null", JSONNull.getInstance() );
      Assertions.assertEquals( JSONNull.getInstance(), jsonObject.get( "null" ) );
   }

   public void testElement_JSONFunction() {
      JSONObject jsonObject = new JSONObject();
      JSONFunction f = new JSONFunction( "return this;" );
      jsonObject.element( "func", f );
      Assertions.assertEquals( f, (JSONFunction) jsonObject.get( "func" ) );
   }

   public void testElement_JSONString() {
      JSONObject jsonObject = new JSONObject();
      ObjectJSONStringBean bean = new ObjectJSONStringBean();
      bean.setName( "json" );
      jsonObject.element( "bean", bean );
      Assertions.assertEquals( JSONObject.fromObject( bean ), jsonObject.getJSONObject( "bean" ) );
   }

   public void testElement_JSONTokener() {
      JSONObject jsonObject = new JSONObject();
      JSONTokener tok = new JSONTokener( "{'name':'json'}" );
      jsonObject.element( "obj", tok );
      tok.reset();
      Assertions.assertEquals( JSONObject.fromObject( tok ), jsonObject.getJSONObject( "obj" ) );
   }

   public void testElement_long() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "long", 1L );
      assertEquals( 1L, jsonObject.getLong( "long" ) );
   }

   public void testElement_Map() {
      Map map = new HashMap();
      map.put( "name", "json" );
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "map", map );
      Assertions.assertEquals( JSONObject.fromObject( map ), jsonObject.getJSONObject( "map" ) );
   }

   public void testElement_Map2() {
      Map map = new HashMap();
      map.put( "name", "json" );
      map.put( "class", "java.lang.Object" );
      map.put( "excluded", "excluded" );
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "map", map );
      JSONObject actual = jsonObject.getJSONObject( "map" );
      Assertions.assertTrue( !actual.has( "class" ) );
   }

   public void testElement_Map2_exclusions() {
      Map map = new HashMap();
      map.put( "name", "json" );
      map.put( "class", "java.lang.Object" );
      map.put( "pexcluded", "excluded" );
      jsonConfig.setExcludes( new String[] { "pexcluded" } );
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "map", map, jsonConfig );
      JSONObject actual = jsonObject.getJSONObject( "map" );
      Assertions.assertTrue( !actual.has( "class" ) );
      Assertions.assertTrue( !actual.has( "pexcluded" ) );
   }

   public void testElement_Map2_exclusions_ignoreDefault() {
      Map map = new HashMap();
      map.put( "name", "json" );
      map.put( "class", "java.lang.Object" );
      map.put( "pexcluded", "excluded" );
      jsonConfig.setExcludes( new String[] { "pexcluded" } );
      jsonConfig.setIgnoreDefaultExcludes( true );
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "map", map, jsonConfig );
      JSONObject actual = jsonObject.getJSONObject( "map" );
      Assertions.assertTrue( actual.has( "class" ) );
      Assertions.assertTrue( !actual.has( "pexcluded" ) );
   }

   public void testElement_null_key() {
      try{
         new JSONObject().element( null, "value" );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // ok
      }
   }

   public void testElement_Number() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "num", new Double( 2 ) );
      Assertions.assertEquals( new Double( 2 ).doubleValue(), jsonObject.getDouble( "num" ), 0d );
   }

   public void testElement_Object() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "bean", new BeanA() );
      Assertions.assertEquals( JSONObject.fromObject( new BeanA() ),
            jsonObject.getJSONObject( "bean" ) );
   }

   public void testElement_String() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "str", "json" );
      Assertions.assertEquals( "json", jsonObject.getString( "str" ) );
   }

   public void testElement_String_JSON() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "str", "[]" );
      Assertions.assertEquals( new JSONArray().toString(), jsonObject.getString( "str" ) );
   }

   public void testElement_String_null() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "str", (String) null );
      // special case, if value null, there is no value associated to key
      try{
         jsonObject.getString( "str" );
         fail( "Should have thrown a JSONException" );
      }catch( JSONException expected ){
         // ok
      }
   }

   public void testFromBean_array() {
      try{
         JSONObject.fromObject( new ArrayList() );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }

      try{
         JSONObject.fromObject( new String[] { "json" } );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testFromBean_ClassBean() {
      ClassBean classBean = new ClassBean();
      classBean.setKlass( Object.class );
      JSONObject json = JSONObject.fromObject( classBean );
      assertEquals( "java.lang.Object", json.get( "klass" ) );
   }

   public void testFromBean_DynaBean() throws Exception {
      JSONObject json = JSONObject.fromObject( createDynaBean() );
      assertEquals( "json", json.getString( "name" ) );
      Assertions.assertEquals( "[1,2]", json.getString( "str" ) );
      Assertions.assertEquals( JSONObject.fromObject( "{'id':'1'}" ), json.getJSONObject( "json" ) );
      Assertions.assertEquals( JSONObject.fromObject( "{'name':''}" ),
            json.getJSONObject( "jsonstr" ) );
      Assertions.assertEquals( "function(){ return this; }", (JSONFunction) json.get( "func" ) );
   }

   public void testFromBean_JSONObject() {
      JSONObject json = new JSONObject();
      json.element( "name", "json" );
      Assertions.assertEquals( json, JSONObject.fromObject( json ) );
   }

   public void testFromBean_JSONString() {
      ObjectJSONStringBean bean = new ObjectJSONStringBean();
      bean.setId( 1 );
      bean.setName( "json" );
      JSONObject json = JSONObject.fromObject( bean );
      assertEquals( "json", json.getString( "name" ) );
      assertTrue( !json.has( "id" ) );
   }

   public void testFromBean_JSONTokener() {
      JSONTokener jsonTokener = new JSONTokener( "{\"string\":\"json\"}" );
      JSONObject json = JSONObject.fromObject( jsonTokener );
      assertEquals( "json", json.getString( "string" ) );
   }

   public void testFromBean_Map() {
      Map map = new HashMap();
      map.put( "bool", Boolean.TRUE );
      map.put( "integer", new Integer( 42 ) );
      map.put( "string", "json" );

      JSONObject json = JSONObject.fromObject( map );
      assertEquals( true, json.getBoolean( "bool" ) );
      assertEquals( 42, json.getInt( "integer" ) );
      assertEquals( "json", json.getString( "string" ) );
   }

   public void testFromBean_noReadMethod() {
      JSONObject json = JSONObject.fromObject( new PropertyBean() );
      assertTrue( json.has( "propertyWithNoWriteMethod" ) );
      assertTrue( !json.has( "propertyWithNoReadMethod" ) );
   }

   public void testFromBean_null() {
      JSONObject json = JSONObject.fromObject( null );
      assertTrue( json.isNullObject() );
      assertEquals( JSONNull.getInstance()
            .toString(), json.toString() );
   }

   public void testFromBean_String() {
      JSONObject json = JSONObject.fromObject( "{\"string\":\"json\"}" );
      assertEquals( "json", json.getString( "string" ) );
   }

   public void testFromBean_use_wrappers() {
      JSONObject json = JSONObject.fromObject( Boolean.TRUE );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Byte( Byte.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Short( Short.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Integer( Integer.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Long( Long.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Float( Float.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Double( Double.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Character( 'A' ) );
      assertTrue( json.isEmpty() );
   }

   public void testFromBeanWithJsonPropertyNameProcessor(){
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.registerJsonPropertyNameProcessor( BeanA.class, new PrefixerPropertyNameProcessor("json") );
      JSONObject jsonObject = JSONObject.fromObject( new BeanA(), jsonConfig );
      assertNotNull( jsonObject );
      assertEquals( 3, jsonObject.names().size() );
      assertTrue( jsonObject.has( "jsonbool" ) );
      assertTrue( jsonObject.has( "jsonstring" ) );
      assertTrue( jsonObject.has( "jsoninteger" ) );
   }

   public void testFromDynaBean_full() throws Exception {
      Map properties = new HashMap();
      properties.put( "string", String.class );
      properties.put( "number", Integer.class );
      properties.put( "array", Object[].class );
      properties.put( "list", List.class );
      properties.put( "func", JSONFunction.class );
      properties.put( "boolean", Boolean.class );
      properties.put( "bean", BeanA.class );
      MorphDynaClass dynaClass = new MorphDynaClass( "JSON", MorphDynaBean.class, properties );
      MorphDynaBean dynaBean = (MorphDynaBean) dynaClass.newInstance();
      dynaBean.setDynaBeanClass( dynaClass );
      dynaBean.set( "string", "json" );
      dynaBean.set( "number", new Double( 2 ) );
      dynaBean.set( "array", new Integer[] { new Integer( 1 ), new Integer( 2 ) } );
      dynaBean.set( "list", new ArrayList() );
      dynaBean.set( "func", new JSONFunction( new String[] { "a" }, "return a;" ) );
      dynaBean.set( "boolean", Boolean.TRUE );
      dynaBean.set( "bean", new BeanA() );

      JSONObject jsonObject = JSONObject.fromObject( dynaBean );
      assertEquals( "json", jsonObject.get( "string" ) );
      assertEquals( new Double( 2 ), jsonObject.get( "number" ) );
      assertEquals( Boolean.TRUE, jsonObject.get( "boolean" ) );
      Assertions.assertEquals( "function(a){ return a; }", (JSONFunction) jsonObject.get( "func" ) );
   }

   public void testFromDynaBean_null() {
      JSONObject jsonObject = JSONObject.fromObject( null );
      assertTrue( jsonObject.isNullObject() );
   }

   public void testFromJSONTokener() {
      JSONTokener jsonTokener = new JSONTokener( "{\"string\":\"json\"}" );
      JSONObject json = JSONObject.fromObject( jsonTokener );
      assertEquals( "json", json.getString( "string" ) );
   }

   public void testFromMap_nested_null_object() {
      Map map = new HashMap();
      map.put( "nested", null );
      map.put( "string", "json" );

      JSONObject json = JSONObject.fromObject( map );
      assertEquals( "json", json.getString( "string" ) );
      Object nested = json.get( "nested" );
      assertTrue( JSONUtils.isNull( nested ) );
   }

   public void testFromMap_null_Map() {
      JSONObject json = JSONObject.fromObject( null );
      assertTrue( json.isNullObject() );
      assertEquals( JSONNull.getInstance()
            .toString(), json.toString() );
   }

   public void testFromObject_array() {
      try{
         JSONObject.fromObject( new ArrayList() );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }

      try{
         JSONObject.fromObject( new String[] { "json" } );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testFromObject_Bean() {
      JSONObject json = JSONObject.fromObject( new BeanA() );
      assertEquals( true, json.getBoolean( "bool" ) );
      assertEquals( 42, json.getInt( "integer" ) );
      assertEquals( "json", json.getString( "string" ) );
   }

   public void testFromObject_BeanWithFunc() {
      JSONObject json = JSONObject.fromObject( new BeanWithFunc( "return a;" ) );
      assertNotNull( json.get( "function" ) );
      assertTrue( JSONUtils.isFunction( json.get( "function" ) ) );
      assertEquals( "function(){ return a; }", json.get( "function" )
            .toString() );
   }

   public void testFromObject_DynaBean() throws Exception {
      JSONObject json = JSONObject.fromObject( createDynaBean() );
      assertEquals( "json", json.getString( "name" ) );
   }

   public void testFromObject_emptyBean() {
      EmptyBean bean = new EmptyBean();
      JSONObject json = JSONObject.fromObject( bean );
      JSONObject expected = new JSONObject();
      expected.element( "arrayp", new JSONArray() );
      expected.element( "listp", new JSONArray() );
      expected.element( "bytep", new Integer( 0 ) );
      expected.element( "shortp", new Integer( 0 ) );
      expected.element( "intp", new Integer( 0 ) );
      expected.element( "longp", new Integer( 0 ) );
      expected.element( "floatp", new Integer( 0 ) );
      expected.element( "doublep", new Double( 0 ) );
      expected.element( "charp", "" );
      expected.element( "stringp", "" );

      Assertions.assertEquals( expected, json );
   }

   public void testFromObject_ExtendedBean() {
      JSONObject json = JSONObject.fromObject( new BeanB() );
      assertEquals( true, json.getBoolean( "bool" ) );
      assertEquals( 42, json.getInt( "integer" ) );
      assertEquals( "json", json.getString( "string" ) );
      assertNotNull( json.get( "intarray" ) );
   }

   public void testFromObject_ignoreTransientFields() {
      jsonConfig.setIgnoreTransientFields( true );
      TransientBean bean = new TransientBean();
      bean.setValue( 42 );
      bean.setTransientValue( 84 );
      JSONObject jsonObject = JSONObject.fromObject( bean, jsonConfig );
      assertTrue( jsonObject.has( "value" ) );
      assertFalse( jsonObject.has( "transientValue" ) );
   }

   public void testFromObject_JSONObject() {
      JSONObject expected = new JSONObject().element( "id", "1" )
            .element( "name", "json" );
      JSONObject actual = JSONObject.fromObject( expected );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_JSONString() {
      ObjectJSONStringBean bean = new ObjectJSONStringBean();
      bean.setId( 1 );
      bean.setName( "json" );
      JSONObject json = JSONObject.fromObject( bean );
      assertEquals( "json", json.getString( "name" ) );
      assertTrue( !json.has( "id" ) );
   }

   public void testFromObject_JSONTokener() {
      JSONTokener jsonTokener = new JSONTokener( "{\"string\":\"json\"}" );
      JSONObject json = JSONObject.fromObject( jsonTokener );
      assertEquals( "json", json.getString( "string" ) );
   }

   public void testFromObject_Map() {
      Map map = new HashMap();
      map.put( "bool", Boolean.TRUE );
      map.put( "integer", new Integer( 42 ) );
      map.put( "string", "json" );
      map.put( "array", JSONArray.fromObject( "[1]" ) );
      map.put( "object", JSONObject.fromObject( "{\"name\":\"json\"}" ) );

      JSONObject json = JSONObject.fromObject( map );
      assertEquals( true, json.getBoolean( "bool" ) );
      assertEquals( 42, json.getInt( "integer" ) );
      assertEquals( "json", json.getString( "string" ) );
      Assertions.assertEquals( JSONArray.fromObject( "[1]" ), json.getJSONArray( "array" ) );
      Assertions.assertEquals( JSONObject.fromObject( "{\"name\":\"json\"}" ),
            json.getJSONObject( "object" ) );
   }

   public void testFromObject_nested_bean() {
      JSONObject json = JSONObject.fromObject( new BeanC() );
      assertNotNull( json.get( "beanA" ) );
      assertNotNull( json.get( "beanB" ) );
   }

   public void testFromObject_null() {
      JSONObject json = JSONObject.fromObject( null );
      assertTrue( json.isNullObject() );
      assertEquals( JSONNull.getInstance()
            .toString(), json.toString() );
   }

   public void testFromObject_ObjectBean() {
      // FR 1611204
      ObjectBean bean = new ObjectBean();
      bean.setPbyte( Byte.valueOf( "1" ) );
      bean.setPshort( Short.valueOf( "1" ) );
      bean.setPint( Integer.valueOf( "1" ) );
      bean.setPlong( Long.valueOf( "1" ) );
      bean.setPfloat( Float.valueOf( "1" ) );
      bean.setPdouble( Double.valueOf( "1" ) );
      bean.setPchar( new Character( '1' ) );
      bean.setPboolean( Boolean.TRUE );
      bean.setPstring( "json" );
      bean.setParray( new String[] { "a", "b" } );
      bean.setPbean( new BeanA() );
      List list = new ArrayList();
      list.add( "1" );
      list.add( "2" );
      bean.setPlist( list );
      Map map = new HashMap();
      map.put( "string", "json" );
      bean.setPmap( map );
      bean.setPfunction( new JSONFunction( "this;" ) );

      JSONObject json = JSONObject.fromObject( bean );
      assertEquals( 1, json.getInt( "pbyte" ) );
      assertEquals( 1, json.getInt( "pshort" ) );
      assertEquals( 1, json.getInt( "pint" ) );
      assertEquals( 1, json.getInt( "plong" ) );
      assertEquals( 1d, json.getDouble( "pfloat" ), 0d );
      assertEquals( 1d, json.getDouble( "pdouble" ), 0d );
      assertTrue( json.getBoolean( "pboolean" ) );
      assertEquals( "json", json.get( "pstring" ) );
      Assertions.assertEquals( JSONArray.fromObject( "['a','b']" ), json.getJSONArray( "parray" ) );
      Assertions.assertEquals( JSONArray.fromObject( "['1','2']" ), json.getJSONArray( "plist" ) );
      assertEquals( "1", json.getString( "pchar" ) );
      JSONObject b = new JSONObject().element( "string", "json" )
            .element( "integer", "42" )
            .element( "bool", "true" );
      Assertions.assertEquals( b, json.getJSONObject( "pbean" ) );
      b = new JSONObject().element( "string", "json" );
      Assertions.assertEquals( b, json.getJSONObject( "pmap" ) );
   }

   public void testFromObject_ObjectBean_empty() {
      // FR 1611204
      ObjectBean bean = new ObjectBean();
      JSONObject json = JSONObject.fromObject( bean );

      String[] keys = { "pbyte", "pshort", "pint", "plong", "pfloat", "pdouble", "pboolean",
            "pchar", "pstring", "parray", "plist", "pmap", "pbean" };
      for( int i = 0; i < keys.length; i++ ){
         assertTrue( JSONNull.getInstance()
               .equals( json.get( keys[i] ) ) );
      }
   }

   public void testFromObject_String() {
      JSONObject json = JSONObject.fromObject( "{\"string\":\"json\"}" );
      assertEquals( "json", json.getString( "string" ) );
   }

   public void testFromObject_toBean_DynaBean() {
      // bug report 1540137

      String jsondata = "{\"person\":{\"phone\":[\"111-222-3333\",\"777-888-9999\"],"
            + "\"address\":{\"street\":\"123 somewhere place\",\"zip\":\"30005\",\"city\":\"Alpharetta\"},"
            + "\"email\":[\"allen@work.com\",\"allen@home.net\"],\"name\":\"Allen\"}}";

      JSONObject jsonobj = JSONObject.fromObject( jsondata );
      Object bean = JSONObject.toBean( jsonobj );
      // bean is a DynaBean
      assertTrue( bean instanceof MorphDynaBean );
      // convert the DynaBean to a JSONObject again
      JSONObject jsonobj2 = JSONObject.fromObject( bean );

      assertNotNull( jsonobj.getJSONObject( "person" ) );
      assertFalse( JSONUtils.isNull( jsonobj.getJSONObject( "person" ) ) );
      assertNotNull( jsonobj2.getJSONObject( "person" ) );
      assertFalse( JSONUtils.isNull( jsonobj2.getJSONObject( "person" ) ) );

      JSONObject person1 = jsonobj.getJSONObject( "person" );
      JSONObject person2 = jsonobj2.getJSONObject( "person" );
      assertEquals( person1.get( "name" ), person2.get( "name" ) );
      assertEquals( person1.get( "phone" )
            .toString(), person2.get( "phone" )
            .toString() );
      assertEquals( person1.get( "email" )
            .toString(), person2.get( "email" )
            .toString() );
      JSONObject address1 = person1.getJSONObject( "address" );
      JSONObject address2 = person2.getJSONObject( "address" );
      assertEquals( address1.get( "street" ), address2.get( "street" ) );
      assertEquals( address1.get( "zip" ), address2.get( "zip" ) );
      assertEquals( address1.get( "city" ), address2.get( "city" ) );
   }

   public void testFromObject_use_wrappers() {
      JSONObject json = JSONObject.fromObject( Boolean.TRUE );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Byte( Byte.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Short( Short.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Integer( Integer.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Long( Long.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Float( Float.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Double( Double.MIN_VALUE ) );
      assertTrue( json.isEmpty() );
      json = JSONObject.fromObject( new Character( 'A' ) );
      assertTrue( json.isEmpty() );
   }

   public void testFromObject_withCustomDefaultValueProcessor() {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.registerDefaultValueProcessor( Integer.class, new NumberDefaultValueProcessor() );
      JSONObject jsonObject = JSONObject.fromObject( new NumberBean(), jsonConfig );
      assertNotNull( jsonObject );
      assertEquals( new Integer( 0 ), jsonObject.get( "pwbyte" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pwshort" ) );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pwint" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pwlong" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pwfloat" ) );
      assertEquals( new Double( 0 ), jsonObject.get( "pwdouble" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pbigdec" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pbigint" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pbyte" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pshort" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pint" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "plong" ) );
      assertEquals( new Double( 0 ), jsonObject.get( "pfloat" ) );
      assertEquals( new Double( 0 ), jsonObject.get( "pdouble" ) );
   }

   public void testFromObject_withCustomDefaultValueProcessor_andMatcher() {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.registerDefaultValueProcessor( Integer.class, new NumberDefaultValueProcessor() );
      jsonConfig.setDefaultValueProcessorMatcher( new NumberDefaultValueProcessorMatcher() );
      JSONObject jsonObject = JSONObject.fromObject( new NumberBean(), jsonConfig );
      assertNotNull( jsonObject );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pbigdec" ) );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pbigint" ) );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pwbyte" ) );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pwshort" ) );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pwint" ) );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pwlong" ) );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pwfloat" ) );
      assertEquals( NumberDefaultValueProcessor.NUMBER, jsonObject.get( "pwdouble" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pbyte" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pshort" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "pint" ) );
      assertEquals( new Integer( 0 ), jsonObject.get( "plong" ) );
      assertEquals( new Double( 0 ), jsonObject.get( "pfloat" ) );
      assertEquals( new Double( 0 ), jsonObject.get( "pdouble" ) );
   }

   public void testFromObject_withExcludesPerClass() {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.registerPropertyExclusion( BeanA.class, "bool" );
      JSONObject jsonA = JSONObject.fromObject( new BeanA(), jsonConfig );
      JSONObject jsonB = JSONObject.fromObject( new BeanB(), jsonConfig );
      assertNotNull( jsonA );
      assertNotNull( jsonB );
      assertFalse( jsonA.has("bool") );
      assertTrue( jsonB.has("bool") );
   }

   public void testFromObject_withExcludesPerClassAndMatcher() {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.registerPropertyExclusion( BeanA.class, "bool" );
      jsonConfig.setPropertyExclusionClassMatcher( new BeanAPropertyExclusionClassMatcher() );
      JSONObject jsonA = JSONObject.fromObject( new BeanA(), jsonConfig );
      JSONObject jsonB = JSONObject.fromObject( new BeanB(), jsonConfig );
      assertNotNull( jsonA );
      assertNotNull( jsonB );
      assertFalse( jsonA.has("bool") );
      assertFalse( jsonB.has("bool") );
   }
   
   public void testFromObject_withFilters() {
      PrimitiveBean bean = new PrimitiveBean();
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setJsonPropertyFilter( new NumberPropertyFilter() );
      JSONObject json = JSONObject.fromObject( bean, jsonConfig );
      assertNotNull( json );
      assertTrue( json.has( "pbean" ) );
      assertTrue( json.has( "pclass" ) );
      assertTrue( json.has( "pexcluded" ) );
      assertTrue( json.has( "pfunction" ) );
      assertTrue( json.has( "plist" ) );
      assertTrue( json.has( "pmap" ) );
      assertTrue( json.has( "pstring" ) );
      assertTrue( json.has( "parray" ) );

      assertTrue( json.has( "pboolean" ) );
      assertTrue( !json.has( "pbyte" ) );
      assertTrue( !json.has( "pshort" ) );
      assertTrue( !json.has( "pint" ) );
      assertTrue( !json.has( "plong" ) );
      assertTrue( !json.has( "pfloat" ) );
      assertTrue( !json.has( "pdouble" ) );
      assertTrue( json.has( "pchar" ) );
   }
   
   public void testFromObject_withFiltersAndExcludes() {
      PrimitiveBean bean = new PrimitiveBean();
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setJsonPropertyFilter( new NumberPropertyFilter() );
      jsonConfig.setExcludes( new String[] { "pexcluded" } );
      JSONObject json = JSONObject.fromObject( bean, jsonConfig );
      assertNotNull( json );
      assertTrue( json.has( "pbean" ) );
      assertTrue( json.has( "pclass" ) );
      assertTrue( !json.has( "pexcluded" ) );
      assertTrue( json.has( "pfunction" ) );
      assertTrue( json.has( "plist" ) );
      assertTrue( json.has( "pmap" ) );
      assertTrue( json.has( "pstring" ) );
      assertTrue( json.has( "parray" ) );

      assertTrue( json.has( "pboolean" ) );
      assertTrue( !json.has( "pbyte" ) );
      assertTrue( !json.has( "pshort" ) );
      assertTrue( !json.has( "pint" ) );
      assertTrue( !json.has( "plong" ) );
      assertTrue( !json.has( "pfloat" ) );
      assertTrue( !json.has( "pdouble" ) );
      assertTrue( json.has( "pchar" ) );
   }

   public void testFromString_null_String() {
      JSONObject json = JSONObject.fromObject( null );
      assertTrue( json.isNullObject() );
      assertEquals( JSONNull.getInstance()
            .toString(), json.toString() );
   }

   public void testHas() {
      assertFalse( new JSONObject().has( "any" ) );
      assertTrue( new JSONObject().element( "any", "value" )
            .has( "any" ) );
   }

   public void testLength() {
      assertEquals( 0, new JSONObject().size() );
   }

   public void testLength_nullObject() {
      /*
      try{
         new JSONObject( true ).size();
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // ok
      }
      */
      assertEquals( 0, new JSONObject(true).size() );
   }

   public void testOptBoolean() {
      assertFalse( new JSONObject().optBoolean( "any" ) );
   }

   public void testOptBoolean_defaultValue() {
      assertTrue( new JSONObject().optBoolean( "any", true ) );
   }

   public void testOptDouble() {
      assertTrue( Double.isNaN( new JSONObject().optDouble( "any" ) ) );
   }

   public void testOptDouble_defaultValue() {
      assertEquals( 2d, new JSONObject().optDouble( "any", 2d ), 0d );
   }

   public void testOptInt() {
      assertEquals( 0, new JSONObject().optInt( "any" ) );
   }

   public void testOptInt_defaultValue() {
      assertEquals( 1, new JSONObject().optInt( "any", 1 ) );
   }

   public void testOptJSONArray() {
      JSONObject json = new JSONObject();
      assertNull( json.optJSONArray( "a" ) );
      json.element( "a", "[]" );
      Assertions.assertEquals( new JSONArray(), json.optJSONArray( "a" ) );
   }

   public void testOptJSONObject() {
      JSONObject json = new JSONObject();
      assertNull( json.optJSONObject( "a" ) );
      json.element( "a", "{}" );
      Assertions.assertEquals( new JSONObject(), json.optJSONObject( "a" ) );
   }

   public void testOptLong() {
      assertEquals( 0L, new JSONObject().optLong( "any" ) );
   }

   public void testOptLong_defaultValue() {
      assertEquals( 1L, new JSONObject().optLong( "any", 1L ) );
   }

   public void testOptString() {
      assertEquals( "", new JSONObject().optString( "any" ) );
   }

   public void testOptString_defaultValue() {
      assertEquals( "json", new JSONObject().optString( "any", "json" ) );
   }

   public void testToBean() throws Exception {
      String json = "{name=\"json\",bool:true,int:1,double:2.2,func:function(a){ return a; },array:[1,2]}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      Object bean = JSONObject.toBean( jsonObject );
      assertEquals( jsonObject.get( "name" ), PropertyUtils.getProperty( bean, "name" ) );
      assertEquals( jsonObject.get( "bool" ), PropertyUtils.getProperty( bean, "bool" ) );
      assertEquals( jsonObject.get( "int" ), PropertyUtils.getProperty( bean, "int" ) );
      assertEquals( jsonObject.get( "double" ), PropertyUtils.getProperty( bean, "double" ) );
      assertEquals( jsonObject.get( "func" ), PropertyUtils.getProperty( bean, "func" ) );
      List expected = (List) JSONArray.toCollection( jsonObject.getJSONArray( "array" ) );
      Assertions.assertEquals( expected, (List) PropertyUtils.getProperty( bean, "array" ) );
   }

   public void testToBean_BeanA() {
      String json = "{bool:true,integer:1,string:\"json\"}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      BeanA bean = (BeanA) JSONObject.toBean( jsonObject, BeanA.class );
      assertEquals( jsonObject.get( "bool" ), Boolean.valueOf( bean.isBool() ) );
      assertEquals( jsonObject.get( "integer" ), new Integer( bean.getInteger() ) );
      assertEquals( jsonObject.get( "string" ), bean.getString() );
   }

   public void testToBean_BeanB() {
      String json = "{bool:true,integer:1,string:\"json\",intarray:[4,5,6]}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      BeanB bean = (BeanB) JSONObject.toBean( jsonObject, BeanB.class );
      assertEquals( jsonObject.get( "bool" ), Boolean.valueOf( bean.isBool() ) );
      assertEquals( jsonObject.get( "integer" ), new Integer( bean.getInteger() ) );
      assertEquals( jsonObject.get( "string" ), bean.getString() );
      Assertions.assertEquals( bean.getIntarray(),
            JSONArray.toArray( jsonObject.getJSONArray( "intarray" ) ) );
   }

   public void testToBean_BeanD() {
      String json = "{bool:true,integer:1,string:\"json\",doublearray:[4.2424245783E7, 123456789.2424245783E7, 6.0]}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      BeanD bean = (BeanD) JSONObject.toBean( jsonObject, BeanD.class );
      assertEquals( jsonObject.get( "bool" ), Boolean.valueOf( bean.isBool() ) );
      assertEquals( jsonObject.get( "integer" ), new Integer( bean.getInteger() ) );
      assertEquals( jsonObject.get( "string" ), bean.getString() );
      Assertions.assertEquals( bean.getDoublearray(),
              JSONArray.toArray( jsonObject.getJSONArray( "doublearray" ) ) );
   }

   public void testToBean_ClassBean() {
      JSONObject json = new JSONObject();
      json.element( "klass", "java.lang.Object" );

      ClassBean bean = (ClassBean) JSONObject.toBean( json, ClassBean.class );
      assertEquals( Object.class, bean.getKlass() );
   }

   public void testToBean_DynaBean__BigInteger_Double() {
      BigInteger l = new BigDecimal( "1.7976931348623157E308" ).toBigInteger();
      BigDecimal m = new BigDecimal( "1.7976931348623157E307" ).add( new BigDecimal( "0.0001" ) );
      JSONObject json = new JSONObject().element( "i", BigInteger.ZERO )
              .element( "d", MorphUtils.BIGDECIMAL_ONE )
              .element( "bi", l )
              .element( "bd", m );
      Object bean = JSONObject.toBean( json );
      Object i = ((MorphDynaBean) bean).get( "i" );
      Object d = ((MorphDynaBean) bean).get( "d" );
      assertTrue( i instanceof Integer );
      assertTrue( d instanceof Integer );

      Object bi = ((MorphDynaBean) bean).get( "bi" );
      Object bd = ((MorphDynaBean) bean).get( "bd" );
      assertTrue( bi instanceof BigInteger );
      assertTrue( bd instanceof Double );
   }


   public void testToBean_DynaBean__BigInteger_BigDecimal() {
      BigInteger l = new BigDecimal( "1.7976931348623157E308" ).toBigInteger();
      BigDecimal m = new BigDecimal( "-1.7976931348623157E309" ).add( new BigDecimal( "0.0001" ) );
      JSONObject json = new JSONObject().element( "i", BigInteger.ZERO )
            .element( "d", MorphUtils.BIGDECIMAL_ONE )
            .element( "bi", l )
            .element( "bd", m );
      Object bean = JSONObject.toBean( json );
      Object i = ((MorphDynaBean) bean).get( "i" );
      Object d = ((MorphDynaBean) bean).get( "d" );
      assertTrue( i instanceof Integer );
      assertTrue( d instanceof Integer );

      Object bi = ((MorphDynaBean) bean).get( "bi" );
      Object bd = ((MorphDynaBean) bean).get( "bd" );
      assertTrue( bi instanceof BigInteger );
      assertTrue( bd instanceof BigDecimal );
   }

   public void testToBean_emptyBean() {
      EmptyBean bean = new EmptyBean();

      JSONObject json = JSONObject.fromObject( bean );
      JSONObject expected = new JSONObject();
      expected.element( "arrayp", new JSONArray() );
      expected.element( "listp", new JSONArray() );
      expected.element( "bytep", new Integer( 0 ) );
      expected.element( "shortp", new Integer( 0 ) );
      expected.element( "intp", new Integer( 0 ) );
      expected.element( "longp", new Integer( 0 ) );
      expected.element( "floatp", new Integer( 0 ) );
      expected.element( "doublep", new Double( 0 ) );
      expected.element( "charp", "" );
      expected.element( "stringp", "" );

      Assertions.assertEquals( expected, json );

      EmptyBean bean2 = (EmptyBean) JSONObject.toBean( json, EmptyBean.class );

      ArrayAssertions.assertEquals( new Object[0], bean2.getArrayp() );
      Assertions.assertEquals( new ArrayList(), bean2.getListp() );
      Assertions.assertEquals( new Byte( (byte) 0 ), bean2.getBytep() );
      Assertions.assertEquals( new Short( (short) 0 ), bean2.getShortp() );
      Assertions.assertEquals( new Integer( 0 ), bean2.getIntp() );
      Assertions.assertEquals( new Long( 0 ), bean2.getLongp() );
      Assertions.assertEquals( new Float( 0 ), bean2.getFloatp() );
      Assertions.assertEquals( new Double( 0 ), bean2.getDoublep() );
      Assertions.assertEquals( new Character( '\0' ), bean2.getCharp() );
      Assertions.assertEquals( "", bean2.getStringp() );
   }

   public void testToBean_interface() {
      // BUG 1542104

      try{
         JSONObject.toBean( JSONObject.fromObject( "{\"int\":1}" ), Serializable.class );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // ok
      }
   }

   public void testToBean_Map() {
      // BUG 1542104

      Map map = new HashMap();
      map.put( "name", "json" );
      Object obj = JSONObject.toBean( JSONObject.fromObject( map ), Map.class );
      assertTrue( obj instanceof Map );
      assertEquals( map.get( "name" ), ((Map) obj).get( "name" ) );
   }

   public void testToBean_nested() throws Exception {
      String json = "{name=\"json\",bool:true,int:1,double:2.2,func:function(a){ return a; },nested:{nested:true}}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      Object bean = JSONObject.toBean( jsonObject );
      assertEquals( jsonObject.get( "name" ), PropertyUtils.getProperty( bean, "name" ) );
      assertEquals( jsonObject.get( "bool" ), PropertyUtils.getProperty( bean, "bool" ) );
      assertEquals( jsonObject.get( "int" ), PropertyUtils.getProperty( bean, "int" ) );
      assertEquals( jsonObject.get( "double" ), PropertyUtils.getProperty( bean, "double" ) );
      assertEquals( jsonObject.get( "func" ), PropertyUtils.getProperty( bean, "func" ) );
      JSONObject nestedJson = jsonObject.getJSONObject( "nested" );
      Object nestedBean = PropertyUtils.getProperty( bean, "nested" );
      assertEquals( nestedJson.get( "nested" ), PropertyUtils.getProperty( nestedBean, "nested" ) );
   }

   public void testToBean_nested_beans__null_object() throws Exception {
      // BUG 1553617

      String json = "{\"beanA\":{bool:true,integer:1,string:\"jsonbean\"},\"beanB\":null}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      BeanC bean = (BeanC) JSONObject.toBean( jsonObject, BeanC.class );
      assertNotNull( bean );
      BeanA beanA = bean.getBeanA();
      assertNotNull( beanA );
      assertEquals( true, beanA.isBool() );
      assertEquals( 1, beanA.getInteger() );
      assertEquals( "jsonbean", beanA.getString() );
      BeanB beanB = bean.getBeanB();
      assertNull( beanB );
   }

   public void testToBean_nested_beans_in_list__beans() {
      // BUG 1592799

      ListingBean listingBean = new ListingBean();

      ValueBean beanA1 = new ValueBean();
      beanA1.setValue( 90000 );
      ValueBean beanA2 = new ValueBean();
      beanA2.setValue( 91000 );

      listingBean.addAttribute( beanA1 );
      listingBean.addAttribute( beanA2 );

      JSONObject jsonObject = JSONObject.fromObject( listingBean );
      Map classMap = new HashMap();
      classMap.put( "attributes", ValueBean.class );
      ListingBean listingBean2 = (ListingBean) JSONObject.toBean( jsonObject, ListingBean.class,
            classMap );
      List attributes = listingBean2.getAttributes();
      Object ba = attributes.get( 0 );
      Object bb = attributes.get( 1 );

      assertTrue( ba instanceof ValueBean );
      assertTrue( bb instanceof ValueBean );
      assertEquals( beanA1.getValue(), ((ValueBean) ba).getValue() );
      assertEquals( beanA2.getValue(), ((ValueBean) bb).getValue() );
   }

   public void testToBean_nested_beans_in_list__DynaBean() {
      // BUG 1592799

      ListingBean listingBean = new ListingBean();

      ValueBean beanA1 = new ValueBean();
      beanA1.setValue( 90000 );
      ValueBean beanA2 = new ValueBean();
      beanA2.setValue( 91000 );

      listingBean.addAttribute( beanA1 );
      listingBean.addAttribute( beanA2 );

      JSONObject jsonObject = JSONObject.fromObject( listingBean );
      ListingBean listingBean2 = (ListingBean) JSONObject.toBean( jsonObject, ListingBean.class );
      List attributes = listingBean2.getAttributes();
      Object ba = attributes.get( 0 );
      Object bb = attributes.get( 1 );

      assertTrue( ba instanceof MorphDynaBean );
      assertTrue( bb instanceof MorphDynaBean );
      assertEquals( new Integer( beanA1.getValue() ), ((MorphDynaBean) ba).get( "value" ) );
      assertEquals( new Integer( beanA2.getValue() ), ((MorphDynaBean) bb).get( "value" ) );
   }

   public void testToBean_nested_beans_in_map__beans() {
      // BUG 1542092

      MappingBean mappingBean = new MappingBean();

      ValueBean beanA = new ValueBean();
      beanA.setValue( 90000 );
      ValueBean beanB = new ValueBean();
      beanB.setValue( 91000 );

      mappingBean.addAttribute( "beanA", beanA );
      mappingBean.addAttribute( "beanB", beanB );

      JSONObject jsonObject = JSONObject.fromObject( mappingBean );
      Map classMap = new HashMap();
      classMap.put( "bean.*", ValueBean.class );
      MappingBean mappingBean2 = (MappingBean) JSONObject.toBean( jsonObject, MappingBean.class,
            classMap );
      Object ba = mappingBean2.getAttributes()
            .get( "beanA" );
      Object bb = mappingBean2.getAttributes()
            .get( "beanB" );
      assertTrue( ba instanceof ValueBean );
      assertTrue( bb instanceof ValueBean );
      assertEquals( beanA.getValue(), ((ValueBean) ba).getValue() );
      assertEquals( beanB.getValue(), ((ValueBean) bb).getValue() );
   }

   public void testToBean_nested_beans_in_map__DynaBean() {
      // BUG 1542092

      MappingBean mappingBean = new MappingBean();

      ValueBean beanA = new ValueBean();
      beanA.setValue( 90000 );
      ValueBean beanB = new ValueBean();
      beanB.setValue( 91000 );

      mappingBean.addAttribute( "beanA", beanA );
      mappingBean.addAttribute( "beanB", beanB );

      JSONObject jsonObject = JSONObject.fromObject( mappingBean );
      MappingBean mappingBean2 = (MappingBean) JSONObject.toBean( jsonObject, MappingBean.class );
      Object ba = mappingBean2.getAttributes()
            .get( "beanA" );
      Object bb = mappingBean2.getAttributes()
            .get( "beanB" );
      assertTrue( ba instanceof MorphDynaBean );
      assertTrue( bb instanceof MorphDynaBean );
      assertEquals( new Integer( beanA.getValue() ), ((MorphDynaBean) ba).get( "value" ) );
      assertEquals( new Integer( beanB.getValue() ), ((MorphDynaBean) bb).get( "value" ) );
   }

   public void testToBean_nested_beans_in_set__beans() {
      // FR 1847116

      SetBean setBean = new SetBean();

      ValueBean beanA1 = new ValueBean();
      beanA1.setValue( 90000 );
      ValueBean beanA2 = new ValueBean();
      beanA2.setValue( 91000 );

      setBean.addAttribute( beanA1 );
      setBean.addAttribute( beanA2 );

      JSONObject jsonObject = JSONObject.fromObject( setBean );
      Map classMap = new HashMap();
      classMap.put( "attributes", ValueBean.class );
      SetBean setBean2 = (SetBean) JSONObject.toBean( jsonObject, SetBean.class, classMap );
      assertEquals( setBean, setBean2 );
   }

   public void testToBean_nested_beans_in_set__DynaBean() {
      // FR 1847116

      SetBean setBean = new SetBean();

      ValueBean beanA1 = new ValueBean();
      beanA1.setValue( 90000 );
      ValueBean beanA2 = new ValueBean();
      beanA2.setValue( 91000 );

      setBean.addAttribute( beanA1 );
      setBean.addAttribute( beanA2 );

      JSONObject jsonObject = JSONObject.fromObject( setBean );
      //SetBean setBean2 = (SetBean) JSONObject.toBean( jsonObject, SetBean.class );
      //assertEquals( setBean, setBean2 );
   }

   public void testToBean_nested_dynabeans__null_object() throws Exception {
      // BUG 1553617

      String json = "{\"beanA\":{bool:true,integer:1,string:\"jsonbean\"},\"beanB\":null}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      Object bean = JSONObject.toBean( jsonObject );
      assertNotNull( bean );
      Object beanA = PropertyUtils.getProperty( bean, "beanA" );
      assertNotNull( beanA );
      assertEquals( Boolean.TRUE, PropertyUtils.getProperty( beanA, "bool" ) );
      assertEquals( new Integer( 1 ), PropertyUtils.getProperty( beanA, "integer" ) );
      assertEquals( "jsonbean", PropertyUtils.getProperty( beanA, "string" ) );
      Object beanB = PropertyUtils.getProperty( bean, "beanB" );
      assertNull( beanB );
   }

   public void testtoBean_noWriteMethod() {
      JSONObject json = new JSONObject();
      json.element( "propertyWithNoReadMethod", "json" );
      json.element( "propertyWithNoWriteMethod", "value" );
      PropertyBean bean = (PropertyBean) JSONObject.toBean( json, PropertyBean.class );
      assertNotNull( bean );
      assertEquals( "json", bean.valueOfPropertyWithNoReadMethod() );
      assertEquals( "json", bean.getPropertyWithNoWriteMethod() );
   }

   public void testToBean_null() {
      assertNull( JSONObject.toBean( null ) );
   }

   public void testToBean_null_2() {
      assertNull( JSONObject.toBean( null, BeanA.class ) );
   }

   public void testToBean_null_object() {
      JSONObject jsonObject = new JSONObject( true );
      BeanA bean = (BeanA) JSONObject.toBean( jsonObject, BeanA.class );
      assertNull( bean );
   }

   public void testToBean_null_values() {
      // bug report 1540196

      String json = "{\"items\":[[\"000\"],[\"010\", \"011\"],[\"020\"]]}";
      JSONObject jsonObject = JSONObject.fromObject( json );

      BeanFoo foo = (BeanFoo) JSONObject.toBean( jsonObject, BeanFoo.class );
      assertNotNull( foo );
      assertNotNull( foo.getItems() );
      String[][] items = foo.getItems();
      assertEquals( 3, items.length );
      assertEquals( "000", items[0][0] );
      assertEquals( "010", items[1][0] );
      assertEquals( "011", items[1][1] );
      assertEquals( "020", items[2][0] );
   }

   public void testToBean_NumberBean() {
      JSONObject json = new JSONObject();
      json.element( "pbyte", new Byte( (byte) 2 ) );
      json.element( "pshort", new Short( (short) 2 ) );
      json.element( "pint", new Integer( 2 ) );
      json.element( "plong", new Long( 2 ) );
      json.element( "pfloat", new Float( 2 ) );
      json.element( "pdouble", new Double( 2 ) );
      json.element( "pbigint", new BigInteger( "2" ) );
      json.element( "pbigdec", new BigDecimal( "2" ) );

      NumberBean bean = (NumberBean) JSONObject.toBean( json, NumberBean.class );
      assertEquals( (byte) 2, bean.getPbyte() );
      assertEquals( (short) 2, bean.getPshort() );
      assertEquals( 2, bean.getPint() );
      assertEquals( 2L, bean.getPlong() );
      assertEquals( 2f, bean.getPfloat(), 0f );
      assertEquals( 2d, bean.getPdouble(), 0d );
      assertEquals( new BigInteger( "2" ), bean.getPbigint() );
      assertEquals( new BigDecimal( "2" ), bean.getPbigdec() );
   }

   public void testToBean_NumberBean_2() {
      JSONObject json = new JSONObject();
      json.element( "pbyte", new Integer( 2 ) );
      json.element( "pshort", new Integer( 2 ) );
      json.element( "pint", new Integer( 2 ) );
      json.element( "plong", new Integer( 2 ) );
      json.element( "pfloat", new Integer( 2 ) );
      json.element( "pdouble", new Integer( 2 ) );
      json.element( "pbigint", new Integer( 2 ) );
      json.element( "pbigdec", new Integer( 2 ) );

      NumberBean bean = (NumberBean) JSONObject.toBean( json, NumberBean.class );
      assertEquals( (byte) 2, bean.getPbyte() );
      assertEquals( (short) 2, bean.getPshort() );
      assertEquals( 2, bean.getPint() );
      assertEquals( 2L, bean.getPlong() );
      assertEquals( 2f, bean.getPfloat(), 0f );
      assertEquals( 2d, bean.getPdouble(), 0d );
      assertEquals( new BigInteger( "2" ), bean.getPbigint() );
      assertEquals( new BigDecimal( "2" ), bean.getPbigdec() );
   }

   public void testToBean_ObjectBean() {
      // FR 1611204
      ObjectBean bean = new ObjectBean();
      bean.setPbyte( Byte.valueOf( "1" ) );
      bean.setPshort( Short.valueOf( "1" ) );
      bean.setPint( Integer.valueOf( "1" ) );
      bean.setPlong( Long.valueOf( "1" ) );
      bean.setPfloat( Float.valueOf( "1" ) );
      bean.setPdouble( Double.valueOf( "1" ) );
      bean.setPchar( new Character( '1' ) );
      bean.setPboolean( Boolean.TRUE );
      bean.setPstring( "json" );
      bean.setParray( new String[] { "a", "b" } );
      bean.setPbean( new BeanA() );
      List list = new ArrayList();
      list.add( "1" );
      list.add( "2" );
      bean.setPlist( list );
      Map map = new HashMap();
      map.put( "string", "json" );
      bean.setPmap( map );
      bean.setPfunction( new JSONFunction( "this;" ) );
      JSONObject json = JSONObject.fromObject( bean );
      Map classMap = new HashMap();
      classMap.put( "pbean", BeanA.class );
      ObjectBean obj = (ObjectBean) JSONObject.toBean( json, ObjectBean.class, classMap );
      assertEquals( Integer.valueOf( "1" ), obj.getPbyte() );
      assertEquals( Integer.valueOf( "1" ), obj.getPshort() );
      assertEquals( Integer.valueOf( "1" ), obj.getPint() );
      assertEquals( Integer.valueOf( "1" ), obj.getPlong() );
      assertEquals( Double.valueOf( "1" ), obj.getPfloat() );
      assertEquals( Double.valueOf( "1" ), obj.getPdouble() );
      assertEquals( "1", obj.getPchar() );
      assertEquals( "json", obj.getPstring() );
      List l = new ArrayList();
      l.add( "a" );
      l.add( "b" );
      ArrayAssertions.assertEquals( l.toArray(), (Object[]) obj.getParray() );
      l = new ArrayList();
      l.add( "1" );
      l.add( "2" );
      ArrayAssertions.assertEquals( l.toArray(), (Object[]) obj.getPlist() );
      assertEquals( new BeanA(), obj.getPbean() );
      assertTrue( obj.getPmap() instanceof MorphDynaBean );
   }

   public void testToBean_ObjectBean_empty() throws Exception {
      // FR 1611204
      ObjectBean bean = new ObjectBean();
      JSONObject json = JSONObject.fromObject( bean );
      Map classMap = new HashMap();
      classMap.put( "bean", BeanA.class );
      ObjectBean obj = (ObjectBean) JSONObject.toBean( json, ObjectBean.class, classMap );

      String[] keys = { "pbyte", "pshort", "pint", "plong", "pfloat", "pdouble", "pboolean",
            "pchar", "pstring", "parray", "plist", "pmap", "pbean" };
      for( int i = 0; i < keys.length; i++ ){
         assertNull( PropertyUtils.getProperty( obj, keys[i] ) );
      }
   }

   public void testToBean_rootObject() {
      JSONObject json = new JSONObject().element( "bool", "false" )
            .element( "integer", 84 )
            .element( "string", "bean" );
      BeanA expected = new BeanA();
      BeanA actual = (BeanA) JSONObject.toBean( json, expected, new JsonConfig() );
      assertNotNull( actual );
      assertEquals( expected, actual );
      assertFalse( actual.isBool() );
      assertEquals( 84, actual.getInteger() );
      assertEquals( "bean", actual.getString() );
   }

   public void testToBean_withFilters() {
      BeanA bean = new BeanA();
      bean.setBool( false );
      bean.setInteger( 84 );
      bean.setString( "filter" );
      JSONObject json = JSONObject.fromObject( bean );
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setRootClass( BeanA.class );
      jsonConfig.setJavaPropertyFilter( new BeanAPropertyFilter() );
      BeanA actual = (BeanA) JSONObject.toBean( json, jsonConfig );
      assertNotNull( actual );
      assertTrue( actual.isBool() );
      assertEquals( 42, actual.getInteger() );
      assertEquals( "filter", actual.getString() );
   }

   public void testToBean_withNonJavaIdentifier_camelCase_Strategy() {
      JSONObject json = new JSONObject().element( "camel case", "json" );
      jsonConfig.setJavaIdentifierTransformer( JavaIdentifierTransformer.CAMEL_CASE );
      jsonConfig.setRootClass( JavaIdentifierBean.class );
      JavaIdentifierBean bean = (JavaIdentifierBean) JSONObject.toBean( json, jsonConfig );
      assertNotNull( bean );
      assertEquals( "json", bean.getCamelCase() );
   }

   public void testToBean_withNonJavaIdentifier_underScore_Strategy() {
      JSONObject json = new JSONObject().element( "under score", "json" );
      jsonConfig.setJavaIdentifierTransformer( JavaIdentifierTransformer.UNDERSCORE );
      jsonConfig.setRootClass( JavaIdentifierBean.class );
      JavaIdentifierBean bean = (JavaIdentifierBean) JSONObject.toBean( json, jsonConfig );
      assertNotNull( bean );
      assertEquals( "json", bean.getUnder_score() );
   }

   public void testToBean_withNonJavaIdentifier_whitespace_Strategy() {
      JSONObject json = new JSONObject().element( " white space ", "json" );
      jsonConfig.setJavaIdentifierTransformer( JavaIdentifierTransformer.WHITESPACE );
      jsonConfig.setRootClass( JavaIdentifierBean.class );
      JavaIdentifierBean bean = (JavaIdentifierBean) JSONObject.toBean( json, jsonConfig );
      assertNotNull( bean );
      assertEquals( "json", bean.getWhitespace() );
   }

   public void testToBean_withPropertySetStrategy() {
      JSONObject json = new JSONObject().element( "key", "value" );
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setRootClass( MappingBean.class );
      jsonConfig.setPropertySetStrategy( new MappingPropertySetStrategy() );
      MappingBean bean = (MappingBean) JSONObject.toBean( json, jsonConfig );
      assertNotNull( bean );
      assertEquals( "value", bean.getAttributes()
            .get( "key" ) );
   }
   
   public void testToBeanWithJavaPropertyNameProcessor(){
      String json = "{bool:false}";
      JSONObject jsonObject = JSONObject.fromObject( json );
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.registerJavaPropertyNameProcessor( BeanA.class, new SwapPropertyNameProcessor() );
      jsonConfig.setRootClass( BeanA.class );
      BeanA bean = (BeanA) JSONObject.toBean( jsonObject, jsonConfig );
      assertNotNull( bean );
      assertTrue( bean.isBool() );
      assertEquals( "false", bean.getString() );
   }
   
   public void testToJSONArray() {
      String json = "{bool:true,integer:1,string:\"json\"}";
      JSONArray names = JSONArray.fromObject( "['string','integer','bool']" );
      JSONObject jsonObject = JSONObject.fromObject( json );
      JSONArray jsonArray = jsonObject.toJSONArray( names );
      assertEquals( "json", jsonArray.getString( 0 ) );
      assertEquals( 1, jsonArray.getInt( 1 ) );
      assertTrue( jsonArray.getBoolean( 2 ) );
   }

   protected void setUp() throws Exception {
      jsonConfig = new JsonConfig();
   }

   private MorphDynaBean createDynaBean() throws Exception {
      Map properties = new HashMap();
      properties.put( "name", String.class );
      properties.put( "func", JSONFunction.class );
      properties.put( "jsonstr", JSONString.class );
      properties.put( "json", JSON.class );
      properties.put( "str", String.class );
      MorphDynaClass dynaClass = new MorphDynaClass( properties );
      MorphDynaBean dynaBean = (MorphDynaBean) dynaClass.newInstance();
      dynaBean.setDynaBeanClass( dynaClass );
      dynaBean.set( "name", "json" );
      dynaBean.set( "func", new JSONFunction( "return this;" ) );
      dynaBean.set( "jsonstr", new ObjectJSONStringBean() );
      dynaBean.set( "json", new JSONObject().element( "id", "1" ) );
      dynaBean.set( "str", "[1,2]" );
      // JSON Strings can not be null, only empty
      return dynaBean;
   }

   public static class BeanAPropertyExclusionClassMatcher extends PropertyExclusionClassMatcher {
      public Object getMatch( Class target, Set set ) {
         for( Iterator i = set.iterator(); i.hasNext(); ){
            Class c = (Class) i.next();
            if( BeanA.class.isAssignableFrom( c ) ){
               return c;
            }
         }
         return null;
      }
   }

   public static class BeanAPropertyFilter implements PropertyFilter {
      public boolean apply( Object source, String name, Object value ) {
         if( "bool".equals( name ) || "integer".equals( name ) ){
            return true;
         }
         return false;
      }

   }

   public static class MappingPropertySetStrategy extends PropertySetStrategy {
      public void setProperty( Object bean, String key, Object value ) throws JSONException {
         ((MappingBean) bean).addAttribute( key, value );
      }
   }

   public static class NumberDefaultValueProcessor implements DefaultValueProcessor {
      public static final Integer NUMBER = new Integer( 42 );

      public Object getDefaultValue( Class type ) {
         return NUMBER;
      }
   }

   public static class NumberDefaultValueProcessorMatcher extends DefaultValueProcessorMatcher {
      public Object getMatch( Class target, Set set ) {
         for( Iterator i = set.iterator(); i.hasNext(); ){
            Class c = (Class) i.next();
            if( Number.class.isAssignableFrom( c ) ){
               return c;
            }
         }
         return null;
      }
   }
   
   public static class NumberPropertyFilter implements PropertyFilter {
      public boolean apply( Object source, String name, Object value ) {
         if( value != null && Number.class.isAssignableFrom( value.getClass() ) ){
            return true;
         }
         return false;
      }
   }
   
   public static class SwapPropertyNameProcessor implements PropertyNameProcessor {
      public String processPropertyName( Class beanClass, String name ) {
         if( name.equals("bool")){
            return "string";
         }
         return name;
      }
   }
   
   public void test_fromJSONObject() {
   }

    public void testCanonicalWrite() throws Exception {
        JSONArray a = new JSONArray();
        a.add(Boolean.valueOf(true));
//        a.add(null);
        a.add(Integer.valueOf(1));
        a.add(Double.valueOf(5.3));
        JSONObject o = new JSONObject();
        o.put("key1","1");
        o.put("key2","2");
        o.put("key3","3");
        o.put("string","123\r\n\b\t\f\\\\u65E5\\u672C\\u8A9E");
        a.add(o);

        StringWriter sw = new StringWriter();
        a.writeCanonical( sw );
        assertEquals(sw.toString(),"[true,1,5.3,{\"key1\":\"1\",\"key2\":\"2\",\"key3\":\"3\",\"string\":\"123\\u000d\\u000a\\u0008\\u0009\\u000c\\\\\\\\u65E5\\\\u672C\\\\u8A9E\"}]");
    }
}