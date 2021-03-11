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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.IControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.properties.controllers.creator.SelectAllTextControlCreator;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;
import org.talend.repository.ui.dialog.UseDynamicJobSelectionDialog;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public class ProcessController extends AbstractElementPropertySectionController {

    private static final String COMMA = ";";

    boolean isSelectUseDynamic = false;

    public ProcessController(IDynamicProperty dp) {
        super(dp);
    }

    @Override
    public Control createControl(final Composite subComposite, final IElementParameter param, final int numInRow,
            final int nbInRow, final int top, final Control lastControl) {
        this.curParameter = param;
        this.paramFieldType = param.getFieldType();
        FormData data;

        IElementParameter processTypeParameter = param.getChildParameters().get(EParameterName.PROCESS_TYPE_PROCESS.getName());
        Text labelText;

        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new SelectAllTextControlCreator());
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        Control cLayout = dField.getLayoutControl();
        labelText = (Text) dField.getControl();

        labelText.setData(PARAMETER_NAME, param.getName());

        cLayout.setBackground(subComposite.getBackground());
        labelText.setEditable(false);
        if (elem instanceof Node) {
            labelText.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
        }

        addDragAndDropTarget(labelText);

        CLabel labelLabel = getWidgetFactory().createCLabel(subComposite, param.getDisplayName());
        data = new FormData();
        if (lastControl != null) {
            data.left = new FormAttachment(lastControl, 0);
        } else {
            data.left = new FormAttachment((((numInRow - 1) * MAX_PERCENT) / (nbInRow + 1)), 0);
        }
        data.top = new FormAttachment(0, top);
        labelLabel.setLayoutData(data);
        if (numInRow != 1) {
            labelLabel.setAlignment(SWT.RIGHT);
        }
        // *********************
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
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.right = new FormAttachment((numInRow * MAX_PERCENT) / (nbInRow + 1), 0);
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);

        Button btn;
        Point btnSize;

        btn = getWidgetFactory().createButton(subComposite, "", SWT.PUSH); //$NON-NLS-1$
        btnSize = btn.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        btn.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(DOTS_BUTTON)));

        btn.addSelectionListener(listenerSelection);
        btn.setData(PARAMETER_NAME, param.getName() + ":" + processTypeParameter.getName()); //$NON-NLS-1$
        btn.setEnabled(!param.isReadOnly());
        data = new FormData();
        data.left = new FormAttachment(cLayout, 0);
        data.right = new FormAttachment(cLayout, STANDARD_BUTTON_WIDTH, SWT.RIGHT);
        data.top = new FormAttachment(0, top);
        data.height = STANDARD_HEIGHT - 2;
        btn.setLayoutData(data);

        // **********************

        hashCurControls.put(param.getName() + ":" + processTypeParameter.getName(), labelText); //$NON-NLS-1$

        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        // feature 19312
        IElementParameter useDynamicJobParameter = param.getElement().getElementParameter(
                EParameterName.USE_DYNAMIC_JOB.getName());
        if (useDynamicJobParameter != null && useDynamicJobParameter instanceof IElementParameter) {
            Object useDynamicJobValue = useDynamicJobParameter.getValue();
            if (useDynamicJobValue != null && useDynamicJobValue instanceof Boolean) {
                isSelectUseDynamic = (Boolean) useDynamicJobValue;
            }
        }
        Control lastControlUsed = btn;
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        boolean allowVerchange = brandingService.getBrandingConfiguration().isAllowChengeVersion();
        if (allowVerchange) {
            lastControlUsed = addJobVersionCombo(subComposite,
                    param.getChildParameters().get(EParameterName.PROCESS_TYPE_VERSION.getName()), lastControlUsed, numInRow + 1,
                    nbInRow, top);
        }
        if (!isSelectUseDynamic) {
            addContextCombo(subComposite, param.getChildParameters().get(EParameterName.PROCESS_TYPE_CONTEXT.getName()),
                    lastControlUsed, numInRow + 1, nbInRow, top);
        }
        dynamicProperty.setCurRowSize(Math.max(initialSize.y, btnSize.y) + ITabbedPropertyConstants.VSPACE);
        return btn;
    }

    IControlCreator cbCtrl = new IControlCreator() {

        @Override
        public Control createControl(final Composite parent, final int style) {
            CCombo cb = new CCombo(parent, style);
            return cb;
        }
    };

    /**
     * ftang Comment method "addContextCombo".
     *
     * @param subComposite
     * @param param
     * @param lastControl
     * @param numInRow
     * @param nbInRow
     * @param top
     * @return
     */
    private Control addJobVersionCombo(Composite subComposite, IElementParameter param, Control lastControl, int numInRow,
            int nbInRow, int top) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("ComboController.valueFromRepository")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        CCombo combo = (CCombo) dField.getControl();
        FormData data;
        combo.setItems(getListToDisplay(param));
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
        combo.setData(PARAMETER_NAME, param.getName());
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
            // feature 19312
            if (isSelectUseDynamic) {
                combo.setEnabled(false);
            }
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
        // *********************
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
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), combo);

        return cLayout;
    }

    private Control addContextCombo(Composite subComposite, IElementParameter param, Control lastControl, int numInRow,
            int nbInRow, int top) {
        DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, cbCtrl);
        if (param.isRequired()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_REQUIRED);
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.TOP, false);
        }
        if (param.isRepositoryValueUsed()) {
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(
                    FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
            decoration.setDescription(Messages.getString("ComboController.valueFromRepository")); //$NON-NLS-1$
            dField.addFieldDecoration(decoration, SWT.RIGHT | SWT.BOTTOM, false);
        }

        Control cLayout = dField.getLayoutControl();
        CCombo combo = (CCombo) dField.getControl();
        FormData data;
        combo.setItems(getListToDisplay(param));
        combo.setEditable(false);
        cLayout.setBackground(subComposite.getBackground());
        combo.setEnabled(!param.isReadOnly());
        combo.addSelectionListener(listenerSelection);
        combo.setData(PARAMETER_NAME, param.getName());
        if (elem instanceof Node) {
            combo.setToolTipText(VARIABLE_TOOLTIP + param.getVariableName());
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
        // *********************
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
            } else {
                data.left = new FormAttachment(0, currentLabelWidth);
            }

        } else {
            data.left = new FormAttachment(labelLabel, 0, SWT.RIGHT);
        }
        data.top = new FormAttachment(0, top);
        cLayout.setLayoutData(data);
        // **********************
        hashCurControls.put(param.getName(), combo);

        return cLayout;
    }

    private String[] getListToDisplay(IElementParameter param) {
        String[] originalList = param.getListItemsDisplayName();
        List<String> stringToDisplay = new ArrayList<String>();
        String[] itemsShowIf = param.getListItemsShowIf();
        if (itemsShowIf != null) {
            String[] itemsNotShowIf = param.getListItemsNotShowIf();
            for (int i = 0; i < originalList.length; i++) {
                if (param.isShow(itemsShowIf[i], itemsNotShowIf[i], elem.getElementParameters())) {
                    stringToDisplay.add(originalList[i]);
                }
            }
        } else {
            for (String element : originalList) {
                stringToDisplay.add(element);
            }
        }
        return stringToDisplay.toArray(new String[0]);
    }

    SelectionListener listenerSelection = new SelectionListener() {

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            // do nothing.
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            Command cmd = createCommand(e);
            executeCommand(cmd);
        }
    };

    private Command createCommand(SelectionEvent selectionEvent) {
        if (selectionEvent.getSource() instanceof Button) {
            return createButtonCommand((Button) selectionEvent.getSource());
        }
        if (selectionEvent.getSource() instanceof CCombo) {
            return createComboCommand((CCombo) selectionEvent.getSource());
        }
        return null;
    }

    /**
     * DOC nrousseau Comment method "createComboCommand".
     *
     * @param source
     * @return
     */
    private Command createComboCommand(CCombo combo) {
        String paramName = (String) combo.getData(PARAMETER_NAME);

        IElementParameter param = elem.getElementParameter(paramName);

        String value = combo.getText();

        for (int j = 0; j < param.getListItemsValue().length; j++) {
            if (combo.getText().equals(param.getListItemsDisplayName()[j])) {
                value = (String) param.getListItemsValue()[j];
            }
        }
        if (value.equals(param.getValue())) {
            return null;
        }

        return new PropertyChangeCommand(elem, paramName, value);
    }

    /**
     * DOC nrousseau Comment method "createButtonCommand".
     *
     * @param source
     * @return
     */
    protected Command createButtonCommand(Button button) {
        String procssId = null;
        if (elem != null && elem instanceof Node) {
            Node runJobNode = (Node) elem;
            procssId = runJobNode.getProcess().getId();
        }
        // feature 19312
        boolean isSelectUseDynamic = false;
        IElementParameter useDynamicJobParameter = elem.getElementParameter(EParameterName.USE_DYNAMIC_JOB.getName());
        if (useDynamicJobParameter != null && useDynamicJobParameter instanceof IElementParameter) {
            Object useDynamicJobValue = useDynamicJobParameter.getValue();
            if (useDynamicJobValue != null && useDynamicJobValue instanceof Boolean) {
                isSelectUseDynamic = (Boolean) useDynamicJobValue;
            }
        }
        if (isSelectUseDynamic) {
            UseDynamicJobSelectionDialog usedialog = new UseDynamicJobSelectionDialog((button).getShell(),
                    ERepositoryObjectType.PROCESS, procssId, isSelectUseDynamic);
            // open the tree dialog and selected job if Checked
            selectJobNodeIfChecked(button, usedialog);

            if (usedialog.open() == UseDynamicJobSelectionDialog.OK) {
                List<RepositoryNode> repositoryNodeList = usedialog.getRepositoryNodes();
                StringBuffer ids = new StringBuffer();
                String paramName = (String) button.getData(PARAMETER_NAME);

                if (repositoryNodeList != null && repositoryNodeList.size() > 0) {
                    for (int i = 0; i < repositoryNodeList.size(); i++) {
                        RepositoryNode node = repositoryNodeList.get(i);
                        IRepositoryViewObject repositoryViewObject = node.getObject();
                        final Item item = repositoryViewObject.getProperty().getItem();
                        String id = item.getProperty().getId();
                        if (i > 0) {
                            ids.append(ProcessController.COMMA);
                        }
                        ids.append(id);
                    }
                }
                if (repositoryNodeList.size() > 1) {
                    MessageDialog.openInformation(button.getShell(), "Message", Messages.getString(
                            "ProcessController.UseDynamicJobSelectionDialog.message", repositoryNodeList.get(0).getLabel()));
                }
                return new PropertyChangeCommand(elem, paramName, ids.toString());
            }
            return null;
        } else {
            List<ERepositoryObjectType> repObjectTypes = new ArrayList<ERepositoryObjectType>();
            repObjectTypes.add(ERepositoryObjectType.PROCESS);
            repObjectTypes.add(ERepositoryObjectType.PROCESS_STORM);
            repObjectTypes.add(ERepositoryObjectType.PROCESS_MR);
            RepositoryReviewDialog dialog = new RepositoryReviewDialog((button).getShell(), repObjectTypes, procssId);

            // see feature 0003664: tRunJob: When opening the tree dialog to select the job target, it could be useful
            // to
            // open it on previous selected job if exists
            selectJobNodeIfExists(button, dialog);

            if (dialog.open() == RepositoryReviewDialog.OK) {
                IRepositoryViewObject repositoryObject = dialog.getResult().getObject();
                final Item item = repositoryObject.getProperty().getItem();
                org.talend.core.model.properties.Project project = ProjectManager.getInstance()
                        .getProject(item.getProperty());
                String id = ProcessUtils.getProjectProcessId(project.getTechnicalLabel(), item.getProperty().getId());
                String paramName = (String) button.getData(PARAMETER_NAME);
                return new PropertyChangeCommand(elem, paramName, id);
            }
            return null;
        }
    }

    /**
     * see feature 0003664: tRunJob: When opening the tree dialog to select the job target, it could be useful to open
     * it on previous selected job if exists.
     *
     * @param button
     * @param dialog
     */
    private void selectJobNodeIfExists(Button button, RepositoryReviewDialog dialog) {
        try {
            if (elem != null && elem instanceof Node) {
                Node runJobNode = (Node) elem;
                String paramName = (String) button.getData(PARAMETER_NAME);
                String jobId = (String) runJobNode.getPropertyValue(paramName); // .getElementParameter(name).getValue();
                if (StringUtils.isNotEmpty(jobId)) {
                    // if user have selected job
                    ProcessItem processItem = ItemCacheManager.getProcessItem(jobId);
                    if (processItem != null) {
                        String jobName = processItem.getProperty().getLabel();
                        // expand the tree node and reveal it
                        dialog.setSelectedNodeName(jobName);
                    }
                }
            }
        } catch (Throwable e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * DOC yhch Comment method "selectJobNodeIfChecked".
     *
     * @param button
     * @param dialog
     */
    private void selectJobNodeIfChecked(Button button, UseDynamicJobSelectionDialog dialog) {
        try {
            if (elem != null && elem instanceof Node) {
                Node runJobNode = (Node) elem;
                String paramName = (String) button.getData(PARAMETER_NAME);
                String jobIds = (String) runJobNode.getPropertyValue(paramName);
                if (StringUtils.isNotEmpty(jobIds)) {
                    String[] jobsArr = jobIds.split(ProcessController.COMMA);
                    dialog.setNeedCheckedjobs(jobsArr);
                }
            }
        } catch (Throwable e) {
            ExceptionHandler.process(e);
        }
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
        final DecoratedField dField = new DecoratedField(subComposite, SWT.BORDER, new IControlCreator() {

            @Override
            public Control createControl(Composite parent, int style) {
                return getWidgetFactory().createButton(parent, EParameterName.PROCESS_TYPE.getDisplayName(), SWT.None);
            }

        });
        Point initialSize = dField.getLayoutControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        dField.getLayoutControl().dispose();

        return initialSize.y + ITabbedPropertyConstants.VSPACE;
    }

    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void refresh(final IElementParameter param, boolean check) {
        new Thread() {

            /*
             * (non-Javadoc)
             *
             * @see java.lang.Thread#run()
             */
            @Override
            public void run() {

                Display.getDefault().syncExec(new Runnable() {

                    /*
                     * (non-Javadoc)
                     *
                     * @see java.lang.Runnable#run()
                     */
                    @Override
                    public void run() {
                        updateContextList(param);
                        if (hashCurControls == null) {
                            return;
                        }
                        IElementParameter processTypeParameter = param.getChildParameters().get(
                                EParameterName.PROCESS_TYPE_PROCESS.getName());
                        Text jobName = (Text) hashCurControls.get(param.getName() + ":" + processTypeParameter.getName()); //$NON-NLS-1$
                        if (jobName != null && !jobName.isDisposed()) {
                            final String labelFromRepository = processTypeParameter.getLabelFromRepository();
                            if (labelFromRepository == null) {
                                jobName.setText(""); //$NON-NLS-1$
                            } else {
                                jobName.setText(labelFromRepository);
                            }
                        }
                        // context
                        refreshCombo(param, EParameterName.PROCESS_TYPE_CONTEXT.getName());
                        // version
                        refreshCombo(param, EParameterName.PROCESS_TYPE_VERSION.getName());

                        if (elem != null && elem instanceof Node) {
                            ((Node) elem).checkAndRefreshNode();
                        }
                    }
                });

            }
        }.start();

    }

    /**
     *
     * ggu Comment method "refreshCombo".
     *
     */
    private void refreshCombo(IElementParameter parentParam, final String childParamName) {
        if (parentParam == null || childParamName == null) {
            return;
        }
        IElementParameter childParameter = parentParam.getChildParameters().get(childParamName);

        CCombo combo = (CCombo) hashCurControls.get(childParameter.getName());

        if (combo == null || combo.isDisposed()) {
            return;
        }
        Object value = childParameter.getValue();
        if (value instanceof String) {
            String strValue = ""; //$NON-NLS-1$
            int nbInList = 0, nbMax = childParameter.getListItemsValue().length;
            String name = (String) value;
            while (strValue.equals(new String("")) && nbInList < nbMax) { //$NON-NLS-1$
                if (name.equals(childParameter.getListItemsValue()[nbInList])) {
                    strValue = childParameter.getListItemsDisplayName()[nbInList];
                }
                nbInList++;
            }
            String[] paramItems = getListToDisplay(childParameter);
            String[] comboItems = combo.getItems();

            if (!Arrays.equals(paramItems, comboItems)) {
                combo.setItems(paramItems);
            }
            combo.setText(strValue);
            combo.setVisible(true);
        }

    }

    /**
     *
     * ggu Comment method "updateContextList".
     *
     * @param processParam
     */
    private void updateContextList(IElementParameter processParam) {
        if (processParam == null
                || (processParam.getFieldType() != EParameterFieldType.PROCESS_TYPE && processParam.getFieldType() != EParameterFieldType.ROUTE_INPUT_PROCESS_TYPE)) {
            return;
        }
        // for context type
        List<String> contextNameList = new ArrayList<String>();
        List<String> contextValueList = new ArrayList<String>();
        // for version type
        List<String> versionNameList = new ArrayList<String>();
        List<String> versionValueList = new ArrayList<String>();
        versionNameList.add(ItemCacheManager.LATEST_VERSION);
        versionValueList.add(ItemCacheManager.LATEST_VERSION);

        IElementParameter jobNameParam = processParam.getChildParameters().get(EParameterName.PROCESS_TYPE_PROCESS.getName());

        // feature 19312
        Item item = null;
        StringBuffer labels = new StringBuffer("");
        List<IRepositoryViewObject> allVersion = new ArrayList<IRepositoryViewObject>();
        final String strJobId = (String) jobNameParam.getValue();
        String[] strJobIds = strJobId.split(ProcessController.COMMA);
        for (int i = 0; i < strJobIds.length; i++) {
            String id = strJobIds[i];
            if (StringUtils.isNotEmpty(id)) {
                allVersion = ProcessorUtilities.getAllVersionObjectById(id);

                // IRepositoryObject lastVersionObject = null;
                String label = null;
                if (allVersion != null) {
                    String oldVersion = null;
                    for (IRepositoryViewObject obj : allVersion) {
                        String version = obj.getVersion();
                        if (oldVersion == null) {
                            oldVersion = version;
                        }
                        if (VersionUtils.compareTo(version, oldVersion) >= 0) {
                            item = obj.getProperty().getItem();
                            // lastVersionObject = obj;
                        }
                        oldVersion = version;
                        versionNameList.add(version);
                        versionValueList.add(version);
                    }
                    label = item.getProperty().getLabel();
                    if (i > 0) {
                        labels.append(ProcessController.COMMA);
                    }
                    labels.append(label);
                    // IPath path = RepositoryNodeUtilities.getPath(lastVersionObject);
                    // if (path != null) {
                    // label = path.toString() + IPath.SEPARATOR + label;
                    // }
                } else {
                    // FIXME TUP-5524, don't set empty, keep the id value instead.
                    // final String parentName = processParam.getName() + ":"; //$NON-NLS-1$
                    // if (elem != null) {
                    // // can be called in multi-thread, dispose method may be already called before executing this
                    // method
                    //elem.setPropertyValue(parentName + jobNameParam.getName(), ""); //$NON-NLS-1$
                    // }
                }
            }
        }
        jobNameParam.setLabelFromRepository(labels.toString());
        // set default context
        String defalutValue = null;
        if (item != null && item instanceof ProcessItem) {
            for (Object o : ((ProcessItem) item).getProcess().getContext()) {
                if (o instanceof ContextType) {
                    ContextType context = (ContextType) o;
                    contextNameList.add(context.getName());
                    contextValueList.add(context.getName());
                }
            }
            defalutValue = ((ProcessItem) item).getProcess().getDefaultContext();
        }

        setProcessTypeRelatedValues(processParam, contextNameList, contextValueList,
                EParameterName.PROCESS_TYPE_CONTEXT.getName(), defalutValue);

        setProcessTypeRelatedValues(processParam, versionNameList, versionValueList,
                EParameterName.PROCESS_TYPE_VERSION.getName(), null);

    }

    /**
     *
     * ggu Comment method "setProcessTypeRelatedValues".
     *
     *
     */
    private void setProcessTypeRelatedValues(IElementParameter parentParam, List<String> nameList, List<String> valueList,
            final String childName, final String defaultValue) {
        if (parentParam == null || childName == null) {
            return;
        }
        final String fullChildName = parentParam.getName() + ":" + childName; //$NON-NLS-1$
        IElementParameter childParam = parentParam.getChildParameters().get(childName);

        IElementParameter jobNameParam = parentParam.getChildParameters().get(EParameterName.PROCESS_TYPE_PROCESS.getName());
        if (jobNameParam != null) {
            String value = (String) jobNameParam.getValue();
            if (value == null || "".equals(value)) { //$NON-NLS-1$
                childParam.setValue(null);
            }
        }
        if (nameList == null) {
            childParam.setListItemsDisplayName(new String[0]);
        } else {
            childParam.setListItemsDisplayName(nameList.toArray(new String[0]));
        }
        if (valueList == null) {
            childParam.setListItemsValue(new String[0]);
        } else {
            childParam.setListItemsValue(valueList.toArray(new String[0]));
        }

        if (elem != null) {
            if (valueList != null && !valueList.contains(childParam.getValue())) {
                if (nameList != null && nameList.size() > 0) {
                    // set default value
                    if (defaultValue != null) {
                        childParam.setValue(defaultValue);
                    } else {
                        elem.setPropertyValue(fullChildName, valueList.get(valueList.size() - 1));
                    }
                }
            } else {
                // force to store the value again to activate the code
                // generation in Node.setPropertyValue
                elem.setPropertyValue(fullChildName, childParam.getValue());
            }
        }
    }
}
