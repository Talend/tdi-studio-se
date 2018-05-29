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

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.ezmorph.Morpher;
import net.sf.ezmorph.array.ObjectArrayMorpher;
import net.sf.ezmorph.bean.BeanMorpher;
import net.sf.ezmorph.object.IdentityObjectMorpher;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonBeanProcessor;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.processors.JsonVerifier;
import net.sf.json.processors.PropertyNameProcessor;
import net.sf.json.regexp.RegexpUtils;
import net.sf.json.util.CycleDetectionStrategy;
import net.sf.json.util.EnumMorpher;
import net.sf.json.util.JSONTokener;
import net.sf.json.util.JSONUtils;
import net.sf.json.util.PropertyFilter;
import net.sf.json.util.PropertySetStrategy;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its external
 * form is a string wrapped in curly braces with colons between the names and
 * values, and commas between the values and names. The internal form is an
 * object having <code>get</code> and <code>opt</code> methods for accessing
 * the values by name, and <code>put</code> methods for adding or replacing
 * values by name. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONNull</code> object. A JSONObject
 * constructor can be used to convert an external form JSON text into an
 * internal form whose values can be retrieved with the <code>get</code> and
 * <code>opt</code> methods, or to convert values into a JSON text using the
 * <code>element</code> and <code>toString</code> methods. A
 * <code>get</code> method returns a value if one can be found, and throws an
 * exception if one cannot be found. An <code>opt</code> method returns a
 * default value instead of throwing an exception, and so is useful for
 * obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and
 * type coercion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example,
 *
 * <pre>
 *     myString = new JSONObject().put("JSON", "Hello, World!").toString();</pre>
 *
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules. The constructors are more forgiving in the texts they
 * will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a
 * quote or single quote, and if they do not contain leading or trailing spaces,
 * and if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>,
 * or <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as
 * by <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small>
 * as well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 * <code>0x-</code> <small>(hex)</small> prefix.</li>
 * <li>Comments written in the slashshlash, slashstar, and hash conventions
 * will be ignored.</li>
 * </ul>
 *
 * @author JSON.org
 */
public final class JSONObject extends AbstractJSON implements JSON, Map<String,Object>, Comparable {

   private static final Log log = LogFactory.getLog( JSONObject.class );

   /**
    * Creates a JSONObject.<br>
    * Inspects the object type to call the correct JSONObject factory method.
    * Accepts JSON formatted strings, Maps, DynaBeans and JavaBeans.
    *
    * @param object
    * @throws JSONException if the object can not be converted to a proper
    *         JSONObject.
    */

   public static JSONObject fromObject( Object object ) {
      return fromObject( object, new JsonConfig() );
   }

   /**
    * Creates a JSONObject.<br>
    * Inspects the object type to call the correct JSONObject factory method.
    * Accepts JSON formatted strings, Maps, DynaBeans and JavaBeans.
    *
    * @param object
    * @throws JSONException if the object can not be converted to a proper
    *         JSONObject.
    */
   public static JSONObject fromObject( Object object, JsonConfig jsonConfig ) {
      if( object == null || JSONUtils.isNull( object ) ){
         return new JSONObject( true );
      }else if( object instanceof Enum ){
         throw new JSONException( "'object' is an Enum. Use JSONArray instead" );
      }else if( object instanceof Annotation || (object != null && object.getClass()
            .isAnnotation()) ){
         throw new JSONException( "'object' is an Annotation." );
      }else if( object instanceof JSONObject ){
         return _fromJSONObject( (JSONObject) object, jsonConfig );
      }else if( object instanceof DynaBean ){
         return _fromDynaBean( (DynaBean) object, jsonConfig );
      }else if( object instanceof JSONTokener ){
         return _fromJSONTokener( (JSONTokener) object, jsonConfig );
      }else if( object instanceof JSONString ){
         return _fromJSONString( (JSONString) object, jsonConfig );
      }else if( object instanceof Map ){
         return _fromMap( (Map) object, jsonConfig );
      }else if( object instanceof String ){
         return _fromString( (String) object, jsonConfig );
      }else if( JSONUtils.isNumber( object ) || JSONUtils.isBoolean( object )
            || JSONUtils.isString( object ) ){
         return new JSONObject();
      }else if( JSONUtils.isArray( object ) ){
         throw new JSONException( "'object' is an array. Use JSONArray instead" );
      }else{
         return _fromBean( object, jsonConfig );
      }
   }

   /**
    * Creates a JSONDynaBean from a JSONObject.
    */
   public static Object toBean( JSONObject jsonObject ) {
      if( jsonObject == null || jsonObject.isNullObject() ){
         return null;
      }

      DynaBean dynaBean = null;

      JsonConfig jsonConfig = new JsonConfig();
      Map props = JSONUtils.getProperties( jsonObject );
      dynaBean = JSONUtils.newDynaBean( jsonObject, jsonConfig );
      for( Iterator entries = jsonObject.names( jsonConfig )
            .iterator(); entries.hasNext(); ){
         String name = (String) entries.next();
         String key = JSONUtils.convertToJavaIdentifier( name, jsonConfig );
         Class type = (Class) props.get( name );
         Object value = jsonObject.get( name );
         try{
            if( !JSONUtils.isNull( value ) ){
               if( value instanceof JSONArray ){
                  dynaBean.set( key, JSONArray.toCollection( (JSONArray) value ) );
               }else if( String.class.isAssignableFrom( type )
                     || Boolean.class.isAssignableFrom( type ) || JSONUtils.isNumber( type )
                     || Character.class.isAssignableFrom( type )
                     || JSONFunction.class.isAssignableFrom( type ) ){
                  dynaBean.set( key, value );
               }else{
                  dynaBean.set( key, toBean( (JSONObject) value ) );
               }
            }else{
               if( type.isPrimitive() ){
                  // assume assigned default value
                  log.warn( "Tried to assign null value to " + key + ":" + type.getName() );
                  dynaBean.set( key, JSONUtils.getMorpherRegistry()
                        .morph( type, null ) );
               }else{
                  dynaBean.set( key, null );
               }
            }
         }catch( JSONException jsone ){
            throw jsone;
         }catch( Exception e ){
            throw new JSONException( "Error while setting property=" + name + " type" + type, e );
         }
      }

      return dynaBean;
   }

   /**
    * Creates a bean from a JSONObject, with a specific target class.<br>
    */
   public static Object toBean( JSONObject jsonObject, Class beanClass ) {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setRootClass( beanClass );
      return toBean( jsonObject, jsonConfig );
   }

   /**
    * Creates a bean from a JSONObject, with a specific target class.<br>
    * If beanClass is null, this method will return a graph of DynaBeans. Any
    * attribute that is a JSONObject and matches a key in the classMap will be
    * converted to that target class.<br>
    * The classMap has the following conventions:
    * <ul>
    * <li>Every key must be an String.</li>
    * <li>Every value must be a Class.</li>
    * <li>A key may be a regular expression.</li>
    * </ul>
    */
   public static Object toBean( JSONObject jsonObject, Class beanClass, Map classMap ) {
      JsonConfig jsonConfig = new JsonConfig();
      jsonConfig.setRootClass( beanClass );
      jsonConfig.setClassMap( classMap );
      return toBean( jsonObject, jsonConfig );
   }

