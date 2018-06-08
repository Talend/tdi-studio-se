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
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.ezmorph.MorphUtils;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.ezmorph.bean.MorphDynaClass;
import net.sf.json.sample.ArrayJSONStringBean;
import net.sf.json.sample.BeanA;
import net.sf.json.util.JSONTokener;

import org.apache.commons.beanutils.DynaBean;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONArray extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONArray.class );
   }

   public TestJSONArray( String testName ) {
      super( testName );
   }

   public void testDiscard() {
      JSONArray jsonArray = new JSONArray().element( "1" )
            .element( "true" )
            .element( "string" )
            .element( "function(){ return this; }" )
            .element( "[1,2,3]" );
      assertEquals( 5, jsonArray.size() );
      jsonArray.discard( "string" )
            .discard( 0 );
      assertEquals( 3, jsonArray.size() );
      assertFalse( jsonArray.contains( "string" ) );
      assertFalse( jsonArray.contains( "1" ) );
   }

   public void testConstructor_Collection() {
      List l = new ArrayList();
      l.add( Boolean.TRUE );
      l.add( new Integer( 1 ) );
      l.add( "string" );
      l.add( Object.class );
      testJSONArray( l, "[true,1,\"string\",\"java.lang.Object\"]" );
   }

   public void testConstructor_Collection_JSONArray() {
      List l = new ArrayList();
      l.add( JSONArray.fromObject( new int[] { 1, 2 } ) );
      testJSONArray( l, "[[1,2]]" );
   }

   public void testConstructor_Collection_JSONFunction() {
      List l = new ArrayList();
      l.add( new JSONFunction( new String[] { "a" }, "return a;" ) );
      testJSONArray( l, "[function(a){ return a; }]" );
   }

   public void testConstructor_Collection_JSONString() {
      ArrayJSONStringBean bean = new ArrayJSONStringBean();
      bean.setValue( "'json','json'" );
      List l = new ArrayList();
      l.add( bean );
      testJSONArray( l, "[[\"json\",\"json\"]]" );
   }

   public void testConstructor_Collection_nulls() {
      List l = new ArrayList();
      l.add( null );
      l.add( null );
      testJSONArray( l, "[null,null]" );
   }

   public void testConstructor_func() {
      JSONArray expected = JSONArray.fromObject( new String[] { "'"
            + new JSONFunction( new String[] { "a" }, "return a;" ).toString() + "'" } );
      JSONArray actual = JSONArray.fromObject( new String[] { "'function(a){ return a; }'" } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_func2() {
      JSONArray expected = JSONArray.fromObject( new String[] { "\""
            + new JSONFunction( new String[] { "a" }, "return a;" ).toString() + "\"" } );
      JSONArray actual = JSONArray.fromObject( new String[] { "\"function(a){ return a; }\"" } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_JSONArray() {
      JSONArray expected = JSONArray.fromObject( "[1,2]" );
      JSONArray actual = JSONArray.fromObject( JSONArray.fromObject( "[1,2]" ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_JSONTokener_functions() {
      testJSONArray( new JSONTokener( "[function(a){ return a; }]" ), "[function(a){ return a; }]" );
   }

   public void testConstructor_JSONTokener_nulls() {
      testJSONArray( new JSONTokener( "[,,]" ), "[null,null]" );
   }

   public void testConstructor_JSONTokener_syntax_errors() {
      try{
         JSONArray.fromObject( "" );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // ok
      }
   }

   public void testConstructor_Object_Array() {
      JSONArray expected = JSONArray.fromObject( "[\"json\",1]" );
      JSONArray actual = JSONArray.fromObject( new Object[] { "json", new Integer( 1 ) } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_Object_Array_Array() {
      JSONArray expected = JSONArray.fromObject( "[[1,2]]" );
      JSONArray actual = JSONArray.fromObject( new Object[] { new int[] { 1, 2 } } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_Object_Array_BigDecimal() {
      // bug 1596168

      // an array of BigDecimals
      Number[] numberArray = new Number[] { BigDecimal.valueOf( 10000000000L, 10 ),
            new BigDecimal( "-1.0" ), new BigDecimal( "99.99E-1" ) };

      assertEquals( "1.0000000000", numberArray[0].toString() );
      assertEquals( "-1.0", numberArray[1].toString() );
      assertEquals( "9.999", numberArray[2].toString() );

      JSONArray jsonNumArray = JSONArray.fromObject( numberArray );

      assertEquals( "1.0000000000", jsonNumArray.get( 0 )
            .toString() );
      assertEquals( "-1.0", jsonNumArray.get( 1 )
            .toString() );
      assertEquals( "9.999", jsonNumArray.get( 2 )
            .toString() );
   }

   public void testConstructor_Object_Array_BigInteger() {
      // bug 1596168

      Number[] numberArray = new Number[] { new BigInteger( "18437736874454810627" ),
            new BigInteger( "9007199254740990" ) };

      assertEquals( "18437736874454810627", numberArray[0].toString() );
      assertEquals( "9007199254740990", numberArray[1].toString() );

      JSONArray jsonNumArray = JSONArray.fromObject( numberArray );

      assertEquals( "18437736874454810627", jsonNumArray.get( 0 )
            .toString() );
      assertEquals( "9007199254740990", jsonNumArray.get( 1 )
            .toString() );
   }

   public void testConstructor_Object_Array_Class() {
      JSONArray expected = JSONArray.fromObject( "[\"java.lang.Object\"]" );
      JSONArray actual = JSONArray.fromObject( new Object[] { Object.class } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_Object_Array_functions() {
      JSONArray expected = JSONArray.fromObject( "[function(a){ return a; }]" );
      JSONArray actual = JSONArray.fromObject( new JSONFunction[] { new JSONFunction(
            new String[] { "a" }, "return a;" ) } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_Object_Array_functions_2() {
      JSONArray expected = JSONArray.fromObject( new JSONFunction[] { new JSONFunction(
            new String[] { "a" }, "return a;" ) } );
      JSONArray actual = JSONArray.fromObject( "[function(a){ return a; }]" );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_Object_Array_functions_3() {
      JSONArray expected = JSONArray.fromObject( new JSONFunction[] { new JSONFunction(
            new String[] { "a" }, "return a;" ) } );
      JSONArray actual = JSONArray.fromObject( new String[] { "function(a){ return a; }" } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_Object_Array_JSONArray() {
      JSONArray expected = JSONArray.fromObject( "[[1,2]]" );
      JSONArray actual = JSONArray.fromObject( new Object[] { JSONArray.fromObject( "[1,2]" ) } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_Object_Array_JSONString() {
      ArrayJSONStringBean bean = new ArrayJSONStringBean();
      bean.setValue( "'json','json'" );
      JSONArray expected = JSONArray.fromObject( "[[\"json\",\"json\"]]" );
      JSONArray actual = JSONArray.fromObject( new Object[] { bean } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_Object_Array_nulls() {
      JSONArray expected = JSONArray.fromObject( "[null,null]" );
      JSONArray actual = JSONArray.fromObject( new Object[] { null, null } );
      Assertions.assertEquals( expected, actual );
   }

   public void testConstructor_primitive_array_boolean() {
      testJSONArray( new boolean[] { true, false }, "[true,false]" );
   }

   public void testConstructor_primitive_array_byte() {
      testJSONArray( new byte[] { 1, 2, 3 }, "[1,2,3]" );
   }

   public void testConstructor_primitive_array_char() {
      testJSONArray( new char[] { 'a', 'b', 'c' }, "[\"a\",\"b\",\"c\"]" );
   }

   public void testConstructor_primitive_array_double() {
      testJSONArray( new double[] { 1.1, 2.2, 3.3 }, "[1.1,2.2,3.3]" );
   }

   public void testConstructor_primitive_array_double_Infinity() {
      try{
         JSONArray.fromObject( new double[] { Double.NEGATIVE_INFINITY } );
         fail( "Should have thrown a JSONException" );
      }catch( JSONException expected ){
         // OK
      }

      try{
         JSONArray.fromObject( new double[] { Double.POSITIVE_INFINITY } );
         fail( "Should have thrown a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testConstructor_primitive_array_double_NaNs() {
      try{
         JSONArray.fromObject( new double[] { Double.NaN } );
         fail( "Should have thrown a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testConstructor_primitive_array_float() {
      testJSONArray( new float[] { 1.1f, 2.2f, 3.3f }, "[1.1,2.2,3.3]" );
   }

   public void testConstructor_primitive_array_float_Infinity() {
      try{
         JSONArray.fromObject( new float[] { Float.NEGATIVE_INFINITY } );
         fail( "Should have thrown a JSONException" );
      }catch( JSONException expected ){
         // OK
      }

      try{
         JSONArray.fromObject( new float[] { Float.POSITIVE_INFINITY } );
         fail( "Should have thrown a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testConstructor_primitive_array_float_NaNs() {
      try{
         JSONArray.fromObject( new float[] { Float.NaN } );
         fail( "Should have thrown a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testConstructor_primitive_array_int() {
      testJSONArray( new int[] { 1, 2, 3 }, "[1,2,3]" );
   }

   public void testConstructor_primitive_array_long() {
      testJSONArray( new long[] { 1, 2, 3 }, "[1,2,3]" );
   }

   public void testConstructor_primitive_array_short() {
      testJSONArray( new short[] { 1, 2, 3 }, "[1,2,3]" );
   }

   public void testConstructor_String_functions() {
      testJSONArray( "[function(a){ return a; }]", "[function(a){ return a; }]" );
   }

   public void testConstructor_String_functions_multi() {
      testJSONArray( "[function(a){ return a; },[function(b){ return b; }]]",
            "[function(a){ return a; },[function(b){ return b; }]]" );
   }

   public void testCycleDetection_arrays() {
      Object[] array1 = new Object[2];
      Object[] array2 = new Object[2];
      array1[0] = new Integer( 1 );
      array1[1] = array2;
      array2[0] = new Integer( 2 );
      array2[1] = array1;
      try{
         JSONArray.fromObject( array1 );
         fail( "A JSONException was expected" );
      }catch( JSONException expected ){
         assertTrue( expected.getMessage()
               .endsWith( "There is a cycle in the hierarchy!" ) );
      }
   }

   public void testElement_Array() {
      JSONArray array = new JSONArray();
      int[] ints = { 1, 2 };
      array.element( ints );
      Assertions.assertEquals( JSONArray.fromObject( ints ), array.getJSONArray( 0 ) );
   }

   public void testElement_boolean() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( true );
      assertEquals( 1, jsonArray.size() );
      assertTrue( jsonArray.getBoolean( 0 ) );
   }

   public void testElement_Boolean() {
      JSONArray array = new JSONArray();
      array.element( Boolean.TRUE );
      Assertions.assertTrue( array.getBoolean( 0 ) );
   }

   public void testElement_Collection() {
      List l = new ArrayList();
      l.add( Boolean.TRUE );
      l.add( new Integer( 1 ) );
      l.add( "string" );
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( l );
      assertEquals( 1, jsonArray.size() );
      Assertions.assertEquals( JSONArray.fromObject( "[true,1,\"string\"]" ),
            jsonArray.getJSONArray( 0 ) );
   }

   public void testElement_double() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 2.0d );
      assertEquals( 1, jsonArray.size() );
      assertEquals( 2.0d, jsonArray.getDouble( 0 ), 0d );
   }

   public void testElement_index_0_Array() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      ;
      int[] ints = { 0, 2 };
      array.element( 0, ints );
      Assertions.assertEquals( JSONArray.fromObject( ints ), array.getJSONArray( 0 ) );
   }

   public void testElement_index_0_Boolean() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      ;
      array.element( 0, Boolean.TRUE );
      Assertions.assertTrue( array.getBoolean( 0 ) );
   }

   public void testElement_index_0_Class() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      array.element( 0, Object.class );
      Assertions.assertEquals( "java.lang.Object", array.getString( 0 ) );
   }

   public void testElement_index_0_JSON() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      array.element( 0, JSONNull.getInstance() );
      Assertions.assertEquals( JSONNull.getInstance(), array.get( 0 ) );
   }

   public void testElement_index_0_JSONFunction() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      JSONFunction f = new JSONFunction( "return this;" );
      array.element( 0, f );
      Assertions.assertEquals( f, (JSONFunction) array.get( 0 ) );
   }

   public void testElement_index_0_JSONString() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      ArrayJSONStringBean bean = new ArrayJSONStringBean();
      bean.setValue( "'json','json'" );
      array.element( 0, bean );
      Assertions.assertEquals( JSONArray.fromObject( bean ), array.getJSONArray( 0 ) );
   }

   public void testElement_index_0_JSONTokener() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      JSONTokener tok = new JSONTokener( "[0,2]" );
      array.element( 0, tok );
      tok.reset();
      Assertions.assertEquals( JSONArray.fromObject( tok ), array.getJSONArray( 0 ) );
   }

   public void testElement_index_0_Number() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      array.element( 0, new Double( 2 ) );
      Assertions.assertEquals( new Double( 2 ).doubleValue(), array.getDouble( 0 ), 0d );
   }

   public void testElement_index_0_Object() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      array.element( 0, new BeanA() );
      Assertions.assertEquals( JSONObject.fromObject( new BeanA() ), array.getJSONObject( 0 ) );
   }

   public void testElement_index_0_String() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      array.element( 0, "json" );
      Assertions.assertEquals( "json", array.getString( 0 ) );
   }

   public void testElement_index_0_String_JSON() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      array.element( 0, "[]" );
      Assertions.assertEquals( new JSONArray().toString(), array.getString( 0 ) );
   }

   public void testElement_index_0_String_null() {
      JSONArray array = JSONArray.fromObject( "[null,null]" );
      array.element( 0, (String) null );
      Assertions.assertEquals( "", array.getString( 0 ) );
   }

   public void testElement_index_1_Array() {
      JSONArray array = new JSONArray();
      int[] ints = { 1, 2 };
      array.element( 1, ints );
      Assertions.assertEquals( JSONArray.fromObject( ints ), array.getJSONArray( 1 ) );
   }

   public void testElement_index_1_boolean() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1, true );
      assertEquals( 2, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      assertTrue( jsonArray.getBoolean( 1 ) );
   }

   public void testElement_index_1_Boolean() {
      JSONArray array = new JSONArray();
      array.element( 1, Boolean.TRUE );
      Assertions.assertTrue( array.getBoolean( 1 ) );
   }

   public void testElement_index_1_Class() {
      JSONArray array = new JSONArray();
      array.element( 1, Object.class );
      Assertions.assertEquals( "java.lang.Object", array.getString( 1 ) );
   }

   public void testElement_index_1_Collection() {
      List l = new ArrayList();
      l.add( Boolean.TRUE );
      l.add( new Integer( 1 ) );
      l.add( "string" );
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1, l );
      assertEquals( 2, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      Assertions.assertEquals( JSONArray.fromObject( "[true,1,\"string\"]" ),
            jsonArray.getJSONArray( 1 ) );
   }

   public void testElement_index_1_double() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1, 2.0d );
      assertEquals( 2, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      assertEquals( 2.0d, jsonArray.getDouble( 1 ), 0d );
   }

   public void testElement_index_1_int() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1, 1 );
      assertEquals( 2, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      assertEquals( 1, jsonArray.getInt( 1 ) );
   }

   public void testElement_index_1_JSON() {
      JSONArray array = new JSONArray();
      array.element( 1, JSONNull.getInstance() );
      Assertions.assertEquals( JSONNull.getInstance(), array.get( 1 ) );
   }

   public void testElement_index_1_JSONFunction() {
      JSONArray array = new JSONArray();
      JSONFunction f = new JSONFunction( "return this;" );
      array.element( 1, f );
      Assertions.assertEquals( f, (JSONFunction) array.get( 1 ) );
   }

   public void testElement_index_1_JSONString() {
      JSONArray array = new JSONArray();
      ArrayJSONStringBean bean = new ArrayJSONStringBean();
      bean.setValue( "'json','json'" );
      array.element( 1, bean );
      Assertions.assertEquals( JSONArray.fromObject( bean ), array.getJSONArray( 1 ) );
   }

   public void testElement_index_1_JSONTokener() {
      JSONArray array = new JSONArray();
      JSONTokener tok = new JSONTokener( "[1,2]" );
      array.element( 1, tok );
      tok.reset();
      Assertions.assertEquals( JSONArray.fromObject( tok ), array.getJSONArray( 1 ) );
   }

   public void testElement_index_1_long() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1, 1L );
      assertEquals( 2, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      assertEquals( 1L, jsonArray.getLong( 1 ) );
   }

   public void testElement_index_1_Map() {
      Map map = new HashMap();
      map.put( "name", "json" );
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1, map );
      assertEquals( 2, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      Assertions.assertEquals( JSONObject.fromObject( map ), jsonArray.getJSONObject( 1 ) );
   }

   public void testElement_index_1_Number() {
      JSONArray array = new JSONArray();
      array.element( 1, new Double( 2 ) );
      Assertions.assertEquals( new Double( 2 ).doubleValue(), array.getDouble( 1 ), 1d );
   }

   public void testElement_index_1_Object() {
      JSONArray array = new JSONArray();
      array.element( 1, new BeanA() );
      Assertions.assertEquals( JSONObject.fromObject( new BeanA() ), array.getJSONObject( 1 ) );
   }

   public void testElement_index_1_String() {
      JSONArray array = new JSONArray();
      array.element( 1, "json" );
      Assertions.assertEquals( "json", array.getString( 1 ) );
   }

   public void testElement_index_1_String_JSON() {
      JSONArray array = new JSONArray();
      array.element( 1, "[]" );
      Assertions.assertEquals( new JSONArray().toString(), array.getString( 1 ) );
   }

   public void testElement_index_1_String_null() {
      JSONArray array = new JSONArray();
      array.element( 1, (String) null );
      Assertions.assertEquals( "", array.getString( 1 ) );
   }

   public void testElement_int() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1 );
      assertEquals( 1, jsonArray.size() );
      assertEquals( 1, jsonArray.getInt( 0 ) );
   }

   public void testElement_JSON() {
      JSONArray array = new JSONArray();
      array.element( JSONNull.getInstance() );
      Assertions.assertEquals( JSONNull.getInstance(), array.get( 0 ) );
   }

   public void testElement_JSONFunction() {
      JSONArray array = new JSONArray();
      JSONFunction f = new JSONFunction( "return this;" );
      array.element( f );
      Assertions.assertEquals( f, (JSONFunction) array.get( 0 ) );
   }

   public void testElement_JSONString() {
      JSONArray array = new JSONArray();
      ArrayJSONStringBean bean = new ArrayJSONStringBean();
      bean.setValue( "'json','json'" );
      array.element( bean );
      Assertions.assertEquals( JSONArray.fromObject( bean ), array.getJSONArray( 0 ) );
   }

   public void testElement_JSONTokener() {
      JSONArray array = new JSONArray();
      JSONTokener tok = new JSONTokener( "[1,2]" );
      array.element( tok );
      tok.reset();
      Assertions.assertEquals( JSONArray.fromObject( tok ), array.getJSONArray( 0 ) );
   }

   public void testElement_long() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1L );
      assertEquals( 1, jsonArray.size() );
      assertEquals( 1L, jsonArray.getLong( 0 ) );
   }

   public void testElement_Map() {
      Map map = new HashMap();
      map.put( "name", "json" );
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( map );
      assertEquals( 1, jsonArray.size() );
      Assertions.assertEquals( JSONObject.fromObject( map ), jsonArray.getJSONObject( 0 ) );
   }

   public void testElement_negativeIndex() {
      try{
         JSONArray jsonArray = new JSONArray();
         jsonArray.element( -1, JSONNull.getInstance() );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testElement_Number() {
      JSONArray array = new JSONArray();
      array.element( new Double( 2 ) );
      Assertions.assertEquals( new Double( 2 ).doubleValue(), array.getDouble( 0 ), 0d );
   }

   public void testElement_Object() {
      JSONArray array = new JSONArray();
      array.element( new BeanA() );
      Assertions.assertEquals( JSONObject.fromObject( new BeanA() ), array.getJSONObject( 0 ) );
   }

   public void testElement_replace() {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( true );
      assertEquals( 1, jsonArray.size() );
      assertTrue( jsonArray.getBoolean( 0 ) );
      jsonArray.element( 0, false );
      assertEquals( 1, jsonArray.size() );
      assertFalse( jsonArray.getBoolean( 0 ) );
   }

   public void testElement_String() {
      JSONArray array = new JSONArray();
      array.element( "json" );
      Assertions.assertEquals( "json", array.getString( 0 ) );
   }

   public void testElement_String_JSON() {
      JSONArray array = new JSONArray();
      array.element( "[]" );
      Assertions.assertEquals( new JSONArray().toString(), array.getString( 0 ) );
   }

   public void testElement_String_null() {
      JSONArray array = new JSONArray();
      array.element( (String) null );
      Assertions.assertEquals( "", array.getString( 0 ) );
   }

   public void testFromObject_BigDecimal() {
      JSONArray actual = JSONArray.fromObject( new BigDecimal( "12345678901234567890.1234567890" ) );
      assertTrue( actual.get( 0 ) instanceof BigDecimal );
   }

   public void testFromObject_BigInteger() {
      JSONArray actual = JSONArray.fromObject( new BigInteger( "123456789012345678901234567890" ) );
      assertTrue( actual.get( 0 ) instanceof BigInteger );
   }

   public void testFromObject_Boolean() {
      JSONArray expected = JSONArray.fromObject( "[true]" );
      JSONArray actual = JSONArray.fromObject( Boolean.TRUE );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_Byte() {
      JSONArray expected = JSONArray.fromObject( "[1]" );
      JSONArray actual = JSONArray.fromObject( new Byte( (byte) 1 ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_Double() {
      JSONArray expected = JSONArray.fromObject( "[1.0]" );
      JSONArray actual = JSONArray.fromObject( new Double( 1d ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_Float() {
      JSONArray expected = JSONArray.fromObject( "[1.0]" );
      JSONArray actual = JSONArray.fromObject( new Float( 1f ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_Integer() {
      JSONArray expected = JSONArray.fromObject( "[1]" );
      JSONArray actual = JSONArray.fromObject( new Integer( 1 ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_JSONArray() {
      JSONArray expected = JSONArray.fromObject( "[1,2]" );
      JSONArray actual = JSONArray.fromObject( JSONArray.fromObject( "[1,2]" ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_JSONFunction() {
      JSONArray expected = JSONArray.fromObject( "[function(a){ return a; }]" );
      JSONArray actual = JSONArray.fromObject( new JSONFunction( new String[] { "a" }, "return a;" ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_JSONString() {
      ArrayJSONStringBean bean = new ArrayJSONStringBean();
      bean.setValue( "'json','json'" );
      JSONArray actual = JSONArray.fromObject( bean );
      JSONArray expected = JSONArray.fromObject( "['json','json']" );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_Long() {
      JSONArray expected = JSONArray.fromObject( "[1]" );
      JSONArray actual = JSONArray.fromObject( new Long( 1L ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_Map() {
      JSONArray expected = JSONArray.fromObject( "[{}]" );
      JSONArray actual = JSONArray.fromObject( new HashMap() );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_Short() {
      JSONArray expected = JSONArray.fromObject( "[1]" );
      JSONArray actual = JSONArray.fromObject( new Short( (short) 1 ) );
      Assertions.assertEquals( expected, actual );
   }

   public void testGet_exception() {
      try{
         JSONArray jsonArray = JSONArray.fromObject( "[]" );
         jsonArray.get( 0 );
         fail( "Expected a IndexOutOfBoundsException" );
      }catch( IndexOutOfBoundsException expected ){
         // OK
      }
   }

   public void testGetBoolean_exception() {
      try{
         JSONArray jsonArray = JSONArray.fromObject( "[[]]" );
         jsonArray.getBoolean( 0 );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testGetBoolean_false() {
      JSONArray jsonArray = JSONArray.fromObject( "[false]" );
      assertFalse( jsonArray.getBoolean( 0 ) );
   }

   public void testGetBoolean_true() {
      JSONArray jsonArray = JSONArray.fromObject( "[true]" );
      assertTrue( jsonArray.getBoolean( 0 ) );
   }

   public void testGetDimensions_empty_array() {
      int[] dims = JSONArray.getDimensions( new JSONArray() );
      assertEquals( 1, dims.length );
      assertEquals( 0, dims[0] );
   }

   public void testGetDimensions_null_array() {
      int[] dims = JSONArray.getDimensions( null );
      assertEquals( 1, dims.length );
      assertEquals( 0, dims[0] );
   }

   public void testGetDimensions_one_dimension() {
      int[] dims = JSONArray.getDimensions( JSONArray.fromObject( "[1,2,3]" ) );
      assertEquals( 1, dims.length );
      assertEquals( 3, dims[0] );
   }

   public void testGetDimensions_pyramid() {
      int[] dims = JSONArray.getDimensions( JSONArray.fromObject( "[1,[2,[3,[4]]]]" ) );
      assertEquals( 4, dims.length );
      assertEquals( 2, dims[0] );
      assertEquals( 2, dims[1] );
      assertEquals( 2, dims[2] );
      assertEquals( 1, dims[3] );

      dims = JSONArray.getDimensions( JSONArray.fromObject( "[[[[1],2],3],4]" ) );
      assertEquals( 4, dims.length );
      assertEquals( 2, dims[0] );
      assertEquals( 2, dims[1] );
      assertEquals( 2, dims[2] );
      assertEquals( 1, dims[3] );
   }

   public void testGetDimensions_two_dimension() {
      int[] dims = JSONArray.getDimensions( JSONArray.fromObject( "[[1,2,3],[4,5,6]]" ) );
      assertEquals( 2, dims.length );
      assertEquals( 2, dims[0] );
      assertEquals( 3, dims[1] );

      dims = JSONArray.getDimensions( JSONArray.fromObject( "[[1,2],[4,5,6]]" ) );
      assertEquals( 2, dims.length );
      assertEquals( 2, dims[0] );
      assertEquals( 3, dims[1] );

      dims = JSONArray.getDimensions( JSONArray.fromObject( "[[1,2,3],[4,5]]" ) );
      assertEquals( 2, dims.length );
      assertEquals( 2, dims[0] );
      assertEquals( 3, dims[1] );
   }

   public void testGetDouble_exception() {
      try{
         JSONArray jsonArray = JSONArray.fromObject( "[[]]" );
         jsonArray.getDouble( 0 );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testGetDouble_Number() {
      JSONArray jsonArray = JSONArray.fromObject( "[2.0]" );
      assertEquals( 2.0d, jsonArray.getDouble( 0 ), 0d );
   }

   public void testGetDouble_String() {
      JSONArray jsonArray = JSONArray.fromObject( "[\"2.0\"]" );
      assertEquals( 2.0d, jsonArray.getDouble( 0 ), 0d );
   }

   public void testGetInt_exception() {
      try{
         JSONArray jsonArray = JSONArray.fromObject( "[[]]" );
         jsonArray.getInt( 0 );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testGetInt_Number() {
      JSONArray jsonArray = JSONArray.fromObject( "[2.0]" );
      assertEquals( 2, jsonArray.getInt( 0 ) );
   }

   public void testGetInt_String() {
      JSONArray jsonArray = JSONArray.fromObject( "[\"2.0\"]" );
      assertEquals( 2, jsonArray.getInt( 0 ) );
   }

   public void testGetJSONArray() {
      JSONArray jsonArray = JSONArray.fromObject( "[[1,2]]" );
      Assertions.assertEquals( JSONArray.fromObject( "[1,2]" )
            .toString(), jsonArray.getJSONArray( 0 )
            .toString() );
   }

   public void testGetJSONArray_exception() {
      try{
         JSONArray jsonArray = JSONArray.fromObject( "[1]" );
         jsonArray.getJSONArray( 0 );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testGetJSONObject() {
      JSONArray jsonArray = JSONArray.fromObject( "[{\"name\":\"json\"}]" );
      Assertions.assertEquals( JSONObject.fromObject( "{\"name\":\"json\"}" ),
            jsonArray.getJSONObject( 0 ) );
   }

   public void testGetJSONObject_exception() {
      try{
         JSONArray jsonArray = JSONArray.fromObject( "[1]" );
         jsonArray.getJSONObject( 0 );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testGetLong_exception() {
      try{
         JSONArray jsonArray = JSONArray.fromObject( "[[]]" );
         jsonArray.getLong( 0 );
         fail( "Expected a JSONException" );
      }catch( JSONException expected ){
         // OK
      }
   }

   public void testGetLong_Number() {
      JSONArray jsonArray = JSONArray.fromObject( "[2.0]" );
      assertEquals( 2, jsonArray.getLong( 0 ) );
   }

   public void testGetLong_String() {
      JSONArray jsonArray = JSONArray.fromObject( "[\"2.0\"]" );
      assertEquals( 2, jsonArray.getLong( 0 ) );
   }

   public void testGetString() {
      JSONArray jsonArray = JSONArray.fromObject( "[\"2.0\"]" );
      assertEquals( "2.0", jsonArray.getString( 0 ) );
   }

   public void testGetString_exception() {
      try{
         JSONArray jsonArray = JSONArray.fromObject( "[]" );
         jsonArray.getString( 0 );
         fail( "Expected a IndexOutOfBoundsException" );
      }catch( IndexOutOfBoundsException expected ){
         // OK
      }
   }

   public void testOptionalGet() {
      Object[] params = new Object[] { new JSONArray(), JSONObject.fromObject( "{\"int\":1}" ) };
      JSONArray jsonArray = JSONArray.fromObject( params );
      assertFalse( jsonArray.optBoolean( 0 ) );
      assertTrue( jsonArray.optBoolean( 0, true ) );
      assertTrue( Double.isNaN( jsonArray.optDouble( 0 ) ) );
      assertEquals( 0d, jsonArray.optDouble( 0, 0d ), 0d );
      assertEquals( 0, jsonArray.optInt( 0 ) );
      assertEquals( 1, jsonArray.optInt( 0, 1 ) );
      assertEquals( null, jsonArray.optJSONArray( 1 ) );
      Assertions.assertEquals( (JSONArray) params[0], jsonArray.optJSONArray( 0 ) );
      assertEquals( null, jsonArray.optJSONObject( 0 ) );
      Assertions.assertEquals( (JSONObject) params[1], jsonArray.optJSONObject( 1 ) );
      assertEquals( 0, jsonArray.optLong( 0 ) );
      assertEquals( 1, jsonArray.optLong( 0, 1 ) );
      assertEquals( "", jsonArray.optString( 3 ) );
      assertEquals( "json", jsonArray.optString( 3, "json" ) );
   }

   public void testToArray_bean_element() {
      BeanA[] expected = new BeanA[] { new BeanA() };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray, BeanA.class );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_BigDecimal() {
      BigDecimal[] expected = new BigDecimal[] { MorphUtils.BIGDECIMAL_ZERO,
            MorphUtils.BIGDECIMAL_ONE };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_BigInteger() {
      BigInteger[] expected = new BigInteger[] { BigInteger.ZERO, BigInteger.ONE };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_boolean() {
      boolean[] expected = new boolean[] { true, false };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Boolean() {
      Boolean[] expected = new Boolean[] { Boolean.TRUE, Boolean.FALSE };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_boolean_multi() {
      boolean[][] expected = new boolean[][] { { true, false }, { false, true } };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_byte() {
      byte[] input = new byte[] { 1, 2, 3, 4, 5, 6 };
      int[] expected = new int[] { 1, 2, 3, 4, 5, 6 };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Byte() {
      Integer[] expected = new Integer[] { new Integer( 1 ), new Integer( 2 ) };
      Byte[] bytes = new Byte[] { new Byte( (byte) 1 ), new Byte( (byte) 2 ) };
      JSONArray jsonArray = JSONArray.fromObject( bytes );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_byte_multi() {
      byte[][] input = new byte[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      int[][] expected = new int[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_char() {
      String[] expected = new String[] { "A", "B" };
      char[] input = new char[] { 'A', 'B' };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_char_multi() {
      String[][] expected = new String[][] { { "a", "b" }, { "c", "d" } };
      char[][] input = new char[][] { { 'a', 'b' }, { 'c', 'd' } };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Character() {
      String[] expected = { "A", "B" };
      Character[] chars = new Character[] { new Character( 'A' ), new Character( 'B' ) };
      JSONArray jsonArray = JSONArray.fromObject( chars );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_double() {
      double[] expected = new double[] { 1, 2, 3, 4, 5, 6 };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Double() {
      Double[] expected = new Double[] { new Double( 1d ), new Double( 2d ) };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_double_multi() {
      double[][] expected = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_dynabean_element() throws Exception {
      DynaBean[] expected = new DynaBean[] { createDynaBean() };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_float() {
      double[] expected = new double[] { 1, 2, 3, 4, 5, 6 };
      float[] input = new float[] { 1, 2, 3, 4, 5, 6 };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Float() {
      Double[] expected = new Double[] { new Double( 1d ), new Double( 2d ) };
      Float[] floats = new Float[] { new Float( 1f ), new Float( 2f ) };
      JSONArray jsonArray = JSONArray.fromObject( floats );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_float_multi() {
      double[][] expected = new double[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      float[][] input = new float[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_int() {
      int[] expected = new int[] { 1, 2, 3, 4, 5, 6 };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_int_multi() {
      int[][] expected = new int[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Integer() {
      Integer[] expected = new Integer[] { new Integer( 1 ), new Integer( 2 ) };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_long() {
      long[] input = new long[] { 1, 2, 3, 4, 5, 6 };
      int[] expected = new int[] { 1, 2, 3, 4, 5, 6 };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Long() {
      Integer[] expected = new Integer[] { new Integer( 1 ), new Integer( 2 ) };
      Long[] longs = new Long[] { new Long( 1L ), new Long( 2L ) };
      JSONArray jsonArray = JSONArray.fromObject( longs );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_long_multi() {
      long[][] input = new long[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      int[][] expected = new int[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Long2() {
      Long[] expected = new Long[] { new Long( Integer.MAX_VALUE + 1L ),
            new Long( Integer.MAX_VALUE + 2L ) };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_null_elements() {
      String[] expected = new String[] { null, null, null };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_short() {
      short[] input = new short[] { 1, 2, 3, 4, 5, 6 };
      int[] expected = new int[] { 1, 2, 3, 4, 5, 6 };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_Short() {
      Integer[] expected = new Integer[] { new Integer( 1 ), new Integer( 2 ) };
      Short[] shorts = new Short[] { new Short( (short) 1 ), new Short( (short) 2 ) };
      JSONArray jsonArray = JSONArray.fromObject( shorts );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_short_multi() {
      short[][] input = new short[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      int[][] expected = new int[][] { { 1, 2, 3 }, { 4, 5, 6 } };
      JSONArray jsonArray = JSONArray.fromObject( input );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_String() {
      String[] expected = new String[] { "1", "2", "3", "4", "5", "6" };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_String_multi() {
      String[][] expected = new String[][] { { "1", "2", "3" }, { "4", "5", "6" } };
      JSONArray jsonArray = JSONArray.fromObject( expected );
      Object actual = JSONArray.toArray( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_StringToInt() {
      int[] expected = new int[] { 1, 2, 3, 4, 5, 6 };
      String[] input = new String[] { "1", "2", "3", "4", "5", "6" };
      JSONArray jsonArray = JSONArray.fromObject( input );
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setRootClass( Integer.TYPE );
      Object actual = JSONArray.toArray( jsonArray, jsonConfig );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_StringToInteger() {
      int[] expected = new int[] { 1, 2, 3, 4, 5, 6 };
      String[] input = new String[] { "1", "2", "3", "4", "5", "6" };
      JSONArray jsonArray = JSONArray.fromObject( input );
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setRootClass( Integer.class );
      Object actual = JSONArray.toArray( jsonArray, jsonConfig );
      Assertions.assertEquals( expected, actual );
   }

   public void testToArray_StringToInteger_empty() {
      int[] expected = new int[] {};
      String[] input = new String[] {};
      JSONArray jsonArray = JSONArray.fromObject( input );
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setRootClass( Integer.class );
      Object actual = JSONArray.toArray( jsonArray, jsonConfig );
      Assertions.assertEquals( expected, actual );
   }

   public void testToJSONObject() {
      JSONArray jsonArray = JSONArray.fromObject( "[\"json\",1,true]" );
      JSONObject expected = JSONObject.fromObject( "{\"string\":\"json\",\"int\":1,\"bool\":true}" );
      JSONArray names = JSONArray.fromObject( "['string','int','bool']" );

      Assertions.assertEquals( expected, jsonArray.toJSONObject( names ) );
   }

   public void testToJSONObject_null_object() {
      JSONArray jsonArray = new JSONArray();
      assertNull( jsonArray.toJSONObject( null ) );
      assertNull( jsonArray.toJSONObject( new JSONArray() ) );
      assertNull( jsonArray.toJSONObject( JSONArray.fromObject( "['json']" ) ) );
   }

   public void testToList_bean_elements() {
      List expected = new ArrayList();
      expected.add( new BeanA() );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray, BeanA.class );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_BigDecimal() {
      List expected = new ArrayList();
      expected.add( MorphUtils.BIGDECIMAL_ZERO );
      expected.add( MorphUtils.BIGDECIMAL_ONE );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_BigInteger() {
      List expected = new ArrayList();
      expected.add( BigInteger.ZERO );
      expected.add( BigInteger.ONE );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Boolean() {
      List expected = new ArrayList();
      expected.add( Boolean.TRUE );
      expected.add( Boolean.FALSE );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Byte() {
      List expected = new ArrayList();
      expected.add( new Integer( 1 ) );
      expected.add( new Integer( 2 ) );
      List bytes = new ArrayList();
      bytes.add( new Byte( (byte) 1 ) );
      bytes.add( new Byte( (byte) 2 ) );
      JSONArray jsonArray = JSONArray.fromObject( bytes );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Character() {
      List expected = new ArrayList();
      expected.add( "A" );
      expected.add( "B" );
      List chars = new ArrayList();
      chars.add( new Character( 'A' ) );
      chars.add( new Character( 'B' ) );
      JSONArray jsonArray = JSONArray.fromObject( chars );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Double() {
      List expected = new ArrayList();
      expected.add( new Double( 1d ) );
      expected.add( new Double( 2d ) );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_dynaBean_elements() throws Exception {
      List expected = new ArrayList();
      expected.add( createDynaBean() );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Float() {
      List expected = new ArrayList();
      expected.add( new Double( 1d ) );
      expected.add( new Double( 2d ) );
      List floats = new ArrayList();
      floats.add( new Float( 1f ) );
      floats.add( new Float( 2f ) );
      JSONArray jsonArray = JSONArray.fromObject( floats );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Integer() {
      List expected = new ArrayList();
      expected.add( new Integer( 1 ) );
      expected.add( new Integer( 2 ) );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_JSONFunction_elements() {
      List expected = new ArrayList();
      expected.add( new JSONFunction( new String[] { "a" }, "return a;" ) );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_JSONFunction_elements_2() {
      List expected = new ArrayList();
      expected.add( "function(a){ return a; }" );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Long() {
      List expected = new ArrayList();
      expected.add( new Integer( 1 ) );
      expected.add( new Integer( 2 ) );
      List longs = new ArrayList();
      longs.add( new Long( 1L ) );
      longs.add( new Long( 2L ) );
      JSONArray jsonArray = JSONArray.fromObject( longs );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Long2() {
      List expected = new ArrayList();
      expected.add( new Long( Integer.MAX_VALUE + 1L ) );
      expected.add( new Long( Integer.MAX_VALUE + 2L ) );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_null_elements() {
      List expected = new ArrayList();
      expected.add( null );
      expected.add( null );
      expected.add( null );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_Short() {
      List expected = new ArrayList();
      expected.add( new Integer( 1 ) );
      expected.add( new Integer( 2 ) );
      List shorts = new ArrayList();
      shorts.add( new Short( (short) 1 ) );
      shorts.add( new Short( (short) 2 ) );
      JSONArray jsonArray = JSONArray.fromObject( shorts );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_String() {
      List expected = new ArrayList();
      expected.add( "A" );
      expected.add( "B" );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testToList_String_multi() {
      List a = new ArrayList();
      a.add( "a" );
      a.add( "b" );
      List b = new ArrayList();
      b.add( "1" );
      b.add( "2" );
      List expected = new ArrayList();
      expected.add( a );
      expected.add( b );
      JSONArray jsonArray = JSONArray.fromObject( expected );
      List actual = JSONArray.toList( jsonArray );
      Assertions.assertEquals( expected, actual );
   }

   public void testWrite() throws IOException {
      JSONArray jsonArray = JSONArray.fromObject( "[[],{},1,true,\"json\"]" );
      StringWriter sw = new StringWriter();
      jsonArray.write( sw );
      assertEquals( "[[],{},1,true,\"json\"]", sw.toString() );
   }

   private MorphDynaBean createDynaBean() throws Exception {
      Map properties = new HashMap();
      properties.put( "name", String.class );
      MorphDynaClass dynaClass = new MorphDynaClass( properties );
      MorphDynaBean dynaBean = (MorphDynaBean) dynaClass.newInstance();
      dynaBean.setDynaBeanClass( dynaClass );
      dynaBean.set( "name", "json" );
      // JSON Strings can not be null, only empty
      return dynaBean;
   }

   private void testJSONArray( Object array, String expected ) {
      try{
         JSONArray jsonArray = JSONArray.fromObject( array );
         assertEquals( expected, jsonArray.toString() );
      }catch( JSONException jsone ){
         fail( jsone.getMessage() );
      }
   }
}