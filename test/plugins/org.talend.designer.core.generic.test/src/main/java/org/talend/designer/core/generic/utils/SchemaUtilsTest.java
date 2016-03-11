// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import orgomg.cwm.objectmodel.core.Package;

/**
 * created by ycbai on 2016年3月3日 Detailled comment
 *
 */
@RunWith(PowerMockRunner.class)
public class SchemaUtilsTest {

    @Test
    public void testGetMetadataTables() {
        // Create some MetadataTables for test.
        MetadataTable table1 = createMetadataTable("table1"); //$NON-NLS-1$
        MetadataTable table2 = createMetadataTable("table2"); //$NON-NLS-1$
        MetadataTable table3 = createMetadataTable("table3"); //$NON-NLS-1$
        MetadataTable table4 = createMetadataTable("table4"); //$NON-NLS-1$

        // ///////////////////////////////////
        // Overall structure:
        //
        // . parentPackage
        // .. table1
        // .. container1
        // ... table2
        // ... container1_1
        // .... table4
        // .. container2
        // ... table3
        //
        // ///////////////////////////////////
        Package parentPackage = mock(Package.class);
        when(parentPackage.getOwnedElement()).thenReturn(new BasicEList<>());
        parentPackage.getOwnedElement().add(table1);

        Package container1 = mock(TestContainer.class);
        when(container1.getOwnedElement()).thenReturn(new BasicEList<>());
        container1.getOwnedElement().add(table2);
        Package container1_1 = mock(TestContainer.class);
        when(container1_1.getOwnedElement()).thenReturn(new BasicEList<>());
        container1_1.getOwnedElement().add(table4);
        container1.getOwnedElement().add(container1_1);
        parentPackage.getOwnedElement().add(container1);

        Package container2 = mock(OtherTestContainer.class);
        when(container2.getOwnedElement()).thenReturn(new BasicEList<>());
        container2.getOwnedElement().add(table3);
        parentPackage.getOwnedElement().add(container2);

        // When there is not any sub-container specified, will return all direct MetadataTables of the parent package.
        List<MetadataTable> metadataTables = SchemaUtils.getMetadataTables(parentPackage, null);
        assertNotNull(getTheTable(metadataTables, table1.getName()));

        // When there is one sub-container specified, will return all direct MetadataTables of the parent package and
        // sub-container recursively.
        metadataTables = SchemaUtils.getMetadataTables(parentPackage, TestContainer.class);
        assertNotNull(getTheTable(metadataTables, table1.getName()));
        assertNotNull(getTheTable(metadataTables, table2.getName()));
        assertNotNull(getTheTable(metadataTables, table4.getName()));
        assertNull(getTheTable(metadataTables, table3.getName()));
    }

    private MetadataTable getTheTable(List<MetadataTable> tableList, String tableName) {
        for (MetadataTable metadataTable : tableList) {
            if (tableName != null && tableName.equals(metadataTable.getName())) {
                return metadataTable;
            }
        }
        return null;
    }

    private MetadataTable createMetadataTable(String name) {
        MetadataTable table = mock(MetadataTable.class);
        when(table.getName()).thenReturn(name);
        when(table.getLabel()).thenReturn(name);
        return table;
    }

    /**
     * A container interface only for test.
     */
    interface TestContainer extends Package {
        // only for test.
    }

    /**
     * A container interface only for test.
     */
    interface OtherTestContainer extends Package {
        // only for test.
    }

}
