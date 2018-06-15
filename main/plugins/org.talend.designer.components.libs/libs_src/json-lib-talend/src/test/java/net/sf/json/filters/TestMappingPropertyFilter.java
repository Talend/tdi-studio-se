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

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestMappingPropertyFilter extends TestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestMappingPropertyFilter.class );
   }

   public TestMappingPropertyFilter( String testName ) {
      super( testName );
   }

   public void testApply() {
      MappingPropertyFilter filter = new SampleMappingPropertyFilter();
      filter.addPropertyFilter( String.class, new TruePropertyFilter() );
      filter.addPropertyFilter( Long.class, new TruePropertyFilter() );

      assertFalse( filter.apply( "String", null, null ) );
      assertTrue( filter.apply( new Long( 1L ), null, null ) );
   }

   public static class SampleMappingPropertyFilter extends MappingPropertyFilter {
      protected boolean keyMatches( Object key, Object source, String name, Object value ) {
         return ((Class) key).isAssignableFrom( source.getClass() ) && source instanceof Number;
      }

   }
}