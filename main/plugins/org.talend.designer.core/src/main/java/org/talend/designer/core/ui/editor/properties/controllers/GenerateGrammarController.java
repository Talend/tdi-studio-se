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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.commands.Command;
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
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.commons.utils.io.FilesUtils;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.ITDQItemService;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.process.ITalendProcessJavaProject;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.runprocess.IRunProcessService;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.views.IRepositoryView;

/**
 * Generate grammar java source files and store them to routines for tStandardizeRow. see feature 18851
 *
 * DOC ytao class global comment. Detailled comment
 */
public class GenerateGrammarController extends AbstractElementPropertySectionController {

    public GenerateGrammarController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public void refresh(IElementParameter param, boolean check) {

    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {

        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            // MOD xwang 2011-08-12 make the editor dirty
            executeCommand(new Command() {

                @Override
                public void execute() {
                    Display disp = Display.getCurrent();
                    if (disp == null) {
                        disp = Display.getDefault();
                    }
                    disp.syncExec(new Runnable() {

                        @Override
                        public void run() {
                            generateJavaFile();
                        }
                    });
                }

            });

            IRepositoryView repoView = RepositoryManagerHelper.findRepositoryView();
            if (repoView != null) {
                repoView.refreshView();
            }
            refreshProject();
        }

    };

