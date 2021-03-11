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
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.talend.designer.gefabstractmap.figures.layout.TreeBranchLayout;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TreeBranch extends Figure {

    private int gaps = 10;

    private ExpandCollapseFigure expandCollapseFigure;

    // ///test
    private boolean enableExpand = true;

    private IFigure title;

    public TreeBranch(IFigure title, boolean enableExpand) {
        setBorder(new MarginBorder(1, 0, 1, 0));
        this.title = title;
        this.enableExpand = enableExpand;
        setLayoutManager(new TreeBranchLayout(this));
        createExpandCollapse();
        add(title);
        updateExpandCollapseState(true);
    }

    public TreeBranch(IFigure title) {
        this(title, true);
    }

    public IFigure getTitle() {
        return this.title;
    }

    private void createExpandCollapse() {
        if (isEnableExpand()) {
            this.expandCollapseFigure = new ExpandCollapseFigure(this);
            add(this.expandCollapseFigure);
        }
    }

    public ExpandCollapseFigure getExpandCollapseFigure() {
        return this.expandCollapseFigure;
    }

    protected void updateExpandCollapseState(boolean expand) {
        if (isEnableExpand()) {
            if (expand) {
                this.expandCollapseFigure.setImage(ImageProviderMapper.getImage(ImageInfo.TREE_EXPAND));
            } else {
                this.expandCollapseFigure.setImage(ImageProviderMapper.getImage(ImageInfo.TREE_COLLAPSE));
            }
        }
    }

    public boolean isEnableExpand() {
        return enableExpand;
    }

    public void setEnableExpand(boolean enableExpand) {
        this.enableExpand = enableExpand;
    }

    public TableTreeEntityFigure getEntityFigure() {
        if (getParent() != null && getParent().getParent() instanceof TableTreeEntityFigure) {
            return (TableTreeEntityFigure) getParent().getParent();
        }
        return null;
    }

    public int getGaps() {
        return gaps;
    }

    public void setGaps(int gaps) {
        this.gaps = gaps;
    }

    public int getDepth() {
        return getBranchLayout().getDepth();
    }

    public TreeBranchLayout getBranchLayout() {
        return (TreeBranchLayout) getLayoutManager();
    }

    @Override
    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
    }

}
