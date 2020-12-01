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
package org.talend.designer.core.ui.action;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.editor.JobEditorInput;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * DOC wchen class global comment. Detailled comment
 */
public abstract class AbstractProcessAction extends AContextualAction {

    public AbstractProcessAction() {
        super();
    }

    public void checkUnLoadedNodeForProcess(JobEditorInput fileEditorInput) {
        if (fileEditorInput == null || fileEditorInput.getLoadedProcess() == null) {
            return;
        }
        IProcess2 loadedProcess = fileEditorInput.getLoadedProcess();
        List<NodeType> unloadedNode = loadedProcess.getUnloadedNode();
        if (unloadedNode != null && !unloadedNode.isEmpty()) {

            String message = Messages.getString("ProcessAction.unloadComponent") + "\n";
            for (int i = 0; i < unloadedNode.size(); i++) {
                message = message + unloadedNode.get(i).getComponentName() + "\n";
            }
            if (!CommonsPlugin.isHeadless() && PlatformUI.isWorkbenchRunning()) {
                Display display = Display.getCurrent();
                if (display == null) {
                    display = Display.getDefault();
                }
                if (display != null) {
                    final Display tmpDis = display;
                    final String tmpMess = message;
                    display.syncExec(new Runnable() {

                        public void run() {
                            Shell shell = null;
                            final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                            if (activeWorkbenchWindow != null) {
                                shell = activeWorkbenchWindow.getShell();
                            } else {
                                if (tmpDis != null) {
                                    shell = tmpDis.getActiveShell();
                                } else {
                                    shell = DisplayUtils.getDefaultShell(false);
                                }
                            }
                            MessageDialog.openWarning(shell, "Warning", tmpMess);
                        }
                    });
                }
            }
        }
    }

}
