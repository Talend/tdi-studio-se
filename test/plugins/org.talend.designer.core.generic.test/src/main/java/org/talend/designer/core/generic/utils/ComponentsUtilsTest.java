// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.generic.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.component.ComponentDefinition;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.api.service.ComponentService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.StringProperty;
import org.talend.designer.core.generic.model.GenericElementParameter;
import org.talend.designer.core.model.FakeElement;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by hcyi on Feb 16, 2016 Detailled comment
 *
 */
public class ComponentsUtilsTest {

    @Before
    public void before() {
        System.setProperty("talend.test.component.filter", "true");
    }

    @Test
    public void testGetParametersFromForm() {
        ComponentProperties props = (ComponentProperties) new TestProperties("test").init(); //$NON-NLS-1$
        Form form = props.getForm(Form.MAIN);

        /*
         * Test wizard
         */
        Element element = new FakeElement(form.getName());

        // Test readonly case
        List<ElementParameter> parameters = ComponentsUtils.getParametersFromForm(element, false, null, props, form);
        for (ElementParameter parameter : parameters) {
            assertFalse(parameter.isReadOnly());
        }

        element.setReadOnly(true);
        parameters = ComponentsUtils.getParametersFromForm(element, false, null, props, form);
        for (ElementParameter parameter : parameters) {
            assertTrue(parameter.isReadOnly());
        }

        /*
         * Test component
         */
        // Test parameter initialization case (mainly to test ComponentsUtils.getParameterValue() method).
        checkParameterInitializationStatus(true);
        checkParameterInitializationStatus(false);
    }

    private void checkParameterInitializationStatus(boolean isInitializing) {
        INode node = createSFTestNode();
        ComponentProperties props = node.getComponentProperties();
        props.setValueEvaluator(null);
        Form form = props.getForm(Form.ADVANCED);
        List<ElementParameter> parameters = ComponentsUtils.getParametersFromForm(node, isInitializing, null, props, form);
        checkParameterInitializationStatus(parameters, isInitializing);
    }

    private INode createSFTestNode() {
        IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceConnection", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
        INode node = new Node(component, new Process(new FakePropertyImpl()));
        return node;
    }

