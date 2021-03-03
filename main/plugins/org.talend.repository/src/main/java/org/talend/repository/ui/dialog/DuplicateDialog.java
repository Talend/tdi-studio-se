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
package org.talend.repository.ui.dialog;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC zwang class global comment. Detailled comment
 */
public class DuplicateDialog extends InputDialog {

    private IStructuredSelection selection = null;

    private Map<String, String> copyNameMap = null;

    private String keyName = null;

    public DuplicateDialog(Shell parentShell, IStructuredSelection selection, String title, String label,
            String message, IInputValidator validator) {
        super(parentShell, title, label, message, validator);
        this.selection = selection;
        this.copyNameMap = new HashMap<String, String>();
        this.keyName = ((RepositoryNode) ((TreeSelection) this.selection).toList().get(0)).getObject().getProperty()
                .getLabel();
        copyNameMap.put(keyName, message);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        copyNameMap.put(this.keyName, getText().getText());
        super.okPressed();
    }

    /**
     * Getter for copyNameMap.
     *
     * @return the copyNameMap
     */
    public Map<String, String> getCopyNameMap() {
        return this.copyNameMap;
    }
}
