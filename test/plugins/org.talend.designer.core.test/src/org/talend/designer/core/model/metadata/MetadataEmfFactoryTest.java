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
package org.talend.designer.core.model.metadata;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;

/**
 * created by ycbai on 2016年6月8日 Detailled comment
 *
 */
public class MetadataEmfFactoryTest {

    private MetadataEmfFactory factory;

    @Before
    public void before() {
        factory = new MetadataEmfFactory();
    }

    @Test
    public void testOriginalLengthValue() {
        // OriginalLength of column is null.
        IMetadataTable table = createTestTable();
        factory.setMetadataTable(table);
        MetadataType metadataType = factory.getMetadataType();
        ColumnType col1 = (ColumnType) metadataType.getColumn().get(0);
        assertEquals(-1, col1.getOriginalLength());

        // OriginalLength of column is not null.
        table = createTestTable();
        table.getListColumns().get(0).setOriginalLength(10);
        factory.setMetadataTable(table);
        metadataType = factory.getMetadataType();
        col1 = (ColumnType) metadataType.getColumn().get(0);
        assertEquals(10, col1.getOriginalLength());
    }

    private IMetadataTable createTestTable() {
        IMetadataTable table = new MetadataTable();
        IMetadataColumn column = new MetadataColumn();
        column.setLabel("C1"); //$NON-NLS-1$
        column.setTalendType(JavaTypesManager.INTEGER.getId());
        table.getListColumns().add(column);
        column = new MetadataColumn();
        column.setLabel("C2"); //$NON-NLS-1$
        column.setTalendType(JavaTypesManager.STRING.getId());
        table.getListColumns().add(column);
        column = new MetadataColumn();
        column.setLabel("C3"); //$NON-NLS-1$
        column.setTalendType(JavaTypesManager.STRING.getId());
        table.getListColumns().add(column);
        return table;
    }

}
