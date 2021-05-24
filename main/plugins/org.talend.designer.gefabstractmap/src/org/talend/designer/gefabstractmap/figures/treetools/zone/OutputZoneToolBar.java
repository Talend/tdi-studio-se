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
package org.talend.designer.gefabstractmap.figures.treetools.zone;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.gefabstractmap.figures.manager.RootModelManager;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarButtonImageFigure;

/**
 * created by wchen on 2013-1-15 Detailled comment
 *
 */
public abstract class OutputZoneToolBar extends ZoneToolBar {

    /**
     * DOC Administrator OutputZoneToolBar constructor comment.
     *
     * @param rootModelManager
     */
    public OutputZoneToolBar(RootModelManager rootModelManager) {
        super(rootModelManager);
    }

    protected ToolBarButtonImageFigure add_btn, remove_btn, auto_map;

    @Override
    public void createZoneContent() {
        super.createZoneContent();
        add_btn = new AddButton();
        this.add(add_btn);
        remove_btn = new RemoveButton();
        remove_btn.setEnabled(false);
        this.add(remove_btn);
        this.add(move_up);
        this.add(move_down);
        add(new Label(" "));

        Image image = null;
        minimized = getMinSizeStatus();
        if (minimized) {
            image = restorImage;
        } else {
            image = miniImage;
        }
        min_size = new MinSizeButton(image);
        min_size.setEnabled(!isMinSizeEnable());
        this.add(min_size);

        auto_map = new AutoMapButton();
        this.add(auto_map);
        setTooltips();
    }

    private void setTooltips() {
        Label tooltip = new Label();
        tooltip.setText("Add output table");
        add_btn.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("Remove selected output table");
        remove_btn.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("Move up selected output table");
        move_up.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("Move down selected output table");
        move_down.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("Minimize all output tables");
        min_size.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("Map automatically inputs and outputs (for empty expressions only)");
        auto_map.setToolTip(tooltip);

    }

    public void setRemoveButtonEnable(boolean enable) {
        remove_btn.setEnabled(enable);
    }

    public void setMoveUpEnable(boolean enable) {
        move_up.setEnabled(enable);
    }

    public void setMoveDownEnable(boolean enable) {
        move_down.setEnabled(enable);
    }

    protected abstract void addTable();

    class AddButton extends ToolBarButtonImageFigure {

        public AddButton() {
            super(ImageProvider.getImage(EImage.ADD_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            addTable();
        }
    }

    protected abstract void removeTable();

    class RemoveButton extends ToolBarButtonImageFigure {

        public RemoveButton() {
            super(ImageProvider.getImage(EImage.MINUS_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            removeTable();
        }
    }

    public abstract void autoMap();

    class AutoMapButton extends ToolBarButtonImageFigure {

        public AutoMapButton() {
            super(null);
            setText("Auto Map");
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            autoMap();
        }
    }
}
