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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.json.sample.AnnotationBean;
import net.sf.json.sample.JsonEnum;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONArrayJdk15 extends TestCase
{
   public static void main( String[] args )
   {
      junit.textui.TestRunner.run( TestJSONArrayJdk15.class );
   }

   public TestJSONArrayJdk15( String testName )
   {
      super( testName );
   }

   public void testConstructor_Annotation()
   {
      AnnotationBean bean = new AnnotationBean();
      Annotation[] annotations = bean.getClass()
            .getAnnotations();
      try{
         JSONArray.fromObject( annotations[0] );
         fail( "Expected a JSONException" );
      }
      catch( JSONException expected ){
         // ok
      }
   }

   public void testConstructor_Collection()
   {
      List l = new ArrayList();
      l.add( Boolean.TRUE );
      l.add( new Integer( 1 ) );
      l.add( "string" );
      l.add( Object.class );
      l.add( JsonEnum.ARRAY );
      testJSONArray( l, "[true,1,\"string\",\"java.lang.Object\",\"ARRAY\"]" );
   }

   public void testConstructor_Enum()
   {
      testJSONArray( JsonEnum.ARRAY, "[\"ARRAY\"]" );
   }

   public void testConstructor_Object_Array_Enum()
   {
      JSONArray expected = JSONArray.fromObject( "[\"ARRAY\",\"OBJECT\"]" );
      JSONArray actual = JSONArray.fromObject( new JsonEnum[] { JsonEnum.ARRAY, JsonEnum.OBJECT } );
      Assertions.assertEquals( expected, actual );
   }

   public void testFromObject_Enum()
   {
      JSONArray actual = JSONArray.fromObject( JsonEnum.ARRAY );
      JSONArray expected = new JSONArray().element( "ARRAY" );
      Assertions.assertEquals( expected, actual );
   }

   public void testPut_Enum()
   {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( JsonEnum.ARRAY );
      assertEquals( 1, jsonArray.size() );
      assertEquals( "ARRAY", jsonArray.get( 0 ) );
   }

   public void testPut_Enum_2()
   {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( (JsonEnum) null );
      assertEquals( 1, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
   }

   public void testPut_index_0_Annotation()
   {
      AnnotationBean bean = new AnnotationBean();
      Annotation[] annotations = bean.getClass()
            .getAnnotations();
      try{
         JSONArray array = JSONArray.fromObject( "[null,null]" );
         array.element( 0, annotations[0] );
         fail( "Expected a JSONException" );
      }
      catch( JSONException expected ){
         // ok
      }
   }

   public void testPut_index_0_Enum()
   {
      JSONArray jsonArray = JSONArray.fromObject( "[null,null]" );
      jsonArray.element( 0, JsonEnum.ARRAY );
      assertEquals( "ARRAY", jsonArray.get( 0 ) );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 1 ) );
   }

   public void testPut_index_0_Enum_2()
   {
      JSONArray jsonArray = JSONArray.fromObject( "[null,null]" );
      jsonArray.element( 0, (JsonEnum) null );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 1 ) );
   }

   public void testPut_index_1_Enum()
   {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1, JsonEnum.ARRAY );
      assertEquals( 2, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      assertEquals( "ARRAY", jsonArray.get( 1 ) );
   }

   public void testPut_index_1_Enum_2()
   {
      JSONArray jsonArray = new JSONArray();
      jsonArray.element( 1, (JsonEnum) null );
      assertEquals( 2, jsonArray.size() );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 0 ) );
      assertEquals( JSONNull.getInstance(), jsonArray.get( 1 ) );
   }

   private void testJSONArray( Object array, String expected )
   {
      try{
         JSONArray jsonArray = JSONArray.fromObject( array );
         assertEquals( expected, jsonArray.toString() );
      }
      catch( JSONException jsone ){
         fail( jsone.getMessage() );
      }
   }
}