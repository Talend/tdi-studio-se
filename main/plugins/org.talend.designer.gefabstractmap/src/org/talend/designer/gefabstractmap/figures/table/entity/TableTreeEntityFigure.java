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

import java.util.List;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.commons.ui.swt.geftree.TreeAnimation;
import org.talend.commons.ui.swt.geftree.layout.TreeAnimatingLayer;
import org.talend.designer.gefabstractmap.figures.manager.EntitySelectManager;
import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;

/**
 * wchen talend class global comment. Detailled comment
 */
public abstract class TableTreeEntityFigure extends TableEntityFigure {

    private EntitySelectManager selectionManager = EntitySelectManager.getManager();

    private TreeAnimatingLayer contentPane;

    private boolean expanded = true;

    private boolean animate = true;

    private TreeBranch treeBranch;

    public TableTreeEntityFigure(TableEntityManager entityManager) {
        this(entityManager, false);
    }

    public TableTreeEntityFigure(TableEntityManager entityManager, boolean isRoot) {
        super(entityManager, isRoot);
    }

    @Override
    protected TableEntityElement createElement() {
        return new TableEntityElement(getEntityManager()) {

            @Override
            protected void createItems(TableEntityElement entityElement) {
                createEntityItems(entityElement);
                for (Object child : entityElement.getChildren()) {
                    if (child instanceof TreeBranch) {
                        treeBranch = (TreeBranch) child;
                    }
                }
            };

        };

    }

