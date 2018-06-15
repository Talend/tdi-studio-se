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

package net.sf.json.sample;

import net.sf.ezmorph.object.AbstractObjectMorpher;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class IdBean {
   private Id id;

   public Id getId() {
      return id;
   }

   public void setId( Id id ) {
      this.id = id;
   }

   public static class Id {
      private long value;

      public Id() {
         value = 0;
      }

      public Id( long value ) {
         super();
         this.value = value;
      }

      public boolean equals( Object obj ) {
         if( obj == this ){
            return true;
         }
         if( obj == null ){
            return false;
         }
         if( !(obj instanceof Id) ){
            return false;
         }
         Id other = (Id) obj;
         return value == other.value;
      }

      public long getValue() {
         return value;
      }

      public int hashCode() {
         return getClass().hashCode() + (int) value;
      }

      public void setValue( long value ) {
         this.value = value;
      }
   }

   public static class IdMorpher extends AbstractObjectMorpher {
      public Object morph( Object value ) {
         if( value != null ){
            if( value instanceof Number ){
               return new IdBean.Id( ((Number) value).longValue() );
            }else if( value instanceof String ){
               return new IdBean.Id( new Long( (String) value ).longValue() );
            }
         }
         return null;
      }

      public Class morphsTo() {
         return IdBean.Id.class;
      }
   }
}