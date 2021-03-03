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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchShortcutExtension;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.repository.ui.views.IJobSettingsView;

/**
 * yzhang class global comment. Detailled comment
 */
public class TalendLaunchShortcutAction extends Action {

    private String fMode;

    private LaunchShortcutExtension fShortcut;

    /**
     * Constructor for LaunchShortcutAction.
     */
    public TalendLaunchShortcutAction(String mode, LaunchShortcutExtension shortcut) {
        super(shortcut.getLabel(), shortcut.getImageDescriptor());
        fMode = mode;
        fShortcut = shortcut;
        updateEnablement();
    }

    /**
     * Runs with either the active editor or workbench selection.
     *
     * @see IAction#run()
     */
    public void run() {
        // IWorkbenchWindow wb = DebugUIPlugin.getActiveWorkbenchWindow();
        IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (wb != null) {
            IWorkbenchPage page = wb.getActivePage();
            if (page != null) {
                if (page.getActivePart() == page.getActiveEditor()) {
                    IEditorPart editor = page.getActiveEditor();
                    if (editor != null) {
                        fShortcut.launch(editor, fMode);
                    }
                } else if (page.getActivePart() instanceof IJobSettingsView) {
                    ISelection selection = ((IJobSettingsView) page.getActivePart()).getSelection();
                    fShortcut.launch(selection, fMode);
                } else {
                    ISelection selection = page.getSelection();
                    if (selection instanceof IStructuredSelection) {
                        fShortcut.launch(selection, fMode);
                    }
                }
            }
        }
    }

    /**
     * Since these actions are re-created each time the run/debug as menu is filled, the enablement of this action is
     * static.
     */
    private void updateEnablement() {
        // IWorkbenchWindow wb = DebugUIPlugin.getActiveWorkbenchWindow();
        IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        boolean enabled = false;
        if (wb != null) {
            IWorkbenchPage page = wb.getActivePage();
            if (page != null) {
                ISelection selection = page.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                    try {
                        // check enablement logic, if any
                        Expression expression = fShortcut.getShortcutEnablementExpression();
                        if (expression == null) {
                            enabled = !structuredSelection.isEmpty();
                        } else {
                            List list = structuredSelection.toList();
                            IEvaluationContext context = new EvaluationContext(null, list);
                            context.addVariable("selection", list); //$NON-NLS-1$
                            enabled = fShortcut.evalEnablementExpression(context, expression);
                        }
                    } catch (CoreException e) {
                    }
                } else {
                    IEditorPart editor = page.getActiveEditor();
                    if (editor != null) {
                        enabled = true;
                    }
                }
            }
        }
        setEnabled(enabled);
    }

}
