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
package org.talend.designer.core.ui.editor.properties.controllers;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.axis2.wsdl.WSDL2Java;
import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.general.Project;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.routines.RoutinesUtil;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.utils.emf.component.ComponentFactory;
import org.talend.designer.core.model.utils.emf.component.IMPORTType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * DOC xtan class global comment. Detailled comment
 */
public class WSDL2JAVAController extends AbstractElementPropertySectionController {

    /**
     *
     */
    private static final String TEMPFOLDER = "wsdl2java"; //$NON-NLS-1$

    private static final String PACK = "routines"; //$NON-NLS-1$

    /**
     * DOC xtan WSDL2JAVAController constructor comment.
     *
     * @param dp
     */
    public WSDL2JAVAController(IDynamicProperty dp) {
        super(dp);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#createControl
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter, int, int, int,
     * org.eclipse.swt.widgets.Control)
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Button btnEdit;

        btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$

        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        FormData data;
        // btnEdit.setData(NAME, EXTERNAL);
        // btnEdit.setData(PARAMETER_NAME, param.getName());
        // btnEdit.setEnabled(!param.isReadOnly());
        btnEdit.addSelectionListener(listenerSelection);
        if (elem instanceof Node) {
            btnEdit.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / nbInRow), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // **************************
        data = new FormData();
        int currentLabelWidth = STANDARD_LABEL_WIDTH;
        GC gc = new GC(labelLabel);
        Point labelSize = gc.stringExtent(param.getDisplayName());
        gc.dispose();

        if ((labelSize.x + ITabbedPropertyConstants.HSPACE) > currentLabelWidth) {
            currentLabelWidth = labelSize.x + ITabbedPropertyConstants.HSPACE;
        }

