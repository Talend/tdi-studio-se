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

/**
 * Convenience utility for working withRegexpMatcher.<br>
 * 
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class RegexpUtils {
   private static String javaVersion = "1.3.1";
   static{
      javaVersion = System.getProperty( "java.version" );
   }

   /**
    * Returns a RegexpMatcher that works in a specific environment.<br>
    * When in a JVM 1.3.1 it will return a Perl5RegexpMatcher, if the JVM is
    * younger (1.4+) it will return a JdkRegexpMatcher.
    */
   public static RegexpMatcher getMatcher( String pattern ) {
      if( isJDK13() ){
         return new Perl5RegexpMatcher( pattern );
      }else{
         return new JdkRegexpMatcher( pattern );
      }
   }

   /**
    * Returns a RegexpMatcher that works in a specific environment.<br>
    * When in a JVM 1.3.1 it will return a Perl5RegexpMatcher, if the JVM is
    * younger (1.4+) it will return a JdkRegexpMatcher.
    */
   public static RegexpMatcher getMatcher( String pattern, boolean multiline ) {
      if( isJDK13() ){
         return new Perl5RegexpMatcher( pattern, true );
      }else{
         return new JdkRegexpMatcher( pattern, true );
      }
   }
   
   /**
    * Queries the environment for the supported JDK version.
    */
   public static boolean isJDK13() {
      return javaVersion.indexOf( "1.3" ) != -1;
   }

   private RegexpUtils() {

   }
}