    private void checkParameterInitializationStatus(List<ElementParameter> parameters, boolean isInitializing) {
        for (ElementParameter parameter : parameters) {
            if (parameter instanceof GenericElementParameter) {
                GenericElementParameter genericElementParameter = (GenericElementParameter) parameter;
                Property property = genericElementParameter.getProperty();
                if (property != null) {
                    if (GenericTypeUtils.isStringType(property)) {
                        String propertyValue = (String) property.getValue();
                        String parameterValue = (String) parameter.getValue();
                        // If value is empty (from default value or input) add double quotes.
                        if (StringUtils.isEmpty(propertyValue)) {
                            assertTrue("\"\"".equals(parameterValue)); //$NON-NLS-1$
                        } else { // Else value is not empty.
                            if (isInitializing) {
                                // If isInitializing is true value will be surrounded with quotes if not exist.
                                assertTrue(parameterValue.startsWith("\"") && parameterValue.endsWith("\"")); //$NON-NLS-1$ //$NON-NLS-2$
                            } else if (!propertyValue.startsWith("\"") || !propertyValue.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
                                // Else shouldn't add quotes to the value.
                                assertFalse(parameterValue.startsWith("\"") && parameterValue.endsWith("\"")); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testGetFormalPossibleValues() {
        ComponentService componentService = ComponentsUtils.getComponentService();
        ComponentProperties props = (ComponentProperties) new TestProperties("test").init(); //$NON-NLS-1$
        Form form = props.getForm(Form.MAIN);
        Element element = new FakeElement(form.getName());
        Widget testWidget = null;
        Property testProperty = null;
        Collection<Widget> widgets = form.getWidgets();
        Iterator<Widget> widgetsIterator = widgets.iterator();
        while (widgetsIterator.hasNext()) {
            Widget widget = widgetsIterator.next();
            NamedThing content = widget.getContent();
            if (content instanceof Property) {
                testWidget = widget;
                testProperty = (Property) content;
                break;
            }
        }
        assertNotNull(testWidget);
        assertNotNull(testProperty);

        // Test NamedThing type
        List<NamedThing> namedThings = new ArrayList<>();
        namedThings.add(new SimpleNamedThing("p1", "Test P1"));
        namedThings.add(new SimpleNamedThing("p2", "Test P2"));
        namedThings.add(new SimpleNamedThing("p3", "Test P3"));
        testProperty.setPossibleValues(namedThings);

        GenericElementParameter param = new GenericElementParameter(element, props, form, testWidget, componentService);
        param.setPossibleValues(testProperty.getPossibleValues());
        List<NamedThing> possibleValues = ComponentsUtils.getFormalPossibleValues(param);
        assertEquals(3, possibleValues.size());
        namedThings.retainAll(possibleValues);
        assertEquals(3, namedThings.size());

        // Test Integer type which is not support yet
        List<Integer> ints = new ArrayList<>();
        ints.add(1);
        ints.add(2);
        ints.add(3);
        testProperty.setPossibleValues(ints);

        param = new GenericElementParameter(element, props, form, testWidget, componentService);
        param.setPossibleValues(testProperty.getPossibleValues());
        possibleValues = ComponentsUtils.getFormalPossibleValues(param);
        assertEquals(0, possibleValues.size());

        // Test StringProperty
        testWidget = null;
        StringProperty testStringProperty = null;
        widgetsIterator = widgets.iterator();
        while (widgetsIterator.hasNext()) {
            Widget widget = widgetsIterator.next();
            NamedThing content = widget.getContent();
            if (content instanceof StringProperty) {
                testWidget = widget;
                testStringProperty = (StringProperty) content;
                break;
            }
        }
        assertNotNull(testWidget);
        assertNotNull(testStringProperty);

        testStringProperty.setPossibleNamedThingValues(namedThings);
        param = new GenericElementParameter(element, props, form, testWidget, componentService);
        param.setPossibleValues(testStringProperty.getPossibleValues());
        possibleValues = ComponentsUtils.getFormalPossibleValues(param);
        assertEquals(3, possibleValues.size());
        List<String> pvNames = new ArrayList<>();
        for (NamedThing pv : possibleValues) {
            pvNames.add(pv.getName());
        }
        for (NamedThing nt : namedThings) {
            assertTrue(pvNames.contains(nt.getName()));
        }
    }

    @Test
    public void testGetAllValuedProperties() {
        TestProperties props = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        List<Property> properties = ComponentsUtils.getAllValuedProperties(props);
        assertTrue(properties.contains(props.userId));
        assertTrue(properties.contains(props.schema.schema));
        assertTrue(properties.contains(props.nestedProps.userName));
        assertTrue(properties.contains(props.nestedProps.userPassword));
        assertTrue(properties.contains(props.contactProps.mobile));
        assertTrue(properties.contains(props.contactProps.email));
    }


    @Test
    public void testLoadComponents() {
        ComponentService componentService = ComponentsUtils.getComponentService();
        Set<ComponentDefinition> componentDefinitions = componentService.getAllComponents();
        Set<IComponent> coms = ComponentsFactoryProvider.getInstance().getComponents();
        List<IComponent> comList = new ArrayList<IComponent>();
        for (ComponentDefinition componentDefinition : componentDefinitions) {
            for(IComponent com : coms){
                if(com.getName().equals(componentDefinition.getName())){
                    comList.add(com);
                    break;
                }
            }
        }
        coms.removeAll(comList);
        ComponentsUtils.loadComponents(componentService);
        coms = ComponentsFactoryProvider.getInstance().getComponents();
        for (ComponentDefinition componentDefinition : componentDefinitions) {
            if(!componentDefinition.getName().equals("tJIRAInput")){
                continue;
            }
            for(IComponent com : coms){
                if(com.getName().equals(componentDefinition.getName())){
                    assertFalse("Component loaded: "+componentDefinition.getName(), true);
                }
            }
        }
    }

    @Test
    public void testGetParameterValue() {
        TestProperties props = (TestProperties) new TestProperties("test").init(); //$NON-NLS-1$
        Property<String> testProperty = props.userId;
        testProperty.setValue("user"); //$NON-NLS-1$
        String testParamName = testProperty.getName();
        EParameterFieldType testFieldType = EParameterFieldType.TEXT;
        Form form = props.getForm(Form.MAIN);
        Element fakeElement = new FakeElement(form.getName());
        INode node = createSFTestNode();

        // If it is a fake element, do not add quotes.
        Object parameterValue = ComponentsUtils.getParameterValue(fakeElement, testProperty, testFieldType, testParamName);
        assertEquals("user", parameterValue); //$NON-NLS-1$

        // If the property value is context mode, do not add quotes.
        testProperty.setValue("context.user"); //$NON-NLS-1$
        parameterValue = ComponentsUtils.getParameterValue(node, testProperty, testFieldType, testParamName);
        assertEquals("context.user", parameterValue); //$NON-NLS-1$

        // If old parameter is null(parameter name is testParamName), will initialize and add quotes.
        testProperty.setValue("user"); //$NON-NLS-1$
        parameterValue = ComponentsUtils.getParameterValue(node, testProperty, testFieldType, testParamName);
        assertEquals("\"user\"", parameterValue); //$NON-NLS-1$

        // If old parameter value is null, will initialize and add quotes.
        IElementParameter testParam = new ElementParameter(node);
        testParam.setName(testParamName);
        testParam.setDisplayName(testParamName);
        testParam.setFieldType(testFieldType);
        ((List<IElementParameter>) node.getElementParameters()).add(testParam);
        parameterValue = ComponentsUtils.getParameterValue(node, testProperty, testFieldType, testParamName);
        assertEquals("\"user\"", parameterValue); //$NON-NLS-1$

        // If old parameter value is not null but not equals to property value, will initialize and add quotes.
        node.setPropertyValue(testParamName, "user1");
        parameterValue = ComponentsUtils.getParameterValue(node, testProperty, testFieldType, testParamName);
        assertEquals("\"user\"", parameterValue); //$NON-NLS-1$

        // If old parameter value is not null and equals to property value, do not add quotes.
        testProperty.setValue("user"); //$NON-NLS-1$
        node.setPropertyValue(testParamName, "user");
        parameterValue = ComponentsUtils.getParameterValue(node, testProperty, testFieldType, testParamName);
        assertEquals("user", parameterValue); //$NON-NLS-1$

        // If old parameter value is not null but property value is NULL, return "".
        testProperty.setValue(null);
        parameterValue = ComponentsUtils.getParameterValue(node, testProperty, testFieldType, testParamName);
        assertEquals("\"\"", parameterValue); //$NON-NLS-1$

        List<String> cList = new ArrayList<String>();
        cList.add("mvn:org.talend.libraries/mysql-connector-java-5.1.30-bin/6.0.0");
        testProperty.setStoredValue(cList);
        parameterValue = ComponentsUtils.getParameterValue(node, testProperty, testFieldType, testParamName);
        assertEquals(null, parameterValue); //$NON-NLS-1$


        cList = new ArrayList<String>();
        cList.add("context.jdbc5_drivers");
        testProperty.setStoredValue(cList);
        parameterValue = ComponentsUtils.getParameterValue(node, testProperty, testFieldType, testParamName);
        assertEquals("context.jdbc5_drivers", parameterValue); //$NON-NLS-1$
    }

    @Test
    public void testGetNameThingFromComponentPropertiesByName() {
        ComponentProperties componentProperties = ComponentsUtils.getComponentProperties("tJDBCInput");
        assertNotNull("tJDBCInput not load", componentProperties);
        NamedThing drivers = ComponentsUtils.getNameThingFromComponentPropertiesByName(componentProperties, "drivers");
        assertNotNull(drivers);
        NamedThing userId = ComponentsUtils.getNameThingFromComponentPropertiesByName(componentProperties, "userId");
        assertNotNull(userId);
        NamedThing jdbcUrl = ComponentsUtils.getNameThingFromComponentPropertiesByName(componentProperties, "jdbcUrl");
        assertNotNull(jdbcUrl);
        NamedThing refComp = ComponentsUtils.getNameThingFromComponentPropertiesByName(componentProperties,
                "referencedComponent");
        assertNotNull(refComp);
        // not exist one
        NamedThing test = ComponentsUtils.getNameThingFromComponentPropertiesByName(componentProperties, "testNoExist");
        assertNull(test);
    }

    @After
    public void after() {
        System.setProperty("talend.test.component.filter", "false");
    }
}
