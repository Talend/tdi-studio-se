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
package org.talend.designer.core.ui.editor.palette;

import java.lang.reflect.Field;

import org.eclipse.draw2d.Animation;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.Toggle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.internal.ui.palette.editparts.ColumnsLayout;
import org.eclipse.gef.internal.ui.palette.editparts.DrawerFigure;
import org.eclipse.gef.internal.ui.palette.editparts.PaletteContainerFlowLayout;
import org.eclipse.gef.internal.ui.palette.editparts.PinFigure;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.themes.core.elements.stylesettings.CommonCSSStyleSetting;
import org.talend.themes.core.elements.stylesettings.TalendPaletteCSSStyleSetting;

/**
 *
 */
public class TalendDrawerFigure extends DrawerFigure {

    protected Control control;

    protected Toggle collapseToggle;

    protected ScrollPane scrollpane;

    protected Label talendDrawerLabel, tipLabel;

    protected boolean skipNextEvent;

    protected TalendEditPartTipHelper tipHelper;

    protected PinFigure talendPinFigure;

    protected TalendPaletteCSSStyleSetting cssStyleSetting;

    protected CustomizedCSSStyleSetting customizedCSSStyleSetting;

    protected Figure title;

    protected int childLevel;

    protected int talendLayoutMode = -1;

    public TalendDrawerFigure(Control control, int childLevel, TalendPaletteCSSStyleSetting cssStyleSetting) {
        super(null);// must be null

        this.control = control;
        this.childLevel = childLevel;

        this.cssStyleSetting = cssStyleSetting;
        this.customizedCSSStyleSetting = new CustomizedCSSStyleSetting();

        applyChange(cssStyleSetting);

        try {
            Field collapseToggleField = DrawerFigure.class.getDeclaredField("collapseToggle");
            collapseToggleField.setAccessible(true);
            Object oldCollapseToggle = collapseToggleField.get(this);
            remove((IFigure) oldCollapseToggle);

            title = new Figure();
            title.setBorder(customizedCSSStyleSetting.getTitleBorder());
            BorderLayout borderLayout = new BorderLayout();
            borderLayout.setHorizontalSpacing(10 * (childLevel + 1));
            title.setLayoutManager(borderLayout);

            Field pinFigureField = DrawerFigure.class.getDeclaredField("pinFigure");
            pinFigureField.setAccessible(true);
            talendPinFigure = new PinFigure();
            pinFigureField.set(this, talendPinFigure);
            Field drawerLabelField = DrawerFigure.class.getDeclaredField("drawerLabel");
            drawerLabelField.setAccessible(true);
            talendDrawerLabel = new Label();
            talendDrawerLabel.setFont(JFaceResources.getFontRegistry().getBold("TalendDrawerFigureSymbolicName"));
            talendDrawerLabel.setLabelAlignment(Label.LEFT);
            drawerLabelField.set(this, talendDrawerLabel);

            title.add(talendPinFigure, BorderLayout.RIGHT);
            title.add(talendDrawerLabel, BorderLayout.CENTER);
            collapseToggle = new CollapseToggle(title);
            collapseToggleField.set(this, collapseToggle);
            add(collapseToggle);

            createHoverHelp(control);

            Field scrollpaneField = DrawerFigure.class.getDeclaredField("scrollpane");
            scrollpaneField.setAccessible(true);
            scrollpane = (ScrollPane) scrollpaneField.get(this);
        } catch (Exception e) {
            CommonExceptionHandler.process(e);
        }

        // getContentPane().setBackgroundColor(backgroundColor);
    }

