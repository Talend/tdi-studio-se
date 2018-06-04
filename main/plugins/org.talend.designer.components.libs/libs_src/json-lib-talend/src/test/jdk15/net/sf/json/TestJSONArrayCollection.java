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

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import junit.framework.TestCase;
import net.sf.json.sample.GenericsBean;
import net.sf.json.sample.GenericsBean.GenericsInternalBean;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author Matt Small <msmall@wavemaker.com>
 */
public class TestJSONArrayCollection extends TestCase {

   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONArrayCollection.class );
   }

   public TestJSONArrayCollection( String testName ) {
      super( testName );
   }

   /*
   public void testErrorInList() throws Exception {
      GenericsBean gb = new GenericsBean();
      PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( gb, "stringList" );

      JSONArray ja = JSONArray.fromObject( "[12, \"bar\"]" );
      JsonConfig jc = new JsonConfig();

      try{
         jc.setCollectionType( pd.getPropertyType() );
         jc.setEnclosedType( JSONArray.getCollectionType( pd, false )[0] );
         JSONArray.toCollection( ja, jc );
         fail( "didn't get exception" );
      }catch( JSONException e ){
         // ignore
      }
   }

   public void testGetCollectionType() throws Exception {
      GenericsBean gb = new GenericsBean();
      PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( gb, "stringSet" );
      assertEquals( 1, JSONArray.getCollectionType( pd, false ).length );
      assertEquals( String.class, JSONArray.getCollectionType( pd, false )[0] );
      assertEquals( 1, JSONArray.getCollectionType( pd, true ).length );
      assertEquals( String.class, JSONArray.getCollectionType( pd, true )[0] );

      pd = PropertyUtils.getPropertyDescriptor( gb, "stringList" );
      assertEquals( 1, JSONArray.getCollectionType( pd, false ).length );
      assertEquals( String.class, JSONArray.getCollectionType( pd, false )[0] );
      assertEquals( 1, JSONArray.getCollectionType( pd, true ).length );
      assertEquals( String.class, JSONArray.getCollectionType( pd, true )[0] );
   }

   public void testToInternalBeanList() throws Exception {
      GenericsBean gb = new GenericsBean();
      PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( gb, "genericsInternalBeanList" );

      JSONArray ja = JSONArray.fromObject( "[{\"string\": \"foo\"}, {\"string\": \"bar\"}]" );
      JsonConfig jc = new JsonConfig();
      jc.setCollectionType( pd.getPropertyType() );
      jc.setEnclosedType( JSONArray.getCollectionType( pd, false )[0] );

      Collection<?> c = JSONArray.toCollection( ja, jc);
      assertEquals( 2, c.size() );
      assertTrue( c instanceof ArrayList );

      for( Object o : c ){
         assertTrue( o instanceof GenericsInternalBean );
         GenericsInternalBean gib = (GenericsInternalBean) o;

         if( gib.getString()
               .equals( "foo" ) ){
            // pass
         }else if( gib.getString()
               .equals( "bar" ) ){
            // pass
         }else{
            fail( "unknown gib: " + gib.getString() );
         }
      }
   }

   public void testToStringArrayList() throws Exception {
      GenericsBean gb = new GenericsBean();
      PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( gb, "stringArrayList" );

      JSONArray ja = JSONArray.fromObject( "[\"foo\", \"bar\"]" );
      JsonConfig jc = new JsonConfig();
      jc.setCollectionType( pd.getPropertyType() );
      jc.setEnclosedType( JSONArray.getCollectionType( pd, false )[0] );

      Collection<?> c = JSONArray.toCollection( ja, jc );
      assertEquals( 2, c.size() );
      assertTrue( c instanceof ArrayList );
      assertTrue( c.contains( "foo" ) );
      assertTrue( c.contains( "bar" ) );
   }

   public void testToStringHashSet() throws Exception {
      GenericsBean gb = new GenericsBean();
      PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( gb, "stringHashSet" );

      JSONArray ja = JSONArray.fromObject( "[\"foo\", \"bar\"]" );
      JsonConfig jc = new JsonConfig();
      jc.setCollectionType( pd.getPropertyType() );
      jc.setEnclosedType( JSONArray.getCollectionType( pd, false )[0]  );

      Collection<?> c = JSONArray.toCollection( ja, jc );
      assertEquals( 2, c.size() );
      assertTrue( c instanceof HashSet );
      assertTrue( c.contains( "foo" ) );
      assertTrue( c.contains( "bar" ) );
   }

   public void testToStringList() throws Exception {
      GenericsBean gb = new GenericsBean();
      PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( gb, "stringList" );

      JSONArray ja = JSONArray.fromObject( "[\"foo\", \"bar\"]" );
      JsonConfig jc = new JsonConfig();
      jc.setCollectionType( pd.getPropertyType() );
      jc.setEnclosedType( JSONArray.getCollectionType( pd, false )[0]  );

      Collection<?> c = JSONArray.toCollection( ja, jc );
      assertEquals( 2, c.size() );
      assertTrue( c instanceof ArrayList );
      assertTrue( c.contains( "foo" ) );
      assertTrue( c.contains( "bar" ) );
   }

   public void testToStringSet() throws Exception {
      GenericsBean gb = new GenericsBean();
      PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( gb, "stringSet" );

      JSONArray ja = JSONArray.fromObject( "[\"foo\", \"bar\"]" );
      JsonConfig jc = new JsonConfig();
      jc.setCollectionType( pd.getPropertyType() );
      jc.setEnclosedType( JSONArray.getCollectionType( pd, false )[0] );

      Collection<?> c = JSONArray.toCollection( ja, jc);
      assertEquals( 2, c.size() );
      assertTrue( c instanceof HashSet );
      assertTrue( c.contains( "foo" ) );
      assertTrue( c.contains( "bar" ) );
   }

   public void testToUntypedList() throws Exception {
      GenericsBean gb = new GenericsBean();
      PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( gb, "noTypeList" );

      JSONArray ja = JSONArray.fromObject( "[\"foo\", \"bar\", 12]" );
      JsonConfig jc = new JsonConfig();
      jc.setCollectionType( pd.getPropertyType() );

      Collection<?> c = JSONArray.toCollection( ja, jc );
      assertEquals( 3, c.size() );
      assertTrue( c instanceof ArrayList );
      assertTrue( c.contains( "foo" ) );
      assertTrue( c.contains( "bar" ) );
      assertTrue( c.contains( 12 ) );
   }
   */
   
   public void testNOOP() {}
}
