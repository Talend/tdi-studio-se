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
package org.talend.designer.core.ui.editor.properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * created by hcyi on Feb 24, 2017 Detailled comment
 *
 */
public class NodeQueryCheckUtilTest {

    protected Node node;

    @Before
    public void setUp() throws Exception {
        node = mock(Node.class);
        IComponent component = mock(IComponent.class);
        node.setLabel("tMysqlInput_1");
        List<IMetadataTable> metadataList = new ArrayList<IMetadataTable>();
        IMetadataTable table = createSimpleMetadata();
        table.setLabel("l1");
        table.setTableName("t1");
        table.setAttachedConnector("FLOW");
        metadataList.add(table);
        node.setMetadataList(metadataList);
        when(node.getComponent()).thenReturn(component);
        when(node.getMetadataList()).thenReturn(metadataList);
    }

    @Test
    public void testCheckQueryOKNULL() {
        String sqlTest = null;
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
    }

    @Test
    public void testCheckQueryOKNullCharacter() {
        String sqlTest = "";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
    }

    @Test
    public void testCheckQueryOKCase1() {
        String sqlTest = "select a1,a2,a3,a4 from t1";
        assertTrue(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
    }

    @Test
    public void testCheckQueryOKCase2() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n"
                + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " count(distinct DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER) AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
    }

