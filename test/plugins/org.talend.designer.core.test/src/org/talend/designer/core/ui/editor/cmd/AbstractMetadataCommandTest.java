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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.runtime.model.components.IComponentConstants;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.test.util.IMetadataTableConstants;
import org.talend.designer.core.test.util.MetadataUtils;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.utils.SchemaProperties;

/**
 * created by ycbai on 2016年4月19日 Detailled comment
 *
 */
public class AbstractMetadataCommandTest {

    protected static final String SCHEMA_PARAM_NAME = "schema.schema"; //$NON-NLS-1$

    protected IProcess2 createFakeProcess() {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId("junitId"); //$NON-NLS-1$
        property.setVersion("0.1"); //$NON-NLS-1$
        property.setLabel("test");//$NON-NLS-1$

        return new Process(property);
    }

    protected IMetadataTable createSimpleMetadata(SchemaProperties schemaProperties) {
        List<Map<String, Object>> columnsMap = new ArrayList<>();
        columnsMap.add(createSimpleColumn("C1", "id_String")); //$NON-NLS-1$ //$NON-NLS-2$
        columnsMap.add(createSimpleColumn("C2", "id_String")); //$NON-NLS-1$ //$NON-NLS-2$
        columnsMap.add(createSimpleColumn("C3", "id_Integer")); //$NON-NLS-1$ //$NON-NLS-2$
        org.talend.core.model.metadata.builder.connection.MetadataTable metadataTable = MetadataUtils.createTable(
                "testTable", columnsMap, schemaProperties); //$NON-NLS-1$
        IMetadataTable table = MetadataToolHelper.convert(metadataTable);
        table.getAdditionalProperties().remove(IComponentConstants.COMPONENT_PROPERTIES_TAG);
        table.getAdditionalProperties().remove(IComponentConstants.COMPONENT_SCHEMA_TAG);
        return table;
    }

    protected Map<String, Object> createSimpleColumn(String name, String type) {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put(IMetadataTableConstants.COLUMN_NAME, name);
        columnMap.put(IMetadataTableConstants.COLUMN_TYPE, type);
        return columnMap;
    }

    protected IElementParameter updateParameterNameByType(INode node, String paramType, String newName) {
        IElementParameter schemaElementParameter = node.getElementParameterFromField(EParameterFieldType.valueOf(paramType));
        if (schemaElementParameter != null) {
            schemaElementParameter.setName(newName);
        }
        return schemaElementParameter;
    }

    protected void removeParameterByType(INode node, String paramType) {
        IElementParameter elementParameter = node.getElementParameterFromField(EParameterFieldType.valueOf(paramType));
        if (elementParameter != null) {
            node.getElementParameters().remove(elementParameter);
        }
    }

    protected IElementParameter createSchemaParameter(Node node) {
        IElementParameter schemaParam = new ElementParameter(node);
        schemaParam.setName(SCHEMA_PARAM_NAME);
        schemaParam.setFieldType(EParameterFieldType.SCHEMA_REFERENCE);
        schemaParam.setContext("FLOW"); //$NON-NLS-1$

        ElementParameter newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.SCHEMA_TYPE.getName());
        newParam.setDisplayName(EParameterName.SCHEMA_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] { EmfComponent.TEXT_BUILTIN, EmfComponent.TEXT_REPOSITORY });
        newParam.setListItemsDisplayCodeName(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        newParam.setListItemsValue(new String[] { EmfComponent.BUILTIN, EmfComponent.REPOSITORY });
        newParam.setValue(EmfComponent.BUILTIN);
        newParam.setNumRow(1);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setShow(true);
        newParam.setReadOnly(true);

        newParam.setContext("FLOW"); //$NON-NLS-1$
        newParam.setParentParameter(schemaParam);

        newParam = new ElementParameter(node);
        newParam.setCategory(EComponentCategory.BASIC);
        newParam.setName(EParameterName.REPOSITORY_SCHEMA_TYPE.getName());
        newParam.setDisplayName(EParameterName.REPOSITORY_SCHEMA_TYPE.getDisplayName());
        newParam.setListItemsDisplayName(new String[] {});
        newParam.setListItemsValue(new String[] {});
        newParam.setNumRow(1);
        newParam.setFieldType(EParameterFieldType.TECHNICAL);
        newParam.setValue(""); //$NON-NLS-1$
        newParam.setShow(false);
        newParam.setRequired(true);
        newParam.setContext("FLOW"); //$NON-NLS-1$
        newParam.setParentParameter(schemaParam);

        return schemaParam;
    }

    protected ElementParameter createUpdateParameter(IElement element) {
        ElementParameter param = new ElementParameter(element);
        param.setName(EParameterName.UPDATE_COMPONENTS.getName());
        param.setValue(true);
        param.setDisplayName(EParameterName.UPDATE_COMPONENTS.getDisplayName());
        param.setFieldType(EParameterFieldType.CHECK);
        param.setCategory(EComponentCategory.TECHNICAL);
        param.setNumRow(1000);
        param.setReadOnly(true);
        param.setRequired(false);
        param.setShow(false);
        return param;
    }

}
