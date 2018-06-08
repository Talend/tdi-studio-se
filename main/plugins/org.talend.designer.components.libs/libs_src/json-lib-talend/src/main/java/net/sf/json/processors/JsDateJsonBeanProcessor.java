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

import java.util.Calendar;
import java.util.Date;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * Transforms a java.util.Date into a JSONObject ideal for JsDate conversion.<br>
 * Example:<br>
 *
 * <pre>
 {
 "minutes": 13,
 "seconds": 14,
 "hours": 12,
 "month": 5,
 "year": 2007,
 "day": 17,
 "milliseconds": 150
 }
 </pre>
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class JsDateJsonBeanProcessor implements JsonBeanProcessor {

   /**
    * Processes the input bean into a compatible JsDate.<br>
    */
   public JSONObject processBean( Object bean, JsonConfig jsonConfig ) {
      JSONObject jsonObject = null;
      if( bean instanceof java.sql.Date ){
         bean = new Date( ((java.sql.Date) bean).getTime() );
      }
      if( bean instanceof Date ){
         Calendar c = Calendar.getInstance();
         c.setTime( (Date) bean );
         jsonObject = new JSONObject().element( "year", c.get( Calendar.YEAR ) )
               .element( "month", c.get( Calendar.MONTH ) )
               .element( "day", c.get( Calendar.DAY_OF_MONTH ) )
               .element( "hours", c.get( Calendar.HOUR_OF_DAY ) )
               .element( "minutes", c.get( Calendar.MINUTE ) )
               .element( "seconds", c.get( Calendar.SECOND ) )
               .element( "milliseconds", c.get( Calendar.MILLISECOND ) );
      }else{
         jsonObject = new JSONObject( true );
      }
      return jsonObject;
   }
}