   /**
    * Creates a bean from a JSONObject, with the specific configuration.
    */
   public static Object toBean( JSONObject jsonObject, JsonConfig jsonConfig ) {
      if( jsonObject == null || jsonObject.isNullObject() ){
         return null;
      }

      Class beanClass = jsonConfig.getRootClass();
      Map classMap = jsonConfig.getClassMap();

      if( beanClass == null ){
         return toBean( jsonObject );
      }
      if( classMap == null ){
         classMap = Collections.EMPTY_MAP;
      }

      Object bean = null;
      try{
         if( beanClass.isInterface() ){
            if( !Map.class.isAssignableFrom( beanClass ) ){
               throw new JSONException( "beanClass is an interface. " + beanClass );
            }else{
               bean = new HashMap();
            }
         }else{
            bean = jsonConfig.getNewBeanInstanceStrategy()
                  .newInstance( beanClass, jsonObject );
         }
      }catch( JSONException jsone ){
         throw jsone;
      }catch( Exception e ){
         throw new JSONException( e );
      }

      Map props = JSONUtils.getProperties( jsonObject );
      PropertyFilter javaPropertyFilter = jsonConfig.getJavaPropertyFilter();
      for( Iterator entries = jsonObject.names( jsonConfig )
            .iterator(); entries.hasNext(); ){
         String name = (String) entries.next();
         Class type = (Class) props.get( name );
         Object value = jsonObject.get( name );
         if( javaPropertyFilter != null && javaPropertyFilter.apply( bean, name, value ) ){
            continue;
         }
         String key = Map.class.isAssignableFrom( beanClass )
               && jsonConfig.isSkipJavaIdentifierTransformationInMapKeys() ? name
               : JSONUtils.convertToJavaIdentifier( name, jsonConfig );
         PropertyNameProcessor propertyNameProcessor = jsonConfig.findJavaPropertyNameProcessor( beanClass );
         if( propertyNameProcessor != null ){
            key = propertyNameProcessor.processPropertyName( beanClass, key );
         }
         try{
            if( Map.class.isAssignableFrom( beanClass ) ){
               // no type info available for conversion
               if( JSONUtils.isNull( value ) ){
                  setProperty( bean, key, value, jsonConfig );
               }else if( value instanceof JSONArray ){
                  setProperty( bean, key, convertPropertyValueToCollection( key, value, jsonConfig, name,
                        classMap, List.class ), jsonConfig );
               }else if( String.class.isAssignableFrom( type ) || JSONUtils.isBoolean( type )
                     || JSONUtils.isNumber( type ) || JSONUtils.isString( type )
                     || JSONFunction.class.isAssignableFrom( type ) ){
                  if( jsonConfig.isHandleJettisonEmptyElement() && "".equals( value ) ){
                     setProperty( bean, key, null, jsonConfig );
                  }else{
                     setProperty( bean, key, value, jsonConfig );
                  }
               }else{
                  Class targetClass = resolveClass(classMap, key, name, type);
                  JsonConfig jsc = jsonConfig.copy();
                  jsc.setRootClass( targetClass );
                  jsc.setClassMap( classMap );
                  if( targetClass != null ){
                     setProperty( bean, key, toBean( (JSONObject) value, jsc ), jsonConfig );
                  }else{
                     setProperty( bean, key, toBean( (JSONObject) value ), jsonConfig );
                  }
               }
            }else{
               PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( bean, key );
               if( pd != null && pd.getWriteMethod() == null ){
                  log.info( "Property '" + key + "' of "+ bean.getClass()+" has no write method. SKIPPED." );
                  continue;
               }

               if( pd != null ){
                  Class targetType = pd.getPropertyType();
                  if( !JSONUtils.isNull( value ) ){
                     if( value instanceof JSONArray ){
                        if( List.class.isAssignableFrom( pd.getPropertyType() ) ){
                           setProperty( bean, key, convertPropertyValueToCollection( key, value,
                                 jsonConfig, name, classMap, pd.getPropertyType() ), jsonConfig );
                        }else if( Set.class.isAssignableFrom( pd.getPropertyType() ) ){
                           setProperty( bean, key, convertPropertyValueToCollection( key, value,
                                 jsonConfig, name, classMap, pd.getPropertyType() ), jsonConfig );
                        }else{
                           setProperty( bean, key, convertPropertyValueToArray( key, value,
                                 targetType, jsonConfig, classMap ), jsonConfig );
                        }
                     }else if( String.class.isAssignableFrom( type ) || JSONUtils.isBoolean( type )
                           || JSONUtils.isNumber( type ) || JSONUtils.isString( type )
                           || JSONFunction.class.isAssignableFrom( type ) ){
                        if( pd != null ){
                           if( jsonConfig.isHandleJettisonEmptyElement() && "".equals( value ) ){
                              setProperty( bean, key, null, jsonConfig );
                           }else if( !targetType.isInstance( value ) ){
                              setProperty( bean, key, morphPropertyValue( key, value, type,
                                    targetType ), jsonConfig );
                           }else{
                              setProperty( bean, key, value, jsonConfig );
                           }
                        }else if( beanClass == null || bean instanceof Map ){
                           setProperty( bean, key, value, jsonConfig );
                        }else{
                           log.warn( "Tried to assign property " + key + ":" + type.getName()
                                 + " to bean of class " + bean.getClass()
                                       .getName() );
                        }
                     }else{
                        if( jsonConfig.isHandleJettisonSingleElementArray() ){
                           JSONArray array = new JSONArray().element( value, jsonConfig );
                           Class newTargetClass = resolveClass(classMap, key, name, type);
                           JsonConfig jsc = jsonConfig.copy();
                           jsc.setRootClass( newTargetClass );
                           jsc.setClassMap( classMap );
                           if( targetType.isArray() ){
                              setProperty( bean, key, JSONArray.toArray( array, jsc ), jsonConfig );
                           }else if( JSONArray.class.isAssignableFrom( targetType ) ){
                              setProperty( bean, key, array, jsonConfig );
                           }else if( List.class.isAssignableFrom( targetType )
                                 || Set.class.isAssignableFrom( targetType ) ){
                              jsc.setCollectionType( targetType );
                              setProperty( bean, key, JSONArray.toCollection( array, jsc ),
                                    jsonConfig );
                           }else{
                              setProperty( bean, key, toBean( (JSONObject) value, jsc ), jsonConfig );
                           }
                        }else{
                           if( targetType == Object.class || targetType.isInterface() ) {
                              Class targetTypeCopy = targetType;
                              targetType = findTargetClass( key, classMap );
                              targetType = targetType == null ? findTargetClass( name, classMap )
                                    : targetType;
                              targetType = targetType == null && targetTypeCopy.isInterface() ? targetTypeCopy
                                    : targetType;
                           }
                           JsonConfig jsc = jsonConfig.copy();
                           jsc.setRootClass( targetType );
                           jsc.setClassMap( classMap );
                           setProperty( bean, key, toBean( (JSONObject) value, jsc ), jsonConfig );
                        }
                     }
                  }else{
                     if( type.isPrimitive() ){
                        // assume assigned default value
                        log.warn( "Tried to assign null value to " + key + ":" + type.getName() );
                        setProperty( bean, key, JSONUtils.getMorpherRegistry()
                              .morph( type, null ), jsonConfig );
                     }else{
                        setProperty( bean, key, null, jsonConfig );
                     }
                  }
               }else{
                  if( !JSONUtils.isNull( value ) ){
                     if( value instanceof JSONArray ){
                        setProperty( bean, key, convertPropertyValueToCollection( key, value,
                              jsonConfig, name, classMap, List.class ), jsonConfig );
                     }else if( String.class.isAssignableFrom( type ) || JSONUtils.isBoolean( type )
                           || JSONUtils.isNumber( type ) || JSONUtils.isString( type )
                           || JSONFunction.class.isAssignableFrom( type ) ){
                        if( beanClass == null || bean instanceof Map || jsonConfig.getPropertySetStrategy() != null ||
                            !jsonConfig.isIgnorePublicFields() ){
                           setProperty( bean, key, value, jsonConfig );
                        }else{
                           log.warn( "Tried to assign property " + key + ":" + type.getName()
                                 + " to bean of class " + bean.getClass()
                                       .getName() );
                        }
                     }else{
                        if( jsonConfig.isHandleJettisonSingleElementArray() ){
                           Class newTargetClass = resolveClass(classMap, key, name, type);
                           JsonConfig jsc = jsonConfig.copy();
                           jsc.setRootClass( newTargetClass );
                           jsc.setClassMap( classMap );
                           setProperty( bean, key, toBean( (JSONObject) value, jsc ), jsonConfig );
                        }else{
                           setProperty( bean, key, value, jsonConfig );
                        }
                     }
                  }else{
                     if( type.isPrimitive() ){
                        // assume assigned default value
                        log.warn( "Tried to assign null value to " + key + ":" + type.getName() );
                        setProperty( bean, key, JSONUtils.getMorpherRegistry()
                              .morph( type, null ), jsonConfig );
                     }else{
                        setProperty( bean, key, null, jsonConfig );
                     }
                  }
               }
            }
         }catch( JSONException jsone ){
            throw jsone;
         }catch( Exception e ){
            throw new JSONException( "Error while setting property=" + name + " type " + type, e );
         }
      }

      return bean;
   }

   /**
    * Creates a bean from a JSONObject, with the specific configuration.
    */
   public static Object toBean( JSONObject jsonObject, Object root, JsonConfig jsonConfig ) {
      if( jsonObject == null || jsonObject.isNullObject() || root == null ){
         return root;
      }

      Class rootClass = root.getClass();
      if( rootClass.isInterface() ){
         throw new JSONException( "Root bean is an interface. " + rootClass );
      }

      Map classMap = jsonConfig.getClassMap();
      if( classMap == null ){
         classMap = Collections.EMPTY_MAP;
      }

      Map props = JSONUtils.getProperties( jsonObject );
      PropertyFilter javaPropertyFilter = jsonConfig.getJavaPropertyFilter();
      for( Iterator entries = jsonObject.names( jsonConfig )
            .iterator(); entries.hasNext(); ){
         String name = (String) entries.next();
         Class type = (Class) props.get( name );
         Object value = jsonObject.get( name );
         if( javaPropertyFilter != null && javaPropertyFilter.apply( root, name, value ) ){
            continue;
         }
         String key = JSONUtils.convertToJavaIdentifier( name, jsonConfig );
         try{
            PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor( root, key );
            if( pd != null && pd.getWriteMethod() == null ){
               log.info( "Property '" + key + "' of "+ root.getClass()+" has no write method. SKIPPED." );
               continue;
            }

            if( !JSONUtils.isNull( value ) ){
               if( value instanceof JSONArray ){
                  if( pd == null || List.class.isAssignableFrom( pd.getPropertyType() ) ){
                     Class targetClass = resolveClass(classMap, key, name, type);
                     Object newRoot = jsonConfig.getNewBeanInstanceStrategy()
                           .newInstance( targetClass, null );
                     List list = JSONArray.toList( (JSONArray) value, newRoot, jsonConfig );
                     setProperty( root, key, list, jsonConfig );
                  }else{
                     Class innerType = JSONUtils.getInnerComponentType( pd.getPropertyType() );
                     Class targetInnerType = findTargetClass( key, classMap );
                     if( innerType.equals( Object.class ) && targetInnerType != null
                           && !targetInnerType.equals( Object.class ) ){
                        innerType = targetInnerType;
                     }
                     Object newRoot = jsonConfig.getNewBeanInstanceStrategy()
                           .newInstance( innerType, null );
                     Object array = JSONArray.toArray( (JSONArray) value, newRoot, jsonConfig );
                     if( innerType.isPrimitive() || JSONUtils.isNumber( innerType )
                           || Boolean.class.isAssignableFrom( innerType )
                           || JSONUtils.isString( innerType ) ){
                        array = JSONUtils.getMorpherRegistry()
                              .morph( Array.newInstance( innerType, 0 )
                                    .getClass(), array );
                     }else if( !array.getClass()
                           .equals(pd.getPropertyType()) ){
                        if( !pd.getPropertyType()
                              .equals(Object.class) ){
                           Morpher morpher = JSONUtils.getMorpherRegistry()
                                 .getMorpherFor( Array.newInstance( innerType, 0 )
                                       .getClass() );
                           if( IdentityObjectMorpher.getInstance()
                                 .equals( morpher ) ){
                              ObjectArrayMorpher beanMorpher = new ObjectArrayMorpher(
                                    new BeanMorpher( innerType, JSONUtils.getMorpherRegistry() ) );
                              JSONUtils.getMorpherRegistry()
                                    .registerMorpher( beanMorpher );
                           }
                           array = JSONUtils.getMorpherRegistry()
                                 .morph( Array.newInstance( innerType, 0 )
                                       .getClass(), array );
                        }
                     }
                     setProperty( root, key, array, jsonConfig );
                  }
               }else if( String.class.isAssignableFrom( type ) || JSONUtils.isBoolean( type )
                     || JSONUtils.isNumber( type ) || JSONUtils.isString( type )
                     || JSONFunction.class.isAssignableFrom( type ) ){
                  if( pd != null ){
                     if( jsonConfig.isHandleJettisonEmptyElement() && "".equals( value ) ){
                        setProperty( root, key, null, jsonConfig );
                     }else if( !pd.getPropertyType()
                           .isInstance( value ) ){
                        Morpher morpher = JSONUtils.getMorpherRegistry()
                              .getMorpherFor( pd.getPropertyType() );
                        if( IdentityObjectMorpher.getInstance()
                              .equals( morpher ) ){
                           log.warn( "Can't transform property '" + key + "' from "
                                 + type.getName() + " into " + pd.getPropertyType()
                                       .getName() + ". Will register a default BeanMorpher" );
                           JSONUtils.getMorpherRegistry()
                                 .registerMorpher(
                                       new BeanMorpher( pd.getPropertyType(),
                                             JSONUtils.getMorpherRegistry() ) );
                        }
                        setProperty( root, key, JSONUtils.getMorpherRegistry()
                              .morph( pd.getPropertyType(), value ), jsonConfig );
                     }else{
                        setProperty( root, key, value, jsonConfig );
                     }
                  }else if( root instanceof Map ){
                     setProperty( root, key, value, jsonConfig );
                  }else{
                     log.warn( "Tried to assign property " + key + ":" + type.getName()
                           + " to bean of class " + root.getClass()
                                 .getName() );
                  }
               }else{
                  if( pd != null ){
                     Class targetClass = pd.getPropertyType();
                     if( jsonConfig.isHandleJettisonSingleElementArray() ){
                        JSONArray array = new JSONArray().element( value, jsonConfig );
                        Class newTargetClass = resolveClass(classMap, key, name, type);
                        Object newRoot = jsonConfig.getNewBeanInstanceStrategy()
                              .newInstance( newTargetClass, (JSONObject) value );
                        if( targetClass.isArray() ){
                           setProperty( root, key, JSONArray.toArray( array, newRoot, jsonConfig ),
                                 jsonConfig );
                        }else if( Collection.class.isAssignableFrom( targetClass ) ){
                           setProperty( root, key, JSONArray.toList( array, newRoot, jsonConfig ),
                                 jsonConfig );
                        }else if( JSONArray.class.isAssignableFrom( targetClass ) ){
                           setProperty( root, key, array, jsonConfig );
                        }else{
                           setProperty( root, key,
                                 toBean( (JSONObject) value, newRoot, jsonConfig ), jsonConfig );
                        }
                     }else{
                        if( targetClass == Object.class ){
                            targetClass = resolveClass(classMap, key, name, type);
                            if(targetClass == null) {
                                targetClass = Object.class;
                            }
                        }
                        Object newRoot = jsonConfig.getNewBeanInstanceStrategy()
                              .newInstance( targetClass, (JSONObject) value );
                        setProperty( root, key, toBean( (JSONObject) value, newRoot, jsonConfig ),
                              jsonConfig );
                     }
                  }else if( root instanceof Map ){
                     Class targetClass = findTargetClass( key, classMap );
                     targetClass = targetClass == null ? findTargetClass( name, classMap )
                           : targetClass;
                     Object newRoot = jsonConfig.getNewBeanInstanceStrategy()
                           .newInstance( targetClass, null );
                     setProperty( root, key, toBean( (JSONObject) value, newRoot, jsonConfig ),
                           jsonConfig );
                  }else{
                     log.warn( "Tried to assign property " + key + ":" + type.getName()
                           + " to bean of class " + rootClass.getName() );
                  }
               }
            }else{
               if( type.isPrimitive() ){
                  // assume assigned default value
                  log.warn( "Tried to assign null value to " + key + ":" + type.getName() );
                  setProperty( root, key, JSONUtils.getMorpherRegistry()
                        .morph( type, null ), jsonConfig );
               }else{
                  setProperty( root, key, null, jsonConfig );
               }
            }
         }catch( JSONException jsone ){
            throw jsone;
         }catch( Exception e ){
            throw new JSONException( "Error while setting property=" + name + " type " + type, e );
         }
      }

      return root;
   }

