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
package org.talend.sqlbuilder.dbstructure.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.SQLAlias;

import org.eclipse.swt.graphics.Image;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * Root node for a database. ChildNodes can be filtered based on expressions in the alias.
 *
 * @author Davy Vanherbergen
 */
public class DatabaseNode extends AbstractNode {

    private SQLAlias palias;

    private List pchildNames = new ArrayList();

    private String pdatabaseProductName = ""; //$NON-NLS-1$

    private boolean psupportsCatalogs = false;

    private boolean psupportsSchemas = false;

    /**
     * Create a new database node with the given name.
     *
     * @param name name
     * @param session session
     */
    public DatabaseNode(String name, SessionTreeNode session) {

        pname = name;
        psessionNode = session;
        palias = (SQLAlias) psessionNode.getAlias();
        pimageKey = "Images.DatabaseIcon"; //$NON-NLS-1$
    }

    /**
     * @return List of catalog nodes
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public List getCatalogs() {

        ArrayList catalogs = new ArrayList();

        Iterator it = getChildIterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof CatalogNode) {
                catalogs.add(o);
            }
        }

        return catalogs;
    }

    /**
     * @return ChildNames.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public String[] getChildNames() {

        if (pchildNames.size() == 0) {
            getChildNodes();
        }
        return (String[]) pchildNames.toArray(new String[] {});
    }

    /**
     * @return DatabaseProductName.
     */
    public String getDatabaseProductName() {

        return pdatabaseProductName;
    }

    /**
     * @return LabelText.
     */
    public String getLabelText() {
        return palias.getName();
    }

    /**
     * @return List of all database schemas
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public List getSchemas() {

        ArrayList schemas = new ArrayList();

        Iterator it = getChildIterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof SchemaNode) {
                schemas.add(o);
            }
        }

        return schemas;
    }

    /**
     * Returns "database" as the type for this node.
     *
     * @return Type.
     * @see org.talend.sqlbuilder.dbstructure.nodes.INode#getType()
     */
    public String getType() {

        return "database"; //$NON-NLS-1$
    }

    /**
     * @return UniqueIdentifier.
     */
    public String getUniqueIdentifier() {

        return getQualifiedName();
    }

    /**
     * Loads childnodes, filtered to a subset of schemas/databases depending on whether a comma separated list of
     * regular expression filters has been set.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void loadChildren() {

        if (psessionNode.getInteractiveConnection() == null) {
            addChildNode(new CatalogNode(this, "", psessionNode)); //$NON-NLS-1$
            return;
        }

        pchildNames = new ArrayList();

        try {

            // if (metadata.supportsCatalogs()) {
            //
            // final String[] catalogs = metadata.getCatalogs();
            // for (int i = 0; i < catalogs.length; ++i) {
            // pchildNames.add(catalogs[i]);
            // if (!isExcludedByFilter(catalogs[i])) {
            // addChildNode(new CatalogNode(this, catalogs[i], psessionNode));
            // }
            // }
            //
            // } else if (metadata.supportsSchemas()) {
            //
            // final String[] schemas = metadata.getSchemas();
            // for (int i = 0; i < schemas.length; ++i) {
            // pchildNames.add(schemas[i]);
            // if (!isExcludedByFilter(schemas[i])) {
            // addChildNode(new SchemaNode(this, schemas[i], psessionNode));
            // }
            // }
            //
            // } else {

            addChildNode(new CatalogNode(this, palias.getSchemaFilterExpression(), psessionNode));
            // }

        } catch (Exception e) {
            SqlBuilderPlugin.log(Messages.getString("DatabaseNode.logMessage"), e); //$NON-NLS-1$
        }

    }

    /**
     * @return true if this database supports catalogs
     */
    public boolean supportsCatalogs() {

        return psupportsCatalogs;
    }

    /**
     * @return true if this database supports schemas
     */
    public boolean supportsSchemas() {

        return psupportsSchemas;
    }

    /**
     * @return Image.
     */
    public Image getImage() {
        return CoreImageProvider.getImage(ERepositoryObjectType.METADATA_CONNECTIONS);
    }

}
