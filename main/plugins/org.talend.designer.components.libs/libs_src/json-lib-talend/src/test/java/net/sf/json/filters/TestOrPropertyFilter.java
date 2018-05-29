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

package net.sf.json.filters;

import junit.framework.TestCase;
import net.sf.json.util.PropertyFilter;

/**
 * @author Orres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestOrPropertyFilter extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestOrPropertyFilter.class );
   }

   public TestOrPropertyFilter( String testName ) {
      super( testName );
   }

   public void testApply_false_false() {
      PropertyFilter filter = new OrPropertyFilter( new FalsePropertyFilter(),
            new FalsePropertyFilter() );
      assertFalse( filter.apply( null, null, null ) );
   }

   public void testApply_false_true() {
      PropertyFilter filter = new OrPropertyFilter( new FalsePropertyFilter(),
            new TruePropertyFilter() );
      assertTrue( filter.apply( null, null, null ) );
   }

   public void testApply_true_false() {
      PropertyFilter filter = new OrPropertyFilter( new TruePropertyFilter(),
            new FalsePropertyFilter() );
      assertTrue( filter.apply( null, null, null ) );
   }

   public void testApply_true_true() {
      PropertyFilter filter = new OrPropertyFilter( new TruePropertyFilter(),
            new TruePropertyFilter() );
      assertTrue( filter.apply( null, null, null ) );
   }
}