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
package org.talend.designer.codegen.components.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.runtime.helper.LocalComponentInstallHelper;
import org.talend.commons.runtime.service.ComponentsInstallComponent;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.preferences.CheckBoxFieldEditor;
import org.talend.commons.ui.utils.workbench.preferences.ComboFieldEditor;
import org.talend.commons.utils.resource.UpdatesHelper;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.designer.codegen.CodeGeneratorActivator;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.i18n.Messages;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.assist.TalendEditorComponentCreationUtil;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */
public class ComponentsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private CheckBoxFieldEditor doNotShowJobAfterDoubleClickCheckBoxField;

    private CheckBoxFieldEditor doNotShowJobletAfterDoubleClickCheckBoxField;

    private CheckBoxFieldEditor enableComponentAssistCheckBoxField;

    private DirectoryFieldEditor filePathTemp;

    private final String dataViewer = "Data Viewer"; //$NON-NLS-1$

    private final String mapper = "Mapper "; //$NON-NLS-1$

    private final String tRunJob = "tRunJob"; //$NON-NLS-1$

    private final String joblet = "Joblet"; //$NON-NLS-1$

    private final String assist = "Component Assist"; //$NON-NLS-1$

    private static String oldPath = null;

    /**
     * This class exists to provide visibility to the <code>refreshValidState</code> method and to perform more
     * intelligent clearing of the error message.
     */
    protected class ConsoleIntegerFieldEditor extends IntegerFieldEditor {

        public ConsoleIntegerFieldEditor(String name, String labelText, Composite parent) {
            super(name, labelText, parent);
        }

        /**
         * @see org.eclipse.jface.preference.FieldEditor#refreshValidState()
         */
        @Override
        protected void refreshValidState() {
            super.refreshValidState();
        }

        /**
         * Clears the error message from the message line if the error message is the error message from this field
         * editor.
         */
        @Override
        protected void clearErrorMessage() {
            if (canClearErrorMessage()) {
                super.clearErrorMessage();
            }
        }
    }

    public ComponentsPreferencePage() {
        super(GRID);
        setPreferenceStore(CodeGeneratorActivator.getDefault().getPreferenceStore());
    }

    public void createFieldEditors2(Composite composite) {

        LINK_STYLE[] linkStyles = LINK_STYLE.values();
        String[][] strComboValues = new String[linkStyles.length][2];

        for (int i = 0; i < linkStyles.length; i++) {
            strComboValues[i][0] = linkStyles[i].getDisplayName();
            strComboValues[i][1] = linkStyles[i].getName();
        }

        ComboFieldEditor dbTypeField = new ComboFieldEditor(IComponentPreferenceConstant.LINK_STYLE,
                Messages.getString("ComponentsPreferencePage.configuration.LINK_STYLE"), strComboValues, composite); //$NON-NLS-1$
        addField(dbTypeField);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

        if (event.getSource() == filePathTemp) {
            // propertyChangeForComponents(event);
        } else {
            Object nValue = event.getNewValue();
            if (event.getProperty().equals(FieldEditor.IS_VALID)) {
                boolean newValue = ((Boolean) nValue).booleanValue();
                if (newValue) {
                    checkState();
                } else {
                    super.propertyChange(event);
                }

            } else {
                super.propertyChange(event);
            }
        }
    }

    protected boolean canClearErrorMessage() {
        return true;
    }

    protected Composite createForMapper(Composite parent) {
        Group group = createGroup(parent);
        group.setText(mapper);
        Composite composite = createComposite(group);
        addFontAndColorFieldsForMapper(composite);
        GridLayout layout = createLayout();
        composite.setLayout(layout);
        return group;
    }

    protected Composite createForDataViewer(Composite parent) {
        Group group = createGroup(parent);
        group.setText(dataViewer);
        Composite composite = createComposite(group);
        addFontAndColorFieldsForDataViewer(composite);
        GridLayout layout = createLayout();
        composite.setLayout(layout);
        return group;
    }

    protected Composite createFortRunJob(Composite parent) {
        Group group = createGroup(parent);
        group.setText(tRunJob);
        Composite composite = createComposite(group);
        addFontAndColorFieldsFortRunJob(composite);
        GridLayout layout = createLayout();
        composite.setLayout(layout);
        return group;
    }

    // for feature 13361
    protected Composite createForJoblet(Composite parent) {
        Group group = createGroup(parent);
        group.setText(joblet);
        Composite composite = createComposite(group);
        addFontAndColorFieldsForJoblet(composite);
        GridLayout layout = createLayout();
        composite.setLayout(layout);
        return group;
    }

    protected Composite createForComponentAssist(Composite parent) {
        Group group = createGroup(parent);
        group.setText(assist);
        Composite composite = createComposite(group);
        addFontAndColorFieldsForAssist(composite);
        GridLayout layout = createLayout();
        composite.setLayout(layout);
        return group;
    }

    protected Group createGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.horizontalSpan = 3;
        group.setLayoutData(layoutData);
        group.setLayout(new GridLayout(3, false));
        return group;
    }

    protected Composite createComposite(Group group) {
        Composite composite = new Composite(group, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        composite.setLayoutData(gridData);
        return composite;
    }

    protected GridLayout createLayout() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 8;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 8;
        return layout;
    }

    protected void addFontAndColorFieldsForDataViewer(Composite composite) {
        IntegerFieldEditor rowLimit = new IntegerFieldEditor(IComponentPreferenceConstant.LIMIT,
                Messages.getString("ComponentsPreferencePage.rowLimit"), composite);//$NON-NLS-1$
        rowLimit.setValidRange(1, 1000);
        addField(rowLimit);
        Text textControl = rowLimit.getTextControl(composite);
        GridData layoutData = new GridData();
        layoutData.widthHint = 200;
        textControl.setLayoutData(layoutData);
    }

    protected void addFontAndColorFieldsForMapper(Composite composite) {
        createFieldEditors2(composite);
    }

    protected void addFontAndColorFieldsFortRunJob(Composite composite) {
        doNotShowJobAfterDoubleClickCheckBoxField = new CheckBoxFieldEditor(IComponentPreferenceConstant.IS_AVOID,
                Messages.getString("ComponenttRunJobPreferencePage.label"), composite); //$NON-NLS-1$
        addField(doNotShowJobAfterDoubleClickCheckBoxField);
    }

    protected void addFontAndColorFieldsForJoblet(Composite composite) {
        doNotShowJobletAfterDoubleClickCheckBoxField = new CheckBoxFieldEditor(IComponentPreferenceConstant.IS_AVOID_JOBLET,
                Messages.getString("ComponentJobletPreferencePage.label"), composite); //$NON-NLS-1$
        addField(doNotShowJobletAfterDoubleClickCheckBoxField);
    }

    protected void addFontAndColorFieldsForAssist(Composite composite) {
        enableComponentAssistCheckBoxField = new CheckBoxFieldEditor(TalendDesignerPrefConstants.COMPONENT_ASSIST,
                Messages.getString("ComponentsPreferencePage.componentAssist"), composite); //$NON-NLS-1$
        addField(enableComponentAssistCheckBoxField);
    }

    @Override
    public void createFieldEditors() {
        final Composite parent = getFieldEditorParent();
        filePathTemp = new DirectoryFieldEditor(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER,
                Messages.getString("ComponentsPreferencePage.directoryFieldLabel"), //$NON-NLS-1$
                parent);
        addField(filePathTemp);

        filePathTemp.getTextControl(parent).addModifyListener(new ModifyListener() {

            String oldPath = getPreferenceStore().getString(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER);

            @Override
            public void modifyText(ModifyEvent e) {
                String newPath = filePathTemp.getTextControl(parent).getText();
                File file = new File(newPath);
                if (!file.exists() && !"".equals(newPath)) { //$NON-NLS-1$
                    // getPreferenceStore().setValue(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER, "");
                    filePathTemp.showErrorMessage();
                    setValid(false);
                } else {
                    if (oldPath.equals(newPath)) {
                        setValid(true);
                        return;
                    }
                    if (!StringUtils.isEmpty(newPath)) {
                        if (!checkUserComponentsFolder(file)) {
                            filePathTemp.getTextControl(parent).setText(oldPath);
                        }
                    }
                    setValid(true);
                }
            }

        });

        if (PluginChecker.isPreviewPluginLoaded()) {
            createForDataViewer(parent);
        }
        createForMapper(parent);
        createFortRunJob(parent);
        if (PluginChecker.isJobLetPluginLoaded()) {
            createForJoblet(parent);
        }
        createForComponentAssist(parent);

        parent.pack();
    }

    private boolean checkUserComponentsFolder(File componentsfolder) {
        if (UpdatesHelper.isOldComponent(componentsfolder)) {
            // should select the component folder directly, should for parent folder.
            boolean isContinue = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm",
                    Messages.getString("ComponentsPreferencePage.notValidDirectory")); //$NON-NLS-1$
            return isContinue;
        }
        return true;
    }

    public void propertyChangeForComponents(PropertyChangeEvent event) {
        MessageDialog warningMessageDialog = new MessageDialog(getFieldEditorParent().getShell(),
                Messages.getString("ComponentsPreferencePage.WarningTitle"), null, //$NON-NLS-1$
                Messages.getString("ComponentsPreferencePage.WarningMsg"), MessageDialog.WARNING, //$NON-NLS-1$
                new String[] { Messages.getString("ComponentsPreferencePage.ButtonLabel0") }, 0); //$NON-NLS-1$
        warningMessageDialog.open();

    }

    @Override
    public void init(IWorkbench workbench) {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
     */
    @Override
    protected void initialize() {
        super.initialize();
        // Because should save the value of TalendDesignerPrefConstants.COMPONENT_ASSIST in
        // DesignerPlugin.getDefault().getPreferenceStore()
        enableComponentAssistCheckBoxField.setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
        enableComponentAssistCheckBoxField.load();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        // Because should save the value of TalendDesignerPrefConstants.COMPONENT_ASSIST in
        // DesignerPlugin.getDefault().getPreferenceStore(), also PreferenceInitializer
        enableComponentAssistCheckBoxField.setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
        enableComponentAssistCheckBoxField.loadDefault();
    }

    @Override
    public boolean performOk() {
        boolean flag = super.performOk();
        String newPath = CodeGeneratorActivator.getDefault().getPreferenceStore()
                .getString(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER);
        if ("".equals(oldPath)) { //$NON-NLS-1$
            oldPath = null;
        }
        if ("".equals(newPath)) { //$NON-NLS-1$
            newPath = null;
        }
        DesignerPlugin.getDefault().getPreferenceStore().setValue(TalendDesignerPrefConstants.COMPONENT_ASSIST,
                enableComponentAssistCheckBoxField.getBooleanValue());
        TalendEditorComponentCreationUtil.updateAssistListener();
        if (this.oldPath != newPath) {

            final IRunnableWithProgress runnable = new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask("Loading user component ......", 100); //$NON-NLS-1$
                    Display display = Display.getCurrent();
                    if (display == null) {
                        display = Display.getDefault();
                    }
                    if (display != null) {
                        display.syncExec(new Runnable() {

                            @Override
                            public void run() {
                                // install the new components via P2
                                ComponentsInstallComponent component = LocalComponentInstallHelper.getComponent();
                                if (component != null) {
                                    String newPath = CodeGeneratorActivator.getDefault().getPreferenceStore()
                                            .getString(IComponentPreferenceConstant.USER_COMPONENTS_FOLDER);

                                    if (newPath != null && StringUtils.isNotEmpty(newPath.trim())) {
                                        File componentFolder = new File(newPath.trim());
                                        if (componentFolder.exists()) {
                                            try {
                                                component.setComponentFolder(componentFolder);
                                                if (component.install()) {

                                                    String installedMessages = component.getInstalledMessages();
                                                    String title = Messages.getString("ComponentsPreferencePage_SuccessTitle"); //$NON-NLS-1$
                                                    if (component.needRelaunch()) {

                                                        String warningMessage = Messages
                                                                .getString("ComponentsPreferencePage_SuccessMessage1") //$NON-NLS-1$
                                                                + Messages.getString("ComponentsPreferencePage_SuccessMessage2"); //$NON-NLS-1$
                                                        boolean confirm = MessageDialog.openConfirm(getShell(), title,
                                                                installedMessages + '\n' + '\n' + warningMessage);

                                                        if (confirm) {
                                                            PlatformUI.getWorkbench().restart();
                                                        }
                                                    } else {
                                                        MessageDialog.openInformation(getShell(), title, installedMessages);
                                                    }
                                                }
                                                if (StringUtils.isNotEmpty(component.getFailureMessage())) {
                                                    MessageDialog.openError(getShell(),
                                                            Messages.getString(
                                                                    "ComponentsPreferencePage.installComponentsFailure"), //$NON-NLS-1$
                                                            component.getFailureMessage());
                                                }
                                            } finally {
                                                // after install, clear the setting for service.
                                                component.setComponentFolder(null);
                                            }
                                        }
                                    }
                                }

                                // components will be reloaded when refreshTemplates;
                                // IComponentsFactory components = ComponentsFactoryProvider.getInstance();
                                // components.loadUserComponentsFromComponentsProviderExtension();
                                CorePlugin.getDefault().getLibrariesService().resetModulesNeeded();
                                monitor.worked(50);
                                // ComponentUtilities.updatePalette();
                                ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault()
                                        .getService(ICodeGeneratorService.class);
                                service.refreshTemplates();
                            }
                        });

                    }
                }
            };

            final ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
            try {
                dialog.run(true, true, runnable);
            } catch (InvocationTargetException e) {
                ExceptionHandler.process(e);
            } catch (InterruptedException e) {
                ExceptionHandler.process(e);
            }

            this.oldPath = newPath;

        }

        return flag;
    }
}
