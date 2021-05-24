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
package org.talend.repository.plugin.integration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.talend.commons.utils.workbench.extensions.ExtensionImplementationProvider;
import org.talend.commons.utils.workbench.extensions.ExtensionPointLimiterImpl;
import org.talend.commons.utils.workbench.extensions.IExtensionPointLimiter;
import org.talend.core.CorePlugin;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: BoundingActions.java 下午04:56:43 2007-10-24 +0000 (2007-10-24) yzhang $
 *
 */
public class BindingActions {

    public static boolean executed;

    private final List<IAction> actions = new ArrayList<IAction>();

    public static final IExtensionPointLimiter GLOBAL_ACTIONS = new ExtensionPointLimiterImpl("org.talend.core.global_actions", //$NON-NLS-1$
            "GlobalAction"); //$NON-NLS-1$

    public void bind() {

        boolean isRCPModel = CorePlugin.getDefault().getRepositoryService().isRCPMode();

        if (!isRCPModel) {
            createActions();
            registerActions();
        }
        executed = true;

    }

    /**
     * DOC smallet Comment method "registerActions".
     */
    private void registerActions() {

        IContextService contextService = (IContextService) PlatformUI.getWorkbench().getAdapter(IContextService.class);
        contextService.activateContext("talend.global"); //$NON-NLS-1$

        IWorkbench workbench = PlatformUI.getWorkbench();
        IHandlerService handlerService = (IHandlerService) workbench.getService(IHandlerService.class);

        IHandler handler;
        for (IAction action : actions) {
            handler = new ActionHandler(action);
            handlerService.activateHandler(action.getActionDefinitionId(), handler);
        }
    }

    /**
     * DOC smallet Comment method "createActions".
     */
    private void createActions() {

        List<IAction> list = ExtensionImplementationProvider.getInstance(GLOBAL_ACTIONS);
        actions.addAll(list);

    }

}
