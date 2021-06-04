// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutinesJarItem;
import org.talend.core.model.routines.CodesJarInfo;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.maven.model.MavenSystemFolders;
import org.talend.designer.maven.model.TalendMavenConstants;

/**
 * created by ggu on 26 Jan 2015 Detailled comment
 *
 */
@SuppressWarnings("nls")
public class TalendProcessJavaProjectTest {

    private ITalendProcessJavaProject talendJavaProject;

    /**
     * DOC ggu Comment method "setUp".
     *
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        talendJavaProject = TalendJavaProjectManager.getTempJavaProject();
        Assert.assertNotNull(talendJavaProject);
    }

    /**
     * DOC ggu Comment method "tearDown".
     *
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        talendJavaProject = null;
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getJavaProject()}.
     */
    @Test
    public void testGetJavaProject() {
        IJavaProject javaProject = talendJavaProject.getJavaProject();
        Assert.assertNotNull(javaProject);
        Assert.assertTrue(javaProject.exists());
        Assert.assertTrue(javaProject.isOpen());
        Assert.assertNotNull(javaProject.getProject());
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getProject()}.
     */
    @Test
    public void testGetProject() {
        IProject project = talendJavaProject.getProject();
        Assert.assertNotNull(project);
        Assert.assertTrue(project.exists());
        Assert.assertTrue(project.isOpen());
        Assert.assertEquals(TalendMavenConstants.PROJECT_NAME, project.getName());
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getSrcFolder()}.
     */
    @Test
    public void testGetSrcFolder() {
        IFolder srcFolder = talendJavaProject.getSrcFolder();
        Assert.assertNotNull(srcFolder);
        Assert.assertTrue(srcFolder.exists());
        IPath projectRelativePath = srcFolder.getProjectRelativePath();
        Assert.assertEquals(MavenSystemFolders.JAVA.getPath(), projectRelativePath.toString());
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getTestSrcFolder()}.
     */
    @Test
    public void testGetTestSrcFolder() {
        IFolder testSrcFolder = talendJavaProject.getTestSrcFolder();
        Assert.assertNotNull(testSrcFolder);
        Assert.assertTrue(testSrcFolder.exists());
        IPath projectRelativePath = testSrcFolder.getProjectRelativePath();
        Assert.assertEquals(MavenSystemFolders.JAVA_TEST.getPath(), projectRelativePath.toString());
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getExternalResourcesFolder()}.
     */
    @Test
    public void testGetResourcesFolder() {
        IFolder resFolder = talendJavaProject.getExternalResourcesFolder();
        Assert.assertNotNull(resFolder);
        Assert.assertTrue(resFolder.exists());
        IPath projectRelativePath = resFolder.getProjectRelativePath();
        Assert.assertEquals(MavenSystemFolders.EXT_RESOURCES.getPath(), projectRelativePath.toString());
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getTestResourcesFolder()}.
     */
    @Test
    public void testGetTestResourcesFolder() {
        IFolder testResFolder = talendJavaProject.getTestResourcesFolder();
        Assert.assertNotNull(testResFolder);
        Assert.assertTrue(testResFolder.exists());
        IPath projectRelativePath = testResFolder.getProjectRelativePath();
        Assert.assertEquals(MavenSystemFolders.RESOURCES_TEST.getPath(), projectRelativePath.toString());
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getLibFolder()}.
     */
    @Test
    public void testGetLibFolder() {
        IFolder libFolder = talendJavaProject.getLibFolder();
        Assert.assertNotNull(libFolder);
        Assert.assertTrue(libFolder.exists());
        IPath projectRelativePath = libFolder.getProjectRelativePath();
        Assert.assertEquals(JavaUtils.JAVA_LIB_DIRECTORY, projectRelativePath.toString());
    }

    /**
     * Test method for {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getOutputFolder()}.
     */
    @Test
    public void testGetOutputFolder() {
        IFolder outputFolder = talendJavaProject.getOutputFolder();
        Assert.assertNotNull(outputFolder);
        Assert.assertTrue(outputFolder.exists());
        IPath projectRelativePath = outputFolder.getProjectRelativePath();
        Assert.assertEquals(MavenSystemFolders.JAVA.getOutputPath(), projectRelativePath.toString());
    }

    /**
     * Test method for
     * {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#cleanSources(org.eclipse.core.runtime.IProgressMonitor, org.talend.core.model.general.Project)}
     * .
     */
    @Test @Ignore
    public void testCleanSources() {
        fail("Not yet implemented");
    }

    /**
     * Test method for
     * {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getSrcSubFolder(org.eclipse.core.runtime.IProgressMonitor, java.lang.String)}
     * .
     */

    @Test
    public void testGetSrcSubFolder() {
        IFolder srcFolder = talendJavaProject.getSrcFolder();
        Assert.assertNotNull(srcFolder);
        Assert.assertTrue(srcFolder.exists());

        IFolder testFolder = talendJavaProject.getSrcSubFolder(null, "test1");
        Assert.assertNotNull(srcFolder);
        Assert.assertTrue(testFolder.exists());
        Assert.assertTrue(testFolder.getLocation().toFile().exists());
        Assert.assertEquals("test1", testFolder.getName());
        Assert.assertEquals(MavenSystemFolders.JAVA.getPath() + "/test1", testFolder.getProjectRelativePath().toString());
    }

    public void testGetSrcSubFolders() {
        IFolder srcFolder = talendJavaProject.getSrcFolder();
        Assert.assertNotNull(srcFolder);
        Assert.assertTrue(srcFolder.exists());

        IFolder testFolder = talendJavaProject.getSrcSubFolder(null, "test1/abc/xyz");
        Assert.assertNotNull(srcFolder);
        Assert.assertTrue(testFolder.exists());
        Assert.assertTrue(testFolder.getLocation().toFile().exists());
        Assert.assertEquals("xyz", testFolder.getName());
        Assert.assertEquals(MavenSystemFolders.JAVA.getPath() + "/test1/abc/xyz", testFolder.getProjectRelativePath().toString());
    }

    /**
     * Test method for
     * {@link org.talend.designer.runprocess.java.TalendProcessJavaProject#getResourceSubFolder(org.eclipse.core.runtime.IProgressMonitor, java.lang.String)}
     * .
     */
    @Test @Ignore
    public void testGetResourceSubFolder() {
        fail("Not yet implemented");
    }

    @Test
    public void testExists() throws Exception {
        RoutinesJarItem jarItem = PropertiesFactory.eINSTANCE.createRoutinesJarItem();
        Property jarProperty = PropertiesFactory.eINSTANCE.createProperty();
        jarProperty.setId(ProxyRepositoryFactory.getInstance().getNextId());
        jarProperty.setLabel("TalendProcessJavaProjectTest_testExists_routinejar1");
        jarProperty.setVersion("1.0");
        jarProperty.setItem(jarItem);
        CodesJarInfo info = CodesJarInfo.create(jarProperty);
        ITalendProcessJavaProject jarProject = TalendJavaProjectManager.getTalendCodesJarJavaProject(info);
        assertTrue(jarProject.exists());
        TalendJavaProjectManager.deleteTalendCodesJarProject(info, true);
        assertFalse(jarProject.exists());
    }

}
