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

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class AbstractJSONArrayStaticBuildersTestCase extends TestCase {
   public AbstractJSONArrayStaticBuildersTestCase( String testName ) {
      super( testName );
   }

   public void testFromObject() {
      JSONArray jsonArray = JSONArray.fromObject( getSource() );
      assertNotNull( jsonArray );
      assertEquals( 1, jsonArray.size() );
      JSONObject jsonObject = jsonArray.getJSONObject( 0 );
      assertJSONObject( jsonObject, getProperties() );
      assertTrue( !jsonObject.has( "class" ) );
   }

   public void testFromObject_excludes() {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setExcludes( getExclusions() );
      JSONArray jsonArray = JSONArray.fromObject( getSource(), jsonConfig );
      assertNotNull( jsonArray );
      assertEquals( 1, jsonArray.size() );
      JSONObject jsonObject = jsonArray.getJSONObject( 0 );
      assertJSONObject( jsonObject, getProperties() );
      String[] excluded = getExclusions();
      for( int i = 0; i < excluded.length; i++ ){
         assertTrue( !jsonObject.has( excluded[i] ) );
      }
      assertTrue( !jsonObject.has( "class" ) );
      assertTrue( !jsonObject.has( "pexcluded" ) );
   }

   public void testFromObject_excludes_ignoreDefaults() {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setExcludes( getExclusions() );
      jsonConfig.setIgnoreDefaultExcludes( true );
      JSONArray jsonArray = JSONArray.fromObject( getSource(), jsonConfig );
      assertNotNull( jsonArray );
      assertEquals( 1, jsonArray.size() );
      JSONObject jsonObject = jsonArray.getJSONObject( 0 );
      assertJSONObject( jsonObject, getProperties() );
      assertTrue( jsonObject.has( "class" ) );
      assertTrue( !jsonObject.has( "pexcluded" ) );
   }

   protected String[] getExclusions() {
      return new String[] { "pexcluded" };
   }

   protected String[] getProperties() {
      return PropertyConstants.getProperties();
   }

   protected abstract Object getSource();

   private void assertJSONObject( JSONObject json, String[] properties ) {
      assertNotNull( json );
      for( int i = 0; i < properties.length; i++ ){
         assertTrue( json.has( properties[i] ) );
      }
   }
}