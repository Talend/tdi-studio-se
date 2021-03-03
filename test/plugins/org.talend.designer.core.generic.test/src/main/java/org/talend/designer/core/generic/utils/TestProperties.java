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
package org.talend.designer.core.generic.utils;

import static org.talend.daikon.properties.presentation.Widget.*;
import static org.talend.daikon.properties.property.PropertyFactory.*;

import java.util.Collections;
import java.util.Set;

import org.talend.components.api.component.Connector;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.api.properties.ComponentReferenceProperties;
import org.talend.components.common.FixedConnectorsComponentProperties;
import org.talend.daikon.properties.ReferenceProperties;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

/**
 *
 * created by ycbai on 2016年3月15日 Detailled comment
 *
 */
public class TestProperties extends FixedConnectorsComponentProperties {

    public Property<String> userId = newProperty("userId").setRequired(); //$NON-NLS-1$

    public SchemaProperties schema = new SchemaProperties("schema"); //$NON-NLS-1$

    public TestNestedProperties nestedProps = new TestNestedProperties("nestedProps"); //$NON-NLS-1$

    public TestContactProperties contactProps = new TestContactProperties("contactProps"); //$NON-NLS-1$

    public ComponentReferenceProperties<TestReferencedProperties> referencePros = new ComponentReferenceProperties<>(
            "referencePros", //$NON-NLS-1$
            TestReferencedProperties.TEST_DEFINTION_NAME);

    public ReferenceProperties<TestReferencedProperties> simpleReferencePros = new ReferenceProperties<>(
            "simpleReferencePros", //$NON-NLS-1$
            TestReferencedProperties.TEST_DEFINTION_NAME);
    protected transient PropertyPathConnector MAIN_CONNECTOR = new PropertyPathConnector(Connector.MAIN_NAME, "schema"); //$NON-NLS-1$

    public TestProperties(String name) {
        super(name);
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = Form.create(this, Form.MAIN);
        mainForm.addRow(userId);
        mainForm.addRow(nestedProps.getForm(Form.MAIN));
        mainForm.addRow(contactProps.getForm(Form.MAIN));

        Form refForm = Form.create(this, Form.REFERENCE);
        Widget compListWidget = widget(referencePros).setWidgetType(Widget.COMPONENT_REFERENCE_WIDGET_TYPE);
        refForm.addRow(compListWidget);
        refForm.addRow(mainForm);
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection) {
            return Collections.singleton(MAIN_CONNECTOR);
        } else {
            return Collections.EMPTY_SET;
        }
    }

}
