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
package org.talend.designer.gefabstractmap.figures.table.entity;

import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.swt.graphics.Image;

/**
 * wchen class global comment. Detailled comment
 */
public class ExpandCollapseFigure extends ImageFigure {

    private TreeBranch parent;

    public ExpandCollapseFigure(TreeBranch parent) {
        this(parent, null);
    }

    public ExpandCollapseFigure(TreeBranch parent, Image image) {
        this(parent, image, PositionConstants.CENTER);
    }

    public ExpandCollapseFigure(TreeBranch parent, Image image, int alignment) {
        super(image, alignment);
        this.parent = parent;
    }

    public TreeBranch getParent() {
        return this.parent;
    }

}
