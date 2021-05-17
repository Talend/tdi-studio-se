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

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.CorePlugin;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.IJobHierarchyViewPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * Refocuses the type hierarchy on a type selection from a all types dialog.
 */
public class FocusOnJobAction extends Action {

    private IJobHierarchyViewPart fViewPart;

    public FocusOnJobAction(IJobHierarchyViewPart part) {
        // super(TypeHierarchyMessages.FocusOnTypeAction_label);
        super(Messages.getString("FocusOnJobAction.FocusOnTypeAction_label")); //$NON-NLS-1$
        // setDescription(TypeHierarchyMessages.FocusOnTypeAction_description);
        setDescription(Messages.getString("FocusOnJobAction.FocusOnTypeAction_description")); //$NON-NLS-1$
        // setToolTipText(TypeHierarchyMessages.FocusOnTypeAction_tooltip);
        setToolTipText(Messages.getString("FocusOnJobAction.FocusOnTypeAction_tooltip")); //$NON-NLS-1$

        fViewPart = part;
    }

    /*
     * @see Action#run
     */
    public void run() {
        Shell parent = fViewPart.getSite().getShell();

        RepositoryReviewDialog dialog = new RepositoryReviewDialog(parent, ERepositoryObjectType.PROCESS, null);

        if (dialog.open() == RepositoryReviewDialog.OK) {
            Item item = dialog.getResult().getObject().getProperty().getItem();
            if (item instanceof ProcessItem) {
                // Process loadedProcess = new Process(((ProcessItem) item).getProperty());
                // loadedProcess.loadXmlFile();
                IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
                Process loadedProcess = (Process) designerCoreService.getProcessFromProcessItem((ProcessItem) item);
                fViewPart.setInputProcess(loadedProcess);
            }
        }
    }
}
