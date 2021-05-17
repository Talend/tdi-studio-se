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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.designer.gefabstractmap.figures.layout.ZoneToolBarLayout;
import org.talend.designer.gefabstractmap.figures.manager.RootModelManager;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarButtonImageFigure;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;

/**
 * DOC talend class global comment. Detailled comment
 */
public abstract class ZoneToolBar extends Figure {

    protected ToolBarButtonImageFigure move_up, move_down, min_size;

    protected boolean minimized = true;

    protected Image restorImage = ImageProviderMapper.getImage(ImageInfo.RESTORE_ICON);

    protected Image miniImage = ImageProviderMapper.getImage(ImageInfo.MINIMIZE_ICON);

    protected RootModelManager rootModelManager;

    public ZoneToolBar(RootModelManager rootModelManager) {
        this.rootModelManager = rootModelManager;

        ZoneToolBarLayout manager = new ZoneToolBarLayout();
        manager.setVertical(false);
        manager.setSpacing(8);
        setLayoutManager(manager);
        setOpaque(true);
        setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.ZONE_BACKGROUND_COLOR));

    }

    public void createZoneContent() {
        move_up = new MoveUpButton();
        move_up.setEnabled(false);
        move_down = new MoveDownButton();
        move_down.setEnabled(false);
        Image image = null;
        minimized = getMinSizeStatus();
        if (minimized) {
            image = restorImage;
        } else {
            image = miniImage;
        }
        min_size = new MinSizeButton(image);
        min_size.setEnabled(!isMinSizeEnable());
    }

    protected abstract boolean getMinSizeStatus();

    protected abstract boolean isMinSizeEnable();

    protected abstract void moveUp();

    protected abstract void moveDown();

    protected abstract void minSize();

    class MoveUpButton extends ToolBarButtonImageFigure {

        public MoveUpButton() {
            super(ImageProvider.getImage(EImage.UP_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            CommandStack commandStack = rootModelManager.getGraphicalViewer().getEditDomain().getCommandStack();
            commandStack.execute(new Command() {

                @Override
                public void execute() {
                    moveUp();
                }
            });

        }
    }

    class MoveDownButton extends ToolBarButtonImageFigure {

        public MoveDownButton() {
            super(ImageProvider.getImage(EImage.DOWN_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);

            CommandStack commandStack = rootModelManager.getGraphicalViewer().getEditDomain().getCommandStack();
            commandStack.execute(new Command() {

                @Override
                public void execute() {
                    moveDown();
                }
            });

        }
    }

    class MinSizeButton extends ToolBarButtonImageFigure {

        public MinSizeButton(Image image) {
            super(image);
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            CommandStack commandStack = rootModelManager.getGraphicalViewer().getEditDomain().getCommandStack();
            commandStack.execute(new Command() {

                @Override
                public void execute() {
                    minSize();
                }
            });

            if (minimized) {
                setImage(restorImage);
            } else {
                setImage(miniImage);
            }
            rootModelManager.getGraphicalViewer().deselectAll();
        }
    }

}