        if (numInRow == 1) {
            if (lastControl != null) {
                data.left = new FormAttachment(lastControl, currentLabelWidth);
                data.right = new FormAttachment(lastControl, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
                data.right = new FormAttachment(0, currentLabelWidth + STANDARD_BUTTON_WIDTH);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
            data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        btnEdit.setLayoutData(data);
        // **************************
        hashCurControls.put(param.getName(), btnEdit);

        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return btnEdit;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#estimateRowSize
     * (org.eclipse.swt.widgets.Composite, org.talend.core.model.process.IElementParameter)
     */
    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnEdit.dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController#refresh
     * (org.talend.core.model.process.IElementParameter, boolean)
     */
    @Override
    public void refresh(IElementParameter param, boolean check) {

    }

    /*
     * (non-Javadoc)
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }

        @Override
        public void widgetSelected(SelectionEvent e) {

            generateJavaFile();

            refreshRepositoryView();
        }

    };

    /*
     * (non-Javadoc)
     *
     * @see
     * org.talend.designer.core.ui.editor.properties2.editors.AbstractElementPropertySectionController#createCommand()
     */
    private void generateJavaFile() {

        final IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("", this) {//$NON-NLS-1$

            @Override
            protected void run() throws PersistenceException {
                Node node = (Node) elem;

                IProcess process = node.getProcess();
                String jobName = process.getName();
                String nodeName = node.getUniqueName();

                String wsdlfile = (String) node.getPropertyValue("ENDPOINT"); //$NON-NLS-1$
                wsdlfile = wsdlfile.substring(1, wsdlfile.length() - 1);
                if (wsdlfile.equals("")) {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openError(Display.getDefault().getActiveShell(),
                                    org.talend.designer.core.i18n.Messages.getString("WSDL2JAVAController.TOS"),
                                    org.talend.designer.core.i18n.Messages.getString("WSDL2JAVAController.WSDLEquals"));
                        }

                    });

                    return;
                }
                File dir = new File(getTmpFolder());

                final TalendWSDL2Java java2WSDL = new TalendWSDL2Java();

                boolean hasError = java2WSDL.generateWSDL(new String[] { "-o" + dir, "-p" + PACK, wsdlfile }); //$NON-NLS-1$ //$NON-NLS-2$

                // give some info about the generate stub.jar result to GUI.
                final String tempWsdlfile = wsdlfile;
                if (hasError) {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openError(
                                    Display.getDefault().getActiveShell(),
                                    org.talend.designer.core.i18n.Messages.getString("WSDL2JAVAController.TOS"), org.talend.designer.core.i18n.Messages.getString("WSDL2JAVAController.generateFileFailed" //$NON-NLS-1$ //$NON-NLS-2$
                                                    , java2WSDL.getException().getClass().getCanonicalName(), java2WSDL
                                                            .getException().getMessage()));
                        }

                    });

                } else {
                    Display.getDefault().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            MessageDialog.openInformation(Display.getDefault().getActiveShell(),
                                    org.talend.designer.core.i18n.Messages.getString("WSDL2JAVAController.TOS"), //$NON-NLS-1$
                                    org.talend.designer.core.i18n.Messages.getString(
                                            "WSDL2JAVAController.generateFileFailedFromWSDL", tempWsdlfile)); //$NON-NLS-1$
                        }

                    });

                }

                IPath path = new Path(jobName + "/" + nodeName); //$NON-NLS-1$

                String[] filter = new String[] { "java" }; //$NON-NLS-1$
                Collection listFiles = FileUtils.listFiles(dir, filter, true);
                Iterator iterator = listFiles.iterator();
                String name = "";
                for (int i = 0; i < listFiles.size(); i++) {
                    File javaFile = (File) listFiles.toArray()[i];
                    String parentFileName = javaFile.getParentFile().getName();
                    if (!parentFileName.equals("routines")) {
                        name = parentFileName;
                    }
                }
                List<RoutineItem> returnItemList = new ArrayList<RoutineItem>();

                while (iterator.hasNext()) {
                    File javaFile = (File) iterator.next();
                    String fileName = javaFile.getName();
                    String label = fileName.substring(0, fileName.indexOf('.'));
                    try {
                        RoutineItem returnItem = createRoutine(path, label, javaFile, name);
                        returnItemList.add(returnItem);
                        syncRoutine(returnItem, true, name);

                        refreshProject();

                    } catch (IllegalArgumentException e) {
                        // nothing need to do for the duplicate label, there don't overwrite it.
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }

                Project currentProject = ProjectManager.getInstance().getCurrentProject();
                IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(
                        IRepositoryService.class);
                IProxyRepositoryFactory factory = service.getProxyRepositoryFactory();
                List<IRepositoryViewObject> all;
                Item item = null;

                try {
                    all = factory.getAll(currentProject, ERepositoryObjectType.PROCESS, true, true);
                    for (IRepositoryViewObject repositoryViewObject : all) {
                        if (repositoryViewObject.getLabel().equals(jobName)) {
                            item = repositoryViewObject.getProperty().getItem();
                        }
                    }
                } catch (PersistenceException ex) {
                    ExceptionHandler.process(ex);
                }

                try {
                    List<RoutinesParameterType> needList = new ArrayList<RoutinesParameterType>();
                    List<RoutinesParameterType> createJobRoutineDependencies = RoutinesUtil.createJobRoutineDependencies(false);
                    for (RoutineItem returnItem : returnItemList) {
                        for (RoutinesParameterType routinesParameterType : createJobRoutineDependencies) {
                            if (routinesParameterType.getId().equals(returnItem.getProperty().getId())) {
                                needList.add(routinesParameterType);
                            }
                        }
                    }
                    if (process instanceof org.talend.designer.core.ui.editor.process.Process) {
                        ((org.talend.designer.core.ui.editor.process.Process) process).addGeneratingRoutines(needList);
                    }
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }

                // try {
                // RoutinesUtil.createJobRoutineDependencies(false);
                // } catch (PersistenceException e) {
                // ExceptionHandler.process(e);
                // }
                FilesUtils.removeFolder(dir, true);
            }
        };
        workUnit.setAvoidUnloadResources(true);
        factory.executeRepositoryWorkUnit(workUnit);
    }

    private void refreshProject() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRunProcessService.class)) {
            IRunProcessService processService = (IRunProcessService) GlobalServiceRegister.getDefault().getService(
                    IRunProcessService.class);
            ITalendProcessJavaProject routineProject = processService.getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);
            try {
                routineProject.buildModules(new NullProgressMonitor(), null, null);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * DOC xtan Comment method "createRoutine".
     *
     * @param path
     */
    private RoutineItem createRoutine(final IPath path, String label, File initFile, String name) {

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        property.setLabel(label);

        final RoutineItem routineItem = PropertiesFactory.eINSTANCE.createRoutineItem();

        routineItem.setProperty(property);

        ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
        InputStream stream = null;
        try {
            stream = initFile.toURL().openStream();
            byte[] innerContent = new byte[stream.available()];
            stream.read(innerContent);

            byteArray.setInnerContent(innerContent);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    //
                }
            }
        }
        String routineContent = new String(byteArray.getInnerContent());
        routineContent = chanageRoutinesPackage(routineContent, name);
        byteArray.setInnerContent(routineContent.getBytes());
        routineItem.setContent(byteArray);
        final IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();
        try {
            property.setId(repositoryFactory.getNextId());
            repositoryFactory.createParentFoldersRecursively(ERepositoryObjectType.getItemType(routineItem), path);
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    try {
                        repositoryFactory.create(routineItem, path);
                    } catch (PersistenceException e) {
                        ExceptionHandler.process(e);
                    }
                }

            });

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
        }
        if (routineItem.eResource() != null) {
            addWsdlNeedLib(routineItem);
        }
        return routineItem;

    }

    // gcui:see bug 9733.
    private void addWsdlNeedLib(RoutineItem routineItem) {
        List<IMPORTType> wsdlNeedImport = new ArrayList<IMPORTType>();
        IMPORTType type1 = ComponentFactory.eINSTANCE.createIMPORTType();
        type1.setMODULE("axis.jar");
        type1.setREQUIRED(true);
        type1.setNAME(routineItem.getProperty().getLabel());
        wsdlNeedImport.add(type1);
        IMPORTType type2 = ComponentFactory.eINSTANCE.createIMPORTType();
        type2.setMODULE("jaxrpc.jar");
        type2.setREQUIRED(true);
        type2.setNAME(routineItem.getProperty().getLabel());
        wsdlNeedImport.add(type2);
        routineItem.getImports().addAll(wsdlNeedImport);

        try {
            CorePlugin.getDefault().getProxyRepositoryFactory().save(routineItem);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        CorePlugin.getDefault().getRunProcessService().updateLibraries(routineItem);
    }

    private String chanageRoutinesPackage(String routineContent, String name) {
        if (!name.equals("")) {
            String oldPackage = JavaUtils.JAVA_ROUTINES_DIRECTORY + "." + name;
            String newPackage = JavaUtils.JAVA_ROUTINES_DIRECTORY;
            routineContent = routineContent.replaceAll(oldPackage.trim(), newPackage.trim());
        }
        return routineContent;
    }

    /**
     * DOC xtan there will be refactor for this method with JavaRoutineSynchronizer.syncRoutine().
     *
     * @param routineItem
     * @param copyToTemp
     * @return
     * @throws SystemException
     */
    private IFile syncRoutine(RoutineItem routineItem, boolean copyToTemp, String name) throws SystemException {
        FileOutputStream fos = null;
        try {
            IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
            ITalendProcessJavaProject talendProcessJavaProject = service.getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);
            if (talendProcessJavaProject == null) {
                return null;
            }
            IFolder srcFolder = talendProcessJavaProject.getSrcFolder();

            IFile file = srcFolder.getFile(JavaUtils.JAVA_ROUTINES_DIRECTORY + '/' + routineItem.getProperty().getLabel()
                    + JavaUtils.JAVA_EXTENSION);

            if (copyToTemp) {
                String routineContent = new String(routineItem.getContent().getInnerContent());
                routineContent = chanageRoutinesPackage(routineContent, name);
                String label = routineItem.getProperty().getLabel();
                if (!label.equals(ITalendSynchronizer.TEMPLATE)) {
                    routineContent = routineContent.replaceAll(ITalendSynchronizer.TEMPLATE, label);
                    File f = file.getLocation().toFile();
                    fos = new FileOutputStream(f);
                    fos.write(routineContent.getBytes());
                    fos.close();
                }
            }
            if (!file.exists()) {
                file.refreshLocal(1, null);
            }
            return file;
        } catch (CoreException e) {
            throw new SystemException(e);
        } catch (IOException e) {
            throw new SystemException(e);
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
                // ignore me even if i'm null
                ExceptionHandler.process(e);
            }
        }
    }

    /**
     * DOC xtan WSDL2JAVAController class global comment. Detailled comment
     */
    static class TalendWSDL2Java extends WSDL2Java {

        private boolean hasError = false;

        private Throwable exception = null;

        /**
         * DOC xtan TalendWSDL2Java constructor comment.
         */
        public TalendWSDL2Java() {
            super();
        }

        public boolean generateWSDL(String[] args) {
            run(args);
            return hasError;
        }

        protected void run(String[] args) {

            try {
                WSDL2Java.main(args);
            } catch (Throwable t) {
                hasError = true;
                exception = t;
                ExceptionHandler.process(t);
                // System.exit(1);
            }
        } // run

        /**
         * Getter for exception.
         *
         * @return the exception
         */
        public Throwable getException() {
            return this.exception;
        }

    }

    /**
     * DOC xtan Comment method "refreshRepositoryView".
     */
    private static void refreshRepositoryView() {
        IRepositoryView viewPart = RepositoryManagerHelper.findRepositoryView();
        if (viewPart != null) {
            viewPart.refreshView();
        }
    }

    protected String getTmpFolder() {
        String tmpFold = getTmpFolderPath();
        File f = new File(tmpFold);
        if (!f.exists()) {
            f.mkdir();
        }
        return tmpFold;
    }

    private String getTmpFolderPath() {
        String tmpFolder = System.getProperty("user.dir"); //$NON-NLS-1$
        tmpFolder = tmpFolder + "/" + TEMPFOLDER; //$NON-NLS-1$
        return tmpFolder;
    }

}
