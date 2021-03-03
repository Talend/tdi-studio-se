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
package org.talend.designer.core.ui.views.statsandlogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
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
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.cmd.LoadProjectSettingsCommand;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.projectsetting.ElementParameter2ParameterType;
import org.talend.designer.core.ui.projectsetting.ProjectSettingManager;
import org.talend.designer.core.ui.views.jobsettings.AbstractPreferenceComposite;
import org.talend.repository.ProjectManager;

/**
 * ftang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class StatsAndLogsComposite extends AbstractPreferenceComposite {

    private ParametersType pType;

    /**
     * ftang StatAndLogsComposite constructor comment.
     *
     * @param parentComposite
     * @param styles
     * @param section
     * @param element
     */
    public StatsAndLogsComposite(Composite parentComposite, int styles, EComponentCategory section, Element element) {
        super(parentComposite, styles, section, element, true);
        setDialogTitle(Messages.getString("StatsAndLogsComposite.StatsLogsSettings")); //$NON-NLS-1$
        // achen modify to fix 0005993 change button's text
        isUsingProjectSetting = true;
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
        // TDI-28709: by default the main composite is opposite with the "STATANDLOG_USE_PROJECT_SETTINGS"
        Object value = ElementParameter2ParameterType.getParameterValue(elem,
                EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName());
        if (value != null && value instanceof Boolean) {
            Boolean v = (Boolean) value;
            setMainCompositeEnable(!v.booleanValue());
        }
    }

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
        ElementParameter2ParameterType.setParameterValue(pType, EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName(),
                Boolean.valueOf(flag));
        PropertyChangeCommand cmd = new PropertyChangeCommand(elem, EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName(),
                Boolean.valueOf(flag));
        getCommandStack().execute(cmd);
        updateContextValue(flag);
        // hywang add for bug 7587
        IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (activeEditor != null && activeEditor instanceof AbstractMultiPageTalendEditor) {
            AbstractTalendEditor workbenchPart = ((AbstractMultiPageTalendEditor) activeEditor).getTalendEditor();
            Boolean currentEditorDirty = workbenchPart.isDirty();
            if (!currentEditorDirty) {
                workbenchPart.setDirty(true);
            }
        }

        if (flag) {
            useProjectSetting();
        }
    }

    @Override
    protected void onReloadPreference() {
        // StatsAndLogsViewHelper.reloadValuesFromPreferencePage(elem, StatsAndLogsComposite.this);
        // achen modify to fix 0005991
        // ProjectSettingManager.reloadStatsAndLogFromProjectSettings(elem,
        // ProjectManager.getInstance().getCurrentProject(),
        // StatsAndLogsComposite.this);
        if (elem instanceof Process) {
            Process process = (Process) elem;
            LoadProjectSettingsCommand command = new LoadProjectSettingsCommand(process,
                    EParameterName.STATANDLOG_USE_PROJECT_SETTINGS.getName(), Boolean.TRUE);

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
        // StatsAndLogsViewHelper.saveValuesToPreferencePage(elem, StatsAndLogsComposite.this);
        // achen modify to fix 0005991
        ProjectSettingManager.saveStatsAndLogToProjectSettings(elem, ProjectManager.getInstance().getCurrentProject());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.core.ui.views.jobsettings.AbstractPreferenceComposite#needApplyToChildren()
     */
    @Override
    protected boolean needApplyToChildren() {
        return true;
    }

    private boolean judgeOldSelection(boolean oldDirty, boolean selection) {
        Boolean a = new Boolean(oldDirty);
        if (a.compareTo(selection) == 0) {
            return false;
        }
        return true;
    }
}
