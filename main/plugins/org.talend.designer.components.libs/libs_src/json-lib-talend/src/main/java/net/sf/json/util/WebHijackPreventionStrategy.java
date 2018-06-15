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

package net.sf.json.util;

/**
 * Defines base implementations for preventing WebHijack in AJAX applications.
 * The default implementations are:
 * <ul>
 * <li>COMMENTS - wraps the string with /* *\/</li>
 * <li>INFINITE_LOOP - prepends "while(1);"</li>
 * </ul>
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class WebHijackPreventionStrategy {
   /** Wraps the string with /* *\/ */
   public static final WebHijackPreventionStrategy COMMENTS = new CommentWebHijackPreventionStrategy();
   /** Prepends "while(1);" */
   public static final WebHijackPreventionStrategy INFINITE_LOOP = new InfiniteLoopWebHijackPreventionStrategy();

   /**
    * Transforms the input with the desired strategy.<br>
    *
    * @param str a json string
    * @return String - the transformed json string
    */
   public abstract String protect( String str );

   private static final class CommentWebHijackPreventionStrategy extends
         WebHijackPreventionStrategy {
      public String protect( String str ) {
         return "/*" + str + "*/";
      }
   }

   private static final class InfiniteLoopWebHijackPreventionStrategy extends
         WebHijackPreventionStrategy {
      public String protect( String str ) {
         return "while(1);" + str;
      }
   }
}