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
package org.talend.designer.core.ui.editor.dependencies;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.dependencies.controls.SearchControl;
import org.talend.designer.core.ui.editor.dependencies.util.ResourceDependenciesUtil;

public class JobDependenciesEditor extends EditorPart implements IJobDependenciesChangedListener, IMessagePart {

    private final CommandStack commandStack;

    private final boolean isReadOnly;

    private Label statusLabel;

    // show all datas

    private ManageResourcePanel manageRouteResourcePanel;

    public JobDependenciesEditor(final AbstractMultiPageTalendEditor editor, boolean isReadOnly) {
        commandStack = editor.getTalendEditor().getCommandStack();
        this.isReadOnly = isReadOnly;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setInput(input);
        setSite(site);
    }

    @Override
    public void createPartControl(Composite parent) {
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        parent.setLayout(new GridLayout());

        // create search group, hide button and refresh button
        Composite toolsPanel = toolkit.createComposite(parent);
        toolsPanel.setLayout(new GridLayout(4, false));
        toolsPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolsPanel.setBackgroundMode(SWT.INHERIT_FORCE);

        toolkit.createLabel(toolsPanel, Messages.getString("JobDependenciesEditor.filterLabel")); //$NON-NLS-1$

        SearchControl searchComposite = new SearchControl(toolsPanel, SWT.NONE);
        searchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        searchComposite.setActiveImage(DesignerPlugin.getImageDescriptor("icons/dependencies/highlight_rem.gif").createImage());
        searchComposite.setDeactiveImage(DesignerPlugin.getImageDescriptor("icons/dependencies/gray_rem.gif").createImage());
        Text filterText = searchComposite.getText();
        /*
         * Button hideBuiltIn = toolkit.createButton(toolsPanel,
         * Messages.getString("JobDependenciesEditor.hideBuiltInItems"), //$NON-NLS-1$ SWT.CHECK);
         */

        Button refreshBtn = toolkit.createButton(toolsPanel, null, SWT.PUSH);
        refreshBtn.setImage(DesignerPlugin.getImageDescriptor("icons/dependencies/refresh.gif").createImage());
        refreshBtn.setToolTipText(Messages.getString("JobDependenciesEditor.refreshDependenciesTooltip")); //$NON-NLS-1$
        refreshBtn.setEnabled(!isReadOnly());

        // create data tables
        ScrolledComposite top = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm mainForm = new SashForm(top, SWT.VERTICAL);
        manageRouteResourcePanel = createResourceTableViewer(mainForm, toolkit,
                Messages.getString("JobDependenciesEditor.Resources")); //$NON-NLS-1$

        top.setExpandHorizontal(true);
        top.setExpandVertical(true);
        top.setContent(mainForm);

        // create status
        Composite statusComposite = toolkit.createComposite(parent);
        statusComposite.setLayout(new GridLayout(2, false));
        statusComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        statusLabel = toolkit.createLabel(statusComposite, null);
        statusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label shortCuts = toolkit.createLabel(statusComposite, Messages.getString("JobDependenciesEditor.KeyBindingWord"),
                SWT.SHADOW_OUT); // $NON-NLS-1$
        shortCuts.setEnabled(false);

        manageRouteResourcePanel.setProcess(getJobEditorInput().getLoadedProcess());
        manageRouteResourcePanel.setCommandStack(commandStack);
        // add filter listener
        filterText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                final String filterString = ((Text) e.widget).getText().trim();
                manageRouteResourcePanel.setFilterString(filterString);
            }
        });

        // add hide listener
        // hideBuiltIn.addSelectionListener(new SelectionAdapter() {
        //
        // @Override
        // public void widgetSelected(SelectionEvent e) {
        // final boolean show = ((Button) e.widget).getSelection();
        // manageRouteResourcePanel.setShowBuiltIn(!show);
        // }
        // });

        refreshBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateInput();
            }
        });
    }

    public void setMessage(String message) {
        if (message == null) {
            message = ""; //$NON-NLS-1$
        }
        statusLabel.setText(message);
        statusLabel.setToolTipText(message);
    }

    private JobEditorInput getJobEditorInput() {
        return (JobEditorInput) getEditorInput();
    }

    private void updateInput() {
        manageRouteResourcePanel
                .setInput(ResourceDependenciesUtil.getResourceDependencies(getJobEditorInput().getLoadedProcess()));
    }

    private ManageResourcePanel createResourceTableViewer(Composite parent, FormToolkit toolkit, String title) {
        Section section = toolkit.createSection(parent, Section.TITLE_BAR);
        section.setText(title);
        final ManageResourcePanel c = new ManageResourcePanel(section, isReadOnly(), this, this);
        section.setClient(c);
        toolkit.adapt(c);
        return c;
    }

    @Override
    public void dependencesChanged(Composite source) {
        final Command cmd;
        cmd = new Command() {

            @Override
            public void execute() {

                ResourceDependenciesUtil.saveResourceDependency(getJobEditorInput().getLoadedProcess().getAdditionalProperties(),
                        manageRouteResourcePanel.getInput());
                ResourceDependenciesUtil.setContextVarForResources(getJobEditorInput().getLoadedProcess(),
                        manageRouteResourcePanel.getInput());
                RelationshipItemBuilder.getInstance().addOrUpdateItem(getJobEditorInput().getItem());

                IDesignerCoreService designerCoreService = CoreRuntimePlugin.getInstance().getDesignerCoreService();
                if (designerCoreService != null) {
                    designerCoreService.switchToCurContextsView();
                }

            }
        };
        commandStack.execute(cmd);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void setFocus() {
        // importPackageViewer.getTableViewer().getTable().setFocus();
        // update all tables input
        updateInput();
        setMessage(null);
    }

    private boolean isReadOnly() {
        return isReadOnly;
    }

}
