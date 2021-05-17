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
package org.talend.designer.gefabstractmap.part;

import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.MouseWheelHelper;
import org.eclipse.swt.widgets.Event;
import org.talend.designer.gefabstractmap.figures.layout.XmlMapDataLayout;
import org.talend.designer.gefabstractmap.figures.layout.ZoneContentLayout;
import org.talend.designer.gefabstractmap.figures.manager.RootModelManager;
import org.talend.designer.gefabstractmap.figures.sash.SashSeparator;
import org.talend.designer.gefabstractmap.figures.treetools.zone.InputZoneToolBar;
import org.talend.designer.gefabstractmap.figures.treetools.zone.OutputZoneToolBar;
import org.talend.designer.gefabstractmap.figures.treetools.zone.SearchZoneToolBar;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;

/**
 * created by Administrator on 2013-1-14 Detailled comment
 *
 */
public abstract class MapperRootEditPart extends BaseEditPart implements MouseWheelHelper {

    private IFigure leftFigure;

    private IFigure centerFigure;

    private IFigure rightFigure;

    private ScrollPane inputScroll;

    private ScrollPane varScroll;

    private ScrollPane outputScroll;

    private OutputZoneToolBar outputToolBar;

    private InputZoneToolBar inputToolBar;

    private SearchZoneToolBar searchToolBar;

    private RootModelManager rootModelManager;

    private static final int SCROLL_OFFSET = 30;