   /**
    * Creates a JSONObject from a POJO.<br>
    * Supports nested maps, POJOs, and arrays/collections.
    *
    * @param bean An object with POJO conventions
    * @throws JSONException if the bean can not be converted to a proper
    *         JSONObject.
    */
   private static JSONObject _fromBean( Object bean, JsonConfig jsonConfig ) {
      if( !addInstance( bean ) ){
         try{
            return jsonConfig.getCycleDetectionStrategy()
                  .handleRepeatedReferenceAsObject( bean );
         }catch( JSONException jsone ){
            removeInstance( bean );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }catch( RuntimeException e ){
            removeInstance( bean );
            JSONException jsone = new JSONException( e );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }
      }
      fireObjectStartEvent( jsonConfig );

      JsonBeanProcessor processor = jsonConfig.findJsonBeanProcessor( bean.getClass() );
      if( processor != null ){
         JSONObject json = null;
         try{
            json = processor.processBean( bean, jsonConfig );
            if( json == null ){
               json = (JSONObject) jsonConfig.findDefaultValueProcessor( bean.getClass() )
                     .getDefaultValue( bean.getClass() );
               if( json == null ){
                  json = new JSONObject( true );
               }
            }
            removeInstance( bean );
            fireObjectEndEvent( jsonConfig );
         }catch( JSONException jsone ){
            removeInstance( bean );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }catch( RuntimeException e ){
            removeInstance( bean );
            JSONException jsone = new JSONException( e );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }
         return json;
      }

      JSONObject jsonObject = defaultBeanProcessing(bean, jsonConfig);
      removeInstance( bean );
      fireObjectEndEvent( jsonConfig );
      return jsonObject;
   }
   
   private static JSONObject defaultBeanProcessing(Object bean, JsonConfig jsonConfig) {      
      Class beanClass = bean.getClass();
      PropertyNameProcessor propertyNameProcessor = jsonConfig.findJsonPropertyNameProcessor( beanClass );      
      Collection exclusions = jsonConfig.getMergedExcludes( beanClass );
      JSONObject jsonObject = new JSONObject();
      try{
         PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors( bean );
         PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
         for( int i = 0; i < pds.length; i++ ){
            boolean bypass = false;
            String key = pds[i].getName();
            if( exclusions.contains( key ) ){
               continue;
            }

            if( jsonConfig.isIgnoreTransientFields() && isTransientField( key, beanClass, jsonConfig ) ){
               continue;
            }

            Class type = pds[i].getPropertyType();
            try { pds[i].getReadMethod(); }
            catch( Exception e ) {
               // bug 2565295
               String warning = "Property '" + key + "' of "+ beanClass+" has no read method. SKIPPED";
               fireWarnEvent( warning, jsonConfig );
               log.info( warning );
               continue;
            }
            if( pds[i].getReadMethod() != null ){
               /*
               if( jsonConfig.isIgnoreJPATransient() ){
                  try{
                     Class transientClass = Class.forName( "javax.persistence.Transient" );
                     if( pds[i].getReadMethod()
                           .getAnnotation( transientClass ) != null ){
                        continue;
                     }
                  }catch( ClassNotFoundException cnfe ){
                     // ignore
                  }
               }
               */
               if(isTransient(pds[i].getReadMethod(), jsonConfig)) continue;

               Object value = PropertyUtils.getProperty( bean, key );
               if( jsonPropertyFilter != null && jsonPropertyFilter.apply( bean, key, value ) ){
                  continue;
               }
               JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor(
                     beanClass, type, key );
               if( jsonValueProcessor != null ){
                  value = jsonValueProcessor.processObjectValue( key, value, jsonConfig );
                  bypass = true;
                  if( !JsonVerifier.isValidJsonValue( value ) ){
                     throw new JSONException( "Value is not a valid JSON value. " + value );
                  }
               }
               if( propertyNameProcessor != null ){
                  key = propertyNameProcessor.processPropertyName( beanClass, key );
               }
               setValue( jsonObject, key, value, type, jsonConfig, bypass );
            }else{
               String warning = "Property '" + key + "' of "+ beanClass+" has no read method. SKIPPED";
               fireWarnEvent( warning, jsonConfig );
               log.info( warning );
            }
         }
         // inspect public fields, this operation may fail under
         // a SecurityManager so we will eat all exceptions
         try {
            if( !jsonConfig.isIgnorePublicFields() ) {
               Field[] fields = beanClass.getFields();
               for( int i = 0; i < fields.length; i++ ) {
                  boolean bypass = false;
                  Field field = fields[i];
                  String key = field.getName();
                  if( exclusions.contains( key ) ) {
                     continue;
                  }

                  if( jsonConfig.isIgnoreTransientFields() && isTransient( field, jsonConfig ) ) {
                     continue;
                  }

                  Class type = field.getType();
                  Object value = field.get( bean );
                  if( jsonPropertyFilter != null && jsonPropertyFilter.apply( bean, key, value ) ) {
                     continue;
                  }
                  JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor( beanClass, type, key );
                  if( jsonValueProcessor != null ) {
                     value = jsonValueProcessor.processObjectValue( key, value, jsonConfig );
                     bypass = true;
                     if( !JsonVerifier.isValidJsonValue( value ) ) {
                        throw new JSONException( "Value is not a valid JSON value. " + value );
                     }
                  }
                  if( propertyNameProcessor != null ) {
                     key = propertyNameProcessor.processPropertyName( beanClass, key );
                  }
                  setValue( jsonObject, key, value, type, jsonConfig, bypass );
               }
            }
         }
         catch( Exception e ){
            log.trace( "Couldn't read public fields.", e );
         }
      }catch( JSONException jsone ){
         removeInstance( bean );
         fireErrorEvent( jsone, jsonConfig );
         throw jsone;
      }catch( Exception e ){
         removeInstance( bean );
         JSONException jsone = new JSONException( e );
         fireErrorEvent( jsone, jsonConfig );
         throw jsone;
      }
      return jsonObject;
   }

   private static JSONObject _fromDynaBean( DynaBean bean, JsonConfig jsonConfig ) {
      if( bean == null ){
         fireObjectStartEvent( jsonConfig );
         fireObjectEndEvent( jsonConfig );
         return new JSONObject( true );
      }

      if( !addInstance( bean ) ){
         try{
            return jsonConfig.getCycleDetectionStrategy()
                  .handleRepeatedReferenceAsObject( bean );
         }catch( JSONException jsone ){
            removeInstance( bean );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }catch( RuntimeException e ){
            removeInstance( bean );
            JSONException jsone = new JSONException( e );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }
      }
      fireObjectStartEvent( jsonConfig );

      JSONObject jsonObject = new JSONObject();
      try{
         DynaProperty[] props = bean.getDynaClass()
               .getDynaProperties();
         Collection exclusions = jsonConfig.getMergedExcludes();
         PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
         for( int i = 0; i < props.length; i++ ){
            boolean bypass = false;
            DynaProperty dynaProperty = props[i];
            String key = dynaProperty.getName();
            if( exclusions.contains( key ) ){
               continue;
            }
            Class type = dynaProperty.getType();
            Object value = bean.get( dynaProperty.getName() );
            if( jsonPropertyFilter != null && jsonPropertyFilter.apply( bean, key, value ) ){
               continue;
            }
            JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor( type, key );
            if( jsonValueProcessor != null ){
               value = jsonValueProcessor.processObjectValue( key, value, jsonConfig );
               bypass = true;
               if( !JsonVerifier.isValidJsonValue( value ) ){
                  throw new JSONException( "Value is not a valid JSON value. " + value );
               }
            }
            setValue( jsonObject, key, value, type, jsonConfig, bypass );
         }
      }catch( JSONException jsone ){
         removeInstance( bean );
         fireErrorEvent( jsone, jsonConfig );
         throw jsone;
      }catch( RuntimeException e ){
         removeInstance( bean );
         JSONException jsone = new JSONException( e );
         fireErrorEvent( jsone, jsonConfig );
         throw jsone;
      }

      removeInstance( bean );
      fireObjectEndEvent( jsonConfig );
      return jsonObject;
   }

