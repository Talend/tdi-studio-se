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

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.sf.json.util.JSONUtils;
import net.sf.json.util.JsonEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for JSONObject and JSONArray.
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
abstract class AbstractJSON implements JSON {
   private static class CycleSet extends ThreadLocal {
      protected Object initialValue() {
         return new SoftReference(new HashSet());
      }

      public Set getSet() {
         Set set = (Set) ((SoftReference)get()).get();
         if( set == null ) {
             set = new HashSet();
             set(new SoftReference(set));
         }
         return set;
      }
   }
   
   private static CycleSet cycleSet = new CycleSet();

   private static final Log log = LogFactory.getLog( AbstractJSON.class );

   /**
    * Adds a reference for cycle detection check.
    *
    * @param instance the reference to add
    * @return true if the instance has not been added previously, false
    *        otherwise.
    */
   protected static boolean addInstance( Object instance ) {
      return getCycleSet().add( instance );
   }

   /**
    * Fires an end of array event.
    */
   protected static void fireArrayEndEvent( JsonConfig jsonConfig ) {
      if( jsonConfig.isEventTriggeringEnabled() ){
         for( Iterator listeners = jsonConfig.getJsonEventListeners()
               .iterator(); listeners.hasNext(); ){
            JsonEventListener listener = (JsonEventListener) listeners.next();
            try{
               listener.onArrayEnd();
            }catch( RuntimeException e ){
               log.warn( e );
            }
         }
      }
   }

   /**
    * Fires a start of array event.
    */
   protected static void fireArrayStartEvent( JsonConfig jsonConfig ) {
      if( jsonConfig.isEventTriggeringEnabled() ){
         for( Iterator listeners = jsonConfig.getJsonEventListeners()
               .iterator(); listeners.hasNext(); ){
            JsonEventListener listener = (JsonEventListener) listeners.next();
            try{
               listener.onArrayStart();
            }catch( RuntimeException e ){
               log.warn( e );
            }
         }
      }
   }

   /**
    * Fires an element added event.
    *
    * @param index the index where the element was added
    * @param element the added element
    */
   protected static void fireElementAddedEvent( int index, Object element, JsonConfig jsonConfig ) {
      if( jsonConfig.isEventTriggeringEnabled() ){
         for( Iterator listeners = jsonConfig.getJsonEventListeners()
               .iterator(); listeners.hasNext(); ){
            JsonEventListener listener = (JsonEventListener) listeners.next();
            try{
               listener.onElementAdded( index, element );
            }catch( RuntimeException e ){
               log.warn( e );
            }
         }
      }
   }

   /**
    * Fires an error event.
    *
    * @param jsone the thrown exception
    */
   protected static void fireErrorEvent( JSONException jsone, JsonConfig jsonConfig ) {
      if( jsonConfig.isEventTriggeringEnabled() ){
         for( Iterator listeners = jsonConfig.getJsonEventListeners()
               .iterator(); listeners.hasNext(); ){
            JsonEventListener listener = (JsonEventListener) listeners.next();
            try{
               listener.onError( jsone );
            }catch( RuntimeException e ){
               log.warn( e );
            }
         }
      }
   }

   /**
    * Fires an end of object event.
    */
   protected static void fireObjectEndEvent( JsonConfig jsonConfig ) {
      if( jsonConfig.isEventTriggeringEnabled() ){
         for( Iterator listeners = jsonConfig.getJsonEventListeners()
               .iterator(); listeners.hasNext(); ){
            JsonEventListener listener = (JsonEventListener) listeners.next();
            try{
               listener.onObjectEnd();
            }catch( RuntimeException e ){
               log.warn( e );
            }
         }
      }
   }

   /**
    * Fires a start of object event.
    */
   protected static void fireObjectStartEvent( JsonConfig jsonConfig ) {
      if( jsonConfig.isEventTriggeringEnabled() ){
         for( Iterator listeners = jsonConfig.getJsonEventListeners()
               .iterator(); listeners.hasNext(); ){
            JsonEventListener listener = (JsonEventListener) listeners.next();
            try{
               listener.onObjectStart();
            }catch( RuntimeException e ){
               log.warn( e );
            }
         }
      }
   }

   /**
    * Fires a property set event.
    *
    * @param key the name of the property
    * @param value the value of the property
    * @param accumulated if the value has been accumulated over 'key'
    */
   protected static void firePropertySetEvent( String key, Object value, boolean accumulated,
         JsonConfig jsonConfig ) {
      if( jsonConfig.isEventTriggeringEnabled() ){
         for( Iterator listeners = jsonConfig.getJsonEventListeners()
               .iterator(); listeners.hasNext(); ){
            JsonEventListener listener = (JsonEventListener) listeners.next();
            try{
               listener.onPropertySet( key, value, accumulated );
            }catch( RuntimeException e ){
               log.warn( e );
            }
         }
      }
   }

