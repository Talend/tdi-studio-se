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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.sf.json.JSONObject;

/**
 * Base class for creating Bean instances.<br>
 * <ul>
 * <li>DEFAULT - calls Class.newInstance().</li>
 * </ul>
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class NewBeanInstanceStrategy {
   /** Calls Class.newInstance() */
   public static final NewBeanInstanceStrategy DEFAULT = new DefaultNewBeanInstanceStrategy();

   /**
    * Creates a new instance.
    *
    * @param target the source class
    * @param source additional properties that may be needed to create the
    *        instance
    */
   public abstract Object newInstance( Class target, JSONObject source )
         throws InstantiationException, IllegalAccessException, SecurityException,
         NoSuchMethodException, InvocationTargetException;

   private static final class DefaultNewBeanInstanceStrategy extends NewBeanInstanceStrategy {
      private static final Object[] EMPTY_ARGS = new Object[0];
      private static final Class[] EMPTY_PARAM_TYPES = new Class[0];

      public Object newInstance( Class target, JSONObject source ) throws InstantiationException,
            IllegalAccessException, SecurityException, NoSuchMethodException,
            InvocationTargetException {
         if( target != null ){
            Constructor c = target.getDeclaredConstructor( EMPTY_PARAM_TYPES );
            c.setAccessible( true );
            try {
               return c.newInstance( EMPTY_ARGS );
            } catch ( InstantiationException e ) {
               // getCause() was added on jdk 1.4
               String cause = "";
               try { cause = e.getCause() != null ? "\n"+e.getCause().getMessage() : ""; }
               catch( Throwable t ) { /* ignore */ }
               throw new InstantiationException(
                     "Instantiation of \"" +  target + "\" failed. " +
                     "It's probably because class is an interface, " +
                     "abstract class, array class, primitive type or void." +
                     cause );
            }
         }
         return null;
      }
   }
}