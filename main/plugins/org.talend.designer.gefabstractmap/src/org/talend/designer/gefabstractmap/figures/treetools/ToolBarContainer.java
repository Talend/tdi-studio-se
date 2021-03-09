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
package org.talend.designer.gefabstractmap.figures.treetools;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.talend.designer.gefabstractmap.figures.layout.ToolBarLayout;
import org.talend.designer.gefabstractmap.figures.manager.TableManager;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;

/**
 * created by Administrator on 2013-1-14 Detailled comment
 *
 */
public class ToolBarContainer extends Figure {

    private TableManager tableManager;

    protected ToolBarButtonImageFigure min_size;

    private Image restorImage = ImageProviderMapper.getImage(ImageInfo.RESTORE_ICON);

    private Image miniImage = ImageProviderMapper.getImage(ImageInfo.MINIMIZE_ICON);

    private ToolBarLayout manager = new ToolBarLayout();

    public ToolBarContainer(TableManager tableManager) {
        this.tableManager = tableManager;
    }

    public TableManager getTableManager() {
        return this.tableManager;
    }

    @Override
    public ToolBarLayout getLayoutManager() {
        return (ToolBarLayout) super.getLayoutManager();
    }

    protected void createToolbar() {
        manager.setVertical(false);
        manager.setSpacing(5);
        this.setLayoutManager(manager);

        Image image = null;
        if (tableManager.isMinimized()) {
            image = restorImage;
        } else {
            image = miniImage;
        }
        min_size = new MinSizeButton(image);
        setTooltips(min_size, "Minimize");
    }

    public void updateMinSizeImage() {
        if (tableManager.isMinimized()) {
            min_size.setImage(restorImage);
        } else {
            min_size.setImage(miniImage);
        }
    }

    public void updateButtonsColor(Color color) {
        min_size.setBackgroundColor(color);

    }

    protected void setTooltips(Figure figure, String text) {
        Label tooltip = new Label();
        tooltip.setText(text);
        figure.setToolTip(tooltip);
    }

    class MinSizeButton extends ToolBarButtonImageFigure {

        public MinSizeButton(Image image) {
            super(image);
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            CommandStack commandStack = tableManager.getGraphicalViewer().getEditDomain().getCommandStack();
            commandStack.execute(new Command() {

                @Override
                public void execute() {
                    tableManager.setMinimized(!tableManager.isMinimized());
                    tableManager.getGraphicalViewer().deselectAll();
                }
            });

        }
    }
}