   private static JSONObject _fromJSONObject( JSONObject object, JsonConfig jsonConfig ) {
      if( object == null || object.isNullObject() ){
         fireObjectStartEvent( jsonConfig );
         fireObjectEndEvent( jsonConfig );
         return new JSONObject( true );
      }

      if( !addInstance( object ) ){
         try{
            return jsonConfig.getCycleDetectionStrategy()
                  .handleRepeatedReferenceAsObject( object );
         }catch( JSONException jsone ){
            removeInstance( object );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }catch( RuntimeException e ){
            removeInstance( object );
            JSONException jsone = new JSONException( e );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }
      }
      fireObjectStartEvent( jsonConfig );

      JSONArray sa = object.names(jsonConfig);
      Collection exclusions = jsonConfig.getMergedExcludes();
      JSONObject jsonObject = new JSONObject();
      PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
      for( Iterator i = sa.iterator(); i.hasNext(); ){
         Object k =  i.next();
         if( k == null ){
            throw new JSONException("JSON keys cannot be null.");
         }
         if( !(k instanceof String) && !jsonConfig.isAllowNonStringKeys() ) {
            throw new ClassCastException("JSON keys must be strings.");
         }
         String key = String.valueOf( k );
         if( "null".equals( key )){
            throw new NullPointerException("JSON keys must not be null nor the 'null' string.");
         }
         if( exclusions.contains( key ) ){
            continue;
         }
         Object value = object.opt( key );
         if( jsonPropertyFilter != null && jsonPropertyFilter.apply( object, key, value ) ){
            continue;
         }
         if( jsonObject.properties.containsKey( key ) ){
            jsonObject.accumulate( key, value, jsonConfig );
            firePropertySetEvent( key, value, true, jsonConfig );
         }else{
            jsonObject.setInternal( key, value, jsonConfig );
            firePropertySetEvent( key, value, false, jsonConfig );
         }
      }

      removeInstance( object );
      fireObjectEndEvent( jsonConfig );
      return jsonObject;
   }

   private static JSONObject _fromJSONString( JSONString string, JsonConfig jsonConfig ) {
      return _fromJSONTokener( new JSONTokener( string.toJSONString() ), jsonConfig );
   }

   private static JSONObject _fromJSONTokener( JSONTokener tokener, JsonConfig jsonConfig ) {

      try{
         char c;
         String key;
         Object value;

         if( tokener.matches( "null.*" ) ){
            fireObjectStartEvent( jsonConfig );
            fireObjectEndEvent( jsonConfig );
            return new JSONObject( true );
         }

         if( tokener.nextClean() != '{' ){
            throw tokener.syntaxError( "A JSONObject text must begin with '{'" );
         }
         fireObjectStartEvent( jsonConfig );

         Collection exclusions = jsonConfig.getMergedExcludes();
         PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
         JSONObject jsonObject = new JSONObject();
         for( ;; ){
            c = tokener.nextClean();
            switch( c ){
               case 0:
                  throw tokener.syntaxError( "A JSONObject text must end with '}'" );
               case '}':
                  fireObjectEndEvent( jsonConfig );
                  return jsonObject;
               default:
                  tokener.back();
                  key = tokener.nextValue( jsonConfig )
                        .toString();
            }

            /*
             * The key is followed by ':'. We will also tolerate '=' or '=>'.
             */

            c = tokener.nextClean();
            if( c == '=' ){
               if( tokener.next() != '>' ){
                  tokener.back();
               }
            }else if( c != ':' ){
               throw tokener.syntaxError( "Expected a ':' after a key" );
            }

            char peek = tokener.peek();
            boolean quoted = peek == '"' || peek == '\'';
            Object v = tokener.nextValue( jsonConfig );
            if( quoted || !JSONUtils.isFunctionHeader( v ) ){
               if( exclusions.contains( key ) ){
                  switch( tokener.nextClean() ){
                     case ';':
                     case ',':
                        if( tokener.nextClean() == '}' ){
                           fireObjectEndEvent( jsonConfig );
                           return jsonObject;
                        }
                        tokener.back();
                        break;
                     case '}':
                        fireObjectEndEvent( jsonConfig );
                        return jsonObject;
                     default:
                        throw tokener.syntaxError( "Expected a ',' or '}'" );
                  }
                  continue;
               }
               if( jsonPropertyFilter == null || !jsonPropertyFilter.apply( tokener, key, v ) ){
                  if( quoted && v instanceof String && (JSONUtils.mayBeJSON( (String) v ) || JSONUtils.isFunction( v ))){
                     v = JSONUtils.DOUBLE_QUOTE + v + JSONUtils.DOUBLE_QUOTE;
                  }
                  if( jsonObject.properties.containsKey( key ) ){
                     jsonObject.accumulate( key, v, jsonConfig );
                     firePropertySetEvent( key, v, true, jsonConfig );
                  }else{
                     jsonObject.element( key, v, jsonConfig );
                     firePropertySetEvent( key, v, false, jsonConfig );
                  }
               }
            }else{
               // read params if any
               String params = JSONUtils.getFunctionParams( (String) v );
               // read function text
               int i = 0;
               StringBuffer sb = new StringBuffer();
               for( ;; ){
                  char ch = tokener.next();
                  if( ch == 0 ){
                     break;
                  }
                  if( ch == '{' ){
                     i++;
                  }
                  if( ch == '}' ){
                     i--;
                  }
                  sb.append( ch );
                  if( i == 0 ){
                     break;
                  }
               }
               if( i != 0 ){
                  throw tokener.syntaxError( "Unbalanced '{' or '}' on prop: " + v );
               }
               // trim '{' at start and '}' at end
               String text = sb.toString();
               text = text.substring( 1, text.length() - 1 )
                     .trim();
               value = new JSONFunction(
                     (params != null) ? StringUtils.split( params, "," ) : null, text );
               if( jsonPropertyFilter == null || !jsonPropertyFilter.apply( tokener, key, value ) ){
                  if( jsonObject.properties.containsKey( key ) ){
                     jsonObject.accumulate( key, value, jsonConfig );
                     firePropertySetEvent( key, value, true, jsonConfig );
                  }else{
                     jsonObject.element( key, value, jsonConfig );
                     firePropertySetEvent( key, value, false, jsonConfig );
                  }
               }
            }

            /*
             * Pairs are separated by ','. We will also tolerate ';'.
             */

            switch( tokener.nextClean() ){
               case ';':
               case ',':
                  if( tokener.nextClean() == '}' ){
                     fireObjectEndEvent( jsonConfig );
                     return jsonObject;
                  }
                  tokener.back();
                  break;
               case '}':
                  fireObjectEndEvent( jsonConfig );
                  return jsonObject;
               default:
                  throw tokener.syntaxError( "Expected a ',' or '}'" );
            }
         }
      }catch( JSONException jsone ){
         fireErrorEvent( jsone, jsonConfig );
         throw jsone;
      }
   }

   private static JSONObject _fromMap( Map map, JsonConfig jsonConfig ) {
      if( map == null ){
         fireObjectStartEvent( jsonConfig );
         fireObjectEndEvent( jsonConfig );
         return new JSONObject( true );
      }

      if( !addInstance( map ) ){
         try{
            return jsonConfig.getCycleDetectionStrategy()
                  .handleRepeatedReferenceAsObject( map );
         }catch( JSONException jsone ){
            removeInstance( map );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }catch( RuntimeException e ){
            removeInstance( map );
            JSONException jsone = new JSONException( e );
            fireErrorEvent( jsone, jsonConfig );
            throw jsone;
         }
      }
      fireObjectStartEvent( jsonConfig );

      Collection exclusions = jsonConfig.getMergedExcludes();
      JSONObject jsonObject = new JSONObject();
      PropertyFilter jsonPropertyFilter = jsonConfig.getJsonPropertyFilter();
      try{
         for( Iterator entries = map.entrySet()
               .iterator(); entries.hasNext(); ){
            boolean bypass = false;
            Map.Entry entry = (Map.Entry) entries.next();
            Object k = entry.getKey();
            if( k == null ){
               throw new JSONException("JSON keys cannot be null.");
            }
            if( !(k instanceof String) && !jsonConfig.isAllowNonStringKeys() ) {
               throw new ClassCastException("JSON keys must be strings.");
            }
            String key = String.valueOf( k );
            if( "null".equals( key )){
               throw new NullPointerException("JSON keys must not be null nor the 'null' string.");
            }
            if( exclusions.contains( key ) ){
               continue;
            }
            Object value = entry.getValue();
            if( jsonPropertyFilter != null && jsonPropertyFilter.apply( map, key, value ) ){
               continue;
            }
            if( value != null ){
               JsonValueProcessor jsonValueProcessor = jsonConfig.findJsonValueProcessor(
                     value.getClass(), key );
               if( jsonValueProcessor != null ){
                  value = jsonValueProcessor.processObjectValue( key, value, jsonConfig );
                  bypass = true;
                  if( !JsonVerifier.isValidJsonValue( value ) ){
                     throw new JSONException( "Value is not a valid JSON value. " + value );
                  }
               }
               setValue( jsonObject, key, value, value.getClass(), jsonConfig, bypass );
            }else{
               if( jsonObject.properties.containsKey( key ) ){
                  jsonObject.accumulate( key, JSONNull.getInstance() );
                  firePropertySetEvent( key, JSONNull.getInstance(), true, jsonConfig );
               }else{
                  jsonObject.element( key, JSONNull.getInstance() );
                  firePropertySetEvent( key, JSONNull.getInstance(), false, jsonConfig );
               }
            }
         }
      }catch( JSONException jsone ){
         removeInstance( map );
         fireErrorEvent( jsone, jsonConfig );
         throw jsone;
      }catch( RuntimeException e ){
         removeInstance( map );
         JSONException jsone = new JSONException( e );
         fireErrorEvent( jsone, jsonConfig );
         throw jsone;
      }

      removeInstance( map );
      fireObjectEndEvent( jsonConfig );
      return jsonObject;
   }

   private static JSONObject _fromString( String str, JsonConfig jsonConfig ) {
      if( str == null || "null".equals( str ) ){
         fireObjectStartEvent( jsonConfig );
         fireObjectEndEvent( jsonConfig );
         return new JSONObject( true );
      }
      return _fromJSONTokener( new JSONTokener( str ), jsonConfig );
   }

   private static Object convertPropertyValueToArray( String key, Object value, Class targetType,
         JsonConfig jsonConfig, Map classMap ) {
      Class innerType = JSONUtils.getInnerComponentType( targetType );
      Class targetInnerType = findTargetClass( key, classMap );
      if( innerType.equals( Object.class ) && targetInnerType != null
            && !targetInnerType.equals( Object.class ) ){
         innerType = targetInnerType;
      }
      JsonConfig jsc = jsonConfig.copy();
      jsc.setRootClass( innerType );
      jsc.setClassMap( classMap );
      Object array = JSONArray.toArray( (JSONArray) value, jsc );
      if( innerType.isPrimitive() || JSONUtils.isNumber( innerType )
            || Boolean.class.isAssignableFrom( innerType ) || JSONUtils.isString( innerType ) ){
         array = JSONUtils.getMorpherRegistry()
               .morph( Array.newInstance( innerType, 0 )
                     .getClass(), array );
      }else if( !array.getClass()
            .equals( targetType ) ){
         if( !targetType.equals( Object.class ) ){
            Morpher morpher = JSONUtils.getMorpherRegistry()
                  .getMorpherFor( Array.newInstance( innerType, 0 )
                        .getClass() );
            if( IdentityObjectMorpher.getInstance()
                  .equals( morpher ) ){
               ObjectArrayMorpher beanMorpher = new ObjectArrayMorpher( new BeanMorpher( innerType,
                     JSONUtils.getMorpherRegistry() ) );
               JSONUtils.getMorpherRegistry()
                     .registerMorpher( beanMorpher );
            }
            array = JSONUtils.getMorpherRegistry()
                  .morph( Array.newInstance( innerType, 0 )
                        .getClass(), array );
         }
      }
      return array;
   }

