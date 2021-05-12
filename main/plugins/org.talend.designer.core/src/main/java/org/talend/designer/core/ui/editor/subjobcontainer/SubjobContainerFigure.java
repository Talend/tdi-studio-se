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
package org.talend.designer.core.ui.editor.subjobcontainer;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.process.IGEFProcess;
import org.talend.core.ui.services.IDesignerCoreUIService;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.utils.DesignerColorUtils;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class SubjobContainerFigure extends Figure {

    protected SubjobContainer subjobContainer;

    protected RoundedRectangle outlineFigure;

    protected SimpleHtmlFigure titleFigure;

    protected RoundedRectangle rectFig;

    protected SubjobCollapseFigure collapseFigure;

    protected RGB mainColor;

    protected boolean showTitle;

    protected String title;

    protected RGB subjobTitleColor;

    /**
     * DOC nrousseau SubjobContainerFigure constructor comment.
     *
     * @param model
     */
    public SubjobContainerFigure(SubjobContainer subjobContainerTmp) {
        setLayoutManager(new FreeformLayout());
        this.subjobContainer = subjobContainerTmp;

        initFigure();

        initSubJobTitleColor();

        updateData();

        initializeSubjobContainer(subjobContainer.getSubjobContainerRectangle());
    }

    /**
     * DOC rdubois Comment method "initFigure".
     */
    protected void initFigure() {
        outlineFigure = new RoundedRectangle();
        rectFig = new RoundedRectangle();
        titleFigure = new SimpleHtmlFigure();
        titleFigure.setOpaque(true);
        collapseFigure = new SubjobCollapseFigure();

        collapseFigure.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                executeCollapseCommand(!subjobContainer.isCollapsed());
            }
        });
    }

    public void executeCollapseCommand(boolean isCollapsed) {
        IProcess2 process = subjobContainer.getProcess();
        if (!process.isReadOnly()) {
            PropertyChangeCommand ppc = new PropertyChangeCommand(subjobContainer, EParameterName.COLLAPSED.getName(),
                    isCollapsed);
            boolean executed = false;
            if (process instanceof IGEFProcess) {
                IDesignerCoreUIService designerCoreUIService = CoreUIPlugin.getDefault().getDesignerCoreUIService();
                if (designerCoreUIService != null) {
                    executed = designerCoreUIService.executeCommand((IGEFProcess) process, ppc);
                }
            }
            if (!executed) {
                ppc.execute();
            }
            reSelection();
        }
    }

    /**
     * yzhang Comment method "initSubJobTitleColor".
     */
    protected void initSubJobTitleColor() {
        IElementParameter colorParameter = subjobContainer.getElementParameter(EParameterName.SUBJOB_TITLE_COLOR.getName());
        // Color titleColor = ColorUtils.SUBJOB_TITLE_COLOR;
        if (subjobContainer.getSubjobStartNode().getComponent().getName().equals("tPrejob") //$NON-NLS-1$
                || subjobContainer.getSubjobStartNode().getComponent().getName().equals("tPostjob")) { //$NON-NLS-1$
            // titleColor = ColorUtils.SPECIAL_SUBJOB_TITLE_COLOR;
        }
        RGB defaultSubjobColor = DesignerColorUtils.getPreferenceSubjobRGB(DesignerColorUtils.SUBJOB_TITLE_COLOR_NAME,
                DesignerColorUtils.SUBJOB_TITLE_COLOR);
        if (colorParameter.getValue() == null) {
            subjobTitleColor = defaultSubjobColor;
            String colorValue = ColorUtils.getRGBValue(subjobTitleColor);
            colorParameter.setValue(colorValue);
        } else {
            String strRgb = (String) colorParameter.getValue();
            subjobTitleColor = ColorUtils.parseStringToRGB(strRgb, defaultSubjobColor);
        }
    }

    protected void reSelection() {
        // select the start node.
        if (subjobContainer.isCollapsed()) {
            IProcess2 process = subjobContainer.getProcess();
            AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) process.getEditor();
            Node startNode = subjobContainer.getSubjobStartNode();
            if (startNode != null && editor != null) {
                if ((startNode.isJoblet() && !startNode.getNodeContainer().isCollapsed()) || startNode.getJunitNode() != null) {
                    editor.getTalendEditor().getViewer().deselectAll();
                    return;
                }
                editor.selectNode(startNode);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paint(Graphics graphics) {
        graphics.setAlpha(100);
        super.paint(graphics);
    }

    public void initializeSubjobContainer(Rectangle rectangle) {
        disposeColors();
        Point location = this.getLocation();
        collapseFigure.setCollapsed(subjobContainer.isCollapsed());

        titleFigure.setText("<b> " + title + "</b>"); //$NON-NLS-1$ //$NON-NLS-2$
        Dimension preferedSize = titleFigure.getPreferredSize();
        preferedSize = preferedSize.getExpanded(0, 2);
        titleFigure.setSize(rectangle.width - preferedSize.height, preferedSize.height);
        titleFigure.setLocation(new Point(location.x + collapseFigure.getPreferredSize().width + 4, location.y));
        titleFigure.setVisible(showTitle);

        outlineFigure.setLocation(location);
        outlineFigure.setVisible(showTitle);
        outlineFigure.setBackgroundColor(new Color(Display.getDefault(), subjobTitleColor));// //////////////////////
        outlineFigure.setForegroundColor(new Color(Display.getDefault(), subjobTitleColor));
        outlineFigure.setSize(rectangle.width, preferedSize.height);

        // collapseFigure.setLocation(new Point(rectangle.width - preferedSize.height + location.x, location.y));
        collapseFigure.setLocation(new Point(location.x, location.y));
        collapseFigure.setSize(preferedSize.height, preferedSize.height);
        // collapseFigure.setBackgroundColor(new Color(null, 50, 50, 250));

        rectFig.setLocation(new Point(location.x, /* preferedSize.height + */location.y));
        rectFig.setSize(new Dimension(rectangle.width, rectangle.height /*- preferedSize.height*/));
        rectFig.setBackgroundColor(new Color(Display.getDefault(), mainColor));// //////////////////////////
        rectFig.setForegroundColor(new Color(Display.getDefault(), subjobTitleColor));
    }

    public void disposeColors() {
        if (rectFig.getForegroundColor() != null && !rectFig.getForegroundColor().isDisposed()) {
            rectFig.getForegroundColor().dispose();
        }
        if (rectFig.getBackgroundColor() != null && !rectFig.getBackgroundColor().isDisposed()) {
            rectFig.getBackgroundColor().dispose();
        }
        if (outlineFigure.getForegroundColor() != null && !outlineFigure.getForegroundColor().isDisposed()) {
            outlineFigure.getForegroundColor().dispose();
        }
        if (outlineFigure.getBackgroundColor() != null && !outlineFigure.getBackgroundColor().isDisposed()) {
            outlineFigure.getBackgroundColor().dispose();
        }
    }

    /**
     * yzhang Comment method "updateSubJobTitleColor".
     */
    public void updateSubJobTitleColor() {
        String rgb = (String) subjobContainer.getPropertyValue(EParameterName.SUBJOB_TITLE_COLOR.getName());
        if (rgb != null && !"".equals(rgb)) { //$NON-NLS-1$
            subjobTitleColor = ColorUtils.parseStringToRGB(rgb);
        } else {
            initSubJobTitleColor();
        }
        updateData();
    }

    /**
     * yzhang Comment method "updateData".
     */
    public void updateData() {

        showTitle = (Boolean) subjobContainer.getPropertyValue(EParameterName.SHOW_SUBJOB_TITLE.getName());

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

        mainColor = ColorUtils.parseStringToRGB(propertyValue, DesignerColorUtils.SUBJOB_COLOR);

        this.getChildren().remove(outlineFigure);
        this.getChildren().remove(rectFig);
        outlineFigure.getChildren().clear();
        rectFig.getChildren().clear();

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

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Figure#setBounds(org.eclipse.draw2d.geometry.Rectangle)
     */
    @Override
    public void setBounds(Rectangle rect) {
        super.setBounds(rect);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Figure#setLocation(org.eclipse.draw2d.geometry.Point)
     */
    @Override
    public void setLocation(Point p) {
        // TODO Auto-generated method stub
        super.setLocation(p);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.Figure#setSize(int, int)
     */
    @Override
    public void setSize(int w, int h) {
        // TODO Auto-generated method stub
        super.setSize(w, h);
    }

    public SubjobContainer getSubjobContainer() {
        return this.subjobContainer;
    }
}
