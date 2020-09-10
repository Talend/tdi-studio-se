// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.core.ui.editor.nodecontainer;

import java.util.List;

import org.eclipse.draw2d.ButtonBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.core.PluginChecker;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.core.utils.ResourceDisposeUtil;

/**
 * This class create a figure with the given image. <br/>
 * eh
 *
 * $Id$
 *
 */
public class NodeContainerFigure extends Figure {

    private int alpha = -1;

    private NodeContainer nodeContainer;

    private ImageFigure breakpointFigure;

    private ImageFigure errorFigure;

    private ImageFigure warningFigure;

    private ImageFigure infoFigure;

    private SimpleHtmlFigure htmlStatusHint;

    private SimpleHtmlFigure hint;

    private ImageFigure markFigure;

    private ImageFigure validationRuleFigure;

    public static final String BREAKPOINT_IMAGE = "icons/breakpoint.png"; //$NON-NLS-1$

    private LabelCenter parallelFigure;

    private LabelCenter windowFigure;

    private RoundedRectangle rectFig;

    private Image errorMarkImage = ImageProvider.getImage(EImage.Error_Mark);

    private boolean showCompareMark;

    private final Node node;

    public NodeContainerFigure(NodeContainer nodeContainer) {
        this.nodeContainer = nodeContainer;
        FreeformLayout ffl = new FreeformLayout();
        ffl.setObserveVisibility(true);
        this.setLayoutManager(ffl);
        this.node = nodeContainer.getNode();
        // this.setOpaque(true);
        // this.setBackgroundColor(new Color(null, new RGB(200, 100, 200)));
        rectFig = new RoundedRectangle() {

            @Override
            protected void fillShape(Graphics graphics) {
                graphics.setLineWidth(4);
                if (node.isCompareFlag() && !node.isErrorFlag()) {
                    graphics.setLineStyle(SWT.LINE_DOT);
                }
                graphics.drawRoundRectangle(getBounds(), corner.width, corner.height);
            }

        };
        rectFig.setBackgroundColor(null);
        this.add(rectFig, null, 0);

        addWindowFigure();

        breakpointFigure = new ImageFigure();
        breakpointFigure.setImage(ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(BREAKPOINT_IMAGE)));
        breakpointFigure.setVisible(false);
        breakpointFigure.setSize(breakpointFigure.getPreferredSize());
        this.add(breakpointFigure);

        errorFigure = new ImageFigure();
        Image image = ImageProvider.getImage(CoreUIPlugin.getImageDescriptor(BREAKPOINT_IMAGE));
        errorFigure.setImage(image);
        errorFigure.setImage(ImageProvider.getImage(EImage.ERROR_SMALL));
        errorFigure.setVisible(false);
        errorFigure.setSize(errorFigure.getPreferredSize());
        this.add(errorFigure);

        warningFigure = new ImageFigure();
        warningFigure.setImage(ImageProvider.getImage(EImage.WARNING_SMALL));
        warningFigure.setVisible(false);
        warningFigure.setSize(warningFigure.getPreferredSize());
        this.add(warningFigure);

        infoFigure = new ImageFigure();
        infoFigure.setImage(ImageProvider.getImage(EImage.INFORMATION_SMALL));
        infoFigure.setVisible(false);
        infoFigure.setSize(infoFigure.getPreferredSize());
        this.add(infoFigure);

        validationRuleFigure = new ImageFigure();
        validationRuleFigure.setImage(ImageProvider.getImage(EImage.LOCK_ICON));
        validationRuleFigure.setVisible(false);
        validationRuleFigure.setSize(validationRuleFigure.getPreferredSize());
        this.add(validationRuleFigure);

        markFigure = new ImageFigure();
        markFigure.setImage(errorMarkImage);
        this.add(markFigure, null, 0);

        if (PluginChecker.isTeamEdition()) {
            addParallelFigure();
        }

        htmlStatusHint = new SimpleHtmlFigure();

