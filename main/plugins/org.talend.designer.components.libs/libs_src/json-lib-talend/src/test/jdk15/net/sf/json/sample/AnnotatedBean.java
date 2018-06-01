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

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class AnnotatedBean
{
   private String string1;
   @JsonAnnotation
   private String string2;
   private String string3;

   public String getString1()
   {
      return string1;
   }

   public void setString1( String string )
   {
      this.string1 = string;
   }
   
   public String getString2()
   {
      return string2;
   }

   public void setString2( String string )
   {
      this.string2 = string;
   }
   
   @JsonAnnotation
   public String getString3()
   {
      return string3;
   }

   public void setString3( String string )
   {
      this.string3 = string;
   }
}