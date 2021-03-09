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

import org.eclipse.swt.graphics.Image;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: ObjectNode.java,v 1.3 2006/11/01 05:40:59 peiqin.hou Exp $
 *
 */
public class ObjectNode extends AbstractNode {

    private String ptype;

    /**
     * Hidden default constructor.
     */
    private ObjectNode() {

    }

    public ObjectNode(String name, String type, INode parent, Image image) {
        ptype = type;
        pname = name;
        psessionNode = parent.getSession();
        pparent = parent;
        pimage = image;
    }

    /**
     * This node cannot have childnodes.
     *
     * @return isEndNode.
     */
    public boolean isEndNode() {
        return true;
    }

    /**
     * This node cannot have childnodes.
     */
    public void loadChildren() {
        return;
    }

    /**
     * @return Type.
     */
    public String getType() {
        return ptype;
    }

    /**
     * @return QualifiedName.
     */
    public String getQualifiedName() {
        return "\"" + getSchemaOrCatalogName() + "\".\"" + getName() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
