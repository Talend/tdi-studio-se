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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.commons.ui.swt.geftree.layout.TreeAnimatingLayer;
import org.talend.designer.gefabstractmap.figures.layout.TableEntityLayout;
import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;
import org.talend.designer.gefabstractmap.figures.table.AbstractTable;

/**
 * created by Administrator on 2013-1-16 Detailled comment
 *
 */
public abstract class TableEntityFigure extends Figure {

    protected TableEntityElement element;

    private TableEntityManager entityManager;

    private boolean isRoot;

    public TableEntityFigure(TableEntityManager entityManager) {
        this(entityManager, true);
    }

    public TableEntityFigure(TableEntityManager entityManager, boolean isRoot) {
        setLayoutManager(new TableEntityLayout(this));
        this.entityManager = entityManager;
        this.isRoot = isRoot;
        this.element = createElement();
        this.add(element);
    }

    public boolean isRoot() {
        return isRoot;
    }

    protected TableEntityManager getEntityManager() {
        return this.entityManager;
    }

    protected TableEntityElement createElement() {
        return new TableEntityElement(entityManager) {

            @Override
            protected void createItems(TableEntityElement entityElement) {
                createEntityItems(entityElement);
            };

        };

    }

    protected abstract void createEntityItems(TableEntityElement entityElement);

    public TreeAnimatingLayer getContents() {
        return null;
    }

    public TableEntityFigure getRoot() {
        return this;
    }

    public AbstractTable getTable() {
        return (AbstractTable) getRoot().getParent().getParent();
    }

    public TableEntityFigure getParentEntity() {
        return null;
    }

    public Rectangle getElementBounds() {
        return getElement().getBounds();
    }

    public TableEntityElement getElement() {
        return this.element;
    }

    @Override
    protected void paintFigure(Graphics graphics) {
        super.paintFigure(graphics);
        // paint rows and columns in root figure
        if (isRoot()) {
            graphics.setForegroundColor(ColorConstants.menuBackground);
            paintLines(this, graphics);
        }

    }

    protected void paintLines(TableEntityFigure entity, Graphics graphics) {
        graphics.drawLine(entity.getElementBounds().x, entity.getElementBounds().getBottom().y - 1, entity.getElementBounds()
                .getRight().x, entity.getElementBounds().getBottom().y - 1);

        List children = entity.getElement().getChildren();
        for (int i = 0; i < children.size() - 1; i++) {
            Figure child = (Figure) children.get(i);
            Rectangle childBounds = child.getBounds();
            graphics.drawLine(childBounds.getTopRight(), childBounds.getBottomRight());
        }
        // final Rectangle expBounds = getElementBounds();
        // graphics.drawLine(expBounds.x + expBounds.width, expBounds.y, expBounds.x + expBounds.width, getBounds().y
        // + getBounds().height);

        // paintChildrenLines(this, graphics);

    }

}
