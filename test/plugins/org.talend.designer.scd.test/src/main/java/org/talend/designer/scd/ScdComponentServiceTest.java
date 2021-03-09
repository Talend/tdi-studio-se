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
package org.talend.designer.scd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.FakePropertyImpl;
import org.talend.core.service.IScdComponentService;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on Jun 15, 2017 Detailled comment
 *
 */
public class ScdComponentServiceTest {

    @Test
    public void testUpdateOutputMetadata() {
        Property property = new FakePropertyImpl();
        Process process = new Process(property);
        IComponent tPostgresqlSCD = ComponentsFactoryProvider.getInstance().get("tPostgresqlSCD",
                ComponentCategory.CATEGORY_4_DI.getName());
        Node tPostgresqlSCDNode = new Node(tPostgresqlSCD, process);
        process.addNodeContainer(new NodeContainer(tPostgresqlSCDNode));

        // tDBSCD_1
        IMetadataTable metadataTable = tPostgresqlSCDNode.getMetadataTable("tDBSCD_1");
        metadataTable.getListColumns().add(createColumn("newColumn", "id_String"));
        IMetadataColumn idColumn = createColumn("id", "id_String");
        metadataTable.getListColumns().add(idColumn);
        IMetadataColumn startColumn = createColumn("scd_start", "id_Date");
        metadataTable.getListColumns().add(startColumn);
        IMetadataColumn endColumn = createColumn("scd_end", "id_Date");
        metadataTable.getListColumns().add(endColumn);
        IMetadataColumn versionColumn = createColumn("scd_version", "id_Integer");
        metadataTable.getListColumns().add(versionColumn);
        IMetadataColumn activeColumn = createColumn("scd_active", "id_Integer");
        metadataTable.getListColumns().add(activeColumn);

        IElementParameter surrogateKey = tPostgresqlSCDNode.getElementParameter(ScdParameterConstants.SURROGATE_KEY);
        surrogateKey.setValue("id");

        IElementParameter elementParameter = tPostgresqlSCDNode.getElementParameter(ScdParameterConstants.L2_FIELDS_PARAM_NAME);
        List<Map<String, String>> values = new ArrayList<Map<String, String>>();
        Map<String, String> type2Columns = new HashMap<String, String>();
        type2Columns.put("NAME", "newColumn");
        values.add(type2Columns);
        elementParameter.setValue(values);

        // start date
        IElementParameter startDate = tPostgresqlSCDNode.getElementParameter(ScdParameterConstants.L2_STARTDATE_FIELD);
        startDate.setValue("scd_start");
        // end date
        IElementParameter endDate = tPostgresqlSCDNode.getElementParameter(ScdParameterConstants.L2_ENDDATE_FIELD);
        endDate.setValue("scd_end");
        // version
        IElementParameter versionCheked = tPostgresqlSCDNode.getElementParameter(ScdParameterConstants.USE_L2_VERSION);
        versionCheked.setValue(true);
        IElementParameter version = tPostgresqlSCDNode.getElementParameter(ScdParameterConstants.L2_VERSION_FIELD);
        version.setValue("scd_version");
        // activate
        IElementParameter activateCheked = tPostgresqlSCDNode.getElementParameter(ScdParameterConstants.USE_L2_ACTIVE);
        activateCheked.setValue(true);
        IElementParameter activate = tPostgresqlSCDNode.getElementParameter(ScdParameterConstants.L2_ACTIVE_FIELD);
        activate.setValue("scd_active");

        IScdComponentService service = (IScdComponentService) GlobalServiceRegister.getDefault().getService(
                IScdComponentService.class);
        service.updateOutputMetadata(tPostgresqlSCDNode, metadataTable);

        Assert.assertTrue(idColumn.isCustom());
        Assert.assertTrue(startColumn.isCustom());
        Assert.assertTrue(endColumn.isCustom());
        Assert.assertTrue(versionColumn.isCustom());
        Assert.assertTrue(activeColumn.isCustom());

    }

    private IMetadataColumn createColumn(String label, String talendType) {
        IMetadataColumn newColumn = new MetadataColumn();
        newColumn.setLabel(label);
        newColumn.setTalendType(talendType);
        return newColumn;
    }

}
