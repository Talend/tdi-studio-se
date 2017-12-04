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
package core.model.components;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.metadata.DummyMetadataTalendTypeFilter;
import org.talend.core.model.metadata.MetadataTalendTypeFilter;
import org.talend.core.model.metadata.MrMetadataTalendTypeFilter;
import org.talend.core.model.metadata.SparkMetadataTalendTypeFilter;
import org.talend.core.model.metadata.StormMetadataTalendTypeFilter;
import org.talend.core.model.process.INode;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.process.DataNode;

/**
 * created by rdubois on 17 juin 2015 Detailled comment
 *
 */
public class NodeUtilTest {

    @Test
    public void testIsBigDataFrameworkNode() {
        DummyComponent comp = Mockito.mock(DummyComponent.class);
        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_CAMEL.getName());
        INode node = new DataNode(comp, ""); //$NON-NLS-1$

        assertFalse(NodeUtil.isBigDataFrameworkNode(node));

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_DI.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        assertFalse(NodeUtil.isBigDataFrameworkNode(node));

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_MAPREDUCE.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        assertTrue(NodeUtil.isBigDataFrameworkNode(node));

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_SPARK.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        assertTrue(NodeUtil.isBigDataFrameworkNode(node));

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        assertTrue(NodeUtil.isBigDataFrameworkNode(node));

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_STORM.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        assertTrue(NodeUtil.isBigDataFrameworkNode(node));

    }

    @Test
    public void createMetadataTalendTypeFilter() {
        DummyComponent comp = Mockito.mock(DummyComponent.class);
        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_CAMEL.getName());
        INode node = new DataNode(comp, ""); //$NON-NLS-1$
        MetadataTalendTypeFilter filter = NodeUtil.createMetadataTalendTypeFilter(node);

        assertTrue(filter instanceof DummyMetadataTalendTypeFilter);

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_DI.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$
        filter = NodeUtil.createMetadataTalendTypeFilter(node);

        assertTrue(filter instanceof DummyMetadataTalendTypeFilter);

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_MAPREDUCE.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        filter = NodeUtil.createMetadataTalendTypeFilter(node);

        assertTrue(filter instanceof MrMetadataTalendTypeFilter);

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_SPARK.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        filter = NodeUtil.createMetadataTalendTypeFilter(node);

        assertTrue(filter instanceof SparkMetadataTalendTypeFilter);

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_SPARKSTREAMING.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        filter = NodeUtil.createMetadataTalendTypeFilter(node);

        assertTrue(filter instanceof SparkMetadataTalendTypeFilter);

        Mockito.when(comp.getType()).thenReturn(ComponentCategory.CATEGORY_4_STORM.getName());
        node = new DataNode(comp, ""); //$NON-NLS-1$

        filter = NodeUtil.createMetadataTalendTypeFilter(node);

        assertTrue(filter instanceof StormMetadataTalendTypeFilter);

    }
}
