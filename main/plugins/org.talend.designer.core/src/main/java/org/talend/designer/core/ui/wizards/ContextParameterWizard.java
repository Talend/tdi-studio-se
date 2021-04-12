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
package org.talend.designer.core.ui.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.IContextParameter;
import org.talend.core.ui.context.view.Contexts;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;

/**
 * Wizard for the creation of a new IContextParameter. <br/>
 *
 * $Id$
 *
 */
public class ContextParameterWizard extends Wizard {

    /** Manager for all contexts in the project. */
    private IContextManager contextManager;

    /** Edited parameter. */
    private IContextParameter parameter;

    private ContextParameterPage ctxPrmPage;

    /**
     * Constructs a new ContextParameterWizard.
     */
    public ContextParameterWizard(IContextManager contextManager, IContextParameter parameter) {
        super();

        this.contextManager = contextManager;
        this.parameter = parameter;

        setWindowTitle(Messages.getString("ContextParameterWizard.title")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(DesignerPlugin.getImageDescriptor("icons/ctxprm_wiz.png")); //$NON-NLS-1$
        setNeedsProgressMonitor(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        ctxPrmPage = new ContextParameterPage(contextManager);
        ctxPrmPage.setParameter(parameter, contextManager.getListContext());
        addPage(ctxPrmPage);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        // set source to built-in
        parameter.setSource(IContextParameter.BUILT_IN);

        for (IContext context : contextManager.getListContext()) {
            if (parameter.getType().equals(JavaTypesManager.FILE.getId())
                    || parameter.getType().equals(JavaTypesManager.DIRECTORY.getId())) {
                parameter.setValue(PathUtils.getPortablePath(parameter.getValue()));
            }
            context.getContextParameterList().add(parameter.clone());
        }
        contextManager.fireContextsChangedEvent();
        Contexts.switchToCurContextsView();
        return true;
    }

}
