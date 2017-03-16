// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.java;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.talend.commons.CommonsPlugin;
import org.talend.core.CorePlugin;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.runprocess.ProcessorException;

/**
 * created by wchen on Mar 16, 2017 Detailled comment
 *
 */
public class JavaProcessorUtilitiesTest {

    @Test
    public void testComputeLibrariesPath() {
        boolean headless = CommonsPlugin.isHeadless();
        try {
            CommonsPlugin.setHeadless(true);
            IComponent tFixedFlowComponent = ComponentsFactoryProvider.getInstance().get("tFixedFlowInput",
                    ComponentCategory.CATEGORY_4_DI.getName());
            IComponent tLogRowComponent = ComponentsFactoryProvider.getInstance().get("tLogRow",
                    ComponentCategory.CATEGORY_4_DI.getName());
            Property property = PropertiesFactory.eINSTANCE.createProperty();
            property.setId("property"); //$NON-NLS-1$
            property.setVersion("0.1"); //$NON-NLS-1$
            property.setLabel("test");//$NON-NLS-1$
            Process process = new Process(property);
            Node tFixedFlowInput_1 = new Node(tFixedFlowComponent, process);
            createMetadataColumns(tFixedFlowInput_1.getMetadataList().get(0), 1);
            Node tLogRow_1 = new Node(tLogRowComponent, process);
            createMetadataColumns(tLogRow_1.getMetadataList().get(0), 1);
            process.addNodeContainer(new NodeContainer(tFixedFlowInput_1));
            process.addNodeContainer(new NodeContainer(tLogRow_1));
            new Connection(tFixedFlowInput_1, tLogRow_1, EConnectionType.FLOW_MAIN, EConnectionType.FLOW_MAIN.getName(),
                    "tFixedFlowInput_1", "row1", "row1", false);

            Set<ModuleNeeded> neededLibraries = CorePlugin.getDefault().getDesignerCoreService()
                    .getNeededLibrariesForProcess(process, false);
            try {
                JavaProcessorUtilities.computeLibrariesPath(neededLibraries, process, new HashSet<ModuleNeeded>());
            } catch (ProcessorException e) {
                fail("computeLibrariesPath failed for process test{tFixedFlowInput->tLogRow}");
            }
        } finally {
            CommonsPlugin.setHeadless(headless);
        }
    }

    @Test
    public void testComputeLibrariesPathWithMissingJars() {
        boolean headless = CommonsPlugin.isHeadless();
        try {
            // don't popup dialog to block ui
            CommonsPlugin.setHeadless(true);
            IComponent tLibraryLoadComponent = ComponentsFactoryProvider.getInstance().get("tLibraryLoad",
                    ComponentCategory.CATEGORY_4_DI.getName());
            Property property = PropertiesFactory.eINSTANCE.createProperty();
            property.setId("property"); //$NON-NLS-1$
            property.setVersion("0.1"); //$NON-NLS-1$
            property.setLabel("test");//$NON-NLS-1$
            Process process = new Process(property);
            Node tLibraryLoad_1 = new Node(tLibraryLoadComponent, process);
            process.addNodeContainer(new NodeContainer(tLibraryLoad_1));
            tLibraryLoad_1.getElementParameter("LIBRARY").setValue("computeLibrariesPath.jar");
            Set<ModuleNeeded> neededLibraries = CorePlugin.getDefault().getDesignerCoreService()
                    .getNeededLibrariesForProcess(process, false);
            ProcessorException exception = null;
            try {
                JavaProcessorUtilities.computeLibrariesPath(neededLibraries, process, new HashSet<ModuleNeeded>());
            } catch (ProcessorException e) {
                exception = e;
            }
            if (exception != null) {
                Assert.assertTrue(exception.getMessage().contains("computeLibrariesPath.jar"));
            } else {
                fail("computeLibrariesPath failed for process test{tLibraryLoad},computeLibrariesPath.jar not found but no exception");
            }
        } finally {
            CommonsPlugin.setHeadless(headless);
        }
    }

    private void createMetadataColumns(IMetadataTable table, int columns) {
        for (int i = 0; i < columns; i++) {
            MetadataColumn newColumn = new MetadataColumn();
            newColumn.setLabel("newColumn" + i);
            newColumn.setTalendType("id_String");
            table.getListColumns().add(newColumn);
        }
    }

}
