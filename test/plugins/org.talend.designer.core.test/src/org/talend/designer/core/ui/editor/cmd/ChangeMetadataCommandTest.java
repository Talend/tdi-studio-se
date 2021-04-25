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

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.components.common.SchemaProperties;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.test.util.NodeTestCreator;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.TestProperties;

/**
 * created by ycbai on 2016年4月18日 Detailled comment
 *
 */
public class ChangeMetadataCommandTest extends AbstractMetadataCommandTest {

    private IProcess2 process;

    @Before
    public void before() {
        process = createFakeProcess();
    }

    @Test
    public void testComponentSchemaUpdated() {
        IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceInput", "DI");
        Node node = new Node(component, new Process(new FakePropertyImpl()));
        IMetadataTable table = node.getMetadataList().get(0);
        IMetadataColumn column = new MetadataColumn();
        column.setLabel("C1");
        column.setTalendType(JavaTypesManager.STRING.getId());
        table.getListColumns().add(column);
        column = new MetadataColumn();
        column.setLabel("C2");
        column.setTalendType(JavaTypesManager.STRING.getId());
        table.getListColumns().add(column);
        column = new MetadataColumn();
        column.setLabel("C3");
        column.setTalendType(JavaTypesManager.STRING.getId());
        table.getListColumns().add(column);

        IMetadataTable newTable = table.clone(true);
        newTable.getListColumns().remove(0);
        IElementParameter schemaParam = null;
        for (IElementParameter param : node.getElementParameters()) {
            if (EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType())) {
                schemaParam = param;
                break;
            }
        }
        assertNotNull(schemaParam);

        ChangeMetadataCommand changeMetadataCommand = new ChangeMetadataCommand(node, schemaParam, null, null, null, table,
                newTable);
        changeMetadataCommand.execute();

        String avroSchemaStr = ((SchemaProperties) node.getComponentProperties().getProperties("module.main")).schema
                .getStringValue();
        assertNotNull(avroSchemaStr);
        Schema avroSchema = new Schema.Parser().parse(avroSchemaStr);
        assertEquals(2, avroSchema.getFields().size());
        assertNull(avroSchema.getField("C1")); //$NON-NLS-1$
        assertEquals(avroSchemaStr, schemaParam.getValue().toString());
    }

    /**
     * Test disabled since for now the propagation is done by the component itself.<br>
     * A new test need to be created with a custom component setting
     */
    @Test
    @Ignore
    public void testComponentSchemaPropagated() {
        Node simpleInputNode = NodeTestCreator.createSimpleInputNode(process);
        TestProperties inputProps = (TestProperties) new TestProperties("testInput").init(); //$NON-NLS-1$
        simpleInputNode.setComponentProperties(inputProps);
        simpleInputNode.getMetadataList().clear();

        IMetadataTable table = createSimpleMetadata(inputProps.schema);
        table.setTableName(simpleInputNode.getUniqueName());
        table.setLabel(simpleInputNode.getUniqueName());
        table.setAttachedConnector("FLOW"); //$NON-NLS-1$
        simpleInputNode.getMetadataList().add(table);

        List<Object> args = new ArrayList<>();
        args.add(simpleInputNode.getUniqueName());
        args.add("connectionName"); //$NON-NLS-1$
        args.add(null); // set null, the command should take the schema from the component directly
        ConnectionCreateCommand ccc = new ConnectionCreateCommand(simpleInputNode, "FLOW", args); //$NON-NLS-1$
        ConnectionCreateCommand.setCreatingConnection(true);

        IComponent component = ComponentsFactoryProvider.getInstance().get("tSalesforceOutput", "DI");
        Node node = new Node(component, new Process(new FakePropertyImpl()));
        ccc.setTarget(node);
        ccc.execute();

        IElementParameter schemaParam = null;
        for (IElementParameter param : node.getElementParameters()) {
            if (EParameterFieldType.SCHEMA_REFERENCE.equals(param.getFieldType()) && param.getContext().equals("MAIN")) {
                schemaParam = param;
                break;
            }
        }
        assertNotNull(schemaParam);


        table = node.getMetadataList().get(0);
        String avroSchemaStr = inputProps.schema.schema.getStringValue();
        assertNotNull(avroSchemaStr);
        Schema avroSchema = new Schema.Parser().parse(avroSchemaStr);

        assertEquals(3, avroSchema.getFields().size());
        assertNotNull(avroSchema.getField("C1")); //$NON-NLS-1$
        assertNotNull(avroSchema.getField("C2")); //$NON-NLS-1$
        assertNotNull(avroSchema.getField("C3")); //$NON-NLS-1$
        assertEquals(avroSchema.getFields().toString(), ((Schema)schemaParam.getValue()).getFields().toString());
    }

}
