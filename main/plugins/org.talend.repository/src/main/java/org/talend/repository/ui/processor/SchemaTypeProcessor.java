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
package org.talend.repository.ui.processor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.metadata.Query;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.param.ERepositoryCategoryType;
import org.talend.core.model.properties.DatabaseConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryContentHandler;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.model.repository.RepositoryContentManager;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.runtime.services.IGenericWizardService;
import org.talend.core.ui.ICDCProviderService;
import org.talend.repository.model.RepositoryNode;

/**
 * bqian TypeProcessor for Schema. <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class SchemaTypeProcessor extends MultiTypesProcessor {

    /**
     * DOC bqian RepositoryTypeProcessor constructor comment.
     *
     * @param repositoryType
     */
    public SchemaTypeProcessor(String repositoryType) {
        super(new String[] { repositoryType });
    }

    protected String getRepositoryType() {
        return getRepositoryTypes()[0];
    }

    @Override
    protected List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> list = new ArrayList<ERepositoryObjectType>(50);
        String repositoryType = getRepositoryType();
        if (repositoryType != null && repositoryType.startsWith(ERepositoryCategoryType.DATABASE.getName())) {
            list.add(ERepositoryObjectType.METADATA_CONNECTIONS);
        } else {
            list.add(ERepositoryObjectType.METADATA_CONNECTIONS);
            list.add(ERepositoryObjectType.METADATA_FILE_DELIMITED);
            list.add(ERepositoryObjectType.METADATA_FILE_POSITIONAL);
            list.add(ERepositoryObjectType.METADATA_FILE_REGEXP);
            list.add(ERepositoryObjectType.METADATA_FILE_XML);
            list.add(ERepositoryObjectType.METADATA_FILE_LDIF);
            list.add(ERepositoryObjectType.METADATA_FILE_EXCEL);
            list.add(ERepositoryObjectType.METADATA_FILE_HL7);
            list.add(ERepositoryObjectType.METADATA_FILE_EBCDIC);
            list.add(ERepositoryObjectType.METADATA_FILE_FTP);
            list.add(ERepositoryObjectType.METADATA_FILE_BRMS);
            list.add(ERepositoryObjectType.METADATA_GENERIC_SCHEMA);
            list.add(ERepositoryObjectType.METADATA_LDAP_SCHEMA);
            list.add(ERepositoryObjectType.METADATA_WSDL_SCHEMA);
            list.add(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA);
            list.add(ERepositoryObjectType.METADATA_SAPCONNECTIONS);
            list.add(ERepositoryObjectType.METADATA_RULES_MANAGEMENT);
            list.add(ERepositoryObjectType.METADATA_MDMCONNECTION);
            list.add(ERepositoryObjectType.METADATA_EDIFACT);

            // Add the type of extended node which has schemas.
            for (IRepositoryContentHandler handler : RepositoryContentManager.getHandlers()) {
                if (handler.hasSchemas() && handler.getHandleType() != null) {
                    list.add(handler.getHandleType());
                }
            }

            // Add generic nodes.
            IGenericWizardService wizardService = null;
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
                wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault()
                        .getService(IGenericWizardService.class);
            }
            if (wizardService != null) {
                List<String> genericTypeNames = wizardService.getGenericTypeNames();
                for (String genericType : genericTypeNames) {
                    ERepositoryObjectType repObjType = ERepositoryObjectType.valueOf(genericType);
                    if (repObjType != null) {
                        list.add(repObjType);
                    }
                }
            }

        }
        return list;
    }

    @Override
    public boolean isSelectionValid(RepositoryNode node) {
        if (node.getObject() instanceof MetadataTable) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean selectRepositoryNode(Viewer viewer, RepositoryNode parentNode, RepositoryNode node) {
        if (super.selectRepositoryNode(viewer, parentNode, node)) {
            IRepositoryViewObject object = node.getObject();
            if (object != null) {
                // query
                if (object instanceof Query) {
                    return false;
                }
            }
            // cdc
            ICDCProviderService cdcService = null;
            if (PluginChecker.isCDCPluginLoaded()) {
                cdcService = (ICDCProviderService) GlobalServiceRegister.getDefault().getService(ICDCProviderService.class);
            }
            String repositoryType = getRepositoryType();
            if (ERepositoryCategoryType.CDC.getName().equals(repositoryType) && (object != null)) {
                if (object.getRepositoryObjectType() == ERepositoryObjectType.METADATA_CONNECTIONS) {
                    DatabaseConnectionItem item = (DatabaseConnectionItem) object.getProperty().getItem();
                    DatabaseConnection connection = (DatabaseConnection) item.getConnection();

                    if (cdcService != null && cdcService.canCreateCDCConnection(connection)) {
                        return true;
                    }
                    return false;
                }
                if (object instanceof MetadataTable) {
                    return ((MetadataTableRepositoryObject) object).getTable().isActivatedCDC();
                }
            }
            return true;
        }
        return false;
    }

}
