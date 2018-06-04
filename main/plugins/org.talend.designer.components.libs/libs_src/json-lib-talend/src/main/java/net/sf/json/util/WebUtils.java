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

import java.util.Iterator;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

/**
 * Provides useful methods for working with JSON and web.
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class WebUtils {
   private static final WebHijackPreventionStrategy DEFAULT_WEB_HIJACK_PREVENTION_STRATEGY = WebHijackPreventionStrategy.INFINITE_LOOP;
   private static WebHijackPreventionStrategy webHijackPreventionStrategy = DEFAULT_WEB_HIJACK_PREVENTION_STRATEGY;

   /**
    * Returns the configured WebHijackPreventionStrategy.
    */
   public static WebHijackPreventionStrategy getWebHijackPreventionStrategy() {
      return webHijackPreventionStrategy;
   }

   /**
    * Transforms the input Json string using the configured
    * WebHijackPreventionStrategy.<br>
    *
    * @param json the input string
    * @return String a transformed json string
    */
   public static String protect( JSON json ) {
      return protect( json, false );
   }

   /**
    * Transforms the input Json string using the configured
    * WebHijackPreventionStrategy.<br>
    *
    * @param json the input string
    * @param shrink if redundant key quotes may be eliminated.
    * @return String a transformed json string
    */
   public static String protect( JSON json, boolean shrink ) {
      String output = !shrink ? json.toString( 0 ) : toString( json );
      return webHijackPreventionStrategy.protect( output );
   }

   /**
    * Sets a WebHijackPreventionStrategy.<br>
    * Will use default value (WebHijackPreventionStrategy.INFINITE_LOOP) if
    * null.
    */
   public static void setWebHijackPreventionStrategy( WebHijackPreventionStrategy strategy ) {
      webHijackPreventionStrategy = strategy == null ? DEFAULT_WEB_HIJACK_PREVENTION_STRATEGY
            : strategy;
   }

   /**
    * Returns a string represenation of a JSON value.<br>
    * When an object property name does not contain a space (' ') or a colon
    * (':'), the quotes are omitted. This is done to reduce the amount of bytes
    * sent to a web browser.<br/>USE WITH CAUTION.
    */
   public static String toString( JSON json ) {
      if( json instanceof JSONObject ){
         return toString( (JSONObject) json );
      }else if( json instanceof JSONArray ){
         return toString( (JSONArray) json );
      }else{
         return toString( (JSONNull) json );
      }
   }

   private static String join( JSONArray jsonArray ) {
      int len = jsonArray.size();
      StringBuffer sb = new StringBuffer();

      for( int i = 0; i < len; i += 1 ){
         if( i > 0 ){
            sb.append( "," );
         }
         Object value = jsonArray.get( i );
         sb.append( toString( value ) );

      }
      return sb.toString();
   }

   private static String quote( String str ) {
      if( str.indexOf( " " ) > -1 || str.indexOf( ":" ) > -1 ){
         return JSONUtils.quote( str );
      }else{
         return str;
      }
   }

   private static String toString( JSONArray jsonArray ) {
      try{
         return '[' + join( jsonArray ) + ']';
      }catch( Exception e ){
         return null;
      }
   }

   private static String toString( JSONNull jsonNull ) {
      return jsonNull.toString();
   }

   private static String toString( JSONObject jsonObject ) {
      if( jsonObject.isNullObject() ){
         return JSONNull.getInstance()
               .toString();
      }
      Iterator keys = jsonObject.keys();
      StringBuffer sb = new StringBuffer( "{" );

      while( keys.hasNext() ){
         if( sb.length() > 1 ){
            sb.append( ',' );
         }
         Object o = keys.next();
         sb.append( quote( o.toString() ) );
         sb.append( ':' );
         sb.append( toString( jsonObject.get( String.valueOf( o ) ) ) );
      }
      sb.append( '}' );
      return sb.toString();
   }

   private static String toString( Object object ) {
      if( object instanceof JSON ){
         return toString( (JSON) object );
      }else{
         return JSONUtils.valueToString( object );
      }
   }

   private WebUtils() {
   }
}