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
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.talend.designer.gefabstractmap.figures.layout.RowLayout;
import org.talend.designer.gefabstractmap.figures.manager.EntitySelectManager;
import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;

/**
 * DOC talend class global comment. Detailled comment
 */
public abstract class TableEntityElement extends Figure {

    private final EntitySelectManager selectManager = EntitySelectManager.getManager();

    protected TableEntityManager entityManager;

    public TableEntityElement(TableEntityManager entityManager) {
        this.entityManager = entityManager;
        setLayoutManager(new RowLayout());
        this.addMouseListener(new MouseListener.Stub() {

            @Override
            public void mousePressed(MouseEvent me) {
                selectManager.setSelection(TableEntityElement.this);
            }
        });

        createItems(this);
    }

    protected abstract void createItems(TableEntityElement entity);

    public TableEntityFigure getTableEntityFigure() {
        return (TableEntityFigure) getParent();
    }

    public RowLayout getRowLayout() {
        return (RowLayout) getLayoutManager();
    }

}
