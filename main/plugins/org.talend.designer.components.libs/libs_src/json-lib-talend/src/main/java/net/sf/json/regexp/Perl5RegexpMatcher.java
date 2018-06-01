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

package net.sf.json.regexp;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Jakarta-oro RegexpMatcher Implementation.<br>
 * Runs on older JVMs (1.3.1). You must have oro-2.0.8.jar configured in your classpath.
 * 
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class Perl5RegexpMatcher implements RegexpMatcher {
   private static final Perl5Compiler compiler = new Perl5Compiler();
   private Pattern pattern;

   public Perl5RegexpMatcher( String pattern ) {
      this( pattern, false );
   }

   public Perl5RegexpMatcher( String pattern, boolean multiline ) {
      try {
         if( multiline ) {
            this.pattern = compiler.compile( pattern, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.MULTILINE_MASK );
         } else {
            this.pattern = compiler.compile( pattern, Perl5Compiler.READ_ONLY_MASK );
         }
      } catch( MalformedPatternException mpe ) {
         throw new NestableRuntimeException( mpe );
      }
   }

   public String getGroupIfMatches( String str, int group ) {
      PatternMatcher matcher = new Perl5Matcher();
      if( matcher.matches( str, pattern ) ) {
         return matcher.getMatch().group( 1 );
      }
      return "";
   }

   public boolean matches( String str ) {
      return new Perl5Matcher().matches( str, pattern );
   }
}