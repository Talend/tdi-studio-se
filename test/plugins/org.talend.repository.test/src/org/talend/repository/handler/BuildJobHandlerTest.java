package org.talend.repository.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.model.Model;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.MavenModelManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.projectsetting.ProjectPreferenceManager;
import org.talend.core.service.ITransformService;
import org.talend.designer.maven.DesignerMavenPlugin;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.model.bridge.ReponsitoryContextBridge;
import org.talend.repository.ProjectManager;
import org.talend.repository.items.importexport.handlers.ImportExportHandlersManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.FileResourcesUnityManager;
import org.talend.repository.items.importexport.ui.managers.ResourcesManagerFactory;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;

public class BuildJobHandlerTest {

    private ProcessItem processItem;

    private String destinationPath;

    private Project bridgeProject;

    private Map<ExportChoice, Object> exportChoiceMap;

    private IRunProcessService runProcessService;

    private ProjectPreferenceManager projectPreferenceManager;

    @Before
    public void setUp() throws Exception {

        projectPreferenceManager = DesignerMavenPlugin.getPlugin().getProjectPreferenceManager();

        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            runProcessService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }

        // Fix the NPE for org.talend.designer.core.ui.editor.process.Process.createMainParameters(Process.java:401)
        bridgeProject = ReponsitoryContextBridge.getProject();
        ReponsitoryContextBridge.setProject(ProjectManager.getInstance().getCurrentProject().getEmfProject());

        ImportExportHandlersManager importManager = new ImportExportHandlersManager();
        // job with tdm and tdq component.
        URL testJobURL = FileLocator.find(Platform.getBundle("org.talend.repository.test"), new Path(
                "/resources/testBuildJob.zip"), null);
        if (testJobURL != null) {
            testJobURL = FileLocator.toFileURL(testJobURL);
        }
        File srcFile = new File(testJobURL.getFile());
        FileResourcesUnityManager fileUnityManager = ResourcesManagerFactory.getInstance().createFileUnityManager(srcFile);
        ResourcesManager resManager = fileUnityManager.doUnify();
        List<ImportItem> projectRecords = importManager.populateImportingItems(resManager, true, new NullProgressMonitor());
        assertTrue(projectRecords.size() > 0);
        importManager.importItemRecords(new NullProgressMonitor(), resManager, projectRecords, true,
                projectRecords.toArray(new ImportItem[0]), null);

        IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance().getLastVersion("_bWyBUAYbEeapTZ0aKwL_YA");
        Item item = obj.getProperty().getItem();
        assertTrue(item instanceof ProcessItem);
        processItem = (ProcessItem) item;

