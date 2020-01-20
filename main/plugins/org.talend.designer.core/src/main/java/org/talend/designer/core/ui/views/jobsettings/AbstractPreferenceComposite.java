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
package org.talend.designer.core.ui.views.jobsettings;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.dialogs.ProgressDialog;
import org.talend.commons.ui.utils.TypedTextCommandExecutor;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.model.update.UpdatesConstants;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.components.EmfComponent;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.ui.MultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.ChangeValuesFromRepository;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.projectsetting.ElementParameter2ParameterType;
import org.talend.designer.core.ui.projectsetting.ProjectSettingManager;
import org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite;
import org.talend.designer.core.ui.views.statsandlogs.StatsAndLogsViewHelper;
import org.talend.designer.core.utils.DesignerUtilities;
import org.talend.designer.joblet.model.JobletProcess;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.repository.ProjectManager;
import org.talend.repository.UpdateRepositoryUtils;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.ui.dialog.ProjectSettingDialog;

/**
 * Add buttons for loading and saving values between preference page and job view.
 */
public abstract class AbstractPreferenceComposite extends MultipleThreadDynamicComposite {

    protected Button reloadBtn;

    protected Button saveBtn;

    private String dialogTitle;

    protected Button applyToChildrenJob;

    private List<INode> tRunJobNodes;

    private static final String PROCESS = "PROCESS"; //$NON-NLS-1$

    // achen added to fix 0005991 & 0005993
    protected boolean isUsingProjectSetting;

    protected Button useProjectSetting;

    protected Composite topComposite;

    /**
     * DOC chuang AbstractPreferenceComposite constructor comment.
     *
     * @param parentComposite
     * @param styles
     * @param section
     * @param element
     * @param isCompactView
     */
    public AbstractPreferenceComposite(Composite parentComposite, int styles, EComponentCategory section, Element element,
            boolean isCompactView) {
        super(parentComposite, styles, section, element, isCompactView);
    }

    public void setDialogTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    protected abstract boolean needApplyToChildren();

