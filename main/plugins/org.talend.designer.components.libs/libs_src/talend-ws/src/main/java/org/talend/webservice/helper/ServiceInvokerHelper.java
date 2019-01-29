/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.talend.webservice.helper;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.talend.webservice.exception.LocalizedException;
import org.talend.webservice.helper.conf.ServiceHelperConfiguration;
import org.talend.webservice.helper.map.MapConverter;
import org.talend.webservice.jaxb.JAXBUtils;
import org.talend.webservice.jaxb.JAXBUtils.IdentifierType;
import org.talend.webservice.mapper.AnyPropertyMapper;
import org.talend.webservice.mapper.ClassMapper;
import org.talend.webservice.mapper.EmptyMessageMapper;
import org.talend.webservice.mapper.MapperFactory;
import org.talend.webservice.mapper.MessageMapper;

/**
 * 
 * @author rlamarche
 */
public class ServiceInvokerHelper implements ClassMapper {

    private ServiceDiscoveryHelper serviceDiscoveryHelper;

    private DynamicClientFactory dynamicClientFactory;

    private final String packagePrefix;

    private Map<String, String> namespacePackageMap;

    private Map<String, String> packageNamespaceMap;

    private Map<QName, Map<QName, Client>> clients;

    private List<String> bindingFiles;

    private Map<Message, MessageMapper> mappers;

    private MapperFactory mapperFactory;

    private ServiceHelperConfiguration configuration;

    protected ServiceInvokerHelper() {
        packagePrefix = "tmp" + (String.valueOf((new Random(Calendar.getInstance().getTimeInMillis())).nextInt()).substring(1));
        dynamicClientFactory = DynamicClientFactory.newInstance();
        namespacePackageMap = new HashMap<String, String>();
        packageNamespaceMap = new HashMap<String, String>();
        clients = new HashMap<QName, Map<QName, Client>>();
        mappers = new HashMap<Message, MessageMapper>();
    }

    public ServiceInvokerHelper(String wsdlUri) throws WSDLException, IOException, TransformerException, URISyntaxException {
        this(new ServiceDiscoveryHelper(wsdlUri));
    }

    public ServiceInvokerHelper(String wsdlUri, String tempPath) throws WSDLException, IOException, TransformerException,
            URISyntaxException {
        this(new ServiceDiscoveryHelper(wsdlUri, tempPath));
    }

    public ServiceInvokerHelper(String wsdlUri, ServiceHelperConfiguration configuration) throws WSDLException, IOException,
            TransformerException, URISyntaxException {
        this(new ServiceDiscoveryHelper(wsdlUri, configuration));
    }

    public ServiceInvokerHelper(String wsdlUri, ServiceHelperConfiguration configuration, String tempPath) throws WSDLException,
            IOException, TransformerException, URISyntaxException {
        this(new ServiceDiscoveryHelper(wsdlUri, configuration, tempPath), configuration);
    }

    public ServiceInvokerHelper(ServiceDiscoveryHelper serviceDiscoveryHelper, ServiceHelperConfiguration configuration) {
        this(serviceDiscoveryHelper);
        this.configuration = configuration;
    }

    public ServiceInvokerHelper(ServiceDiscoveryHelper serviceDiscoveryHelper) {
        this();
        this.serviceDiscoveryHelper = serviceDiscoveryHelper;

        Set<String> namespaces = serviceDiscoveryHelper.getNamespaces();

        bindingFiles = new ArrayList<String>(namespaces.size());
        for (String ns : namespaces) {
            String packageName = packagePrefix + JAXBUtils.namespaceURIToPackage(ns);
            namespacePackageMap.put(ns, packageName);
            packageNamespaceMap.put(packageName, ns);

            File f = org.apache.cxf.tools.util.JAXBUtils.getPackageMappingSchemaBindingFile(ns, packageName);
            f.deleteOnExit();
            bindingFiles.add(f.getAbsolutePath());
        }

        mapperFactory = new MapperFactory(this, serviceDiscoveryHelper.getSchema());
    }

