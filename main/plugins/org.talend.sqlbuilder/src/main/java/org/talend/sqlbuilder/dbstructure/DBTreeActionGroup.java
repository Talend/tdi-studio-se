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
package org.talend.sqlbuilder.dbstructure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.sqlexplorer.plugin.views.DatabaseStructureView;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionGroup;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.actions.AbstractDBTreeContextAction;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * ActionGroup for Database Structure View. This group controls what context
 * menu actions are being shown for which node.
 *
 * @author Davy Vanherbergen
 */
public class DBTreeActionGroup extends ActionGroup {

    private static final int END_INDEX = 28;

    private TreeViewer pTreeViewer;

    private DatabaseStructureView pView;


    /**
     * Construct a new action group for a given database structure outline.
     *
     * @param treeViewer TreeViewer used for this outline.
     */
    public DBTreeActionGroup(TreeViewer treeViewer, DatabaseStructureView view) {

        pTreeViewer = treeViewer;
        pView = view;
    }


    /**
     * Fill the node context menu with all the correct actions.
     *
     * @param menu MenuManager.
     * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public void fillContextMenu(IMenuManager menu) {

        // find our target node..
        IStructuredSelection selection = (IStructuredSelection) pTreeViewer.getSelection();

        // check if we have a valid selection
        if (selection == null) {
            return;
        }

        ArrayList selectedNodes = new ArrayList();
        Iterator it = selection.iterator();

        while (it.hasNext()) {
            Object object = it.next();
            if (object instanceof INode) {
                selectedNodes.add(object);
            }
        }

        if (selectedNodes.size() == 0) {
            return;
        }

        INode[] nodes = (INode[]) selectedNodes.toArray(new INode[] {});
        IAction[] actions = getContextActions(nodes);

        for (int i = 0; i < actions.length; i++) {
            menu.add(actions[i]);
        }

    }


    /**
     * Loop through all extensions and add the appropriate actions.
     *
     * Actions are selected by database product name, node type and
     * availability.
     *
     * @param nodes currently selected nodes
     * @return array of actions
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    private IAction[] getContextActions(INode[] nodes) {

        String databaseProductName = nodes[0].getSession().getRoot().getDatabaseProductName().toLowerCase().trim();
        String nodeType = nodes[0].getType().toLowerCase().trim();

        List actions = new ArrayList();

        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("net.sourceforge.sqlexplorer", "nodeContextAction"); //$NON-NLS-1$ //$NON-NLS-2$
        IExtension[] extensions = point.getExtensions();

        for (int i = 0; i < extensions.length; i++) {

            IExtension e = extensions[i];

            IConfigurationElement[] ces = e.getConfigurationElements();

            for (int j = 0; j < ces.length; j++) {
                try {

                    boolean isValidProduct = false;
                    boolean isValidNodeType = false;

                    String id = ces[j].getAttribute("id"); //$NON-NLS-1$
                    String[] validProducts = ces[j].getAttribute("database-product-name").split(","); //$NON-NLS-1$ //$NON-NLS-2$
                    String[] validNodeTypes = ces[j].getAttribute("node-type").split(","); //$NON-NLS-1$ //$NON-NLS-2$
                    String imagePath = ces[j].getAttribute("icon"); //$NON-NLS-1$

                    // check if action is valid for current database product
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

                    // check if action is valid for current node type
                    for (int k = 0; k < validNodeTypes.length; k++) {

                        String type = validNodeTypes[k].toLowerCase().trim();

                        if (type.length() == 0) {
                            continue;
                        }

                        if (type.equals("*")) { //$NON-NLS-1$
                            isValidNodeType = true;
                            break;
                        }

                        String regex = TextUtil.replaceChar(type, '*', ".*"); //$NON-NLS-1$
                        if (nodeType.matches(regex)) {
                            isValidNodeType = true;
                            break;
                        }

                    }

                    if (!isValidNodeType) {
                        continue;
                    }

                    // check if the action thinks it is suitable..
                    AbstractDBTreeContextAction action = (AbstractDBTreeContextAction) ces[j].createExecutableExtension("class"); //$NON-NLS-1$
                    action.setSelectedNodes(nodes);
                    action.setTreeViewer(pTreeViewer);
                    action.setView(pView);

                    String fragmentId = id.substring(0, id.indexOf('.', END_INDEX));

                    if (imagePath != null && imagePath.trim().length() != 0) {
                        action.setImageDescriptor(ImageUtil.getFragmentDescriptor(fragmentId, imagePath));
                    }

                    if (action.isAvailable()) {
                        actions.add(action);
                    }

                } catch (Throwable ex) {
                    SqlBuilderPlugin.log(Messages.getString("DBTreeActionGroup.logMessage"), ex); //$NON-NLS-1$
                }
            }
        }

        return (IAction[]) actions.toArray(new IAction[] {});
    }

}
