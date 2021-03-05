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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.LockInfo;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class VersionAuthorSection extends AbstractSection {

    private Text versionText;

    private Text authorText;

    private Text lockerText;

    private Button btnDown;

    private Button btnUp;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#createControls(org.eclipse.swt.widgets.Composite,
     * org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
     */
    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);

        Composite composite = getWidgetFactory().createFlatFormComposite(parent);
        FormData data;

        authorText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        authorText.setEnabled(false);
        data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(35, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        authorText.setLayoutData(data);

        CLabel authorLabel = getWidgetFactory().createCLabel(composite, Messages.getString("VersionAuthorSection.authorLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(authorText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(authorText, 0, SWT.CENTER);
        authorLabel.setLayoutData(data);

        lockerText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        lockerText.setEnabled(false);
        data = new FormData();
        data.left = new FormAttachment(37, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(70, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        lockerText.setLayoutData(data);

        CLabel lockerLabel = getWidgetFactory().createCLabel(composite, Messages.getString("VersionAuthorSection.lockerLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(37, 0);
        data.right = new FormAttachment(lockerText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(lockerText, 0, SWT.CENTER);
        lockerLabel.setLayoutData(data);

        btnDown = getWidgetFactory().createButton(composite, "m", SWT.PUSH); //$NON-NLS-1$
        data = new FormData();
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        btnDown.setLayoutData(data);
        btnDown.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                versionMinorUp();
            }
        });

        btnUp = getWidgetFactory().createButton(composite, "M", SWT.PUSH); //$NON-NLS-1$
        data = new FormData();
        data.right = new FormAttachment(btnDown, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        btnUp.setLayoutData(data);
        btnUp.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                versionMajorUp();
            }
        });

        versionText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        versionText.setEnabled(false);
        data = new FormData();
        data.left = new FormAttachment(lockerText, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(btnUp, -2);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        versionText.setLayoutData(data);

        CLabel versionLabel = getWidgetFactory().createCLabel(composite, Messages.getString("VersionAuthorSection.versionLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(lockerText, ITabbedPropertyConstants.HSPACE * 3);
        data.right = new FormAttachment(versionText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(versionText, 0, SWT.CENTER);
        versionLabel.setLayoutData(data);

        addFocusListenerToChildren(composite);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.views.properties.tabbed.AbstractPropertySection#refresh()
     */
    @Override
    public void refresh() {
        if (getAuthor() != null && getAuthor().getLogin() != null) {
            authorText.setText(getAuthor().getLogin());
        } else {
            authorText.setText(""); //$NON-NLS-1$
        }

        String locker = ""; //$NON-NLS-1$
        LockInfo lockInfo = getLockInfo();
        if (lockInfo != null) {
            locker = lockInfo.getUser();
        }
        lockerText.setText(locker);

        versionText.setText(getVersion() == null ? "" : getVersion()); //$NON-NLS-1$
    }

    private LockInfo getLockInfo() {
        LockInfo lockInfo = null;
        Property property = getObject().getProperty();
        if (property == null) {
            return lockInfo;
        }
        Item item = property.getItem();
        if (item == null) {
            return lockInfo;
        }
        lockInfo = ProxyRepositoryFactory.getInstance().getLockInfo(item);
        return lockInfo;
    }

    private void versionMajorUp() {
        String newVersion = VersionUtils.upMajor(versionText.getText());
        versionText.setText(newVersion);
        beforeSave();
    }

    private void versionMinorUp() {
        String newVersion = VersionUtils.upMinor(versionText.getText());
        versionText.setText(newVersion);
        beforeSave();
    }

    protected User getAuthor() {
        return getObject().getAuthor();
    }

    protected String getVersion() {
        return getObject().getVersion();
    }

    @Override
    protected void beforeSave() {
        // String version = versionText.getText();
        // String version2 = getObject().getVersion();
        // if (version != null && version2 != null) {
        // if (VersionUtils.compareTo(version, version2) != 0) {
        // getObject().setVersion(version);
        // }
        // }
    }

    @Override
    protected void enableControl(boolean enable) {
        btnDown.setEnabled(enable);
        btnUp.setEnabled(enable);
    }

    @Override
    protected void showControl(boolean visible) {
        authorText.getParent().setVisible(visible);
        lockerText.getParent().setVisible(visible);
    }

    public boolean select(Object object) {
        if (object instanceof RepositoryNode) {
            RepositoryNode node = (RepositoryNode) object;
            return node.getType() != ENodeType.SIMPLE_FOLDER;
        }
        return false;
    }
}
