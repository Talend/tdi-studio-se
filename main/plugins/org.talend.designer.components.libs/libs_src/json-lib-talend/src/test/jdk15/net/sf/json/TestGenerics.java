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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.sf.json.sample.GenericsBean;
import net.sf.json.sample.GenericsBean.GenericsInternalBean;

/**
 * @author Matt Small <msmall@wavemaker.com>
 */
public class TestGenerics extends TestCase {

   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestGenerics.class );
   }

   public TestGenerics( String testName ) {
      super( testName );
   }

   /*
   public void testBasicList() throws Exception {
      GenericsBean gb = new GenericsBean();
      List<String> stringAL = new ArrayList<String>();
      stringAL.add( "foo" );
      stringAL.add( "bar" );
      gb.setStringList( stringAL );

      JSONObject jo = JSONObject.fromObject( gb );
      String serializedString = jo.toString();
      jo = JSONObject.fromObject( serializedString );
      Object o = JSONObject.toBean( jo, GenericsBean.class );
      assertTrue( o instanceof GenericsBean );

      List<String> resultStringAL = ((GenericsBean) o).getStringList();
      assertEquals( stringAL.size(), resultStringAL.size() );
      assertEquals( stringAL.get( 0 ), resultStringAL.get( 0 ) );
      assertEquals( stringAL.get( 1 ), resultStringAL.get( 1 ) );
   }

   public void testBasicSet() throws Exception {
      GenericsBean gb = new GenericsBean();
      Set<String> stringHS = new HashSet<String>();
      stringHS.add( "foo" );
      stringHS.add( "bar" );
      gb.setStringSet( stringHS );

      JSONObject jo = JSONObject.fromObject( gb );
      String serializedString = jo.toString();
      jo = JSONObject.fromObject( serializedString );
      Object o = JSONObject.toBean( jo, GenericsBean.class );
      assertTrue( o instanceof GenericsBean );

      Set<String> resultStringHS = ((GenericsBean) o).getStringSet();
      assertEquals( stringHS.size(), resultStringHS.size() );

      for( String s : resultStringHS ){
         assertTrue( stringHS.contains( s ) );
      }
   }

   public void testComplexInList() throws Exception {
      GenericsBean gb = new GenericsBean();
      List<GenericsInternalBean> ibList = new ArrayList<GenericsInternalBean>();
      GenericsInternalBean gib = new GenericsInternalBean();
      gib.setString( "foo" );
      ibList.add( gib );
      gib = new GenericsInternalBean();
      gib.setString( "bar" );
      ibList.add( gib );
      gb.setGenericsInternalBeanList( ibList );

      JSONObject jo = JSONObject.fromObject( gb );
      String serializedString = jo.toString();
      jo = JSONObject.fromObject( serializedString );
      Object o = JSONObject.toBean( jo, GenericsBean.class );
      assertTrue( o instanceof GenericsBean );

      List<GenericsInternalBean> resultIBList = ((GenericsBean) o).getGenericsInternalBeanList();
      assertEquals( ibList.size(), resultIBList.size() );
      assertEquals( ibList.get( 0 )
            .getString(), resultIBList.get( 0 )
            .getString() );
      assertEquals( ibList.get( 1 )
            .getString(), resultIBList.get( 1 )
            .getString() );
   }

   public void testOnlyList() throws Exception {
      OnlyListBean gb = new OnlyListBean();
      ArrayList<String> stringAL = new ArrayList<String>();
      stringAL.add( "foo" );
      stringAL.add( "bar" );
      gb.setStringList( stringAL );

      JSONObject jo = JSONObject.fromObject( gb );
      Object slO = jo.get( "stringList" );
      assertTrue( slO instanceof JSONArray );
      JSONArray sloJA = (JSONArray) slO;
      assertEquals( 2, sloJA.size() );
      assertTrue( sloJA.contains( "foo" ) );
      assertTrue( sloJA.contains( "bar" ) );

      String serializedString = jo.toString();
      jo = JSONObject.fromObject( serializedString );
      Object o = JSONObject.toBean( jo, OnlyListBean.class );
      assertTrue( o instanceof OnlyListBean );
      OnlyListBean olb = (OnlyListBean) o;
      assertEquals( 2, olb.getStringList()
            .size() );
      assertTrue( olb.getStringList()
            .contains( "foo" ) );
      assertTrue( olb.getStringList()
            .contains( "bar" ) );
   }
   */

   public void testNOOP() {}

   public static class OnlyListBean {
      private List<String> stringList;

      public List<String> getStringList() {
         return stringList;
      }

      public void setStringList( List<String> stringList ) {
         this.stringList = stringList;
      }
   }
}
