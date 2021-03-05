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
package org.talend.repository.ui.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.repository.i18n.Messages;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class DescriptionSection extends AbstractSection {

    private static final int NB_LINES = 4;

    private Text descriptionText;

    @Override
    protected void enableControl(boolean enable) {
        descriptionText.setEnabled(enable);
    }

    @Override
    protected void showControl(boolean visible) {
        descriptionText.getParent().setVisible(visible);
    }

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);
        Composite composite = getWidgetFactory().createFlatFormComposite(parent);
        FormData data;

        descriptionText = getWidgetFactory().createText(composite, "", SWT.MULTI); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        data.height = NB_LINES * descriptionText.getLineHeight();
        descriptionText.setLayoutData(data);
        addFocusListener(descriptionText);

        CLabel labelLabel = getWidgetFactory().createCLabel(composite, Messages.getString("DescriptionSection.Label")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(descriptionText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(descriptionText, 0, SWT.TOP);
        labelLabel.setLayoutData(data);

        addFocusListenerToChildren(composite);
    }

    @Override
    public void refresh() {
        descriptionText.setText(getDescription() != null ? getDescription().toString() : ""); //$NON-NLS-1$
    }

    protected String getDescription() {
        return getObject().getDescription();
    }

    @Override
    protected void beforeSave() {
        // String text = descriptionText.getText();
        // if (!text.equals(getObject().getDescription())) {
        // getObject().setDescription(text);
        // }
    }
}
