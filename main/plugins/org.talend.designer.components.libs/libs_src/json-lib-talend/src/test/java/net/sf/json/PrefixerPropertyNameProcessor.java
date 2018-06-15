/**
 * 
 */
package net.sf.json;

import net.sf.json.processors.PropertyNameProcessor;

public class PrefixerPropertyNameProcessor implements PropertyNameProcessor {
      private final String prefix;

      public PrefixerPropertyNameProcessor( String prefix ) {
         this.prefix = prefix;
      }

      public String processPropertyName( Class beanClass, String name ) {
         return prefix + name;
      }
   }