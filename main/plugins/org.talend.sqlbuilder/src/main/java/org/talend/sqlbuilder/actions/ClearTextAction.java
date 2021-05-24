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
package org.talend.sqlbuilder.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 *
 * This class is used for clearing the text in Text component. <br/>
 *
 * @author ftang
 *
 */
public class ClearTextAction extends AbstractEditorAction {

    private ImageDescriptor img = ImageUtil.getDescriptor("Images.ClearTextIcon"); //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getText()
     */
    public String getText() {
        return Messages.getString("Clear_1"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#run()
     */
    public void run() {
        editor.clearText();
    }

    // Checks if enabled.
    public boolean isEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getToolTipText()
     */
    public String getToolTipText() {
        return Messages.getString("Clear_2"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#getHoverImageDescriptor()
     */
    public ImageDescriptor getHoverImageDescriptor() {
        return img;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return img;
    };
}
