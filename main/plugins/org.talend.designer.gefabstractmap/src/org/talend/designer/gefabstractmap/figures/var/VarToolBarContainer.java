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
package org.talend.designer.gefabstractmap.figures.var;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.gefabstractmap.figures.manager.TableManager;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarButtonImageFigure;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarContainer;

/**
 * DOC wchen class global comment. Detailled comment
 */
public abstract class VarToolBarContainer extends ToolBarContainer {

    protected ToolBarButtonImageFigure add, remove, move_up, move_down;

    private static Label restoretooltip = new Label("Restore");

    public VarToolBarContainer(TableManager tableManager) {
        super(tableManager);
    }

    /**
     * DOC hywang Comment method "createToolbar".
     */
    protected void createToolbar() {
        super.createToolbar();
        getLayoutManager().setSpacing(0);
        add = new AddButton();
        setTooltips(add, "Add variable");
        remove = new RemoveButton();
        setTooltips(remove, "Remove selected variable(s)");
        remove.setEnabled(false);
        move_up = new MoveUpButton();
        setTooltips(move_up, "Move up selected variable(s)");
        move_up.setEnabled(false);
        move_down = new MoveDownButton();
        setTooltips(move_down, "Move down selected variable(s)");
        move_down.setEnabled(false);
        this.add(add);
        this.add(remove);
        this.add(move_up);
        this.add(move_down);
        this.add(min_size);
    }

    protected abstract void addVar();

    class AddButton extends ToolBarButtonImageFigure {

        public AddButton() {
            super(ImageProvider.getImage(EImage.ADD_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            addVar();
        }
    }

    protected abstract void removeVar();

    class RemoveButton extends ToolBarButtonImageFigure {

        public RemoveButton() {
            super(ImageProvider.getImage(EImage.MINUS_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            removeVar();
        }
    }

    protected abstract void moveUp();

    class MoveUpButton extends ToolBarButtonImageFigure {

        public MoveUpButton() {
            super(ImageProvider.getImage(EImage.UP_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            moveUp();

        }
    }

    protected abstract void moveDown();

    class MoveDownButton extends ToolBarButtonImageFigure {

        public MoveDownButton() {
            super(ImageProvider.getImage(EImage.DOWN_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            moveDown();

        }
    }

    public void setRemoveEnable(boolean value) {
        this.remove.setEnabled(value);
    }

    public void setMoveUpEnable(boolean value) {
        this.move_up.setEnabled(value);
    }

    public void setMoveDownEnable(boolean value) {
        this.move_down.setEnabled(value);
    }

}
