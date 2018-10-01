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
import java.util.Map;

import junit.framework.TestCase;
import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.ezmorph.bean.MorphDynaClass;
import net.sf.json.sample.BeanA;
import net.sf.json.sample.JsonEventAdpater;
import net.sf.json.sample.PropertyBean;

import org.apache.commons.beanutils.DynaBean;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONObjectEvents extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONObjectEvents.class );
   }

   private JsonConfig jsonConfig;
   private JsonEventAdpater jsonEventAdpater;

   public TestJSONObjectEvents( String name ) {
      super( name );
   }

   public void testFromObject_bean() {
      JSONObject.fromObject( new BeanA(), jsonConfig );
      assertEvents();
   }

   public void testFromObject_bean2() {
      JSONObject.fromObject( new PropertyBean(), jsonConfig );
      assertEquals( 0, jsonEventAdpater.getError() );
      assertEquals( 1, jsonEventAdpater.getWarning() );
      assertEquals( 0, jsonEventAdpater.getArrayStart() );
      assertEquals( 0, jsonEventAdpater.getArrayEnd() );
      assertEquals( 1, jsonEventAdpater.getObjectStart() );
      assertEquals( 1, jsonEventAdpater.getObjectEnd() );
      assertEquals( 0, jsonEventAdpater.getElementAdded() );
      assertEquals( 1, jsonEventAdpater.getPropertySet() );
   }

   public void testFromObject_dynaBean() throws Exception {
      JSONObject.fromObject( createDynaBean(), jsonConfig );
      assertEvents();
   }

   public void testFromObject_error() {
      try{
         JSONObject.fromObject( "[]", jsonConfig );
         fail( "A JSONException was expected" );
      }catch( JSONException expected ){
         assertEquals( 1, jsonEventAdpater.getError() );
         assertEquals( 0, jsonEventAdpater.getWarning() );
         assertEquals( 0, jsonEventAdpater.getArrayStart() );
         assertEquals( 0, jsonEventAdpater.getArrayEnd() );
         assertEquals( 0, jsonEventAdpater.getObjectStart() );
         assertEquals( 0, jsonEventAdpater.getObjectEnd() );
         assertEquals( 0, jsonEventAdpater.getElementAdded() );
         assertEquals( 0, jsonEventAdpater.getPropertySet() );
      }
   }

   public void testFromObject_JSONObject() {
      JSONObject jsonObject = new JSONObject().element( "name", "json" )
            .element( "func", new JSONFunction( "return this;" ) )
            .element( "int", new Integer( 1 ) );
      JSONObject.fromObject( jsonObject, jsonConfig );
      assertEvents();
   }

   public void testFromObject_map() {
      Map map = new HashMap();
      map.put( "name", "json" );
      map.put( "func", new JSONFunction( "return this;" ) );
      map.put( "int", new Integer( 1 ) );
      JSONObject.fromObject( map, jsonConfig );
      assertEvents();
   }

   public void testFromObject_string() {
      JSONObject.fromObject( "{name:'json',int:1,func:function(){ return this; }}", jsonConfig );
      assertEvents();
   }

   protected void setUp() throws Exception {
      jsonEventAdpater = new JsonEventAdpater();
      jsonConfig = new JsonConfig();
      jsonConfig.addJsonEventListener( jsonEventAdpater );
      jsonConfig.enableEventTriggering();

   }

   protected void tearDown() throws Exception {
      jsonEventAdpater.reset();
   }

   private void assertEvents() {
      assertEquals( 0, jsonEventAdpater.getError() );
      assertEquals( 0, jsonEventAdpater.getWarning() );
      assertEquals( 0, jsonEventAdpater.getArrayStart() );
      assertEquals( 0, jsonEventAdpater.getArrayEnd() );
      assertEquals( 1, jsonEventAdpater.getObjectStart() );
      assertEquals( 1, jsonEventAdpater.getObjectEnd() );
      assertEquals( 0, jsonEventAdpater.getElementAdded() );
      assertEquals( 3, jsonEventAdpater.getPropertySet() );
   }

   private DynaBean createDynaBean() throws Exception {
      Map properties = new HashMap();
      properties.put( "name", String.class );
      properties.put( "func", JSONFunction.class );
      properties.put( "int", Integer.class );
      MorphDynaClass dynaClass = new MorphDynaClass( properties );
      MorphDynaBean dynaBean = (MorphDynaBean) dynaClass.newInstance();
      dynaBean.setDynaBeanClass( dynaClass );
      dynaBean.set( "name", "json" );
      dynaBean.set( "func", new JSONFunction( "return this;" ) );
      dynaBean.set( "int", new Integer( 1 ) );
      return dynaBean;
   }
}