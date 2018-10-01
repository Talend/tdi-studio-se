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

import java.util.List;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class EmptyBean {
   private Object[] arrayp;
   private Byte bytep;
   private Character charp;
   private Double doublep;
   private Float floatp;
   private Integer intp;
   private List listp;
   private Long longp;
   private Short shortp;
   private String stringp;

   public Object[] getArrayp() {
      return arrayp;
   }

   public Byte getBytep() {
      return bytep;
   }

   public Character getCharp() {
      return charp;
   }

   public Double getDoublep() {
      return doublep;
   }

   public Float getFloatp() {
      return floatp;
   }

   public Integer getIntp() {
      return intp;
   }

   public List getListp() {
      return listp;
   }

   public Long getLongp() {
      return longp;
   }

   public Short getShortp() {
      return shortp;
   }

   public String getStringp() {
      return stringp;
   }

   public void setArrayp( Object[] arrayp ) {
      this.arrayp = arrayp;
   }

   public void setBytep( Byte bytep ) {
      this.bytep = bytep;
   }

   public void setCharp( Character charp ) {
      this.charp = charp;
   }

   public void setDoublep( Double doublep ) {
      this.doublep = doublep;
   }

   public void setFloatp( Float floatp ) {
      this.floatp = floatp;
   }

   public void setIntp( Integer intp ) {
      this.intp = intp;
   }

   public void setListp( List listp ) {
      this.listp = listp;
   }

   public void setLongp( Long longp ) {
      this.longp = longp;
   }

   public void setShortp( Short shortp ) {
      this.shortp = shortp;
   }

   public void setStringp( String stringp ) {
      this.stringp = stringp;
   }
}