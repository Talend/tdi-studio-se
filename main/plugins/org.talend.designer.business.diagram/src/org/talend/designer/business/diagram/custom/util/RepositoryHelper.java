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
package org.talend.designer.business.diagram.custom.util;

import java.util.Iterator;

import org.eclipse.emf.ecore.EClass;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.SAPConnectionItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.repositoryObject.MetadataTableRepositoryObject;
import org.talend.core.repository.model.repositoryObject.QueryRepositoryObject;
import org.talend.core.repository.model.repositoryObject.SAPFunctionRepositoryObject;
import org.talend.designer.business.model.business.BusinessAssignment;
import org.talend.designer.business.model.business.BusinessFactory;
import org.talend.designer.business.model.business.BusinessPackage;
import org.talend.designer.business.model.business.BusinessProcess;
import org.talend.designer.business.model.business.Repository;
import org.talend.designer.business.model.business.TalendItem;
import org.talend.repository.model.IRepositoryNode.EProperties;
import org.talend.repository.model.RepositoryNode;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class RepositoryHelper {

    public TalendItem getTalendItem(Repository repository, Object object) {
        if (object instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) object;
            if (repositoryNode.getType() == RepositoryNode.ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = repositoryNode.getObject();
                for (Iterator iter = repository.getTalenditems().iterator(); iter.hasNext();) {
                    TalendItem talendItem = (TalendItem) iter.next();
                    if (talendItem.getId().equals(repositoryObject.getId())) {
                        return talendItem;
                    }
                }
            }
        }
        return null;
    }

    public TalendItem createTalendItem(Repository repository, Object object) {
        TalendItem result = null;

        if (object instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) object;
            if (repositoryNode.getType() == RepositoryNode.ENodeType.REPOSITORY_ELEMENT) {
                IRepositoryViewObject repositoryObject = repositoryNode.getObject();
                ERepositoryObjectType nodeType = (ERepositoryObjectType) repositoryNode.getProperties(EProperties.CONTENT_TYPE);

                EClass class1 = getEClass(nodeType);
                if (class1 != null) {
                    result = (TalendItem) BusinessFactory.eINSTANCE.create(class1);
                    result.setRepository(repository);
                    if (ERepositoryObjectType.METADATA_CON_TABLE.equals(repositoryObject.getRepositoryObjectType())) {
                        Property property = ((MetadataTableRepositoryObject) repositoryObject).getProperty();
                        Item item = property.getItem();
                        if (item instanceof SAPConnectionItem) {
                            if (repositoryNode.getParent() != null) {
                                result.setId(property.getId() + " - "
                                        + repositoryNode.getParent().getProperties(EProperties.LABEL) + " - "
                                        + repositoryObject.getLabel());
                            }
                        } else {
                            result.setId(property.getId() + " - " + repositoryObject.getLabel()); //$NON-NLS-1$
                        }
                    } else if (ERepositoryObjectType.METADATA_CON_QUERY.equals(repositoryObject.getRepositoryObjectType())) {
                        Property property = ((QueryRepositoryObject) repositoryObject).getProperty();
                        result.setId(property.getId() + " - " + repositoryObject.getLabel()); //$NON-NLS-1$
                    } else if (ERepositoryObjectType.METADATA_SAP_FUNCTION.equals(repositoryObject.getRepositoryObjectType())) {
                        Property property = ((SAPFunctionRepositoryObject) repositoryObject).getProperty();
                        result.setId(property.getId() + " - " + repositoryObject.getLabel()); //$NON-NLS-1$
                    } else {
                        result.setId(repositoryObject.getId());
                    }
                    result.setLabel(repositoryObject.getLabel());
                }
            }
        }

        return result;
    }

    public void unassignTalendItemsFromBusinessAssignment(BusinessAssignment businessAssignment) {
        TalendItem talendItem = businessAssignment.getTalendItem();
        if (talendItem != null && talendItem.getAssignments() != null) {
            talendItem.getAssignments().remove(businessAssignment);
            if (talendItem.getAssignments().size() == 0) {
                talendItem.getRepository().getTalenditems().remove(talendItem);
            }
        }
    }

    /**
     * DOC mhelleboid Comment method "getEClass".
     *
     * @param type
     * @return
     */
    private EClass getEClass(ERepositoryObjectType type) {
        if (type == ERepositoryObjectType.BUSINESS_PROCESS) {
            return BusinessPackage.eINSTANCE.getBusinessProcess();
        } else if (type == ERepositoryObjectType.PROCESS) {
            return BusinessPackage.eINSTANCE.getProcess();
        } else if (type == ERepositoryObjectType.ROUTINES) {
            return BusinessPackage.eINSTANCE.getRoutine();
        } else if (type == ERepositoryObjectType.DOCUMENTATION || type == ERepositoryObjectType.JOB_DOC
                || type == ERepositoryObjectType.JOBLET_DOC) {
            return BusinessPackage.eINSTANCE.getDocumentation();
        } else if (type == ERepositoryObjectType.METADATA_CONNECTIONS) {
            return BusinessPackage.eINSTANCE.getDatabaseMetadata();
        } else if (type == ERepositoryObjectType.METADATA_SAPCONNECTIONS) {
            return BusinessPackage.eINSTANCE.getSapFunctionMetadata();
        } else if (type == ERepositoryObjectType.METADATA_SAP_FUNCTION) {
            return BusinessPackage.eINSTANCE.getSAPFunction();
        } else if (type == ERepositoryObjectType.METADATA_CON_TABLE) {
            return BusinessPackage.eINSTANCE.getTableMetadata();
        } else if (type == ERepositoryObjectType.METADATA_FILE_DELIMITED) {
            return BusinessPackage.eINSTANCE.getFileDelimitedMetadata();
        } else if (type == ERepositoryObjectType.METADATA_FILE_POSITIONAL) {
            return BusinessPackage.eINSTANCE.getFilePositionalMetadata();
        } else if (type == ERepositoryObjectType.METADATA_FILE_REGEXP) {
            return BusinessPackage.eINSTANCE.getFileRegexpMetadata();
        } else if (type == ERepositoryObjectType.METADATA_FILE_XML) {
            return BusinessPackage.eINSTANCE.getFileXmlMetadata();
        } else if (type == ERepositoryObjectType.METADATA_FILE_EXCEL) {
            return BusinessPackage.eINSTANCE.getFileExcelMetadata();
        } else if (type == ERepositoryObjectType.METADATA_FILE_LDIF) {
            return BusinessPackage.eINSTANCE.getFileLdifMetadata();
        } else if (type == ERepositoryObjectType.CONTEXT) {
            return BusinessPackage.eINSTANCE.getContext();
        } else if (type == ERepositoryObjectType.METADATA_GENERIC_SCHEMA) {
            return BusinessPackage.eINSTANCE.getGenericSchemaMetadata();
        } else if (type == ERepositoryObjectType.METADATA_CON_QUERY) {
            return BusinessPackage.eINSTANCE.getQuery();
        } else if (type == ERepositoryObjectType.JOBLET) {
            return BusinessPackage.eINSTANCE.getJoblet();
        } else if (type == ERepositoryObjectType.METADATA_LDAP_SCHEMA) {
            return BusinessPackage.eINSTANCE.getLdap();
        } else if (type == ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA) {
            return BusinessPackage.eINSTANCE.getSalesforce();
        } else if (type == ERepositoryObjectType.METADATA_WSDL_SCHEMA) {
            return BusinessPackage.eINSTANCE.getWsdl();
        } else if (type == ERepositoryObjectType.SQLPATTERNS) {
            return BusinessPackage.eINSTANCE.getSQLPattern();
        } else if (type == ERepositoryObjectType.METADATA_FILE_EBCDIC) {
            return BusinessPackage.eINSTANCE.getCopybook();
        } else if (type == ERepositoryObjectType.METADATA_MDMCONNECTION) {
            return BusinessPackage.eINSTANCE.getMDM();
        } else if (type == ERepositoryObjectType.getTypeFromKey("Services")) {
            return BusinessPackage.eINSTANCE.getService();
        }
        return null;
    }

    /**
     * DOC mhelleboid Comment method "createLocalRepositoryCopy".
     *
     * @param businessProcess
     * @return
     */
    public Repository createLocalRepositoryCopy(BusinessProcess businessProcess) {
        Repository repository = businessProcess.getLocalRepositoryCopy();
        if (repository == null) {
            repository = BusinessFactory.eINSTANCE.createRepository();
            businessProcess.setLocalRepositoryCopy(repository);
        }
        return repository;
    }
}
