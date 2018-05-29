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

import net.sf.ezmorph.ObjectMorpher;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class EnumMorpher implements ObjectMorpher
{
   private Class enumClass;

   public EnumMorpher( Class enumClass )
   {
      if( enumClass == null ){
         throw new IllegalArgumentException( "enumClass is null" );
      }
      if( !Enum.class.isAssignableFrom( enumClass ) ){
         throw new IllegalArgumentException( "enumClass is not an Enum class" );
      }
      this.enumClass = enumClass;
   }

   public Object morph( Object value )
   {
      if( value == null ){
         return enumClass.cast( null );
      }
      return Enum.valueOf( enumClass, String.valueOf( value ) );
   }

   public Class morphsTo()
   {
      return enumClass;
   }

   public boolean supports( Class clazz )
   {
      return String.class.isAssignableFrom( clazz );
   }
}