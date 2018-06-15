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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.processors.DefaultDefaultValueProcessor;
import net.sf.json.processors.DefaultValueProcessor;
import net.sf.json.processors.DefaultValueProcessorMatcher;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.processors.JsonBeanProcessorMatcher;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.processors.JsonValueProcessorMatcher;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.processors.PropertyNameProcessorMatcher;
import net.sf.json.util.CycleDetectionStrategy;
import net.sf.json.util.JavaIdentifierTransformer;
import net.sf.json.util.JsonEventListener;
import net.sf.json.util.NewBeanInstanceStrategy;
import net.sf.json.util.PropertyExclusionClassMatcher;
import net.sf.json.util.PropertyFilter;
import net.sf.json.util.PropertySetStrategy;

import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class that helps configuring the serialization process.
 * 
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public class JsonConfig {
   public static final DefaultValueProcessorMatcher DEFAULT_DEFAULT_VALUE_PROCESSOR_MATCHER = DefaultValueProcessorMatcher.DEFAULT;
   public static final JsonBeanProcessorMatcher DEFAULT_JSON_BEAN_PROCESSOR_MATCHER = JsonBeanProcessorMatcher.DEFAULT;
   public static final JsonValueProcessorMatcher DEFAULT_JSON_VALUE_PROCESSOR_MATCHER = JsonValueProcessorMatcher.DEFAULT;
   public static final NewBeanInstanceStrategy DEFAULT_NEW_BEAN_INSTANCE_STRATEGY = NewBeanInstanceStrategy.DEFAULT;
   public static final PropertyExclusionClassMatcher DEFAULT_PROPERTY_EXCLUSION_CLASS_MATCHER = PropertyExclusionClassMatcher.DEFAULT;
   public static final PropertyNameProcessorMatcher DEFAULT_PROPERTY_NAME_PROCESSOR_MATCHER = PropertyNameProcessorMatcher.DEFAULT;
   public static final int MODE_LIST = 1;
   public static final int MODE_OBJECT_ARRAY = 2;
   public static final int MODE_SET = 2;
   private static final Class DEFAULT_COLLECTION_TYPE = List.class;
   private static final CycleDetectionStrategy DEFAULT_CYCLE_DETECTION_STRATEGY = CycleDetectionStrategy.STRICT;
   private static final String[] DEFAULT_EXCLUDES = new String[] { "class", "declaringClass", "metaClass" };
   private static final JavaIdentifierTransformer DEFAULT_JAVA_IDENTIFIER_TRANSFORMER = JavaIdentifierTransformer.NOOP;
   private static final DefaultValueProcessor DEFAULT_VALUE_PROCESSOR = new DefaultDefaultValueProcessor();
   private static final String[] EMPTY_EXCLUDES = new String[0];

   /** Array conversion mode */
   private int arrayMode = MODE_LIST;
   private MultiKeyMap beanKeyMap = new MultiKeyMap();
   private Map beanProcessorMap = new HashMap();
   private MultiKeyMap beanTypeMap = new MultiKeyMap();
   /** Map of attribute/class */
   private Map classMap;
   private Class collectionType = DEFAULT_COLLECTION_TYPE;
   private CycleDetectionStrategy cycleDetectionStrategy = DEFAULT_CYCLE_DETECTION_STRATEGY;
   private Map defaultValueMap = new HashMap();
   private DefaultValueProcessorMatcher defaultValueProcessorMatcher = DEFAULT_DEFAULT_VALUE_PROCESSOR_MATCHER;
   private Class enclosedType;
   private List eventListeners = new ArrayList();
   private String[] excludes = EMPTY_EXCLUDES;
   private Map exclusionMap = new HashMap();
   private boolean handleJettisonEmptyElement;
   private boolean handleJettisonSingleElementArray;
   private boolean ignoreDefaultExcludes;
   //private boolean ignoreJPATransient;
   private boolean ignoreTransientFields;
   private boolean ignorePublicFields = true;
   private boolean javascriptCompliant;
   private JavaIdentifierTransformer javaIdentifierTransformer = DEFAULT_JAVA_IDENTIFIER_TRANSFORMER;
   private PropertyFilter javaPropertyFilter;
   private Map javaPropertyNameProcessorMap = new HashMap();
   private PropertyNameProcessorMatcher javaPropertyNameProcessorMatcher = DEFAULT_PROPERTY_NAME_PROCESSOR_MATCHER;
   private JsonBeanProcessorMatcher jsonBeanProcessorMatcher = DEFAULT_JSON_BEAN_PROCESSOR_MATCHER;
   private PropertyFilter jsonPropertyFilter;
   private Map jsonPropertyNameProcessorMap = new HashMap();
   private PropertyNameProcessorMatcher jsonPropertyNameProcessorMatcher = DEFAULT_PROPERTY_NAME_PROCESSOR_MATCHER;
   private JsonValueProcessorMatcher jsonValueProcessorMatcher = DEFAULT_JSON_VALUE_PROCESSOR_MATCHER;
   private Map keyMap = new HashMap();
   private NewBeanInstanceStrategy newBeanInstanceStrategy = DEFAULT_NEW_BEAN_INSTANCE_STRATEGY;
   private PropertyExclusionClassMatcher propertyExclusionClassMatcher = DEFAULT_PROPERTY_EXCLUSION_CLASS_MATCHER;
   private PropertySetStrategy propertySetStrategy;
   /** Root class used when converting to an specific bean */
   private Class rootClass;
   private boolean skipJavaIdentifierTransformationInMapKeys;
   private boolean triggerEvents;
   private Map typeMap = new HashMap();
   private List ignoreFieldAnnotations = new ArrayList();
   private boolean allowNonStringKeys = false;

   public JsonConfig() {
   }

   /**
    * Registers a listener for JSON events.<br>
    * The events will be triggered only when using the static builders and if event triggering is
    * enabled.<br>
    * [Java -&gt; JSON]
    * 
    * @see #enableEventTriggering
    * @see #disableEventTriggering
    * @see #removeJsonEventListener(JsonEventListener)
    * @param listener a listener for events
    */
   public synchronized void addJsonEventListener( JsonEventListener listener ) {
      if( !eventListeners.contains( listener ) ) {
         eventListeners.add( listener );
      }
   }

   /**
    * Removes all registered PropertyNameProcessors.<br>
    * [JSON -&gt; Java]
    */
   public void clearJavaPropertyNameProcessors() {
      javaPropertyNameProcessorMap.clear();
   }

   /**
    * Removes all registered JsonBeanProcessors.<br>
    * [Java -&gt; JSON]
    */
   public void clearJsonBeanProcessors() {
      beanProcessorMap.clear();
   }

   /**
    * Removes all registered listener for JSON Events.<br>
    * [Java -&gt; JSON]
    */
   public synchronized void clearJsonEventListeners() {
      eventListeners.clear();
   }

   /**
    * Removes all registered PropertyNameProcessors.<br>
    * [Java -&gt; JSON]
    */
   public void clearJsonPropertyNameProcessors() {
      jsonPropertyNameProcessorMap.clear();
   }   
   
   /**
    * Removes all registered JsonValueProcessors.<br>
    * [Java -&gt; JSON]
    */
   public void clearJsonValueProcessors() {
      beanKeyMap.clear();
      beanTypeMap.clear();
      keyMap.clear();
      typeMap.clear();
   }
   
   /**
    * Removes all property exclusions registered per class.<br>
    * [Java -&gt; JSON]
    */
   public void clearPropertyExclusions() {
      exclusionMap.clear();
   }

   /**
    * Removes all registered PropertyNameProcessors.<br>
    * [JSON -&gt; Java]
    * 
    * @deprecated use clearJavaPropertyNameProcessors() instead
    */
   public void clearPropertyNameProcessors() {
      clearJavaPropertyNameProcessors();
   }

   public JsonConfig copy() {
      JsonConfig jsc = new JsonConfig();
      jsc.beanKeyMap.putAll( beanKeyMap );
      jsc.beanTypeMap.putAll( beanTypeMap );
      jsc.classMap = new HashMap();
      if( classMap != null ) {
         jsc.classMap.putAll( classMap );
      }
      jsc.cycleDetectionStrategy = cycleDetectionStrategy;
      if( eventListeners != null ) {
         jsc.eventListeners.addAll( eventListeners );
      }
      if( excludes != null ) {
         jsc.excludes = new String[excludes.length];
         System.arraycopy( excludes, 0, jsc.excludes, 0, excludes.length );
      }
      jsc.handleJettisonEmptyElement = handleJettisonEmptyElement;
      jsc.handleJettisonSingleElementArray = handleJettisonSingleElementArray;
      jsc.ignoreDefaultExcludes = ignoreDefaultExcludes;
      jsc.ignoreTransientFields = ignoreTransientFields;
      jsc.ignorePublicFields = ignorePublicFields;
      jsc.javaIdentifierTransformer = javaIdentifierTransformer;
      jsc.javascriptCompliant = javascriptCompliant;
      jsc.keyMap.putAll( keyMap );
      jsc.beanProcessorMap.putAll( beanProcessorMap );
      jsc.rootClass = rootClass;
      jsc.skipJavaIdentifierTransformationInMapKeys = skipJavaIdentifierTransformationInMapKeys;
      jsc.triggerEvents = triggerEvents;
      jsc.typeMap.putAll( typeMap );
      jsc.jsonPropertyFilter = jsonPropertyFilter;
      jsc.javaPropertyFilter = javaPropertyFilter;
      jsc.jsonBeanProcessorMatcher = jsonBeanProcessorMatcher;
      jsc.newBeanInstanceStrategy = newBeanInstanceStrategy;
      jsc.defaultValueProcessorMatcher = defaultValueProcessorMatcher;
      jsc.defaultValueMap.putAll( defaultValueMap );
      jsc.propertySetStrategy = propertySetStrategy;
      //jsc.ignoreJPATransient = ignoreJPATransient;
      jsc.collectionType = collectionType;
      jsc.enclosedType = enclosedType;
      jsc.jsonValueProcessorMatcher = jsonValueProcessorMatcher;
      jsc.javaPropertyNameProcessorMatcher = javaPropertyNameProcessorMatcher;
      jsc.javaPropertyNameProcessorMap.putAll( javaPropertyNameProcessorMap );
      jsc.jsonPropertyNameProcessorMatcher = jsonPropertyNameProcessorMatcher;
      jsc.jsonPropertyNameProcessorMap.putAll( jsonPropertyNameProcessorMap );
      jsc.propertyExclusionClassMatcher = propertyExclusionClassMatcher;
      jsc.exclusionMap.putAll(  exclusionMap );
      jsc.ignoreFieldAnnotations.addAll( ignoreFieldAnnotations );
      jsc.allowNonStringKeys = allowNonStringKeys;
      return jsc;
   }

   /**
    * Disables event triggering when building.<br>
    * [Java -&gt; JSON]
    */
   public void disableEventTriggering() {
      triggerEvents = false;
   }

   /**
    * Enables event triggering when building.<br>
    * [Java -&gt; JSON]
    */
   public void enableEventTriggering() {
      triggerEvents = true;
   }

   /**
    * Finds a DefaultValueProcessor registered to the target class.<br>
    * Returns null if none is registered.<br>
    * [Java -&gt; JSON]
    * 
    * @param target a class used for searching a DefaultValueProcessor.
    */
   public DefaultValueProcessor findDefaultValueProcessor( Class target ) {
      if( !defaultValueMap.isEmpty() ) {
         Object key = defaultValueProcessorMatcher.getMatch( target, defaultValueMap.keySet() );
         DefaultValueProcessor processor = (DefaultValueProcessor) defaultValueMap.get( key );
         if( processor != null ) {
            return processor;
         }
      }
      return DEFAULT_VALUE_PROCESSOR;
   }

   /**
    * Finds a PropertyNameProcessor registered to the target class.<br>
    * Returns null if none is registered.<br>
    * [JSON -&gt; Java]
    * 
    * @param propertyType a class used for searching a PropertyNameProcessor.
    */
   public PropertyNameProcessor findJavaPropertyNameProcessor( Class beanClass ) {
      if( !javaPropertyNameProcessorMap.isEmpty() ) {
         Object key = javaPropertyNameProcessorMatcher.getMatch( beanClass, javaPropertyNameProcessorMap.keySet() );
         return (PropertyNameProcessor) javaPropertyNameProcessorMap.get( key );

      }
      return null;
   }

   /**
    * Finds a JsonBeanProcessor registered to the target class.<br>
    * Returns null if none is registered.<br>
    * [Java -&gt; JSON]
    * 
    * @param target a class used for searching a JsonBeanProcessor.
    */
   public JsonBeanProcessor findJsonBeanProcessor( Class target ) {
      if( !beanProcessorMap.isEmpty() ) {
         Object key = jsonBeanProcessorMatcher.getMatch( target, beanProcessorMap.keySet() );
         return (JsonBeanProcessor) beanProcessorMap.get( key );
      }
      return null;
   }

   /**
    * Finds a PropertyNameProcessor registered to the target class.<br>
    * Returns null if none is registered.<br>
    * [Java -&gt; JSON]
    * 
    * @param propertyType a class used for searching a PropertyNameProcessor.
    */
   public PropertyNameProcessor findJsonPropertyNameProcessor( Class beanClass ) {
      if( !jsonPropertyNameProcessorMap.isEmpty() ) {
         Object key = jsonPropertyNameProcessorMatcher.getMatch( beanClass, jsonPropertyNameProcessorMap.keySet() );
         return (PropertyNameProcessor) jsonPropertyNameProcessorMap.get( key );

      }
      return null;
   }

   /**
    * Finds a JsonValueProcessor registered to the target type.<br>
    * Returns null if none is registered.<br>
    * [Java -&gt; JSON]
    * 
    * @param propertyType a class used for searching a JsonValueProcessor.
    */
   public JsonValueProcessor findJsonValueProcessor( Class propertyType ) {
      if( !typeMap.isEmpty() ) {
         Object key = jsonValueProcessorMatcher.getMatch( propertyType, typeMap.keySet() );
         return (JsonValueProcessor) typeMap.get( key );

      }
      return null;
   }

   /**
    * Finds a JsonValueProcessor.<br>
    * It will search the registered JsonValueProcessors in the following order:
    * <ol>
    * <li>beanClass, key</li>
    * <li>beanClass, type</li>
    * <li>key</li>
    * <li>type</li>
    * </ol>
    * Returns null if none is registered.<br>
    * [Java -&gt; JSON]
    * 
    * @param beanClass the class to which the property may belong
    * @param propertyType the type of the property
    * @param key the name of the property which may belong to the target class
    */
   public JsonValueProcessor findJsonValueProcessor( Class beanClass, Class propertyType, String key ) {
      JsonValueProcessor jsonValueProcessor = null;
      jsonValueProcessor = (JsonValueProcessor) beanKeyMap.get( beanClass, key );
      if( jsonValueProcessor != null ) {
         return jsonValueProcessor;
      }

      jsonValueProcessor = (JsonValueProcessor) beanTypeMap.get( beanClass, propertyType );
      if( jsonValueProcessor != null ) {
         return jsonValueProcessor;
      }

      jsonValueProcessor = (JsonValueProcessor) keyMap.get( key );
      if( jsonValueProcessor != null ) {
         return jsonValueProcessor;
      }

      Object tkey = jsonValueProcessorMatcher.getMatch( propertyType, typeMap.keySet() );
      jsonValueProcessor = (JsonValueProcessor) typeMap.get( tkey );
      if( jsonValueProcessor != null ) {
         return jsonValueProcessor;
      }

      return null;
   }

   /**
    * Finds a JsonValueProcessor.<br>
    * It will search the registered JsonValueProcessors in the following order:
    * <ol>
    * <li>key</li>
    * <li>type</li>
    * </ol>
    * Returns null if none is registered.<br>
    * [Java -&gt; JSON]
    * 
    * @param propertyType the type of the property
    * @param key the name of the property which may belong to the target class
    */
   public JsonValueProcessor findJsonValueProcessor( Class propertyType, String key ) {
      JsonValueProcessor jsonValueProcessor = null;
      jsonValueProcessor = (JsonValueProcessor) keyMap.get( key );
      if( jsonValueProcessor != null ) {
         return jsonValueProcessor;
      }

      Object tkey = jsonValueProcessorMatcher.getMatch( propertyType, typeMap.keySet() );
      jsonValueProcessor = (JsonValueProcessor) typeMap.get( tkey );
      if( jsonValueProcessor != null ) {
         return jsonValueProcessor;
      }

      return null;
   }
   
   /**
    * Finds a PropertyNameProcessor registered to the target class.<br>
    * Returns null if none is registered. <br>
    * [JSON -&gt; Java]
    * 
    * @param propertyType a class used for searching a PropertyNameProcessor.
    * 
    * @deprecated use findJavaPropertyNameProcessor() instead
    */
   public PropertyNameProcessor findPropertyNameProcessor( Class beanClass ) {
      return findJavaPropertyNameProcessor( beanClass );
   }   
   
   /**
    * Returns the current array mode conversion.<br>
    * [JSON -&gt; Java]
    * 
    * @return MODE_OBJECT_ARRAY, MODE_LIST or MODE_SET
    */
   public int getArrayMode() {
      return arrayMode;
   }

   /**
    * Returns the current attribute/class Map.<br>
    * [JSON -&gt; Java]
    * 
    * @return a Map of classes, every key identifies a property or a regexp
    */
   public Map getClassMap() {
      return classMap;
   }

   /**
    * Returns the current collection type used for collection transformations.<br>
    * [JSON -&gt; Java]
    * 
    * @return the target collection class for conversion
    */
   public Class getCollectionType() {
      return collectionType;
   }

   /**
    * Returns the configured CycleDetectionStrategy.<br>
    * Default value is CycleDetectionStrategy.STRICT<br>
    * [Java -&gt; JSON]
    */
   public CycleDetectionStrategy getCycleDetectionStrategy() {
      return cycleDetectionStrategy;
   }

   /**
    * Returns the configured DefaultValueProcessorMatcher.<br>
    * Default value is DefaultValueProcessorMatcher.DEFAULT<br>
    * [Java -&gt; JSON]
    */
   public DefaultValueProcessorMatcher getDefaultValueProcessorMatcher() {
      return defaultValueProcessorMatcher;
   }

   /**
    * Returns the current enclosed type for generic collection transformations.<br>
    * [JSON -&gt; Java]
    * 
    * @return the target type for conversion
    */
   public Class getEnclosedType() {
      return enclosedType;
   }

   /**
    * Returns the configured properties for exclusion. <br>
    * [Java -&gt; JSON]
    */
   public String[] getExcludes() {
      return excludes;
   }

   /**
    * Returns the configured JavaIdentifierTransformer. <br>
    * Default value is JavaIdentifierTransformer.NOOP<br>
    * [JSON -&gt; Java]
    */
   public JavaIdentifierTransformer getJavaIdentifierTransformer() {
      return javaIdentifierTransformer;
   }

   /**
    * Returns the configured property filter when serializing to Java.<br>
    * [JSON -&gt; Java]
    */
   public PropertyFilter getJavaPropertyFilter() {
      return javaPropertyFilter;
   }

   /**
    * Returns the configured PropertyNameProcessorMatcher.<br>
    * Default value is PropertyNameProcessorMatcher.DEFAULT<br>
    * [JSON -&gt; Java]
    */
   public PropertyNameProcessorMatcher getJavaPropertyNameProcessorMatcher() {
      return javaPropertyNameProcessorMatcher;
   }
   
   /**
    * Returns the configured JsonBeanProcessorMatcher.<br>
    * Default value is JsonBeanProcessorMatcher.DEFAULT<br>
    * [JSON -&gt; Java]
    */
   public JsonBeanProcessorMatcher getJsonBeanProcessorMatcher() {
      return jsonBeanProcessorMatcher;
   }

   /**
    * Returns a list of registered listeners for JSON events.<br>
    * [JSON -&gt; Java]
    */
   public synchronized List getJsonEventListeners() {
      return eventListeners;
   }

   /**
    * Returns the configured property filter when serializing to JSON.<br>
    * [Java -&gt; JSON]
    */
   public PropertyFilter getJsonPropertyFilter() {
      return jsonPropertyFilter;
   }

   /**
    * Returns the configured PropertyNameProcessorMatcher.<br>
    * Default value is PropertyNameProcessorMatcher.DEFAULT<br>
    * [Java -&gt; JSON]
    */
   public PropertyNameProcessorMatcher getJsonPropertyNameProcessorMatcher() {
      return javaPropertyNameProcessorMatcher;
   }

   /**
    * Returns the configured JsonValueProcessorMatcher.<br>
    * Default value is JsonValueProcessorMatcher.DEFAULT<br>
    * [Java -&gt; JSON]
    */
   public JsonValueProcessorMatcher getJsonValueProcessorMatcher() {
      return jsonValueProcessorMatcher;
   }

   /**
    * Returns a set of default excludes with user-defined excludes.<br>
    * [Java -&gt; JSON]
    */
   public Collection getMergedExcludes() {
      Collection exclusions = new HashSet();
      for( int i = 0; i < excludes.length; i++ ) {
         String exclusion = excludes[i];
         if( !StringUtils.isBlank( excludes[i] ) ) {
            exclusions.add( exclusion.trim() );
         }
      }

      if( !ignoreDefaultExcludes ) {
         for( int i = 0; i < DEFAULT_EXCLUDES.length; i++ ) {
            if( !exclusions.contains( DEFAULT_EXCLUDES[i] ) ) {
               exclusions.add( DEFAULT_EXCLUDES[i] );
            }
         }
      }

      return exclusions;
   }
   
   /**
    * Returns a set of default excludes with user-defined excludes.<br>
    * Takes into account any additional excludes per matching class.
    * [Java -&gt; JSON]
    */
   public Collection getMergedExcludes( Class target ) {
      if( target == null ) {
         return getMergedExcludes();
      }

      Collection exclusionSet = getMergedExcludes();
      if( !exclusionMap.isEmpty() ) {
         Object key = propertyExclusionClassMatcher.getMatch( target, exclusionMap.keySet() );
         Set set = (Set) exclusionMap.get( key );
         if( set != null && !set.isEmpty() ) {
            for( Iterator i = set.iterator(); i.hasNext(); ) {
               Object e = i.next();
               if( !exclusionSet.contains( e ) ) {
                  exclusionSet.add( e );
               }
            }
         }
      }

      return exclusionSet;
   }

   /**
    * Returns the configured NewBeanInstanceStrategy.<br>
    * Default value is NewBeanInstanceStrategy.DEFAULT<br>
    * [JSON -&gt; Java]
    */
   public NewBeanInstanceStrategy getNewBeanInstanceStrategy() {
      return newBeanInstanceStrategy;
   }
   
   /**
    * Returns the configured PropertyExclusionClassMatcher.<br>
    * Default value is PropertyExclusionClassMatcher.DEFAULT<br>
    * [JSON -&gt; Java]
    */
   public PropertyExclusionClassMatcher getPropertyExclusionClassMatcher() {
      return propertyExclusionClassMatcher;
   }
   
   /**
    * Returns the configured PropertyNameProcessorMatcher.<br>
    * Default value is PropertyNameProcessorMatcher.DEFAULT<br>
    * [JSON -&gt; Java]
    * 
    * @deprecated use getJavaPropertyNameProcessorMatcher() instead
    */
   public PropertyNameProcessorMatcher getPropertyNameProcessorMatcher() {
      return getJavaPropertyNameProcessorMatcher();
   }

   /**
    * Returns the configured PropertySetStrategy.<br>
    * Default value is PropertySetStrategy.DEFAULT<br>
    * [JSON -&gt; Java]
    */
   public PropertySetStrategy getPropertySetStrategy() {
      return propertySetStrategy;
   }

   /**
    * Returns the current root Class.<br>
    * [JSON -&gt; Java]
    * 
    * @return the target class for conversion
    */
   public Class getRootClass() {
      return rootClass;
   }

   /**
    * Returns true if non-String keys are allowed on JSONObject.<br>
    * Default value is false<br>
    * [Java -&gt; JSON]
    */
   public boolean isAllowNonStringKeys() {
      return allowNonStringKeys;
   }
   
   /**
    * Returns true if event triggering is enabled during building.<br>
    * Default value is false<br>
    * [Java -&gt; JSON]
    */
   public boolean isEventTriggeringEnabled() {
      return triggerEvents;
   }

   /**
    * Returns true if this Jettison convention will be handled when converting to Java.<br>
    * Jettison assumes that "" (empty string) can be assigned to empty elements (objects), which
    * clearly violates the JSON spec.<br>
    * [JSON -&gt; Java]
    */
   public boolean isHandleJettisonEmptyElement() {
      return handleJettisonEmptyElement;
   }

   /**
    * Returns true if this jettison convention will be handled when converting to Java.<br>
    * Jettison states the following JSON {'media':{'title':'hello'}} can be set as a single element
    * JSONArray (media is the array).<br>
    * [JSON -&gt; Java]
    */
   public boolean isHandleJettisonSingleElementArray() {
      return handleJettisonSingleElementArray;
   }

   /**
    * Returns true if default excludes will not be used.<br>
    * Default value is false.<br>
    * [Java -&gt; JSON]
    */
   public boolean isIgnoreDefaultExcludes() {
      return ignoreDefaultExcludes;
   }

   /**
    * Returns true if JPA Transient annotated methods should be ignored.<br>
    * Default value is false.<br>
    * [Java -&gt; JSON]
    */
   public boolean isIgnoreJPATransient() {
      return ignoreFieldAnnotations.contains("javax.persistence.Transient");
   }

   /**
    * Returns true if transient fields of a bean will be ignored.<br>
    * Default value is false.<br>
    * [Java -&gt; JSON]
    */
   public boolean isIgnoreTransientFields() {
      return ignoreTransientFields;
   }
   
   /**
    * Returns true if public fields of a bean will be ignored.<br>
    * Default value is true.<br>
    * [Java -&gt; JSON]
    */
   public boolean isIgnorePublicFields() {
      return ignorePublicFields;
   }
   
   /**
    * Returns true if Javascript compatibility is turned on.<br>
    * Default value is false.<br>
    * [Java -&gt; JSON]
    */
   public boolean isJavascriptCompliant() {
      return javascriptCompliant;
   }

   /**
    * Returns true if map keys will not be transformed.<br>
    * Default value is false.<br>
    * [JSON -&gt; Java]
    */
   public boolean isSkipJavaIdentifierTransformationInMapKeys() {
      return skipJavaIdentifierTransformationInMapKeys;
   }

   /**
    * Registers a DefaultValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param target the class to use as key
    * @param defaultValueProcessor the processor to register
    */
   public void registerDefaultValueProcessor( Class target, DefaultValueProcessor defaultValueProcessor ) {
      if( target != null && defaultValueProcessor != null ) {
         defaultValueMap.put( target, defaultValueProcessor );
      }
   }

   /**
    * Registers a PropertyNameProcessor.<br>
    * [JSON -&gt; Java]
    * 
    * @param target the class to use as key
    * @param propertyNameProcessor the processor to register
    */
   public void registerJavaPropertyNameProcessor( Class target, PropertyNameProcessor propertyNameProcessor ) {
      if( target != null && propertyNameProcessor != null ) {
         javaPropertyNameProcessorMap.put( target, propertyNameProcessor );
      }
   }
   
   /**
    * Registers a JsonBeanProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param target the class to use as key
    * @param jsonBeanProcessor the processor to register
    */
   public void registerJsonBeanProcessor( Class target, JsonBeanProcessor jsonBeanProcessor ) {
      if( target != null && jsonBeanProcessor != null ) {
         beanProcessorMap.put( target, jsonBeanProcessor );
      }
   }

   /**
    * Registers a PropertyNameProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param target the class to use as key
    * @param propertyNameProcessor the processor to register
    */
   public void registerJsonPropertyNameProcessor( Class target, PropertyNameProcessor propertyNameProcessor ) {
      if( target != null && propertyNameProcessor != null ) {
         jsonPropertyNameProcessorMap.put( target, propertyNameProcessor );
      }
   }

   /**
    * Registers a JsonValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param beanClass the class to use as key
    * @param propertyType the property type to use as key
    * @param jsonValueProcessor the processor to register
    */
   public void registerJsonValueProcessor( Class beanClass, Class propertyType, JsonValueProcessor jsonValueProcessor ) {
      if( beanClass != null && propertyType != null && jsonValueProcessor != null ) {
         beanTypeMap.put( beanClass, propertyType, jsonValueProcessor );
      }
   }

   /**
    * Registers a JsonValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param propertyType the property type to use as key
    * @param jsonValueProcessor the processor to register
    */
   public void registerJsonValueProcessor( Class propertyType, JsonValueProcessor jsonValueProcessor ) {
      if( propertyType != null && jsonValueProcessor != null ) {
         typeMap.put( propertyType, jsonValueProcessor );
      }
   }

   /**
    * Registers a JsonValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param beanClass the class to use as key
    * @param key the property name to use as key
    * @param jsonValueProcessor the processor to register
    */
   public void registerJsonValueProcessor( Class beanClass, String key, JsonValueProcessor jsonValueProcessor ) {
      if( beanClass != null && key != null && jsonValueProcessor != null ) {
         beanKeyMap.put( beanClass, key, jsonValueProcessor );
      }
   }

   /**
    * Registers a JsonValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param key the property name to use as key
    * @param jsonValueProcessor the processor to register
    */
   public void registerJsonValueProcessor( String key, JsonValueProcessor jsonValueProcessor ) {
      if( key != null && jsonValueProcessor != null ) {
         keyMap.put( key, jsonValueProcessor );
      }
   }
   
   /**
    * Registers a exclusion for a target class.<br>
    * [Java -&gt; JSON]
    * 
    * @param target the class to use as key
    * @param propertyName the property to be excluded
    */
   public void registerPropertyExclusion( Class target, String propertyName ) {
      if( target != null && propertyName != null ) {
         Set set = (Set) exclusionMap.get( target );
         if( set == null ){
            set = new HashSet();
            exclusionMap.put(  target, set );
         }
         if( !set.contains( propertyName )){
            set.add(propertyName );
         }
      }
   }
   
   /**
    * Registers exclusions for a target class.<br>
    * [Java -&gt; JSON]
    * 
    * @param target the class to use as key
    * @param properties the properties to be excluded
    */
   public void registerPropertyExclusions( Class target, String[] properties ) {
      if( target != null && properties != null && properties.length > 0 ) {
         Set set = (Set) exclusionMap.get( target );
         if( set == null ) {
            set = new HashSet();
            exclusionMap.put( target, set );
         }
         for( int i = 0; i < properties.length; i++ ) {
            if( !set.contains( properties[i] ) ) {
               set.add( properties[i] );
            }
         }
      }
   }
   
   /**
    * Registers a PropertyNameProcessor.<br>
    * [JSON -&gt; Java]
    * 
    * @param target the class to use as key
    * @param propertyNameProcessor the processor to register
    * 
    * @deprecated use registerJavaPropertyNameProcessor() instead
    */
   public void registerPropertyNameProcessor( Class target, PropertyNameProcessor propertyNameProcessor ) {
      registerJavaPropertyNameProcessor( target, propertyNameProcessor );
   }

   /**
    * Removes a listener for JSON events.<br>
    * [Java -&gt; JSON]
    * 
    * @see #addJsonEventListener(JsonEventListener)
    * @param listener a listener for events
    */
   public synchronized void removeJsonEventListener( JsonEventListener listener ) {
      eventListeners.remove( listener );
   }

   /**
    * Resets all values to its default state.
    */
   public void reset() {
      excludes = EMPTY_EXCLUDES;
      ignoreDefaultExcludes = false;
      ignoreTransientFields = false;
      ignorePublicFields = true;
      javascriptCompliant = false;
      javaIdentifierTransformer = DEFAULT_JAVA_IDENTIFIER_TRANSFORMER;
      cycleDetectionStrategy = DEFAULT_CYCLE_DETECTION_STRATEGY;
      skipJavaIdentifierTransformationInMapKeys = false;
      triggerEvents = false;
      handleJettisonEmptyElement = false;
      handleJettisonSingleElementArray = false;
      arrayMode = MODE_LIST;
      rootClass = null;
      classMap = null;
      keyMap.clear();
      typeMap.clear();
      beanKeyMap.clear();
      beanTypeMap.clear();
      jsonPropertyFilter = null;
      javaPropertyFilter = null;
      jsonBeanProcessorMatcher = DEFAULT_JSON_BEAN_PROCESSOR_MATCHER;
      newBeanInstanceStrategy = DEFAULT_NEW_BEAN_INSTANCE_STRATEGY;
      defaultValueProcessorMatcher = DEFAULT_DEFAULT_VALUE_PROCESSOR_MATCHER;
      defaultValueMap.clear();
      propertySetStrategy = null/* DEFAULT_PROPERTY_SET_STRATEGY */;
      //ignoreJPATransient = false;
      collectionType = DEFAULT_COLLECTION_TYPE;
      enclosedType = null;
      jsonValueProcessorMatcher = DEFAULT_JSON_VALUE_PROCESSOR_MATCHER;
      javaPropertyNameProcessorMap.clear();
      javaPropertyNameProcessorMatcher = DEFAULT_PROPERTY_NAME_PROCESSOR_MATCHER;
      jsonPropertyNameProcessorMap.clear();
      jsonPropertyNameProcessorMatcher = DEFAULT_PROPERTY_NAME_PROCESSOR_MATCHER;
      beanProcessorMap.clear();
      propertyExclusionClassMatcher = DEFAULT_PROPERTY_EXCLUSION_CLASS_MATCHER;
      exclusionMap.clear();
      ignoreFieldAnnotations.clear();
      allowNonStringKeys = false;
   }

   /**
    * Sets if non-String keys are allowed on JSONObject.<br>
    * [Java -&gt; JSON]
    */
   public void setAllowNonStringKeys( boolean allowNonStringKeys ) {
      this.allowNonStringKeys = allowNonStringKeys;
   }
   
   /**
    * Sets the current array mode for conversion.<br>
    * If the value is not MODE_LIST, MODE_OBJECT_ARRAY nor MODE_SET, then MODE_LIST will be used.<br>
    * [JSON -&gt; Java]
    * 
    * @param arrayMode array mode for conversion
    */
   public void setArrayMode( int arrayMode ) {
      if( arrayMode == MODE_OBJECT_ARRAY ) {
         this.arrayMode = arrayMode;
      } else if( arrayMode == MODE_SET ) {
         this.arrayMode = arrayMode;
         this.collectionType = Set.class;
      } else {
         this.arrayMode = MODE_LIST;
         this.enclosedType = DEFAULT_COLLECTION_TYPE;
      }
   }

   /**
    * Sets the current attribute/Class Map<br>
    * [JSON -&gt; Java]
    * 
    * @param classMap a Map of classes, every key identifies a property or a regexp
    */
   public void setClassMap( Map classMap ) {
      this.classMap = classMap;
   }

   /**
    * Sets the current collection type used for collection transformations.<br>
    * [JSON -&gt; Java]
    * 
    * @param collectionType the target collection class for conversion
    */
   public void setCollectionType( Class collectionType ) {
      if( collectionType != null ) {
         if( !Collection.class.isAssignableFrom( collectionType ) ) {
            throw new JSONException( "The configured collectionType is not a Collection: " + collectionType.getName() );
         }
         this.collectionType = collectionType;
      } else {
         collectionType = DEFAULT_COLLECTION_TYPE;
      }
   }

   /**
    * Sets a CycleDetectionStrategy to use.<br>
    * Will set default value (CycleDetectionStrategy.STRICT) if null.<br>
    * [Java -&gt; JSON]
    */
   public void setCycleDetectionStrategy( CycleDetectionStrategy cycleDetectionStrategy ) {
      this.cycleDetectionStrategy = cycleDetectionStrategy == null ? DEFAULT_CYCLE_DETECTION_STRATEGY
            : cycleDetectionStrategy;
   }

   /**
    * Sets a DefaultValueProcessorMatcher to use.<br>
    * Will set default value (DefaultValueProcessorMatcher.DEFAULT) if null.<br>
    * [Java -&gt; JSON]
    */
   public void setDefaultValueProcessorMatcher( DefaultValueProcessorMatcher defaultValueProcessorMatcher ) {
      this.defaultValueProcessorMatcher = defaultValueProcessorMatcher == null ? DEFAULT_DEFAULT_VALUE_PROCESSOR_MATCHER
            : defaultValueProcessorMatcher;
   }

   /**
    * Sets the current enclosed type for generic collection transformations.<br>
    * [JSON -&gt; Java]
    * 
    * @param enclosedType the target type for conversion
    */
   public void setEnclosedType( Class enclosedType ) {
      this.enclosedType = enclosedType;
   }

   /**
    * Sets the excludes to use.<br>
    * Will set default value ([]) if null.<br>
    * [Java -&gt; JSON]
    */
   public void setExcludes( String[] excludes ) {
      this.excludes = excludes == null ? EMPTY_EXCLUDES : excludes;
   }

   /**
    * Activate/Deactivate handling this jettison convention when converting to Java.<br>
    * Jettison states that "" (empty string) can be assigned to empty elements (objects), which
    * clearly violates the JSON spec.<br>
    * [JSON -&gt; Java]
    */
   public void setHandleJettisonEmptyElement( boolean handleJettisonEmptyElement ) {
      this.handleJettisonEmptyElement = handleJettisonEmptyElement;
   }

   /**
    * Activate/Deactivate handling this jettison convention when converting to Java.<br> * Jettison
    * states the following JSON {'media':{'title':'hello'}} can be set as a single element JSONArray
    * (media is the array).<br>
    * [JSON -&gt; Java]
    */
   public void setHandleJettisonSingleElementArray( boolean handleJettisonSingleElementArray ) {
      this.handleJettisonSingleElementArray = handleJettisonSingleElementArray;
   }

   /**
    * Sets if default excludes would be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public void setIgnoreDefaultExcludes( boolean ignoreDefaultExcludes ) {
      this.ignoreDefaultExcludes = ignoreDefaultExcludes;
   }

   /**
    * Sets if JPA Transient annotated methods would be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public void setIgnoreJPATransient( boolean ignoreJPATransient ) {
      if(ignoreJPATransient) {
         addIgnoreFieldAnnotation("javax.persistence.Transient");
      } else {
         removeIgnoreFieldAnnotation("javax.persistence.Transient");
      }
   }
   
   /**
    * Adds an annotation that marks a field to be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public void addIgnoreFieldAnnotation( String annotationClassName ) {
      if( annotationClassName != null && !ignoreFieldAnnotations.contains( annotationClassName )) {
         ignoreFieldAnnotations.add(annotationClassName);
      }
   }
   
   /**
    * Adds an annotation that marks a field to be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public void removeIgnoreFieldAnnotation( String annotationClassName ) {
      if( annotationClassName != null ) ignoreFieldAnnotations.remove(annotationClassName);
   }

   /**
    * Removes an annotation that marks a field to be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public void addIgnoreFieldAnnotation( Class annotationClass ) {
      if( annotationClass != null && !ignoreFieldAnnotations.contains( annotationClass.getName() )) {
         ignoreFieldAnnotations.add(annotationClass.getName());
      }
   }
   
   /**
    * Removes an annotation that marks a field to be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public void removeIgnoreFieldAnnotation( Class annotationClass ) {
      if( annotationClass != null ) ignoreFieldAnnotations.remove(annotationClass.getName());
   }
   
   /**
    * Returns a List of all annotations that mark a field to be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public List getIgnoreFieldAnnotations() {
      return Collections.unmodifiableList(ignoreFieldAnnotations);
   }
   
   /**
    * Sets if transient fields would be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public void setIgnoreTransientFields( boolean ignoreTransientFields ) {
      this.ignoreTransientFields = ignoreTransientFields;
   }

   /**
    * Sets if public fields would be skipped when building.<br>
    * [Java -&gt; JSON]
    */
   public void setIgnorePublicFields( boolean ignorePublicFields ) {
      this.ignorePublicFields = ignorePublicFields;
   }
   
   /**
    * Sets if Javascript compatibility is enabled when building.<br>
    * [Java -&gt; JSON]
    */
   public void setJavascriptCompliant( boolean javascriptCompliant ) {
      this.javascriptCompliant = javascriptCompliant;
   }
   
   /**
    * Sets the JavaIdentifierTransformer to use.<br>
    * Will set default value (JavaIdentifierTransformer.NOOP) if null.<br>
    * [JSON -&gt; Java]
    */
   public void setJavaIdentifierTransformer( JavaIdentifierTransformer javaIdentifierTransformer ) {
      this.javaIdentifierTransformer = javaIdentifierTransformer == null ? DEFAULT_JAVA_IDENTIFIER_TRANSFORMER
            : javaIdentifierTransformer;
   }

   /**
    * Sets a property filter used when serializing to Java.<br>
    * [JSON -&gt; Java]
    * 
    * @param javaPropertyFilter the property filter
    */
   public void setJavaPropertyFilter( PropertyFilter javaPropertyFilter ) {
      this.javaPropertyFilter = javaPropertyFilter;
   }

   /**
    * Sets a PropertyNameProcessorMatcher to use.<br>
    * Will set default value (PropertyNameProcessorMatcher.DEFAULT) if null.<br>
    * [JSON -&gt; Java]
    */
   public void setJavaPropertyNameProcessorMatcher( PropertyNameProcessorMatcher propertyNameProcessorMatcher ) {
      this.javaPropertyNameProcessorMatcher = propertyNameProcessorMatcher == null ? DEFAULT_PROPERTY_NAME_PROCESSOR_MATCHER
            : propertyNameProcessorMatcher;
   }

   /**
    * Sets a JsonBeanProcessorMatcher to use.<br>
    * Will set default value (JsonBeanProcessorMatcher.DEFAULT) if null.<br>
    * [Java -&gt; JSON]
    */
   public void setJsonBeanProcessorMatcher( JsonBeanProcessorMatcher jsonBeanProcessorMatcher ) {
      this.jsonBeanProcessorMatcher = jsonBeanProcessorMatcher == null ? DEFAULT_JSON_BEAN_PROCESSOR_MATCHER
            : jsonBeanProcessorMatcher;
   }

   /**
    * Sets a property filter used when serializing to JSON.<br>
    * [Java -&gt; JSON]
    * 
    * @param jsonPropertyFilter the property filter
    */
   public void setJsonPropertyFilter( PropertyFilter jsonPropertyFilter ) {
      this.jsonPropertyFilter = jsonPropertyFilter;
   }

   /**
    * Sets a PropertyNameProcessorMatcher to use.<br>
    * Will set default value (PropertyNameProcessorMatcher.DEFAULT) if null.<br>
    * [Java -&gt; JSON]
    */
   public void setJsonPropertyNameProcessorMatcher( PropertyNameProcessorMatcher propertyNameProcessorMatcher ) {
      this.jsonPropertyNameProcessorMatcher = propertyNameProcessorMatcher == null ? DEFAULT_PROPERTY_NAME_PROCESSOR_MATCHER
            : propertyNameProcessorMatcher;
   }

   /**
    * Sets a JsonValueProcessorMatcher to use.<br>
    * Will set default value (JsonValueProcessorMatcher.DEFAULT) if null.<br>
    * [Java -&gt; JSON]
    */
   public void setJsonValueProcessorMatcher( JsonValueProcessorMatcher jsonValueProcessorMatcher ) {
      this.jsonValueProcessorMatcher = jsonValueProcessorMatcher == null ? DEFAULT_JSON_VALUE_PROCESSOR_MATCHER
            : jsonValueProcessorMatcher;
   }
   
   /**
    * Sets the NewBeanInstanceStrategy to use.<br>
    * Will set default value (NewBeanInstanceStrategy.DEFAULT) if null.<br>
    * [JSON -&gt; Java]
    */
   public void setNewBeanInstanceStrategy( NewBeanInstanceStrategy newBeanInstanceStrategy ) {
      this.newBeanInstanceStrategy = newBeanInstanceStrategy == null ? DEFAULT_NEW_BEAN_INSTANCE_STRATEGY
            : newBeanInstanceStrategy;
   }
   
   /**
    * Sets a PropertyExclusionClassMatcher to use.<br>
    * Will set default value (PropertyExclusionClassMatcher.DEFAULT) if null.<br>
    * [Java -&gt; JSON]
    */
   public void setPropertyExclusionClassMatcher( PropertyExclusionClassMatcher propertyExclusionClassMatcher ) {
      this.propertyExclusionClassMatcher = propertyExclusionClassMatcher == null ? DEFAULT_PROPERTY_EXCLUSION_CLASS_MATCHER
            : propertyExclusionClassMatcher;
   }
   
   /**
    * Sets a PropertyNameProcessorMatcher to use.<br>
    * Will set default value (PropertyNameProcessorMatcher.DEFAULT) if null.<br>
    * [JSON -&gt; Java]
    * 
    * @deprecated use setJavaPropertyNameProcessorMatcher() instead
    */
   public void setPropertyNameProcessorMatcher( PropertyNameProcessorMatcher propertyNameProcessorMatcher ) {
      setJavaPropertyNameProcessorMatcher( propertyNameProcessorMatcher );
   }

   /**
    * Sets a PropertySetStrategy to use.<br>
    * Will set default value (PropertySetStrategy.DEFAULT) if null.<br>
    * [JSON -&gt; Java]
    */
   public void setPropertySetStrategy( PropertySetStrategy propertySetStrategy ) {
      this.propertySetStrategy = propertySetStrategy;
   }

   /**
    * Sets the current root Class.<br>
    * [JSON -&gt; Java]
    * 
    * @param rootClass the target class for conversion
    */
   public void setRootClass( Class rootClass ) {
      this.rootClass = rootClass;
   }

   /**
    * Sets if property name as JavaIndetifier transformations would be skipped.<br>
    * [JSON -&gt; Java]
    */
   public void setSkipJavaIdentifierTransformationInMapKeys( boolean skipJavaIdentifierTransformationInMapKeys ) {
      this.skipJavaIdentifierTransformationInMapKeys = skipJavaIdentifierTransformationInMapKeys;
   }

   /**
    * Removes a DefaultValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param target a class used for searching a DefaultValueProcessor.
    */
   public void unregisterDefaultValueProcessor( Class target ) {
      if( target != null ) {
         defaultValueMap.remove( target );
      }
   }

   /**
    * Removes a PropertyNameProcessor.<br>
    * [JSON -&gt; Java]
    * 
    * @param target a class used for searching a PropertyNameProcessor.
    */
   public void unregisterJavaPropertyNameProcessor( Class target ) {
      if( target != null ) {
         javaPropertyNameProcessorMap.remove( target );
      }
   }
   
   /**
    * Removes a JsonBeanProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param target a class used for searching a JsonBeanProcessor.
    */
   public void unregisterJsonBeanProcessor( Class target ) {
      if( target != null ) {
         beanProcessorMap.remove( target );
      }
   }

   /**
    * Removes a PropertyNameProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param target a class used for searching a PropertyNameProcessor.
    */
   public void unregisterJsonPropertyNameProcessor( Class target ) {
      if( target != null ) {
         jsonPropertyNameProcessorMap.remove( target );
      }
   }

   /**
    * Removes a JsonValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param propertyType a class used for searching a JsonValueProcessor.
    */
   public void unregisterJsonValueProcessor( Class propertyType ) {
      if( propertyType != null ) {
         typeMap.remove( propertyType );
      }
   }

   /**
    * Removes a JsonValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param beanClass the class to which the property may belong
    * @param propertyType the type of the property
    */
   public void unregisterJsonValueProcessor( Class beanClass, Class propertyType ) {
      if( beanClass != null && propertyType != null ) {
         beanTypeMap.remove( beanClass, propertyType );
      }
   }

   /**
    * Removes a JsonValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param beanClass the class to which the property may belong
    * @param key the name of the property which may belong to the target class
    */
   public void unregisterJsonValueProcessor( Class beanClass, String key ) {
      if( beanClass != null && key != null ) {
         beanKeyMap.remove( beanClass, key );
      }
   }

   /**
    * Removes a JsonValueProcessor.<br>
    * [Java -&gt; JSON]
    * 
    * @param key the name of the property which may belong to the target class
    */
   public void unregisterJsonValueProcessor( String key ) {
      if( key != null ) {
         keyMap.remove( key );
      }
   }

   /**
    * Removes a property exclusion assigned to the target class.<br>
    * [Java -&gt; JSON]
    * 
    * @param target a class used for searching property exclusions.
    * @param propertyName the name of the property to be removed from the exclusion list.
    */
   public void unregisterPropertyExclusion( Class target, String propertyName ) {
      if( target != null && propertyName != null ) {
         Set set = (Set) exclusionMap.get( target );
         if( set == null ) {
            set = new HashSet();
            exclusionMap.put( target, set );
         }
         set.remove( propertyName );
      }
   }
   
   /**
    * Removes all property exclusions assigned to the target class.<br>
    * [Java -&gt; JSON]
    * 
    * @param target a class used for searching property exclusions.
    */
   public void unregisterPropertyExclusions( Class target ) {
      if( target != null ) {
         Set set = (Set) exclusionMap.get( target );
         if( set != null ) {
            set.clear();
         }
      }
   }
   
   /**
    * Removes a PropertyNameProcessor.<br>
    * [JSON -&gt; Java]
    * 
    * @param target a class used for searching a PropertyNameProcessor.
    * 
    * @deprecated use unregisterJavaPropertyNameProcessor() instead
    */
   public void unregisterPropertyNameProcessor( Class target ) {
      unregisterJavaPropertyNameProcessor( target );
   }
}