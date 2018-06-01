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

package net.sf.json;

import java.util.List;

import net.sf.ezmorph.test.ArrayAssertions;
import net.sf.json.test.JSONAssert;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class Assertions extends JSONAssert {
   public static void assertEquals( List expecteds, List actuals ) {
      assertEquals( null, expecteds, actuals );
   }

   public static void assertEquals( Object expected, Object actual ) {
      assertEquals( null, expected, actual );
   }

   public static void assertEquals( String message, List expecteds, List actuals ) {
      String header = message == null ? "" : message + ": ";
      if( expecteds == null ){
         fail( header + "expected list was null" );
      }
      if( actuals == null ){
         fail( header + "actual list was null" );
      }
      if( expecteds == actuals || expecteds.equals( actuals ) ){
         return;
      }
      if( actuals.size() != expecteds.size() ){
         fail( header + "list sizes differed, expected.size()=" + expecteds.size()
               + " actual.size()=" + actuals.size() );
      }

      int max = expecteds.size();
      for( int i = 0; i < max; i++ ){
         Object o1 = expecteds.get( i );
         Object o2 = actuals.get( i );

         // handle nulls
         if( o1 == null ){
            if( o2 == null ){
               return;
            }else{
               fail( header + "lists first differed at element [" + i + "];" );
            }
         }else{
            if( o2 == null ){
               fail( header + "lists first differed at element [" + i + "];" );
            }
         }

         if( o1.getClass()
               .isArray() && o2.getClass()
               .isArray() ){
            Object[] expected = (Object[]) o1;
            Object[] actual = (Object[]) o2;
            assertEquals( header + "lists first differed at element " + i + ";", expected, actual );
         }else if( List.class.isAssignableFrom( o1.getClass() )
               && List.class.isAssignableFrom( o2.getClass() ) ){
            assertEquals( header + "lists first differed at element [" + i + "];", (List) o1,
                  (List) o2 );
         }else{
            if( o1 instanceof String && o2 instanceof JSONFunction ){
               assertEquals( header + "lists first differed at element [" + i + "];", (String) o1,
                     (JSONFunction) o2 );
            }else if( o1 instanceof JSONFunction && o2 instanceof String ){
               assertEquals( header + "lists first differed at element [" + i + "];",
                     (JSONFunction) o1, (String) o2 );
            }else if( o1 instanceof JSONObject && o2 instanceof JSONObject ){
               assertEquals( header + "lists first differed at element [" + i + "];",
                     (JSONObject) o1, (JSONObject) o2 );
            }else if( o1 instanceof JSONArray && o2 instanceof JSONArray ){
               assertEquals( header + "lists first differed at element [" + i + "];",
                     (JSONArray) o1, (JSONArray) o2 );
            }else if( o1 instanceof JSONFunction && o2 instanceof JSONFunction ){
               assertEquals( header + "lists first differed at element [" + i + "];",
                     (JSONFunction) o1, (JSONFunction) o2 );
            }else{
               assertEquals( header + "lists first differed at element [" + i + "];", o1, o2 );
            }
         }
      }
   }

   public static void assertEquals( String message, Object expected, Object actual ) {
      if( expected == null && actual == null )
         return;
      if( expected != null && expected.equals( actual ) )
         return;
      Class expectedClass = expected.getClass();
      Class actualClass = actual.getClass();
      if( expectedClass.isArray() && actualClass.isArray() ){
         Class expectedInnerType = expectedClass.getComponentType();
         Class actualInnerType = actualClass.getComponentType();
         if( expectedInnerType.isPrimitive() ){
            assertExpectedPrimitiveArrays( message, expected, actual, expectedInnerType,
                  actualInnerType );
         }else if( actualInnerType.isPrimitive() ){
            assertActualPrimitiveArrays( message, expected, actual, expectedInnerType,
                  actualInnerType );
         }else{
            ArrayAssertions.assertEquals( message, (Object[]) expected, (Object[]) actual );
         }
      }else{
         failNotEquals( message, expected, actual );
      }
   }

   private static void assertActualPrimitiveArrays( String message, Object expected, Object actual,
         Class expectedInnerType, Class actualInnerType ) {
      if( Boolean.TYPE.isAssignableFrom( actualInnerType ) ){
         if( Boolean.TYPE.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (boolean[]) expected, (boolean[]) actual );
         }else if( Boolean.class.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (Boolean[]) expected, (boolean[]) actual );
         }else if( !expectedInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (Object[]) expected, (boolean[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Byte.TYPE.isAssignableFrom( actualInnerType ) ){
         if( Byte.TYPE.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (byte[]) expected, (byte[]) actual );
         }else if( Byte.class.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (Byte[]) expected, (byte[]) actual );
         }else if( !expectedInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (Object[]) expected, (byte[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Short.TYPE.isAssignableFrom( actualInnerType ) ){
         if( Short.TYPE.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (short[]) expected, (short[]) actual );
         }else if( Short.class.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (Short[]) expected, (short[]) actual );
         }else if( !expectedInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (Object[]) expected, (short[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Integer.TYPE.isAssignableFrom( actualInnerType ) ){
         if( Integer.TYPE.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (int[]) expected, (int[]) actual );
         }else if( Integer.class.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (Integer[]) expected, (int[]) actual );
         }else if( !expectedInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (Object[]) expected, (int[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Long.TYPE.isAssignableFrom( actualInnerType ) ){
         if( Long.TYPE.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (long[]) expected, (long[]) actual );
         }else if( Long.class.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (Long[]) expected, (long[]) actual );
         }else if( !expectedInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (Object[]) expected, (long[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Float.TYPE.isAssignableFrom( actualInnerType ) ){
         if( Float.TYPE.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (float[]) expected, (float[]) actual );
         }else if( Float.class.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (Float[]) expected, (float[]) actual );
         }else if( !expectedInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (Object[]) expected, (float[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Double.TYPE.isAssignableFrom( actualInnerType ) ){
         if( Double.TYPE.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (double[]) expected, (double[]) actual );
         }else if( Double.class.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (Double[]) expected, (double[]) actual );
         }else if( !expectedInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (Object[]) expected, (double[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Character.TYPE.isAssignableFrom( actualInnerType ) ){
         if( Character.TYPE.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (char[]) expected, (char[]) actual );
         }else if( Character.class.isAssignableFrom( expectedInnerType ) ){
            ArrayAssertions.assertEquals( message, (Character[]) expected, (char[]) actual );
         }else if( !expectedInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (Object[]) expected, (char[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }
   }

   private static void assertExpectedPrimitiveArrays( String message, Object expected,
         Object actual, Class expectedInnerType, Class actualInnerType ) {
      if( Boolean.TYPE.isAssignableFrom( expectedInnerType ) ){
         if( Boolean.TYPE.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (boolean[]) expected, (boolean[]) actual );
         }else if( Boolean.class.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (boolean[]) expected, (Boolean[]) actual );
         }else if( !actualInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (boolean[]) expected, (Object[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Byte.TYPE.isAssignableFrom( expectedInnerType ) ){
         if( Byte.TYPE.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (byte[]) expected, (byte[]) actual );
         }else if( Byte.class.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (byte[]) expected, (Byte[]) actual );
         }else if( !actualInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (byte[]) expected, (Object[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Short.TYPE.isAssignableFrom( expectedInnerType ) ){
         if( Short.TYPE.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (short[]) expected, (short[]) actual );
         }else if( Short.class.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (short[]) expected, (Short[]) actual );
         }else if( !actualInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (short[]) expected, (Object[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Integer.TYPE.isAssignableFrom( expectedInnerType ) ){
         if( Integer.TYPE.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (int[]) expected, (int[]) actual );
         }else if( Integer.class.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (int[]) expected, (Integer[]) actual );
         }else if( !actualInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (int[]) expected, (Object[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Long.TYPE.isAssignableFrom( expectedInnerType ) ){
         if( Long.TYPE.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (long[]) expected, (long[]) actual );
         }else if( Long.class.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (long[]) expected, (Long[]) actual );
         }else if( !actualInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (long[]) expected, (Object[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Float.TYPE.isAssignableFrom( expectedInnerType ) ){
         if( Float.TYPE.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (float[]) expected, (float[]) actual );
         }else if( Float.class.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (float[]) expected, (Float[]) actual );
         }else if( !actualInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (float[]) expected, (Object[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Double.TYPE.isAssignableFrom( expectedInnerType ) ){
         if( Double.TYPE.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (double[]) expected, (double[]) actual );
         }else if( Double.class.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (double[]) expected, (Double[]) actual );
         }else if( !actualInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (double[]) expected, (Object[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }else if( Character.TYPE.isAssignableFrom( expectedInnerType ) ){
         if( Character.TYPE.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (char[]) expected, (char[]) actual );
         }else if( Character.class.isAssignableFrom( actualInnerType ) ){
            ArrayAssertions.assertEquals( message, (char[]) expected, (Character[]) actual );
         }else if( !actualInnerType.isPrimitive() ){
            ArrayAssertions.assertEquals( message, (char[]) expected, (Object[]) actual );
         }else{
            failNotEquals( message, expected, actual );
         }
      }
   }
}