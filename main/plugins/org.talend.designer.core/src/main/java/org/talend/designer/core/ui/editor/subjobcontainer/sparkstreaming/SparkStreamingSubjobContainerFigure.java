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
package org.talend.designer.core.ui.editor.subjobcontainer.sparkstreaming;

import java.util.List;

import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.INode;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerUtils;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainer;
import org.talend.designer.core.ui.editor.subjobcontainer.SubjobContainerFigure;
import org.talend.designer.core.utils.DesignerColorUtils;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class SparkStreamingSubjobContainerFigure extends SubjobContainerFigure {

    private RoundedRectangle timeoutFigureRect;

    private RoundedRectangle statsFigureRect;

    private SimpleHtmlFigure timeoutFigureText;

    private SimpleHtmlFigure statsFigureText;

    private String timeoutText;

    private boolean showTimeout = false;

    // This attribute is used to define if a subjob has more than one node (or not). We won't display the stats if there
    // is only one node.
    private boolean subjobHasMoreThanOneNode = false;

    // This attribute is used to define if a subjob is activated (or not). We won't display the statistics if a subjob
    // is deactivated.
    private boolean subjobStartNodeIsActive = false;

    private final static int TIMEOUT_FIGURE_WIDTH = 200;

    private final static int TIMEOUT_FIGURE_HEIGHT = 45;

    private final static int TIMEOUT_PADDING = 5;

    private final static int STATS_FIGURE_WIDTH = 300;

    private final static int STATS_FIGURE_HEIGHT = 100;

    public SparkStreamingSubjobContainerFigure(SubjobContainer subjobContainerTmp) {
        super(subjobContainerTmp);

        updateData();

        this.subjobHasMoreThanOneNode = this.subjobContainer.getNodeContainers().size() > 1;

        this.subjobStartNodeIsActive = this.subjobContainer.getSubjobStartNode() != null
                && this.subjobContainer.getSubjobStartNode().isActivate();
    }

    @Override
    protected void initFigure() {
        super.initFigure();
        timeoutFigureRect = new RoundedRectangle();
        timeoutFigureText = new SimpleHtmlFigure();
        timeoutFigureText.setOpaque(false);

        statsFigureRect = new RoundedRectangle();
        statsFigureText = new SimpleHtmlFigure();
        statsFigureText.setOpaque(false);

        String text = "<b>Batch started</b>: <br>"; //$NON-NLS-1$
        text += "<b>Batch completed</b>: <br><br><br>"; //$NON-NLS-1$
        text += "<b>Last scheduling delay</b>: <br>"; //$NON-NLS-1$
        text += "<b>Last processing delay</b>: <br>"; //$NON-NLS-1$
        text += "<b>Last total delay</b>: <br>"; //$NON-NLS-1$

        statsFigureText.setText(text);
    }

    @Override
    public void initializeSubjobContainer(Rectangle rectangle) {

        // Resize the rectangle to take the spark streaming information into account in the rectangle dimensions.
        // This part is for the information at the top left corner.

        this.subjobHasMoreThanOneNode = this.subjobContainer.getNodeContainers().size() > 1;
        this.subjobStartNodeIsActive = this.subjobContainer.getSubjobStartNode() != null
                && this.subjobContainer.getSubjobStartNode().isActivate();

        if ((!this.subjobContainer.isCollapsed()) && this.subjobHasMoreThanOneNode && this.subjobStartNodeIsActive) {
            rectangle.resize((rectangle.width() < TIMEOUT_FIGURE_WIDTH) ? TIMEOUT_FIGURE_WIDTH : 0, TIMEOUT_FIGURE_HEIGHT + 10);
            rectangle.setY(rectangle.y() - TIMEOUT_FIGURE_HEIGHT - 10);

            rectangle.resize(STATS_FIGURE_WIDTH, STATS_FIGURE_HEIGHT);
        }

        super.initializeSubjobContainer(rectangle);
        Point location = this.getLocation();

        timeoutFigureText.setText(timeoutText);
        timeoutFigureText.setSize(TIMEOUT_FIGURE_WIDTH - TIMEOUT_PADDING * 2, showTimeout ? TIMEOUT_FIGURE_HEIGHT
                - TIMEOUT_PADDING * 2 : (TIMEOUT_FIGURE_HEIGHT - TIMEOUT_PADDING) / 2);
        timeoutFigureText.setLocation(new Point(location.x() + TIMEOUT_PADDING * 2, location.y() + 20 + TIMEOUT_PADDING));
        timeoutFigureText.setVisible(false);
        if (this.subjobHasMoreThanOneNode && this.subjobStartNodeIsActive) {
            timeoutFigureText.setVisible(true);
        }
        timeoutFigureText.setBackgroundColor(new Color(Display.getDefault(), mainColor));// //////////////////////
        timeoutFigureText.setForegroundColor(new Color(Display.getDefault(), subjobTitleColor));

        timeoutFigureRect.setLocation(new Point(location.x() + TIMEOUT_PADDING, location.y() + 20));
        timeoutFigureRect.setVisible(false);
        if (this.subjobHasMoreThanOneNode && this.subjobStartNodeIsActive) {
            timeoutFigureRect.setVisible(!this.subjobContainer.isCollapsed());
        }
        timeoutFigureRect.setBackgroundColor(new Color(Display.getDefault(), mainColor));// //////////////////////
        timeoutFigureRect.setForegroundColor(new Color(Display.getDefault(), subjobTitleColor));
        timeoutFigureRect.setSize(TIMEOUT_FIGURE_WIDTH, showTimeout ? TIMEOUT_FIGURE_HEIGHT
                : (TIMEOUT_FIGURE_HEIGHT + TIMEOUT_PADDING) / 2);

        setStatText();
        statsFigureText.setSize(STATS_FIGURE_WIDTH - 10, STATS_FIGURE_HEIGHT - 10);
        statsFigureText.setLocation(new Point(location.x() + rectangle.width() - STATS_FIGURE_WIDTH, location.y()
                + rectangle.height() - STATS_FIGURE_HEIGHT));
        statsFigureText.setVisible(false);
        if (this.subjobHasMoreThanOneNode && this.subjobStartNodeIsActive) {
            statsFigureText.setVisible(true);
        }
        statsFigureText.setBackgroundColor(new Color(Display.getDefault(), mainColor));// //////////////////////
        statsFigureText.setForegroundColor(new Color(Display.getDefault(), subjobTitleColor));

        statsFigureRect.setLocation(new Point(location.x() + rectangle.width() - STATS_FIGURE_WIDTH - 5, location.y()
                + rectangle.height() - STATS_FIGURE_HEIGHT - 5));
        statsFigureRect.setVisible(false);
        if (this.subjobHasMoreThanOneNode && this.subjobStartNodeIsActive) {
            statsFigureRect.setVisible(!this.subjobContainer.isCollapsed());
        }
        statsFigureRect.setBackgroundColor(new Color(Display.getDefault(), mainColor));// //////////////////////
        statsFigureRect.setForegroundColor(new Color(Display.getDefault(), subjobTitleColor));
        statsFigureRect.setSize(STATS_FIGURE_WIDTH, STATS_FIGURE_HEIGHT);

    }

    @Override
    public void disposeColors() {
        super.disposeColors();
        if (timeoutFigureRect.getForegroundColor() != null && !timeoutFigureRect.getForegroundColor().isDisposed()) {
            timeoutFigureRect.getForegroundColor().dispose();
        }
        if (timeoutFigureRect.getBackgroundColor() != null && !timeoutFigureRect.getBackgroundColor().isDisposed()) {
            timeoutFigureRect.getBackgroundColor().dispose();
        }

        if (statsFigureRect.getForegroundColor() != null && !statsFigureRect.getForegroundColor().isDisposed()) {
            statsFigureRect.getForegroundColor().dispose();
        }
        if (statsFigureRect.getBackgroundColor() != null && !statsFigureRect.getBackgroundColor().isDisposed()) {
            statsFigureRect.getBackgroundColor().dispose();
        }
    }

    /**
     * yzhang Comment method "updateData".
     */
    @Override
    public void updateData() {

        showTitle = (Boolean) subjobContainer.getPropertyValue(EParameterName.SHOW_SUBJOB_TITLE.getName());
        List<? extends INode> nodes = subjobContainer.getProcess().getNodesOfType("tSparkConfiguration"); //$NON-NLS-1$
        INode sparkConf = (nodes != null && nodes.size() > 0) ? nodes.get(0) : null;
        showTimeout = "true".equals(ElementParameterParser.getValue(sparkConf, "__DEFINE_DURATION__")); //$NON-NLS-1$ //$NON-NLS-2$
        String timeout = (showTimeout ? "<br><b>Duration:</b> " + NodeContainerUtils.formatTime(ElementParameterParser.getValue(sparkConf, "__STREAMING_DURATION__")) : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        timeoutFigureText.setSize(TIMEOUT_FIGURE_WIDTH - TIMEOUT_PADDING * 2, showTimeout ? TIMEOUT_FIGURE_HEIGHT
                - TIMEOUT_PADDING * 2 : (TIMEOUT_FIGURE_HEIGHT - TIMEOUT_PADDING) / 2);
        timeoutFigureRect.setSize(TIMEOUT_FIGURE_WIDTH, showTimeout ? TIMEOUT_FIGURE_HEIGHT
                : (TIMEOUT_FIGURE_HEIGHT + TIMEOUT_PADDING) / 2);

        timeoutText = "<b>Batch size:</b> " + NodeContainerUtils.formatTime(ElementParameterParser.getValue(sparkConf, "__STREAMING_BATCH_SIZE__")) + timeout; //$NON-NLS-1$ //$NON-NLS-2$

        title = (String) subjobContainer.getPropertyValue(EParameterName.SUBJOB_TITLE.getName());
        if (subjobContainer.getSubjobStartNode().getComponent().getName().equals("tPrejob")) { //$NON-NLS-1$
            title = " Prejob:" + title; //$NON-NLS-1$
            subjobContainer.getElementParameter(EParameterName.SHOW_SUBJOB_TITLE.getName()).setShow(false);
        } else if (subjobContainer.getSubjobStartNode().getComponent().getName().equals("tPostjob")) { //$NON-NLS-1$
            title = " Postjob:" + title; //$NON-NLS-1$
            subjobContainer.getElementParameter(EParameterName.SHOW_SUBJOB_TITLE.getName()).setShow(false);
        } else {
            subjobContainer.getElementParameter(EParameterName.SHOW_SUBJOB_TITLE.getName()).setShow(true);
        }
        String propertyValue = (String) subjobContainer.getPropertyValue(EParameterName.SUBJOB_TITLE_COLOR.getName());
        if (propertyValue == null || "".equals(propertyValue)) { //$NON-NLS-1$
            RGB colorValue = DesignerColorUtils.getPreferenceSubjobRGB(DesignerColorUtils.SUBJOB_TITLE_COLOR_NAME,
                    DesignerColorUtils.SUBJOB_TITLE_COLOR);
            subjobContainer.setPropertyValue(EParameterName.SUBJOB_TITLE_COLOR.getName(), ColorUtils.getRGBValue(colorValue));
        }
        //
        propertyValue = (String) subjobContainer.getPropertyValue(EParameterName.SUBJOB_COLOR.getName());
        if (propertyValue == null || "".equals(propertyValue)) { //$NON-NLS-1$
            RGB colorValue = DesignerColorUtils.getPreferenceSubjobRGB(DesignerColorUtils.SUBJOB_COLOR_NAME,
                    DesignerColorUtils.SUBJOB_COLOR);
            subjobContainer.setPropertyValue(EParameterName.SUBJOB_COLOR.getName(), ColorUtils.getRGBValue(colorValue));
        }

        setStatText();

        mainColor = ColorUtils.parseStringToRGB(propertyValue, DesignerColorUtils.SUBJOB_COLOR);

        this.getChildren().remove(outlineFigure);
        this.getChildren().remove(rectFig);
        outlineFigure.getChildren().clear();
        rectFig.getChildren().clear();
        timeoutFigureRect.getChildren().clear();
        statsFigureRect.getChildren().clear();

        timeoutFigureRect.add(timeoutFigureText);
        statsFigureRect.add(statsFigureText);
        rectFig.add(timeoutFigureRect);
        rectFig.add(statsFigureRect);
        if (showTitle) {
            outlineFigure.add(titleFigure);
            outlineFigure.add(collapseFigure);
            add(rectFig, null, 0);
            add(outlineFigure, null, 1);
        } else {
            outlineFigure.add(titleFigure);
            rectFig.add(collapseFigure);
            add(outlineFigure, null, 0);
            add(rectFig, null, 1);
        }
    }

    private void setStatText() {
        String text = "<b>Batch started</b>: " + ((SparkStreamingSubjobContainer) subjobContainer).getBatchStarted() + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        text += "<b>Batch completed</b>: " + ((SparkStreamingSubjobContainer) subjobContainer).getBatchCompleted() + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        text += "<b>Last scheduling delay</b>: " + ((SparkStreamingSubjobContainer) subjobContainer).getLastSchedulingDelay() + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        text += "<b>Last processing delay</b>: " + ((SparkStreamingSubjobContainer) subjobContainer).getLastProcessingDelay() + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        text += "<b>Last total delay</b>: " + ((SparkStreamingSubjobContainer) subjobContainer).getLastTotalDelay() + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
        statsFigureText.setText(text);
    }

    public SubjobContainer getSubjobContainer() {
        return this.subjobContainer;
    }

    public SimpleHtmlFigure getStatsFigure() {
        return this.statsFigureText;
    }
}
