package org.talend.repository.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.designer.core.IDesignerCoreService;
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

    private ProcessItem jobWithTdqItem;

    private ProcessItem jobWithTdmItem;

    private ProcessItem jobWithChildrenItem;

    private ProcessItem jobWithJobletItem;

    private ProcessItem jobWithTestcaseItem;

    private ProcessItem childJobItem;

    private Item jobletItem;

    private List<Item> testItems;

    private List<String> destinationPaths;

    private Project bridgeProject;

    private Map<ExportChoice, Object> exportChoiceMap;

    private IRunProcessService runProcessService;

    private static final String JOB_WITH_TDQ_ID = "_3TtbgD7OEeiHhJsSj16U_A";

    private static final String JOB_WITH_TDM_ID = "_bWyBUAYbEeapTZ0aKwL_YA";

    private static final String JOB_WITH_CHILDREN_ID = "_HGAFAD7OEeiNfpYj4K_XrA";

    private static final String JOB_WITH_JOBLET_ID = "_FKbJID7OEeiNfpYj4K_XrA";

    private static final String JOB_WITH_TESTCASE_ID = "_YmcxoHniEeiA8rKAx4YxMw";

    private static final String JOB_CHILD_ID = "_JJsbED7OEeiNfpYj4K_XrA";

    private static final String JOBLET_ID = "_V92qED7OEeiNfpYj4K_XrA";

    @Before
    public void setUp() throws Exception {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            runProcessService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(IRunProcessService.class);
        }
        assertNotNull(runProcessService);

        // Fix the NPE for org.talend.designer.core.ui.editor.process.Process.createMainParameters(Process.java:401)
        bridgeProject = ReponsitoryContextBridge.getProject();
        ReponsitoryContextBridge.setProject(ProjectManager.getInstance().getCurrentProject().getEmfProject());
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        RepositoryContext rc = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        rc.setUser(ReponsitoryContextBridge.getProject().getAuthor());


        ImportExportHandlersManager importManager = new ImportExportHandlersManager();

        URL testJobURL = FileLocator.find(Platform.getBundle("org.talend.repository.test"),
                new Path("/resources/testBuildJob.zip"), null);
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

        jobWithTdqItem = (ProcessItem) getItemById(JOB_WITH_TDQ_ID);
        jobWithTdmItem = (ProcessItem) getItemById(JOB_WITH_TDM_ID);
        jobWithChildrenItem = (ProcessItem) getItemById(JOB_WITH_CHILDREN_ID);
        jobWithJobletItem = (ProcessItem) getItemById(JOB_WITH_JOBLET_ID);
        jobWithTestcaseItem = (ProcessItem) getItemById(JOB_WITH_TESTCASE_ID);
        childJobItem = (ProcessItem) getItemById(JOB_CHILD_ID);
        jobletItem = getItemById(JOBLET_ID);

        testItems = new ArrayList<>();
        testItems.add(jobWithTdqItem);
        testItems.add(jobWithTdmItem);
        testItems.add(jobWithChildrenItem);
        testItems.add(jobWithJobletItem);
        testItems.add(jobWithTestcaseItem);
        testItems.add(childJobItem);
        testItems.add(jobletItem);

        initExportChoice();

        destinationPaths = new ArrayList<>();
        CommonsPlugin.setMavenOfflineState(false);
    }

    @Test
    public void testBuildJobWithTDQ() throws Exception {
        String destinationPath = getDestinationPath(jobWithTdqItem);
        destinationPaths.add(destinationPath);
        BuildJobManager.getInstance().buildJob(destinationPath, jobWithTdqItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());
        validateBuildResult(jobWithTdqItem, destinationPath);
    }

    @Test
    public void testBuildJobWithTDM() throws Exception {
        String destinationPath = getDestinationPath(jobWithTdmItem);
        destinationPaths.add(destinationPath);
        BuildJobManager.getInstance().buildJob(destinationPath, jobWithTdmItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());
        validateBuildResult(jobWithTdmItem, destinationPath);
    }

    @Test
    public void testBuildJobWithChildren() throws Exception {
        String destinationPath = getDestinationPath(jobWithChildrenItem);
        destinationPaths.add(destinationPath);
        BuildJobManager.getInstance().buildJob(destinationPath, jobWithChildrenItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());
        validateBuildResult(jobWithChildrenItem, destinationPath);
    }

    @Test
    public void testBuildJobWithJoblet() throws Exception {
        String destinationPath = getDestinationPath(jobWithJobletItem);
        destinationPaths.add(destinationPath);
        BuildJobManager.getInstance().buildJob(destinationPath, jobWithJobletItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());
        validateBuildResult(jobWithJobletItem, destinationPath);
    }

    @Test
    public void testBuildJobWithTestcase() throws Exception {
        String destinationPath = getDestinationPath(jobWithTestcaseItem);
        destinationPaths.add(destinationPath);
        BuildJobManager.getInstance().buildJob(destinationPath, jobWithTestcaseItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());
        validateBuildResult(jobWithTestcaseItem, destinationPath);
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

    private Item getItemById(String jobId) throws PersistenceException {
        IRepositoryViewObject obj = ProxyRepositoryFactory.getInstance().getLastVersion(jobId);
        return obj.getProperty().getItem();
    }

    private String getDestinationPath(Item item) {
        return ExportJobUtil.getTmpFolderPath() + "/" + item.getProperty().getLabel() + ".zip";
    }

    private void validateBuildResult(ProcessItem jobItem, String destinationPath) throws Exception {
        assertTrue(new File(destinationPath).exists());
        ZipFile zip = null;
        try {
            zip = new ZipFile(destinationPath);
            // jobInfo
            ZipEntry jobInfoEntry = zip.getEntry("jobInfo.properties");
            assertNotNull("Can't find the jobInfo.properties", jobInfoEntry);
            final InputStream jobInfoStream = zip.getInputStream(jobInfoEntry);

            Property property = jobItem.getProperty();
            String jobName = property.getLabel();
            String jobVersion = property.getVersion();

            Properties jobInfoProp = new Properties();
            jobInfoProp.load(jobInfoStream);
            jobInfoStream.close();

            assertEquals(property.getId(), jobInfoProp.getProperty("jobId"));
            assertEquals(jobName, jobInfoProp.getProperty("job"));
            assertEquals(jobVersion, jobInfoProp.getProperty("jobVersion"));
            assertEquals(jobItem.getProcess().getJobType(), jobInfoProp.getProperty("jobType"));
            assertEquals(jobItem.getProcess().getDefaultContext(), jobInfoProp.getProperty("contextName"));

            final String technicalLabel = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel();
            assertEquals(technicalLabel, jobInfoProp.getProperty("project"));

            ZipEntry libEntry = zip.getEntry("lib");
            assertNotNull("No lib folder", libEntry);
            if (jobItem == jobWithChildrenItem) {
                String dependencyFromParent = "commons-lang-2.5.jar";
                ZipEntry dependencyEntry = zip.getEntry("lib/" + dependencyFromParent);
                assertNotNull("No parent job dependency in lib folder", dependencyEntry);
                String dependencyFromChild = "commons-lang-2.6.jar";
                dependencyEntry = zip.getEntry("lib/" + dependencyFromChild);
                assertNotNull("No child job dependency in lib folder", dependencyEntry);
            }
            if (jobItem == jobWithJobletItem) {
                String dependencyFromJoblet = "commons-beanutils-1.9.4.jar";
                ZipEntry dependencyEntry = zip.getEntry("lib/" + dependencyFromJoblet);
                assertNotNull("No joblet dependency in lib folder", dependencyEntry);
            }
            if (jobItem == jobWithTestcaseItem) {
                String dependencyFromTestcase = "c3p0-0.9.1.2.jar";
                ZipEntry dependencyEntry = zip.getEntry("lib/" + dependencyFromTestcase);
                assertNull("Should not have testcase dependency in lib folder", dependencyEntry);
                String dependencyFromJobAndTestcase = "commons-io-2.4.jar";
                dependencyEntry = zip.getEntry("lib/" + dependencyFromJobAndTestcase);
                assertNotNull("No job dependency in lib folder", dependencyEntry);
            }
            if (runProcessService != null) {
                if (runProcessService.isSelectLog4j2()) {
                    // log4j2
                    ZipEntry log4jXmlEntry = zip.getEntry(jobName + "/log4j2.xml");
                    assertNotNull("No log4j2.xml", log4jXmlEntry);
                } else {
                    // log4j1
                    ZipEntry log4jXmlEntry = zip.getEntry(jobName + "/log4j.xml");
                    assertNotNull("No log4j.xml", log4jXmlEntry);
                }
            }

            // shell, ps1, bat
            ZipEntry batEntry = zip.getEntry(jobName + "/" + jobName + "_run.bat");
            assertNotNull("No bat file", batEntry);

            ZipEntry ps1Entry = zip.getEntry(jobName + "/" + jobName + "_run.ps1");
            assertNotNull("No ps1 file", ps1Entry);

            ZipEntry shEntry = zip.getEntry(jobName + "/" + jobName + "_run.sh");
            assertNotNull("No shell file", shEntry);

            String jobJarName = JavaResourcesHelper.getJobJarName(jobName, jobVersion);
            ZipEntry jarEntry = zip.getEntry(jobName + "/" + jobJarName + ".jar");
            assertNotNull("No job jar file", jarEntry);
            if (jobItem == jobWithChildrenItem) {
                String subJobJarName = JavaResourcesHelper.getJobJarName(childJobItem.getProperty().getLabel(),
                        childJobItem.getProperty().getVersion());
                ZipEntry subjobJarEntry = zip.getEntry(jobName + "/" + subJobJarName + ".jar");
                assertNotNull("No sub job jar file", subjobJarEntry);
            }

            // src
            String jobFolderName = JavaResourcesHelper.getJobFolderName(jobName, jobVersion);
            ZipEntry javaEntry = zip.getEntry(
                    jobName + "/src/main/java/" + technicalLabel.toLowerCase() + "/" + jobFolderName + "/" + jobName + ".java");
            assertNotNull("No job source code file", javaEntry);

            ZipEntry contextEntry = zip.getEntry(jobName + "/src/main/resources/" + technicalLabel.toLowerCase()
                    + "/" + jobFolderName + "/contexts/Default.properties");
            assertNotNull("No context file", contextEntry);

            if (jobItem == jobWithTdqItem) {
                // dq
                ZipEntry tdq = zip.getEntry(jobName + "/items/reports/");
                assertNotNull("Can't find the dq reports items", tdq);
                assertTrue(tdq.isDirectory());
            }

            if (jobItem == jobWithTdmItem) {
                // if the tdm is load
                boolean isTDM = false;
                IDesignerCoreService coreService = CorePlugin.getDefault().getDesignerCoreService();
                if (coreService != null) {
                    IProcess process = coreService.getProcessFromProcessItem(jobItem);
                    for (INode node : process.getGeneratingNodes()) {
                        IElementParameter param = node.getElementParameter("COMPONENT_NAME");
                        if ("tHMap".equals(param.getValue())) {
                            isTDM = true;
                            break;
                        }
                    }
                }
                assertTrue("Not TDM item", isTDM);

                ZipEntry tdmSettingEntry = zip.getEntry(
                        jobName + "/items/" + technicalLabel.toLowerCase() + "/.settings/com.oaklandsw.base.projectProps");
                assertNotNull("Can't export tdm rightly", tdmSettingEntry);
                // the __tdm has been moved into job jar. so need test it in jar.
                // testbuildWithXXX_0_1.jar!/__tdm/...
                JarInputStream jarStream = null;
                try {
                    jarStream = new JarInputStream(zip.getInputStream(jarEntry));
                    boolean found = false;
                    JarEntry entry;
                    while ((entry = jarStream.getNextJarEntry()) != null) {
                        if (entry.getName().equals("__tdm/") && entry.isDirectory()) {
                            found = true;
                            break;
                        }
                    }
                    assertTrue("Can't find __tdm folder in job jar after build", found);
                } finally {
                    if (jarStream != null) {
                        jarStream.close();
                    }
                }
            }
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        ReponsitoryContextBridge.setProject(bridgeProject);
        Context ctx = CoreRuntimePlugin.getInstance().getContext();
        RepositoryContext rc = (RepositoryContext) ctx.getProperty(Context.REPOSITORY_CONTEXT_KEY);
        rc.setUser(ReponsitoryContextBridge.getProject().getAuthor());

        if (!testItems.isEmpty()) {
            for (Item item : testItems) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                IRepositoryViewObject repObj = factory.getLastVersion(item.getProperty().getId());
                if (repObj != null) {
                    factory.deleteObjectPhysical(repObj);
                }
            }
            testItems.clear();
        }

        ExportJobUtil.deleteTempFiles();

        if (!destinationPaths.isEmpty()) {
            for (String destinationPath : destinationPaths) {
                File file = new File(destinationPath);
                if (file.exists()) {
                    file.delete();
                }
            }
            destinationPaths.clear();
        }
        CommonsPlugin.setMavenOfflineState(true);
    }

}
