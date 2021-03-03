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
package org.talend.designer.runprocess.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.PluginChecker;
import org.talend.core.model.general.Project;
import org.talend.core.model.general.TalendJobNature;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.RepositoryObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.maven.model.TalendJavaProjectConstants;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.repository.ProjectManager;

/**
 * DOC zwxue class global comment. Detailled comment
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TalendJavaProjectManagerTest {

    private ProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

    private Property property;

    @Before
    public void setUp() throws Exception {
        property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId(factory.getNextId());
        property.setLabel("testTalendJavaProjectManagerTest");
        property.setVersion(VersionUtils.DEFAULT_VERSION);

        ProcessItem item = PropertiesFactory.eINSTANCE.createProcessItem();
        ProcessType process = TalendFileFactory.eINSTANCE.createProcessType();
        item.setProcess(process);
        item.setProperty(property);

        factory.create(item, new Path(""));
    }

    @Test
    public void testInitJavaProjects() {
        Project projectTechName = ProjectManager.getInstance().getCurrentProject();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IFolder pomsFolder = root.getFolder(new Path(projectTechName + "/poms"));
        assertTrue(pomsFolder.exists());
        IFile pomFile = pomsFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
        assertTrue(pomFile.exists());

        IFolder codesFolder = pomsFolder.getFolder(TalendJavaProjectConstants.DIR_CODES);
        assertTrue(codesFolder.exists());
        IFolder routinesFolder = codesFolder.getFolder(TalendJavaProjectConstants.DIR_ROUTINES);
        assertTrue(routinesFolder.exists());
        IFile routinesPom = routinesFolder.getFile(TalendMavenConstants.POM_FILE_NAME);
        assertTrue(routinesPom.exists());

        IFolder jobsFolder = pomsFolder.getFolder(TalendJavaProjectConstants.DIR_JOBS);
        assertTrue(jobsFolder.exists());
        IFolder processFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_PROCESS);
        assertTrue(processFolder.exists());

        if (PluginChecker.isJobLetPluginLoaded()) {
            IFolder jobletsFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_JOBLETS);
            assertTrue(jobletsFolder.exists());
        }
        if (PluginChecker.isSparkJobLetPluginLoaded()) {
            IFolder sparkKobletsFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_JOBLETS_SPARK);
            assertTrue(sparkKobletsFolder.exists());
        }
        if (PluginChecker.isSparkStreamingJobLetPluginLoaded()) {
            IFolder sparkstrJobletsFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_JOBLETS_SPARK_STREAMING);
            assertTrue(sparkstrJobletsFolder.exists());
        }

        if (PluginChecker.isMapReducePluginLoader()) {
            IFolder processMRFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_PROCESS_MR);
            assertTrue(processMRFolder.exists());
        }
        if (PluginChecker.isStormPluginLoader()) {
            IFolder processStormFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_PROCESS_STORM);
            assertTrue(processStormFolder.exists());
        }
        if (PluginChecker.isRouteLoaded()) {
            IFolder processRouteFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_PROCESS_ROUTES);
            assertTrue(processRouteFolder.exists());
        }
        if (PluginChecker.isServiceLoaded()) {
            IFolder processServiceFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_PROCESS_SERVICES);
            assertTrue(processServiceFolder.exists());
        }
        if (PluginChecker.isRouteletLoaded()) {
            IFolder routeletsFolder = jobsFolder.getFolder(TalendJavaProjectConstants.DIR_ROUTELETS);
            assertTrue(routeletsFolder.exists());
        }
    }

    @Test
    public void testGetTalendCodeJavaProject() throws Exception {
        ITalendProcessJavaProject talendJavaProject = TalendJavaProjectManager
                .getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);

        validateProject(talendJavaProject, false);

        String projectTechName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel() + "_"
                + ERepositoryObjectType.ROUTINES.name();
        assertEquals(projectTechName, talendJavaProject.getProject().getName());
    }

    @Test
    public void testGetTalendJobJavaProject() throws Exception {
        boolean enbleMavenNature = false;
        ITalendProcessJavaProject talendJavaProject = TalendJavaProjectManager
                .getTalendJobJavaProject(property, enbleMavenNature);

        validateProject(talendJavaProject, enbleMavenNature);

        String projectTechName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel() + "_"
                + property.getLabel().toUpperCase() + "_" + property.getVersion();
        assertEquals(projectTechName, talendJavaProject.getProject().getName());
    }

    @Test
    public void testGetTempJavaProject() throws Exception {
        ITalendProcessJavaProject talendJavaProject = TalendJavaProjectManager.getTempJavaProject();

        validateProject(talendJavaProject, true);

        assertEquals(TalendMavenConstants.PROJECT_NAME, talendJavaProject.getProject().getName());
    }

    @Test
    public void testGetCodeProjectId() {
        String projectTechName = "testProject";
        String routinesId = TalendJavaProjectManager.getCodeProjectId(ERepositoryObjectType.ROUTINES, projectTechName);
        assertEquals(projectTechName + "|ROUTINES", routinesId);
        String beansId = TalendJavaProjectManager.getCodeProjectId(ERepositoryObjectType.BEANS, projectTechName);
        assertEquals(projectTechName + "|BEANS", beansId);
    }

    @Test
    public void testZDeleteEclipseProjectByNatureId() throws Exception {
        ITalendProcessJavaProject routinesProject = TalendJavaProjectManager
                .getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);
        ITalendProcessJavaProject jobProject = TalendJavaProjectManager.getTalendJobJavaProject(property);
        ITalendProcessJavaProject tempProject = TalendJavaProjectManager.getTempJavaProject();

        TalendJavaProjectManager.deleteEclipseProjectByNatureId(TalendJobNature.ID);

        assertFalse(routinesProject.getProject().exists());
        assertFalse(jobProject.getProject().exists());
        assertFalse(tempProject.getProject().exists());
    }

    private void validateProject(ITalendProcessJavaProject talendJavaProject, boolean enbleMavenNature) throws Exception {
        assertNotNull(talendJavaProject);
        IProject project = talendJavaProject.getProject();
        assertNotNull(project);
        assertTrue(project.exists());
        assertTrue(project.isOpen());
        assertTrue(project.hasNature(TalendJobNature.ID));

        if (enbleMavenNature) {
            assertTrue(project.hasNature(JavaCore.NATURE_ID));
            assertTrue(project.hasNature("org.eclipse.m2e.core.maven2Nature"));

            IJavaProject javaProject = talendJavaProject.getJavaProject();
            assertNotNull(javaProject);
            assertTrue(javaProject.exists());
            assertTrue(javaProject.isOpen());
            assertTrue(javaProject.getRawClasspath() != null && javaProject.getRawClasspath().length > 0);
        }

        assertTrue(talendJavaProject.getProjectPom().exists());
    }

    @After
    public void tearDown() throws Exception {
        factory.deleteObjectPhysical(new RepositoryObject(property));
    }
}