    @Override
    public TreeAnimatingLayer getContents() {
        if (this.contentPane == null) {
            contentPane = new TreeAnimatingLayer() {

                public void setBounds(Rectangle rect) {

                    int x = bounds.x, y = bounds.y;

                    boolean resize = (rect.width != bounds.width) || (rect.height != bounds.height), translate = (rect.x != x)
                            || (rect.y != y);

                    if (isVisible() && (resize || translate))
                        erase();
                    if (translate) {
                        int dx = rect.x - x;
                        int dy = rect.y - y;
                        primTranslate(dx, dy);
                    }
                    bounds.width = rect.width;
                    bounds.height = rect.height;
                    if (resize)
                        invalidate();
                    if (resize || translate) {
                        fireFigureMoved();
                        fireCoordinateSystemChanged();
                        repaint();
                    }
                }
            };
            ToolbarLayout layout = new ToolbarLayout() {

                @Override
                public void layout(IFigure parent) {
                    TreeAnimation.recordInitialState(parent);
                    if (TreeAnimation.playbackState(parent)) {
                        return;
                    }

                    super.layout(parent);
                }

                @Override
                protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
                    return super.calculatePreferredSize(container, wHint, hHint);
                }
            };
            layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
            contentPane.setLayoutManager(layout);
            this.add(contentPane);

            // ///////test
            // contentPane.setOpaque(true);
            // contentPane.setBackgroundColor(ColorConstants.yellow);

        }
        return contentPane;
    }

    @Override
    public TableTreeEntityFigure getRoot() {
        return isRoot() ? this : ((TableTreeEntityFigure) getParent().getParent()).getRoot();
    }

    @Override
    public TableTreeEntityFigure getParentEntity() {
        if (getParent() != null && getParent().getParent() instanceof TableTreeEntityFigure) {
            return (TableTreeEntityFigure) getParent().getParent();
        }
        return null;
    }

    public void expand() {
        if (expanded) {
            return;
        }
        setExpanded(true);
        animationReset(getElementBounds());

        TreeAnimation.mark(getElement());
        TreeAnimation.captureLayout(getRoot());

        while (TreeAnimation.step()) {
            getUpdateManager().performUpdate();
        }
        TreeAnimation.end();
    }

    public void collapse() {
        if (!expanded) {
            return;
        }

        IFigure root = this;
        Viewport port = null;
        Point viewportStart = null;
        while (root.getParent() != null) {
            if (root instanceof Viewport) {
                port = ((Viewport) root);
            }
            root = root.getParent();
            if (port != null) {
                break;
            }
        }
        viewportStart = port.getViewLocation();
        Point elementStart = getElement().getBounds().getLocation();
        setExpanded(false);
        root.validate();

        setExpanded(true);
        animationReset(getElementBounds());
        TreeAnimation.mark(getElement());
        TreeAnimation.captureLayout(getRoot());
        TreeAnimation.swap();
        TreeAnimation.trackLocation = elementStart;

        // root.validate();
        port.setViewLocation(viewportStart);
        while (TreeAnimation.step()) {
            getUpdateManager().performUpdate();
        }
        TreeAnimation.end();
        setExpanded(false);
    }

    /**
     * recursively set all nodes and sub-treebranch nodes to the same location. This gives the appearance of all nodes
     * coming from the same place.
     *
     * @param bounds where to set
     */
    public void animationReset(Rectangle bounds) {
        List subtrees = getContents().getChildren();
        getContents().setBounds(bounds);

        // Make the center of this node match the center of the given bounds
        Rectangle r = element.getBounds();
        int dx = bounds.x + bounds.width / 2 - r.x - r.width / 2;
        int dy = bounds.y + bounds.height / 2 - r.y - r.height / 2;
        getElement().translate(dx, dy);
        revalidate(); // Otherwise, this branch will not layout

        // Pass the location to all children
        for (int i = 0; i < subtrees.size(); i++) {
            TableTreeEntityFigure subtree = (TableTreeEntityFigure) subtrees.get(i);
            subtree.setBounds(bounds);
            subtree.animationReset(bounds);
        }
    }

    public boolean isAnimate() {
        return this.animate;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public void setExpanded(boolean b) {
        if (expanded == b) {
            return;
        }
        expanded = b;
        getContents().setVisible(b);
        revalidate();
        getTreeBranch().updateExpandCollapseState(b);
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    public Rectangle getElementBounds() {
        return getElement().getBounds();
    }

    public TreeBranch getTreeBranch() {
        return this.treeBranch;
    }

    public boolean isEnableExpand() {
        return getTreeBranch().isEnableExpand();
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);

        // paint do lines of tree
        if (this.isExpanded() && getTreeBranch() != null) {
            graphics.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_TREE_LINES));
            getTreeBranch().getBranchLayout().paintLines(graphics);
            // getBranchLayout().paintLines(graphics);
        }

    }

    public void doExpandCollapse() {

        if (!isEnableExpand()) {
            return;
        }
        if (selectionManager.getSelection() == null) {
            return;
        }
        TableEntityElement selectedRow = selectionManager.getSelection();

        TableTreeEntityFigure parent = (TableTreeEntityFigure) selectedRow.getParent();

        if (parent.getContents().getChildren().isEmpty()) {
            return;
        }
        if (isAnimate()) {
            if (parent.isExpanded()) {
                parent.collapse();
            } else {
                parent.expand();
            }
        } else {
            parent.setExpanded(!parent.isExpanded());
        }

    }

    public void doExpandCollapse(TableTreeEntityFigure branch) {
        if (!isEnableExpand()) {
            return;
        }
        if (branch == null) {
            return;
        }
        if (branch.getContents().getChildren().isEmpty()) {
            return;
        }
        if (isAnimate()) {
            if (branch.isExpanded()) {
                branch.collapse();
            } else {
                branch.expand();
            }
        } else {
            branch.setExpanded(!branch.isExpanded());
        }

    }

    private void paintChildrenLines(TableTreeEntityFigure entity, Graphics graphics) {
        if (entity.isExpanded()) {
            // graphics.setLineStyle(SWT.LINE_DOT);
            final List children = entity.getContents().getChildren();
            for (Object child : children) {
                if (child instanceof TableTreeEntityFigure) {
                    paintLines((TableTreeEntityFigure) child, graphics);
                }
            }
        }
    }

    @Override
    protected void paintLines(TableEntityFigure entity, Graphics graphics) {
        // TODO Auto-generated method stub
        super.paintLines(entity, graphics);
        paintChildrenLines((TableTreeEntityFigure) entity, graphics);
    }

}
