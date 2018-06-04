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

import junit.framework.TestCase;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public abstract class AbstractRegexpMatcherTestCase extends TestCase {
   public AbstractRegexpMatcherTestCase( String name ) {
      super( name );
   }

   public void testGetGroupIfMatches() {
      RegexpMatcher regexpMatcher = getRegexpMatcher( "[a-z]*([0-9]+)[a-z]*" );
      assertEquals( "123", regexpMatcher.getGroupIfMatches( "abc123edf", 1 ) );
      assertEquals( "", regexpMatcher.getGroupIfMatches( "abcedf", 1 ) );
   }

   public void testMatches() {
      assertTrue( getRegexpMatcher( ".*" ).matches( "everything" ) );
      assertTrue( getRegexpMatcher( "^json$" ).matches( "json" ) );
      assertFalse( getRegexpMatcher( "^json$" ).matches( "json " ) );
   }

   protected abstract RegexpMatcher getRegexpMatcher( String pattern );
}