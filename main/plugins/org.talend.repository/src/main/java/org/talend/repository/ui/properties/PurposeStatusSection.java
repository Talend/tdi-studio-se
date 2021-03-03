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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.Status;
import org.talend.repository.i18n.Messages;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class PurposeStatusSection extends AbstractSection {

    private Text purposeText;

    private StatusHelper statusHelper = new StatusHelper(getRepositoryFactory());

    private CCombo statusText;

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);

        Composite composite = getWidgetFactory().createFlatFormComposite(parent);
        FormData data;

        purposeText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(70, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        purposeText.setLayoutData(data);
        addFocusListener(purposeText);

        CLabel purposeLabel = getWidgetFactory().createCLabel(composite, Messages.getString("PurposeStatusSection.purposeLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(purposeText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(purposeText, 0, SWT.CENTER);
        purposeLabel.setLayoutData(data);

        statusText = getWidgetFactory().createCCombo(composite, SWT.READ_ONLY | SWT.BORDER); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(purposeText, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        statusText.setLayoutData(data);
        addFocusListener(statusText);

        // addComboFieldListeners(statusText, new ModifyListener() {
        // public void modifyText(ModifyEvent e) {
        // if (enableListener)
        // needSave = true;
        // }}, true);

        CLabel statusLabel = getWidgetFactory().createCLabel(composite, Messages.getString("PurposeStatusSection.statusLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(purposeText, ITabbedPropertyConstants.HSPACE * 3);
        data.right = new FormAttachment(statusText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(statusText, 0, SWT.CENTER);
        statusLabel.setLayoutData(data);

        addFocusListenerToChildren(composite);
    }

    @Override
    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        try {
            Property property = getObject().getProperty();
            List<Status> status = property == null ? new ArrayList<Status>() : statusHelper.getStatusList(property);
            statusText.setItems(toArray(status));
        } catch (PersistenceException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }
    }

    public static String[] toArray(List<Status> status) {
        String[] res = new String[status.size()];
        int i = 0;
        for (Status s : status) {
            res[i++] = s.getLabel();
        }
        return res;
    }

    @Override
    public void refresh() {
        purposeText.setText(getPurpose() != null ? getPurpose().toString() : ""); //$NON-NLS-1$
        // enableListener = false;
        statusText.setText(getStatus() != null ? getStatus().toString() : ""); //$NON-NLS-1$
        // enableListener = true;
    }

    protected String getPurpose() {
        return getObject().getPurpose();
    }

    protected String getStatus() {
        String statusCode = getObject().getStatusCode();
        if (statusCode == null || "".equals(statusCode)) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        return statusHelper.getStatusLabel(statusCode);
    }

    @Override
    protected void beforeSave() {
        // String text = purposeText.getText();
        // if (!text.equals(getObject().getPurpose())) {
        // getObject().setPurpose(text);
        // }

        // text = statusHelper.getStatusCode(statusText.getText());
        // if (!text.equals(getObject().getStatusCode())) {
        // getObject().setStatusCode(text);
        // }
    }

    @Override
    protected void enableControl(boolean enable) {
        purposeText.setEnabled(enable);
        statusText.setEnabled(enable);
    }

    @Override
    protected void showControl(boolean visible) {
        purposeText.getParent().setVisible(visible);
    }
}
