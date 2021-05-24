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
package org.talend.designer.core.ui.views.contexts;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.EditorPart;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.context.ContextComposite;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.jobsettings.JobSettings;
import org.talend.designer.core.ui.views.properties.ComponentSettings;

/**
 * A concrete class of ContextComposite for the context view. <br/>
 *
 */
public class ContextViewComposite extends ContextComposite {

    /**
     * bqian ContextComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public ContextViewComposite(Composite parent, ContextsView contextView) {
        super(parent, false);
        // CSS
        CoreUIPlugin.setCSSClass(this, this.getClass().getSimpleName());
    }

    @Override
    public void setPart(EditorPart part) {
        super.setPart(part);
        refreshRelationship();
        // for bug 13730
    }

    private void refreshRelationship() {
        IProcess process = getProcess();
        if (process != null) {
            setReadOnly(process.isReadOnly());
        }

        DesignerPlugin.getDefault().getRunProcessService().refreshView();
    }

    @Override
    public void refresh() {
        refreshRelationship();
        super.refresh();
    }

    @Override
    public CommandStack getCommandStack() {
        if (part == null) {
            return null;
        }
        if (((AbstractMultiPageTalendEditor) part).getTalendEditor() == null) {
            return null;
        }
        return (CommandStack) (((AbstractMultiPageTalendEditor) part).getTalendEditor().getAdapter(CommandStack.class));
    }

    @Override
    public IContextManager getContextManager() {
        return getProcess() == null ? null : getProcess().getContextManager();
    }

    @Override
    public IProcess2 getProcess() {
        if (part == null || !(part instanceof AbstractMultiPageTalendEditor)) {
            return null;
        }
        if (((AbstractMultiPageTalendEditor) part).getTalendEditor() == null) {
            return null;
        }
        return ((AbstractMultiPageTalendEditor) part).getTalendEditor().getProcess();
    }

    private Process getJob() {
        return (Process) getProcess();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.context.ContextComposite#switchSettingsView()
     */
    @Override
    public void switchSettingsView(String oldName, String newName) {
        if (UpdateContextVariablesHelper.updateProcessForRenamed(getProcess(), oldName, newName)) {
            JobSettings.switchToCurJobSettingsView();
            ComponentSettings.switchToCurComponentSettingsView();
        }
    }

}