    public Client getClient(QName service, QName port) {
        Map<QName, Client> serviceClients = clients.get(service);
        if (serviceClients == null) {
            serviceClients = new HashMap<QName, Client>();
            clients.put(service, serviceClients);
        }

        if (serviceClients.get(port) == null) {
            serviceClients.put(port, createClient(service, port));
        }

        return serviceClients.get(port);
    }

    protected Client createClient(QName service, QName port) {
        // bug 8674

        Client client = dynamicClientFactory.createClient(serviceDiscoveryHelper.getLocalWsdlUri(), service, Thread
                .currentThread().getContextClassLoader(), port, bindingFiles);
        // end
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        if (configuration != null) {
            configuration.configureHttpConduit(conduit);
        }
        return client;
    }

    private MessageMapper getMessageMapper(Message message) throws LocalizedException {

        MessageMapper messageMapper = mappers.get(message);
        if (messageMapper == null) {
            messageMapper = createMessageMapper(message);
            mappers.put(message, messageMapper);
        }

        return messageMapper;
    }

    private MessageMapper createMessageMapper(Message message) throws LocalizedException {
        return mapperFactory.createMessageMapper(message);
    }

    protected Map<String, Object> invoke(Client client, Operation operation, QName operationQName, Object value)
            throws Exception, LocalizedException {

        Input input = operation.getInput();
        Output output = operation.getOutput();
        MessageMapper inMessageMapper = null;
        MessageMapper outMessageMapper = null;

        BindingOperationInfo bindingOperationInfo = client.getEndpoint().getEndpointInfo().getBinding()
                .getOperation(operationQName);
        if (input != null) {
            inMessageMapper = getMessageMapper(input.getMessage());
        } else {
            inMessageMapper = new EmptyMessageMapper();
        }
        if (output != null) {
            outMessageMapper = getMessageMapper(output.getMessage());
        } else {
            outMessageMapper = new EmptyMessageMapper();
        }
        if (bindingOperationInfo.isUnwrappedCapable()) {
            inMessageMapper.setUnwrapped(true);
            outMessageMapper.setUnwrapped(true);
        }

        Object[] retParams;
        if (value != null) {
            Object[] params = inMessageMapper.convertToParams(value);
            retParams = client.invoke(operationQName, params);
        } else {
            retParams = client.invoke(operationQName);
        }

        Map<String, Object> retValues = outMessageMapper.convertToValue(retParams);

        return retValues;
    }

    public Map<String, Object> invoke(QName serviceName, QName portName, String operationName, Object params) throws Exception,
            LocalizedException {
        if (serviceName == null) {
            throw new IllegalArgumentException("serviceName is mandatory.");
        }
        Service service = serviceDiscoveryHelper.getDefinition().getService(serviceName);
        if (service == null) {
            throw new IllegalArgumentException("Service " + serviceName.toString() + " does not exists.");
        }

        if (portName == null) {
            throw new IllegalArgumentException("portName is mandatory.");
        }
        Port port = service.getPort(portName.getLocalPart());
        if (port == null) {
            throw new IllegalArgumentException("Port " + portName + " does not exists for service " + serviceName.toString()
                    + ".");
        }
        if (operationName == null) {
            throw new IllegalArgumentException("operationName is mandatory.");
        }
        Operation operation = port.getBinding().getPortType().getOperation(operationName, null, null);
        if (operation == null) {
            throw new IllegalArgumentException("Operation " + operationName + " does not exists for service "
                    + serviceName.toString() + ".");
        }

        QName operationQName = new QName(port.getBinding().getPortType().getQName().getNamespaceURI(), operation.getName());

        Client client = getClient(serviceName, portName);

        return invoke(client, operation, operationQName, params);
    }