    @Override
    public int estimateRowSize(Composite subComposite, IElementParameter param) {
        Button btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));
        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnEdit.dispose();
        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    /**
     * create a button and a listener
     */
    @Override
    public Control createControl(Composite subComposite, IElementParameter param, int numInRow, int nbInRow, int top,
            Control lastControl) {
        Button btnEdit;
        btnEdit = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnEdit.setImage(ImageProvider.getImage(DesignerPlugin.getImageDescriptor("icons/routine_generate.gif")));
        FormData data;
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
                data.right = new FormAttachment(lastControl, currentLabelWidth + STANDARD_BUTTON_WIDTH + 8);
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
                data.right = new FormAttachment(0, currentLabelWidth + STANDARD_BUTTON_WIDTH + 8);
            }
        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
            data.right = new FormAttachment(labelLabel, STANDARD_BUTTON_WIDTH + 8, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        btnEdit.setLayoutData(data);
        // **************************
        hashCurControls.put(param.getName(), btnEdit);
        Point initialSize = btnEdit.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dynamicProperty.setCurRowSize(initialSize.y + ITabbedPropertyConstants.VSPACE);
        return addButton(subComposite, param, btnEdit, numInRow, top);
        // return btnEdit;
    }

    private Control addButton(Composite subComposite, final IElementParameter param, Control lastControl, int numInRow, int top) {
        FormData data;
        Button btnImport = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnImport.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor("icons/import.gif")));//$NON-NLS-1$
        btnImport.addSelectionListener(new ImportRulesFromRepository(this));
        btnImport.setData(NAME, Messages.getString("GenerateGrammarController.import"));//$NON-NLS-1$
        btnImport.setData(PARAMETER_NAME, param.getName());
        if (elem instanceof Node) {
            btnImport.setToolTipText(Messages.getString("GenerateGrammarController.importtip"));//$NON-NLS-1$
        }
        data = new FormData();
        data.left = new FormAttachment(lastControl, 0);
        data.right = new FormAttachment(lastControl, btnImport.computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                + (ITabbedPropertyConstants.HSPACE * 2), SWT.RIGHT);
        {
            data.top = new FormAttachment(0, top);
        }
        data.height = STANDARD_HEIGHT + 5;
        data.width = STANDARD_BUTTON_WIDTH;
        btnImport.setLayoutData(data);
        if (numInRow != 1) {
            btnImport.setAlignment(SWT.RIGHT);
        }
        Button btnExport = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnExport.setSize(lastControl.getSize());

        Point btnSize = btnExport.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        btnExport.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor("icons/export.gif")));//$NON-NLS-1$
        btnExport.addSelectionListener(new ExportRulesToRepository(this));
        btnExport.setData(NAME, Messages.getString("GenerateGrammarController.export"));//$NON-NLS-1$
        btnExport.setData(PARAMETER_NAME, param.getName());
        if (elem instanceof Node) {
            btnExport.setToolTipText(Messages.getString("GenerateGrammarController.exporttip"));//$NON-NLS-1$
        }
        data = new FormData();
        data.left = new FormAttachment(btnImport, 0);
        data.right = new FormAttachment(btnImport, STANDARD_BUTTON_WIDTH + 15, SWT.RIGHT);
        {
            data.top = new FormAttachment(0, top);
        }
        data.height = STANDARD_HEIGHT + 5;
        data.width = STANDARD_BUTTON_WIDTH;
        btnExport.setLayoutData(data);
        dynamicProperty.setCurRowSize(btnSize.y + ITabbedPropertyConstants.VSPACE);
        return btnExport;
    }

    /**
     * Generate java source file
     *
     * DOC ytao Comment method "generateJavaFile".
     */
    private void generateJavaFile() {
        Node node = (Node) elem;

        final String JOB_NAME = node.getProcess().getName().toLowerCase();
        final String COMPONENT_NAME = node.getUniqueName().toLowerCase();

        String javaClassName = StringUtils.capitalize(JOB_NAME) + StringUtils.capitalize(COMPONENT_NAME);
        ITDQItemService service = (ITDQItemService) GlobalServiceRegister.getDefault().getService(ITDQItemService.class);
        File fileCreated = null;
        try {
            fileCreated = service.fileCreatedInRoutines(node, javaClassName);
        } catch (Exception ex) {
            MessageDialog.openError(Display.getDefault().getActiveShell(),
                    Messages.getString("GenerateGrammarController.prompt"),//$NON-NLS-1$
                    ex.getMessage());
        }

        if (fileCreated == null) {
            return;
        }

        try {
            RoutineItem returnItem = persistInRoutine(new Path(JOB_NAME), fileCreated, javaClassName);
            addReferenceJavaFile(returnItem, true);
            // ADD (to line 292) xwang 2011-08-12 add routine dependency in job
            if (node.getProcess() instanceof org.talend.designer.core.ui.editor.process.Process) {
                RoutinesParameterType r = TalendFileFactory.eINSTANCE.createRoutinesParameterType();
                r.setId(returnItem.getProperty().getId());
                r.setName(returnItem.getProperty().getLabel());
                List<RoutinesParameterType> routines = new ArrayList<RoutinesParameterType>();
                routines.add(r);
                ((org.talend.designer.core.ui.editor.process.Process) node.getProcess()).addGeneratingRoutines(routines);
            }
            refreshProject();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        // remove temporary files of grammar
        FilesUtils.removeFolder(new File(fileCreated.getParent()), true);
    }

    /**
     * Persist item in routines
     *
     * DOC ytao Comment method "persistInRoutine".
     *
     * @param path, sub folder named with job id
     * @param label, java file name without suffix
     * @param initFile, File handler
     * @param name, job id as package name
     * @return
     */
    private RoutineItem persistInRoutine(IPath inFolder, File fileToFill, String label) {

        // item property to be set
        Property property = PropertiesFactory.eINSTANCE.createProperty();
        property.setAuthor(((RepositoryContext) CorePlugin.getContext().getProperty(Context.REPOSITORY_CONTEXT_KEY)).getUser());
        property.setVersion(VersionUtils.DEFAULT_VERSION);
        property.setStatusCode(""); //$NON-NLS-1$
        // Label must match pattern ^[a-zA-Z\_]+[a-zA-Z0-9\_]*$
        // Must be composed with JAVA_PORJECT_NAME + JOB NAME + COMPONENT NAME,
        // since all projects share with the same routines
        property.setLabel(label);

        // add properties to item
        RoutineItem routineItem = PropertiesFactory.eINSTANCE.createRoutineItem();
        routineItem.setProperty(property);

        // get the content of java file as byte array.
        ByteArray byteArray = PropertiesFactory.eINSTANCE.createByteArray();
        InputStream stream = null;

        try {
            stream = new FileInputStream(fileToFill);
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            byteArray.setInnerContent(bytes);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        routineItem.setContent(byteArray);

        // persist item in routines
        IProxyRepositoryFactory repositoryFactory = ProxyRepositoryFactory.getInstance();

        try {
            property.setId(repositoryFactory.getNextId());
            // create folder with name job id: routines/JOBID (seems from TOS)
            repositoryFactory.createParentFoldersRecursively(ERepositoryObjectType.getItemType(routineItem), inFolder);
            // add the item
            repositoryFactory.create(routineItem, inFolder);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        // TDQ-8142 DEL sizhaoliu do not add jar dependencies to routines
        // //add required jar packages used to compile java file
        // if (routineItem.eResource() != null) {
        // addRequiredLib(routineItem);
        // }

        return routineItem;
    }

    /**
     * Store file to file system. Actually, it locates src/routines/xx DOC ytao Comment method "addReferenceJavaFile".
     *
     * @param routineItem
     * @param copyToTemp
     * @return
     * @throws SystemException
     */
    private IFile addReferenceJavaFile(RoutineItem routineItem, boolean copyToTemp) throws SystemException {
        FileOutputStream fos = null;

        try {
            IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
            ITalendProcessJavaProject talendProcessJavaProject = service.getTalendCodeJavaProject(ERepositoryObjectType.ROUTINES);
            if (talendProcessJavaProject == null) {
                return null;
            }
            String label = routineItem.getProperty().getLabel();

            IFile file = talendProcessJavaProject.getSrcFolder().getFile(
                    JavaUtils.JAVA_ROUTINES_DIRECTORY + '/' + label + JavaUtils.JAVA_EXTENSION);

            if (copyToTemp) {
                String routineContent = new String(routineItem.getContent().getInnerContent());

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
        } catch (Exception e) {
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
     * refresh the project
     *
     * DOC ytao Comment method "refreshProject".
     */
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

}
