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
package org.talend.designer.core.generic.context;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.INode;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by ycbai on 2016年5月30日 Detailled comment
 *
 */
public class ComponentContextPropertyValueEvaluatorTest {

    private static final String PROP_USER_ID = "connection.userPassword.userId"; //$NON-NLS-1$

    private INode node;

    private ComponentProperties props;

    private ComponentContextPropertyValueEvaluator evaluator;

    @Before
    public void before() {
        IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI"); //$NON-NLS-1$ //$NON-NLS-2$
        node = new Node(component, new Process(new FakePropertyImpl()));
        props = node.getComponentProperties();
        evaluator = new ComponentContextPropertyValueEvaluator(node);
    }

    @Test
    public void testQuotesValue() {
        checkStringQuotes(PROP_USER_ID, false);
        checkStringQuotes(PROP_USER_ID, true);
    }

    private void checkStringQuotes(String propName, boolean withQuotes) {
        String testValue = "testValue"; //$NON-NLS-1$
        if (withQuotes) {
            testValue = "\"" + testValue + "\""; //$NON-NLS-1$//$NON-NLS-2$
        }
        props.setValue(propName, testValue);
        props.setValueEvaluator(evaluator);
        assertEquals("testValue", props.getValuedProperty(propName).getValue()); //$NON-NLS-1$
    }

}
