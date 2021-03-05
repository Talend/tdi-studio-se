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
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.designer.gefabstractmap.figures.layout.SearchZoneToolBarLayout;
import org.talend.designer.gefabstractmap.figures.manager.RootModelManager;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarButtonImageFigure;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public abstract class SearchZoneToolBar extends ZoneToolBar {

    public HightLightAllButton hightLightAll;

    public SearchZoneToolBar(RootModelManager rootModelManager) {
        super(rootModelManager);
    }

    @Override
    public void createZoneContent() {
        super.createZoneContent();
        SearchZoneToolBarLayout manager = new SearchZoneToolBarLayout();
        manager.setVertical(false);
        manager.setSpacing(8);
        setLayoutManager(manager);
        hightLightAll = new HightLightAllButton();
        setTooltips();
    }

    private void setTooltips() {
        Label tooltip = new Label();
        tooltip.setText("next");
        move_down.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("previous");
        move_up.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("hightlight all");
        hightLightAll.setToolTip(tooltip);

    }

    protected abstract void hightlightAll();

    public class HightLightAllButton extends ToolBarButtonImageFigure {

        public HightLightAllButton() {
            super(org.talend.commons.ui.runtime.image.ImageProvider.getImage(org.talend.commons.ui.runtime.image.ImageProvider
                    .getImageDesc(EImage.HIGHTLIGHT_ICON)));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            CommandStack commandStack = rootModelManager.getGraphicalViewer().getEditDomain().getCommandStack();
            commandStack.execute(new Command() {

                @Override
                public void execute() {
                    hightlightAll();
                }
            });
        }
    }
}
