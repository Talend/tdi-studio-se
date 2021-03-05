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
package org.talend.designer.codegen.components.model;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.commons.utils.resource.BundleFileUtil;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.ProjectManager;
import org.talend.utils.files.FileUtils;
import org.talend.utils.io.FilesUtils;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class UserComponentsProviderTest {

    final String PATH_OLD_COMPONENT = "testresources/components/tHTMLInput.zip";

    final String PATH_NEW_COMPONENT = "testresources/components/components-myjira-0.16.0-SNAPSHOT-updatesite.zip";

    static class UserComponentsProviderTestClass extends UserComponentsProvider {

        public UserComponentsProviderTestClass() {
            super();
            this.setFolderName("user");
        }

        protected File getExternalComponentsLocation() {
            return null;
        }
    }

    static File backupFolder;

    @BeforeClass
    public static void prepare() throws IOException {
        backupFolder = FileUtils.createTmpFolder("Backup", "");
        // backup old
        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass();
        File installationFolder = provider.getInstallationFolder();
        if (installationFolder != null && installationFolder.exists()) {
            FilesUtils.copyFolder(installationFolder, backupFolder, false, null, null, true);
        }

    }

    @AfterClass
    public static void tearDown() throws Exception {
        // backup old
        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass();
        File installationFolder = provider.getInstallationFolder();
        if (installationFolder != null && installationFolder.exists()) {
            FilesUtils.copyFolder(backupFolder, installationFolder, true, null, null, true);
        }
        FilesUtils.deleteFolder(backupFolder, true);
    }

    File workFolder;

    @Before
    public void setup() throws Exception {
        workFolder = FileUtils.createTmpFolder("UserComponentsProviderTest", "");

        cleanComponentsInProject();

    }

    @After
    public void clean() throws Exception {
        if (workFolder != null) {
            FilesUtils.deleteFolder(workFolder, true);
        }
        cleanComponentsInProject();
    }

    private void cleanComponentsInProject() throws Exception {
        final Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final IProject project = ResourceUtils.getProject(currentProject);
        final IFolder projectComponentsFolder = project.getFolder(ERepositoryObjectType
                .getFolderName(ERepositoryObjectType.COMPONENTS));
        if (projectComponentsFolder.exists()) {
            FilesUtils.deleteFolder(projectComponentsFolder.getLocation().toFile(), false);
        }
        projectComponentsFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
    }

    protected File getTestDataFile(String bundlePath) throws IOException {
        return BundleFileUtil.getBundleFile(this.getClass(), bundlePath);
    }

    private void testEmpty(UserComponentsProviderTestClass provider) throws Exception {
        File installationFolder = provider.getInstallationFolder();
        Assert.assertTrue(installationFolder.exists());
        Assert.assertEquals(0, installationFolder.list().length);

    }

    @Test
    public void test_preComponentsLoad_clean_InstallationFolder() throws Exception {
        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass();

        File installationFolder = provider.getInstallationFolder();
        FilesUtils.deleteFolder(installationFolder, true);
        Assert.assertFalse(installationFolder.exists());

        installationFolder.mkdirs();
        new File(installationFolder, "abc.txt").createNewFile();
        Assert.assertEquals(1, installationFolder.list().length);

        provider.preComponentsLoad();

        testEmpty(provider);
    }

    @Test
    public void test_preComponentsLoad_notExistedUserComponentFolder() throws Exception {

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return new File(workFolder, "ABC");
            }
        };
        provider.preComponentsLoad();

        testEmpty(provider);
    }

    @Test
    public void test_preComponentsLoad_no_OldUserComponent() throws Exception {
        new File(workFolder, "abc.txt").createNewFile();
        new File(workFolder, "xyz").mkdirs();

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        testEmpty(provider);
    }

    @Test
    public void test_preComponentsLoad_contain_oldUserComponent_Folder() throws Exception {
        final String componentName = "tHTMLInput";
        File testDataFile = getTestDataFile(PATH_OLD_COMPONENT);
        Assert.assertTrue(testDataFile.exists());
        FilesUtils.unzip(testDataFile.getAbsolutePath(), workFolder.getAbsolutePath());

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        File installationFolder = provider.getInstallationFolder();
        Assert.assertTrue(installationFolder.exists());
        Assert.assertEquals(1, installationFolder.list().length);

        File compFolder = new File(installationFolder, componentName);
        Assert.assertTrue(compFolder.exists());
        Assert.assertTrue(new File(compFolder, componentName + "_java.xml").exists());
    }

    @Test
    public void test_preComponentsLoad_contain_oldUserComponent_Zip() throws Exception {
        File testDataFile = getTestDataFile(PATH_OLD_COMPONENT);
        Assert.assertTrue(testDataFile.exists());
        FilesUtils.copyFile(testDataFile, new File(workFolder, testDataFile.getName()));

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        File installationFolder = provider.getInstallationFolder();
        Assert.assertTrue(installationFolder.exists());
        Assert.assertEquals(
                "NOTE: don't support the zip format for old component before.  if support, this test will be failure.", 0,
                installationFolder.list().length);
    }

    @Test
    public void test_preComponentsLoad_contain_oldUserComponent_inSubFolder() throws Exception {
        File testDataFile = getTestDataFile(PATH_OLD_COMPONENT);
        Assert.assertTrue(testDataFile.exists());
        FilesUtils.unzip(testDataFile.getAbsolutePath(), new File(workFolder, "abc/xyz").getAbsolutePath());

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        testEmpty(provider);
    }

    @Test
    public void test_preComponentsLoad_contain_newUserComponent_Zip() throws Exception {
        File testDataFile = getTestDataFile(PATH_NEW_COMPONENT);
        Assert.assertTrue(testDataFile.exists());
        FilesUtils.copyFile(testDataFile, new File(workFolder, testDataFile.getName()));

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        testEmpty(provider);
    }

    @Test
    public void test_preComponentsLoad_contain_newUserComponent_Folder() throws Exception {
        File testDataFile = getTestDataFile(PATH_NEW_COMPONENT);
        Assert.assertTrue(testDataFile.exists());
        FilesUtils.unzip(testDataFile.getAbsolutePath(), workFolder.getAbsolutePath());

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        testEmpty(provider);
    }

    @Test
    public void test_preComponentsLoad_oldComponentsInProject_Folder() throws Exception {
        final String componentName = "tHTMLInput";
        File testDataFile = getTestDataFile(PATH_OLD_COMPONENT);
        Assert.assertTrue(testDataFile.exists());

        final Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final IProject project = ResourceUtils.getProject(currentProject);
        final IFolder projectComponentsFolder = project.getFolder(ERepositoryObjectType
                .getFolderName(ERepositoryObjectType.COMPONENTS));
        Assert.assertTrue(projectComponentsFolder.exists());

        FilesUtils.unzip(testDataFile.getAbsolutePath(), projectComponentsFolder.getLocation().toFile().getAbsolutePath());

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        File installationFolder = provider.getInstallationFolder();
        Assert.assertTrue(installationFolder.exists());
        Assert.assertEquals(1, installationFolder.list().length);
        File compFolder = new File(installationFolder, componentName);
        Assert.assertTrue(compFolder.exists());
        Assert.assertTrue(new File(compFolder, componentName + "_java.xml").exists());
    }

    @Test
    public void test_preComponentsLoad_oldComponentsInProject_Zip() throws Exception {
        File testDataFile = getTestDataFile(PATH_OLD_COMPONENT);
        Assert.assertTrue(testDataFile.exists());

        final Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final IProject project = ResourceUtils.getProject(currentProject);
        final IFolder projectComponentsFolder = project.getFolder(ERepositoryObjectType
                .getFolderName(ERepositoryObjectType.COMPONENTS));
        Assert.assertTrue(projectComponentsFolder.exists());

        FilesUtils.copyFile(testDataFile, new File(projectComponentsFolder.getLocation().toFile(), testDataFile.getName()));

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        testEmpty(provider);
    }

    @Test
    public void test_preComponentsLoad_contain_newComponentsInProject() throws Exception {
        File testDataFile = getTestDataFile(PATH_NEW_COMPONENT);
        Assert.assertTrue(testDataFile.exists());

        final Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final IProject project = ResourceUtils.getProject(currentProject);
        final IFolder projectComponentsFolder = project.getFolder(ERepositoryObjectType
                .getFolderName(ERepositoryObjectType.COMPONENTS));
        Assert.assertTrue(projectComponentsFolder.exists());

        final File target = new File(projectComponentsFolder.getLocation().toFile(), testDataFile.getName());
        FilesUtils.copyFile(testDataFile, target);

        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass() {

            protected File getExternalComponentsLocation() {
                return workFolder;
            }
        };
        provider.preComponentsLoad();

        testEmpty(provider);

        FilesUtils.deleteFolder(target.getParentFile(), false);
    }
}