   /**
    * Fires a warning event.
    *
    * @param warning the warning message
    */
   protected static void fireWarnEvent( String warning, JsonConfig jsonConfig ) {
      if( jsonConfig.isEventTriggeringEnabled() ){
         for( Iterator listeners = jsonConfig.getJsonEventListeners()
               .iterator(); listeners.hasNext(); ){
            JsonEventListener listener = (JsonEventListener) listeners.next();
            try{
               listener.onWarning( warning );
            }catch( RuntimeException e ){
               log.warn( e );
            }
         }
      }
   }

   /**
    * Removes a reference for cycle detection check.
    */
   protected static void removeInstance( Object instance ) {
      Set set = getCycleSet();
      set.remove( instance );
      if(set.size() == 0) {
          cycleSet.remove();
      }
   }

   protected Object _processValue( Object value, JsonConfig jsonConfig ) {
      if( JSONNull.getInstance().equals( value ) ) {
         return JSONNull.getInstance();
      } else if( Class.class.isAssignableFrom( value.getClass() ) || value instanceof Class ) {
         return ((Class) value).getName();
      } else if( JSONUtils.isFunction( value ) ) {
         if( value instanceof String ) {
            value = JSONFunction.parse( (String) value );
         }
         return value;
      } else if( value instanceof JSONString ) {
         return JSONSerializer.toJSON( (JSONString) value, jsonConfig );
      } else if( value instanceof JSON ) {
         return JSONSerializer.toJSON( value, jsonConfig );
      } else if( JSONUtils.isArray( value ) ) {
         return JSONArray.fromObject( value, jsonConfig );
      } else if( JSONUtils.isString( value ) ) {
         String str = String.valueOf( value );
         if( JSONUtils.hasQuotes( str ) ){
            String stripped = JSONUtils.stripQuotes( str );
            if( JSONUtils.isFunction( stripped )){
               return JSONUtils.DOUBLE_QUOTE + stripped + JSONUtils.DOUBLE_QUOTE;
            }
            if(stripped.startsWith("[") && stripped.endsWith("]")) {
               return stripped;
            }
            if(stripped.startsWith("{") && stripped.endsWith("}")) {
               return stripped;
            }
            return str;
         } else if( JSONUtils.isJsonKeyword( str, jsonConfig ) ) {
            if( jsonConfig.isJavascriptCompliant() && "undefined".equals( str )){
               return JSONNull.getInstance();
            }
            return str;
         } else if( JSONUtils.mayBeJSON( str ) ) {
            try {
               return JSONSerializer.toJSON( str, jsonConfig );
            } catch( JSONException jsone ) {
               return str;
            }
         }
         return str;
      } else if( JSONUtils.isNumber( value ) ) {
         JSONUtils.testValidity( value );
         return JSONUtils.transformNumber( (Number) value );
      } else if( JSONUtils.isBoolean( value ) ) {
         return value;
      } else {
         JSONObject jsonObject = JSONObject.fromObject( value, jsonConfig );
         if( jsonObject.isNullObject() ) {
            return JSONNull.getInstance();
         } else {
            return jsonObject;
         }
      }
   }
   
   private static Set getCycleSet() {
      return cycleSet.getSet();
   }

    public final Writer write(Writer writer) throws IOException {
        write(writer,NORMAL);
        return writer;
    }

    public final Writer writeCanonical(Writer writer) throws IOException {
        write(writer,CANONICAL);
        return writer;
    }

    protected abstract void write(Writer w, WritingVisitor v) throws IOException;

    interface WritingVisitor {
        Collection keySet(JSONObject o);
        void on(JSON o, Writer w) throws IOException;
        void on(Object value, Writer w) throws IOException;
    }

    private static final WritingVisitor NORMAL = new WritingVisitor() {
        public Collection keySet(JSONObject o) {
            return o.keySet();
        }

        public void on(JSON o, Writer w) throws IOException {
            o.write(w);
        }

        public void on(Object value, Writer w) throws IOException {
            w.write(JSONUtils.valueToString(value));
        }
    };

    private static final WritingVisitor CANONICAL = new WritingVisitor() {
        public Collection keySet(JSONObject o) {
            return new TreeSet(o.keySet()); // sort them alphabetically
        }

        public void on(JSON o, Writer w) throws IOException {
            o.writeCanonical(w);
        }

        public void on(Object value, Writer w) throws IOException {
            w.write(JSONUtils.valueToCanonicalString(value));
        }
    };
}