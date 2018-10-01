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

package net.sf.json.util;

import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestWebUtils extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestWebUtils.class );
   }

   public TestWebUtils( String name ) {
      super( name );
   }

   public void testProtect_comments() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "with:quotes", "json" );
      WebUtils.setWebHijackPreventionStrategy( WebHijackPreventionStrategy.COMMENTS );
      String str = WebUtils.protect( jsonObject );
      assertEquals( 0, str.compareTo( "/*{\"with:quotes\":\"json\"}*/" ) );
   }

   public void testProtect_comments_and_shrink() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "noquotes", "json" );
      WebUtils.setWebHijackPreventionStrategy( WebHijackPreventionStrategy.COMMENTS );
      String str = WebUtils.protect( jsonObject, true );
      assertEquals( 0, str.compareTo( "/*{noquotes:\"json\"}*/" ) );
   }

   public void testProtect_inifiniteLoop() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "with:quotes", "json" );
      WebUtils.setWebHijackPreventionStrategy( WebHijackPreventionStrategy.INFINITE_LOOP );
      String str = WebUtils.protect( jsonObject );
      assertEquals( 0, str.compareTo( "while(1);{\"with:quotes\":\"json\"}" ) );
   }

   public void testProtect_inifiniteLoop_and_shrink() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "noquotes", "json" );
      WebUtils.setWebHijackPreventionStrategy( WebHijackPreventionStrategy.INFINITE_LOOP );
      String str = WebUtils.protect( jsonObject, true );
      assertEquals( 0, str.compareTo( "while(1);{noquotes:\"json\"}" ) );
   }

   public void testToString_array_noquotes() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "noquotes", "json" );
      JSONArray jsonArray = new JSONArray().element( jsonObject );
      String str = WebUtils.toString( jsonArray );
      assertEquals( 0, str.compareTo( "[{noquotes:\"json\"}]" ) );
   }

   public void testToString_array_withquotes1() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "with quotes", "json" );
      JSONArray jsonArray = new JSONArray().element( jsonObject );
      String str = WebUtils.toString( jsonArray );
      assertEquals( 0, str.compareTo( "[{\"with quotes\":\"json\"}]" ) );
   }

   public void testToString_array_withquotes2() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "with:quotes", "json" );
      JSONArray jsonArray = new JSONArray().element( jsonObject );
      String str = WebUtils.toString( jsonArray );
      assertEquals( 0, str.compareTo( "[{\"with:quotes\":\"json\"}]" ) );
   }

   public void testToString_object_noquotes() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "noquotes", "json" );
      String str = WebUtils.toString( jsonObject );
      assertEquals( 0, str.compareTo( "{noquotes:\"json\"}" ) );
   }

   public void testToString_object_withquotes1() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "with quotes", "json" );
      String str = WebUtils.toString( jsonObject );
      assertEquals( 0, str.compareTo( "{\"with quotes\":\"json\"}" ) );
   }

   public void testToString_object_withquotes2() {
      JSONObject jsonObject = new JSONObject();
      jsonObject.element( "with:quotes", "json" );
      String str = WebUtils.toString( jsonObject );
      assertEquals( 0, str.compareTo( "{\"with:quotes\":\"json\"}" ) );
   }
}