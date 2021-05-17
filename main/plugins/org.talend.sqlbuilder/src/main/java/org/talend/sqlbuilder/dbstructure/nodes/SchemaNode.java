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

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: SchemaNode.java,v 1.4 2006/11/01 05:40:59 peiqin.hou Exp $
 *
 */
public class SchemaNode extends AbstractNode {

    private static final int END_INDEX = 28;

    private List pchildNames = new ArrayList();

    private String[] pfilteredNames;

    /**
     * Create new database Schema node.
     *
     * @param parent node
     * @param name of this node
     * @param sessionNode session for this node
     */
    public SchemaNode(INode parent, String name, SessionTreeNode sessionNode) {

        psessionNode = sessionNode;
        pparent = parent;
        pname = name;

        pimageKey = "Images.SchemaNodeIcon"; //$NON-NLS-1$
    }

    /**
     * Add Extension nodes.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private void addExtensionNodes() {

        String databaseProductName = getSession().getRoot().getDatabaseProductName().toLowerCase().trim();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "node"); //$NON-NLS-1$ //$NON-NLS-2$
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    // include only nodes that are attachted to the schema
                    // node..
                    String parent = ces[j].getAttribute("parent-node"); //$NON-NLS-1$
                    if (parent.indexOf("schema") == -1) { //$NON-NLS-1$
                        continue;
                    }

                    boolean isValidProduct = false;
                    String[] validProducts = ces[j].getAttribute("database-product-name").split(","); //$NON-NLS-1$ //$NON-NLS-2$

                    // include only nodes valid for this database
                    for (int k = 0; k < validProducts.length; k++) {

                        String product = validProducts[k].toLowerCase().trim();

                        if (product.length() == 0) {
                            continue;
                        }

                        if (product.equals("*")) { //$NON-NLS-1$
                            isValidProduct = true;
                            break;
                        }

                        String regex = TextUtil.replaceChar(product, '*', ".*"); //$NON-NLS-1$
                        if (databaseProductName.matches(regex)) {
                            isValidProduct = true;
                            break;
                        }

                    }

                    if (!isValidProduct) {
                        continue;
                    }

                    String imagePath = ces[j].getAttribute("icon"); //$NON-NLS-1$
                    String id = ces[j].getAttribute("id"); //$NON-NLS-1$
                    String type = ces[j].getAttribute("table-type").trim(); //$NON-NLS-1$

                    AbstractNode childNode = (AbstractNode) ces[j].createExecutableExtension("class"); //$NON-NLS-1$
                    childNode.setParent(this);
                    childNode.setSession(psessionNode);
                    childNode.setType(type);

                    String fragmentId = id.substring(0, id.indexOf('.', END_INDEX));
                    if (imagePath != null && imagePath.trim().length() != 0) {
                        childNode.setImage(ImageUtil.getFragmentImage(fragmentId, imagePath));
                    }

                    pchildNames.add(childNode.getLabelText());
                    if (!isExcludedByFilter(childNode.getLabelText())) {
                        addChildNode(childNode);
                    }

                } catch (Throwable ex) {
                    SqlBuilderPlugin.log(Messages.getString("SchemaNode.logMessage1"), ex); //$NON-NLS-1$
                }
            }
        }

    }

    /**
     * Location extenstion nodes for a given tableType.
     *
     * @param tableType for which to find extension node
     * @return INode or null if no extensions found
     */
    private INode findExtensionNode(String tableType) {

        String databaseProductName = getSession().getRoot().getDatabaseProductName().toLowerCase().trim();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "node"); //$NON-NLS-1$ //$NON-NLS-2$
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    // include only nodes that are attachted to the schema
                    // node..
                    String parent = ces[j].getAttribute("parent-node"); //$NON-NLS-1$
                    if (parent.indexOf("schema") == -1) { //$NON-NLS-1$
                        continue;
                    }

                    boolean isValidProduct = false;
                    String[] validProducts = ces[j].getAttribute("database-product-name").split(","); //$NON-NLS-1$ //$NON-NLS-2$

                    // include only nodes valid for this database
                    for (int k = 0; k < validProducts.length; k++) {

                        String product = validProducts[k].toLowerCase().trim();

                        if (product.length() == 0) {
                            continue;
                        }

                        if (product.equals("*")) { //$NON-NLS-1$
                            isValidProduct = true;
                            break;
                        }

                        String regex = TextUtil.replaceChar(product, '*', ".*"); //$NON-NLS-1$
                        if (databaseProductName.matches(regex)) {
                            isValidProduct = true;
                            break;
                        }

                    }

                    if (!isValidProduct) {
                        continue;
                    }

                    // check if it is the correct type
                    String type = ces[j].getAttribute("table-type").trim(); //$NON-NLS-1$
                    if (!type.equalsIgnoreCase(tableType)) {
                        continue;
                    }

                    AbstractNode childNode = (AbstractNode) ces[j].createExecutableExtension("class"); //$NON-NLS-1$
                    childNode.setParent(this);
                    childNode.setSession(psessionNode);

                    return childNode;

                } catch (Throwable ex) {
                    SqlBuilderPlugin.log(Messages.getString("SchemaNode.logMessage2"), ex); //$NON-NLS-1$
                }
            }
        }

        return null;
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
     * Returns "schema" as the type for this node.
     *
     * @return Type.
     * @see org.talend.sqlbuilder.dbstructure.nodes.INode#getType()
     */
    public String getType() {

        return "schema"; //$NON-NLS-1$
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
                return true;
            }
        }

        // no match found
        return false;

    }

    /**
     * LoadChildren.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void loadChildren() {

        pchildNames = new ArrayList();

        try {

            ITableInfo[] tables = null;
            String[] tableTypes = psessionNode.getMetaData().getTableTypes();

            try {
                tables = psessionNode.getMetaData().getTables(pname, pname, "%", tableTypes); //$NON-NLS-1$
            } catch (Throwable e) {
                SqlBuilderPlugin.log(Messages.getString("SchemaNode.logMessage3"), e); //$NON-NLS-1$
            }

            for (int i = 0; i < tableTypes.length; ++i) {

                INode childNode = findExtensionNode(tableTypes[i]);

                if (childNode != null) {
                    pchildNames.add(childNode.getLabelText());
                    if (!isExcludedByFilter(childNode.getLabelText())) {
                        addChildNode(childNode);
                    }
                } else {
                    TableFolderNode node = new TableFolderNode(this, tableTypes[i], psessionNode, tables);
                    pchildNames.add(node.getLabelText());
                    if (!isExcludedByFilter(node.getLabelText())) {
                        addChildNode(node);
                    }
                }
            }

            // load extension nodes
            addExtensionNodes();

        } catch (Throwable e) {

            SqlBuilderPlugin.log(Messages.getString("SchemaNode.logMessage4") + pname, e); //$NON-NLS-1$
        }

    }
}