    @Override
    protected IFigure createFigure() {
        Figure mainFigure = new Figure();
        XmlMapDataLayout manager2 = new XmlMapDataLayout(getRootModelManager().getGraphicalViewer());
        mainFigure.setLayoutManager(manager2);

        // input
        Figure inputZone = new Figure();
        inputZone.setLayoutManager(new ZoneLayout());

        inputToolBar = createInputZoneToolBar();
        inputToolBar.setBorder(new LineBorder(ColorProviderMapper.getColor(ColorInfo.COLOR_TREE_BORDER)));
        inputZone.add(inputToolBar);

        inputScroll = new ScrollPane();
        inputScroll.setHorizontalScrollBarVisibility(ScrollPane.NEVER);
        leftFigure = new Figure();
        // ToolbarLayout subManager = new ToolbarLayout();
        ToolbarLayout subManager = createZoneContentLayout();
        subManager.setSpacing(20);
        subManager.setVertical(true);
        leftFigure.setLayoutManager(subManager);
        leftFigure.setBorder(new MarginBorder(20, 40, 20, 40));
        inputScroll.getViewport().setContents(leftFigure);
        inputScroll.getViewport().setContentsTracksWidth(true);

        inputZone.add(inputScroll);
        mainFigure.add(inputZone);

        // separator 1
        SashSeparator separatorLeft = new SashSeparator();
        separatorLeft.setImage(ImageProviderMapper.getImage(ImageInfo.ZONE_SASH));
        mainFigure.add(separatorLeft);

        // search , var
        searchToolBar = createSearchZoneToolBar();
        searchToolBar.setBorder(new LineBorder(ColorProviderMapper.getColor(ColorInfo.COLOR_TREE_BORDER)));

        varScroll = new ScrollPane();
        varScroll.setHorizontalScrollBarVisibility(ScrollPane.NEVER);
        centerFigure = new Figure();

        subManager = createZoneContentLayout();
        subManager.setSpacing(20);
        subManager.setVertical(true);
        centerFigure.setLayoutManager(subManager);
        centerFigure.setBorder(new MarginBorder(10, 40, 10, 40));
        varScroll.getViewport().setContents(centerFigure);
        varScroll.getViewport().setContentsTracksWidth(true);

        centerFigure.add(searchToolBar);
        mainFigure.add(varScroll);

        // separetor 2
        SashSeparator separatorRight = new SashSeparator();
        separatorRight.setImage(ImageProviderMapper.getImage(ImageInfo.ZONE_SASH));
        mainFigure.add(separatorRight);

        // output
        Figure outputZone = new Figure();
        outputZone.setLayoutManager(new ZoneLayout());

        outputToolBar = createOutputZoneToolBar();
        outputToolBar.setBorder(new LineBorder(ColorProviderMapper.getColor(ColorInfo.COLOR_TREE_BORDER)));
        outputZone.add(outputToolBar);
        outputScroll = new ScrollPane();
        outputScroll.setHorizontalScrollBarVisibility(ScrollPane.NEVER);
        rightFigure = new Figure();

        subManager = createZoneContentLayout();
        subManager.setSpacing(20);
        subManager.setVertical(true);
        rightFigure.setLayoutManager(subManager);
        rightFigure.setBorder(new MarginBorder(20, 40, 20, 40));
        outputScroll.getViewport().setContents(rightFigure);
        outputScroll.getViewport().setContentsTracksWidth(true);
        outputZone.add(outputScroll);
        mainFigure.add(outputZone);

        mainFigure.setOpaque(true);
        mainFigure.setBackgroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_BACKGROUND_LINKS_ZONE));

        separatorLeft.setLeftFigure(inputZone);
        separatorLeft.setRightFigure(varScroll);
        separatorLeft.setParentFigure(mainFigure);

        separatorRight.setLeftFigure(varScroll);
        separatorRight.setRightFigure(outputZone);
        separatorRight.setParentFigure(mainFigure);

        return mainFigure;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.MouseWheelHelper#handleMouseWheelScrolled(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void handleMouseWheelScrolled(Event event) {
        ScrollPane scroolPane = getScroolPane(event.x, event.y);
        if (scroolPane != null) {
            scroolPane.scrollVerticalTo(scroolPane.getViewport().getVerticalRangeModel().getValue()
                    + (-event.count * SCROLL_OFFSET));
        }
    }

    private ScrollPane getScroolPane(int x, int y) {
        if (inputScroll.containsPoint(x, y)) {
            return inputScroll;
        } else if (varScroll.containsPoint(x, y)) {
            return varScroll;
        } else if (outputScroll.containsPoint(x, y)) {
            return outputScroll;
        }
        return null;
    }

    protected abstract ZoneContentLayout createZoneContentLayout();

    @Override
    protected void addChildVisual(EditPart childEditPart, int index) {
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();

        if (childEditPart instanceof InputTablePart) {
            /* get first figure to put all input tables figures in */
            Object model = childEditPart.getModel();
            index = getRootModelManager().getInputTables().indexOf(model);
            if (index != -1) {
                leftFigure.add(child, index);
            } else {
                leftFigure.add(child);
            }

        }
        if (childEditPart instanceof OutputTablePart) {
            /* get third figure to put all output tables figures in */
            Object model = childEditPart.getModel();
            index = getRootModelManager().getOutputTables().indexOf(model);
            if (index != -1) {
                rightFigure.add(child, index);
            } else {
                rightFigure.add(child);
            }
        }
        if (childEditPart instanceof VarTablePart) {
            Object model = childEditPart.getModel();
            index = getRootModelManager().getVarTables().indexOf(model);
            if (index != -1) {
                // had added search figure when inited the center figure.
                if (centerFigure.getChildren() != null && centerFigure.getChildren().size() != 0) {
                    centerFigure.add(child, index + centerFigure.getChildren().size());
                } else {
                    centerFigure.add(child, index);
                }
            } else {
                centerFigure.add(child);
            }
        }

    }

    @Override
    public void notifyChanged(Notification notification) {
        int eventType = notification.getEventType();
        switch (eventType) {
        case Notification.ADD:
        case Notification.REMOVE:
        case Notification.REMOVE_MANY:
            refreshChildren();
            break;

        default:
            break;
        }
    }

    @Override
    protected void removeChildVisual(EditPart childEditPart) {
        IFigure child = ((GraphicalEditPart) childEditPart).getFigure();
        if (childEditPart instanceof InputTablePart) {
            leftFigure.remove(child);
        }
        if (childEditPart instanceof OutputTablePart) {
            rightFigure.remove(child);
        }
        if (childEditPart instanceof VarTablePart) {
            centerFigure.remove(child);
        }
    }

    protected abstract OutputZoneToolBar createOutputZoneToolBar();

    protected abstract InputZoneToolBar createInputZoneToolBar();

    protected abstract RootModelManager createRootModelManager();

    protected abstract SearchZoneToolBar createSearchZoneToolBar();

    public RootModelManager getRootModelManager() {
        if (rootModelManager == null) {
            rootModelManager = createRootModelManager();
        }
        return rootModelManager;
    }

    public OutputZoneToolBar getOutputZoneToolBar() {
        return this.outputToolBar;
    }

    public InputZoneToolBar getInputZoneToolBar() {
        return this.inputToolBar;
    }

    class ZoneLayout extends ToolbarLayout {

        private static final int TOOL_BAR_HEIGHT = 35;

        @Override
        public void layout(IFigure parent) {
            List children = parent.getChildren();
            org.eclipse.swt.graphics.Point avilableSize = getViewer().getControl().getSize();
            Rectangle clientArea = parent.getClientArea();
            int x = clientArea.x;
            int y = clientArea.y;
            if (children.size() == 2) {
                // layout input output zone
                Figure toolBarFigure = (Figure) children.get(0);
                Rectangle newBounds = new Rectangle(x, y, clientArea.width, TOOL_BAR_HEIGHT);
                toolBarFigure.setBounds(newBounds);

                Figure scroll = (Figure) children.get(1);
                Rectangle bounds = new Rectangle(x, y + TOOL_BAR_HEIGHT, clientArea.width, avilableSize.y - TOOL_BAR_HEIGHT);
                scroll.setBounds(bounds);
            } else if (children.size() == 1) {
                // layout var zone
                Rectangle vBounds = null;
                if (parent.getParent() instanceof Viewport) {
                    vBounds = ((Viewport) parent.getParent()).getBounds();
                }
                Figure zoneFigure = (Figure) children.get(0);
                final Insets insets = parent.getBorder().getInsets(zoneFigure);

                int width = vBounds == null ? clientArea.width : (vBounds.width - insets.left - insets.right);
                final Dimension prefSize = zoneFigure.getPreferredSize(clientArea.width, -1);
                Rectangle newBounds = new Rectangle(x, y, width, prefSize.height);
                zoneFigure.setBounds(newBounds);
            }

        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.draw2d.AbstractHintLayout#invalidate()
         */
        @Override
        public void invalidate() {
            super.invalidate();
        }

        @Override
        protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
            // TODO Auto-generated method stub
            return super.calculatePreferredSize(container, wHint, hHint);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.draw2d.ToolbarLayout#calculateMinimumSize(org.eclipse.draw2d.IFigure, int, int)
         */
        @Override
        protected Dimension calculateMinimumSize(IFigure container, int wHint, int hHint) {
            // TODO Auto-generated method stub
            return super.calculateMinimumSize(container, wHint, hHint);
        }
    }

    public ScrollPane getInputScroll() {
        return this.inputScroll;
    }

    public void setInputScroll(ScrollPane inputScroll) {
        this.inputScroll = inputScroll;
    }

    public ScrollPane getVarScroll() {
        return this.varScroll;
    }

    public void setVarScroll(ScrollPane varScroll) {
        this.varScroll = varScroll;
    }

    public ScrollPane getOutputScroll() {
        return this.outputScroll;
    }

    public void setOutputScroll(ScrollPane outputScroll) {
        this.outputScroll = outputScroll;
    }

}
