package org.talend.repository.handler;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Project;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.service.ITransformService;
import org.talend.model.bridge.ReponsitoryContextBridge;
import org.talend.repository.ProjectManager;
import org.talend.repository.items.importexport.handlers.ImportExportHandlersManager;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.items.importexport.ui.managers.FileResourcesUnityManager;
import org.talend.repository.items.importexport.ui.managers.ResourcesManagerFactory;
import org.talend.repository.ui.wizards.exportjob.JavaJobScriptsExportWSWizardPage.JobExportType;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.BuildJobManager;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager.ExportChoice;
import org.talend.repository.ui.wizards.exportjob.util.ExportJobUtil;

public class BuildJobHandlerTest {

    private ProcessItem processItem;

    private String destinationPath;

    private Project bridgeProject;

    @Before
    public void setUp() throws Exception {
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
    }

    @Test
    public void testBuildJob() throws Exception {
        Map<ExportChoice, Object> exportChoiceMap = new HashMap<ExportChoice, Object>();
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

        destinationPath = ExportJobUtil.getTmpFolderPath() + "/testBuildJob.zip";
        BuildJobManager.getInstance().buildJob(destinationPath, processItem, "0.1", "Default", exportChoiceMap,
                JobExportType.POJO, new NullProgressMonitor());
        assertTrue(new File(destinationPath).exists());
        ZipFile zip = new ZipFile(destinationPath);

        // if the tdm is load
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ITransformService.class)) {
            ZipEntry tdm = zip.getEntry("testBuildJob/__tdm/");
            assertTrue("build job with tdm failure", tdm != null && tdm.isDirectory());

        }
        ZipEntry tdq = zip.getEntry("testBuildJob/items/reports/");
        assertTrue(tdq != null && tdq.isDirectory());
        zip.close();
    }

    @After
    public void tearDown() throws Exception {
        ReponsitoryContextBridge.setProject(bridgeProject);

        ExportJobUtil.deleteTempFiles();
        File file = new File(destinationPath);
        if (file.exists()) {
            file.delete();
        }
    }
}
