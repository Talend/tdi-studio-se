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
package org.talend.designer.mapper.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.designer.mapper.managers.MapperManager;

/**
 * DOC xwen  class global comment. Detailled comment
 */
public class AutoMappingDialog extends Dialog {

    private MapperManager mapperManager;

    /**
     * DOC xwen AutoMappingDialog constructor comment.
     *
     * @param parentShell
     */
    public AutoMappingDialog(Shell parentShell, MapperManager mapperManager) {
        super(parentShell);
        this.mapperManager = mapperManager;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginLeft = 10;
        gridLayout.marginTop = 10;
        gridLayout.marginHeight = 10;
        container.setLayout(gridLayout);

        return container;
    }


}
