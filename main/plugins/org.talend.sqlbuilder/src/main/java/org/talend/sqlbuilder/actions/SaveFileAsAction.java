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
 * This class is responsible for saving current text into a file. <br/>
 *
 * @author ftang
 *
 */
public class SaveFileAsAction extends AbstractEditorAction {

    private ImageDescriptor image = ImageUtil.getDescriptor("Images.SaveFileAsIcon"); //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return image;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getText()
     */
    public String getText() {
        return Messages.getString("SQLEditor.Actions.SaveAs"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.Action#isEnabled()
     */
    public boolean isEnabled() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#getToolTipText()
     */
    public String getToolTipText() {
        return Messages.getString("SQLEditor.Actions.SaveAsToolTip"); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.sqlbuilder.actions.AbstractEditorAction#run()
     */
    public void run() {
        editor.doSaveAs();
    };
}
