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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class NumberBean {
   private BigDecimal pbigdec;
   private BigInteger pbigint;
   private byte pbyte;
   private double pdouble;
   private float pfloat;
   private int pint;
   private long plong;
   private short pshort;
   private Byte pwbyte;
   private Double pwdouble;
   private Float pwfloat;
   private Integer pwint;
   private Long pwlong;
   private Short pwshort;

   public BigDecimal getPbigdec() {
      return pbigdec;
   }

   public BigInteger getPbigint() {
      return pbigint;
   }

   public byte getPbyte() {
      return pbyte;
   }

   public double getPdouble() {
      return pdouble;
   }

   public float getPfloat() {
      return pfloat;
   }

   public int getPint() {
      return pint;
   }

   public long getPlong() {
      return plong;
   }

   public short getPshort() {
      return pshort;
   }

   public Byte getPwbyte() {
      return pwbyte;
   }

   public Double getPwdouble() {
      return pwdouble;
   }

   public Float getPwfloat() {
      return pwfloat;
   }

   public Integer getPwint() {
      return pwint;
   }

   public Long getPwlong() {
      return pwlong;
   }

   public Short getPwshort() {
      return pwshort;
   }

   public void setPbigdec( BigDecimal pbigdec ) {
      this.pbigdec = pbigdec;
   }

   public void setPbigint( BigInteger pbigint ) {
      this.pbigint = pbigint;
   }

   public void setPbyte( byte pbyte ) {
      this.pbyte = pbyte;
   }

   public void setPdouble( double pdouble ) {
      this.pdouble = pdouble;
   }

   public void setPfloat( float pfloat ) {
      this.pfloat = pfloat;
   }

   public void setPint( int pint ) {
      this.pint = pint;
   }

   public void setPlong( long plong ) {
      this.plong = plong;
   }

   public void setPshort( short pshort ) {
      this.pshort = pshort;
   }

   public void setPwbyte( Byte pwbyte ) {
      this.pwbyte = pwbyte;
   }

   public void setPwdouble( Double pwdouble ) {
      this.pwdouble = pwdouble;
   }

   public void setPwfloat( Float pwfloat ) {
      this.pwfloat = pwfloat;
   }

   public void setPwint( Integer pwint ) {
      this.pwint = pwint;
   }

   public void setPwlong( Long pwlong ) {
      this.pwlong = pwlong;
   }

   public void setPwshort( Short pwshort ) {
      this.pwshort = pwshort;
   }
}