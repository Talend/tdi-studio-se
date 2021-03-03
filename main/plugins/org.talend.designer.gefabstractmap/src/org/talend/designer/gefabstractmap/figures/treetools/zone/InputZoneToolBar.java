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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.talend.designer.gefabstractmap.figures.manager.RootModelManager;
import org.talend.designer.gefabstractmap.figures.treetools.ToolBarButtonImageFigure;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;

/**
 * created by Administrator on 2013-1-15 Detailled comment
 *
 */
public abstract class InputZoneToolBar extends ZoneToolBar {

    /**
     * DOC Administrator InputZoneTooBar constructor comment.
     *
     * @param mapDataPart
     */
    public InputZoneToolBar(RootModelManager rootModelManager) {
        super(rootModelManager);
    }

    private ToolBarButtonImageFigure propertyButton;

    private boolean isDieOnError = true;

    @Override
    public void createZoneContent() {
        super.createZoneContent();

        this.add(move_up);

        this.add(move_down);
        Label figure = new Label("");
        figure.setOpaque(true);
        figure.setBackgroundColor(ColorConstants.lightGray);
        add(figure);

        // disable for 5.0.0
        // propertyButton = new PropertyButton();
        // add(propertyButton);

        this.add(min_size);
        setTooltips();
    }

    private void setTooltips() {
        Label tooltip = new Label();
        tooltip.setText("Move up selected input lookup table");
        move_up.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("Move down selected input lookup table");
        move_down.setToolTip(tooltip);

        tooltip = new Label();
        tooltip.setText("Minimize all input tables");
        min_size.setToolTip(tooltip);

        // disable for 5.0.0
        // tooltip = new Label();
        // tooltip.setText("Setup the configurations of tXMLMap");
        // propertyButton.setToolTip(tooltip);

    }

    public void setMoveUpEnable(boolean enable) {
        move_up.setEnabled(enable);
    }

    public void setMoveDownEnable(boolean enable) {
        move_down.setEnabled(enable);
    }

    public abstract void changProperty();

    class PropertyButton extends ToolBarButtonImageFigure {

        public PropertyButton() {
            super(ImageProviderMapper.getImage(ImageInfo.PROPERTY_TOOL_ICON));
        }

        @Override
        public void toolBarButtonPressed(MouseEvent me) {
            super.toolBarButtonPressed(me);
            changProperty();
        }
    }

}
