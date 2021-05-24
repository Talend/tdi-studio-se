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
package org.talend.designer.xmlmap.ui.dialog;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MDMConnectionItem;
import org.talend.core.model.properties.XmlFileConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.Folder;
import org.talend.designer.xmlmap.i18n.Messages;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.dialog.RepositoryReviewDialog;

/**
 * DOC talend class global comment. Detailled comment
 */
public class RepositoryXmlSelectionDialog extends RepositoryReviewDialog {

    private Label errorLabel;

    public RepositoryXmlSelectionDialog(Shell parentShell, String[] repositoryTypes) {
        super(parentShell, repositoryTypes);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control composite = super.createDialogArea(parent);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        Composite container = new Composite(parent, SWT.NONE);
        errorLabel = new Label(container, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginLeft = 5;
        container.setLayout(layout);
        container.setLayoutData(layoutData);
        layoutData = new GridData(GridData.FILL_HORIZONTAL);
        errorLabel.setLayoutData(layoutData);
        return composite;
    }

    @Override
    protected boolean isSelectionValid(SelectionChangedEvent event) {
        boolean highlightOKButton = true;
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (selection == null || selection.size() != 1) {
            highlightOKButton = false;
        } else {
            RepositoryNode node = (RepositoryNode) selection.getFirstElement();
            if (node.getObject() == null || node.getObject().getProperty() == null) {
                return false;
            }

            if (node.getObject() instanceof Folder) {
                return false;
            }

            Item item = node.getObject().getProperty().getItem();
            if (item instanceof XmlFileConnectionItem) {
                ERepositoryObjectType t = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
                if (node.getType() != ENodeType.REPOSITORY_ELEMENT) {
                    highlightOKButton = false;
                }
            } else if (item instanceof MDMConnectionItem) {
                if (node.getObject() instanceof MetadataTable) {
                    highlightOKButton = true;
                    updateErrorMessage(null);
                } else {
                    highlightOKButton = false;
                    updateErrorMessage(Messages.getString("RepositoryXmlSelectionDialog.UpdateErrorMessage"));
                }
            }
        }
        return highlightOKButton;
    }

    private void updateErrorMessage(String message) {
        if (message == null || "".equals(message)) {
            errorLabel.setText("");
        } else {
            errorLabel.setText(message);
        }
    }

    @Override
    public RepositoryNode getResult() {
        return super.getResult();
    }

}
