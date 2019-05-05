package org.talend.designer.core.ui.editor.properties.controllers;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.param.EConnectionParameterName;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.sqlbuilder.util.ConnectionParameters;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
public class AbstractElementPropertySectionControllerTest {

    private Process process;

    @Before
    public void setUp() throws Exception {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        process = new Process(property);
    }

    @Test
    public void testsetAllConnectionParameters() throws Exception {
        QueryTypeController instance = Whitebox.newInstance(QueryTypeController.class);
        ConnectionParameters oldConnParameters = new ConnectionParameters();
        instance.connParameters = oldConnParameters;
        IComponent elemComponent = ComponentsFactoryProvider.getInstance().get("tMysqlInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node elem = new Node(elemComponent, process);
        IComponent component = ComponentsFactoryProvider.getInstance().get("tJDBCInput",
                ComponentCategory.CATEGORY_4_DI.getName());
        DataNode element = new DataNode(component, "tOracleConnection_1");
        List<IElementParameter> listParams = new ArrayList<IElementParameter>();
        ElementParameter userParam = new ElementParameter(element);
        userParam.setFieldType(EParameterFieldType.TEXT);
        userParam.setValue("username");
        userParam.setRepositoryValue(EConnectionParameterName.GENERIC_USERNAME.getDisplayName());
        userParam.setName(EConnectionParameterName.GENERIC_USERNAME.getDisplayName());
        listParams.add(userParam);
        element.setElementParameters(listParams);
        instance.setElem(elem);
        Whitebox.invokeMethod(instance, "setAllConnectionParameters",
                new Object[] { EDatabaseTypeName.ORACLE_OCI.getDisplayName(), element });
        assertTrue(StringUtils.equals(instance.connParameters.getUserName(), "username"));
    }
}