        hint = new SimpleHtmlFigure();

        initializeNodeContainer(nodeContainer.getNodeMarkRectangle());
        rectFig.setVisible(false);
        markFigure.setVisible(false);

        addListenerForDialog();
    }

    /**
     * DOC bqian Comment method "addParallelFigure".
     */
    private void addParallelFigure() {
        parallelFigure = new LabelCenter();
        parallelFigure.setImage(ImageProvider.getImage(EImage.PARALLEL_EXECUTION));
        parallelFigure.setFont(Display.getDefault().getSystemFont());
        parallelFigure.setText("x0"); //$NON-NLS-1$
        parallelFigure.setToolTip(new Label("x0")); //$NON-NLS-1$

        boolean visible = false;
        IElementParameter enableParallelizeParameter = nodeContainer.getNode().getElementParameter(
                EParameterName.PARALLELIZE.getName());
        if (enableParallelizeParameter != null) {
            visible = (Boolean) enableParallelizeParameter.getValue();
        }
        parallelFigure.setVisible(visible);
        parallelFigure.setSize(parallelFigure.getPreferredSize());
        this.add(parallelFigure);
    }

    private void addWindowFigure() {
        windowFigure = new LabelCenter();
        windowFigure.setImage(ImageProvider.getImage(EImage.WINDOW));
        windowFigure.setFont(Display.getDefault().getSystemFont());

        boolean visible = false;
        IElementParameter windowDuration = nodeContainer.getNode().getElementParameter(EParameterName.WINDOW_DURATION.getName());
        IElementParameter defineSlideDuration = nodeContainer.getNode().getElementParameter(
                EParameterName.DEFINE_SLIDE_DURATION.getName());
        boolean defSlideDuration = false;
        if (defineSlideDuration != null) {
            defSlideDuration = (Boolean) defineSlideDuration.getValue();
        }

        if (windowDuration != null) {
            visible = true;
            String slideDuration = ""; //$NON-NLS-1$
            if (defSlideDuration) {
                slideDuration = "\nEvery: " //$NON-NLS-1$
                        + NodeContainerUtils.formatTime((String) nodeContainer.getNode()
                                .getElementParameter("SLIDE_DURATION").getValue()); //$NON-NLS-1$
            }

            windowFigure.setText("Length: " + NodeContainerUtils.formatTime((String) windowDuration.getValue()) + slideDuration); //$NON-NLS-1$
            windowFigure.setVisible(visible);
            windowFigure.setSize(windowFigure.getPreferredSize());
            this.add(windowFigure);
        }
    }

    public void updateErrorFlag(boolean flag) {
        rectFig.setVisible(flag);
        markFigure.setVisible(flag);
    }

    public void updateValidationRuleFigure(boolean isShow) {
        validationRuleFigure.setVisible(isShow);
    }

    public void updateStatus(int status, boolean showInfoFlag) {
        if ((status & Process.BREAKPOINT_STATUS) != 0) {
            breakpointFigure.setVisible(true);
        } else {
            breakpointFigure.setVisible(false);
        }

        if ((status & Process.ERROR_STATUS) != 0) {
            warningFigure.setVisible(false);
            errorFigure.setVisible(true);
        } else {
            errorFigure.setVisible(false);
            errorFigure.setToolTip(null);
        }

        if (((status & Process.WARNING_STATUS) != 0) && !errorFigure.isVisible()) {
            warningFigure.setVisible(true);
        } else {
            warningFigure.setVisible(false);
            warningFigure.setToolTip(null);
        }

        if (((status & Process.INFO_STATUS) != 0) && !errorFigure.isVisible() && !warningFigure.isVisible() && showInfoFlag) {
            warningFigure.setVisible(false);
            errorFigure.setVisible(false);
            infoFigure.setVisible(true);
        } else {
            infoFigure.setVisible(false);
        }

        if (errorFigure.isVisible() || warningFigure.isVisible() || infoFigure.isVisible()) {
            List<String> problemsList;

            String text = "<b>" + nodeContainer.getNode().getUniqueName() + "</b><br><br>"; //$NON-NLS-1$ //$NON-NLS-2$

            if ((status & Process.WARNING_STATUS) != 0) {
                text += "<i>Warnings:</i><br>"; //$NON-NLS-1$

                problemsList = Problems.getStatusList(ProblemStatus.WARNING, nodeContainer.getNode());
                for (String str : problemsList) {
                    text += "\t- " + str + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            if ((status & Process.ERROR_STATUS) != 0) {
                text += "<i>Errors:</i><br>"; //$NON-NLS-1$
                problemsList = Problems.getStatusList(ProblemStatus.ERROR, nodeContainer.getNode());
                for (String str : problemsList) {
                    text += "\t- " + str + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            htmlStatusHint.setText(text, true);
            if (errorFigure.isVisible()) {
                warningFigure.setToolTip(null);
                errorFigure.setToolTip(htmlStatusHint);
            } else if (infoFigure.isVisible()) {
                errorFigure.setToolTip(null);
                warningFigure.setToolTip(null);
            } else {
                errorFigure.setToolTip(null);
                warningFigure.setToolTip(htmlStatusHint);
            }
        }

        updateParallelFigure(status);

        updateWindowFigure(status);

        updateValidationRuleFigure();
    }

    private void updateValidationRuleFigure() {
        IElementParameter param = nodeContainer.getNode().getElementParameter(EParameterName.VALIDATION_RULES.getName());
        if (param != null && param.getValue() != null && param.getValue() instanceof Boolean) {
            updateValidationRuleFigure((Boolean) param.getValue());
        }
    }

    /**
     * DOC YeXiaowei Comment method "updateParallelFigure".
     *
     * @param status
     */
    private void updateParallelFigure(int status) {

        if (!PluginChecker.isTeamEdition() || parallelFigure == null) {
            return;
        }

        String numberParallel = "0"; //$NON-NLS-1$
        if ((status & Process.PARALLEL_STATUS) != 0) {
            IElementParameter numberParallelizeParameter = nodeContainer.getNode().getElementParameter(
                    EParameterName.PARALLELIZE_NUMBER.getName());
            if (numberParallelizeParameter != null) {
                numberParallel = (String) numberParallelizeParameter.getValue();
            }
            String paralString = "x" + numberParallel; //$NON-NLS-1$
            parallelFigure.setText(paralString);
            parallelFigure.setToolTip(new Label(paralString));
            parallelFigure.setVisible(true);
        } else {
            parallelFigure.setVisible(false);
        }
    }

    /**
     * DOC YeXiaowei Comment method "updateParallelFigure".
     *
     * @param status
     */
    private void updateWindowFigure(int status) {

        if (windowFigure == null) {
            return;
        }

        if ((status & Process.WINDOW_STATUS) != 0) {
            boolean visible = false;
            IElementParameter windowDuration = nodeContainer.getNode().getElementParameter(
                    EParameterName.WINDOW_DURATION.getName());
            IElementParameter defineSlideDuration = nodeContainer.getNode().getElementParameter(
                    EParameterName.DEFINE_SLIDE_DURATION.getName());
            boolean defSlideDuration = false;
            if (defineSlideDuration != null) {
                defSlideDuration = (Boolean) defineSlideDuration.getValue();
            }

            if (windowDuration != null) {
                visible = true;
                String slideDuration = ""; //$NON-NLS-1$
                if (defSlideDuration) {
                    slideDuration = "\nEvery: " //$NON-NLS-1$
                            + NodeContainerUtils.formatTime((String) nodeContainer.getNode()
                                    .getElementParameter("SLIDE_DURATION").getValue()); //$NON-NLS-1$
                }

                windowFigure
                        .setText("Length: " + NodeContainerUtils.formatTime((String) windowDuration.getValue()) + slideDuration); //$NON-NLS-1$
                windowFigure.setVisible(visible);
            }
        } else {
            windowFigure.setVisible(false);
        }
    }

    @Override
    public void paint(Graphics graphics) {
        if (alpha != -1) {
            graphics.setAlpha(alpha);
        } else {
            graphics.setAlpha(255);
        }
        if (breakpointFigure.isVisible()) {
            breakpointFigure.setLocation(nodeContainer.getBreakpointLocation());
        }
        if (errorFigure.isVisible()) {
            errorFigure.setLocation(nodeContainer.getErrorLocation());
//errorFigure.setBorder(new LineBorder(new Color(Display.getDefault(), new RGB(255,255,0)), 2));
        }
        if (infoFigure.isVisible()) {
            infoFigure.setLocation(nodeContainer.getInfoLocation());
        }
        if (warningFigure.isVisible()) {
            warningFigure.setLocation(nodeContainer.getWarningLocation());
        }
        if (parallelFigure != null && parallelFigure.isVisible()) {
            parallelFigure.setLocation(nodeContainer.getParallelLocation());
        }
        if (windowFigure != null && windowFigure.isVisible()) {
            windowFigure.setLocation(nodeContainer.getWindowLocation());
        }
        if (rectFig.isVisible()) {
            rectFig.setLocation(nodeContainer.getMarkLocation());
        }
        if (markFigure.isVisible()) {
            markFigure.setLocation(nodeContainer.getErrorMarkLocation());
        }
        if (validationRuleFigure.isVisible()) {
            validationRuleFigure.setLocation(nodeContainer.getValidationRuleLocation());
        }
        super.paint(graphics);
    }

    public int getAlpha() {
        return this.alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void initializeNodeContainer(Rectangle rectangle) {
        this.disposeColors();
        rectFig.setLocation(new Point(rectangle.x, rectangle.y));
        rectFig.setSize(new Dimension(rectangle.width, rectangle.height));
        if (isShowCompareMark()) {
            rectFig.setVisible(true);
            rectFig.setForegroundColor(new Color(Display.getDefault(), new RGB(204, 153, 255)));
        } else {
            rectFig.setForegroundColor(new Color(Display.getDefault(), new RGB(255, 102, 102)));
        }
        markFigure.setSize(errorMarkImage.getImageData().width, errorMarkImage.getImageData().height);
    }

    public void disposeColors() {

        ResourceDisposeUtil.disposeColor(rectFig.getForegroundColor());
    }

    public void setInfoHint(String hintText) {
        if (infoFigure.isVisible()) {
            if (hintText.equals("") || hintText == null) { //$NON-NLS-1$
                infoFigure.setToolTip(null);
            } else {
                htmlStatusHint.setText(hintText);
                infoFigure.setToolTip(htmlStatusHint);
            }
        } else {
            infoFigure.setToolTip(null);
        }

    }

    public void addListenerForDialog() {

        // markFigure.addMouseMotionListener(new MouseMotionListener() {
        //
        // public void mouseDragged(MouseEvent me) {
        // }
        //
        // public void mouseEntered(MouseEvent me) {
        // Node node = nodeContainer.getNode();
        // if (node.isErrorFlag()) {
        // Shell shell = Display.getCurrent().getActiveShell();// getViewer().getControl().getShell();
        // ErrorMessageDialog dialog = new ErrorMessageDialog(new Shell(shell), node);
        // dialog.open();
        // }
        //
        // }
        //
        // public void mouseExited(MouseEvent me) {
        // }
        //
        // public void mouseHover(MouseEvent me) {
        //
        // }
        //
        // public void mouseMoved(MouseEvent me) {
        // }
        //
        // });
    }

    public boolean isShowCompareMark() {
        return this.showCompareMark;
    }

    public void setShowCompareMark(boolean showCompareMark) {
        this.showCompareMark = showCompareMark;
    }

}
