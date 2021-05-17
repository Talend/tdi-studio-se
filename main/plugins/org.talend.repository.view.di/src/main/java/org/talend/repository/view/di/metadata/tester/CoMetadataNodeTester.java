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

import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.repository.model.IRepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.tester.AbstractNodeTester;
import org.talend.repository.tester.SubNodeTester;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class CoMetadataNodeTester extends AbstractNodeTester {

    protected SchemaNodeTester schemaTester = new SchemaNodeTester();

    protected SchemaColumnNodeTester schemaColTester = new SchemaColumnNodeTester();

    protected QueryNodeTester queryTester = new QueryNodeTester();

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.tester.AbstractNodeTester#testProperty(java.lang.Object, java.lang.String,
     * java.lang.Object[], java.lang.Object)
     */
    @Override
    protected Boolean testProperty(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof RepositoryNode) {
            RepositoryNode repositoryNode = (RepositoryNode) receiver;

            final ERepositoryObjectType propertyType = findType(property);

            if (propertyType != null) {
                if (repositoryNode.getType() == ENodeType.STABLE_SYSTEM_FOLDER) {
                    // stable folder(Queries,Table schemas, View schemas, Synonym schemas, Columns)
                    Boolean parentTest = testProperty(repositoryNode.getParent(), property, args, expectedValue);
                    if (parentTest != null) { // only do for the checked parent.
                        return parentTest;
                    }
                }

                boolean currentType = isTypeNode(repositoryNode, propertyType);
                /*
                 * check the implication, such as query, schema, column,[CDC] etc
                 */
                boolean schemaTest = checkImplicatedTeser(schemaTester, repositoryNode, ERepositoryObjectType.METADATA_CON_TABLE,
                        propertyType);
                boolean schemaColTest = checkImplicatedTeser(schemaColTester, repositoryNode,
                        ERepositoryObjectType.METADATA_CON_COLUMN, propertyType);
                boolean queryTest = checkImplicatedTeser(queryTester, repositoryNode, ERepositoryObjectType.METADATA_CON_QUERY,
                        propertyType);

                return currentType || schemaTest || schemaColTest || queryTest;
            }
        }
        return null;
    }

    protected abstract ERepositoryObjectType findType(String property);

    protected boolean checkImplicatedTeser(SubNodeTester subTester, RepositoryNode repositoryNode,
            ERepositoryObjectType testerType, ERepositoryObjectType propertyType) {
        return subTester.isTypeNode(repositoryNode, testerType) && (subTester.findParentItemType(repositoryNode) == propertyType);
    }

}
