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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class SetBean {
   private Set attributes = new HashSet();

   public void addAttribute( Object value ) {
      this.attributes.add( value );
   }

   public boolean equals( Object obj ) {
      if( obj == this ){
         return true;
      }
      if( obj == null ){
         return false;
      }
      if( !SetBean.class.isAssignableFrom( obj.getClass() ) ){
         return false;
      }
      return EqualsBuilder.reflectionEquals( this, obj );
   }

   public Set getAttributes() {
      return attributes;
   }

   public int hashCode() {
      return HashCodeBuilder.reflectionHashCode( this );
   }

   public void setAttributes( Set attributes ) {
      this.attributes = attributes;
   }

   public String toString() {
      return ToStringBuilder.reflectionToString( this, ToStringStyle.MULTI_LINE_STYLE );
   }
}