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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.dbstructure.nodes.DatabaseNode;
import org.talend.sqlbuilder.dbstructure.nodes.INode;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * DatabaseModel container for the database node and used to set as the input for the treeViewer in the database
 * structure outline.
 *
 * @modified Davy Vanherbergen
 */
public class DatabaseModel implements INode {

    private DatabaseNode pRoot;

    private SessionTreeNode pSessionNode;

    /*
     * Create new DatabaseModel for a database session
     *
     * @param sessionNode @param pm
     */
    public DatabaseModel(SessionTreeNode sessionNode) {

        pRoot = new DatabaseNode(Messages.getString("Database_1"), sessionNode); //$NON-NLS-1$

    }

    /**
     * @param composite Composite.
     */
    public void fillDetailComposite(Composite composite) {

    }

    /**
     * Returns an array of all root nodes..
     *
     * @see net.sourceforge.sqlexplorer.dbviewer.model.IDbModel#getChildren()
     * @return INode[]
     */
    public INode[] getChildNodes() {

        INode[] rootNodes = new INode[1];
        rootNodes[0] = pRoot;

        return rootNodes;
    }

    /**
     * @return Image.
     */
    public Image getExpandedImage() {

        return null;
    }

    /**
     * @return Image.
     */
    public Image getImage() {

        return null;
    }

    /**
     * @return LabelDecoration.
     */
    public String getLabelDecoration() {

        return null;
    }

    /**
     * @return LabelText.
     */
    public String getLabelText() {

        return null;
    }

    /**
     * @return Name.
     */
    public String getName() {

        return getQualifiedName();
    }

    /**
     * Always returns null, since this is the root...
     *
     * @see net.sourceforge.sqlexplorer.dbviewer.model.IDbModel#getParent()
     * @return Parent.
     */
    public INode getParent() {

        return null;
    }

    /**
     * @return QualifiedName
     */
    public String getQualifiedName() {

        return "databaseModel"; //$NON-NLS-1$
    }

    /**
     * @return DatabaseNode.
     */
    public DatabaseNode getRoot() {

        return (DatabaseNode) pRoot;
    }

    /**
     * @return SchemaOrCatalogName.
     */
    public String getSchemaOrCatalogName() {

        return null;
    }

    /**
     * @return SessionTreeNode for this node.
     */
    public final SessionTreeNode getSession() {

        if (pSessionNode == null) {
            pSessionNode = getRoot().getSession();
        }
        return pSessionNode;
    }

    /**
     * Returns "model" as the type for this node. This method is not used and only implemented for the interface.
     *
     * @see org.talend.sqlbuilder.dbstructure.nodes.INode#getType()
     * @return Type.
     */
    public String getType() {

        return "model"; //$NON-NLS-1$
    }

    /**
     * @return UniqueIdentifier.
     */
    public String getUniqueIdentifier() {

        return getQualifiedName();
    }

    /**
     * @return hasChildNodes.
     */
    public boolean hasChildNodes() {

        return false;
    }

    /**
     * @param parent parent
     * @param name name
     * @param sessionNode sessionTreeNode.
     */
    public void initialize(INode parent, String name, SessionTreeNode sessionNode) {
    }

    /**
     * @return isEndNode.
     */
    public boolean isEndNode() {

        return false;
    }

    /**
     * @return isExpanded.
     */
    public boolean isExpanded() {

        return false;
    }

    /**
     * refresh.
     */
    public void refresh() {
    }

    /**
     * @param expanded expanded..
     */
    public void setExpanded(boolean expanded) {
        return;
    }

    /**
     * @param columnIndex columnIndex.
     * @return label text.
     */
    public String getLabelAtColumn(int columnIndex) {
        return getLabelText();
    }

    /**
     * @param columnIndex Column Index.
     * @return Image.
     */
    public Image getImageAtColumn(int columnIndex) {
        return getImage();
    }

    /**
     * @return Background.
     */
    public Color getBackground() {
        return Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    }

    /**
     * @return Foreground.
     */
    public Color getForeground() {
        return Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    }
}
