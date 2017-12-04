// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.language.oracle;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.IConnection;
import org.talend.designer.dbmap.DbMapComponent;

/**
 * created by ggu on Jun 25, 2012 Detailled comment
 * 
 */

public class OracleGenerationManagerTest {

    private DbMapComponent dbMapComponent;

    private IConnection conn;

    private OracleGenerationManager oracleManager;

    @Before
    public void setUp() throws Exception {
        dbMapComponent = new DbMapComponent();

        List<IConnection> incomingConnections = new ArrayList<IConnection>();

        conn = mock(IConnection.class);
        incomingConnections.add(conn);
        dbMapComponent.setIncomingConnections(incomingConnections);

        oracleManager = new OracleGenerationManager();

    }

    @After
    public void tearDown() throws Exception {
        dbMapComponent = null;
        conn = null;
        oracleManager = null;
    }

    /**
     * 
     * for TDI-21413
     * 
     */
    @Test
    public void testAddQuoteForSpecialChar_NPE() throws Exception {
        // only test the conn.getMetadataTable() for NPE
        checkValue("test.ABC", "test.ABC"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * 
     * for TDI-21413
     * 
     */
    @Test
    public void testAddQuoteForSpecialChar_Order1() throws Exception {
        // positive-sequence
        when(conn.getMetadataTable()).thenReturn(createMetadataTable(new String[] { "都市名", "国", "国(コード)",//$NON-NLS-1$ //$NON-NLS-2$
                "国(コード)123", "abc$1234", "abc$1234xyz" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        testMore();
        //
    }

    @Test
    public void testAddQuoteForSpecialChar_Order2() throws Exception {
        // reverse order
        when(conn.getMetadataTable()).thenReturn(createMetadataTable(new String[] { "都市名", "国(コード)123", "国(コード)",//$NON-NLS-1$ //$NON-NLS-2$
                "国", "abc$1234", "abc$1234xyz" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        testMore();
        //
    }

    @Test
    public void testAddQuoteForSpecialChar_Order3() throws Exception {

        when(conn.getMetadataTable()).thenReturn(createMetadataTable(new String[] { "都市名", "国(コード)", "国(コード)123",//$NON-NLS-1$ //$NON-NLS-2$
                "国", "abc$1234", "abc$1234xyz" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        testMore();
        //
    }

    @Test
    public void testAddQuoteForSpecialChar_Order4() throws Exception {

        when(conn.getMetadataTable()).thenReturn(createMetadataTable(new String[] { "都市名", "国", "国(コード)123",//$NON-NLS-1$ //$NON-NLS-2$
                "国(コード)", "abc$1234", "abc$1234xyz" })); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        testMore();
        //
    }

    @Test
    public void testAddQuoteForSpecialChar_Escape5() throws Exception {
        when(conn.getMetadataTable())
                .thenReturn(createMetadataTable(new String[] { "\\\"id\\\"", "\\\"name\\\"", "$acount", "_age", "email" }));

        testEscape();
        //
    }

    public void testEscape() throws Exception {

        checkValue("A.\\\"id\\\"", "A.\\\"id\\\"");
        checkValue("A.\\\"name\\\"", "A.\\\"name\\\"");
        checkValue("A.$acount", "A.\\\"$acount\\\"");
        checkValue("A.\\\"_age\\\"", "A.\\\"_age\\\"");
        checkValue("A.email", "A.email");
        // context
        checkValue("context.schema.\\\"id\\\"", "context.schema.\\\"id\\\"");
        checkValue("context.schema.\\\"name\\\"", "context.schema.\\\"name\\\"");
        checkValue("context.schema.$acount", "context.schema.\\\"$acount\\\"");
        checkValue("context.schema._age", "context.schema._age");
        checkValue("context.schema.email", "context.schema.email");
    }

    private void testMore() throws Exception {

        checkValue("A.都市名", "A.\\\"都市名\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.国(コード)", "A.\\\"国(コード)\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.国(コード)123", "A.\\\"国(コード)123\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.国", "A.\\\"国\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.abc$1234", "A.\\\"abc$1234\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("A.abc$1234xyz", "A.\\\"abc$1234xyz\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        // context
        // not good context
        checkValue("context.schema.国", "context.schema.\\\"国\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("context.schema.国(コード)", "context.schema.\\\"国(コード)\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("context.schema.国(コード)123", "context.schema.\\\"国(コード)123\\\""); //$NON-NLS-1$ //$NON-NLS-2$
        // should be context model
        checkValue("context.schema+\".国\"", "context.schema+\".\\\"国\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("context.schema+\".国(コード)\"", "context.schema+\".\\\"国(コード)\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
        checkValue("context.schema+\".国(コード)123\"", "context.schema+\".\\\"国(コード)123\\\"\""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void checkValue(String expression, String expected) throws Exception {

        Assert.assertEquals(expected, oracleManager.addQuoteForSpecialChar(expression, dbMapComponent));
    }

    private IMetadataTable createMetadataTable(String... columnLabels) {
        IMetadataTable metadataTable = new MetadataTable();

        // will be error, strange
        // List<IMetadataColumn> columns = mock(List.class);
        List<IMetadataColumn> columns = new ArrayList<IMetadataColumn>();
        if (columnLabels != null) {
            for (String columnlabel : columnLabels) {
                columns.add(createColumn(columnlabel));
            }
        }
        // when(metadataTable.getListColumns()).thenReturn(columns);
        metadataTable.setListColumns(columns);
        return metadataTable;
    }

    private IMetadataColumn createColumn(String label) {
        IMetadataColumn column = new MetadataColumn();

        // will be error, strange
        // when(column.getLabel()).thenReturn(label);
        column.setLabel(label);
        column.setOriginalDbColumnName(label);
        return column;
    }
}
