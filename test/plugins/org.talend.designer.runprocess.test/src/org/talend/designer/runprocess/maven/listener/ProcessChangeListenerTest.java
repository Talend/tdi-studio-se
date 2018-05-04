// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.runprocess.maven.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.AggregatorPomsHelper;
import org.talend.designer.runprocess.java.TalendJavaProjectManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.documentation.ERepositoryActionName;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProcessChangeListenerTest {

    private ProxyRepositoryFactory factory;

    private List<Property> testJobs;

    private String projectTechName;

    @Before
    public void setUp() throws Exception {
        factory = ProxyRepositoryFactory.getInstance();
        testJobs = new ArrayList<>();
        projectTechName = ProjectManager.getInstance().getCurrentProject().getTechnicalLabel();
    }

    @Test
    public void testPropertiesChange_Label() throws Exception {
        String id = factory.getNextId();
        String oldJobName = "testPropertiesChangeOldNameJob";
        Property oldProperty1 = createJobProperty(id, oldJobName, "0.1", true);
        TalendJavaProjectManager.generatePom(oldProperty1.getItem());
        Property oldProperty2 = createJobProperty(id, oldJobName, "0.2", true);
        ITalendProcessJavaProject jobProject = TalendJavaProjectManager.getTalendJobJavaProject(oldProperty2, true);
        TalendJavaProjectManager.generatePom(oldProperty2.getItem());

        IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        IFolder processFolder = project.getFolder("process");
        for (IResource resource : processFolder.members()) {
            if (resource instanceof IFile && resource.getName().startsWith(oldJobName)) {
                resource.delete(true, null);
            }
        }

        String newJobName = "testPropertiesChangeNewNameJob";
        Property newProperty1 = createJobProperty(id, newJobName, "0.1", true);
        Property newProperty2 = createJobProperty(id, newJobName, "0.2", true);

        factory.fireRepositoryPropertyChange(ERepositoryActionName.PROPERTIES_CHANGE.getName(),
                new String[] { oldJobName, "0.2" }, newProperty2);

        assertFalse(getItemPomFile(oldProperty1).exists());
        assertFalse(getItemPomFile(oldProperty2).exists());
        assertFalse(jobProject.getProject().exists());

        assertTrue(getItemPomFile(newProperty1).exists());
        assertTrue(getItemPomFile(newProperty2).exists());

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toRemove.add("jobs/process/testPropertiesChangeOldNameJob_0.1".toLowerCase());
        toRemove.add("jobs/process/testPropertiesChangeOldNameJob_0.2".toLowerCase());
        toAdd.add("jobs/process/testPropertiesChangeNewNameJob_0.1".toLowerCase());
        toAdd.add("jobs/process/testPropertiesChangeNewNameJob_0.2".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    @Test
    public void testPropertiesChange_Version() throws Exception {
        String jobName = "testPropertiesVersionChangeJob";
        Property property = createJobProperty(jobName, "0.3", true);
        IFile pomFile = getItemPomFile(property);
        assertFalse(pomFile.exists());
        ProxyRepositoryFactory.getInstance().fireRepositoryPropertyChange(ERepositoryActionName.PROPERTIES_CHANGE.getName(),
                new String[] { jobName, "0.2" }, property);
        assertTrue(pomFile.exists());

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toAdd.add("jobs/process/testPropertiesVersionChangeJob_0.3".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    @Test
    public void testMove() throws Exception {
        String id = factory.getNextId();
        String oldJobName = "testMoveJob";
        Property oldProperty1 = createJobProperty(id, oldJobName, "0.1", true);
        TalendJavaProjectManager.generatePom(oldProperty1.getItem());
        Property oldProperty2 = createJobProperty(id, oldJobName, "0.2", true);
        ITalendProcessJavaProject jobProject = TalendJavaProjectManager.getTalendJobJavaProject(oldProperty2, true);
        TalendJavaProjectManager.generatePom(oldProperty2.getItem());
        Map<IRepositoryViewObject, IPath> sourcePathMap = new HashMap<>();
        IRepositoryViewObject objToMove = factory.getLastVersion(id);
        IPath targetPath = new Path("after_move");
        IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        IFolder processFolder = project.getFolder("process");
        processFolder.getFolder(targetPath).create(true, true, null);
        sourcePathMap.put(objToMove, new Path(""));

        factory.moveObjectMulti(new IRepositoryViewObject[] { objToMove }, targetPath, sourcePathMap);

        IFolder pomsProcessFolder = new AggregatorPomsHelper().getProcessFolder(ERepositoryObjectType.PROCESS);
        assertFalse(pomsProcessFolder.getFolder(AggregatorPomsHelper.getJobProjectFolderName(oldProperty1)).exists());
        assertFalse(pomsProcessFolder.getFolder(AggregatorPomsHelper.getJobProjectFolderName(oldProperty2)).exists());
        assertFalse(jobProject.getProject().exists());

        List<IRepositoryViewObject> newObjects = factory.getAllVersion(id);
        assertTrue(newObjects.size() == 2);
        for (IRepositoryViewObject obj : newObjects) {
            Property property = obj.getProperty();
            IFile pomFile = getItemPomFile(property);
            assertTrue(getItemPomFile(property).exists());
            String jobProjectFolderName = AggregatorPomsHelper.getJobProjectFolderName(property);
            assertEquals("/" + projectTechName + "/poms/jobs/process/after_move/" + jobProjectFolderName + "/pom.xml",
                    pomFile.getFullPath().toPortableString());
        }

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toRemove.add("jobs/process/testMoveJob_0.1".toLowerCase());
        toRemove.add("jobs/process/testMoveJob_0.2".toLowerCase());
        toAdd.add("jobs/process/after_move/testMoveJob_0.1".toLowerCase());
        toAdd.add("jobs/process/after_move/testMoveJob_0.2".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    @Test
    public void testDeleteForever() throws Exception {
        String id = factory.getNextId();
        String jobName = "testDeleteJob";
        Property property1 = createJobProperty(id, jobName, "0.1", true);
        TalendJavaProjectManager.generatePom(property1.getItem());
        Property property2 = createJobProperty(id, jobName, "0.2", true);
        ITalendProcessJavaProject jobProject = TalendJavaProjectManager.getTalendJobJavaProject(property2, true);
        TalendJavaProjectManager.generatePom(property2.getItem());

        IRepositoryViewObject objToDelete = factory.getLastVersion(id);

        factory.deleteObjectPhysical(objToDelete);

        assertFalse(getItemPomFile(property1).exists());
        assertFalse(getItemPomFile(property2).exists());
        assertFalse(jobProject.getProject().exists());

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toRemove.add("jobs/process/testDeleteJob_0.1".toLowerCase());
        toRemove.add("jobs/process/testDeleteJob_0.2".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    @Test
    public void testFolderRename() throws Exception {
        String id = factory.getNextId();
        String jobName = "testFolderRenameJob";
        IPath oldPath = new Path("before_folder_rename");
        Property property1 = createJobProperty(id, jobName, "0.1", oldPath, true);
        TalendJavaProjectManager.generatePom(property1.getItem());
        Property property2 = createJobProperty(id, jobName, "0.2", oldPath, true);
        ITalendProcessJavaProject jobProject = TalendJavaProjectManager.getTalendJobJavaProject(property2, true);
        TalendJavaProjectManager.generatePom(property2.getItem());

        IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        IFolder processFolder = project.getFolder("process");

        factory.renameFolder(ERepositoryObjectType.PROCESS, oldPath, "after_folder_rename");

        assertFalse(processFolder.getFolder(oldPath).exists());
        assertFalse(jobProject.getProject().exists());

        List<IRepositoryViewObject> newObjects = factory.getAllVersion(id);
        assertTrue(newObjects.size() == 2);
        for (IRepositoryViewObject obj : newObjects) {
            Property property = obj.getProperty();
            IFile pomFile = getItemPomFile(property);
            assertTrue(getItemPomFile(property).exists());
            String jobProjectFolderName = AggregatorPomsHelper.getJobProjectFolderName(property);
            assertEquals(
                    "/" + projectTechName + "/poms/jobs/process/after_folder_rename/" + jobProjectFolderName + "/pom.xml",
                    pomFile.getFullPath().toPortableString());
        }

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toRemove.add("jobs/process/before_folder_rename/testFolderRenameJob_0.1".toLowerCase());
        toRemove.add("jobs/process/before_folder_rename/testFolderRenameJob_0.2".toLowerCase());
        toAdd.add("jobs/process/after_folder_rename/testFolderRenameJob_0.1".toLowerCase());
        toAdd.add("jobs/process/after_folder_rename/testFolderRenameJob_0.2".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    @Test
    public void testFolderMove() throws Exception {
        String id = factory.getNextId();
        String jobName = "testFolderMoveJob";
        IPath sourcePath = new Path("before_folder_move");
        Property property1 = createJobProperty(id, jobName, "0.1", sourcePath, true);
        TalendJavaProjectManager.generatePom(property1.getItem());
        Property property2 = createJobProperty(id, jobName, "0.2", sourcePath, true);
        ITalendProcessJavaProject jobProject = TalendJavaProjectManager.getTalendJobJavaProject(property2, true);
        TalendJavaProjectManager.generatePom(property2.getItem());

        IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        IFolder processFolder = project.getFolder("process");
        IPath targetPath = new Path("after_folder_move");
        processFolder.getFolder(targetPath).create(true, true, null);

        factory.moveFolder(ERepositoryObjectType.PROCESS, sourcePath, targetPath);

        IFolder pomsProcessFolder = new AggregatorPomsHelper().getProcessFolder(ERepositoryObjectType.PROCESS);
        assertFalse(pomsProcessFolder.getFolder(sourcePath).exists());
        assertFalse(jobProject.getProject().exists());

        assertTrue(processFolder.getFolder(targetPath).exists());
        List<IRepositoryViewObject> newObjects = factory.getAllVersion(id);
        assertTrue(newObjects.size() == 2);
        for (IRepositoryViewObject obj : newObjects) {
            Property property = obj.getProperty();
            IFile pomFile = getItemPomFile(property);
            assertTrue(getItemPomFile(property).exists());
            String jobProjectFolderName = AggregatorPomsHelper.getJobProjectFolderName(property);
            assertEquals("/" + projectTechName + "/poms/jobs/process/after_folder_move/before_folder_move/" + jobProjectFolderName
                    + "/pom.xml",
                    pomFile.getFullPath().toPortableString());
        }

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toRemove.add("jobs/process/before_folder_move/testFolderMoveJob_0.1".toLowerCase());
        toRemove.add("jobs/process/before_folder_move/testFolderMoveJob_0.2".toLowerCase());
        toAdd.add("jobs/process/after_folder_move/before_folder_move/testFolderMoveJob_0.1".toLowerCase());
        toAdd.add("jobs/process/after_folder_move/before_folder_move/testFolderMoveJob_0.2".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    @Test
    public void testFolderDelete() throws Exception {
        String id = factory.getNextId();
        String jobName = "testFolderDeleteJob";
        IPath path = new Path("to_delete");
        Property property1 = createJobProperty(id, jobName, "0.1", path, true);
        TalendJavaProjectManager.generatePom(property1.getItem());
        Property property2 = createJobProperty(id, jobName, "0.2", path, true);
        ITalendProcessJavaProject jobProject = TalendJavaProjectManager.getTalendJobJavaProject(property2, true);
        TalendJavaProjectManager.generatePom(property2.getItem());

        // simulate action of delete folder, should delete all items under folder first.
        IRepositoryViewObject objToDelete = factory.getLastVersion(id);
        factory.deleteObjectPhysical(objToDelete);
        factory.deleteFolder(ERepositoryObjectType.PROCESS, path);

        assertFalse(getItemPomFile(property1).exists());
        assertFalse(getItemPomFile(property2).exists());
        assertFalse(jobProject.getProject().exists());

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toRemove.add("jobs/process/to_delete/testFolderDeleteJob_0.1".toLowerCase());
        toRemove.add("jobs/process/to_delete/testFolderDeleteJob_0.2".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    @Test
    public void testCreateAndSave() throws Exception {
        String id = factory.getNextId();
        String jobName = "testCreateAndSaveJob";
        Property property = createJobProperty(id, jobName, "0.1", true);
        assertFalse(getItemPomFile(property).exists());

        factory.save(property.getItem());
        assertTrue(getItemPomFile(property).exists());
        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toAdd.add("jobs/process/testCreateAndSaveJob_0.1".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    @Test
    public void testImport() throws Exception {
        String id = factory.getNextId();
        String jobName = "testImportJob";
        Property property1 = createJobProperty(id, jobName, "0.1", true);
        Property property2 = createJobProperty(id, jobName, "0.2", true);

        Set<Item> importedItems = new HashSet<>();
        importedItems.add(property1.getItem());
        importedItems.add(property2.getItem());

        factory.fireRepositoryPropertyChange(ERepositoryActionName.IMPORT.getName(), null, importedItems);

        assertTrue(getItemPomFile(property1).exists());
        assertTrue(getItemPomFile(property2).exists());

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();
        toAdd.add("jobs/process/testImportJob_0.1".toLowerCase());
        toAdd.add("jobs/process/testImportJob_0.2".toLowerCase());
        checkRootPomModules(toRemove, toAdd);
    }

    private Property createJobProperty(String label, String version, boolean create) throws Exception {
        String id = ProxyRepositoryFactory.getInstance().getNextId();
        return createJobProperty(id, label, version, new Path(""), create);
    }

    private Property createJobProperty(String id, String label, String version, boolean create) throws Exception {
        return createJobProperty(id, label, version, new Path(""), create);
    }

    private Property createJobProperty(String id, String label, String version, IPath path, boolean create) throws Exception {
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setId(id);
        property.setLabel(label);
        property.setVersion(version);

        ProcessItem item = PropertiesFactory.eINSTANCE.createProcessItem();
        item.setProperty(property);

        ProcessType process = TalendFileFactory.eINSTANCE.createProcessType();
        ParametersType parameterType = TalendFileFactory.eINSTANCE.createParametersType();
        process.setParameters(parameterType);
        item.setProcess(process);

        if (create) {
            ProxyRepositoryFactory.getInstance().create(item, path);
            testJobs.add(property);
        }
        return property;
    }

    private void checkRootPomModules(List<String> toRemove, List<String> toAdd) throws Exception {
        File projectPomFile = new AggregatorPomsHelper().getProjectRootPom().getLocation().toFile();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(projectPomFile);
        Node modulesNode = getElement(document.getDocumentElement(), "modules", 1);
        NodeList modules = modulesNode.getChildNodes();
        List<String> existModules = new ArrayList<>();
        for (int i = 0; i < modules.getLength(); i++) {
            Node module = modules.item(i);
            if (module.getNodeType() == Node.ELEMENT_NODE) {
                String modulePath = module.getTextContent();
                if (modulePath.startsWith("jobs/")) {
                    existModules.add(modulePath);
                }
            }
        }
        for (String toRemoveModule : toRemove) {
            assertFalse("to remove module[" + toRemoveModule + "] still exist", existModules.contains(toRemoveModule));
        }
        for (String toAddModule : toAdd) {
            assertTrue("to add module[" + toAddModule + "] not exist", toAdd.contains(toAddModule));
        }
    }

    private Node getElement(Node parent, String elemName, int level) {
        NodeList childrenNodeList = parent.getChildNodes();
        for (int i = 0; i < childrenNodeList.getLength(); i++) {
            Node child = childrenNodeList.item(i);
            if (child != null && child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals(elemName)) {
                    return child;
                }
            }
            if (level > 1) {
                Node element = getElement(child, elemName, --level);
                if (element != null) {
                    return element;
                }
            }
        }
        return null;
    }

    private IFile getItemPomFile(Property property) {
        return AggregatorPomsHelper.getItemPomFolder(property).getFile(TalendMavenConstants.POM_FILE_NAME);
    }

    @After
    public void tearDown() throws Exception {
        // clear all test jobs.
        if (!testJobs.isEmpty()) {
            for (Property property : testJobs) {
                IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
                IRepositoryViewObject repObj = factory.getLastVersion(property.getId());
                if (repObj != null) {
                    factory.deleteObjectPhysical(repObj);
                }
            }
            testJobs.clear();
        }
    }

}
