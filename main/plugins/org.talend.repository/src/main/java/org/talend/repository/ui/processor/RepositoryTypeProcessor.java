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
package org.talend.repository.ui.processor;

import org.eclipse.jface.viewers.Viewer;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.IESBService;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.MetadataTalendType;
import org.talend.core.model.metadata.builder.connection.Connection;
import org.talend.core.model.metadata.builder.connection.HeaderFooterConnection;
import org.talend.core.model.metadata.builder.connection.XmlFileConnection;
import org.talend.core.model.metadata.designerproperties.RepositoryToComponentProperty;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.HeaderFooterConnectionItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;

/**
 * bqian TypeProcessor for Repository. <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class RepositoryTypeProcessor extends SingleTypeProcessor {

    boolean hidenTypeSelection;

    boolean isHeaderButton;

    boolean isGeneric;

    /**
     * DOC bqian RepositoryTypeProcessor constructor comment.
     *
     * @param repositoryType
     */
    public RepositoryTypeProcessor(String repositoryType) {
        super(repositoryType);
    }

    public RepositoryTypeProcessor(String repositoryType, boolean isGeneric) {
        super(repositoryType);
        this.isGeneric = isGeneric;
    }

    @Override
    protected ERepositoryObjectType getType() {
        String repositoryType = getRepositoryType();

        if (repositoryType == null) { // all
            return ERepositoryObjectType.METADATA;
        }
        if (repositoryType.equals(ERepositoryCategoryType.DELIMITED.getName())) {
            return ERepositoryObjectType.METADATA_FILE_DELIMITED;
        }
        if (repositoryType.equals(ERepositoryCategoryType.POSITIONAL.getName())) {
            return ERepositoryObjectType.METADATA_FILE_POSITIONAL;
        }
        if (repositoryType.equals(ERepositoryCategoryType.REGEX.getName())) {
            return ERepositoryObjectType.METADATA_FILE_REGEXP;
        }
        if (repositoryType.equals(ERepositoryCategoryType.XML.getName())
                || repositoryType.equals(ERepositoryCategoryType.XMLOUTPUT.getName())) {
            return ERepositoryObjectType.METADATA_FILE_XML;
        }
        if (repositoryType.equals(ERepositoryCategoryType.LDIF.getName())) {
            return ERepositoryObjectType.METADATA_FILE_LDIF;
        }
        if (repositoryType.equals(ERepositoryCategoryType.EXCEL.getName())) {
            return ERepositoryObjectType.METADATA_FILE_EXCEL;
        }
        if (repositoryType.equals(ERepositoryCategoryType.GENERIC.getName())) {
            return ERepositoryObjectType.METADATA_GENERIC_SCHEMA;
        }
        if (repositoryType.equals(ERepositoryCategoryType.LDAP.getName())) {
            return ERepositoryObjectType.METADATA_LDAP_SCHEMA;
        }
        if (repositoryType.equals(ERepositoryCategoryType.WSDL.getName())) {
            return ERepositoryObjectType.METADATA_WSDL_SCHEMA;
        }
        if (repositoryType.equals(ERepositoryCategoryType.SALESFORCE.getName())) {
            if (isGeneric) {
                return ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA;
            } else {
                // Javajet components: tSalesforceEinsteinBulkExec/tSalesforceEinsteinOutputBulkExec
                repositoryType = repositoryType.toLowerCase();
            }
        }

        if (repositoryType.startsWith(ERepositoryCategoryType.DATABASE.getName())) {
            return ERepositoryObjectType.METADATA_CONNECTIONS;
        }
        if (repositoryType.startsWith(ERepositoryCategoryType.SAP.getName())) {
            return ERepositoryObjectType.METADATA_SAPCONNECTIONS;
        }
        if (repositoryType.startsWith(ERepositoryCategoryType.HEADERFOOTER.getName())) {
            return ERepositoryObjectType.METADATA_HEADER_FOOTER;
        }
        if (repositoryType.equals(ERepositoryCategoryType.EBCDIC.getName())) {
            return ERepositoryObjectType.METADATA_FILE_EBCDIC;
        }
        if (repositoryType.equals(ERepositoryCategoryType.MDM.getName())) {
            return ERepositoryObjectType.METADATA_MDMCONNECTION;
        }
        if (repositoryType.equals(ERepositoryCategoryType.FTP.getName())) {
            return ERepositoryObjectType.METADATA_FILE_FTP;
        }
        if (repositoryType.equals(ERepositoryCategoryType.BRMS.getName())) {
            return ERepositoryObjectType.METADATA_FILE_BRMS;
        }
        if (repositoryType.equals(ERepositoryCategoryType.HL7.getName())) {
            return ERepositoryObjectType.METADATA_FILE_HL7;
        }
        // added by hyWang
        if (repositoryType.equals(ERepositoryCategoryType.RULE.getName())) {
            return ERepositoryObjectType.METADATA_FILE_RULES;
        }
        if (repositoryType.equals(ERepositoryCategoryType.VALIDATIONRULES.getName())) {
            return ERepositoryObjectType.METADATA_VALIDATION_RULES;
        }
        if (repositoryType.equals(ERepositoryCategoryType.EDIFACT.getName())) {
            return ERepositoryObjectType.METADATA_EDIFACT;
        }
        // http://jira.talendforge.org/browse/TESB-5218 LiXiaopeng
        if (repositoryType.equals("SERVICES:OPERATION") || repositoryType.equals("WEBSERVICE")) { //$NON-NLS-1$
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IESBService.class)) {
                IESBService service = GlobalServiceRegister.getDefault().getService(IESBService.class);
                return service.getServicesType();
            }
        }

        // try to use the repositoryType as a key for ERepoObjType, this avoid all the if above that are not extensible
        // friendly.
        ERepositoryObjectType eRepositoryObjectType = ERepositoryObjectType.getTypeFromKey(repositoryType);
        if (eRepositoryObjectType != null) {
            return eRepositoryObjectType;
        }

        return null;
    }

    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        // only for item
        IRepositoryViewObject object = node.getObject();
        if (object != null && object.getProperty().getItem() != null) {
            return true;
        }
        return false;
    }

    public boolean isHidenTypeSelection() {
        return this.hidenTypeSelection;
    }

    public void setHidenTypeSelection(boolean hidenTypeSelection) {
        this.hidenTypeSelection = hidenTypeSelection;
    }

    public boolean isHeaderButton() {
        return this.isHeaderButton;
    }

    public void setHeaderButton(boolean isHeaderButton) {
        this.isHeaderButton = isHeaderButton;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        final String repositoryType = getRepositoryType();
        if (node == null) {
            return false;
        }
        if (node.getContentType() == ERepositoryObjectType.REFERENCED_PROJECTS) {
            return true;
        }

        // ProjectManager pManager = ProjectManager.getInstance();
        // if (!pManager.isInCurrentMainProject(node)) {
        // for sub folders
        if (node.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
            return false;
        }
        // for Db Connections
        if (node.getType() == ENodeType.SYSTEM_FOLDER) {
            return true;
        }
        // }
        IRepositoryViewObject object = node.getObject();
        if (object == null || object.getProperty().getItem() == null) {
            return false;
        }
        if (object instanceof MetadataTable) {
            return false;
        }
        Item item = object.getProperty().getItem();
        if (item instanceof FolderItem) {
            return true;
        }

        if (item instanceof ConnectionItem) {
            ConnectionItem connectionItem = (ConnectionItem) item;
            Connection connection = connectionItem.getConnection();
            // tAdvancedFileOutputXML
            if (repositoryType != null && repositoryType.equals(ERepositoryCategoryType.XMLOUTPUT.getName())) {
                if (connection instanceof XmlFileConnection && ((XmlFileConnection) connection).isInputModel()) {
                    return false;
                }
            }

            if (repositoryType.startsWith(ERepositoryCategoryType.DATABASE.getName())) {
                String currentDbType = (String) RepositoryToComponentProperty.getValue(connection, "TYPE", null); //$NON-NLS-1$
                if (repositoryType.contains(":")) { // database //$NON-NLS-1$
                    // is
                    // specified
                    // //$NON-NLS-1$
                    String neededDbType = repositoryType.substring(repositoryType.indexOf(":") + 1); //$NON-NLS-1$
                    if (hidenTypeSelection) {
                        return true;
                    }
                    if (!MetadataTalendType.sameDBProductType(neededDbType, currentDbType)) {
                        return false;
                    }
                    if(isGeneric && connection.getCompProperties() == null){
                        return false;
                    }
                }
            }
        }
        if (repositoryType.startsWith(ERepositoryCategoryType.HEADERFOOTER.getName())) {
            if (item instanceof HeaderFooterConnectionItem) {
                HeaderFooterConnectionItem connectionItem = (HeaderFooterConnectionItem) item;
                HeaderFooterConnection connection = (HeaderFooterConnection) connectionItem.getConnection();
                boolean isHeader = connection.isIsHeader();

                if ((isHeader && isHeaderButton) || (!isHeader && !isHeaderButton)) {
                    return true;
                } else {
                    return false;
                }

            }
        }
        return true;
    }

}
