// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.model.process.jobsettings;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.editor.process.Process;

/**
 * created by wchen on 2016年1月21日 Detailled comment
 *
 */
public class JobSettingsManagerTest {

    /**
     * Test method for
     * {@link org.talend.designer.core.model.process.jobsettings.JobSettingsManager#createExtraContextLoadNodes(org.talend.core.model.process.IProcess)}
     * .
     */
    @Test
    public void testCreateExtraContextLoadNodes() {
        // junit for TUP-3972
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        IProcess2 process = new Process(property);
        process.getElementParameter(EParameterName.IMPLICIT_TCONTEXTLOAD.getName()).setValue(true);
        process.getElementParameter(EParameterName.IMPLICIT_TCONTEXTLOAD_FILE.getName()).setValue("the test file");
        process.getElementParameter("FROM_FILE_FLAG_IMPLICIT_CONTEXT").setValue(true);
        final List<DataNode> createContextLoadNodes = JobSettingsManager.createExtraContextLoadNodes(process);
        assertNotEquals(createContextLoadNodes.size(), 0);
        final DataNode dataNode = createContextLoadNodes.get(0);
        final IMetadataTable metadataTable = dataNode.getMetadataList().get(0);
        for (IMetadataColumn column : metadataTable.getListColumns()) {
            assertNotNull(column.getDefault());
            assertNotNull(JavaTypesManager.getDefaultValueFromJavaType(column.getTalendType(), column.getDefault()));
        }
    }
}
