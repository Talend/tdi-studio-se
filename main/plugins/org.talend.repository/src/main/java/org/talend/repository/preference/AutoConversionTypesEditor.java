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
package org.talend.repository.preference;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.model.metadata.types.AutoConversionType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.AutoConversionTypeModel;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.utils.AutoConvertTypesUtils;

/**
 *
 * created by hcyi on Aug 18, 2016 Detailled comment
 *
 */
public class AutoConversionTypesEditor extends FieldEditor {

    public static final String ID = "org.talend.repository.preference.AutoConversionTypesEditor"; //$NON-NLS-1$

    public Button enableBtn;

    public AutoConversionTypeModel typeModel;

    public AutoConversionTypesEditorView tableEditorView;

    public IPreferenceStore preferenceStore = RepositoryPlugin.getDefault().getPreferenceStore();

    public AutoConversionTypesEditor(String name, Composite parent) {
        init(name, Messages.getString("AutoConversionTypesEditor.title"));//$NON-NLS-1$
        createControl(parent);
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Composite parentComposite = new Composite(parent, SWT.NULL);
        GridLayout parentCompLayout = new GridLayout(1, false);
        parentCompLayout.marginWidth = 0;
        parentCompLayout.marginHeight = 0;
        parentComposite.setLayout(parentCompLayout);
        parentComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        enableBtn = new Button(parentComposite, SWT.CHECK);
        enableBtn.setText(Messages.getString("AutoConversionTypesEditor.Button.enable"));//$NON-NLS-1$

        Label noteLabel = new Label(parentComposite, SWT.NONE);
        noteLabel.setText(Messages.getString("AutoConversionTypesEditor.Label.messages"));//$NON-NLS-1$

        typeModel = new AutoConversionTypeModel();
        tableEditorView = new AutoConversionTypesEditorView(parentComposite, typeModel);
        Composite tableComposite = tableEditorView.getMainComposite();
        GridData tableData = new GridData(GridData.FILL_BOTH);
        tableData.heightHint = 300;
        tableComposite.setLayoutData(tableData);
        typeModel.setModifiedBeanListenable(tableEditorView.getTableViewerCreator());
        //
        addListeners();
        init();
    }

    public void init() {
        enableBtn.setSelection(preferenceStore.getBoolean(AutoConvertTypesUtils.ENABLE_AUTO_CONVERSION));
        tableEditorView.setReadOnly(!enableBtn.getSelection());
    }

    protected void addListeners() {
        enableBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (enableBtn.getSelection()) {
                    tableEditorView.setReadOnly(false);
                    doLoad();
                } else {
                    tableEditorView.setReadOnly(true);
                }
                preferenceStore.setValue(AutoConvertTypesUtils.ENABLE_AUTO_CONVERSION, enableBtn.getSelection());
            }
        });
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {

    }

    @Override
    protected void doLoad() {
        if (enableBtn.getSelection()) {
            List<AutoConversionType> beanList = AutoConvertTypesUtils.load(AutoConvertTypesUtils.getTypeFile());
            tableEditorView.getTableViewerCreator().setInputList(beanList);
        }
    }

    @Override
    protected void doLoadDefault() {
        super.load();
    }

    @Override
    protected void doStore() {
        if (!enableBtn.getSelection()) {
            return;
        }
        RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit(
                Messages.getString("AutoConversionTypesEditor.doStore.title")) { //$NON-NLS-1$

            @Override
            public void run() throws PersistenceException {
                IWorkspaceRunnable runnable = new IWorkspaceRunnable() {

                    @Override
                    public void run(final IProgressMonitor monitor) throws CoreException {
                        applyChange();
                    }
                };
                // unlockObject();
                // alreadyEditedByUser = true; // to avoid 2 calls of unlock
                IWorkspace workspace = ResourcesPlugin.getWorkspace();
                try {
                    ISchedulingRule schedulingRule = workspace.getRoot();
                    // the update the project files need to be done in the workspace runnable to
                    // avoid all
                    // notification
                    // of changes before the end of the modifications.
                    workspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, null);
                } catch (CoreException e1) {
                    MessageBoxExceptionHandler.process(e1.getCause());
                }
            }
        };
        repositoryWorkUnit.setAvoidSvnUpdate(true);
        repositoryWorkUnit.setAvoidUnloadResources(true);
        ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);
    }

    private void applyChange() {
        try {
            IProject project = ResourceUtils.getProject(ProjectManager.getInstance().getCurrentProject());
            IFolder prefsSettingFolder = ResourceUtils.getFolder(project, RepositoryConstants.SETTING_DIRECTORY, false);
            AutoConvertTypesUtils.save(typeModel.getBeansList(), AutoConvertTypesUtils.getTypeFile());
            prefsSettingFolder.refreshLocal(IResource.DEPTH_ONE, null);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        } catch (PersistenceException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
