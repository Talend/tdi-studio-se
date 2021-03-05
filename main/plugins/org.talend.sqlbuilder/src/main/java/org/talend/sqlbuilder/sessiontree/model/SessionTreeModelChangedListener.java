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
package org.talend.sqlbuilder.sessiontree.model;

/**
 * DOC qianbing class global comment. Detailled comment <br/>
 *
 * $Id: SessionTreeModelChangedListener.java,v 1.3 2006/11/01 06:45:49 peiqin.hou Exp $
 *
 */
public abstract class SessionTreeModelChangedListener {

    /**
     * @param newNode SessionTreeNode.
     */
    abstract void modelChanged(SessionTreeNode newNode);
}
