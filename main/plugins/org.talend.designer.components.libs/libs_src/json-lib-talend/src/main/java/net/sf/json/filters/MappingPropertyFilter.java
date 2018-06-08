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

package net.sf.json.filters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.util.PropertyFilter;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class MappingPropertyFilter implements PropertyFilter {
   private Map filters = new HashMap();

   public MappingPropertyFilter() {
      this( null );
   }

   public MappingPropertyFilter( Map filters ) {
      if( filters != null ){
         for( Iterator i = filters.entrySet()
               .iterator(); i.hasNext(); ){
            Map.Entry entry = (Map.Entry) i.next();
            Object key = entry.getKey();
            Object filter = entry.getValue();
            if( filter instanceof PropertyFilter ){
               this.filters.put( key, filter );
            }
         }
      }
   }

   public void addPropertyFilter( Object target, PropertyFilter filter ) {
      if( filter != null ){
         filters.put( target, filter );
      }
   }

   public boolean apply( Object source, String name, Object value ) {
      for( Iterator i = filters.entrySet()
            .iterator(); i.hasNext(); ){
         Map.Entry entry = (Map.Entry) i.next();
         Object key = entry.getKey();
         if( keyMatches( key, source, name, value ) ){
            PropertyFilter filter = (PropertyFilter) entry.getValue();
            return filter.apply( source, name, value );
         }
      }
      return false;
   }

   public void removePropertyFilter( Object target ) {
      if( target != null ){
         filters.remove( target );
      }
   }

   protected abstract boolean keyMatches( Object key, Object source, String name, Object value );
}