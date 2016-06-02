package org.talend.designer.webservice.test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.junit.Assert;
import org.junit.Test;
import org.talend.designer.webservice.ws.wsdlinfo.ParameterInfo;
import org.talend.designer.webservice.ws.wsdlutil.ComponentBuilder;

public class ComponentBuilderTest {

    @Test
    public void testBuildParameterFromTypes() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        ParameterInfo parameter = new ParameterInfo();
        String name = "parameter";
        String nameSpace = "http://parameter";
        parameter.setName(name);
        parameter.setNameSpace(nameSpace);
        ComponentBuilder cb = new ComponentBuilder();
        Field field = cb.getClass().getDeclaredField("allXmlSchemaType");
        field.setAccessible(true);
        List<XmlSchemaType> allXmlSchemaType = (List<XmlSchemaType>) field.get(cb);
        XmlSchema first = new XmlSchema();
        first.setId("first");
        XmlSchemaSimpleType xsst = new XmlSchemaSimpleType(first);
        xsst.setId("first");
        xsst.setName("first");
        xsst.setSourceURI("http://first");
        XmlSchema two = new XmlSchema();
        two.setId("two");
        two.setTargetNamespace(nameSpace);
        String nameSpaceTwo = "http://parameter";
        XmlSchemaComplexType xsc = new XmlSchemaComplexType(two);
        XmlSchemaSequence xmlSchemaSequence = new XmlSchemaSequence();
        xmlSchemaSequence.setId("sequence");
        XmlSchemaElement xmlSchemaElement = new XmlSchemaElement();
        xmlSchemaElement.setSourceURI(nameSpaceTwo);
        xmlSchemaElement.setQName(new QName(nameSpaceTwo, "parameter"));
        xmlSchemaElement.setName(name);
        xmlSchemaSequence.getItems().add(xmlSchemaElement);
        xsc.setParticle(xmlSchemaSequence);
        xsc.setId("two");
        xsc.setName("parameter");
        XmlSchemaComplexContent contentModel = new XmlSchemaComplexContent();
        XmlSchemaComplexContentExtension xmlSchemaContent = new XmlSchemaComplexContentExtension();
        xmlSchemaContent.setId("two");
        xmlSchemaContent.setBaseTypeName(new QName(nameSpace, "two"));
        xmlSchemaContent.setSourceURI(nameSpaceTwo);
        contentModel.setSourceURI(nameSpace);
        contentModel.setContent(xmlSchemaContent);
        xsc.setContentModel(contentModel);
        allXmlSchemaType.add(xsst);
        allXmlSchemaType.add(xsc);
        Method m = cb.getClass().getDeclaredMethod("buileParameterFromTypes",
                new Class[] { String.class, String.class, ParameterInfo.class, int.class });
        m.setAccessible(true);
        m.invoke(cb, nameSpace, name, parameter, 5);
        ParameterInfo parameterInfo = parameter.getParameterInfos().get(0);
        Assert.assertNotNull(parameterInfo);
        Assert.assertEquals(nameSpace, parameterInfo.getNameSpace());

    }
}
