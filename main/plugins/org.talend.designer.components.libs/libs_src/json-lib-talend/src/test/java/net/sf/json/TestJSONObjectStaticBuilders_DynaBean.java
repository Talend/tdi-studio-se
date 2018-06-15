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

import java.util.HashMap;
import java.util.Map;

import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.ezmorph.bean.MorphDynaClass;

/**
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class TestJSONObjectStaticBuilders_DynaBean extends AbstractJSONObjectStaticBuildersTestCase {
   public static void main( String[] args ) {
      junit.textui.TestRunner.run( TestJSONObjectStaticBuilders_DynaBean.class );
   }

   public TestJSONObjectStaticBuilders_DynaBean( String name ) {
      super( name );
   }

   protected Object getSource() {
      Map map = new HashMap();
      String[] props = getProperties();
      for( int i = 0; i < props.length; i++ ){
         map.put( props[i], PropertyConstants.getPropertyClass( props[i] ) );
      }
      map.put( "class", Class.class );
      map.put( "pexcluded", String.class );
      MorphDynaClass dynaClass = new MorphDynaClass( map );
      MorphDynaBean dynaBean = null;
      try{
         dynaBean = (MorphDynaBean) dynaClass.newInstance();
         for( int i = 0; i < props.length; i++ ){
            dynaBean.set( props[i], PropertyConstants.getPropertyValue( props[i] ) );
         }
         dynaBean.set( "class", Object.class );
         dynaBean.set( "pexcluded", "" );
      }catch( Exception e ){
         throw new RuntimeException( e );
      }

      return dynaBean;
   }
}