    protected void createHoverHelp(final Control control) {
        if (control == null) {
            return;
        }
        // If a control was provided, create the tipLabel -- if the text in the
        // header is
        // truncated, it will display it as a tooltip.
        tipLabel = new Label() {

            /**
             * @see org.eclipse.draw2d.Figure#getToolTip()
             */
            @Override
            public IFigure getToolTip() {
                return buildTooltip();
            }

            @Override
            protected void paintFigure(Graphics graphics) {
                Rectangle r = Rectangle.SINGLETON;
                r.setBounds(getBounds());
                graphics.pushState();
                paintToggleGradient(graphics, getBounds());
                graphics.popState();
                super.paintFigure(graphics);
            }
        };
        tipLabel.setOpaque(false);
        tipLabel.setBorder(TOOLTIP_BORDER);
        try {
            Field tipLabelField = DrawerFigure.class.getDeclaredField("tipLabel");
            tipLabelField.setAccessible(true);
            tipLabelField.set(this, tipLabel);
        } catch (Exception e) {
            CommonExceptionHandler.process(e);
        }

        collapseToggle.addMouseMotionListener(new MouseMotionListener.Stub() {

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!talendDrawerLabel.getBounds().contains(e.getLocation())) {
                    return;
                }
                if (skipNextEvent) {
                    skipNextEvent = false;
                    return;
                }
                if (talendDrawerLabel.isTextTruncated() && !TalendEditPartTipHelper.isCurrent(tipHelper)) {
                    tipLabel.setText(talendDrawerLabel.getText());
                    tipLabel.setIcon(talendDrawerLabel.getIcon());
                    tipLabel.setFont(talendDrawerLabel.getFont());
                    tipHelper = new TalendEditPartTipHelper(control);
                    Rectangle bounds = talendDrawerLabel.getBounds().getExpanded(2, 2);
                    talendDrawerLabel.translateToAbsolute(bounds);
                    org.eclipse.swt.graphics.Rectangle loc = new org.eclipse.swt.graphics.Rectangle(bounds.x, bounds.y,
                            bounds.width, bounds.height);
                    loc = Display.getCurrent().map(control, null, loc);
                    tipHelper.displayToolTipAt(tipLabel, loc.x, loc.y);
                }
            }
        });
        tipLabel.addMouseListener(new MouseListener.Stub() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.button == 1) {
                    Rectangle original = getCollapseToggle().getBounds().getCopy();
                    getCollapseToggle().requestFocus();
                    setExpanded(!isExpanded());
                    // Hide the tip if expanding the drawer causes the collapse
                    // toggle to move
                    if (!original.equals(getCollapseToggle().getBounds())) {
                        tipHelper.hide();
                    }
                } else {
                    tipHelper.hide();
                    if (e.button == 3) {
                        skipNextEvent = true;
                    }
                }
            }
        });
    }

    public void disposeColors() {
        // here must not dispose colors, since they are managed by JFaceResource
    }

    @Override
    public void setLayoutMode(int layoutMode) {
        if (this.talendLayoutMode == layoutMode) {
            return;
        }

        this.talendLayoutMode = layoutMode;

        LayoutManager manager;
        if (layoutMode == PaletteViewerPreferences.LAYOUT_COLUMNS) {
            manager = new ColumnsLayout();
            getContentPane().setBorder(customizedCSSStyleSetting.getScrollPaneBorder());
        } else if (layoutMode == PaletteViewerPreferences.LAYOUT_ICONS) {
            PaletteContainerFlowLayout fl = new PaletteContainerFlowLayout();
            fl.setMinorSpacing(0);
            fl.setMajorSpacing(0);
            manager = fl;
            getContentPane().setBorder(customizedCSSStyleSetting.getScrollPaneBorder());
        } else {
            manager = new ToolbarLayout();
            getContentPane().setBorder(customizedCSSStyleSetting.getScrollPaneListBorder());
        }
        getContentPane().setLayoutManager(manager);
    }

    @Override
    public Rectangle getBounds() {
        // return new Rectangle(bounds.x + X_OFFSET, bounds.y, bounds.width, bounds.height);
        return new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    IFigure buildTooltip() {
        return null;
    }

    private class CollapseToggle extends Toggle {

        public CollapseToggle(IFigure contents) {
            super(contents);
            setSelected(true);
            setRequestFocusEnabled(true);
            addChangeListener(new ChangeListener() {

                @Override
                public void handleStateChanged(ChangeEvent e) {
                    if (e.getPropertyName().equals(ButtonModel.SELECTED_PROPERTY)) {
                        Animation.markBegin();
                        handleExpandStateChanged();
                        Animation.run(150);
                    } else if (e.getPropertyName().equals(ButtonModel.MOUSEOVER_PROPERTY)) {
                        repaint();
                    }
                }
            });
        }

        @Override
        public IFigure getToolTip() {
            return buildTooltip();
        }

        @Override
        protected void paintBorder(Graphics graphics) {
            // needn't to paint the focus border
        }

        @Override
        protected void paintFigure(Graphics g) {
            applyChange(null);
            super.paintFigure(g);
            Rectangle r = Rectangle.SINGLETON;
            r.setBounds(getBounds());

            Color color = null;

            // draw top border of drawer figure
            // g.setForegroundColor(PaletteColorUtil.WIDGET_NORMAL_SHADOW);
            color = customizedCSSStyleSetting.getCollapseTopBorderForgroundLineColor1();
            if (color != null) {
                g.setForegroundColor(color);
                g.drawLine(r.getTopLeft(), r.getTopRight());
            }

            // g.setForegroundColor(ColorConstants.white);
            color = customizedCSSStyleSetting.getCollapseTopBorderForgroundLineColor2();
            if (color != null) {
                g.setForegroundColor(color);
                g.drawLine(r.getTopLeft().getTranslated(0, 1), r.getTopRight().getTranslated(0, 1));
                // r.crop(new Insets(2, 0, 0, 0));
                r.crop(new Insets(1, 0, 0, 0));
            }
            if (isExpanded()) {
                // g.setForegroundColor(PaletteColorUtil.WIDGET_BACKGROUND_NORMAL_SHADOW_65);
                color = customizedCSSStyleSetting.getCollapseExpandedLineForgroundColor();
                if (color != null) {
                    g.setForegroundColor(color);
                    g.drawLine(r.getLocation(), r.getTopRight());
                    // r.crop(new Insets(1, 0, 0, 0));
                    r.crop(new Insets(1, 0, 0, 0));
                }
            }

            // draw bottom border of drawer figure
            if (!isExpanded()) {
                // g.setForegroundColor(ColorConstants.white);
                color = customizedCSSStyleSetting.getCollapseNotExpandedLineForgroundColor();
                if (color != null) {
                    g.setForegroundColor(color);
                    g.drawLine(r.getBottomLeft().getTranslated(0, -1), r.getBottomRight().getTranslated(0, -1));
                    // r.crop(new Insets(0, 0, 1, 0));
                    r.crop(new Insets(0, 0, 1, 0));
                }
            }

            paintToggleGradient(g, r);

        }
    }

    private void paintToggleGradient(Graphics g, Rectangle rect) {
        applyChange(null);
        if (isExpanded()) {
            // g.setBackgroundColor(PaletteColorUtil.WIDGET_BACKGROUND_LIST_BACKGROUND_85);
            g.setBackgroundColor(customizedCSSStyleSetting.getExpandedBackgroundColor());
            g.fillRectangle(rect);
        } else if (collapseToggle.getModel().isMouseOver()) {
            // Color color1 = PaletteColorUtil.WIDGET_BACKGROUND_LIST_BACKGROUND_60;
            // Color color2 = PaletteColorUtil.WIDGET_BACKGROUND_NORMAL_SHADOW_90;
            // Color color3 = PaletteColorUtil.WIDGET_BACKGROUND_NORMAL_SHADOW_95;
            // Color color4 = PaletteColorUtil.WIDGET_BACKGROUND_LIST_BACKGROUND_90;

            g.setForegroundColor(customizedCSSStyleSetting.getMouseOverForgroundColor1());
            g.setBackgroundColor(customizedCSSStyleSetting.getMouseOverBackgroundColor1());
            g.fillGradient(rect.x, rect.y, rect.width, rect.height - 4, true);

            g.setForegroundColor(customizedCSSStyleSetting.getMouseOverForgroundColor2());
            g.setBackgroundColor(customizedCSSStyleSetting.getMouseOverBackgroundColor2());
            g.fillGradient(rect.x, rect.bottom() - 4, rect.width, 2, true);

            g.setForegroundColor(customizedCSSStyleSetting.getMouseOverForgroundColor3());
            g.setBackgroundColor(customizedCSSStyleSetting.getMouseOverBackgroundColor3());
            g.fillGradient(rect.x, rect.bottom() - 2, rect.width, 2, true);
        } else {
            // g.setForegroundColor(PaletteColorUtil.WIDGET_BACKGROUND_LIST_BACKGROUND_85);
            // g.setBackgroundColor(PaletteColorUtil.WIDGET_BACKGROUND_NORMAL_SHADOW_45);
            g.setForegroundColor(customizedCSSStyleSetting.getCollapsedForgroundColor());
            g.setBackgroundColor(customizedCSSStyleSetting.getCollapsedBackgroundColor());
            g.fillGradient(rect, true);
        }
    }

    public void applyChange(CommonCSSStyleSetting setting) {
        // this.X_OFFSET = cssStyleSetting.getxOffset();
        // this.COLOR_INCREMENT = cssStyleSetting.getColorIncrement();
        // title.setBorder(getTitleBorder());
        // this.cssStyleSetting.setTitleMarginBorderInstance(TITLE_MARGIN_BORDER);

        if (cssStyleSetting.getTimeStamp().compareTo(customizedCSSStyleSetting.getTimeStamp()) == 0) {
            return;
        }

        customizedCSSStyleSetting.setTimeStamp(cssStyleSetting.getTimeStamp());

        customizedCSSStyleSetting.setColorIncrement(cssStyleSetting.getColorIncrement());
        customizedCSSStyleSetting.setXOffset(cssStyleSetting.getxOffset());
        // getIncrement must be called after setColorIncrement
        int increment = getIncrement();

        customizedCSSStyleSetting.setTitleBorder(getTitleBorder());

        customizedCSSStyleSetting.setScrollPaneBorder(cssStyleSetting.getScrollPaneBorder());
        customizedCSSStyleSetting.setScrollPaneListBorder(cssStyleSetting.getScrollPaneListBorder());

        customizedCSSStyleSetting.setBaseColor(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getDrawerFigureBaseColor(), increment));
        customizedCSSStyleSetting.setTitleMarginBorder(cssStyleSetting.getTitleMarginBorder());
        customizedCSSStyleSetting.setCollapseTopBorderForgroundLineColor1(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getCollapseTopBorderForgroundLineColor1(), increment));
        customizedCSSStyleSetting.setCollapseTopBorderForgroundLineColor2(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getCollapseTopBorderForgroundLineColor2(), increment));
        customizedCSSStyleSetting.setCollapseExpandedLineForgroundColor(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getCollapseExpandedLineForgroundColor(), increment));
        customizedCSSStyleSetting.setCollapseNotExpandedLineForgroundColor(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getCollapseNotExpandedLineForgroundColor(), increment));
        customizedCSSStyleSetting.setExpandedBackgroundColor(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getExpandedBackgroundColor(), increment));
        customizedCSSStyleSetting.setMouseOverForgroundColor1(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getMouseOverForgroundColor1(), increment));
        customizedCSSStyleSetting.setMouseOverForgroundColor2(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getMouseOverForgroundColor2(), increment));
        customizedCSSStyleSetting.setMouseOverForgroundColor3(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getMouseOverForgroundColor3(), increment));
        customizedCSSStyleSetting.setMouseOverBackgroundColor1(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getMouseOverBackgroundColor1(), increment));
        customizedCSSStyleSetting.setMouseOverBackgroundColor2(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getMouseOverBackgroundColor2(), increment));
        customizedCSSStyleSetting.setMouseOverBackgroundColor3(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getMouseOverBackgroundColor3(), increment));
        customizedCSSStyleSetting.setCollapsedForgroundColor(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getCollapsedForgroundColor(), increment));
        customizedCSSStyleSetting.setCollapsedBackgroundColor(TalendPaletteCSSStyleSetting.getSubColor(
                cssStyleSetting.getCollapsedBackgroundColor(), increment));

        if (scrollpane != null) {
            Color backgroundColor = TalendPaletteCSSStyleSetting.getSubColor(customizedCSSStyleSetting.getBaseColor(), increment);
            scrollpane.getContents().setBackgroundColor(backgroundColor);
        }
    }

    protected int getIncrement() {
        return childLevel * customizedCSSStyleSetting.getColorIncrement();
    }

    protected Border getTitleBorder() {
        Insets insets = cssStyleSetting.getTitleMarginBorder().getInsets(null);
        Border talendTitleBorder = new MarginBorder(insets.top,
                insets.left + customizedCSSStyleSetting.getXOffset() * childLevel, insets.bottom, insets.right);
        return talendTitleBorder;
    }

    public class CustomizedCSSStyleSetting extends CommonCSSStyleSetting {

        protected Border titleBorder;

        protected Color baseColor;

        protected int colorIncrement;

        protected int xOffset;

        protected Border titleMarginBorder;

        protected Color collapseTopBorderForgroundLineColor1;

        protected Color collapseTopBorderForgroundLineColor2;

        protected Color collapseExpandedLineForgroundColor;

        protected Color collapseNotExpandedLineForgroundColor;

        protected Color expandedBackgroundColor;

        protected Color mouseOverForgroundColor1;

        protected Color mouseOverForgroundColor2;

        protected Color mouseOverForgroundColor3;

        protected Color mouseOverBackgroundColor1;

        protected Color mouseOverBackgroundColor2;

        protected Color mouseOverBackgroundColor3;

        protected Color collapsedForgroundColor;

        protected Color collapsedBackgroundColor;

        protected Border scrollPaneBorder;

        protected Border scrollPaneListBorder;

        /**
         * Getter for titleBorder.
         *
         * @return the titleBorder
         */
        public Border getTitleBorder() {
            return this.titleBorder;
        }

        /**
         * Sets the titleBorder.
         *
         * @param titleBorder the titleBorder to set
         */
        public void setTitleBorder(Border titleBorder) {
            if (this.titleBorder == null) {
                this.titleBorder = new MarginBorder(titleBorder.getInsets(null));
            } else {
                copyBorderSetting(titleBorder, this.titleBorder);
            }
        }

        /**
         * Getter for baseColor.
         *
         * @return the baseColor
         */
        public Color getBaseColor() {
            return this.baseColor;
        }

        /**
         * Sets the baseColor.
         *
         * @param baseColor the baseColor to set
         */
        public void setBaseColor(Color baseColor) {
            this.baseColor = baseColor;
        }

        /**
         * Getter for cOLOR_INCREMENT.
         *
         * @return the cOLOR_INCREMENT
         */
        public int getColorIncrement() {
            return this.colorIncrement;
        }

        /**
         * Sets the cOLOR_INCREMENT.
         *
         * @param cOLOR_INCREMENT the cOLOR_INCREMENT to set
         */
        public void setColorIncrement(int colorIncrement) {
            this.colorIncrement = colorIncrement;
        }

        /**
         * Getter for x_OFFSET.
         *
         * @return the x_OFFSET
         */
        public int getXOffset() {
            return this.xOffset;
        }

        /**
         * Sets the x_OFFSET.
         *
         * @param x_OFFSET the x_OFFSET to set
         */
        public void setXOffset(int xOffset) {
            this.xOffset = xOffset;
        }

        /**
         * Getter for titleMarginBorder.
         *
         * @return the titleMarginBorder
         */
        public Border getTitleMarginBorder() {
            return this.titleMarginBorder;
        }

        /**
         * Sets the titleMarginBorder.
         *
         * @param titleMarginBorder the titleMarginBorder to set
         */
        public void setTitleMarginBorder(Border titleMarginBorder) {
            if (this.titleMarginBorder == null) {
                this.titleMarginBorder = new MarginBorder(titleMarginBorder.getInsets(null));
            } else {
                copyBorderSetting(titleMarginBorder, this.titleMarginBorder);
            }
        }

        /**
         * Getter for collapseTopBorderForgroundLineColor1.
         *
         * @return the collapseTopBorderForgroundLineColor1
         */
        public Color getCollapseTopBorderForgroundLineColor1() {
            return this.collapseTopBorderForgroundLineColor1;
        }

        /**
         * Sets the collapseTopBorderForgroundLineColor1.
         *
         * @param collapseTopBorderForgroundLineColor1 the collapseTopBorderForgroundLineColor1 to set
         */
        public void setCollapseTopBorderForgroundLineColor1(Color collapseTopBorderForgroundLineColor1) {
            this.collapseTopBorderForgroundLineColor1 = collapseTopBorderForgroundLineColor1;
        }

        /**
         * Getter for collapseTopBorderForgroundLineColor2.
         *
         * @return the collapseTopBorderForgroundLineColor2
         */
        public Color getCollapseTopBorderForgroundLineColor2() {
            return this.collapseTopBorderForgroundLineColor2;
        }

        /**
         * Sets the collapseTopBorderForgroundLineColor2.
         *
         * @param collapseTopBorderForgroundLineColor2 the collapseTopBorderForgroundLineColor2 to set
         */
        public void setCollapseTopBorderForgroundLineColor2(Color collapseTopBorderForgroundLineColor2) {
            this.collapseTopBorderForgroundLineColor2 = collapseTopBorderForgroundLineColor2;
        }

        /**
         * Getter for collapseExpandedLineForgroundColor.
         *
         * @return the collapseExpandedLineForgroundColor
         */
        public Color getCollapseExpandedLineForgroundColor() {
            return this.collapseExpandedLineForgroundColor;
        }

        /**
         * Sets the collapseExpandedLineForgroundColor.
         *
         * @param collapseExpandedLineForgroundColor the collapseExpandedLineForgroundColor to set
         */
        public void setCollapseExpandedLineForgroundColor(Color collapseExpandedLineForgroundColor) {
            this.collapseExpandedLineForgroundColor = collapseExpandedLineForgroundColor;
        }

        /**
         * Getter for collapseNotExpandedLineForgroundColor.
         *
         * @return the collapseNotExpandedLineForgroundColor
         */
        public Color getCollapseNotExpandedLineForgroundColor() {
            return this.collapseNotExpandedLineForgroundColor;
        }

        /**
         * Sets the collapseNotExpandedLineForgroundColor.
         *
         * @param collapseNotExpandedLineForgroundColor the collapseNotExpandedLineForgroundColor to set
         */
        public void setCollapseNotExpandedLineForgroundColor(Color collapseNotExpandedLineForgroundColor) {
            this.collapseNotExpandedLineForgroundColor = collapseNotExpandedLineForgroundColor;
        }

        /**
         * Getter for expandedBackgroundColor.
         *
         * @return the expandedBackgroundColor
         */
        public Color getExpandedBackgroundColor() {
            return this.expandedBackgroundColor;
        }

        /**
         * Sets the expandedBackgroundColor.
         *
         * @param expandedBackgroundColor the expandedBackgroundColor to set
         */
        public void setExpandedBackgroundColor(Color expandedBackgroundColor) {
            this.expandedBackgroundColor = expandedBackgroundColor;
        }

        /**
         * Getter for mouseOverForgroundColor1.
         *
         * @return the mouseOverForgroundColor1
         */
        public Color getMouseOverForgroundColor1() {
            return this.mouseOverForgroundColor1;
        }

        /**
         * Sets the mouseOverForgroundColor1.
         *
         * @param mouseOverForgroundColor1 the mouseOverForgroundColor1 to set
         */
        public void setMouseOverForgroundColor1(Color mouseOverForgroundColor1) {
            this.mouseOverForgroundColor1 = mouseOverForgroundColor1;
        }

        /**
         * Getter for mouseOverForgroundColor2.
         *
         * @return the mouseOverForgroundColor2
         */
        public Color getMouseOverForgroundColor2() {
            return this.mouseOverForgroundColor2;
        }

        /**
         * Sets the mouseOverForgroundColor2.
         *
         * @param mouseOverForgroundColor2 the mouseOverForgroundColor2 to set
         */
        public void setMouseOverForgroundColor2(Color mouseOverForgroundColor2) {
            this.mouseOverForgroundColor2 = mouseOverForgroundColor2;
        }

        /**
         * Getter for mouseOverForgroundColor3.
         *
         * @return the mouseOverForgroundColor3
         */
        public Color getMouseOverForgroundColor3() {
            return this.mouseOverForgroundColor3;
        }

        /**
         * Sets the mouseOverForgroundColor3.
         *
         * @param mouseOverForgroundColor3 the mouseOverForgroundColor3 to set
         */
        public void setMouseOverForgroundColor3(Color mouseOverForgroundColor3) {
            this.mouseOverForgroundColor3 = mouseOverForgroundColor3;
        }

        /**
         * Getter for mouseOverBackgroundColor1.
         *
         * @return the mouseOverBackgroundColor1
         */
        public Color getMouseOverBackgroundColor1() {
            return this.mouseOverBackgroundColor1;
        }

        /**
         * Sets the mouseOverBackgroundColor1.
         *
         * @param mouseOverBackgroundColor1 the mouseOverBackgroundColor1 to set
         */
        public void setMouseOverBackgroundColor1(Color mouseOverBackgroundColor1) {
            this.mouseOverBackgroundColor1 = mouseOverBackgroundColor1;
        }

        /**
         * Getter for mouseOverBackgroundColor2.
         *
         * @return the mouseOverBackgroundColor2
         */
        public Color getMouseOverBackgroundColor2() {
            return this.mouseOverBackgroundColor2;
        }

        /**
         * Sets the mouseOverBackgroundColor2.
         *
         * @param mouseOverBackgroundColor2 the mouseOverBackgroundColor2 to set
         */
        public void setMouseOverBackgroundColor2(Color mouseOverBackgroundColor2) {
            this.mouseOverBackgroundColor2 = mouseOverBackgroundColor2;
        }

        /**
         * Getter for mouseOverBackgroundColor3.
         *
         * @return the mouseOverBackgroundColor3
         */
        public Color getMouseOverBackgroundColor3() {
            return this.mouseOverBackgroundColor3;
        }

        /**
         * Sets the mouseOverBackgroundColor3.
         *
         * @param mouseOverBackgroundColor3 the mouseOverBackgroundColor3 to set
         */
        public void setMouseOverBackgroundColor3(Color mouseOverBackgroundColor3) {
            this.mouseOverBackgroundColor3 = mouseOverBackgroundColor3;
        }

        /**
         * Getter for collapsedForgroundColor.
         *
         * @return the collapsedForgroundColor
         */
        public Color getCollapsedForgroundColor() {
            return this.collapsedForgroundColor;
        }

        /**
         * Sets the collapsedForgroundColor.
         *
         * @param collapsedForgroundColor the collapsedForgroundColor to set
         */
        public void setCollapsedForgroundColor(Color collapsedForgroundColor) {
            this.collapsedForgroundColor = collapsedForgroundColor;
        }

        /**
         * Getter for collapsedBackgroundColor.
         *
         * @return the collapsedBackgroundColor
         */
        public Color getCollapsedBackgroundColor() {
            return this.collapsedBackgroundColor;
        }

        /**
         * Sets the collapsedBackgroundColor.
         *
         * @param collapsedBackgroundColor the collapsedBackgroundColor to set
         */
        public void setCollapsedBackgroundColor(Color collapsedBackgroundColor) {
            this.collapsedBackgroundColor = collapsedBackgroundColor;
        }

        /**
         * Getter for scrollPaneBorder.
         *
         * @return the scrollPaneBorder
         */
        public Border getScrollPaneBorder() {
            return this.scrollPaneBorder;
        }

        /**
         * Sets the scrollPaneBorder.
         *
         * @param scrollPaneBorder the scrollPaneBorder to set
         */
        public void setScrollPaneBorder(Border scrollPaneBorder) {
            if (this.scrollPaneBorder == null) {
                this.scrollPaneBorder = new MarginBorder(scrollPaneBorder.getInsets(null));
            } else {
                copyBorderSetting(scrollPaneBorder, this.scrollPaneBorder);
            }
        }

        /**
         * Getter for scrollPaneListBorder.
         *
         * @return the scrollPaneListBorder
         */
        public Border getScrollPaneListBorder() {
            return this.scrollPaneListBorder;
        }

        /**
         * Sets the scrollPaneListBorder.
         *
         * @param scrollPaneListBorder the scrollPaneListBorder to set
         */
        public void setScrollPaneListBorder(Border scrollPaneListBorder) {
            if (this.scrollPaneListBorder == null) {
                this.scrollPaneListBorder = new MarginBorder(scrollPaneListBorder.getInsets(null));
            } else {
                copyBorderSetting(scrollPaneListBorder, this.scrollPaneListBorder);
            }
        }

    }
}
