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
package org.talend.designer.core.ui.views.jobsettings;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.model.utils.emf.talendfile.ParametersType;
import org.talend.designer.core.ui.editor.cmd.LoadProjectSettingsCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.projectsetting.ElementParameter2ParameterType;
import org.talend.designer.core.ui.projectsetting.ProjectSettingManager;
import org.talend.repository.ProjectManager;

/**
 * DOC chuang class global comment. Detailled comment
 */
public class ExtraComposite extends AbstractPreferenceComposite {

    boolean isClicked;

    private ParametersType pType;

    /**
     * DOC chuang ExtraComposite constructor comment.
     *
     * @param parentComposite
     * @param styles
     * @param section
     * @param element
     * @param isCompactView
     */
    public ExtraComposite(Composite parentComposite, int styles, EComponentCategory section, Element element,
            boolean isCompactView) {
        super(parentComposite, styles, section, element, isCompactView);
        setDialogTitle(Messages.getString("ExtraComposite.ImplicitContextSettings")); //$NON-NLS-1$
        Process process = (Process) elem;
        Item pItem = process.getProperty().getItem();
        if (pItem instanceof ProcessItem) {
            pType = ((ProcessItem) pItem).getProcess().getParameters();
        } else if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
            ITestContainerProviderService testContainerService = (ITestContainerProviderService) GlobalServiceRegister
                    .getDefault().getService(ITestContainerProviderService.class);
            if (testContainerService != null) {
                pType = testContainerService.getTestContainerProcess(pItem).getParameters();
            }
        }
        // achen modify to fix 0005993
        isUsingProjectSetting = true;
        // wzhang modified to fixed bug 8218
        boolean readOnly = element.isReadOnly();
        parentComposite.setEnabled(!readOnly);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.views.properties.MultipleThreadDynamicComposite#addComponents(boolean)
     */
    @Override
    public void addComponents(boolean forceRedraw, boolean reInitialize, int height) {
        // TODO Auto-generated method stub
        super.addComponents(forceRedraw, reInitialize, height);
        // achen add to fix 0005991 & 0005993 when reload
        Object value = ElementParameter2ParameterType.getParameterValue(elem,
                EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName());
        if (value != null && value instanceof Boolean) {
            Boolean v = (Boolean) value;
            useProjectSetting.setSelection(v.booleanValue());
            setMainCompositeEnable(!v.booleanValue());
            topComposite.setEnabled(true);
            if (v.booleanValue()) {
                if (elem == null) {
                    return;
                }
                // achen modify to fix 0005991& 0005993
                ProjectSettingManager.reloadImplicitValuesFromProjectSettings(elem, ProjectManager.getInstance()
                        .getCurrentProject(), ExtraComposite.this);
                refresh();
            }
        }
        if (useProjectSetting != null) {
            useProjectSetting.removeSelectionListener(selectionListener);
            useProjectSetting.addSelectionListener(selectionListener);
        }
    }

    SelectionAdapter selectionListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
            useProjectSettingButtonClick();
        }
    };

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.views.jobsettings.AbstractPreferenceComposite#useProjectSettingButtonClick()
     */
    @Override
    protected void useProjectSettingButtonClick() {
        boolean flag = useProjectSetting.getSelection();
        setMainCompositeEnable(!flag);
        topComposite.setEnabled(true);
        ElementParameter2ParameterType.setParameterValue(pType, EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName(),
                Boolean.valueOf(flag));
        PropertyChangeCommand cmd = new PropertyChangeCommand(elem,
                EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName(), Boolean.valueOf(flag));
        getCommandStack().execute(cmd);
        updateContextValue(flag);
        if (flag) {
            useProjectSetting();
        }
    }

    @Override
    protected void onReloadPreference() {
        // ImplicitContextLoadHelper.reloadValuesFromPreferencePage(elem, ExtraComposite.this);
        // achen modify to fix 0005993
        // ProjectSettingManager.reloadImplicitValuesFromProjectSettings(elem,
        // ProjectManager.getInstance().getCurrentProject(),
        // ExtraComposite.this);
        if (elem instanceof Process) {
            Process process = (Process) elem;
            LoadProjectSettingsCommand command = new LoadProjectSettingsCommand(process,
                    EParameterName.IMPLICITCONTEXT_USE_PROJECT_SETTINGS.getName(), Boolean.TRUE);

            boolean executed = false;
            IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
            if (designerCoreUIService != null) {
                executed = designerCoreUIService.executeCommand(process, command);
            }

            if (!executed) {
                command.execute();
            }

        }
    }

    @Override
    protected void onSavePreference() {
        // ImplicitContextLoadHelper.saveValuesToPreferencePage(elem, ExtraComposite.this);
        // achen modify to fix 0005993
        ProjectSettingManager.saveImplicitValuesToProjectSettings(elem, ProjectManager.getInstance().getCurrentProject());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.views.jobsettings.AbstractPreferenceComposite#needApplyToChildren()
     */
    @Override
    protected boolean needApplyToChildren() {
        return false;
    }
}
