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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.talend.designer.gefabstractmap.figures.cells.ITextCell;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;

/**
 * wchen class global comment. Detailled comment
 */
public abstract class TreeBranchContent extends Figure implements ITextCell {

    private int alpha = 255;

    protected Label nameFigure;

    public TreeBranchContent() {
        setDirectEditType(DirectEditType.NODE_NAME);
        setOpaque(true);
    }

    protected void createContent() {
        GridLayout manager = new GridLayout(4, false);
        manager.horizontalSpacing = 5;
        manager.verticalSpacing = 1;
        manager.marginHeight = -1;
        manager.marginWidth = 5;
        setLayoutManager(manager);
        nameFigure = new Label();
        nameFigure.setText(getNameValue());
        this.add(nameFigure);
    }

    @Override
    public void paint(Graphics graphics) {
        if (alpha != -1) {
            graphics.setAlpha(alpha);
        } else {
            graphics.setAlpha(255);
        }
        super.paint(graphics);
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    protected abstract String getNameValue();

    public abstract void updataNameFigure();

    private DirectEditType type;

    @Override
    public void setDirectEditType(DirectEditType type) {
        this.type = type;
    }

    @Override
    public DirectEditType getDirectEditType() {
        return this.type;
    }

}
