// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import org.apache.commons.lang.StringUtils;
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
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.components.IComponentsService;
import org.talend.core.model.general.Project;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.components.ui.IComponentPreferenceConstant;
import org.talend.repository.ProjectManager;
import org.talend.utils.files.FileUtils;
import org.talend.utils.io.FilesUtils;
import org.talend.utils.json.JSONArray;
import org.talend.utils.json.JSONObject;

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

        cleanInstalledSetting();
        cleanComponentFactorySetting();
    }

    @AfterClass
    public static void restore() throws IOException {
        // backup old
        UserComponentsProviderTestClass provider = new UserComponentsProviderTestClass();
        File installationFolder = provider.getInstallationFolder();
        if (installationFolder != null && installationFolder.exists()) {
            FilesUtils.copyFolder(backupFolder, installationFolder, true, null, null, true);
        }
        FilesUtils.deleteFolder(backupFolder, true);

        cleanInstalledSetting();
        cleanComponentFactorySetting();
    }

    private static void cleanComponentFactorySetting() {
        // remove all
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IComponentsService.class)) {
            IComponentsService compService = (IComponentsService) GlobalServiceRegister.getDefault().getService(
                    IComponentsService.class);
            IComponentsFactory componentsFactory = compService.getComponentsFactory();
            if (componentsFactory instanceof ComponentsFactory) {
                ((ComponentsFactory) componentsFactory).resetNewComponentsCache();
            }
        }
    }

    File workFolder;

    @Before
    public void setup() throws Exception {
        workFolder = FileUtils.createTmpFolder("UserComponentsProviderTest", "");

        cleanInstalledSetting();

        final Project currentProject = ProjectManager.getInstance().getCurrentProject();
        final IProject project = ResourceUtils.getProject(currentProject);
        final IFolder projectComponentsFolder = project.getFolder(ERepositoryObjectType
                .getFolderName(ERepositoryObjectType.COMPONENTS));
        if (projectComponentsFolder.exists()) {
            FilesUtils.deleteFolder(projectComponentsFolder.getLocation().toFile(), false);
        }
        projectComponentsFolder.refreshLocal(IResource.DEPTH_INFINITE, null);

    }

    @After
    public void clean() {
        if (workFolder != null) {
            FilesUtils.deleteFolder(workFolder, true);
        }
        cleanInstalledSetting();
    }

    private static void cleanInstalledSetting() {
        // set empty first
        CodeGeneratorActivator.getDefault().getPreferenceStore()
                .setValue(IComponentPreferenceConstant.INSTALLED_USER_COMPONENTS, new JSONArray().toString());
    }

    protected File getTestDataFile(String bundlePath) throws IOException {
        return BundleFileUtil.getBundleFile(this.getClass(), bundlePath);
    }

    private void testEmpty(UserComponentsProviderTestClass provider) throws Exception {
        File installationFolder = provider.getInstallationFolder();
        Assert.assertTrue(installationFolder.exists());
        Assert.assertEquals(0, installationFolder.list().length);

        String installedComponentsValues = CodeGeneratorActivator.getDefault().getPreferenceStore()
                .getString(IComponentPreferenceConstant.INSTALLED_USER_COMPONENTS);
        JSONArray newCFComponentsJson = new JSONArray();
        if (StringUtils.isNotEmpty(installedComponentsValues)) {
            newCFComponentsJson = new JSONArray(installedComponentsValues);
        }
        Assert.assertEquals(0, newCFComponentsJson.length());

        final JSONObject needInstalledNewCFComponents = provider.getNeedInstalledNewCFComponents();
        Assert.assertNotNull(needInstalledNewCFComponents);
        Assert.assertEquals(0, needInstalledNewCFComponents.length());
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
    public void test_preComponentsLoad_newComponentsInProject() throws Exception {

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

        File installationFolder = provider.getInstallationFolder();
        Assert.assertTrue(installationFolder.exists());
        Assert.assertEquals(0, installationFolder.list().length);

        String installedComponentsValues = CodeGeneratorActivator.getDefault().getPreferenceStore()
                .getString(IComponentPreferenceConstant.INSTALLED_USER_COMPONENTS);
        JSONArray newCFComponentsJson = new JSONArray();
        if (StringUtils.isNotEmpty(installedComponentsValues)) {
            newCFComponentsJson = new JSONArray(installedComponentsValues);
        }
        Assert.assertEquals(0, newCFComponentsJson.length());

        final JSONObject needInstalledNewCFComponents = provider.getNeedInstalledNewCFComponents();
        Assert.assertNotNull(needInstalledNewCFComponents);
        final String technicalLabel = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel();
        Assert.assertTrue(needInstalledNewCFComponents.has(technicalLabel));
        final JSONArray jsonArray = needInstalledNewCFComponents.getJSONArray(technicalLabel);
        Assert.assertNotNull(jsonArray);
        Assert.assertEquals(1, jsonArray.length());
        final String path = jsonArray.getString(0);
        Assert.assertEquals(target.getAbsolutePath(), path);

        provider.resetNewComponentsCache();
    }
}
