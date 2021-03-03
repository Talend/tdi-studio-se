// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.cmd;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IElementParameterDefaultValue;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.ElementParameterDefaultValue;
import org.talend.designer.core.test.util.NodeTestCreator;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * created by ycbai on 2017年6月29日 Detailled comment
 *
 */
public class PropertyChangeCommandTest extends AbstractMetadataCommandTest {

    private IProcess2 process;

    @Before
    public void before() {
        process = createFakeProcess();
    }

    @Test
    public void testUpdateHiddenParameterDefaultValues() {
        Node node = NodeTestCreator.createSimpleInputNode(process);

        ElementParameter param = new ElementParameter(node);
        param.setName("AUTH_TYPE"); //$NON-NLS-1$
        param.setValue("ON_PREMISE"); //$NON-NLS-1$
        param.setDisplayName("AUTH_TYPE"); //$NON-NLS-1$
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.BASIC);
        node.addElementParameter(param);

        param = new ElementParameter(node);
        param.setName("API_VERSION"); //$NON-NLS-1$
        param.setValue("API_2011"); //$NON-NLS-1$
        param.setDisplayName("API_VERSION"); //$NON-NLS-1$
        param.setFieldType(EParameterFieldType.CLOSED_LIST);
        param.setCategory(EComponentCategory.BASIC);
        node.addElementParameter(param);

        param = new ElementParameter(node);
        param.setName("MAPPING"); //$NON-NLS-1$
        param.setValue("mscrm_id"); //$NON-NLS-1$
        param.setDisplayName("MAPPING"); //$NON-NLS-1$
        param.setFieldType(EParameterFieldType.MAPPING_TYPE);
        param.setCategory(EComponentCategory.ADVANCED);
        param.setShow(false);
        node.addElementParameter(param);
        List<IElementParameterDefaultValue> defaultValues = new ArrayList<>();
        IElementParameterDefaultValue defaultTypeVal = new ElementParameterDefaultValue();
        defaultTypeVal.setIfCondition("(AUTH_TYPE!='ONLINE') OR (API_VERSION !='API_2016_ODATA')"); //$NON-NLS-1$
        defaultTypeVal.setDefaultValue("mscrm_id"); //$NON-NLS-1$
        defaultValues.add(defaultTypeVal);
        defaultTypeVal = new ElementParameterDefaultValue();
        defaultTypeVal.setIfCondition("(AUTH_TYPE=='ONLINE') AND (API_VERSION=='API_2016_ODATA')"); //$NON-NLS-1$
        defaultTypeVal.setDefaultValue("mscrm_odata_id"); //$NON-NLS-1$
        defaultValues.add(defaultTypeVal);
        param.setDefaultValues(defaultValues);

        PropertyChangeCommand command = new PropertyChangeCommand(node, "AUTH_TYPE", "ONLINE"); //$NON-NLS-1$ //$NON-NLS-2$
        command.execute();
        IElementParameter mappingParameter = node.getElementParameter("MAPPING"); //$NON-NLS-1$
        assertEquals("mscrm_id", mappingParameter.getValue()); //$NON-NLS-1$

        command = new PropertyChangeCommand(node, "API_VERSION", "API_2016_ODATA"); //$NON-NLS-1$ //$NON-NLS-2$
        command.execute();
        assertEquals("mscrm_odata_id", mappingParameter.getValue()); //$NON-NLS-1$
    }

}
