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
package org.talend.repository.ui.actions.metadata;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.runtime.model.repository.ERepositoryStatus;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.CorePlugin;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.ui.actions.metadata.CopyToGenericSchemaHelper;
import org.talend.repository.ProjectManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * Administrator class global comment. Detailed comment <br/>
 *
 */
public class CopyToGenericSchemaAction extends AContextualAction {

    protected static final String LABEL = "Copy to Generic schema"; //$NON-NLS-1$

    private boolean isAllowedRepositoryElement = false;

    private RepositoryNode sourceNode;

    private TreeViewer viewer;

    public CopyToGenericSchemaAction() {
        super();
        this.setText(LABEL);
        this.setToolTipText(LABEL);
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.COPY_ICON));
    }

    public void init(final TreeViewer viewer, final IStructuredSelection selection) {
        this.viewer = viewer;
        setEnabled(false);
        Object o = selection.getFirstElement();
        if (selection.isEmpty() || selection.size() != 1 || o == null || !(o instanceof RepositoryNode)) {
            return;
        }
        init((RepositoryNode) o);
        if (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()) {
            // setEnabled(false);
        }
        if (!ProjectManager.getInstance().isInCurrentMainProject((RepositoryNode) o)) {
            setEnabled(false);
        }
    }

    /**
     * Administrator Comment method "init".
     *
     * @param node
     */
    protected void init(RepositoryNode node) {
        ERepositoryObjectType nodeType = (ERepositoryObjectType) node.getProperties(EProperties.CONTENT_TYPE);

        if (nodeType == null || !(node.getObject() instanceof MetadataTableRepositoryObject)) {
            return;
        }

        if (nodeType == ERepositoryObjectType.METADATA_CON_TABLE || nodeType == ERepositoryObjectType.METADATA_CON_VIEW
                || nodeType == ERepositoryObjectType.METADATA_CON_SYNONYM
                || nodeType == ERepositoryObjectType.METADATA_CON_CALCULATION_VIEW
                || nodeType == ERepositoryObjectType.METADATA_FILE_DELIMITED
                || nodeType == ERepositoryObjectType.METADATA_FILE_POSITIONAL
                || nodeType == ERepositoryObjectType.METADATA_FILE_REGEXP || nodeType == ERepositoryObjectType.METADATA_FILE_XML
                || nodeType == ERepositoryObjectType.METADATA_FILE_EXCEL
                || nodeType == ERepositoryObjectType.METADATA_LDAP_SCHEMA
                || nodeType == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA
                || nodeType == ERepositoryObjectType.METADATA_FILE_LDIF) {

        } else {
            return;
        }

        RepositoryNode genericNode = (RepositoryNode) CorePlugin.getDefault().getRepositoryService()
                .getRootRepositoryNode(ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
        if (genericNode == null) {
            return;
        }
        if (ProxyRepositoryFactory.getInstance().getStatus(node.getObject()) == ERepositoryStatus.DELETED) {
            return;
        }
        switch (node.getType()) {
        case REPOSITORY_ELEMENT:
            isAllowedRepositoryElement = true;
            this.sourceNode = node;
            break;
        default:
            return;
        }
        setEnabled(true);
    }

    @Override
    protected void doRun() {
        if (isAllowedRepositoryElement) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            try {
                CopyToGenericSchemaHelper.copyToGenericSchema(factory, this.sourceNode.getObject(), new Path("")); //$NON-NLS-1$
            } catch (PersistenceException e) {
                // e.printStackTrace();
                ExceptionHandler.process(e);
            }
        }
    }
}
