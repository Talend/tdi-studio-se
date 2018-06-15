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

package net.sf.json.processors;

import java.util.Set;

/**
 * Base class for finding a matching DefaultValueProcessor.<br>
 * <ul>
 * <li>DEFAULT - matches the target class with equals().</li>
 * </ul>
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class DefaultValueProcessorMatcher {
   /** Matches the target with equals() */
   public static final DefaultValueProcessorMatcher DEFAULT = new DefaultDefaultValueProcessorMatcher();

   /**
    * Returns the matching class calculated with the target class and the
    * provided set.
    *
    * @param target the target class to match
    * @param set a set of possible matches
    */
   public abstract Object getMatch( Class target, Set set );

   private static final class DefaultDefaultValueProcessorMatcher extends
         DefaultValueProcessorMatcher {
      public Object getMatch( Class target, Set set ) {
         if( target != null && set != null && set.contains( target ) ){
            return target;
         }
         return null;
      }
   }
}