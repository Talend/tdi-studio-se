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
package org.talend.designer.business.diagram.custom.figures;

import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.talend.designer.business.diagram.i18n.Messages;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class BusinessTooltipFigure extends Figure {

    private int alpha = -1;

    private String text = ""; //$NON-NLS-1$

    public BusinessTooltipFigure() {
        super();
        setLayoutManager(new FlowLayout(false));

    }

    public void buildFigures(List<Label> labels) {
        if (labels != null) {
            removeAll();
            Label label = new Label(Messages.getString("BusinessTooltipFigure.assignedMeta", labels.size())); //$NON-NLS-1$
            String fontName = JFaceResources.getDefaultFont().getFontData()[0].getName();
            if (label.getFont() != null) {
                fontName = label.getFont().getFontData()[0].getName();
            }
            label.setFont(JFaceResources.getFontRegistry().getBold(fontName));
            add(label);
            for (int i = 0; i < labels.size(); i++) {
                add(labels.get(i));
            }
        }
        // setPreferredSize(computePreferedSize());
    }

    @SuppressWarnings("unchecked")
    private Dimension computePreferedSize() {
        Dimension size = new Dimension();

        // Vertical path
        List<IFigure> children = getChildren();
        for (IFigure fv : children) {

            // Horizontal path
            Dimension sizeH = new Dimension();
            List<IFigure> childrenH = fv.getChildren();
            for (IFigure fh : childrenH) {
                sizeH.width += fh.getPreferredSize().width;
                sizeH.height = Math.max(sizeH.height, fh.getPreferredSize().height);
            }

            size.width = Math.max(size.width, sizeH.width);
            size.height += sizeH.height;
        }
        return size;
    }

    @Override
    public void paint(Graphics graphics) {
        if (alpha != -1) {
            graphics.setAlpha(alpha);
        } else {
            graphics.setAlpha(255);
        }
        graphics.setBackgroundColor(new Color(Display.getCurrent(), 255, 255, 180));
        graphics.fillRectangle(getBounds());
        super.paint(graphics);
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public String getText() {
        return this.text;
    }

}
