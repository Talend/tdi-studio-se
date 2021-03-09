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
package org.talend.designer.fileoutputxml.ui.header;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;

/**
 * DOC xzhang class global comment. Detailled comment
 */
public class HeaderComposite extends Composite {

    private Label statusLabel;

    private Label iconLabel;

    public HeaderComposite(Composite parent, int style) {
        super(parent, style);
        createComponents();
    }

    public void updateStatus(String message) {
        iconLabel.setImage(ImageProvider.getImage(EImage.ERROR_SMALL));
        statusLabel.setText(message);
    }

    public void clearStatus() {
        iconLabel.setImage(null);
        statusLabel.setText(""); //$NON-NLS-1$
    }

    private void createComponents() {
        GridData headerCompositeGridData = new GridData(GridData.FILL_HORIZONTAL);
        this.setLayoutData(headerCompositeGridData);

        FormLayout formLayout = new FormLayout();
        this.setLayout(formLayout);

        this.statusLabel = new Label(this, SWT.NONE);
        this.iconLabel = new Label(this, SWT.NONE);

        FormData iconFormData = new FormData();
        Point minSize = statusLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        iconFormData.width = Math.max(15, minSize.x);
        iconFormData.left = new FormAttachment(0, 5);
        iconLabel.setLayoutData(iconFormData);
        iconLabel.setImage(null);

        FormData labelFormData = new FormData();
        Point iconminSize = statusLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        labelFormData.width = Math.max(1000, iconminSize.x);
        labelFormData.left = new FormAttachment(iconLabel, 5);
        statusLabel.setLayoutData(labelFormData);
        statusLabel.setText(""); //$NON-NLS-1$
    }
}
