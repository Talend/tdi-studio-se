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
import java.util.List;

import net.sourceforge.sqlexplorer.SQLAlias;
import net.sourceforge.squirrel_sql.fw.sql.ITableInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * Database catalog node.
 *
 * @author Davy Vanherbergen
 *
 */
public class CatalogNode extends AbstractNode {

    private List pchildNames = new ArrayList();

    private String[] pfilteredNames;

    /**
     * Create new database Catalog node.
     *
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public CatalogNode(INode parent, String name, SessionTreeNode sessionNode) {

        psessionNode = sessionNode;
        pparent = parent;
        pname = name;

        pimageKey = "Images.CatalogNodeIcon"; //$NON-NLS-1$
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
     * Returns "catalog" as the type for this node.
     *
     * @return Type.
     * @see org.talend.sqlbuilder.dbstructure.nodes.INode#getType()
     */
    public String getType() {

        return "catalog"; //$NON-NLS-1$
    }

    /**
     * @return UniqueIdentifier.
     */
    public String getUniqueIdentifier() {

        return getQualifiedName();
    }

    /**
     * Checks if a node name should be filtered.
     *
     * @param name to check for filtering
     * @return true if the name should be filtered
     */
    protected boolean isExcludedByFilter(String name) {

        if (pfilteredNames == null) {
            String filter = ((SQLAlias) getSession().getAlias()).getFolderFilterExpression();
            if (filter != null) {
                pfilteredNames = filter.split(","); //$NON-NLS-1$
            }
        }
        if (pfilteredNames == null || pfilteredNames.length == 0) {
            // no active filter
            return false;
        }

        for (int i = 0; i < pfilteredNames.length; i++) {

            if (pfilteredNames[i].equalsIgnoreCase(name)) {
                // we have a match, exclude node..
                return false;
            }
        }

        // no match found
        return true;

    }

    /**
     * LoadChildren.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void loadChildren() {

        if (psessionNode.getInteractiveConnection() == null) {
            TableFolderNode node = new TableFolderNode(this, "TABLE", psessionNode, new ITableInfo[0]); //$NON-NLS-1$
            addChildNode(node);
            return;
        }

        pchildNames = new ArrayList();

        try {

            ITableInfo[] tables = null;
            // String[] tableTypes = psessionNode.getMetaData().getTableTypes();
            String[] tableTypes = { "TABLE", "VIEW", "SYNONYM" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            try {
                tables = psessionNode.getMetaData().getTables(null, getSchemaName(), "%", tableTypes); //$NON-NLS-1$

            } catch (Throwable e) {
                SqlBuilderPlugin.log(Messages.getString("CatalogNode.logMessage1"), e); //$NON-NLS-1$
            }

            TableFolderNode node = new TableFolderNode(this, "TABLE", psessionNode, tables); //$NON-NLS-1$
            pchildNames.add(node.getLabelText());
            if (!isExcludedByFilter(node.getLabelText())) {
                addChildNode(node);
            }

        } catch (Throwable e) {
            SqlBuilderPlugin.log(Messages.getString("CatalogNode.logMessage2", pname), e); //$NON-NLS-1$
        }
    }

    /**
     * @return Image.
     */
    public Image getImage() {
        return CoreImageProvider.getImage(ERepositoryObjectType.METADATA_CONNECTIONS);
    }

    /**
     * @return Column label.
     * @param columnIndex column index.
     */
    @Override
    public String getLabelAtColumn(int columnIndex) {
        if (columnIndex == 0) {
            if (getSchemaName() != null && !getSchemaName().trim().equals("") && getLabelText() != null //$NON-NLS-1$
                    && !getLabelText().trim().equals("")) { //$NON-NLS-1$
                return getLabelText() + "." + getSchemaName(); //$NON-NLS-1$
            } else {
                return getLabelText();
            }
        } else if (columnIndex == 1) {
            return psessionNode.getRepositoryNode() == null ? null : psessionNode.getRepositoryName();
        }
        return null;
    }

    /**
     * @return SchemaName.
     */
    public String getSchemaName() {

        DatabaseConnection databaseConnection = getSession().getDatabaseConnection();
        if (databaseConnection == null) {
            return null;
        }
        String schema = databaseConnection.getUiSchema();
        if (schema != null && schema.length() == 0) {
            return null;
        }
        return ExtractMetaDataUtils.getInstance().getSchema();
    }

    /**
     * @return Background color.
     */
    @Override
    public Color getBackground() {
        if (getLabelText() == null || getLabelText().trim().equals("")) { //$NON-NLS-1$
            return Display.getDefault().getSystemColor(SWT.COLOR_RED);
        } else {
            return super.getBackground();
        }
    }

    /**
     * @return Foreground color.
     */
    @Override
    public Color getForeground() {
        if (getLabelText() == null || getLabelText().trim().equals("")) { //$NON-NLS-1$
            return Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
        } else {
            return super.getBackground();
        }
    }

    /**
     * @return ChildNodes.
     */
    @Override
    public INode[] getChildNodes() {
        INode[] nodes = super.getChildNodes();

        if (nodes == null || nodes.length < 1) {
            return nodes;
        }
        return nodes[0].getChildNodes();
    }

}
