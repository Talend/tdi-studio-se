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
package org.talend.sdk.component.studio.model.parameter;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;

/**
 * created by hcyi on Aug 27, 2019
 * Detailled comment
 *
 */
public class SettingVisitorTest {

    @Test
    public void testVisit4NotLeafNode() throws Exception {
        final Node nodeMock = mockNode(metadata());
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", "FLOW", null, true, //$NON-NLS-1$ //$NON-NLS-2$
                Collections.emptyList());

        SettingVisitor visitor = new SettingVisitor(nodeMock, parameter, new ArrayList());

        PropertyNode propertyNode = getPropertyNode(PropertyTypes.STRING, EParameterFieldType.TEXT);
        PropertyNode childPropertyNode = getPropertyNode(PropertyTypes.STRING, EParameterFieldType.TEXT);
        propertyNode.addChild(childPropertyNode);

        visitor.visit(propertyNode);
        
        Assertions.assertEquals(0, visitor.getSettings().size());
    }

    @Test
    public void testVisit4LeafNodeObject() throws Exception {
        final Node nodeMock = mockNode(metadata());
        final OutputSchemaParameter parameter = new OutputSchemaParameter(nodeMock, "schema", "FLOW", null, true, //$NON-NLS-1$ //$NON-NLS-2$
                Collections.emptyList());
        
        SettingVisitor visitor = new SettingVisitor(nodeMock, parameter, new ArrayList());
        
        PropertyNode propertyNode = getPropertyNode(PropertyTypes.OBJECT, EParameterFieldType.TEXT);
        
        visitor.visit(propertyNode);
        
        Assertions.assertEquals(0, visitor.getSettings().size());
    }

    private PropertyNode getPropertyNode(String propertyType, EParameterFieldType fieldType) {
        SimplePropertyDefinition definition = new SimplePropertyDefinition();
        definition.setName("propertyName1");
        definition.setType(propertyType);
        definition.setMetadata(new HashMap());
        PropertyNode propertyNode = new PropertyNode(new PropertyDefinitionDecorator(definition), fieldType, false);
        return propertyNode;
    }

    private Node mockNode(final IMetadataTable metadata) {
        final Node nodeMock = mock(Node.class);
        when(nodeMock.getMetadataFromConnector("FLOW")).thenReturn(metadata);
        return nodeMock;
    }

    private IMetadataTable metadata() {
        final IMetadataTable metadata = new MetadataTable();
        final List<IMetadataColumn> columns = new ArrayList<>();

        final IMetadataColumn c1 = new MetadataColumn();
        c1.setLabel("c1");
        columns.add(c1);

        final IMetadataColumn c2 = new MetadataColumn();
        c2.setLabel("c2");
        columns.add(c2);
        metadata.setListColumns(columns);
        return metadata;
    }
}
