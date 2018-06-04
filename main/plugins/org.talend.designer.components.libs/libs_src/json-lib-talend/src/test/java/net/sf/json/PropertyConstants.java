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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.sample.ObjectBean;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public final class PropertyConstants {
   private static final String ARRAY = "parray";
   private static final String BEAN = "pbean";
   private static final String BOOLEAN = "pboolean";
   private static final String BYTE = "pbyte";
   private static final String CHAR = "pchar";
   private static final String CLASS = "pclass";
   private static Map classes = new HashMap();
   private static final String DOUBLE = "pdouble";
   private static final String FLOAT = "pfloat";
   private static final String FUNCTION = "pfunction";
   private static final String INT = "pint";
   private static final String LIST = "plist";
   private static final String LONG = "plong";
   private static final String SHORT = "pshort";
   private static final String STRING = "pstring";
   private static Map values = new HashMap();

   static{
      values.put( BYTE, new Byte( Byte.MAX_VALUE ) );
      values.put( SHORT, new Short( Short.MAX_VALUE ) );
      values.put( INT, new Integer( Integer.MAX_VALUE ) );
      values.put( LONG, new Long( Long.MAX_VALUE ) );
      values.put( FLOAT, new Float( Float.MAX_VALUE ) );
      values.put( DOUBLE, new Double( Double.MAX_VALUE ) );
      values.put( BOOLEAN, Boolean.TRUE );
      values.put( CHAR, new Character( 'J' ) );
      values.put( STRING, "json" );
      values.put( FUNCTION, new JSONFunction( "this;" ) );
      values.put( ARRAY, new int[] { 1, 2 } );
      List list = new ArrayList();
      list.add( "a" );
      list.add( "b" );
      values.put( LIST, list );
      values.put( CLASS, Object.class );
      values.put( BEAN, new ObjectBean() );

      classes.put( BYTE, Byte.class );
      classes.put( SHORT, Short.class );
      classes.put( INT, Integer.class );
      classes.put( LONG, Long.class );
      classes.put( FLOAT, Float.class );
      classes.put( DOUBLE, Double.class );
      classes.put( BOOLEAN, Boolean.class );
      classes.put( CHAR, Character.class );
      classes.put( STRING, String.class );
      classes.put( FUNCTION, JSONFunction.class );
      classes.put( ARRAY, int[].class );
      classes.put( LIST, List.class );
      classes.put( CLASS, Class.class );
      classes.put( BEAN, ObjectBean.class );
   }

   public static String[] getProperties() {
      return new String[] { BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, CHAR, BOOLEAN, STRING, FUNCTION,
            ARRAY, LIST, CLASS, BEAN };
   }

   public static Class getPropertyClass( String key ) {
      return (Class) classes.get( key );
   }

   public static Object getPropertyValue( String key ) {
      return values.get( key );
   }
}