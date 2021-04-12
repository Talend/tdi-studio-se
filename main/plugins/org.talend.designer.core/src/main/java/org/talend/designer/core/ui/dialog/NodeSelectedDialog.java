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
package org.talend.designer.core.ui.dialog;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.talend.core.model.process.INode;
import org.talend.designer.core.i18n.Messages;

/**
 * DOC ycbai class global comment. Detailled comment
 */
public class NodeSelectedDialog extends ElementListSelectionDialog {

    public NodeSelectedDialog(Shell parent, List<INode> nodes) {
        super(parent, new NodeLabelProvider());
        setMessage(Messages.getString("NodeSelectedDialog.msg")); //$NON-NLS-1$
        setElements(nodes.toArray(new INode[0]));
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("NodeSelectedDialog.title")); //$NON-NLS-1$
        newShell.setSize(500, 600);
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        getShell().setSize(400, 500);
        Point location = getInitialLocation(getShell().getSize());
        getShell().setLocation(location.x, location.y);
    }

    static class NodeLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {
            if (element instanceof INode) {
                INode node = (INode) element;
                return node.getLabel();
            }
            return ""; //$NON-NLS-1$
        }

        @Override
        public Image getImage(Object element) {
            if (element instanceof INode) {
                INode node = (INode) element;
                ImageDescriptor icon = node.getComponent().getIcon32();
                if (icon != null) {
                    return icon.createImage();
                }
            }
            return null;
        }

    }
}
