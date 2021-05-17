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
package org.talend.sqlbuilder.dbdetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbdetail.tab.ColumnInfoTab;
import org.talend.sqlbuilder.dbdetail.tab.ConnectionInfoTab;
import org.talend.sqlbuilder.dbdetail.tab.ExportedKeysTab;
import org.talend.sqlbuilder.dbdetail.tab.ImportedKeysTab;
import org.talend.sqlbuilder.dbdetail.tab.IndexesTab;
import org.talend.sqlbuilder.dbdetail.tab.PreviewTab;
import org.talend.sqlbuilder.dbdetail.tab.PrimaryKeysTab;
import org.talend.sqlbuilder.dbdetail.tab.RowCountTab;
import org.talend.sqlbuilder.dbstructure.nodes.DatabaseNode;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.dbstructure.nodes.TableNode;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * Controls creation of detail tabs for all nodes. All detail tabs are cached.
 *
 * <br/>
 *
 * @author yzhang
 *
 */
public class DetailTabManager {

    private static String pActiveTabName = null;

    private static final HashMap CONSTANT_SESSION_TAB_CACHE = new HashMap();


    /**
     * Clear the detail tab cache for a given node.
     *
     * @param node INode to remove from cache.
     */
    public static void clearCacheForNode(INode node) {

//        if (CONSTANT_LOGGER.isDebugEnabled()) {
//            CONSTANT_LOGGER.debug("Clearing tab cache for: " + node.getUniqueIdentifier());
//        }

        HashMap tabCache = (HashMap) CONSTANT_SESSION_TAB_CACHE.get(node.getSession());
        tabCache.remove(node.getUniqueIdentifier());

    }


    /**
     * Clear cache of a given session. This method is called when a session is
     * closed or when the database node is refreshed.
     *
     * @param session SessionTreeNode
     */
    public static void clearCacheForSession(SessionTreeNode session) {

//        if (CONSTANT_LOGGER.isDebugEnabled()) {
//            CONSTANT_LOGGER.debug("Clearing tab cache for: " + session.toString());
//        }

        CONSTANT_SESSION_TAB_CACHE.remove(session);
    }


