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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.Property;
import org.talend.repository.i18n.Messages;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class DateSection extends AbstractSection {

    private Text creationDate;

    private Text modificationDate;

    private Text commitDate;

    private CLabel commitLabel;

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat();

    @Override
    protected void enableControl(boolean enable) {
    }

    @Override
    protected void showControl(boolean visible) {
        creationDate.getParent().setVisible(visible);
    }

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);

        Composite composite = getWidgetFactory().createFlatFormComposite(parent);
        FormData data;

        creationDate = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(33, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        creationDate.setLayoutData(data);
        creationDate.setEnabled(false);

        CLabel creationLabel = getWidgetFactory().createCLabel(composite, Messages.getString("DateSection.creationLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(creationDate, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(creationDate, 0, SWT.CENTER);
        creationLabel.setLayoutData(data);

        modificationDate = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(creationDate, STANDARD_LABEL_WIDTH + 15);
        data.right = new FormAttachment(66, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        modificationDate.setLayoutData(data);
        modificationDate.setEnabled(false);

        CLabel modificationLabel = getWidgetFactory()
                .createCLabel(composite, Messages.getString("DateSection.ModificationLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(creationDate, ITabbedPropertyConstants.HSPACE * 3);
        data.right = new FormAttachment(modificationDate, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(modificationDate, 0, SWT.CENTER);
        modificationLabel.setLayoutData(data);

        commitDate = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(modificationDate, STANDARD_LABEL_WIDTH + 15);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        commitDate.setLayoutData(data);
        commitDate.setEnabled(false);

        commitLabel = getWidgetFactory().createCLabel(composite, Messages.getString("DateSection.commitLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(modificationDate, ITabbedPropertyConstants.HSPACE * 3);
        data.right = new FormAttachment(commitDate, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(commitDate, 0, SWT.CENTER);
        commitLabel.setLayoutData(data);

        addFocusListenerToChildren(composite);
    }

    @Override
    public void refresh() {
        creationDate.setText(getCreationDate() != null ? FORMATTER.format(getCreationDate()) : ""); //$NON-NLS-1$
        modificationDate.setText(getModificationDate() != null ? FORMATTER.format(getModificationDate()) : ""); //$NON-NLS-1$
        commitDate.setText(getCommitDate() != null ? FORMATTER.format(getCommitDate()) : ""); //$NON-NLS-1$

        commitLabel.setVisible(getCommitDate() != null);
        commitDate.setVisible(getCommitDate() != null);
    }

    protected Date getCreationDate() {
        return getObject().getCreationDate();
    }

    protected Date getModificationDate() {
        return getObject().getModificationDate();
    }

    protected Date getCommitDate() {
        Property property = getObject().getProperty();
        if (property == null || property.getItem() == null) {
            return null;
        }

        Item item = property.getItem();
        ItemState state = item.getState();

        return state.getCommitDate();
    }

    protected void beforeSave() {
    }

}
