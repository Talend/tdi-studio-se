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
package org.talend.designer.core.ui.views.contexts;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorPart;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.ui.context.view.AbstractContextView;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.RepositoryPlugin;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 */
public class ContextsView extends AbstractContextView implements PropertyChangeListener {

    public ContextsView() {
        ProxyRepositoryFactory.getInstance().addPropertyChangeListener(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.context.view.AbstractContextView#getContextManager()
     */
    @Override
    protected IContextManager getContextManager() {
        return ((AbstractMultiPageTalendEditor) part).getProcess().getContextManager();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.context.view.AbstractContextView#getPart()
     */
    @Override
    protected void getPart() {
        final IEditorPart activeEditor = getSite().getPage().getActiveEditor();
        if (activeEditor instanceof AbstractMultiPageTalendEditor) {
            part = (AbstractMultiPageTalendEditor) activeEditor;
        } else {
            part = null;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.context.view.AbstractContextView#setEditorDirty(org.eclipse.ui.part.EditorPart)
     */
    @Override
    protected void setEditorDirty(EditorPart part) {
        ((AbstractMultiPageTalendEditor) part).getTalendEditor().setDirty(Boolean.TRUE);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.context.view.AbstractContextView#createContextComposite()
     */
    @Override
    protected void createContextComposite(Composite parent) {
        contextComposite = new ContextViewComposite(parent, this);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.context.view.AbstractContextView#handleDrop(org.talend.core.model.properties.ContextItem,
     * java.util.Set, org.eclipse.ui.IEditorInput)
     */
    @Override
    protected boolean handleDropContext(ContextItem contextItem, Set<String> contextSet, IEditorInput editorInput) {
        boolean created = false;
        if (editorInput instanceof JobEditorInput) {
            JobEditorInput jobInput = (JobEditorInput) editorInput;
            IProcess2 process = jobInput.getLoadedProcess();
            IContextManager contextManager = process.getContextManager();
            // context group will reflect absolutely if no context variable in contextViewer
            if (!ConnectionContextHelper.containsVariable(contextManager)) {
                // for bug 15608
                ConnectionContextHelper.addContextVarForJob(process, contextItem, contextManager);
                // ConnectionContextHelper.checkAndAddContextsVarDND(contextItem,
                // contextManager);
                created = true;
            } else {
                Set<String> addedContext = ConnectionContextHelper.checkAndAddContextVariables(contextItem, contextSet,
                        contextManager, false, Collections.EMPTY_MAP);
                if (addedContext != null && addedContext.size() > 0) {
                    ConnectionContextHelper.addContextVarForJob(process, contextItem, addedContext);
                    created = true;
                }
            }
        }
        return created;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.context.view.AbstractContextView#swithToContextView()
     */
    @Override
    protected void swithToContextView() {
        RepositoryPlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.context.view.AbstractContextView#setCompositeReadonly(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void setCompositeReadonly(IEditorInput editorInput) {
        if (editorInput != null && editorInput instanceof JobEditorInput) {
            JobEditorInput jobInput = (JobEditorInput) editorInput;
            IProcess2 process = jobInput.getLoadedProcess();
            if (process != null) {
                contextComposite.setReadOnly(process.isReadOnly());
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("view_refresh")) { //$NON-NLS-1$
            swithToContextView();
        }

    }

    @Override
    public void dispose() {
        ProxyRepositoryFactory.getInstance().removePropertyChangeListener(this);
        super.dispose();
    }
}
