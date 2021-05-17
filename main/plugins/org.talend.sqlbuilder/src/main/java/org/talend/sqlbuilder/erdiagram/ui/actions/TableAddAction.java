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
package org.talend.sqlbuilder.erdiagram.ui.actions;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.erdiagram.ui.AddTablesDialog;
import org.talend.sqlbuilder.erdiagram.ui.ErDiagramComposite;
import org.talend.sqlbuilder.erdiagram.ui.commands.CreateTableCommand;
import org.talend.sqlbuilder.erdiagram.ui.editor.ErdiagramDiagramEditor;
import org.talend.sqlbuilder.erdiagram.ui.nodes.ErDiagram;
import org.talend.sqlbuilder.repository.utility.EMFRepositoryNodeManager;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class TableAddAction extends SelectionAction {

    public static final String TABLE_ADD = Messages.getString("TableAddAction.textAddTables"); //$NON-NLS-1$

    private IWorkbenchPart part;

    /*
     * (non-Java)
     *
     * @see org.eclipse.jface.action.Action#getId()
     */
    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return TABLE_ADD;
    }

    /**
     * admin TableAddAction constructor comment.
     *
     * @param part
     */
    public TableAddAction(IWorkbenchPart part) {
        super(part);
        this.part = part;
        setText(TABLE_ADD);
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        return true;
    }

    /*
     * (non-Java)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    @Override
    public void run() {
        ErdiagramDiagramEditor erdiagramDiagramEditor = (ErdiagramDiagramEditor) part;
        Control control = erdiagramDiagramEditor.getGraphicalControl();
        AddTablesDialog dialog = new AddTablesDialog(DisplayUtils.getDefaultShell(false));
        if (control.getParent() instanceof ErDiagramComposite) {
            if (EMFRepositoryNodeManager.getInstance().getRoot() == null) {
                return;
            }
            dialog.setRootNode(EMFRepositoryNodeManager.getInstance().getRoot());
        }
        if (Window.OK == dialog.open()) {
            List tables1 = dialog.getTables();
            this.execute(createCreateCommand((ErDiagram) erdiagramDiagramEditor.getViewer().getContents().getModel(),
                    tables1));
        }
    }

    public void update() {
        setSelection(((ErdiagramDiagramEditor) part).getViewer().getSelection());
    }

    private Command createCreateCommand(ErDiagram erDiagram, List<MetadataTable> tables) {
        Command cmd = new CreateTableCommand(erDiagram, tables);
        return cmd;
    }
}