   private static List convertPropertyValueToList( String key, Object value, JsonConfig jsonConfig,
         String name, Map classMap ) {
      Class targetClass = findTargetClass( key, classMap );
      targetClass = targetClass == null ? findTargetClass( name, classMap ) : targetClass;
      JsonConfig jsc = jsonConfig.copy();
      jsc.setRootClass( targetClass );
      jsc.setClassMap( classMap );
      List list = (List) JSONArray.toCollection( (JSONArray) value, jsc );
      return list;
   }

   private static Collection convertPropertyValueToCollection( String key, Object value, JsonConfig jsonConfig,
         String name, Map classMap, Class collectionType ) {
      Class targetClass = findTargetClass( key, classMap );
      targetClass = targetClass == null ? findTargetClass( name, classMap ) : targetClass;
      JsonConfig jsc = jsonConfig.copy();
      jsc.setRootClass( targetClass );
      jsc.setClassMap( classMap );
      jsc.setCollectionType( collectionType );
      return JSONArray.toCollection( (JSONArray) value, jsc );
   }

   /*
   private static Collection convertPropertyValueToCollection( String key, Object value, JsonConfig jsonConfig,
         String name, Map classMap, Object bean ) {
      Class targetClass = findTargetClass( key, classMap );
      targetClass = targetClass == null ? findTargetClass( name, classMap ) : targetClass;

      PropertyDescriptor pd;
      try{
         pd = PropertyUtils.getPropertyDescriptor( bean, key );
      }catch( IllegalAccessException e ){
         throw new JSONException( e );
      }catch( InvocationTargetException e ){
         throw new JSONException( e );
      }catch( NoSuchMethodException e ){
         throw new JSONException( e );
      }

      if( null == targetClass ){
         Class[] cType = JSONArray.getCollectionType( pd, false );
         if( null != cType && cType.length == 1 ){
            targetClass = cType[0];
         }
      }

      JsonConfig jsc = jsonConfig.copy();
      jsc.setRootClass( targetClass );
      jsc.setClassMap( classMap );
      jsc.setCollectionType( pd.getPropertyType() );
      jsc.setEnclosedType( targetClass );
      Collection collection = JSONArray.toCollection( (JSONArray) value, jsonConfig );
      return collection;
   }
   */

   private static Class resolveClass(Map classMap, String key, String name, Class type) {
       Class targetClass = findTargetClass(key, classMap);
       if (targetClass == null) {
           targetClass = findTargetClass(name, classMap);
       }
       if(targetClass == null && type != null) {
           if(List.class.equals(type)) {
               targetClass = ArrayList.class;
           } else if(Map.class.equals(type)) {
               targetClass = LinkedHashMap.class;
           } else if(Set.class.equals(type)) {
               targetClass = LinkedHashSet.class;
           } else if(!type.isInterface() && !Object.class.equals(type)) {
               targetClass = type;
           }
       }
       return targetClass;
   }
   
   /**
    * Locates a Class associated to a specifi key.<br>
    * The key may be a regexp.
    */
   private static Class findTargetClass( String key, Map classMap ) {
      // try get first
      Class targetClass = (Class) classMap.get( key );
      if( targetClass == null ){
         // try with regexp
         // this will hit performance as it must iterate over all the keys
         // and create a RegexpMatcher for each key
         for( Iterator i = classMap.entrySet()
               .iterator(); i.hasNext(); ){
            Map.Entry entry = (Map.Entry) i.next();
            if( RegexpUtils.getMatcher( (String) entry.getKey() )
                  .matches( key ) ){
               targetClass = (Class) entry.getValue();
               break;
            }
         }
      }

      return targetClass;
   }

