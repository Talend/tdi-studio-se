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
package org.talend.sqlbuilder.sessiontree.model.utility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbstructure.nodes.CatalogNode;
import org.talend.sqlbuilder.dbstructure.nodes.DatabaseNode;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.dbstructure.nodes.SchemaNode;
import org.talend.sqlbuilder.dbstructure.nodes.TableNode;
import org.talend.sqlbuilder.sqleditor.SQLCodeScanner;

/**
 * Dictionary.
 * <br/>
 *
 * $Id: Dictionary.java,v 1.5 2006/11/01 07:49:10 peiqin.hou Exp $
 *
 */
public class Dictionary {

    // TODO check if we need to add more types or remove restriction completely?
    private static final String[] SUPPORTED_CONTENT_ASSIST_TYPES = new String[] {"TABLE_FOLDER", "TABLE_TYPE", "VIEW_FOLDER", "VIEW_TYPE"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    public Dictionary() {

    }

    private static TernarySearchTree keywordsTree = new TernarySearchTree();
    static {
        String[] str = SQLCodeScanner.getFgKeywords();
        for (int i = 0; i < str.length; i++) {
            keywordsTree.put(str[i], str[i]);
        }

    }

    private TernarySearchTree tree = new TernarySearchTree();

    private TernarySearchTree catalogSchemaTree = new TernarySearchTree();

    private TernarySearchTree externalObjectTree = new TernarySearchTree();

    private HashMap realTables = new HashMap();

    private HashMap realCatalogSchemas = new HashMap();

    private HashMap realExternalObjects = new HashMap();

    private HashMap colMap = new HashMap();

    private static final int ROOT_WORK_UNIT = 1000;

    /**
     * @param key key
     * @param value value
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public void putTableName(String key, Object value) {

        tree.put(key.toLowerCase(), value);
        realTables.put(key.toLowerCase(), key);
    }

    /**
     * @param key key
     * @param value value.
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public void putCatalogSchemaName(String key, Object value) {

        catalogSchemaTree.put(key.toLowerCase(), value);
        realCatalogSchemas.put(key.toLowerCase(), key);
    }

    /**
     * @param key key
     * @param value value.
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public void putExternalObjectName(String key, Object value) {

        externalObjectTree.put(key.toLowerCase(), value);
        realExternalObjects.put(key.toLowerCase(), key);
    }

    /**
     * @param key key.
     * @return object.
     */
    public Object getByTableName(String key) {

        return tree.get(key);
    }

    /**
     * @param key key.
     * @return object.
     */
    public Object getByCatalogSchemaName(String key) {

        return catalogSchemaTree.get(key);
    }

    /**
     * @param key key.
     * @return object.
     */
    public Object getByExternalObjectName(String key) {

        return catalogSchemaTree.get(key);
    }

    /**
     * @param key key.
     * @param value value.
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public void putColumnsByTableName(String key, Object value) {
        colMap.put(key.toLowerCase(), value);
    }

    /**
     * @param key key.
     * @return object.
     */
    public Object getColumnListByTableName(String key) {

        return colMap.get(key);
    }

    /**
     * @return Iterator.
     */
    public Iterator getTableNames() {

        return realTables.keySet().iterator();
    }

    /**
     * @return Iterator.
     */
    public Iterator getCatalogSchemaNames() {

        return realCatalogSchemas.keySet().iterator();
    }

    /**
     * @return Iterator.
     */
    public Iterator getExternalObjectNames() {

        return realExternalObjects.keySet().iterator();
    }

    /**
     * @param tableName tableName
     * @return ArrayList
     */
    public ArrayList getTableObjectList(String tableName) {

        return (ArrayList) tree.get(tableName.toLowerCase());
    }

    /**
     * @param prefix prefix
     * @return String[]
     */
    public String[] matchTablePrefix(String prefix) {

        prefix = prefix.toLowerCase();
        DoublyLinkedList linkedList = tree.matchPrefix(prefix);
        int size = linkedList.size();
        DoublyLinkedList.DLLIterator iterator = linkedList.iterator();
        String[] result = new String[size];
        int k = 0;
        while (iterator.hasNext()) {
            result[k++] = (String) realTables.get(iterator.next());
        }
        return result;
    }

    /**
     * @param prefix prefix
     * @return String[]
     */
    public String[] matchCatalogSchemaPrefix(String prefix) {

        prefix = prefix.toLowerCase();
        DoublyLinkedList linkedList = catalogSchemaTree.matchPrefix(prefix);
        int size = linkedList.size();
        DoublyLinkedList.DLLIterator iterator = linkedList.iterator();
        String[] result = new String[size];
        int k = 0;
        while (iterator.hasNext()) {
            result[k++] = (String) realCatalogSchemas.get(iterator.next());
        }
        return result;
    }

    /**
     * @param prefix prefix
     * @return String[]
     */
    public String[] matchExternalObjectPrefix(String prefix) {

        prefix = prefix.toLowerCase();
        DoublyLinkedList linkedList = externalObjectTree.matchPrefix(prefix);
        int size = linkedList.size();
        DoublyLinkedList.DLLIterator iterator = linkedList.iterator();
        String[] result = new String[size];
        int k = 0;
        while (iterator.hasNext()) {
            result[k++] = (String) realExternalObjects.get(iterator.next());
        }
        return result;
    }

    /**
     * @param prefix prefix
     * @return String[]
     */
    public static String[] matchKeywordsPrefix(String prefix) {

        prefix = prefix.toLowerCase();
        DoublyLinkedList linkedList = keywordsTree.matchPrefix(prefix);
        int size = linkedList.size();
        DoublyLinkedList.DLLIterator iterator = linkedList.iterator();
        String[] result = new String[size];
        int k = 0;
        while (iterator.hasNext()) {
            result[k++] = (String) iterator.next();
        }
        return result;
    }


    /**
     * Loads the persisted dictionary from a previous session.
     *
     * @param dbNode DatabaseNode for which to load the dictionary
     * @param monitor IProgressMonitor
     * @return true if dictionary was found and loaded.
     * @exception InterruptedException InterruptedException
     */
    public boolean restore(DatabaseNode dbNode, IProgressMonitor monitor) throws InterruptedException {
        return false;
    }


    /**
     * Persists this dictionary so that it can be reused in next sessions
     * without having to be reloaded from database.
     */
    public void store() {
    }


    /**
     * Perform full load of dictionary for dbNode.
     *
     * @param dbNode DatabaseNode of which to load dictionary information
     * @param monitor ProgressMonitor displayed whilst loading
     * @throws InterruptedException If user cancelled loading
     */
    public void load(DatabaseNode dbNode, IProgressMonitor monitor) throws InterruptedException {

        try {

            // check for cancellation by user
            if (monitor.isCanceled()) {
                throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled")); //$NON-NLS-1$
            }

            INode[] children = dbNode.getChildNodes();

            if (children == null) {
                return;
            }

            // start task with a 1000 work units for every root node
            monitor.beginTask(dbNode.getSession().toString(), children.length * ROOT_WORK_UNIT);

            for (int i = 0; i < children.length; i++) {

                // check for cancellation by user
                if (monitor.isCanceled()) {
                    throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled")); //$NON-NLS-1$
                }

                INode node = (INode) children[i];

                if (node instanceof SchemaNode || node instanceof CatalogNode) {
                    loadSchemaCatalog(node, monitor);
                }

            }

            // store dictionary immediately so that
            // we can resuse it if a second session is opened
            store();

        } finally {
            monitor.done();
        }

    }


    /**
     * Load dictionary data for catalog.
     *
     * @param iNode catalognode to load
     * @param monitor ProgressMonitor displayed whilst loading
     * @throws InterruptedException If user cancelled loading
     */
    @SuppressWarnings("unchecked") //$NON-NLS-1$
    private void loadSchemaCatalog(INode iNode, IProgressMonitor monitor) throws InterruptedException {

//        if (logger.isDebugEnabled()) {
//            logger.debug("Loading dictionary: " + iNode.getName());
//        }

        // check for cancellation by user
        if (monitor.isCanceled()) {
            throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled")); //$NON-NLS-1$
        }

        putCatalogSchemaName(iNode.toString(), iNode);
        monitor.subTask(iNode.getName());

        INode[] children = iNode.getChildNodes();

        if (children != null) {

            // check for cancellation by user
            if (monitor.isCanceled()) {
                throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled")); //$NON-NLS-1$
            }

            // divide work equally between type nodes
            int typeNodeWorkUnit = ROOT_WORK_UNIT / SUPPORTED_CONTENT_ASSIST_TYPES.length;
            int typeNodeWorkCompleted = 0;

            for (int i = 0; i < children.length; i++) {

                INode typeNode = children[i];

//                if (logger.isDebugEnabled()) {
//                    logger.debug("Loading dictionary: " + typeNode.getName());
//                }

                // only load a few types like tables and view nodes into the
                // dictionary
                boolean isIncludedInContentAssist = false;
                for (int j = 0; j < SUPPORTED_CONTENT_ASSIST_TYPES.length; j++) {
                    if (typeNode.getType().equalsIgnoreCase(SUPPORTED_CONTENT_ASSIST_TYPES[j])) {
                        isIncludedInContentAssist = true;
                    }
                }
                if (!isIncludedInContentAssist) {
                    continue;
                }

                monitor.subTask(typeNode.getName());

                // check for cancellation by user
                if (monitor.isCanceled()) {
                    throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled")); //$NON-NLS-1$
                }

                INode[] tableNodes = typeNode.getChildNodes();
                if (tableNodes != null) {

                    // check for cancellation by user
                    if (monitor.isCanceled()) {
                        throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled")); //$NON-NLS-1$
                    }

                    int tableNodeWorkUnit = typeNodeWorkUnit / tableNodes.length;

                    for (int j = 0; j < tableNodes.length; j++) {

                        INode tableNode = tableNodes[j];

//                        if (logger.isDebugEnabled()) {
//                            logger.debug("Loading dictionary: " + tableNode.getName());
//                        }

                        if (monitor != null) {

                            monitor.worked(tableNodeWorkUnit);
                            typeNodeWorkCompleted = typeNodeWorkCompleted + tableNodeWorkUnit;

//                            if (logger.isDebugEnabled()) {
//                                logger.debug("worked table: " + tableNodeWorkUnit + ", total type work: "
//                                        + typeNodeWorkCompleted);
//                            }

                            monitor.subTask(tableNode.getQualifiedName());

                            // check for cancellation by user
                            if (monitor.isCanceled()) {
                                throw new InterruptedException(Messages.getString("Progress.Dictionary.Cancelled")); //$NON-NLS-1$
                            }
                        }

                        // add table name
                        ArrayList tableDetails = (ArrayList) getByTableName(tableNode.getName());
                        if (tableDetails == null) {
                            tableDetails = new ArrayList();
                            putTableName(tableNode.getName(), tableDetails);
                        }
                        tableDetails.add(tableNode);

                        // add column names
                        if (tableNode instanceof TableNode) {

                            TreeSet columnNames = new TreeSet();
                            List columns = ((TableNode) tableNode).getColumnNames();
                            if (columns != null) {

                                Iterator it = columns.iterator();
                                while (it.hasNext()) {
                                    columnNames.add(it.next());
                                }
                            }
                            putColumnsByTableName(tableNode.getName(), columnNames);
                        }

                    }
                }

                if (typeNodeWorkCompleted < typeNodeWorkUnit) {
                    // consume remainder of work for this type node

//                    if (logger.isDebugEnabled()) {
//                        logger.debug("consuming remainder: " + (typeNodeWorkUnit - typeNodeWorkCompleted));
//                    }

                    monitor.worked(typeNodeWorkUnit - typeNodeWorkCompleted);
                }
                typeNodeWorkCompleted = 0;

            }

        }

    }

}
