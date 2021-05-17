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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.talend.sqlbuilder.sessiontree.model.SessionTreeNode;

/**
 * All nodes displayed in the database structure outline should implement this interface.
 *
 * @author Davy Vanherbergen
 */
public interface INode {

    /**
     * @param composite Composite.
     */
    public void fillDetailComposite(Composite composite);

    /**
     * @return All child nodes of this node.
     */
    public INode[] getChildNodes();

    /**
     * The returned image is displayed in the database structure outline for this node when the node is expanded.
     *
     * @return Image to be used for this node.
     */
    public Image getExpandedImage();

    /**
     * The returned image is displayed in the database structure outline for this node.
     *
     * @return Image to be used for this node.
     */
    public Image getImage();

    /**
     * @return text to append after node label.
     */

    public String getLabelDecoration();

    /**
     * @return Text that is displayed for this node in the treeviewer.
     */
    public String getLabelText();

    /**
     * Get the clumn label text.
     *
     * method description.
     *
     * @param i -- the column number.
     * @return -- the label text.
     * @exception
     */
    public String getLabelAtColumn(int i);

    /**
     * @return Simple name for this node.
     */
    public String getName();

    /**
     * @return Parent node of this node.
     */
    public INode getParent();

    /**
     * @return Qualified name for this node.
     */
    public String getQualifiedName();

    /**
     * @return SchemaOrCatalogName.
     */
    public String getSchemaOrCatalogName();

    /**
     * @return SessionTreeNode for this node.
     */
    public SessionTreeNode getSession();

    /**
     * @return type of this node, e.g. Database, schema, catalog, table, view, ...
     */
    public String getType();

    /**
     * @return Qualified path for this node.
     */
    public String getUniqueIdentifier();

    /**
     * @return true if the node has children.
     */
    public boolean hasChildNodes();

    /**
     * Initialize this node.
     *
     * @param parent the parent INode of this node.
     * @param name the name of this node.
     * @param sessionNode the session this node belongs too.
     */
    public void initialize(INode parent, String name, SessionTreeNode sessionNode);

    /**
     * @return true if this node cannot have children..
     */
    public boolean isEndNode();

    /**
     * @return true if node is expanded.
     */
    public boolean isExpanded();

    /**
     * Refresh. This will clear the nodes' children and reload them.
     */
    public void refresh();

    /**
     * Set expanded state of element.
     *
     * @param expanded expanded.
     */
    public void setExpanded(boolean expanded);

    /**
     * @param columnIndex Column Index.
     * @return Column Image.
     */
    public Image getImageAtColumn(int columnIndex);

    /**
     * @return Foreground color.
     */
    public Color getForeground();

    /**
     * @return Background color.
     */
    public Color getBackground();

}
