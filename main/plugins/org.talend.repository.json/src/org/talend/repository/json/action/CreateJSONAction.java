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
package org.talend.repository.json.action;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.update.RepositoryUpdateManager;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.actions.metadata.AbstractCreateAction;
import org.talend.repository.ProjectManager;
import org.talend.repository.json.i18n.Messages;
import org.talend.repository.json.node.JSONRepositoryNodeType;
import org.talend.repository.json.ui.wizards.JSONWizard;
import org.talend.repository.json.util.JSONImage;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.json.JSONFileConnectionItem;

/**
 * DOC wanghong class global comment. Detailled comment
 */
public class CreateJSONAction extends AbstractCreateAction {

    private static final String CREATE_LABEL = Messages.CreateJSONAction_CREATE_JSON;

    private static final String EDIT_LABEL = Messages.CreateJSONAction_EDIT_JSON;

    private static final String OPEN_LABEL = Messages.CreateJSONAction_OPEN_JSON;

    protected static final int WIZARD_WIDTH = 700;

    protected static final int WIZARD_HEIGHT = 400;

    private boolean creation = true;

    public CreateJSONAction() {
        super();
        this.setText(CREATE_LABEL);
        this.setToolTipText(CREATE_LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(JSONImage.JSON_ICON));
    }

    public CreateJSONAction(boolean isToolbar) {
        this();
        setToolbar(isToolbar);
    }

    @Override
    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);
        if (!JSONRepositoryNodeType.JSON.equals(nodeType)) {
            return;
        }
        IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
        switch (node.getType()) {
        case SIMPLE_FOLDER:
            if (node.getObject() != null && node.getObject().getProperty().getItem().getState().isDeleted()) {
                setEnabled(false);
                return;
            }
        case SYSTEM_FOLDER:
            if (factory.isUserReadOnlyOnCurrentProject() || !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                setEnabled(false);
                return;
            }
            this.setText(CREATE_LABEL);
            collectChildNames(node);
            creation = true;
            break;
        case REPOSITORY_ELEMENT:
            if (factory.isPotentiallyEditable(node.getObject())) {
                this.setText(EDIT_LABEL);
                collectSiblingNames(node);
            } else {
                this.setText(OPEN_LABEL);
            }
            collectSiblingNames(node);
            creation = false;
            break;
        default:
            return;
        }
        setEnabled(true);
    }

    @Override
    public Class getClassForDoubleClick() {
        return JSONFileConnectionItem.class;
    }

    @Override
    protected void doRun() {
        if (repositoryNode == null) {
            repositoryNode = getCurrentRepositoryNode();
        }

        if (isToolbar()) {
            if (repositoryNode != null && repositoryNode.getContentType() != JSONRepositoryNodeType.JSON) {
                repositoryNode = null;
            }
            if (repositoryNode == null) {
                repositoryNode = getRepositoryNodeForDefault(JSONRepositoryNodeType.JSON);
            }

        }

        WizardDialog wizardDialog = null;
        JSONWizard jsonWizard = null;
        try {
            if (!creation) {
                ConnectionItem conntectionItem = (ConnectionItem) repositoryNode.getObject().getProperty().getItem();
                RepositoryUpdateManager.updateConnectionContextParam(conntectionItem);
            }
            if (isToolbar()) {
                init(repositoryNode);
                jsonWizard = new JSONWizard(PlatformUI.getWorkbench(), creation, repositoryNode, getExistingNames());
                // hdfs.setToolBar(true);
                wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), jsonWizard);
            } else {
                jsonWizard = new JSONWizard(PlatformUI.getWorkbench(), creation, repositoryNode, getExistingNames());
                wizardDialog = new WizardDialog(Display.getCurrent().getActiveShell(), jsonWizard);
            }
            wizardDialog.setPageSize(WIZARD_WIDTH, WIZARD_HEIGHT);
            wizardDialog.create();
            wizardDialog.open();
        } catch (PersistenceException ex) {
            ExceptionHandler.process(ex);
        } finally {
            if (jsonWizard != null) {
                jsonWizard.deleteTemFile();
            }
        }

    }
}
