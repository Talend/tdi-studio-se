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

import java.lang.reflect.Field;
import java.util.Map;

import net.sf.json.JSONException;
import net.sf.json.JsonConfig;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Defines a custom setter to be used when setting object values.<br>
 * Specify with JsonConfig.setJsonPropertySetter().
 *
 * @author Gino Miceli <ginomiceli@users.sourceforge.net>
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class PropertySetStrategy {
   public static final PropertySetStrategy DEFAULT = new DefaultPropertySetStrategy();

   public abstract void setProperty( Object bean, String key, Object value ) throws JSONException;
   
   public void setProperty( Object bean, String key, Object value, JsonConfig jsonConfig ) throws JSONException {
      setProperty( bean, key, value );
   }

   private static final class DefaultPropertySetStrategy extends PropertySetStrategy {
      public void setProperty( Object bean, String key, Object value ) throws JSONException {
         setProperty( bean, key, value, new JsonConfig());
      }
      
      public void setProperty( Object bean, String key, Object value, JsonConfig jsonConfig ) throws JSONException {
         if( bean instanceof Map ){
            ((Map) bean).put( key, value );
         } else {
            if( !jsonConfig.isIgnorePublicFields() ) {
               try {
                  Field field = bean.getClass().getField( key );
                  if( field != null ) field.set( bean, value );
               } catch( Exception e ){
                  _setProperty( bean, key, value );
               }
            } else {
               _setProperty( bean, key, value );
            }
         }
      }
      
      private void _setProperty( Object bean, String key, Object value ) {
         try {
            PropertyUtils.setSimpleProperty( bean, key, value );
         } catch( Exception e ) {
            throw new JSONException( e );
         }
      }
      
   }
}