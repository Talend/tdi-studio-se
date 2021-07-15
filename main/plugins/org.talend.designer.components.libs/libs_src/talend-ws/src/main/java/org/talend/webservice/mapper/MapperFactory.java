/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.mapper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.talend.webservice.exception.LocalizedException;
import org.talend.webservice.jaxb.JAXBUtils;

/**
 * 
 * @author rlamarche
 */
public class MapperFactory {

    private static final Map<String, String> BUILTIN_DATATYPES_MAP;

    private static final Map<String, String> BUILTIN_DATATYPES_MAP_REVERSE;

    private static final String W3C_XML_SCHEMA_URI = "http://www.w3.org/2001/XMLSchema";
    
    private static final String W3C_XML_SCHEMA_DATETYPES_URI = "http://www.w3.org/2001/XMLSchema-datatypes";
    
    private static final QName ANYTYPE_QNAME = new QName(W3C_XML_SCHEMA_URI, "anyType");

    private static String byteArrayName = new byte[0].getClass().getName();
    // refer to :http://www.w3.org/TR/xmlschema-2/

    static {
        BUILTIN_DATATYPES_MAP = new HashMap<String, String>();
        BUILTIN_DATATYPES_MAP.put("string", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("integer", "java.math.BigInteger");
        BUILTIN_DATATYPES_MAP.put("int", "java.lang.Integer");
        BUILTIN_DATATYPES_MAP.put("long", "java.lang.Long");
        BUILTIN_DATATYPES_MAP.put("short", "java.lang.Short");
        BUILTIN_DATATYPES_MAP.put("decimal", "java.math.BigDecimal");
        BUILTIN_DATATYPES_MAP.put("float", "java.lang.Float");
        BUILTIN_DATATYPES_MAP.put("double", "java.lang.Double");
        BUILTIN_DATATYPES_MAP.put("boolean", "java.lang.Boolean");
        BUILTIN_DATATYPES_MAP.put("byte", "java.lang.Byte");
        BUILTIN_DATATYPES_MAP.put("QName", "javax.xml.namespace.QName");
        BUILTIN_DATATYPES_MAP.put("dateTime", "javax.xml.datatype.XMLGregorianCalendar");
        BUILTIN_DATATYPES_MAP.put("base64Binary", byteArrayName);
        BUILTIN_DATATYPES_MAP.put("hexBinary", byteArrayName);
        BUILTIN_DATATYPES_MAP.put("unsignedInt", "java.lang.Long");
        BUILTIN_DATATYPES_MAP.put("unsignedShort", "java.lang.Integer");
        BUILTIN_DATATYPES_MAP.put("unsignedByte", "java.lang.Short");
        BUILTIN_DATATYPES_MAP.put("time", "javax.xml.datatype.XMLGregorianCalendar");
        BUILTIN_DATATYPES_MAP.put("date", "javax.xml.datatype.XMLGregorianCalendar");
        BUILTIN_DATATYPES_MAP.put("gYear", "javax.xml.datatype.XMLGregorianCalendar");
        BUILTIN_DATATYPES_MAP.put("gYearMonth", "javax.xml.datatype.XMLGregorianCalendar");
        BUILTIN_DATATYPES_MAP.put("gMonth", "javax.xml.datatype.XMLGregorianCalendar");
        BUILTIN_DATATYPES_MAP.put("gMonthDay", "javax.xml.datatype.XMLGregorianCalendar");
        BUILTIN_DATATYPES_MAP.put("gDay", "javax.xml.datatype.XMLGregorianCalendar");
        BUILTIN_DATATYPES_MAP.put("duration", "javax.xml.datatype.Duration");
        BUILTIN_DATATYPES_MAP.put("NOTATION", "javax.xml.namespace.QName");
        BUILTIN_DATATYPES_MAP.put("anyURI", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("token", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("IDREF", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("NCName", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("ENTITY", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("normalizedString", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("language", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("Name", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("NMTOKEN", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("NMTOKENS", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("ID", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("IDREFS", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("ENTITIES", "java.lang.String");
        BUILTIN_DATATYPES_MAP.put("nonPositiveInteger", "java.math.BigInteger");
        BUILTIN_DATATYPES_MAP.put("nonNegativeInteger", "java.math.BigInteger");
        BUILTIN_DATATYPES_MAP.put("negativeInteger", "java.math.BigInteger");
        BUILTIN_DATATYPES_MAP.put("positiveInteger", "java.math.BigInteger");
        BUILTIN_DATATYPES_MAP.put("unsignedLong", "java.math.BigInteger");

        BUILTIN_DATATYPES_MAP_REVERSE = new HashMap<String, String>();
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.String", "string");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.math.BigInteger", "integer");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Integer", "int");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Long", "long");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Short", "short");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.math.BigDecimal", "decimal");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Float", "float");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Double", "double");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Boolean", "boolean");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Byte", "byte");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.namespace.QName", "QName");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.XMLGregorianCalendar", "dateTime");
        BUILTIN_DATATYPES_MAP_REVERSE.put(byteArrayName, "base64Binary");
        BUILTIN_DATATYPES_MAP_REVERSE.put(byteArrayName, "hexBinary");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Long", "unsignedInt");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Short", "unsignedShort");
        BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.Byte", "unsignedByte");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.XMLGregorianCalendar", "time");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.XMLGregorianCalendar", "date");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.XMLGregorianCalendar", "gYear");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.XMLGregorianCalendar", "gYearMonth");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.XMLGregorianCalendar", "gMonth");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.XMLGregorianCalendar", "gMonthDay");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.XMLGregorianCalendar", "gDay");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.datatype.Duration", "duration");
        BUILTIN_DATATYPES_MAP_REVERSE.put("javax.xml.namespace.QName", "NOTATION");
        // BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.String", "anyURI"); // bug13001
        // BUILTIN_DATATYPES_MAP_REVERSE.put("java.lang.String", "token"); // bug13001

    }

    protected ClassMapper classMapper;

    protected XmlSchemaCollection schemaCollection;

    // bug 13001 nested call type
    protected Map<QName, TypeMapper> typeMappers;

    public MapperFactory(ClassMapper classMapper, XmlSchemaCollection schemaCollection) {
        this.classMapper = classMapper;
        this.schemaCollection = schemaCollection;
        this.typeMappers = new HashMap<QName, TypeMapper>();
    }

    public XmlSchemaCollection getSchemaCollection() {
        return schemaCollection;
    }

    public ClassMapper getClassMapper() {
        return classMapper;
    }

    /**
     * Return the message mapper or null if the message does not have any parts or is null
     * 
     * @param message
     * @return
     */
    public MessageMapper createMessageMapper(Message message) throws LocalizedException {
        if (message == null) {
            return new EmptyMessageMapper();
        }

        Map<String, Part> parts = message.getParts();
        if (parts == null || parts != null && parts.size() == 0) {
            return new EmptyMessageMapper();
        } else {
            Map<String, TypeMapper> mappers = new HashMap<String, TypeMapper>(parts.size());

            for (Map.Entry<String, Part> entry : parts.entrySet()) {
                String name = entry.getKey();
                Part part = entry.getValue();

                XmlSchemaType xmlSchemaType = null;
                QName elementName = part.getElementName();
                if (elementName == null) {
                    QName typeName = part.getTypeName();
                    if (typeName == null) {
                        throw new IllegalArgumentException("Unable to find type of part " + part.getName() + " for message "
                                + message.getQName());
                    }

                    xmlSchemaType = schemaCollection.getTypeByQName(typeName);
                    if (xmlSchemaType.getName() == null) {
                        xmlSchemaType.setName(typeName.getLocalPart());
                    }
                } else {
                    XmlSchemaElement xmlSchemaElement = schemaCollection.getElementByQName(part.getElementName());
                    xmlSchemaType = xmlSchemaElement.getSchemaType();
                    if (xmlSchemaType.getName() == null) {
                        xmlSchemaType.setName(xmlSchemaElement.getName());
                    }
                }
                if (xmlSchemaType == null) {
                    throw new IllegalArgumentException("Type " + part.getElementName() + " was not found in the schema.");
                }

                mappers.put(name, createTypeMapper(xmlSchemaType));
            }

            return new MessageMapperImpl(mappers, message);
        }
    }

    protected TypeMapper createTypeMapper(XmlSchemaType xmlSchemaType) throws LocalizedException {
        if (!typeMappers.containsKey(xmlSchemaType.getQName())) {
            typeMappers.put(xmlSchemaType.getQName(), null);
            TypeMapper typeMapper = null;
            if (xmlSchemaType instanceof XmlSchemaComplexType) {
                typeMapper = createComplexTypeMapper((XmlSchemaComplexType) xmlSchemaType);
            } else if (xmlSchemaType instanceof XmlSchemaSimpleType) {
                if (ANYTYPE_QNAME.equals(xmlSchemaType.getQName())) {
                    typeMapper = new AnyTypeMapper(this);
                } else {
                    typeMapper = createSimpleTypeMapper((XmlSchemaSimpleType) xmlSchemaType);
                }
            } else {
                throw new IllegalArgumentException("Type " + xmlSchemaType.getClass().getName() + " is not yes supported.");
            }
            typeMappers.put(xmlSchemaType.getQName(), typeMapper);
            return typeMapper;
        } else {
            return typeMappers.get(xmlSchemaType.getQName());
        }
    }

    private TypeMapper createSimpleTypeMapper(XmlSchemaSimpleType xmlSchemaSimpleType) {

        XmlSchemaSimpleTypeContent xmlSchemaSimpleTypeContent = xmlSchemaSimpleType.getContent();

        QName qname = xmlSchemaSimpleType.getQName();
        String namespaceuri = qname == null ? null : qname.getNamespaceURI();
        
        // simple type
        String simpleClassName = builtInTypeToJavaType(xmlSchemaSimpleType.getName());
        
        if (simpleClassName != null && (W3C_XML_SCHEMA_URI.equals(namespaceuri) || W3C_XML_SCHEMA_DATETYPES_URI.equals(namespaceuri))) {
            Class<?> clazz;
            try {
                clazz = Class.forName(simpleClassName);
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException("Unable to find java type " + simpleClassName, ex);
            }

            return new SimpleTypeMapper(clazz);

        } else if (xmlSchemaSimpleTypeContent instanceof XmlSchemaSimpleTypeList) {
            // using java.util.list to map xmlschemasimpletypelist
            // bug 13922
            Class<?> clazz;
            try {
                clazz = Class.forName("java.util.List");
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException("Unable to find java type java.util.List", ex);
            }

            return new SimpleTypeMapper(clazz);
        } else if (xmlSchemaSimpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
            // Enum or others

            // bug 13001 for
            // <xsd:simpleType name="EventType">
            // <xsd:restriction base="xsd:string"/>
            // </xsd:simpleType>
            // and not enum
            // and enum have not typename,means jaxb didn't gen class for the enum without simpleType.getName
            XmlSchemaSimpleTypeRestriction xmlSchemaSimpleTypeRestriction = (XmlSchemaSimpleTypeRestriction) xmlSchemaSimpleTypeContent;

            List<XmlSchemaFacet> facets = xmlSchemaSimpleTypeRestriction.getFacets();
            if (facets.size() > 0 && facets.get(0) instanceof XmlSchemaEnumerationFacet && xmlSchemaSimpleType.getName() != null) {
                Class<?> clazz = classMapper.getClassForType(xmlSchemaSimpleType.getQName());
                if (!clazz.isEnum()) {
                    throw new IllegalArgumentException("Class " + clazz.getName() + " should be an enum.");
                }
                return new EnumTypeMapper(clazz);
            } else {
                String className = builtInTypeToJavaType(((XmlSchemaSimpleTypeRestriction) xmlSchemaSimpleTypeContent)
                        .getBaseTypeName().getLocalPart());
                if (className == null) {
                    throw new IllegalArgumentException("Unsupported type " + xmlSchemaSimpleType.getQName());
                }
                Class<?> clazz;
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException ex) {
                    throw new IllegalArgumentException("Unable to find java type " + className, ex);
                }
                return new SimpleTypeMapper(clazz);
            }
        } else if (xmlSchemaSimpleTypeContent instanceof XmlSchemaSimpleTypeUnion) {
            Class<?> clazz;
            try {
                clazz = Class.forName("java.lang.String");
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException("Unable to find java type java.lang.String", ex);
            }
            return new SimpleTypeMapper(clazz);
        } else {
            throw new IllegalArgumentException("Unsupported type " + xmlSchemaSimpleTypeContent.getClass().getName());
        }
    }

    private ComplexTypeMapper createComplexTypeMapper(XmlSchemaComplexType xmlSchemaComplexType) throws LocalizedException {
        // 0. if that class is abstract, find the instance class
        Map<QName, ComplexTypeMapper> instanceComplexTypeMapper = null;
        if (xmlSchemaComplexType.isAbstract()) {
            instanceComplexTypeMapper = findInstance(xmlSchemaComplexType.getQName());
        }
        // 1. get the all properties from complexType
        Map<String, Object> properties = getProperties(xmlSchemaComplexType);

        Map<String, Object> tempProperties = new ListOrderedMap();
        for (String key : properties.keySet()) {
            tempProperties.put(JAXBUtils.removePunctuation(key), properties.get(key));
        }
        properties.clear();
        properties.putAll(tempProperties);

        // 2. get class with typeMapperQname(type Qname, element Qname, inner class Qname)
        // and use properties to check the class name(class,class1,class2)
        ListOrderedMap orderedMap = (ListOrderedMap) properties;
        Class<?> clazz = classMapper.getClassForType(xmlSchemaComplexType.getQName(), orderedMap.keyList(), 1);

        // 3.create propertyMapper (propertyName,class,schemaTypeMap,typeMapperQname)
        //need to use the order map as the order is important for response auto parser by index
        Map<String, PropertyMapper> mappers = new LinkedHashMap<>();
        for (String key : properties.keySet()) {
            Object xmlSchemaObject = properties.get(key);
            if (xmlSchemaObject == null) {
                if (AnyPropertyMapper.LABEL.equals(key)) {
                    mappers.put(key, new AnyPropertyMapper(clazz, this));
                }
            } else if (xmlSchemaObject instanceof XmlSchemaElement) {
                mappers.put(key, createPropertyMapper((XmlSchemaElement) xmlSchemaObject, clazz, key));

            } else if (xmlSchemaObject instanceof XmlSchemaType) {
                mappers.put(key, createPropertyMapper((XmlSchemaType) xmlSchemaObject, clazz, key));
            }
        }

        // 4.create new ComplexTypeMapper
        ComplexTypeMapper complexTypeMapper = new ComplexTypeMapper(mappers, clazz, orderedMap.keyList(),
                xmlSchemaComplexType.getQName());
        if (instanceComplexTypeMapper != null && !instanceComplexTypeMapper.isEmpty()) {
            complexTypeMapper.setInstanceComplexTypeMapper(instanceComplexTypeMapper);
        }
        return complexTypeMapper;
    }

    /**
     * DOC bchen Comment method "findInstance".
     * 
     * @param qName
     * @throws LocalizedException
     */
    private Map<QName, ComplexTypeMapper> findInstance(QName abstractQName) throws LocalizedException {
        Map<QName, ComplexTypeMapper> instanceTypeMappers = new HashMap<QName, ComplexTypeMapper>();
        XmlSchema[] xmlSchemas = schemaCollection.getXmlSchemas();
        for (XmlSchema xmlSchema : xmlSchemas) {
            Map<QName, XmlSchemaType> xmlSchemaTypes = xmlSchema.getSchemaTypes();
            for (QName key : xmlSchemaTypes.keySet()) {
                XmlSchemaType xmlSchemaType = xmlSchemaTypes.get(key);
                if (xmlSchemaType instanceof XmlSchemaComplexType) {
                    XmlSchemaContentModel xmlSchemaContentModel = ((XmlSchemaComplexType) xmlSchemaType).getContentModel();
                    XmlSchemaComplexContent xmlSchemaComplexContent = (XmlSchemaComplexContent) xmlSchemaContentModel;
                    if (xmlSchemaContentModel instanceof XmlSchemaComplexContent) {
                        XmlSchemaContent xmlSchemaContent = xmlSchemaComplexContent.getContent();
                        if (xmlSchemaContent instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension xmlSchemaComplexContentExtension = (XmlSchemaComplexContentExtension) xmlSchemaContent;
                            if (xmlSchemaComplexContentExtension.getBaseTypeName().equals(abstractQName)) {
                                instanceTypeMappers.put(xmlSchemaType.getQName(),
                                        (ComplexTypeMapper) createTypeMapper(xmlSchemaType));
                            }
                        }
                    }
                }
            }
        }
        return instanceTypeMappers;
    }

    protected PropertyMapper createPropertyMapper(XmlSchemaType xmlSchemaType, Class<?> clazz, String propertyName)
            throws LocalizedException {
        if (propertyName == null || "".equals(propertyName)) {
            propertyName = xmlSchemaType.getName();
        }
        createTypeMapper(xmlSchemaType);
        return new SimpleContentPropertyMapper(clazz, xmlSchemaType.getQName(), propertyName, typeMappers);
    }

    protected PropertyMapper createPropertyMapper(XmlSchemaElement xmlSchemaElement, Class<?> clazz, String propertyName)
            throws LocalizedException {
        XmlSchemaType xmlSchemaType = xmlSchemaElement.getSchemaType();
        // bug 13001
        if (xmlSchemaType == null && xmlSchemaElement.getRef() != null && xmlSchemaElement.getRef().getTarget() != null) {
            xmlSchemaElement = schemaCollection.getElementByQName(xmlSchemaElement.getRef().getTargetQName());
            xmlSchemaType = xmlSchemaElement.getSchemaType();
        }

        // bug 13001, inner class
        if (xmlSchemaType.getName() == null) {
            if (xmlSchemaType instanceof XmlSchemaComplexType) {
                String innerClassName = clazz.getName().substring(clazz.getName().lastIndexOf(".") + 1);
                innerClassName += "$";
                // innerClassName += xmlSchemaElement.getName(); inner Class name
                // should be capital first
                innerClassName += xmlSchemaElement.getName().substring(0, 1).toUpperCase()
                        + xmlSchemaElement.getName().substring(1);
                xmlSchemaType.setName(innerClassName);

            } else {
                // The name is required if the simpleType element is a child of the
                // schema element, otherwise it is not allowed
                xmlSchemaType.setName(xmlSchemaElement.getName());
            }
        }

        createTypeMapper(xmlSchemaType);

        if (xmlSchemaElement.getMaxOccurs() > 1) {
            return new ListPropertyMapper(clazz, xmlSchemaType.getQName(), propertyName, typeMappers);
        } else {
            return new SimplePropertyMapper(clazz, xmlSchemaType.getQName(), propertyName, typeMappers);
        }
    }

    private Map<String, Object> getProperties(XmlSchemaComplexType xmlSchemaComplexType) throws LocalizedException {
        Map<String, Object> mappers = new ListOrderedMap();
        XmlSchemaContentModel xmlSchemaContentModel = xmlSchemaComplexType.getContentModel();
        if (xmlSchemaContentModel == null) {
            XmlSchemaParticle xmlSchemaParticle = xmlSchemaComplexType.getParticle();
            if (xmlSchemaParticle instanceof XmlSchemaGroupParticle) {
                XmlSchemaGroupParticle xmlSchemaGroupParticle = (XmlSchemaGroupParticle) xmlSchemaParticle;
                mappers.putAll(getProperties(xmlSchemaGroupParticle));
            } else if (xmlSchemaParticle instanceof XmlSchemaGroupRef) {
                XmlSchemaGroupRef xmlSchemaGroupRef = (XmlSchemaGroupRef) xmlSchemaParticle;
                XmlSchemaGroupParticle xmlSchemaGroupRefParticle = xmlSchemaGroupRef.getParticle();
                mappers.putAll(getProperties(xmlSchemaGroupRefParticle));
            } else {
                // if(!xmlSchemaComplexType.isAbstract())
                // throw new IllegalArgumentException("unknown Content of XmlSchemaComplexType:" +
                // xmlSchemaComplexType.getQName());
            }
        } else if (xmlSchemaContentModel instanceof XmlSchemaComplexContent) {
            XmlSchemaComplexContent xmlSchemaComplexContent = (XmlSchemaComplexContent) xmlSchemaContentModel;
            XmlSchemaContent xmlSchemaContent = xmlSchemaComplexContent.getContent();
            if (xmlSchemaContent instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension xmlSchemaComplexContentExtension = (XmlSchemaComplexContentExtension) xmlSchemaContent;
                XmlSchemaComplexType baseXmlSchemaComplexType = (XmlSchemaComplexType) schemaCollection
                        .getTypeByQName(xmlSchemaComplexContentExtension.getBaseTypeName());
                // First, recursion on parent class (for properties order)
                mappers.putAll(getProperties(baseXmlSchemaComplexType));

                XmlSchemaParticle xmlSchemaParticle = xmlSchemaComplexContentExtension.getParticle();
                if (xmlSchemaParticle instanceof XmlSchemaGroupParticle) {
                    mappers.putAll(getProperties((XmlSchemaGroupParticle) xmlSchemaParticle));
                }
            } else if (xmlSchemaContent instanceof XmlSchemaComplexContentRestriction) {
                // TODO
                throw new IllegalArgumentException("XmlSchemaComplexContentRestriction is not yet supported.");
            } else {
                throw new IllegalArgumentException("Invalid XmlSchemaContent for a XmlSchemaComplexContent.");
            }
        } else if (xmlSchemaContentModel instanceof XmlSchemaSimpleContent) {
            XmlSchemaSimpleContent xmlSchemaSimpleContent = (XmlSchemaSimpleContent) xmlSchemaContentModel;
            XmlSchemaContent xmlSchemaContent = xmlSchemaSimpleContent.getContent();
            if (xmlSchemaContent instanceof XmlSchemaSimpleContentExtension) {
                XmlSchemaSimpleContentExtension xmlSchemaSimpleContentExtension = (XmlSchemaSimpleContentExtension) xmlSchemaContent;
                XmlSchemaType baseXmlSchemaType = schemaCollection.getTypeByQName(xmlSchemaSimpleContentExtension
                        .getBaseTypeName());
                if (baseXmlSchemaType instanceof XmlSchemaSimpleType) {
                    mappers.put("value", baseXmlSchemaType);
                } else if (baseXmlSchemaType instanceof XmlSchemaComplexType) {
                    throw new IllegalArgumentException(
                            "A complex type with a simple content or a simple type is expected but found:"
                                    + baseXmlSchemaType.getQName());
                }
            } else if (xmlSchemaContent instanceof XmlSchemaSimpleContentRestriction) {
                XmlSchemaSimpleContentRestriction xmlSchemaSimpleContentRestriction = (XmlSchemaSimpleContentRestriction) xmlSchemaContent;
                // TODO
                throw new IllegalArgumentException("XmlSchemaSimpleContentRestriction is not yet supported.");
            } else {
                throw new IllegalArgumentException("Invalid XmlSchemaContent for a XmlSchemaComplexContent.");
            }
        } else {
            throw new IllegalArgumentException("unknown XmlSchemaContentModel.");
        }

        return mappers;
    }

    private Map<String, XmlSchemaElement> getProperties(XmlSchemaGroupParticle xmlSchemaGroupParticle) throws LocalizedException {
        Map<String, XmlSchemaElement> mappers = new ListOrderedMap();
        if (xmlSchemaGroupParticle instanceof XmlSchemaSequence) {
            XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) xmlSchemaGroupParticle;
            List<XmlSchemaSequenceMember> sequences = xmlSchemaSequence.getItems();
            for (XmlSchemaSequenceMember sequence : sequences) {
                mappers.putAll(getProperties(sequence));
            }
        } else if (xmlSchemaGroupParticle instanceof XmlSchemaAll) {
            XmlSchemaAll xmlSchemaAll = (XmlSchemaAll) xmlSchemaGroupParticle;
            List<XmlSchemaElement> elements = xmlSchemaAll.getItems();
            for (XmlSchemaElement element : elements) {
                mappers.put(element.getName(), element);
            }
        } else if (xmlSchemaGroupParticle instanceof XmlSchemaChoice) {
            XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice) xmlSchemaGroupParticle;
            List<XmlSchemaObject> xmlSchemaObjects = xmlSchemaChoice.getItems();
            for (XmlSchemaObject xmlSchemaObject : xmlSchemaObjects) {
                mappers.putAll(getProperties(xmlSchemaObject));
            }
        }
        return mappers;
    }

    private Map<String, XmlSchemaElement> getProperties(XmlSchemaObjectBase xmlSchemaObjectBase) throws LocalizedException {
        Map<String, XmlSchemaElement> mappers = new ListOrderedMap();

        if (xmlSchemaObjectBase instanceof XmlSchemaElement) {
            XmlSchemaElement xmlSchemaElement = (XmlSchemaElement) xmlSchemaObjectBase;
            if (xmlSchemaElement.getName() == null) {
                if (xmlSchemaElement.getRefBase() != null && xmlSchemaElement.getRefBase().getTargetQName() != null) {
                    mappers.put(xmlSchemaElement.getRefBase().getTargetQName().getLocalPart(), xmlSchemaElement);
                } else {
                    throw new IllegalArgumentException("An Element don't defined name.");
                }
            } else {
                mappers.put(xmlSchemaElement.getName(), xmlSchemaElement);
            }
        } else if (xmlSchemaObjectBase instanceof XmlSchemaAny) { // bug 14053
            mappers.put(AnyPropertyMapper.LABEL, null);
        } else if (xmlSchemaObjectBase instanceof XmlSchemaChoice) {
            mappers.putAll(getProperties((XmlSchemaChoice) xmlSchemaObjectBase));
        } else if (xmlSchemaObjectBase instanceof XmlSchemaGroupRef) {
            XmlSchemaGroupRef xmlSchemaGroupRef = (XmlSchemaGroupRef) xmlSchemaObjectBase;
            XmlSchemaGroupParticle xmlSchemaGroupRefParticle = xmlSchemaGroupRef.getParticle();
            mappers.putAll(getProperties(xmlSchemaGroupRefParticle));
        } else if (xmlSchemaObjectBase instanceof XmlSchemaSequence) {
            mappers.putAll(getProperties((XmlSchemaSequence) xmlSchemaObjectBase));
        } else {
            throw new IllegalArgumentException("Invalid xmlSchemaObject.");
        }

        return mappers;
    }

    private static String builtInTypeToJavaType(String type) {
        return BUILTIN_DATATYPES_MAP.get(type);
    }

    public static QName javaTypeToBuiltInType(String type) {
        return new QName(W3C_XML_SCHEMA_URI, BUILTIN_DATATYPES_MAP_REVERSE.get(type));
    }
}
