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

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.core.model.components.ComponentCategory;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.ui.editor.cmd.DisplaySubjobCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerPart;
import org.talend.designer.core.ui.preferences.TalendDesignerPrefConstants;

/**
 * Action used to set the start status on a node with the context menu. <br/>
 *
 * $Id: NodeSetActivateAction.java 3351 2007-05-04 12:14:00 +0000 (ven., 04 mai 2007) plegall $
 *
 */
public class DisplaySubjobAction extends SelectionAction {

    public static final String ID = "org.talend.designer.core.ui.editor.action.DisplaySubjobAction"; //$NON-NLS-1$

    private static final String TEXT_DISPLAY_SUBJOB = Messages.getString("DisplaySubjobCommand.DisplaySubjob2"); //$NON-NLS-1$

    private static final String TEXT_HIDE_SUBJOB = Messages.getString("DisplaySubjobCommand.HideSubjob2"); //$NON-NLS-1$

    public DisplaySubjobAction(IWorkbenchPart part) {
        super(part);
        setId(ID);
    }

    @Override
    protected boolean calculateEnabled() {
        return canPerformAction();
    }

    /**
     * Test if the selected item is a node.
     *
     * @return true / false
     */
    private boolean canPerformAction() {
        if (getSelectedObjects().isEmpty()) {
            return false;
        }
        List parts = getSelectedObjects();
        if (parts.size() == 1) {
            Object o = parts.get(0);
            if (o instanceof NodePart) {
                NodePart part = (NodePart) o;
                Node node = (Node) part.getModel();

                //fix TESB-8326
                if(node != null && node.getComponent() != null){
                    if(ComponentCategory.CATEGORY_4_CAMEL.getName().equals(node.getComponent().getPaletteType())){
                        return false;
                    }
                }//end Fix of TESB-8326

                if (node.getJobletNode() != null) {
                    node = (Node) node.getJobletNode();
                }
                if (!isDisplayWholeSubjob()) {
                    // if the subjobs are not displayed, no need to display this action.
                    return false;
                }
                if (node.getNodeContainer().getSubjobContainer() != null
                        && node.getNodeContainer().getSubjobContainer().isDisplayed()) {
                    setText(TEXT_HIDE_SUBJOB);
                } else {
                    setText(TEXT_DISPLAY_SUBJOB);
                }

            } else if (o instanceof SubjobContainerPart) {
                SubjobContainerPart part = (SubjobContainerPart) o;
                SubjobContainer subjob = (SubjobContainer) part.getModel();
                if (!isDisplayWholeSubjob()) {
                    // if the subjobs are not displayed, no need to display this action.
                    return false;
                }

                if (!subjob.isDisplayed()) {
                    return false;
                }
                setText(TEXT_HIDE_SUBJOB);
            } else {
                return false;
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        List editparts = getSelectedObjects();
        if (editparts.size() == 1) {
            Object o = editparts.get(0);
            if (o instanceof NodePart) {
                NodePart part = (NodePart) o;
                Node node = (Node) part.getModel();
                if (node.getJobletNode() != null) {
                    node = (Node) node.getJobletNode();
                }
                DisplaySubjobCommand displaySubjobCommand = new DisplaySubjobCommand(node.getNodeContainer().getSubjobContainer());
                execute(displaySubjobCommand);
            } else if (o instanceof SubjobContainerPart) {
                SubjobContainerPart part = (SubjobContainerPart) o;
                SubjobContainer subjob = (SubjobContainer) part.getModel();
                DisplaySubjobCommand displaySubjobCommand = new DisplaySubjobCommand(subjob);
                execute(displaySubjobCommand);
            }
        }
    }

    private boolean isDisplayWholeSubjob() {
        return DesignerPlugin.getDefault().getPreferenceStore().getBoolean(TalendDesignerPrefConstants.DISPLAY_SUBJOBS);
    }
}
