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
package org.talend.designer.core.ui.hierarchy;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.ProcessPart;
import org.talend.designer.core.utils.DesignerUtilities;

/**
 * This action opens a job hierarchy on the selected job.
 * <p>
 * The action is applicable to selections containing elements of jobs <code>IProcess2</code>.
 *
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @since 2.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class OpenJobHierarchyAction extends SelectionAction {

    // private JavaEditor fEditor;

    /**
     * Creates a new <code>OpenJobHierarchyAction</code>. The action requires that the selection provided by the site's
     * selection provider is of type <code>
     * org.eclipse.jface.viewers.IStructuredSelection</code>.
     *
     * @param site the site providing context information for this action
     */
    public OpenJobHierarchyAction(IWorkbenchPart part) {
        super(part);
        setText(Messages.getString("OpenJobHierarchyAction.textContent")); //$NON-NLS-1$
        setToolTipText(Messages.getString("OpenJobHierarchyAction.tipTextContent")); //$NON-NLS-1$
        setDescription(Messages.getString("OpenJobHierarchyAction.description")); //$NON-NLS-1$
    }

    @Override
    protected boolean calculateEnabled() {
        List parts = getSelectedObjects();
        if (parts.size() != 1) {
            return false;
        }

        Object o = parts.get(0);
        if (o instanceof NodePart) {
            NodePart part = (NodePart) o;
            Node node = (Node) part.getModel();
            if (DesignerUtilities.isTRunJobComponent(node)) {
                return true;
            }
        } else if (o instanceof ProcessPart) {
            return true;
        }
        return false;
    }

    /**
     * retrieve the process from the NodePart or ProcessPart
     *
     * @param input
     * @return
     */
    private IProcess2 caculateProcess(Object input) {
        if (input instanceof NodePart) {
            NodePart part = (NodePart) input;
            Node node = (Node) part.getModel();
            if (DesignerUtilities.isTRunJobComponent(node)) {
                return DesignerUtilities.getCorrespondingProcessFromTRunjob(node);
            }
        } else if (input instanceof ProcessPart) {
            ProcessPart processPart = (ProcessPart) input;
            return (IProcess2) processPart.getModel();
        }
        return null;
    }

    public void run() {

        List selection = getSelectedObjects();

        if (selection.size() != 1)
            return;
        Object input = selection.get(0);

        IProcess2 element = caculateProcess(input);
        if (element != null) {
            run(element);
        }
    }

    /*
     * No Javadoc since the method isn't meant to be public but is since the beginning
     */
    public void run(IProcess2 elements) {
        open(elements, getWorkbenchPart().getSite().getWorkbenchWindow());
    }

    public static void open(IProcess2 element, IWorkbenchWindow window) {
        if (element == null)
            return;
        openInViewPart(window, element);
    }

    private static void openInViewPart(IWorkbenchWindow window, IProcess2 input) {
        IWorkbenchPage page = window.getActivePage();
        try {
            JobHierarchyViewPart result = (JobHierarchyViewPart) page.findView(JobHierarchyViewPart.ID);
            // if (result != null) {
            // result.clearNeededRefresh(); // avoid refresh of old hierarchy on 'becomes visible'
            // }
            result = (JobHierarchyViewPart) page.showView(JobHierarchyViewPart.ID);
            result.setInputProcess(input);
        } catch (CoreException e) {
            ExceptionHandler.process(e);
        }
    }

}
