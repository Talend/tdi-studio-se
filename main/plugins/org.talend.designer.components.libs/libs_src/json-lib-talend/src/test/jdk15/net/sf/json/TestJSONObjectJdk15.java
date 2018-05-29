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
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.ezmorph.Morpher;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.ezmorph.bean.MorphDynaClass;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.sample.AnnotationBean;
import net.sf.json.sample.AnnotatedBean;
import net.sf.json.sample.EnumBean;
import net.sf.json.sample.JsonEnum;
import net.sf.json.sample.JsonAnnotation;
import net.sf.json.util.EnumMorpher;
import net.sf.json.util.JSONUtils;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONObjectJdk15 extends TestCase
{
   public static void main( String[] args )
   {
      junit.textui.TestRunner.run( TestJSONObjectJdk15.class );
   }

   public TestJSONObjectJdk15( String testName )
   {
      super( testName );
   }

   public void testFromBean_AnnotationBean()
   {
      AnnotationBean bean = new AnnotationBean();
      Annotation[] annotations = bean.getClass()
            .getAnnotations();
      try{
         JSONObject.fromObject( annotations[0] );
         fail( "Expected a JSONException" );
      }
      catch( JSONException expected ){
         // ok
      }
   }

   public void testFromBean_Enum()
   {
      try{
         JSONObject.fromObject( JsonEnum.OBJECT );
         fail( "Expected a JSONException" );
      }
      catch( JSONException expected ){
         // ok
      }
   }

   public void testFromBean_EnumBean()
   {
      EnumBean bean = new EnumBean();
      bean.setJsonEnum( JsonEnum.OBJECT );
      bean.setString( "string" );
      JSONObject json = JSONObject.fromObject( bean );
      assertNotNull( json );
      assertEquals( JsonEnum.OBJECT.toString(), json.get( "jsonEnum" ) );
      assertEquals( "string", json.get( "string" ) );
   }

   public void testFromObject_AnnotationBean()
   {
      AnnotationBean bean = new AnnotationBean();
      Annotation[] annotations = bean.getClass()
            .getAnnotations();
      try{
         JSONObject.fromObject( annotations[0] );
         fail( "Expected a JSONException" );
      }
      catch( JSONException expected ){
         // ok
      }
   }

   public void testFromObject_DynaBean__Enum() throws Exception
   {
      Map properties = new HashMap();
      properties.put( "jsonEnum", JsonEnum.class );
      MorphDynaClass dynaClass = new MorphDynaClass( properties );
      MorphDynaBean dynaBean = (MorphDynaBean) dynaClass.newInstance();
      dynaBean.setDynaBeanClass( dynaClass );
      dynaBean.set( "jsonEnum", JsonEnum.OBJECT );
      JSONObject json = JSONObject.fromObject( dynaBean );
      assertNotNull( json );
      assertEquals( JsonEnum.OBJECT.toString(), json.get( "jsonEnum" ) );
   }

   public void testFromObject_Enum()
   {
      try{
         JSONObject.fromObject( JsonEnum.OBJECT );
         fail( "Expected a JSONException" );
      }
      catch( JSONException expected ){
         // ok
      }
   }

   public void testFromObject_EnumBean()
   {
      EnumBean bean = new EnumBean();
      bean.setJsonEnum( JsonEnum.OBJECT );
      bean.setString( "string" );
      JSONObject json = JSONObject.fromObject( bean );
      assertNotNull( json );
      assertEquals( JsonEnum.OBJECT.toString(), json.get( "jsonEnum" ) );
      assertEquals( "string", json.get( "string" ) );
   }

   public void testFromObject_Map__Enum()
   {
      Map properties = new HashMap();
      properties.put( "jsonEnum", JsonEnum.OBJECT );
      JSONObject json = JSONObject.fromObject( properties );
      assertNotNull( json );
      assertEquals( JsonEnum.OBJECT.toString(), json.get( "jsonEnum" ) );
   }

   public void testPut_Annotation()
   {
      AnnotationBean bean = new AnnotationBean();
      Annotation[] annotations = bean.getClass()
            .getAnnotations();
      try{
         JSONObject jsonObject = new JSONObject();
         jsonObject.element( "annotation", annotations[0] );
         fail( "Expected a JSONException" );
      }
      catch( JSONException expected ){
         // ok
      }
   }

   public void testPut_Enum()
   {
      JSONObject json = new JSONObject();
      json.element( "jsonEnum", JsonEnum.OBJECT );
      assertEquals( JsonEnum.OBJECT.toString(), json.get( "jsonEnum" ) );
   }

   public void testToBean_EnumBean()
   {
      JSONUtils.getMorpherRegistry()
            .registerMorpher( new EnumMorpher( JsonEnum.class ) );
      JSONObject json = new JSONObject();
      json.element( "jsonEnum", "OBJECT" );
      EnumBean bean = (EnumBean) JSONObject.toBean( json, EnumBean.class );
      assertNotNull( bean );
      assertEquals( bean.getJsonEnum(), JsonEnum.OBJECT );
   }

   /*
   public void testToBean_EnumBean2()
   {
      JSONUtils.getMorpherRegistry()
            .registerMorpher( new EnumMorpher( JsonEnum.class ) );
      EnumBean bean = new EnumBean();
      bean.getEnums().add(JsonEnum.ARRAY);
      bean.getEnums().add(JsonEnum.OBJECT);
      JSONObject json = JSONObject.fromObject(bean);
      System.err.println(json);
      EnumBean bean2 = (EnumBean) JSONObject.toBean( json, EnumBean.class );
      assertNotNull( bean2 );
      System.err.println(bean.getEnums().toString());
      System.err.println(bean2.getEnums().toString());
      Map classMap = new HashMap();
      classMap.put("enums", JsonEnum.class);
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setRootClass( EnumBean.class );
      jsonConfig.setClassMap(classMap);
      EnumBean bean3 = (EnumBean) JSONObject.toBean( JSONObject.fromObject(json.toString()), jsonConfig );
      assertNotNull( bean3 );
      System.err.println(bean3.getEnums().toString());
      for(java.util.Iterator i= bean3.getEnums().iterator(); i.hasNext();) System.err.println(i.next().getClass());
   }
   */

   public void testToBean_EnumBean_autoRegisterMorpher()
   {
      JSONObject json = new JSONObject();
      json.element( "jsonEnum", "OBJECT" );
      EnumBean bean = (EnumBean) JSONObject.toBean( json, EnumBean.class );
      assertNotNull( bean );
      assertEquals( bean.getJsonEnum(), JsonEnum.OBJECT );
   }

   public void testFromObject_ignoreAnnotations()
   {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.addIgnoreFieldAnnotation( JsonAnnotation.class );
      AnnotatedBean bean = new AnnotatedBean();
      bean.setString1("STRING_1");
      bean.setString2("STRING_2");
      bean.setString3("STRING_3");
      JSONObject json = JSONObject.fromObject(bean, jsonConfig);
      assertNotNull(json);
      assertEquals("STRING_1", json.get("string1"));
      assertEquals("STRING_2", json.get("string2"));
      assertFalse(json.has( "string3" ));
      
      jsonConfig.setIgnoreTransientFields( true );
      json = JSONObject.fromObject(bean, jsonConfig);
      assertNotNull(json);
      assertEquals("STRING_1", json.get("string1"));
      assertFalse(json.has( "string2" ));
      assertFalse(json.has( "string3" ));
   }
   
   protected void setUp() throws Exception {
      Morpher morpher = JSONUtils.getMorpherRegistry().getMorpherFor( JsonEnum.class );
      JSONUtils.getMorpherRegistry().deregisterMorpher( morpher );
   }
}