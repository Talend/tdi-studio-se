// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.metadata.builder.connection.CDCConnection;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryContentHandler;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryContentManager;
import org.talend.core.model.update.extension.UpdateManagerProviderDetector;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.ui.actions.AContextualAction;

/**
 * ggu class global comment. Detailled comment
 */
public class DetecteViewImpactAction extends AContextualAction {

    private static final String LABEL = Messages.getString("DetecteViewImpactAction.Label"); //$NON-NLS-1$

    /**
     * ggu DetectedModificationAction constructor comment.
     */
    public DetecteViewImpactAction() {
        setText(LABEL);
        setToolTipText(LABEL);
        setImageDescriptor(ImageProvider.getImageDesc(EImage.REFRESH_ICON));
    }

    public boolean isOnlySimpleShow() {
        // not used
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.actions.ITreeContextualAction#init(org.eclipse.jface.viewers.TreeViewer,
     * org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(TreeViewer viewer, IStructuredSelection selection) {
        // try to check via extension point first.
        boolean canWork = UpdateManagerProviderDetector.INSTANCE.validateAction(viewer, selection);
        if (canWork) {
            setEnabled(true);
            return;
        }
        canWork = !selection.isEmpty() && selection.size() == 1;
        if (canWork) {
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();
            if (factory.isUserReadOnlyOnCurrentProject()) {
                canWork = false;
            } else {
                Object o = selection.getFirstElement();
                RepositoryNode node = (RepositoryNode) o;
                ENodeType nodeType = node.getType();
                switch (nodeType) {
                case REPOSITORY_ELEMENT:
                    ERepositoryObjectType objectType = node.getObjectType();

                    /*
                     * TESB-6415 if it's "CAMEL" product, then is disable
                     */
                    if (objectType != null) {
                        String[] products = objectType.getProducts();
                        if (products != null && products.length == 1 && "CAMEL".equals(products[0])) {
                            setEnabled(false);
                            return;
                        }
                    }// end of TESB-6415

                    if (objectType == ERepositoryObjectType.METADATA_CON_TABLE) {
                        IRepositoryViewObject repositoryObject = node.getObject();
                        if (repositoryObject != null) {
                            Item item2 = repositoryObject.getProperty().getItem();
                            if (item2 instanceof DatabaseConnectionItem) {
                                DatabaseConnectionItem item = (DatabaseConnectionItem) repositoryObject.getProperty().getItem();
                                DatabaseConnection connection = (DatabaseConnection) item.getConnection();
                                CDCConnection cdcConns = connection.getCdcConns();
                                if (cdcConns != null) {
                                    if (repositoryObject instanceof MetadataTableRepositoryObject) {
                                        MetadataTable table = ((MetadataTableRepositoryObject) repositoryObject).getTable();
                                        String tableType = table.getTableType();
                                        canWork = RepositoryConstants.TABLE.equals(tableType);
                                        break;
                                    }
                                }
                            }
                        }
                        canWork = true;
                    } else if (objectType == ERepositoryObjectType.METADATA_CON_QUERY
                            || objectType == ERepositoryObjectType.METADATA_CONNECTIONS
                            || objectType == ERepositoryObjectType.METADATA_FILE_DELIMITED
                            || objectType == ERepositoryObjectType.METADATA_FILE_POSITIONAL
                            || objectType == ERepositoryObjectType.METADATA_FILE_REGEXP
                            || objectType == ERepositoryObjectType.METADATA_FILE_XML
                            || objectType == ERepositoryObjectType.METADATA_FILE_LDIF
                            || objectType == ERepositoryObjectType.METADATA_FILE_EXCEL
                            || objectType == ERepositoryObjectType.METADATA_SAPCONNECTIONS
                            || objectType == ERepositoryObjectType.METADATA_FILE_EBCDIC
                            || objectType == ERepositoryObjectType.METADATA_FILE_HL7
                            || objectType == ERepositoryObjectType.METADATA_VALIDATION_RULES
                            || objectType == ERepositoryObjectType.METADATA_FILE_FTP
                            || objectType == ERepositoryObjectType.METADATA_FILE_BRMS
                            || objectType == ERepositoryObjectType.METADATA_MDMCONNECTION
                            || objectType == ERepositoryObjectType.CONTEXT || objectType == ERepositoryObjectType.JOBLET) {
                        canWork = true;
                    } else if (objectType == ERepositoryObjectType.BUSINESS_PROCESS
                            || objectType == ERepositoryObjectType.PROCESS || objectType == ERepositoryObjectType.ROUTINES
                            || objectType == ERepositoryObjectType.ROUTINESJAR || objectType == ERepositoryObjectType.BEANSJAR
                            || objectType == ERepositoryObjectType.JOB_SCRIPT
                            || objectType == ERepositoryObjectType.SQLPATTERNS || objectType == ERepositoryObjectType.JOB_DOC
                            || objectType == ERepositoryObjectType.JOBLET_DOC
                            || objectType == ERepositoryObjectType.DOCUMENTATION
                            || objectType == ERepositoryObjectType.PROCESS_MR
                            || objectType == ERepositoryObjectType.METADATA_CON_COLUMN
                            ||(ERepositoryObjectType.TEST_CONTAINER != null&&objectType == ERepositoryObjectType.TEST_CONTAINER)) {
                        canWork = false;
                    } else {
                        Object obj = selection.getFirstElement();
                        RepositoryNode nodeObj = (RepositoryNode) obj;
                        Item item = nodeObj.getObject().getProperty().getItem();
                        if (item instanceof ConnectionItem) {
                            if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                                IESBService service = (IESBService) GlobalServiceRegister.getDefault().getService(
                                        IESBService.class);
                                if (service != null) {
                                    boolean flag = service.isServiceItem(item.eClass().getClassifierID());
                                    if (flag) {
                                        canWork = false;
                                        break;
                                    }
                                }
                            }
                        }
                        for (IRepositoryContentHandler handler : RepositoryContentManager.getHandlers()) {
                            ERepositoryObjectType stype = handler.getRepositoryObjectType(item);
                            if (stype == objectType) {
                                canWork = true;
                                break;
                            }
                        }
                        if (RepositoryContentManager.getHandlers().size() < 0) {
                            canWork = false;
                        }

                    }
                    break;
                default:
                    canWork = false;
                }
                RepositoryNode parent = node.getParent();
                if (canWork && parent != null && parent.isBin()) {
                    canWork = false;
                }
                if (canWork && !ProjectManager.getInstance().isInCurrentMainProject(node)) {
                    canWork = false;
                }
            }
        }
        setEnabled(canWork);

    }

    @Override
    protected void doRun() {
        RepositoryNode node = getCurrentRepositoryNode();
        if (node == null) {
            return;
        }
        IStructuredSelection selection = new StructuredSelection(node);
        // try to check via extension point first.
        UpdateManagerProviderDetector.INSTANCE.updateForRepository(selection, true);
    }
}
