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
package org.talend.designer.core.ui.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.utils.platform.PluginChecker;
import org.talend.core.model.process.EConnectionType;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.AbstractTalendEditor;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainer;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletContainerPart;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.utils.DesignerColorUtils;

/**
 * ggu class global comment. Detailled comment
 */
public class DesignerColorsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public DesignerColorsPreferencePage() {
        super(GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        Composite parent = new Composite(getFieldEditorParent(), SWT.NONE);
        parent.setLayout(new GridLayout());
        GridData data = new GridData(GridData.FILL_BOTH);
        parent.setLayoutData(data);

        createEditorFieldEditors(parent);
        createSubjobFieldEditors(parent);
        boolean loadJoblet = PluginChecker.isPluginLoaded("org.talend.designer.joblet");//$NON-NLS-1$
        if (loadJoblet) {
            createJobletGroupFieldEditors(parent);
        }
        boolean loadMR = PluginChecker.isPluginLoaded("org.talend.designer.mapreduce");//$NON-NLS-1$
        if (loadMR) {
            createMRGroupFieldEditors(parent);
        }
        createConnectionFieldEditors(parent);
    }

    private void createSubjobFieldEditors(Composite parent) {
        Group subjobGroup = new Group(parent, SWT.NULL);
        subjobGroup.setText(Messages.getString("DesignerPreferencePage.SubjobColorGroup")); //$NON-NLS-1$
        subjobGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addField(new ColorFieldEditor(DesignerColorUtils.SUBJOB_TITLE_COLOR_NAME,
                Messages.getString("DesignerPreferencePage.SubjobTitleColorLabel"), subjobGroup)); //$NON-NLS-1$

        addField(new ColorFieldEditor(DesignerColorUtils.SUBJOB_COLOR_NAME,
                Messages.getString("DesignerPreferencePage.SubjobColorLabel"), subjobGroup)); //$NON-NLS-1$
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 10;
        subjobGroup.setLayout(layout);
    }

    private void createJobletGroupFieldEditors(Composite parent) {
        Group jobletGroup = new Group(parent, SWT.NULL);
        jobletGroup.setText(Messages.getString("DesignerPreferencePage.JobletColorGroup")); //$NON-NLS-1$
        jobletGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addField(new ColorFieldEditor(DesignerColorUtils.JOBLET_COLOR_NAME,
                Messages.getString("DesignerPreferencePage.JobletGroupColorLabel"), jobletGroup)); //$NON-NLS-1$

        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 10;
        jobletGroup.setLayout(layout);
    }

    private void createMRGroupFieldEditors(Composite parent) {
        Group mrGroup = new Group(parent, SWT.NULL);
        mrGroup.setText(Messages.getString("DesignerPreferencePage.MRColorGroup")); //$NON-NLS-1$
        mrGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addField(new ColorFieldEditor(DesignerColorUtils.MRGROUP_COLOR_NAME,
                Messages.getString("DesignerPreferencePage.MRGroupColorLabel"), mrGroup)); //$NON-NLS-1$

        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 10;
        mrGroup.setLayout(layout);
    }

    private void createEditorFieldEditors(Composite parent) {
        Group jobBackgroundGroup = new Group(parent, SWT.NULL);
        jobBackgroundGroup.setText(Messages.getString("DesignerPreferencePage.JobDesignerEditorBackgroundColorLabel")); //$NON-NLS-1$
        jobBackgroundGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        addField(new ColorFieldEditor(DesignerColorUtils.JOBDESIGNER_EGITOR_BACKGROUND_COLOR_NAME,
                Messages.getString("DesignerPreferencePage.DesignerEditorBackgroundColor"), jobBackgroundGroup)); //$NON-NLS-1$

        addField(new ColorFieldEditor(DesignerColorUtils.READONLY_BACKGROUND_COLOR_NAME,
                Messages.getString("DesignerPreferencePage.ReadonlyBackgroundColor"), jobBackgroundGroup)); //$NON-NLS-1$
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 10;
        jobBackgroundGroup.setLayout(layout);
    }

    private void createConnectionFieldEditors(Composite parent) {
        Group connGroup = new Group(parent, SWT.NULL);
        connGroup.setText(Messages.getString("DesignerColorsPreferencePage.ConnectionColorGroup")); //$NON-NLS-1$
        connGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, true);
        layout.marginLeft = 10;
        connGroup.setLayout(layout);

        Label message = new Label(connGroup, SWT.NULL);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        data.minimumWidth = 400;
        data.heightHint = 20;
        message.setLayoutData(data);
        message.setText(Messages.getString("DesignerColorsPreferencePage.ConnectionColorMessages")); //$NON-NLS-1$

        Composite left = new Composite(connGroup, SWT.NULL);
        left.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite right = new Composite(connGroup, SWT.NULL);
        right.setLayoutData(new GridData(GridData.FILL_BOTH));

        EConnectionType[] values = EConnectionType.values();
        for (int i = 0; i < values.length; i++) {
            Composite comp = left;
            if (i % 2 > 0) {
                comp = right;
            }
            addField(new ColorFieldEditor(DesignerColorUtils.getPreferenceConnectionName(values[i]),
                    values[i].getDefaultMenuName(), comp));
        }

    }

    @Override
    public boolean performOk() {
        boolean performOk = super.performOk();
        switchToCurrentColor();
        return performOk;
    }

    @Override
    protected void performApply() {
        super.performApply();
        switchToCurrentColor();
    }

    /**
     * zli Comment method "switchToCurrentColor".
     */
    private void switchToCurrentColor() {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {
            IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
            if (activePage != null) {
                for (IEditorReference ref : activePage.getEditorReferences()) {
                    IEditorPart editor = ref.getEditor(true);
                    if (editor instanceof AbstractMultiPageTalendEditor) {
                        AbstractMultiPageTalendEditor pageEditor = (AbstractMultiPageTalendEditor) editor;
                        AbstractTalendEditor talendEditor = pageEditor.getTalendEditor();
                        ProcessPart processPart = talendEditor.getProcessPart();
                        processPart.ajustReadOnly();
                        changeMRGroupColor(processPart);
                    }
                }
            }
        }
    }

    private void changeMRGroupColor(ProcessPart processPart) {
        for (Object o : processPart.getChildren()) {
            if (o instanceof SubjobContainerPart) {
                for (Object child : ((SubjobContainerPart) o).getChildren()) {
                    if (child instanceof JobletContainerPart) {
                        JobletContainer jCon = (JobletContainer) ((JobletContainerPart) child).getModel();
                        jCon.setPropertyValue(JobletContainer.UPDATE_JOBLET_DISPLAY, DesignerColorUtils.getPreferenceMRGroupRGB(
                                DesignerColorUtils.MRGROUP_COLOR_NAME, DesignerColorUtils.MR_COLOR));
                        jCon.setPropertyValue(JobletContainer.UPDATE_JOBLET_DISPLAY, DesignerColorUtils.getPreferenceMRGroupRGB(
                                DesignerColorUtils.JOBLET_COLOR_NAME, DesignerColorUtils.JOBLET_COLOR));
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {

    }

}
