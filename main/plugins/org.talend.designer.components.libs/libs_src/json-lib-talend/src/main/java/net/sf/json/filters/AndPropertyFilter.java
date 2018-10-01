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

import net.sf.json.util.PropertyFilter;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class AndPropertyFilter implements PropertyFilter {
   private PropertyFilter filter1;
   private PropertyFilter filter2;

   public AndPropertyFilter( PropertyFilter filter1, PropertyFilter filter2 ) {
      this.filter1 = filter1;
      this.filter2 = filter2;
   }

   public boolean apply( Object source, String name, Object value ) {
      if( filter1 != null && filter1.apply( source, name, value ) && filter2 != null
            && filter2.apply( source, name, value ) ){
         return true;
      }
      return false;
   }
}