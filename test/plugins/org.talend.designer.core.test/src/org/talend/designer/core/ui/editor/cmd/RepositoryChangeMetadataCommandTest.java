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

import org.apache.avro.Schema;
import org.junit.Before;
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
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by ycbai on 2016年4月19日 Detailled comment
 *
 */
public class RepositoryChangeMetadataCommandTest extends AbstractMetadataCommandTest {

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


        String fullParamName = SCHEMA_PARAM_NAME + ":REPOSITORY_SCHEMA_TYPE"; //$NON-NLS-1$
        String propValue = "testconnid" + " - " + newTable.getTableName(); //$NON-NLS-1$ //$NON-NLS-2$
        RepositoryChangeMetadataCommand changeMetadataCommand = new RepositoryChangeMetadataCommand(node,
                fullParamName, propValue, newTable, null, null);
        changeMetadataCommand.execute();

        newTable = node.getMetadataList().get(0);
        String avroSchemaStr = ((SchemaProperties) node.getComponentProperties().getProperties("module.main")).schema
                .getStringValue();

        assertNotNull(avroSchemaStr);
        Schema avroSchema = new Schema.Parser().parse(avroSchemaStr);
        assertEquals(2, avroSchema.getFields().size());
        assertNull(avroSchema.getField("C1")); //$NON-NLS-1$
        assertEquals(avroSchemaStr, schemaParam.getValue().toString());
    }

}
