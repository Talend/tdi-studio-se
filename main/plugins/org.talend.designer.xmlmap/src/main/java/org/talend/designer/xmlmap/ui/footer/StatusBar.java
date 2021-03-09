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
package org.talend.designer.xmlmap.ui.footer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.IImage;

/**
 * DOC amaumont class global comment. Detailled comment
 */
public class StatusBar extends Composite {

    /**
     *
     * DOC amaumont UIManager class global comment. Detailled comment
     */
    public enum STATUS {
        EMPTY("", null), //$NON-NLS-1$
        WARN("Warning", EImage.WARNING_ICON), //$NON-NLS-1$
        INFO("Info", EImage.INFORMATION_ICON), //$NON-NLS-1$
        ERROR("Error", EImage.ERROR_ICON), ; //$NON-NLS-1$

        String label;

        IImage image;

        private STATUS(String label, IImage image) {
            this.label = label;
            this.image = image;
        }

        /**
         * Getter for label.
         *
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * Getter for image.
         *
         * @return the image
         */
        public IImage getImage() {
            return image;
        }

    }

    private Label statusBarImage;

    private Label statusBarLabel;

    public StatusBar(Composite parent, int style) {
        super(parent, style);

        FormLayout formLayout = new FormLayout();
        setLayout(formLayout);

        statusBarImage = new Label(this, SWT.NONE);

        statusBarLabel = new Label(this, SWT.NONE);
        statusBarLabel.setText(""); //$NON-NLS-1$

        FormData labelData = new FormData();
        labelData.left = new FormAttachment(statusBarImage);
        statusBarLabel.setLayoutData(labelData);

    }

    public void setValues(STATUS status, String text) {
        Image image = null;
        String content = ""; //$NON-NLS-1$
        if (status != STATUS.EMPTY) {
//            if (status != STATUS.INFO) {
                content = " : " + text; //$NON-NLS-1$
                IImage iimage = status.getImage();
                if (iimage == null) {
                    image = null;
                } else {
                    image = org.talend.commons.ui.runtime.image.ImageProvider.getImage(org.talend.commons.ui.runtime.image.ImageProvider
                            .getImageDesc(iimage));
                }
//            } else {
//                content = text;
//            }
        }
        statusBarLabel.setText(content);
        statusBarImage.setImage(image);
        setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

}
