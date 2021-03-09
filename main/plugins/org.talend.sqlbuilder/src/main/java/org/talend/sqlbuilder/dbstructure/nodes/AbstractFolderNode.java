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

import net.sourceforge.sqlexplorer.SQLAlias;

import org.eclipse.swt.graphics.Image;
import org.talend.core.sqlbuilder.util.TextUtil;
import org.talend.sqlbuilder.util.ImageUtil;

/**
 * DOC dev class global comment. Detailled comment <br/>
 *
 * $Id: AbstractFolderNode.java,v 1.3 2006/11/01 05:40:59 peiqin.hou Exp $
 *
 */
public abstract class AbstractFolderNode extends AbstractNode {

    private String[] pfilterExpressions;

    public AbstractFolderNode() {

        pimageKey = "Images.closedFolder"; //$NON-NLS-1$
        pexpandedImageKey = "Images.OpenFolder"; //$NON-NLS-1$
    }

    /**
     * Override this method to change the image that is displayed for this node in the database structure outline.
     *
     * @return Image
     */
    public Image getImage() {

        if (pimageKey == null) {
            return pimage;
        }
        return ImageUtil.getImage(pimageKey);
    }

    /**
     * @return Name.
     */
    public abstract String getName();

    /**
     * @return UniqueIdentifier
     */
    public final String getUniqueIdentifier() {

        return getParent().getName() + '.' + getType();
    }

    /**
     * Checks if a node name should be filtered.
     *
     * @param name to check for filtering
     * @return true if the name should be filtered
     */
    protected boolean isExcludedByFilter(String name) {

        if (pfilterExpressions == null) {
            String filter = ((SQLAlias) getSession().getAlias()).getNameFilterExpression();
            if (filter != null) {
                pfilterExpressions = filter.split(","); //$NON-NLS-1$
            }
        }
        if (pfilterExpressions == null || pfilterExpressions.length == 0) {
            // no active filter
            return false;
        }

        for (int i = 0; i < pfilterExpressions.length; i++) {

            String regex = pfilterExpressions[i].trim();
            regex = TextUtil.replaceChar(regex, '?', "."); //$NON-NLS-1$
            regex = TextUtil.replaceChar(regex, '*', ".*"); //$NON-NLS-1$

            if (regex.length() != 0 && name.matches(regex)) {
                // we have a match, exclude node..
                return true;
            }
        }

        // no match found
        return false;

    }

    public abstract void loadChildren();
}
