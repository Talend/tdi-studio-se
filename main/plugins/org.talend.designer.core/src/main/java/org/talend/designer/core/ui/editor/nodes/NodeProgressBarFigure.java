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
package org.talend.designer.core.ui.editor.nodes;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class NodeProgressBarFigure extends Figure {

    private ImageFigure progressFig;

    private int alpha = -1;

    int widthFi;

    int heightFi;

    private Node node;

    public NodeProgressBarFigure(Node node) {
        ToolbarLayout layout = new ToolbarLayout();
        setLayoutManager(layout);
        this.node = node;

    }

    // public void updateVisible(boolean flag) {
    // setProgressData(new Double(0));
    // }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setProgressData(Double extent) {
        List childs = this.getChildren();
        childs.clear();
        if (extent == 0) {
            return;
        }
        Figure baseFigure = new Figure();
        baseFigure.setLayoutManager(new ToolbarLayout(true));
        baseFigure.setVisible(true);

        Double extentString = Math.floor(extent / 10);
        Double extentFlag = Math.floor(extent);

        Figure progressBarFigure = new Figure();
        progressBarFigure.setBorder(new LineBorder(ColorConstants.black, 1));
        progressBarFigure.setLayoutManager(new ToolbarLayout(true));
        progressBarFigure.setSize(60, 10);
        progressBarFigure.setPreferredSize(60, 10);

        progressBarFigure.setVisible(true);

        SimpleHtmlFigure dataFigure = new SimpleHtmlFigure();
        dataFigure.setVisible(true);
        // Font font = new Font(Display.getDefault(), "Arial", 9, SWT.ITALIC);
        // dataFigure.setFont(font);
        //
        //        dataFigure.setText(extentFlag.intValue() + "%");//$NON-NLS-1$

        dataFigure.setText("<font color='#000000'> <b> " + extentFlag.intValue() + "%" //$NON-NLS-1$
                + "</b></font>"); //$NON-NLS-1$ //$NON-NLS-2$

        // dataFigure.setSize(27, 12);
        // dataFigure.setPreferredSize(27, 12);

        int nodeX = progressBarFigure.getLocation().x;
        int nodeY = progressBarFigure.getLocation().y;

        if (extentString == 0) {
            if (extent > 0 && extent < 1) {
                ImageFigure progressDataFigure = new ImageFigure();
                Image image = ImageProvider.getImage(ECoreImage.PROGRESSGRAYBAR);
                progressDataFigure.setImage(image);
                progressDataFigure.setVisible(true);
                progressBarFigure.add(progressDataFigure);
            } else if (extent >= 1 && extent < 10) {
                ImageFigure progressDataFigure = new ImageFigure();
                Image image = ImageProvider.getImage(ECoreImage.PROGRESSGRAYGEBAR);
                progressDataFigure.setImage(image);
                progressDataFigure.setVisible(true);
                progressBarFigure.add(progressDataFigure);
            }

        } else if (extentString == 10) {
            ImageFigure progressDataFigure = new ImageFigure();
            Image image = ImageProvider.getImage(ECoreImage.PROGRESSGREEBAR);
            progressDataFigure.setImage(image);
            progressDataFigure.setVisible(true);
            progressBarFigure.add(progressDataFigure);
        } else if (extentString > 0 && extentString < 10) {

            for (int i = 0; i < extentString; i++) {
                ImageFigure progressDataFigure = new ImageFigure();
                Image image = ImageProvider.getImage(ECoreImage.PROGRESSBAR);
                progressDataFigure.setImage(image);
                progressDataFigure.setVisible(true);
                progressBarFigure.add(progressDataFigure);
                int imageWith = image.getImageData().width;
                if (i != 0) {
                    Point point = new Point(nodeX + i * imageWith, nodeY);
                    progressDataFigure.setLocation(point);
                }

            }

            for (int j = 0; j < (10 - extentString); j++) {
                ImageFigure progressDataFigure = new ImageFigure();
                Image image = ImageProvider.getImage(ECoreImage.PROGRESSBARBlACK);
                progressDataFigure.setImage(image);
                progressDataFigure.setVisible(true);
                progressBarFigure.add(progressDataFigure);
                int imageWith = image.getImageData().width;
                if (j != 0) {
                    Point point = new Point(nodeX + extentString * imageWith + j * imageWith, nodeY);
                    progressDataFigure.setLocation(point);
                }
            }

        }

        baseFigure.setSize(dataFigure.getPreferredSize().width + progressBarFigure.getPreferredSize().width, dataFigure
                .getPreferredSize().height);
        baseFigure.setPreferredSize(dataFigure.getPreferredSize().width + progressBarFigure.getPreferredSize().width, dataFigure
                .getPreferredSize().height);

        baseFigure.add(progressBarFigure);
        baseFigure.add(dataFigure);

        this.add(baseFigure);
        this.setSize(baseFigure.getSize());
    }
}