   private static boolean isTransientField( String name, Class beanClass, JsonConfig jsonConfig ) {
      try{
         Field field = beanClass.getDeclaredField( name );
         if((field.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT) return true;
         return isTransient(field, jsonConfig);
      }catch( Exception e ){
         log.info( "Error while inspecting field "+beanClass+"."+name+" for transient status." ,e );
      }
      return false;
   }

   private static boolean isTransient( AnnotatedElement element, JsonConfig jsonConfig ) {
      for( Iterator annotations = jsonConfig.getIgnoreFieldAnnotations().iterator(); annotations.hasNext(); ) {
         try {
           String annotationClassName = (String) annotations.next();
           if( element.getAnnotation((Class) Class.forName( annotationClassName )) != null ) return true;
         } catch( Exception e ){
            log.info( "Error while inspecting "+element+" for transient status." ,e );
         }
      }
      return false;
   }

   private static Object morphPropertyValue( String key, Object value, Class type, Class targetType ) {
      Morpher morpher = JSONUtils.getMorpherRegistry()
            .getMorpherFor( targetType );
      if( IdentityObjectMorpher.getInstance()
            .equals( morpher ) ){
         log.warn( "Can't transform property '" + key + "' from " + type.getName() + " into "
               + targetType.getName() + ". Will register a default Morpher" );
         if( Enum.class.isAssignableFrom( targetType ) ){
            JSONUtils.getMorpherRegistry()
                  .registerMorpher( new EnumMorpher( targetType ) );
         }else{
            JSONUtils.getMorpherRegistry()
                  .registerMorpher( new BeanMorpher( targetType, JSONUtils.getMorpherRegistry() ) );
         }
      }

      value = JSONUtils.getMorpherRegistry()
            .morph( targetType, value );
      return value;
   }

   /**
    * Sets a property on the target bean.<br>
    * Bean may be a Map or a POJO.
    */
   private static void setProperty( Object bean, String key, Object value, JsonConfig jsonConfig )
         throws Exception {
      PropertySetStrategy propertySetStrategy = jsonConfig.getPropertySetStrategy() != null ? jsonConfig.getPropertySetStrategy()
            : PropertySetStrategy.DEFAULT;
      propertySetStrategy.setProperty( bean, key, value, jsonConfig );
   }

   private static void setValue( JSONObject jsonObject, String key, Object value, Class type,
         JsonConfig jsonConfig, boolean bypass ) {
      boolean accumulated = false;
      if( value == null ){
         value = jsonConfig.findDefaultValueProcessor( type )
               .getDefaultValue( type );
         if( !JsonVerifier.isValidJsonValue( value ) ){
            throw new JSONException( "Value is not a valid JSON value. " + value );
         }
      }
      if( jsonObject.properties.containsKey( key ) ){
         if( String.class.isAssignableFrom( type ) ){
            Object o = jsonObject.opt( key );
            if( o instanceof JSONArray ){
               ((JSONArray) o).addString( (String) value );
            }else{
               jsonObject.properties.put( key, new JSONArray().element( o )
                     .addString( (String) value ) );
            }
         }else{
            jsonObject.accumulate( key, value, jsonConfig );
         }
         accumulated = true;
      }else{
         if( bypass || String.class.isAssignableFrom( type ) ){
            jsonObject.properties.put( key, value );
         }else{
            jsonObject.setInternal( key, value, jsonConfig );
         }
      }

      value = jsonObject.opt( key );
      if( accumulated ){
         JSONArray array = (JSONArray) value;
         value = array.get( array.size() - 1 );
      }
      firePropertySetEvent( key, value, accumulated, jsonConfig );
   }

   // ------------------------------------------------------

   /** identifies this object as null */
   private boolean nullObject;

   /**
    * The Map where the JSONObject's properties are kept.
    */
   private Map properties;

   /**
    * Construct an empty JSONObject.
    */
   public JSONObject() {
      this.properties = new ListOrderedMap();
   }

   /**
    * Creates a JSONObject that is null.
    */
   public JSONObject( boolean isNull ) {
      this();
      this.nullObject = isNull;
   }

   /**
    * Accumulate values under a key. It is similar to the element method except
    * that if there is already an object stored under the key then a JSONArray
    * is stored under the key to hold all of the accumulated values. If there is
    * already a JSONArray, then the new value is appended to it. In contrast,
    * the replace method replaces the previous value.
    *
    * @param key A key string.
    * @param value An object to be accumulated under the key.
    * @return this.
    * @throws JSONException If the value is an invalid number or if the key is
    *         null.
    */
   public JSONObject accumulate( String key, boolean value ) {
      return _accumulate( key, value ? Boolean.TRUE : Boolean.FALSE, new JsonConfig() );
   }

   /**
    * Accumulate values under a key. It is similar to the element method except
    * that if there is already an object stored under the key then a JSONArray
    * is stored under the key to hold all of the accumulated values. If there is
    * already a JSONArray, then the new value is appended to it. In contrast,
    * the replace method replaces the previous value.
    *
    * @param key A key string.
    * @param value An object to be accumulated under the key.
    * @return this.
    * @throws JSONException If the value is an invalid number or if the key is
    *         null.
    */
   public JSONObject accumulate( String key, double value ) {
      return _accumulate( key, Double.valueOf( value ), new JsonConfig() );
   }

   /**
    * Accumulate values under a key. It is similar to the element method except
    * that if there is already an object stored under the key then a JSONArray
    * is stored under the key to hold all of the accumulated values. If there is
    * already a JSONArray, then the new value is appended to it. In contrast,
    * the replace method replaces the previous value.
    *
    * @param key A key string.
    * @param value An object to be accumulated under the key.
    * @return this.
    * @throws JSONException If the value is an invalid number or if the key is
    *         null.
    */
   public JSONObject accumulate( String key, int value ) {
      return _accumulate( key, Integer.valueOf( value ), new JsonConfig() );
   }

   /**
    * Accumulate values under a key. It is similar to the element method except
    * that if there is already an object stored under the key then a JSONArray
    * is stored under the key to hold all of the accumulated values. If there is
    * already a JSONArray, then the new value is appended to it. In contrast,
    * the replace method replaces the previous value.
    *
    * @param key A key string.
    * @param value An object to be accumulated under the key.
    * @return this.
    * @throws JSONException If the value is an invalid number or if the key is
    *         null.
    */
   public JSONObject accumulate( String key, long value ) {
      return _accumulate( key, Long.valueOf( value ), new JsonConfig() );
   }

   /**
    * Accumulate values under a key. It is similar to the element method except
    * that if there is already an object stored under the key then a JSONArray
    * is stored under the key to hold all of the accumulated values. If there is
    * already a JSONArray, then the new value is appended to it. In contrast,
    * the replace method replaces the previous value.
    *
    * @param key A key string.
    * @param value An object to be accumulated under the key.
    * @return this.
    * @throws JSONException If the value is an invalid number or if the key is
    *         null.
    */
   public JSONObject accumulate( String key, Object value ) {
      return _accumulate( key, value, new JsonConfig() );
   }

   /**
    * Accumulate values under a key. It is similar to the element method except
    * that if there is already an object stored under the key then a JSONArray
    * is stored under the key to hold all of the accumulated values. If there is
    * already a JSONArray, then the new value is appended to it. In contrast,
    * the replace method replaces the previous value.
    *
    * @param key A key string.
    * @param value An object to be accumulated under the key.
    * @return this.
    * @throws JSONException If the value is an invalid number or if the key is
    *         null.
    */
   public JSONObject accumulate( String key, Object value, JsonConfig jsonConfig ) {
      return _accumulate( key, value, jsonConfig );
   }

   public void accumulateAll( Map map ) {
      accumulateAll( map, new JsonConfig() );
   }

   public void accumulateAll( Map map, JsonConfig jsonConfig ) {
      if( map instanceof JSONObject ){
         for( Iterator entries = map.entrySet()
               .iterator(); entries.hasNext(); ){
            Map.Entry entry = (Map.Entry) entries.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            accumulate( key, value, jsonConfig );
         }
      }else{
         for( Iterator entries = map.entrySet()
               .iterator(); entries.hasNext(); ){
            Map.Entry entry = (Map.Entry) entries.next();
            String key = String.valueOf( entry.getKey() );
            Object value = entry.getValue();
            accumulate( key, value, jsonConfig );
         }
      }
   }

   public void clear() {
      properties.clear();
   }

   public int compareTo( Object obj ) {
      if( obj != null && (obj instanceof JSONObject) ){
         JSONObject other = (JSONObject) obj;
         int size1 = size();
         int size2 = other.size();
         if( size1 < size2 ){
            return -1;
         }else if( size1 > size2 ){
            return 1;
         }else if( this.equals( other ) ){
            return 0;
         }
      }
      return -1;
   }

   public boolean containsKey( Object key ) {
      return properties.containsKey( key );
   }

   public boolean containsValue( Object value ) {
      return containsValue( value, new JsonConfig() );
   }

   public boolean containsValue( Object value, JsonConfig jsonConfig ) {
      try{
         value = processValue( value, jsonConfig );
      }catch( JSONException e ){
         return false;
      }
      return properties.containsValue( value );
   }

   /**
    * Remove a name and its value, if present.
    *
    * @param key A key string.
    * @return this.
    */
   public JSONObject discard( String key ) {
      verifyIsNull();
      this.properties.remove( key );
      return this;
   }

   /**
    * Put a key/boolean pair in the JSONObject.
    *
    * @param key A key string.
    * @param value A boolean which is the value.
    * @return this.
    * @throws JSONException If the key is null.
    */
   public JSONObject element( String key, boolean value ) {
      verifyIsNull();
      return element( key, value ? Boolean.TRUE : Boolean.FALSE );
   }

   /**
    * Put a key/value pair in the JSONObject, where the value will be a
    * JSONArray which is produced from a Collection.
    *
    * @param key A key string.
    * @param value A Collection value.
    * @return this.
    * @throws JSONException
    */
   public JSONObject element( String key, Collection value ) {
      return element( key, value, new JsonConfig() );
   }

   /**
    * Put a key/value pair in the JSONObject, where the value will be a
    * JSONArray which is produced from a Collection.
    *
    * @param key A key string.
    * @param value A Collection value.
    * @return this.
    * @throws JSONException
    */
   public JSONObject element( String key, Collection value, JsonConfig jsonConfig ) {
      if( !(value instanceof JSONArray) ){
         value = JSONArray.fromObject( value, jsonConfig );
      }
      return setInternal( key, value, jsonConfig );
   }

   /**
    * Put a key/double pair in the JSONObject.
    *
    * @param key A key string.
    * @param value A double which is the value.
    * @return this.
    * @throws JSONException If the key is null or if the number is invalid.
    */
   public JSONObject element( String key, double value ) {
      verifyIsNull();
      Double d = new Double( value );
      JSONUtils.testValidity( d );
      return element( key, d );
   }

   /**
    * Put a key/int pair in the JSONObject.
    *
    * @param key A key string.
    * @param value An int which is the value.
    * @return this.
    * @throws JSONException If the key is null.
    */
   public JSONObject element( String key, int value ) {
      verifyIsNull();
      return element( key, new Integer( value ) );
   }

   /**
    * Put a key/long pair in the JSONObject.
    *
    * @param key A key string.
    * @param value A long which is the value.
    * @return this.
    * @throws JSONException If the key is null.
    */
   public JSONObject element( String key, long value ) {
      verifyIsNull();
      return element( key, new Long( value ) );
   }

   /**
    * Put a key/value pair in the JSONObject, where the value will be a
    * JSONObject which is produced from a Map.
    *
    * @param key A key string.
    * @param value A Map value.
    * @return this.
    * @throws JSONException
    */
   public JSONObject element( String key, Map value ) {
      return element( key, value, new JsonConfig() );
   }

   /**
    * Put a key/value pair in the JSONObject, where the value will be a
    * JSONObject which is produced from a Map.
    *
    * @param key A key string.
    * @param value A Map value.
    * @return this.
    * @throws JSONException
    */
   public JSONObject element( String key, Map value, JsonConfig jsonConfig ) {
      verifyIsNull();
      if( value instanceof JSONObject ){
         return setInternal( key, value, jsonConfig );
      }else{
         return element( key, JSONObject.fromObject( value, jsonConfig ), jsonConfig );
      }
   }

   /**
    * Put a key/value pair in the JSONObject. If the value is null, then the key
    * will be removed from the JSONObject if it is present.<br>
    * If there is a previous value assigned to the key, it will call accumulate.
    *
    * @param key A key string.
    * @param value An object which is the value. It should be of one of these
    *        types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
    *        String, or the JSONNull object.
    * @return this.
    * @throws JSONException If the value is non-finite number or if the key is
    *         null.
    */
   public JSONObject element( String key, Object value ) {
      return element( key, value, new JsonConfig() );
   }

   /**
    * Put a key/value pair in the JSONObject. If the value is null, then the key
    * will be removed from the JSONObject if it is present.<br>
    * If there is a previous value assigned to the key, it will call accumulate.
    *
    * @param key A key string.
    * @param value An object which is the value. It should be of one of these
    *        types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
    *        String, or the JSONNull object.
    * @return this.
    * @throws JSONException If the value is non-finite number or if the key is
    *         null.
    */
   public JSONObject element( String key, Object value, JsonConfig jsonConfig ) {
      verifyIsNull();
      if( key == null ){
         throw new JSONException( "Null key." );
      }
      if( value != null ){
         value = processValue( key, value, jsonConfig );
         _setInternal( key, value, jsonConfig );
      }else{
         remove( key );
      }
      return this;
   }

   /**
    * Put a key/value pair in the JSONObject, but only if the key and the value
    * are both non-null.
    *
    * @param key A key string.
    * @param value An object which is the value. It should be of one of these
    *        types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
    *        String, or the JSONNull object.
    * @return this.
    * @throws JSONException If the value is a non-finite number.
    */
   public JSONObject elementOpt( String key, Object value ) {
      return elementOpt( key, value, new JsonConfig() );
   }

   /**
    * Put a key/value pair in the JSONObject, but only if the key and the value
    * are both non-null.
    *
    * @param key A key string.
    * @param value An object which is the value. It should be of one of these
    *        types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
    *        String, or the JSONNull object.
    * @return this.
    * @throws JSONException If the value is a non-finite number.
    */
   public JSONObject elementOpt( String key, Object value, JsonConfig jsonConfig ) {
      verifyIsNull();
      if( key != null && value != null ){
         element( key, value, jsonConfig );
      }
      return this;
   }

   public Set entrySet() {
      return Collections.unmodifiableSet( properties.entrySet() );
   }

   public boolean equals( Object obj ) {
      if( obj == this ){
         return true;
      }
      if( obj == null ){
         return false;
      }

      if( !(obj instanceof JSONObject) ){
         return false;
      }

      JSONObject other = (JSONObject) obj;

      if( isNullObject() ){
         if( other.isNullObject() ){
            return true;
         }else{
            return false;
         }
      }else{
         if( other.isNullObject() ){
            return false;
         }
      }

      if( other.size() != size() ){
         return false;
      }

      for( Iterator keys = properties.keySet()
            .iterator(); keys.hasNext(); ){
         String key = (String) keys.next();
         if( !other.properties.containsKey( key ) ){
            return false;
         }
         Object o1 = properties.get( key );
         Object o2 = other.properties.get( key );

         if( JSONNull.getInstance()
               .equals( o1 ) ){
            if( JSONNull.getInstance()
                  .equals( o2 ) ){
               continue;
            }else{
               return false;
            }
         }else{
            if( JSONNull.getInstance()
                  .equals( o2 ) ){
               return false;
            }
         }

         if( o1 instanceof String && o2 instanceof JSONFunction ){
            if( !o1.equals( String.valueOf( o2 ) ) ){
               return false;
            }
         }else if( o1 instanceof JSONFunction && o2 instanceof String ){
            if( !o2.equals( String.valueOf( o1 ) ) ){
               return false;
            }
         }else if( o1 instanceof JSONObject && o2 instanceof JSONObject ){
            if( !o1.equals( o2 ) ){
               return false;
            }
         }else if( o1 instanceof JSONArray && o2 instanceof JSONArray ){
            if( !o1.equals( o2 ) ){
               return false;
            }
         }else if( o1 instanceof JSONFunction && o2 instanceof JSONFunction ){
            if( !o1.equals( o2 ) ){
               return false;
            }
         }else{
            if( o1 instanceof String ){
               if( !o1.equals( String.valueOf( o2 ) ) ){
                  return false;
               }
            }else if( o2 instanceof String ){
               if( !o2.equals( String.valueOf( o1 ) ) ){
                  return false;
               }
            }else{
               Morpher m1 = JSONUtils.getMorpherRegistry()
                     .getMorpherFor( o1.getClass() );
               Morpher m2 = JSONUtils.getMorpherRegistry()
                     .getMorpherFor( o2.getClass() );
               if( m1 != null && m1 != IdentityObjectMorpher.getInstance() ){
                  if( !o1.equals( JSONUtils.getMorpherRegistry()
                        .morph( o1.getClass(), o2 ) ) ){
                     return false;
                  }
               }else if( m2 != null && m2 != IdentityObjectMorpher.getInstance() ){
                  if( !JSONUtils.getMorpherRegistry()
                        .morph( o1.getClass(), o1 )
                        .equals( o2 ) ){
                     return false;
                  }
               }else{
                  if( !o1.equals( o2 ) ){
                     return false;
                  }
               }
            }
         }
      }
      return true;
   }

   public Object get( Object key ) {
      if( key instanceof String ){
         return get( (String) key );
      }
      return null;
   }

   /**
    * Get the value object associated with a key.
    *
    * @param key A key string.
    * @return The object associated with the key.
    * @throws JSONException if this.isNull() returns true.
    */
   public Object get( String key ) {
      verifyIsNull();
      return this.properties.get( key );
   }

   /**
    * Get the boolean value associated with a key.
    *
    * @param key A key string.
    * @return The truth.
    * @throws JSONException if the value is not a Boolean or the String "true"
    *         or "false".
    */
   public boolean getBoolean( String key ) {
      verifyIsNull();
      Object o = get( key );
      if( o != null ){
         if( o.equals( Boolean.FALSE )
               || (o instanceof String && ((String) o).equalsIgnoreCase( "false" )) ){
            return false;
         }else if( o.equals( Boolean.TRUE )
               || (o instanceof String && ((String) o).equalsIgnoreCase( "true" )) ){
            return true;
         }
      }
      throw new JSONException( "JSONObject[" + JSONUtils.quote( key ) + "] is not a Boolean." );
   }

   /**
    * Get the double value associated with a key.
    *
    * @param key A key string.
    * @return The numeric value.
    * @throws JSONException if the key is not found or if the value is not a
    *         Number object and cannot be converted to a number.
    */
   public double getDouble( String key ) {
      verifyIsNull();
      Object o = get( key );
      if( o != null ){
         try{
            return o instanceof Number ? ((Number) o).doubleValue()
                  : Double.parseDouble( (String) o );
         }catch( Exception e ){
            throw new JSONException( "JSONObject[" + JSONUtils.quote( key ) + "] is not a number." );
         }
      }
      throw new JSONException( "JSONObject[" + JSONUtils.quote( key ) + "] is not a number." );
   }

   /**
    * Get the int value associated with a key. If the number value is too large
    * for an int, it will be clipped.
    *
    * @param key A key string.
    * @return The integer value.
    * @throws JSONException if the key is not found or if the value cannot be
    *         converted to an integer.
    */
   public int getInt( String key ) {
      verifyIsNull();
      Object o = get( key );
      if( o != null ){
         return o instanceof Number ? ((Number) o).intValue() : (int) getDouble( key );
      }
      throw new JSONException( "JSONObject[" + JSONUtils.quote( key ) + "] is not a number." );
   }

   /**
    * Get the JSONArray value associated with a key.
    *
    * @param key A key string.
    * @return A JSONArray which is the value.
    * @throws JSONException if the key is not found or if the value is not a
    *         JSONArray.
    */
   public JSONArray getJSONArray( String key ) {
      verifyIsNull();
      Object o = get( key );
      if( o != null && o instanceof JSONArray ){
         return (JSONArray) o;
      }
      throw new JSONException( "JSONObject[" + JSONUtils.quote( key ) + "] is not a JSONArray." );
   }

   /**
    * Get the JSONObject value associated with a key.
    *
    * @param key A key string.
    * @return A JSONObject which is the value.
    * @throws JSONException if the key is not found or if the value is not a
    *         JSONObject.
    */
   public JSONObject getJSONObject( String key ) {
      verifyIsNull();
      Object o = get( key );
      if( JSONNull.getInstance()
            .equals( o ) ){
         return new JSONObject( true );
      }else if( o instanceof JSONObject ){
         return (JSONObject) o;
      }
      throw new JSONException( "JSONObject[" + JSONUtils.quote( key ) + "] is not a JSONObject." );
   }

   /**
    * Get the long value associated with a key. If the number value is too long
    * for a long, it will be clipped.
    *
    * @param key A key string.
    * @return The long value.
    * @throws JSONException if the key is not found or if the value cannot be
    *         converted to a long.
    */
   public long getLong( String key ) {
      verifyIsNull();
      Object o = get( key );
      if( o != null ){
         return o instanceof Number ? ((Number) o).longValue() : (long) getDouble( key );
      }
      throw new JSONException( "JSONObject[" + JSONUtils.quote( key ) + "] is not a number." );
   }

   /**
    * Get the string associated with a key.
    *
    * @param key A key string.
    * @return A string which is the value.
    * @throws JSONException if the key is not found.
    */
   public String getString( String key ) {
      verifyIsNull();
      Object o = get( key );
      if( o != null ){
         return o.toString();
      }
      throw new JSONException( "JSONObject[" + JSONUtils.quote( key ) + "] not found." );
   }

   /**
    * Determine if the JSONObject contains a specific key.
    *
    * @param key A key string.
    * @return true if the key exists in the JSONObject.
    */
   public boolean has( String key ) {
      verifyIsNull();
      return this.properties.containsKey( key );
   }

   public int hashCode() {
      int hashcode = 19;
      if( isNullObject() ){
         return hashcode + JSONNull.getInstance()
               .hashCode();
      }
      for( Iterator entries = properties.entrySet()
            .iterator(); entries.hasNext(); ){
         Map.Entry entry = (Map.Entry) entries.next();
         Object key = entry.getKey();
         Object value = entry.getValue();
         hashcode += key.hashCode() + JSONUtils.hashCode( value );
      }
      return hashcode;
   }

   public boolean isArray() {
      return false;
   }

   public boolean isEmpty() {
      // verifyIsNull();
      return this.properties.isEmpty();
   }

   /**
    * Returs if this object is a null JSONObject.
    */
   public boolean isNullObject() {
      return nullObject;
   }

   /**
    * Get an enumeration of the keys of the JSONObject.
    *
    * @return An iterator of the keys.
    */
   public Iterator keys() {
      verifyIsNull();
      return keySet().iterator();
   }

   public Set keySet() {
      return Collections.unmodifiableSet( properties.keySet() );
   }

   /**
    * Produce a JSONArray containing the names of the elements of this
    * JSONObject.
    *
    * @return A JSONArray containing the key strings, or null if the JSONObject
    *         is empty.
    */
   public JSONArray names() {
      verifyIsNull();
      JSONArray ja = new JSONArray();
      Iterator keys = keys();
      while( keys.hasNext() ){
         ja.element( keys.next() );
      }
      return ja;
   }

   /**
    * Produce a JSONArray containing the names of the elements of this
    * JSONObject.
    *
    * @return A JSONArray containing the key strings, or null if the JSONObject
    *         is empty.
    */
   public JSONArray names( JsonConfig jsonConfig ) {
      verifyIsNull();
      JSONArray ja = new JSONArray();
      Iterator keys = keys();
      while( keys.hasNext() ){
         ja.element( keys.next(), jsonConfig );
      }
      return ja;
   }

   /**
    * Get an optional value associated with a key.
    *
    * @param key A key string.
    * @return An object which is the value, or null if there is no value.
    */
   public Object opt( String key ) {
      verifyIsNull();
      return key == null ? null : this.properties.get( key );
   }

   /**
    * Get an optional boolean associated with a key. It returns false if there
    * is no such key, or if the value is not Boolean.TRUE or the String "true".
    *
    * @param key A key string.
    * @return The truth.
    */
   public boolean optBoolean( String key ) {
      verifyIsNull();
      return optBoolean( key, false );
   }

   /**
    * Get an optional boolean associated with a key. It returns the defaultValue
    * if there is no such key, or if it is not a Boolean or the String "true" or
    * "false" (case insensitive).
    *
    * @param key A key string.
    * @param defaultValue The default.
    * @return The truth.
    */
   public boolean optBoolean( String key, boolean defaultValue ) {
      verifyIsNull();
      try{
         return getBoolean( key );
      }catch( Exception e ){
         return defaultValue;
      }
   }

   /**
    * Get an optional double associated with a key, or NaN if there is no such
    * key or if its value is not a number. If the value is a string, an attempt
    * will be made to evaluate it as a number.
    *
    * @param key A string which is the key.
    * @return An object which is the value.
    */
   public double optDouble( String key ) {
      verifyIsNull();
      return optDouble( key, Double.NaN );
   }

   /**
    * Get an optional double associated with a key, or the defaultValue if there
    * is no such key or if its value is not a number. If the value is a string,
    * an attempt will be made to evaluate it as a number.
    *
    * @param key A key string.
    * @param defaultValue The default.
    * @return An object which is the value.
    */
   public double optDouble( String key, double defaultValue ) {
      verifyIsNull();
      try{
         Object o = opt( key );
         return o instanceof Number ? ((Number) o).doubleValue()
               : new Double( (String) o ).doubleValue();
      }catch( Exception e ){
         return defaultValue;
      }
   }

   /**
    * Get an optional int value associated with a key, or zero if there is no
    * such key or if the value is not a number. If the value is a string, an
    * attempt will be made to evaluate it as a number.
    *
    * @param key A key string.
    * @return An object which is the value.
    */
   public int optInt( String key ) {
      verifyIsNull();
      return optInt( key, 0 );
   }

   /**
    * Get an optional int value associated with a key, or the default if there
    * is no such key or if the value is not a number. If the value is a string,
    * an attempt will be made to evaluate it as a number.
    *
    * @param key A key string.
    * @param defaultValue The default.
    * @return An object which is the value.
    */
   public int optInt( String key, int defaultValue ) {
      verifyIsNull();
      try{
         return getInt( key );
      }catch( Exception e ){
         return defaultValue;
      }
   }

   /**
    * Get an optional JSONArray associated with a key. It returns null if there
    * is no such key, or if its value is not a JSONArray.
    *
    * @param key A key string.
    * @return A JSONArray which is the value.
    */
   public JSONArray optJSONArray( String key ) {
      verifyIsNull();
      Object o = opt( key );
      return o instanceof JSONArray ? (JSONArray) o : null;
   }

   /**
    * Get an optional JSONObject associated with a key. It returns null if there
    * is no such key, or if its value is not a JSONObject.
    *
    * @param key A key string.
    * @return A JSONObject which is the value.
    */
   public JSONObject optJSONObject( String key ) {
      verifyIsNull();
      Object o = opt( key );
      return o instanceof JSONObject ? (JSONObject) o : null;
   }

   /**
    * Get an optional long value associated with a key, or zero if there is no
    * such key or if the value is not a number. If the value is a string, an
    * attempt will be made to evaluate it as a number.
    *
    * @param key A key string.
    * @return An object which is the value.
    */
   public long optLong( String key ) {
      verifyIsNull();
      return optLong( key, 0 );
   }

   /**
    * Get an optional long value associated with a key, or the default if there
    * is no such key or if the value is not a number. If the value is a string,
    * an attempt will be made to evaluate it as a number.
    *
    * @param key A key string.
    * @param defaultValue The default.
    * @return An object which is the value.
    */
   public long optLong( String key, long defaultValue ) {
      verifyIsNull();
      try{
         return getLong( key );
      }catch( Exception e ){
         return defaultValue;
      }
   }

   /**
    * Get an optional string associated with a key. It returns an empty string
    * if there is no such key. If the value is not a string and is not null,
    * then it is coverted to a string.
    *
    * @param key A key string.
    * @return A string which is the value.
    */
   public String optString( String key ) {
      verifyIsNull();
      return optString( key, "" );
   }

   /**
    * Get an optional string associated with a key. It returns the defaultValue
    * if there is no such key.
    *
    * @param key A key string.
    * @param defaultValue The default.
    * @return A string which is the value.
    */
   public String optString( String key, String defaultValue ) {
      verifyIsNull();
      Object o = opt( key );
      return o != null ? o.toString() : defaultValue;
   }

   public Object put( String key, Object value ) {
      if( key == null ){
         throw new IllegalArgumentException( "key is null." );
      }
      Object previous = properties.get( key );
      element( String.valueOf( key ), value );
      return previous;
   }

   public void putAll( Map map ) {
      putAll( map, new JsonConfig() );
   }

   public void putAll( Map map, JsonConfig jsonConfig ) {
      if( map instanceof JSONObject ){
         for( Iterator entries = map.entrySet()
               .iterator(); entries.hasNext(); ){
            Map.Entry entry = (Map.Entry) entries.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            this.properties.put( key, value );
         }
      }else{
         for( Iterator entries = map.entrySet()
               .iterator(); entries.hasNext(); ){
            Map.Entry entry = (Map.Entry) entries.next();
            String key = String.valueOf( entry.getKey() );
            Object value = entry.getValue();
            element( key, value, jsonConfig );
         }
      }
   }

   public Object remove( Object key ) {
      return properties.remove( key );
   }

   /**
    * Remove a name and its value, if present.
    *
    * @param key The name to be removed.
    * @return The value that was associated with the name, or null if there was
    *         no value.
    */
   public Object remove( String key ) {
      verifyIsNull();
      return this.properties.remove( key );
   }

   /**
    * Get the number of keys stored in the JSONObject.
    *
    * @return The number of keys in the JSONObject.
    */
   public int size() {
      // verifyIsNull();
      return this.properties.size();
   }

   /**
    * Produce a JSONArray containing the values of the members of this
    * JSONObject.
    *
    * @param names A JSONArray containing a list of key strings. This determines
    *        the sequence of the values in the result.
    * @return A JSONArray of values.
    * @throws JSONException If any of the values are non-finite numbers.
    */
   public JSONArray toJSONArray( JSONArray names ) {
      verifyIsNull();
      if( names == null || names.size() == 0 ){
         return null;
      }
      JSONArray ja = new JSONArray();
      for( int i = 0; i < names.size(); i += 1 ){
         ja.element( this.opt( names.getString( i ) ) );
      }
      return ja;
   }

   /**
    * Make a JSON text of this JSONObject. For compactness, no whitespace is
    * added. If this would not result in a syntactically correct JSON text, then
    * null will be returned instead.
    * <p>
    * Warning: This method assumes that the data structure is acyclical.
    *
    * @return a printable, displayable, portable, transmittable representation
    *         of the object, beginning with <code>{</code>&nbsp;<small>(left
    *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
    *         brace)</small>.
    */
   public String toString() {
      if( isNullObject() ){
         return JSONNull.getInstance()
               .toString();
      }
      try{
         Iterator keys = keys();
         StringBuffer sb = new StringBuffer( "{" );

         while( keys.hasNext() ){
            if( sb.length() > 1 ){
               sb.append( ',' );
            }
            Object o = keys.next();
            sb.append( JSONUtils.quote( o.toString() ) );
            sb.append( ':' );
            sb.append( JSONUtils.valueToString( this.properties.get( o ) ) );
         }
         sb.append( '}' );
         return sb.toString();
      }catch( Exception e ){
         return null;
      }
   }

   /**
    * Make a prettyprinted JSON text of this JSONObject.
    * <p>
    * Warning: This method assumes that the data structure is acyclical.
    *
    * @param indentFactor The number of spaces to add to each level of
    *        indentation.
    * @return a printable, displayable, portable, transmittable representation
    *         of the object, beginning with <code>{</code>&nbsp;<small>(left
    *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
    *         brace)</small>.
    * @throws JSONException If the object contains an invalid number.
    */
   public String toString( int indentFactor ) {
      if( isNullObject() ){
         return JSONNull.getInstance()
               .toString();
      }
      if( indentFactor == 0 ){
         return this.toString();
      }
      return toString( indentFactor, 0 );
   }

   /**
    * Make a prettyprinted JSON text of this JSONObject.
    * <p>
    * Warning: This method assumes that the data structure is acyclical.
    *
    * @param indentFactor The number of spaces to add to each level of
    *        indentation.
    * @param indent The indentation of the top level.
    * @return a printable, displayable, transmittable representation of the
    *         object, beginning with <code>{</code>&nbsp;<small>(left brace)</small>
    *         and ending with <code>}</code>&nbsp;<small>(right brace)</small>.
    * @throws JSONException If the object contains an invalid number.
    */
   public String toString( int indentFactor, int indent ) {
      if( isNullObject() ){
         return JSONNull.getInstance()
               .toString();
      }
      int i;
      int n = size();
      if( n == 0 ){
         return "{}";
      }
      if( indentFactor == 0 ){
         return this.toString();
      }
      Iterator keys = keys();
      StringBuffer sb = new StringBuffer( "{" );
      int newindent = indent + indentFactor;
      Object o;
      if( n == 1 ){
         o = keys.next();
         sb.append( JSONUtils.quote( o.toString() ) );
         sb.append( ": " );
         sb.append( JSONUtils.valueToString( this.properties.get( o ), indentFactor, indent ) );
      }else{
         while( keys.hasNext() ){
            o = keys.next();
            if( sb.length() > 1 ){
               sb.append( ",\n" );
            }else{
               sb.append( '\n' );
            }
            for( i = 0; i < newindent; i += 1 ){
               sb.append( ' ' );
            }
            sb.append( JSONUtils.quote( o.toString() ) );
            sb.append( ": " );
            sb.append( JSONUtils.valueToString( this.properties.get( o ), indentFactor, newindent ) );
         }
         if( sb.length() > 1 ){
            sb.append( '\n' );
            for( i = 0; i < indent; i += 1 ){
               sb.append( ' ' );
            }
         }
         for( i = 0; i < indent; i += 1 ){
            sb.insert( 0, ' ' );
         }
      }
      sb.append( '}' );
      return sb.toString();
   }

   public Collection values() {
      return Collections.unmodifiableCollection( properties.values() );
   }

   /**
    * Write the contents of the JSONObject as JSON text to a writer. For
    * compactness, no whitespace is added.
    * <p>
    * Warning: This method assumes that the data structure is acyclical.
    *
    * @return The writer.
    * @throws JSONException
    */
   protected void write(Writer writer, WritingVisitor visitor) throws IOException {
      try{
         if( isNullObject() ){
            writer.write( JSONNull.getInstance()
                  .toString());
         }

         boolean b = false;
         Iterator keys = visitor.keySet(this).iterator();
         writer.write( '{' );

         while( keys.hasNext() ){
            if( b ){
               writer.write( ',' );
            }
            Object k = keys.next();
            writer.write( JSONUtils.quote( k.toString() ) );
            writer.write( ':' );
            Object v = this.properties.get( k );
            if( v instanceof JSON ){
               visitor.on( (JSON) v, writer );
            }else{
               visitor.on( v, writer );
            }
            b = true;
         }
         writer.write( '}' );
      }catch( IOException e ){
         throw new JSONException( e );
      }
   }

   private JSONObject _accumulate( String key, Object value, JsonConfig jsonConfig ) {
      if( isNullObject() ){
         throw new JSONException( "Can't accumulate on null object" );
      }

      if( !has( key ) ){
         setInternal( key, value, jsonConfig );
      }else{
         Object o = opt( key );
         if( o instanceof JSONArray ){
            ((JSONArray) o).element( value, jsonConfig );
         }else{
            setInternal( key, new JSONArray().element( o )
                  .element( value, jsonConfig ), jsonConfig );
         }
      }

      return this;
   }

   protected Object _processValue( Object value, JsonConfig jsonConfig ) {
      if( value instanceof JSONTokener ) {
         return _fromJSONTokener( (JSONTokener) value, jsonConfig );
      }else if( value != null && Enum.class.isAssignableFrom( value.getClass() ) ){
         return ((Enum) value).name();
      }
      return super._processValue( value, jsonConfig );
   }

   /**
    * Put a key/value pair in the JSONObject.
    *
    * @param key A key string.
    * @param value An object which is the value. It should be of one of these
    *        types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
    *        String, or the JSONNull object.
    * @return this.
    * @throws JSONException If the value is non-finite number or if the key is
    *         null.
    */
   private JSONObject _setInternal( String key, Object value, JsonConfig jsonConfig ) {
      verifyIsNull();
      if( key == null ){
         throw new JSONException( "Null key." );
      }

      if( JSONUtils.isString( value ) && JSONUtils.mayBeJSON( String.valueOf( value ) ) ){
         this.properties.put( key, value );
      }else{
         /*
         Object jo = _processValue( value, jsonConfig );
         if( CycleDetectionStrategy.IGNORE_PROPERTY_OBJ == jo
               || CycleDetectionStrategy.IGNORE_PROPERTY_ARR == jo ){
            // do nothing
         }else{
            this.properties.put( key, jo );
         }
         */
         if( CycleDetectionStrategy.IGNORE_PROPERTY_OBJ == value
               || CycleDetectionStrategy.IGNORE_PROPERTY_ARR == value ){
            // do nothing
         }else{
            this.properties.put( key, value );
         }
      }

      return this;
   }

   private Object processValue( Object value, JsonConfig jsonConfig ) {
      if( value != null ){
         JsonValueProcessor processor = jsonConfig.findJsonValueProcessor( value.getClass() );
         if( processor != null ){
            value = processor.processObjectValue( null, value, jsonConfig );
            if( !JsonVerifier.isValidJsonValue( value ) ){
               throw new JSONException( "Value is not a valid JSON value. " + value );
            }
         }
      }
      return _processValue( value, jsonConfig );
   }

   private Object processValue( String key, Object value, JsonConfig jsonConfig ) {
      if( value != null ){
         JsonValueProcessor processor = jsonConfig.findJsonValueProcessor( value.getClass(), key );
         if( processor != null ){
            value = processor.processObjectValue( null, value, jsonConfig );
            if( !JsonVerifier.isValidJsonValue( value ) ){
               throw new JSONException( "Value is not a valid JSON value. " + value );
            }
         }
      }
      return _processValue( value, jsonConfig );
   }

   /**
    * Put a key/value pair in the JSONObject.
    *
    * @param key A key string.
    * @param value An object which is the value. It should be of one of these
    *        types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
    *        String, or the JSONNull object.
    * @return this.
    * @throws JSONException If the value is non-finite number or if the key is
    *         null.
    */
   private JSONObject setInternal( String key, Object value, JsonConfig jsonConfig ) {
      return _setInternal( key, processValue( key, value, jsonConfig ), jsonConfig );
   }

   /**
    * Checks if this object is a "null" object.
    */
   private void verifyIsNull() {
      if( isNullObject() ){
         throw new JSONException( "null object" );
      }
   }
}
