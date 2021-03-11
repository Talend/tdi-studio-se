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

import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.RepositoryPlugin;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNodeUtilities;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class NameSection extends AbstractSection {

    private Text nameText;

    private CLabel errorLabel;

    private Composite composite;

    @Override
    public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);

        composite = getWidgetFactory().createFlatFormComposite(parent);
        FormData data;

        nameText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(50, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        nameText.setLayoutData(data);
        addFocusListener(nameText);
        nameText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                IStatus status = evaluateTextField();
                if (status.getSeverity() == IStatus.ERROR) {
                    errorLabel.setText(status.getMessage());
                    errorLabel.setVisible(true);
                } else {
                    errorLabel.setVisible(false);
                }
            }
        });

        CLabel labelLabel = getWidgetFactory().createCLabel(composite, Messages.getString("NameSection.Name")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(nameText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(nameText, 0, SWT.CENTER);
        labelLabel.setLayoutData(data);

        errorLabel = getWidgetFactory().createCLabel(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(nameText, ITabbedPropertyConstants.HSPACE * 3);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(nameText, 0, SWT.CENTER);
        errorLabel.setLayoutData(data);
        errorLabel.setImage(ImageProvider.getImage(EImage.ERROR_ICON));
        errorLabel.setVisible(false);

        addFocusListenerToChildren(composite);
    }

    protected IStatus evaluateTextField() {
        if (getObject().getRepositoryObjectType() == null) {
            return createOkStatus();
        }
        ERepositoryObjectType routeResource = ERepositoryObjectType.valueOf(ERepositoryObjectType.class, "ROUTE_RESOURCES");

        String text = nameText.getText();
        if (text.length() == 0) {
            return createStatus(IStatus.ERROR, Messages.getString("NameSection.NameEmpty")); //$NON-NLS-1$
        } else if (getObject().getRepositoryNode() != null && routeResource != null
                && routeResource.equals(getObject().getRepositoryNode().getContentType())) {
            return ResourcesPlugin.getWorkspace().validateName(text, IResource.FOLDER);
        } else if (!Pattern.matches(RepositoryConstants.getPattern(getType()), text)) {
            return createStatus(IStatus.ERROR, Messages.getString("NameSection.NameIncorrect")); //$NON-NLS-1$
        }
        // else if (!isValid(text)) {
        // return createStatus(IStatus.ERROR, Messages.getString("NameSection.NameExist")); //$NON-NLS-1$
        // }
        else {
            return createOkStatus();
        }
    }

    public boolean isValid(String itemName) {
        try {
            return getRepositoryFactory().isNameAvailable(getObject().getProperty().getItem(), itemName);
        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return false;
        }
    }

    protected static IStatus createOkStatus() {
        return new Status(IStatus.OK, RepositoryPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
    }

    protected static IStatus createStatus(int severity, String message) {
        return new Status(severity, RepositoryPlugin.PLUGIN_ID, IStatus.OK, message, null);
    }

    @Override
    public void refresh() {
        nameText.setText(getName() != null ? getName().toString() : ""); //$NON-NLS-1$
    }

    protected String getName() {
        return getObject().getLabel();
    }

    @Override
    protected void beforeSave() {
        IStatus status = evaluateTextField();
        if (status.getSeverity() != IStatus.ERROR) {
            String text = nameText.getText();
            if (!text.equals(getObject().getLabel())) {
                if (getType() == ERepositoryObjectType.FOLDER) {
                    IPath path = RepositoryNodeUtilities.getPath(getNode());
                    try {
                        ERepositoryObjectType type = getNode().getContentType();
                        getRepositoryFactory().renameFolder(type, path, text);
                        // TDI-21143 : Studio repository view : remove all refresh call to repo view
                        // IRepositoryView view = RepositoryManagerHelper.findRepositoryView();
                        // if (view != null) {
                        // view.refresh();
                        // }
                    } catch (PersistenceException e) {
                        // e.printStackTrace();
                        ExceptionHandler.process(e);
                        return;
                    }
                }
                // getObject().setLabel(text);
            }
        }
    }

    @Override
    protected void enableControl(boolean enable) {
        // nameText.setEditable(enable);
        nameText.setEnabled(enable);
    }

    @Override
    protected void showControl(boolean visible) {
        nameText.getParent().setVisible(visible);
    }

}