        initExportChoice();
    }

    @Test
    public void testBuildJob() throws Exception {
        destinationPath = ExportJobUtil.getTmpFolderPath() + "/testBuildJob.zip";
        BuildJobManager.getInstance().buildJob(destinationPath, processItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());
        validateBuildResult();
    }

    @Test
    public void testDefaultVersioningSettings() throws Exception {
        destinationPath = ExportJobUtil.getTmpFolderPath() + "/testBuildJob.zip";
        BuildJobManager.getInstance().buildJob(destinationPath, processItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());
        String projectDefaultVersion = projectPreferenceManager.getPreferenceStore().getDefaultString(
                MavenConstants.PROJECT_VERSION);
        String itemDefaultVersion = VersionUtils.getPublishVersion(processItem.getProperty().getVersion());
        // check project pom.
        validateVersioningResult(TalendMavenConstants.POM_FILE_NAME, projectDefaultVersion, null);
        // check routine pom.
        validateVersioningResult("pom_routines.xml", projectDefaultVersion, projectDefaultVersion);
        // check job pom.
        validateVersioningResult("pom_testBuildJob_0.1.xml", itemDefaultVersion, projectDefaultVersion);
    }

    @Test
    public void testCustomVersioningSettings() throws Exception {
        EMap additionalProperties = processItem.getProperty().getAdditionalProperties();
        String customItemVersion = "8.8.0";
        additionalProperties.put(MavenConstants.NAME_USER_VERSION, customItemVersion);

        String customProjectVersion = "8.7.0";
        projectPreferenceManager.setValue(MavenConstants.PROJECT_VERSION, customProjectVersion);

        destinationPath = ExportJobUtil.getTmpFolderPath() + "/testBuildJob.zip";
        BuildJobManager.getInstance().buildJob(destinationPath, processItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());

        // check project pom.
        validateVersioningResult(TalendMavenConstants.POM_FILE_NAME, customProjectVersion, null);
        // check routine pom.
        validateVersioningResult("pom_routines.xml", customProjectVersion, customProjectVersion);
        // check job pom.
        validateVersioningResult("pom_testBuildJob_0.1.xml", customItemVersion, customProjectVersion);
    }

    @Test
    public void testCustomVersioningSettingsWithSnapshot() throws Exception {
        EMap additionalProperties = processItem.getProperty().getAdditionalProperties();
        additionalProperties.put(MavenConstants.NAME_USER_VERSION, "8.8.0");
        additionalProperties.put(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT, "true");

        projectPreferenceManager.setValue(MavenConstants.PROJECT_VERSION, "8.7.0");
        projectPreferenceManager.setValue(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT, true);

        destinationPath = ExportJobUtil.getTmpFolderPath() + "/testBuildJob.zip";
        BuildJobManager.getInstance().buildJob(destinationPath, processItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());

        String customProjectVersion = "8.7.0-SNAPSHOT";
        String customItemVersion = "8.8.0-SNAPSHOT";
        // check project pom.
        validateVersioningResult(TalendMavenConstants.POM_FILE_NAME, customProjectVersion, null);
        // check routine pom.
        validateVersioningResult("pom_routines.xml", customProjectVersion, customProjectVersion);
        // check job pom.
        validateVersioningResult("pom_testBuildJob_0.1.xml", customItemVersion, customProjectVersion);
    }

    private Map<ExportChoice, Object> initExportChoice() {
        exportChoiceMap = new HashMap<ExportChoice, Object>();
        exportChoiceMap.put(ExportChoice.needLauncher, true);
        exportChoiceMap.put(ExportChoice.needSystemRoutine, true);
        exportChoiceMap.put(ExportChoice.needUserRoutine, true);
        exportChoiceMap.put(ExportChoice.needTalendLibraries, true);
        exportChoiceMap.put(ExportChoice.needJobItem, false);
        exportChoiceMap.put(ExportChoice.needJobScript, true);
        exportChoiceMap.put(ExportChoice.needSourceCode, true);
        exportChoiceMap.put(ExportChoice.includeLibs, true);
        exportChoiceMap.put(ExportChoice.includeTestSource, false);
        exportChoiceMap.put(ExportChoice.executeTests, false);
        exportChoiceMap.put(ExportChoice.binaries, true);
        exportChoiceMap.put(ExportChoice.needContext, true);
        exportChoiceMap.put(ExportChoice.contextName, "Default");
        exportChoiceMap.put(ExportChoice.applyToChildren, false);
        exportChoiceMap.put(ExportChoice.needLog4jLevel, false);
        exportChoiceMap.put(ExportChoice.log4jLevel, null);
        exportChoiceMap.put(ExportChoice.needDependencies, true);
        exportChoiceMap.put(ExportChoice.needParameterValues, false);
        return exportChoiceMap;
    }

    private void validateBuildResult() throws IOException {
        assertTrue(new File(destinationPath).exists());
        ZipFile zip = null;
        try {
            zip = new ZipFile(destinationPath);

            // jobInfo
            ZipEntry jobInfoEntry = zip.getEntry("jobInfo.properties");
            assertNotNull("Can't find the jobInfo.properties", jobInfoEntry);
            final InputStream jobInfoStream = zip.getInputStream(jobInfoEntry);
            Properties jobInfoProp = new Properties();
            jobInfoProp.load(jobInfoStream);
            assertEquals("testBuildJob", jobInfoProp.getProperty("job"));
            assertEquals("0.1", jobInfoProp.getProperty("jobVersion"));
            assertEquals("Default", jobInfoProp.getProperty("contextName"));
            assertEquals("_bWyBUAYbEeapTZ0aKwL_YA", jobInfoProp.getProperty("jobId"));
            assertEquals("Standard", jobInfoProp.getProperty("jobType"));
            final String technicalLabel = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel();
            assertEquals(technicalLabel, jobInfoProp.getProperty("project"));

            ZipEntry libEntry = zip.getEntry("lib");
            assertNotNull("No lib folder", libEntry);

            // log4j
            ZipEntry log4jXmlEntry = zip.getEntry("testBuildJob/log4j.xml");
            assertNotNull("No log4j.xml", log4jXmlEntry);

            // shell+bat
            ZipEntry batEntry = zip.getEntry("testBuildJob/testBuildJob_run.bat");
            assertNotNull("No bat file", batEntry);

            ZipEntry shEntry = zip.getEntry("testBuildJob/testBuildJob_run.sh");
            assertNotNull("No shell file", shEntry);

            ZipEntry jarEntry = zip.getEntry("testBuildJob/testbuildjob_0_1.jar");
            assertNotNull("No shell file", jarEntry);

            // src
            ZipEntry javaEntry = zip.getEntry("testBuildJob/src/main/java/" + technicalLabel.toLowerCase()
                    + "/testbuildjob_0_1/testBuildJob.java");
            assertNotNull("No job source code file", javaEntry);

            ZipEntry routinesEntry = zip.getEntry("testBuildJob/src/main/java/routines/");
            assertNotNull("No routines source code files", routinesEntry);
            assertTrue(routinesEntry.isDirectory());

            ZipEntry contextEntry = zip.getEntry("testBuildJob/src/main/resources/" + technicalLabel.toLowerCase()
                    + "/testbuildjob_0_1/contexts/Default.properties");
            assertNotNull("No context file", contextEntry);

            // dq
            ZipEntry tdq = zip.getEntry("testBuildJob/items/reports/");
            assertNotNull("Can't find the dq reports items", tdq);
            assertTrue(tdq.isDirectory());

            // if the tdm is load
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITransformService.class)) {
                ITransformService tdmService = (ITransformService) GlobalServiceRegister.getDefault().getService(
                        ITransformService.class);
                if (tdmService.isTransformItem(processItem)) {
                    ZipEntry tdmSettingEntry = zip.getEntry("testBuildJob/items/" + technicalLabel.toLowerCase()
                            + "/.settings/com.oaklandsw.base.projectProps");
                    assertNotNull("Can't export tdm rightly", tdmSettingEntry);

                    /*
                     * the __tdm has been moved into job jar. so need test it in jar.
                     */
                    // ZipEntry tdmEntry = zip.getEntry("testBuildJob/__tdm/");
                    // assertNotNull("Can't export tdm rightly", tdmEntry);
                    // assertTrue("build job with tdm failure", tdmEntry.isDirectory());

                    // testbuildjob_0_1.jar!/__tdm/TEST_NOLOGIN.zip
                    final JarInputStream jarStream = new JarInputStream(zip.getInputStream(jarEntry));
                    boolean found = false;
                    JarEntry entry;
                    while ((entry = jarStream.getNextJarEntry()) != null) {
                        final String name = entry.getName();
                        if (name.contains("__tdm") && name.endsWith(technicalLabel + ".zip")) {
                            found = true;
                        }
                    }
                    jarStream.close();
                    assertTrue("Can't find the __tdm in job jar after build", found);
                }
            }

        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    private void validateVersioningResult(String pomFileName, String version, String parentVersion) throws CoreException {
        assertNotNull(runProcessService);
        MavenModelManager manager = MavenPlugin.getMavenModelManager();
        IFile pomFile = runProcessService.getTalendProcessJavaProject().getProject().getFile(pomFileName);
        Model model = manager.readMavenModel(pomFile);
        assertEquals(version, model.getVersion());
        if (parentVersion != null) {
            assertEquals(parentVersion, model.getParent().getVersion());
        }
    }

    @After
    public void tearDown() throws Exception {
        ReponsitoryContextBridge.setProject(bridgeProject);

        projectPreferenceManager.getPreferenceStore().setToDefault(MavenConstants.PROJECT_VERSION);
        projectPreferenceManager.getPreferenceStore().setToDefault(MavenConstants.NAME_PUBLISH_AS_SNAPSHOT);

        if (processItem != null) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            IRepositoryViewObject repObj = factory.getLastVersion(processItem.getProperty().getId());
            if (repObj != null) {
                factory.deleteObjectPhysical(repObj);
            }
        }

        ExportJobUtil.deleteTempFiles();
        if (destinationPath != null) {
            File file = new File(destinationPath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