    @Test
    public void testCheckQueryOKCase3() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n"
                + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " --AGREEMENT_STATUS_VW.FUND_MANAGER_ANC_ID, --AS סטטוס_עמית, \r\n"
                + " /*AGREEMENT_STATUS_VW.BANK_ID,*/ --AS סטטוס_עמית, \r\n"
                + " DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
    }

    @Test
    public void testCheckQueryOKCase4() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " /*DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n*/"
                + " --AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " count(distinct DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER) AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
    }

    @Test
    public void testMatchQueryCommentsCase1() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n"
                + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " count(distinct DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER) AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
        String matchSql = NodeQueryCheckUtil.matchQueryComments(node, sqlTest);
        assertTrue(NodeQueryCheckUtil.checkQueryOK(node, matchSql));
    }

    @Test
    public void testMatchQueryCommentsCase2() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n"
                + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " --AGREEMENT_STATUS_VW.FUND_MANAGER_ANC_ID, --AS סטטוס_עמית, \r\n"
                + " /*AGREEMENT_STATUS_VW.BANK_ID,*/ --AS סטטוס_עמית, \r\n"
                + " DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
        String matchSql = NodeQueryCheckUtil.matchQueryComments(node, sqlTest);
        assertTrue(NodeQueryCheckUtil.checkQueryOK(node, matchSql));
    }

    @Test
    public void testMatchQueryCommentsCase3() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " /*DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n*/"
                + " --AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " count(distinct DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER) AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
        String matchSql = NodeQueryCheckUtil.matchQueryComments(node, sqlTest);
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, matchSql));
    }

    @Test
    public void testNeedMatchQueryCase1() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n"
                + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " count(distinct DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER) AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
        assertTrue(NodeQueryCheckUtil.isNeedMatchQuery());
        String matchSql = NodeQueryCheckUtil.matchQueryComments(node, sqlTest);
        assertTrue(NodeQueryCheckUtil.checkQueryOK(node, matchSql));
        assertFalse(NodeQueryCheckUtil.isNeedMatchQuery());
    }

    @Test
    public void testNeedMatchQueryCase2() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n"
                + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " --AGREEMENT_STATUS_VW.FUND_MANAGER_ANC_ID, --AS סטטוס_עמית, \r\n"
                + " /*AGREEMENT_STATUS_VW.BANK_ID,*/ --AS סטטוס_עמית, \r\n"
                + " DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
        assertTrue(NodeQueryCheckUtil.isNeedMatchQuery());
        String matchSql = NodeQueryCheckUtil.matchQueryComments(node, sqlTest);
        assertTrue(NodeQueryCheckUtil.checkQueryOK(node, matchSql));
        assertFalse(NodeQueryCheckUtil.isNeedMatchQuery());
    }

    @Test
    public void testNeedMatchQueryCase3() {
        String sqlTest = "\"select DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER, --AS מספר_מעסיק \r\n"
                + " /*DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,-- AS משפחת_קופה, \r\n*/"
                + " --AGREEMENT_STATUS_VW.AGMT_STATUS_DESC, --AS סטטוס_עמית, \r\n"
                + " count(distinct DIM_POLICYHOLDER_CUR.IDENTIFICATION_DOC_NUMBER) AS CountAmit \r\n" + " FROM\r\n"
                + " ADW.F_GML_IND_AGMT_VW C01_F_GML_IND_AGMT\r\n" + " INNER JOIN ADW.AGREEMENT_STATUS_VW AGREEMENT_STATUS_VW\r\n"
                + " ON C01_F_GML_IND_AGMT.INDIVIDUAL_AGMT_STATUS = AGREEMENT_STATUS_VW.AGMT_STATUS_ID\r\n"
                + " INNER JOIN ADW.DIM_EMPLR_CUR DIM_EMPLR_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.EMPLR_ANC_ID = DIM_EMPLR_CUR.EMPLR_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_GML_FUND_CUR DIM_GML_FUND\r\n"
                + " ON C01_F_GML_IND_AGMT.FUND_ANC_ID = DIM_GML_FUND.FUND_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_POLICYHOLDER_CUR DIM_POLICYHOLDER_CUR\r\n"
                + " ON C01_F_GML_IND_AGMT.POLICYHOLDER_ANC_ID = DIM_POLICYHOLDER_CUR.POLICYHOLDER_ANC_ID\r\n"
                + " INNER JOIN ADW.PROCESS_RUN_LOG_VW PROCESS_RUN_LOG_VAL\r\n" + " ON \r\n"
                + " C01_F_GML_IND_AGMT.VALIDITY_DATE = PROCESS_RUN_LOG_VAL.VALIDITY_END_DATE AND\r\n"
                + " C01_F_GML_IND_AGMT.FUND_MANAGER_ANC_ID = PROCESS_RUN_LOG_VAL.FUND_MANAGER_ANC_ID\r\n"
                + " INNER JOIN ADW.DIM_FUND_FAMILY_VW DIM_FUND_FAMILY_VW\r\n"
                + " ON DIM_GML_FUND.FUND_FAMILY_ID = DIM_FUND_FAMILY_VW.FUND_FAMILY_ID \r\n" + "WHERE \r\n"
                + " DIM_GML_FUND.TYPE_ID = 3 AND\r\n" + " PROCESS_RUN_LOG_VAL.BANK_ID IN ( '10', '62', '42' )\r\n"
                + "group by \r\n" + " DIM_EMPLR_CUR.IDENTIFICATION_DOC_NUMBER,\r\n" + " \r\n"
                + " DIM_FUND_FAMILY_VW.FUND_FAMILY_NAME,\r\n" + " AGREEMENT_STATUS_VW.AGMT_STATUS_DESC\"";
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, sqlTest));
        assertTrue(NodeQueryCheckUtil.isNeedMatchQuery());
        String matchSql = NodeQueryCheckUtil.matchQueryComments(node, sqlTest);
        assertFalse(NodeQueryCheckUtil.checkQueryOK(node, matchSql));
        assertTrue(NodeQueryCheckUtil.isNeedMatchQuery());
    }

    private IMetadataTable createSimpleMetadata() {
        IMetadataTable table = new org.talend.core.model.metadata.MetadataTable();
        IMetadataColumn column1 = new MetadataColumn();
        column1.setLabel("C1"); //$NON-NLS-1$
        column1.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column1);

        IMetadataColumn column2 = new MetadataColumn();
        column2.setLabel("C2"); //$NON-NLS-1$
        column2.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column2);

        IMetadataColumn column3 = new MetadataColumn();
        column3.setLabel("C3"); //$NON-NLS-1$
        column3.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column3);

        IMetadataColumn column4 = new MetadataColumn();
        column4.setLabel("C4"); //$NON-NLS-1$
        column4.setTalendType("id_String"); //$NON-NLS-1$
        table.getListColumns().add(column4);
        return table;
    }
}