    @Override
    public void addComponents(boolean forceRedraw, boolean reInitialize, int height) {
        if (forceRedraw || isNeedRedraw()) {
            disposeChildren();
            topComposite = new Composite(getComposite(), SWT.NONE);

            if (hasRunJobNode(false) && needApplyToChildren() || isUsingProjectSetting) {
                topComposite.setLayout(new GridLayout(3, false));
            } else {
                topComposite.setLayout(new GridLayout(2, false));
            }

            topComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            reloadBtn = new Button(topComposite, SWT.PUSH);
            reloadBtn.setText(Messages.getString("StatsAndLogsComposite.Reload")); //$NON-NLS-1$
            reloadBtn.setToolTipText(Messages.getString("StatsAndLogsComposite.ReloadToolTipText")); //$NON-NLS-1$

            saveBtn = new Button(topComposite, SWT.PUSH);
            saveBtn.setText(Messages.getString("StatsAndLogsComposite.Save")); //$NON-NLS-1$
            saveBtn.setToolTipText(Messages.getString("StatsAndLogsComposite.SaveToolTipText")); //$NON-NLS-1$
            // achen modify to fix 0005993
            if (isUsingProjectSetting) {
                saveBtn.setText(Messages.getString("SaveToProjectSettings")); //$NON-NLS-1$
                saveBtn.setToolTipText(Messages.getString("SaveToProjectSettingsToolTipText")); //$NON-NLS-1$

                reloadBtn.setText(Messages.getString("ReloadFromProjectSettings")); //$NON-NLS-1$
                reloadBtn.setToolTipText(Messages.getString("ReloadFromProjectSettingsToolTipText")); //$NON-NLS-1$

                // add useprojectsetting button
                useProjectSetting = new Button(topComposite, SWT.CHECK);
                useProjectSetting.setText(Messages.getString("StatsAndLogs.UseProjectSettings")); //$NON-NLS-1$
                useProjectSetting.setToolTipText(Messages.getString("StatsAndLogs.UseProjectSettings")); //$NON-NLS-1$
                useProjectSetting.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

                // TDI-28709: Enhance the display of refresh Stats & Logs composite after import ProjectSetting.xml
                Object value = ElementParameter2ParameterType.getParameterValue(elem,
                        EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName());
                if (value != null && value instanceof Boolean) {
                    Boolean v = (Boolean) value;
                    useProjectSetting.setSelection(v.booleanValue());
                    topComposite.setEnabled(true);
                    if (v.booleanValue()) {
                        if (elem == null) {
                            return;
                        }
                        // achen modify to fix 0005991& 0005993
                        ProjectSettingManager.reloadStatsAndLogFromProjectSettings(elem, ProjectManager.getInstance()
                                .getCurrentProject(), null);
                    }
                }
                if (useProjectSetting != null) {
                    useProjectSetting.removeSelectionListener(selectionListener);
                    useProjectSetting.addSelectionListener(selectionListener);
                }
            }
            // end
            if (hasRunJobNode(false) && needApplyToChildren()) {
                applyToChildrenJob = new Button(topComposite, SWT.PUSH);
                applyToChildrenJob.setText(Messages.getString("AbstractPreferenceComposite.textContent2")); //$NON-NLS-1$
                applyToChildrenJob.setToolTipText(Messages.getString("AbstractPreferenceComposite.tipContent2")); //$NON-NLS-1$
            }

            Point initialSize = topComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);

            addButtonListeners();
            refresh();
            super.addComponents(true, false, initialSize.y + ITabbedPropertyConstants.VSPACE);

        }
    }

    // protected ProcessItem getItem() {
    // Process process = (Process) elem;
    // return (ProcessItem) process.getProperty().getItem();
    // }
    SelectionAdapter selectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            useProjectSettingButtonClick();
        }
    };

    /**
     *
     * DOC aimingchen Comment method "setMainCompositeEnable".
     *
     * @param enabled
     */
    protected void setMainCompositeEnable(boolean enabled) {
        Control[] controls = getComposite().getChildren();
        for (Control control : controls) {
            if (control != topComposite) {
                if (control instanceof Composite) {
                    if (control instanceof CCombo) {
                        setTextEnable(control, enabled, enabled);
                    }
                    setEditable((Composite) control, enabled);
                } else {
                    setTextEnable(control, enabled, enabled);
                }
            }
        }
    }

    /**
     * wchen Comment method "setTextEnable".
     */
    private void setTextEnable(Control control, boolean enabled, boolean flag) {
        if (control instanceof Text) {
            Text t = (Text) control;
            Object data = t.getData(TypedTextCommandExecutor.PARAMETER_NAME);
            if (data != null && data instanceof String) {
                if (((String) data).equals("PARALLELIZE_UNIT_SIZE")) {
                    return;
                } else if (((String) data).equals("PASS")) {
                    return;
                }
            }
            if (useRepository((String) data)) {
                t.setEditable(false);
            } else {
                t.setEditable(enabled);
            }
            if (!t.getEditable()) {
                t.removeMouseListener(listenerSelection);
                t.addMouseListener(listenerSelection);
            } else {
                t.removeMouseListener(listenerSelection);
            }
        } else if (control instanceof Button) {
            Button b = (Button) control;
            Object p = control.getData(TypedTextCommandExecutor.PARAMETER_NAME);
            Object n = control.getData("NAME");
            if (p != null && p instanceof String) {
                if (((String) p).equals("MULTI_THREAD_EXECATION")) {
                    return;
                }
            }
            if (p != null && n != null && p instanceof String && n instanceof String) {
                if (((String) p).equals("SQLEDITOR") && ((String) n).equals("TABLE_STATS")) {
                    return;
                }
            }
            b.setEnabled(flag);
        } else {
            control.setEnabled(flag);
        }
    }

    private void setEditable(Composite parent, boolean editable) {
        Control[] children = parent.getChildren();
        for (Control element : children) {
            if (element instanceof Composite) {
                if (element instanceof CCombo) {
                    setTextEnable(element, editable, editable);
                }
                setEditable((Composite) element, editable);
            } else if (element instanceof Button) {
                setTextEnable(element, editable, editable);
            } else {
                setTextEnable(element, editable, true);
            }
        }
    }

    protected void updateContextValue(boolean update) {
        if (!update) {
            return;
        }
        IElementParameter proElement = elem.getElementParameter(EParameterName.PROPERTY_TYPE.getName()).getChildParameters()
                .get(EParameterName.PROPERTY_TYPE.getName());
        Object value = proElement.getValue();
        if (value instanceof String && ((String) value).equalsIgnoreCase(EmfComponent.REPOSITORY)) {
            String id = (String) elem.getElementParameter(
                    EParameterName.PROPERTY_TYPE.getName() + ":" + EParameterName.REPOSITORY_PROPERTY_TYPE.getName()) //$NON-NLS-1$
                    .getValue();
            String propertyType = EParameterName.PROPERTY_TYPE.getName() + ":"
                    + EParameterName.REPOSITORY_PROPERTY_TYPE.getName();

            ConnectionItem connectionItem = UpdateRepositoryUtils.getConnectionItemByItemId(id);
            Connection connection = connectionItem.getConnection();
            ChangeValuesFromRepository command = new ChangeValuesFromRepository(elem, connection, propertyType, id);
            getCommandStack().execute(command);
        }
    }

    protected boolean useRepository(String paramName) {
        if (elem == null) {
            return false;
        }
        IElementParameter param = elem.getElementParameterFromField(EParameterFieldType.PROPERTY_TYPE, section);
        if (param == null) {
            return false;
        }
        param = param.getChildParameters().get(EParameterName.PROPERTY_TYPE.getName());
        if (param == null) {
            return false;
        }

        if (!EmfComponent.REPOSITORY.equals(param.getValue())) {
            return false;
        }
        if (EParameterName.PROPERTY_TYPE.getName().equals(paramName)) {
            return true;
        }
        IElementParameter elementParameter = elem.getElementParameter(paramName); // ?
        if (elementParameter != null && elementParameter.getCategory() == section
                && elementParameter.getRepositoryValue() != null) {
            return true;
        }
        return false;

    }

    private String implicitParamName(String parameterName) {
        return parameterName + "_IMPLICIT_CONTEXT";
    }

    MouseListener listenerSelection = new MouseAdapter() {

        @Override
        public void mouseDown(MouseEvent e) {
            if (inUseProjectSettingMode(elem, section, EParameterName.STATANDLOG_USE_PROJECT_SETTINGS)
                    || inUseProjectSettingMode(elem, section, EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS)) {

                UseProjectSettingDialog modelSelect = new UseProjectSettingDialog(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell());

                if (modelSelect.open() == UseProjectSettingDialog.OK) {
                    if (modelSelect.getOptionValue().equals("noUseProjectSettings")) { //$NON-NLS-1$
                        useProjectSetting.setSelection(false);
                        useProjectSettingButtonClick();
                    }
                    if (modelSelect.getOptionValue().equals("updateProjectSettings")) {//$NON-NLS-1$
                        useProjectSetting.setSelection(true);
                        useProjectSettingButtonClick();
                        ProjectSettingDialog dialog = new ProjectSettingDialog();
                        dialog.open();
                    }

                }
            }
        }
    };

    private void addButtonListeners() {
        reloadBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onReloadButtonClick();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        saveBtn.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onSaveButtonClick();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });

        if (hasRunJobNode(false) && needApplyToChildren()) {

            applyToChildrenJob.addSelectionListener(new SelectionListener() {

                /*
                 * (non-Javadoc)
                 *
                 * @see
                 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {

                }

                /*
                 * (non-Javadoc)
                 *
                 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetSelected(SelectionEvent e) {
                    // zli for bug 12335
                    final ProgressDialog progress = new ProgressDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell()) {

                        @Override
                        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

                            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                            IEditorReference[] editorReferences = activePage.getEditorReferences();

                            CorePlugin defaultPlugin = CorePlugin.getDefault();
                            IDesignerCoreService designerCoreService = defaultPlugin.getDesignerCoreService();
                            List<IProcess2> openedProcessList = designerCoreService.getOpenedProcess(RepositoryUpdateManager
                                    .getEditors());

                            List<String> activeProcessId = new ArrayList<String>();
                            for (IProcess process : openedProcessList) {
                                activeProcessId.add(process.getId());
                            }
                            final int rate = 1 / openedProcessList.size() - activeProcessId.size();
                            for (INode runjobNode : tRunJobNodes) {
                                String id = (String) runjobNode
                                        .getElementParameter(EParameterName.PROCESS_TYPE_PROCESS.getName()).getValue();
                                String version = (String) runjobNode.getElementParameter(
                                        EParameterName.PROCESS_TYPE_VERSION.getName()).getValue();
                                if ("".equals(id) || id == null) { //$NON-NLS-1$
                                    MessageDialog.openWarning(
                                            getShell(),
                                            Messages.getString("AbstractPreferenceComposite.warning", runjobNode.getUniqueName()), //$NON-NLS-1$
                                            Messages.getString(
                                                    "AbstractPreferenceComposite.jobAssigned", runjobNode.getUniqueName())); //$NON-NLS-1$
                                    return;
                                }
                                if (activeProcessId.contains(ProcessUtils.getPureItemId(id))) {
                                    IEditorPart activeEditorPart = activePage.getActiveEditor();
                                    for (IEditorReference editorReference : editorReferences) {
                                        IEditorPart editorPart = editorReference.getEditor(false);
                                        if ((editorPart != activeEditorPart) && (editorPart instanceof MultiPageTalendEditor)) {
                                            IElement element = ((MultiPageTalendEditor) editorPart).getProcess();
                                            StatsAndLogsViewHelper.applySettings(elem, element, AbstractPreferenceComposite.this);
                                        }
                                    }
                                } else {
                                    try {
                                        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
                                                1 * UpdatesConstants.SCALE, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
                                        subMonitor.beginTask(UpdatesConstants.EMPTY, 1 * rate);
                                        IRepositoryViewObject lastVersion = DesignerPlugin.getDefault()
                                                .getProxyRepositoryFactory().getLastVersion(id);
                                        if (lastVersion != null) {
                                            Item item = lastVersion.getProperty().getItem();
                                            IProcess processFromItem = designerCoreService.getProcessFromItem(item);
                                            if (processFromItem instanceof Process) {

                                                Process process = (Process) processFromItem;

                                                StatsAndLogsViewHelper.applySettings(elem, process,
                                                        AbstractPreferenceComposite.this);

                                                IProxyRepositoryFactory factory = defaultPlugin.getProxyRepositoryFactory();
                                                Property property = factory.getUptodateProperty(process.getProperty());
                                                process.setProperty(property);
                                                subMonitor.subTask(RepositoryUpdateManager.getUpdateJobInfor(process
                                                        .getProperty()));

                                                ProcessType processType = process.saveXmlFile();
                                                Item item2 = process.getProperty().getItem();
                                                if (item2 instanceof JobletProcessItem) {
                                                    ((JobletProcessItem) item2).setJobletProcess((JobletProcess) processType);
                                                } else {
                                                    ((ProcessItem) item2).setProcess(processType);
                                                }
                                                factory.save(item2);
                                                subMonitor.done();

                                            }
                                        }
                                    } catch (PersistenceException e1) {
                                        e1.printStackTrace();
                                    } catch (IOException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                                applySettingsToSubJob(id, version);
                            }
                        }
                    };
                    try {
                        progress.executeProcess();
                    } catch (InvocationTargetException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                /**
                 * yzhang Comment method "applySettingsToSubJob".
                 *
                 * @param id
                 * @param version
                 */
                private void applySettingsToSubJob(String id, String version) {
                    final ProcessItem processItem = ItemCacheManager.getProcessItem(id, version);
                    if (processItem == null) {
                        return;
                    }
                    ProcessType processType = processItem.getProcess();
                    EList<ElementParameterType> parameters = processType.getParameters().getElementParameter();
                    StatsAndLogsViewHelper.applySettings(parameters, elem);

                    List<NodeType> nodes = findRunJobNode(processType.getNode());
                    for (NodeType nodeType : nodes) {
                        EList<ElementParameterType> list = nodeType.getElementParameter();

                        ElementParameterType idParam = getElementParameterType(list, PROCESS + ":" //$NON-NLS-1$
                                + EParameterName.PROCESS_TYPE_PROCESS.getName());
                        ElementParameterType versionParam = getElementParameterType(list, PROCESS + ":" //$NON-NLS-1$
                                + EParameterName.PROCESS_TYPE_VERSION.getName());

                        String subId = idParam.getValue();
                        String subVersion = versionParam.getValue();

                        applySettingsToSubJob(subId, subVersion);
                    }
                }
            });
        }
    }

    /**
     *
     * DOC aimingchen Comment method "useProjectSetting".
     */
    protected void useProjectSetting() {
        if (elem == null) {
            return;
        }
        // achen modify to fix 0005991& 0005993
        onReloadPreference();

        // IEditorPart activeEditor =
        // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        //
        // if (activeEditor != null) {
        // AbstractTalendEditor workbenchPart = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor();
        // workbenchPart.setDirty(true);
        // }

        addComponents(true);
        refresh();
    }

    /**
     * yzhang Comment method "getElementParameterType".
     *
     * @param paraName
     * @return
     */
    private ElementParameterType getElementParameterType(EList<ElementParameterType> list, String paraName) {
        for (ElementParameterType parameter : list) {
            if (parameter.getName().equals(paraName)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * yzhang Comment method "hasRunJobNode".
     *
     * @return
     */
    private boolean hasRunJobNode(boolean needRefresh) {
        if (tRunJobNodes == null || needRefresh) {
            tRunJobNodes = findRunJobNode();
        }
        if (tRunJobNodes == null) {
            return false;
        }
        return tRunJobNodes.size() > 0;
    }

    /**
     * yzhang Comment method "foundRunJobNode".
     *
     * @return
     */
    private List<INode> findRunJobNode() {
        if (elem instanceof IProcess) {
            return DesignerUtilities.getTRunjobs((IProcess) elem);
        } else {
            return null;
        }
    }

    /**
     * yzhang Comment method "findRunJobNode".
     *
     * @param nodes
     * @return
     */
    private List<NodeType> findRunJobNode(EList<NodeType> nodes) {
        List<NodeType> matchingNodes = new ArrayList<NodeType>();
        for (NodeType nodeType : nodes) {
            if (DesignerUtilities.isTRunJobComponent(nodeType)) {
                matchingNodes.add(nodeType);
            }
        }
        return matchingNodes;
    }

    protected void onReloadButtonClick() {
        if (elem == null) {
            return;
        }
        // achen modify to fix 0005991& 0005993
        String message = ""; //$NON-NLS-1$
        if (!isUsingProjectSetting) {
            message = Messages.getString("StatsAndLogsComposite.ReloadMessages"); //$NON-NLS-1$
        } else {
            message = Messages.getString("ReloadFromProjectSettingsMessages"); //$NON-NLS-1$
        }

        boolean isOK = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), dialogTitle, message);
        if (isOK) {
            onReloadPreference();

            // IEditorPart activeEditor =
            // PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            //
            // if (activeEditor != null) {
            // AbstractTalendEditor workbenchPart = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor();
            // workbenchPart.setDirty(true);
            // }
        }
        addComponents(true);
        refresh();
    }

    /**
     * Override by subclass
     *
     */
    protected void useProjectSettingButtonClick() {

    }

    /**
     * Override by subclass.
     */
    protected void onReloadPreference() {

    }

    protected void onSaveButtonClick() {
        if (elem == null) {
            return;
        }
        // achen modify to fix 0005991& 0005993
        String message = ""; //$NON-NLS-1$
        if (!isUsingProjectSetting) {
            message = Messages.getString("StatsAndLogsComposite.SavePreferenceMessages"); //$NON-NLS-1$
        } else {
            message = Messages.getString("SaveToProjectSettingsMessage"); //$NON-NLS-1$
        }
        boolean isOK = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), dialogTitle, message);
        if (isOK) {
            onSavePreference();
        }
    }

    /**
     * Override by subclass.
     */
    protected void onSavePreference() {
    }

    @Override
    public void refresh() {
        super.refresh();

        Element element = getElement();
        if (element != null && element instanceof IProcess) {
            IProcess process = (IProcess) element;
            if (reloadBtn != null && !reloadBtn.isDisposed()) {
                reloadBtn.setEnabled(!process.isReadOnly());
            }
            if (saveBtn != null && !saveBtn.isDisposed()) {
                saveBtn.setEnabled(!process.isReadOnly());
            }
            if (applyToChildrenJob != null && !applyToChildrenJob.isDisposed()) {
                applyToChildrenJob.setEnabled(!process.isReadOnly());
            }
        }
    }

    public static boolean inUseProjectSettingMode(final IElement element, final EComponentCategory category,
            final EParameterName paramName) {
        if (element != null
                && category != null
                && element instanceof IProcess
                && (paramName == EParameterName.STATANDLOG_USE_PROJECT_SETTINGS || paramName == EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS)) {

            IElementParameter tmpParam = element.getElementParameter(paramName.getName());
            if (tmpParam != null && tmpParam.getCategory() == category && tmpParam.getValue() instanceof Boolean
                    && (Boolean) tmpParam.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * DOC Administrator AbstractPreferenceComposite class global comment. Detailled comment
     */
    class UseProjectSettingDialog extends SelectionDialog {

        private final String TITLE = "Edit parameter"; //$NON-NLS-1$

        private final String MESSAGE = "Plese choose one option."; //$NON-NLS-1$

        private String str;

        private Button noUseProjectSettingsButton, updateProjectSettingsButton;

        private Boolean readOnlyJob;

        public UseProjectSettingDialog(Shell parentShell) {
            this(parentShell, false);
        }

        public UseProjectSettingDialog(Shell parentShell, boolean isReadOnly) {
            super(parentShell);
            setHelpAvailable(false);
            setTitle(TITLE);
            setMessage(MESSAGE);

            this.readOnlyJob = isReadOnly;

        }

        @Override
        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);
            createMessageArea(composite);
            Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
            titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            createOptionArea(composite);
            return composite;
        }

        protected Control createOptionArea(Composite composite) {
            Composite inner = new Composite(composite, SWT.NONE);
            GridLayout gridLayout = new GridLayout();
            gridLayout.marginHeight = 0;
            inner.setLayout(gridLayout);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 300;
            inner.setLayoutData(gridData);

            Group group = new Group(inner, SWT.NONE);
            group.setText("Option"); //$NON-NLS-1$
            gridLayout = new GridLayout();
            gridLayout.horizontalSpacing = 10;
            group.setLayout(gridLayout);
            group.setLayoutData(gridData);

            noUseProjectSettingsButton = new Button(group, SWT.RADIO);
            noUseProjectSettingsButton.setText("Don't use project settings"); //$NON-NLS-1$

            updateProjectSettingsButton = new Button(group, SWT.RADIO);
            updateProjectSettingsButton.setText("Update project settings"); //$NON-NLS-1$

            configControlStatus();

            return inner;
        }

        private void configControlStatus() {
            if (readOnlyJob) {
                noUseProjectSettingsButton.setEnabled(false);
                updateProjectSettingsButton.setEnabled(false);
            }
        }

        @Override
        protected void okPressed() {
            if (noUseProjectSettingsButton.getSelection()) {
                setOptionValue("noUseProjectSettings"); //$NON-NLS-1$
            }
            if (updateProjectSettingsButton.getSelection()) {
                setOptionValue("updateProjectSettings");//$NON-NLS-1$
            }

            super.okPressed();
        }

        public String getOptionValue() {
            return this.str;
        }

        public void setOptionValue(String str) {
            this.str = str;
        }

    }
}