    /**
     * Invoke a service with a simple map of parametes (address.city=LYON, address.zipCode=69003, etc ...) Returned
     * results are also in this format
     * 
     * @param serviceName
     * @param portName
     * @param operationName
     * @param params
     * @return
     * @throws java.lang.Exception
     * @throws org.talend.webservice.exception.LocalizedException
     */
    public Map<String, Object> invokeSimple(QName serviceName, QName portName, String operationName, Object params)
            throws Exception, LocalizedException {
        if (params instanceof Map) {
            params = MapConverter.mapToDeepMap((Map<String, Object>) params);
        }

        Map<String, Object> result = invoke(serviceName, portName, operationName, params);

        return MapConverter.deepMapToMap(result);
    }

    protected String getClassNameForType(QName xmlSchemaTypeMapperQname) {
        StringBuilder sb = new StringBuilder();
        sb.append(getPackageForNamespaceURI(xmlSchemaTypeMapperQname.getNamespaceURI()));
        sb.append(".");
        sb.append(getClassNameForTypeName(xmlSchemaTypeMapperQname.getLocalPart()));
        String className = sb.toString();

        return className;
    }

    protected String getPackageForNamespaceURI(String ns) {
        return namespacePackageMap.get(ns);
    }

    protected String getNamespaceURIForPackage(String packageName) {
        return packageNamespaceMap.get(packageName);
    }

    protected String getClassNameForTypeName(String typeName) {
        return toCamelCase(JAXBUtils.nameToIdentifier(typeName, IdentifierType.CLASS), true);
    }

    public Class<?> getClassForType(QName xmlSchemaTypeMapperQname) {
        String className = getClassNameForType(xmlSchemaTypeMapperQname);
        try {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            return clazz;

        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Class<?> getClassForType(QName xmlSchemaTypeMapperQName, List<String> propertiesName, int tempSuffix) {
        Class<?> clazz = getClassForType(xmlSchemaTypeMapperQName);
        boolean allCorrect = false;
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(clazz);

        if (propertiesName.size() == 0) {
            allCorrect = true;
        }
        for (String propertyName : propertiesName) {
            for (PropertyDescriptor descriptor : descriptors) {
                if (propertyName.equalsIgnoreCase(descriptor.getName()) || (AnyPropertyMapper.LABEL.equalsIgnoreCase(propertyName) && ("any".equalsIgnoreCase(descriptor.getName()) || "content".equalsIgnoreCase(descriptor.getName())))) {
                    allCorrect = true;
                    break;
                } else {
                    allCorrect = false;
                }
            }
        }
        if (!allCorrect) {
            return getClassForType(new QName(xmlSchemaTypeMapperQName.getNamespaceURI(), xmlSchemaTypeMapperQName.getLocalPart()
                    + tempSuffix), propertiesName, tempSuffix++);
        } else {
            return clazz;
        }
    }

    public XmlSchemaType getTypeForClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(XmlType.class)) {
            XmlType type = clazz.getAnnotation(XmlType.class);
            XmlSchema schema = clazz.getPackage().getAnnotation(XmlSchema.class);
            QName qname = new QName(schema.namespace(), type.name());

            return serviceDiscoveryHelper.getSchema().getTypeByQName(qname);
        } else {
            QName type = MapperFactory.javaTypeToBuiltInType(clazz.getName());
            if (type != null) {
                return serviceDiscoveryHelper.getSchema().getTypeByQName(type);
            } else {
                throw new IllegalArgumentException("Unmapped class : " + clazz.getName());
            }
        }
    }

    public ServiceDiscoveryHelper getServiceDiscoveryHelper() {
        return serviceDiscoveryHelper;
    }

    private String toCamelCase(String value, boolean startWithLowerCase) {
        String[] strings = StringUtils.split(value, "_");
        for (int i = startWithLowerCase ? 1 : 0; i < strings.length; i++) {
            strings[i] = StringUtils.capitalize(strings[i]);
        }
        return StringUtils.join(strings, "_");
    }
}
