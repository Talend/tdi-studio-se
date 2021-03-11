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
package core.model.components;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.DummyMetadataTalendTypeFilter;
import org.talend.core.model.metadata.MetadataTalendTypeFilter;
import org.talend.core.model.metadata.MrMetadataTalendTypeFilter;
import org.talend.core.model.metadata.SparkMetadataTalendTypeFilter;
import org.talend.core.model.metadata.StormMetadataTalendTypeFilter;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.core.runtime.projectsetting.RuntimeLineageManager;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.model.process.DataNode;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.ProjectManager;
import org.talend.utils.json.JSONObject;

import us.monoid.json.JSONArray;

/**
 * created by rdubois on 17 juin 2015 Detailled comment
 *
 */
public class NodeUtilTest {

    static final String TEST_FILE_PREFIX = "org.talend.runtimeLineage"; //$NON-NLS-1$

    private RuntimeLineageManager runtimeLineageManager = new RuntimeLineageManager();

    @BeforeClass
    @AfterClass
    public static void clean() throws PersistenceException {
        Project currentProject = ProjectManager.getInstance().getCurrentProject();
        IProject project = ResourceUtils.getProject(currentProject);
        IFolder settingsFolder = project.getFolder(ProjectPreferenceManager.DEFAULT_PREFERENCES_DIRNAME);
        if (settingsFolder.exists()) {
            File folder = settingsFolder.getLocation().toFile();
            File[] testPrefFiles = folder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(TEST_FILE_PREFIX);
                }
            });
            if (testPrefFiles != null) {
                for (File f : testPrefFiles) {
                    f.delete();
                }
            }
        }
    }

    public void init() {
        try {
            ProjectPreferenceManager prefManager = runtimeLineageManager.getPrefManager();
            JSONArray jobsJson = new JSONArray();
            JSONObject jobJson = new JSONObject();
            jobJson.put(runtimeLineageManager.JOB_ID, "_HT5BMNFmEeqhpr5Qh0-X9g");
            jobsJson.put(jobJson);
            prefManager.setValue(runtimeLineageManager.RUNTIMELINEAGE_ALL, true);
            prefManager.setValue(runtimeLineageManager.RUNTIMELINEAGE_SELECTED, jobsJson.toString());
            prefManager.save();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    @Test
    public void testIsJobUsingRuntimeLineage4StandardJob() {
        init();
        runtimeLineageManager.load();
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        Process process = new Process(property);
        assertTrue(NodeUtil.isJobUsingRuntimeLineage(process));
    }

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
