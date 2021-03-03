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
package org.talend.repository.view.di.metadata.tester;

import org.talend.core.hadoop.IHadoopClusterService;
import org.talend.core.hadoop.repository.HadoopRepositoryUtil;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.RepositoryNode;

/**
 *
 * DOC ggu class global comment. Detailled comment
 */
public class DIMetadatasNodeTester extends CoMetadataNodeTester {

    /*
     * children
     */
    private static final String IS_DB_CONNECTION = "isDbConnection"; //$NON-NLS-1$

    private static final String IS_FILE_DELIMITED = "isFileDelimited"; //$NON-NLS-1$

    private static final String IS_FILE_POSITIONAL = "isFilePositional"; //$NON-NLS-1$

    private static final String IS_FILE_REGEXP = "isFileRegexp"; //$NON-NLS-1$

    private static final String IS_FILE_XML = "isFileXML"; //$NON-NLS-1$

    private static final String IS_FILE_EXCEL = "isFileExcel"; //$NON-NLS-1$

    private static final String IS_FILE_LDIF = "isFileLDIF"; //$NON-NLS-1$

    private static final String IS_LDAP = "isLDAP"; //$NON-NLS-1$

    private static final String IS_SALESFORCE = "isSalesforce"; //$NON-NLS-1$

    private static final String IS_GENERIC_SCHEMA = "isGenericSchema"; //$NON-NLS-1$

    private static final String IS_WEB_SERVICE = "isWebService"; //$NON-NLS-1$

    private static final String IS_MDM = "isMDM"; //$NON-NLS-1$

    @Override
    protected ERepositoryObjectType findType(String property) {
        if (property != null) {
            if (IS_DB_CONNECTION.equals(property)) {
                return ERepositoryObjectType.METADATA_CONNECTIONS;
            } else if (IS_FILE_DELIMITED.equals(property)) {
                return ERepositoryObjectType.METADATA_FILE_DELIMITED;
            } else if (IS_FILE_POSITIONAL.equals(property)) {
                return ERepositoryObjectType.METADATA_FILE_POSITIONAL;
            } else if (IS_FILE_REGEXP.equals(property)) {
                return ERepositoryObjectType.METADATA_FILE_REGEXP;
            } else if (IS_FILE_XML.equals(property)) {
                return ERepositoryObjectType.METADATA_FILE_XML;
            } else if (IS_FILE_EXCEL.equals(property)) {
                return ERepositoryObjectType.METADATA_FILE_EXCEL;
            } else if (IS_FILE_LDIF.equals(property)) {
                return ERepositoryObjectType.METADATA_FILE_LDIF;
            } else if (IS_LDAP.equals(property)) {
                return ERepositoryObjectType.METADATA_LDAP_SCHEMA;
            } else if (IS_SALESFORCE.equals(property)) {
                return ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA;
            } else if (IS_GENERIC_SCHEMA.equals(property)) {
                return ERepositoryObjectType.METADATA_GENERIC_SCHEMA;
            } else if (IS_WEB_SERVICE.equals(property)) {
                return ERepositoryObjectType.METADATA_WSDL_SCHEMA;
            } else if (IS_MDM.equals(property)) {
                return ERepositoryObjectType.METADATA_MDMCONNECTION;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.tester.AbstractNodeTester#isTypeNode(org.talend.repository.model.RepositoryNode,
     * org.talend.core.model.repository.ERepositoryObjectType)
     */
    @Override
    public boolean isTypeNode(RepositoryNode repositoryNode, ERepositoryObjectType type) {
        if (ERepositoryObjectType.METADATA_CONNECTIONS.equals(type)) {
            IHadoopClusterService hadoopClusterService = HadoopRepositoryUtil.getHadoopClusterService();
            if (hadoopClusterService != null && hadoopClusterService.isHadoopFolderNode(repositoryNode.getParent())) {
                return false;
            }
        }
        return super.isTypeNode(repositoryNode, type);
    }

}
