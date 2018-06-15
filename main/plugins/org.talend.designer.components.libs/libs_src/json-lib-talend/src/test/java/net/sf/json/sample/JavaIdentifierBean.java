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
public class JavaIdentifierBean {
   private String camelCase;
   private String under_score;
   private String whitespace;

   public String getCamelCase() {
      return camelCase;
   }

   public String getUnder_score() {
      return under_score;
   }

   public String getWhitespace() {
      return whitespace;
   }

   public void setCamelCase( String camelCase ) {
      this.camelCase = camelCase;
   }

   public void setUnder_score( String under_score ) {
      this.under_score = under_score;
   }

   public void setWhitespace( String whitespace ) {
      this.whitespace = whitespace;
   }
}