    /**
     * Creates all the tabs in the detail pane to display the information for a
     * given node.
     *
     * @param composite
     * @param node
     */
    public static void createTabs(Composite composite, INode node) {

        List tabs = getTabs(node);

        if (tabs == null || tabs.size() == 0) {
            // no detail found..

            Label label = new Label(composite, SWT.FILL | SWT.WRAP);
            label.setText(Messages.getString("DatabaseDetailView.Tab.Unavailable") + " " + node.getLabelText()); //$NON-NLS-1$ //$NON-NLS-2$
            //label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

            return;
        }

        // create tabs
        CTabFolder tabFolder = new CTabFolder(composite, SWT.NULL);
        tabFolder.setSimple(false);
        // only init tabs when the tab becomes active
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                CTabItem tabItem = (CTabItem) e.item;
                IDetailTab tab = (IDetailTab) tabItem.getData();
                if (tab != null) {

                    // create composite on tab and fill it..
                    Composite detailComposite = new Composite(tabItem.getParent(), SWT.FILL);
                    tabItem.setControl(detailComposite);
                    detailComposite.setLayout(new FillLayout());
                    tab.fillComposite(detailComposite);
                    detailComposite.layout();

                    // store tab name, so we can reselect when other node is
                    // chosen
                    DetailTabManager.setActiveTabName(tabItem.getText());
                }
            }

        });

        // add tabs to folder
        Iterator it = tabs.iterator();
        int tabIndex = 0;

        while (it.hasNext()) {

            IDetailTab detailTab = (IDetailTab) it.next();

            // create tab
            CTabItem tabItem = new CTabItem(tabFolder, SWT.NULL);
            tabItem.setText(detailTab.getLabelText());
            tabItem.setToolTipText(detailTab.getLabelToolTipText());

            // store tab so we can fill later
            tabItem.setData(detailTab);

            // reselect same tab as was previous selected
            if (tabItem.getText() != null && pActiveTabName != null) {
                if (tabItem.getText().equals(pActiveTabName)) {
                    tabFolder.setSelection(tabIndex);
                }
            }

            tabIndex++;
        }

        // load data for active tab, default to first one if none is selected
        tabIndex = tabFolder.getSelectionIndex();
        if (tabIndex == -1) {
            tabIndex = 0;
        }

        CTabItem tabItem = tabFolder.getItem(tabIndex);
        if (tabItem != null) {
            Composite detailComposite = new Composite(tabItem.getParent(), SWT.FILL);
            tabItem.setControl(detailComposite);
            detailComposite.setLayout(new FillLayout());
            IDetailTab tab = (IDetailTab) tabItem.getData();
            tab.fillComposite(detailComposite);
            detailComposite.layout();
        }
        if (tabFolder.getChildren().length > 0 && tabFolder.getSelection() == null) {
        	tabFolder.setSelection(0);
        }
        tabFolder.layout();

    }


    /**
     * Returns a list of all available tabs for a given node. These tabs can be
     * standard or plugin tabs.
     *
     * @param node for which to find tabs.
     * @return List of tabs
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    private static List createTabs(INode node) {

//        if (CONSTANT_LOGGER.isDebugEnabled()) {
//            CONSTANT_LOGGER.debug("Creating tabs for: " + node.getUniqueIdentifier());
//        }

        ArrayList tabList = new ArrayList();

        // create connection info tab if needed
        if (node instanceof DatabaseNode) {

            IDetailTab dbTab = new ConnectionInfoTab();
            dbTab.setNode(node);
            tabList.add(dbTab);

        }

        // create our basic table tabs
        if (node instanceof TableNode) {

            IDetailTab tab1 = new ColumnInfoTab();
            //IDetailTab tab2 = new TableInfoTab();
            IDetailTab tab3 = new PreviewTab();
            IDetailTab tab4 = new RowCountTab();
            IDetailTab tab5 = new PrimaryKeysTab();
            IDetailTab tab6 = new ExportedKeysTab();
            IDetailTab tab7 = new ImportedKeysTab();
            IDetailTab tab8 = new IndexesTab();
            //IDetailTab tab9 = new PriviligesTab();
            //IDetailTab tab10 = new ColumnPriviligesTab();
            //IDetailTab tab11 = new RowIdsTab();
            //IDetailTab tab12 = new VersionsTab();

            tab1.setNode(node);
            //tab2.setNode(node);
            tab3.setNode(node);
            tab4.setNode(node);
            tab5.setNode(node);
            tab6.setNode(node);
            tab7.setNode(node);
            tab8.setNode(node);
            //tab9.setNode(node);
            //tab10.setNode(node);
            //tab11.setNode(node);
            //tab12.setNode(node);

            tabList.add(tab1);
            //tabList.add(tab2);
            tabList.add(tab3);
            tabList.add(tab4);
            tabList.add(tab5);
            tabList.add(tab6);
            tabList.add(tab7);
            tabList.add(tab8);
            //tabList.add(tab9);
            //tabList.add(tab10);
            //tabList.add(tab11);
            //tabList.add(tab12);

        }

        // create extension point tabs
//        String databaseProductName = node.getSession().getRoot().getDatabaseProductName().toLowerCase().trim();
//        String nodeType = node.getType().toLowerCase().trim();
//
//        IExtensionRegistry registry = Platform.getExtensionRegistry();
//        IExtensionPoint point = registry.getExtensionPoint(SqlBuilderPlugin.PLUGIN_ID, "nodeDetailTab");
//        IExtension[] extensions = point.getExtensions();
//
//        for (int i = 0; i < extensions.length; i++) {
//
//            IExtension e = extensions[i];
//
//            IConfigurationElement[] ces = e.getConfigurationElements();
//
//            for (int j = 0; j < ces.length; j++) {
//                try {
//
//                    boolean isValidProduct = false;
//                    boolean isValidNodeType = false;
//
//                    String[] validProducts = ces[j].getAttribute("database-product-name").split(",");
//                    String[] validNodeTypes = ces[j].getAttribute("node-type").split(",");
//
//                    // check if tab is valid for current database product
//                    for (int k = 0; k < validProducts.length; k++) {
//
//                        String product = validProducts[k].toLowerCase().trim();
//
//                        if (product.length() == 0) {
//                            continue;
//                        }
//
//                        if (product.equals("*")) {
//                            isValidProduct = true;
//                            break;
//                        }
//
//                        String regex = TextUtil.replaceChar(product, '*', ".*");
//                        if (databaseProductName.matches(regex)) {
//                            isValidProduct = true;
//                            break;
//                        }
//
//                    }
//
//                    if (!isValidProduct) {
//                        continue;
//                    }
//
//                    // check if tab is valid for current node type
//                    for (int k = 0; k < validNodeTypes.length; k++) {
//
//                        String type = validNodeTypes[k].toLowerCase().trim();
//
//                        if (type.length() == 0) {
//                            continue;
//                        }
//
//                        if (type.equals("*")) {
//                            isValidNodeType = true;
//                            break;
//                        }
//
//                        String regex = TextUtil.replaceChar(type, '*', ".*");
//                        if (nodeType.matches(regex)) {
//                            isValidNodeType = true;
//                            break;
//                        }
//
//                    }
//
//                    if (!isValidNodeType) {
//                        continue;
//                    }
//
//                    // add tab to list
//                    IDetailTab tab = (IDetailTab) ces[j].createExecutableExtension("class");
//                    tab.setNode(node);
//
//                    tabList.add(tab);
//
//                } catch (Throwable ex) {
//                    SqlBuilderPlugin.log("Could not create menu action", ex);
//                }
//            }
//        }

        return tabList;
    }


    /**
     * This method returns the tabs for a given node from the cache. Tabs are
     * cached per sessionTreeNode. If the tabs don't exist in the cache, they
     * are created.
     *
     * @param node INode for which to retrieve tabs.
     * @return List of tabs.
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    private static List getTabs(INode node) {

//        if (CONSTANT_LOGGER.isDebugEnabled()) {
//            CONSTANT_LOGGER.debug("Loading tabs for: " + node.getUniqueIdentifier());
//        }

        HashMap tabCache = (HashMap) CONSTANT_SESSION_TAB_CACHE.get(node.getSession());

        if (tabCache == null) {
            // create cache
            tabCache = new HashMap();
            CONSTANT_SESSION_TAB_CACHE.put(node.getSession(), tabCache);
        }

        List tabs = (List) tabCache.get(node.getUniqueIdentifier());

        if (tabs == null) {
            // create tabs & store for later
            tabs = createTabs(node);
            tabCache.put(node.getUniqueIdentifier(), tabs);
        }

        // display parent details if we have nothing for this node..
        if ((tabs == null || tabs.size() == 0) && node.getParent() != null) {
            return getTabs(node.getParent());
        }

        return tabs;
    }


    /**
     * Store the name of the active tab, so that we can reselect it when a
     * different node is selected.
     *
     * @param name tab label
     */
    public static void setActiveTabName(String name) {

        pActiveTabName = name;
    }
}
