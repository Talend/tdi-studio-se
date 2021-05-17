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
import java.lang.reflect.Method;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ButtonBorder;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Toggle;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.internal.ui.palette.editparts.DetailedLabelFigure;
import org.eclipse.gef.internal.ui.palette.editparts.ToolEntryEditPart;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.themes.core.elements.stylesettings.CommonCSSStyleSetting;
import org.talend.themes.core.elements.stylesettings.TalendPaletteCSSStyleSetting;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TalendEntryEditPart extends ToolEntryEditPart {

    protected DetailedLabelFigure talendCustomLabel;

    protected Border TOOLBAR_ITEM_BORDER = new ButtonBorder(ButtonBorder.SCHEMES.TOOLBAR);

    protected TalendPaletteCSSStyleSetting cssStyleSetting;

    protected TalendEntryCSSStyleSetting entryCSSStyleSetting;

    protected int childLevel;

    /**
     * DOC talend TalendEntryEditPart constructor comment.
     *
     * @param paletteEntry
     */
    public TalendEntryEditPart(PaletteEntry paletteEntry, TalendPaletteCSSStyleSetting cssStyleSetting) {
        super(paletteEntry);
        this.cssStyleSetting = cssStyleSetting;
        entryCSSStyleSetting = new TalendEntryCSSStyleSetting();
        childLevel = getChildLevel();
        applyChange();
    }

    @Override
    protected void setImageDescriptor(ImageDescriptor desc) {
        setImageInFigure(CoreImageProvider.getComponentImageFromDesc(desc));
    }

    @Override
    public IFigure createFigure() {
        Field customLabelField;
        try {
            talendCustomLabel = new DetailedLabelFigure();
            customLabelField = ToolEntryEditPart.class.getDeclaredField("customLabel"); //$NON-NLS-1$
            customLabelField.setAccessible(true);
            customLabelField.set(this, talendCustomLabel);
        } catch (Exception e) {
            CommonExceptionHandler.process(e);
        }
        Clickable button = new ToolEntryToggle(talendCustomLabel);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                getPaletteViewer().setActiveTool(getToolEntry());
            }
        });

        return button;
    }

    protected ToolEntry getToolEntry() {
        return (ToolEntry) getPaletteEntry();
    }

    class ToolEntryToggle extends Toggle {

        private boolean showHoverFeedback = false;

        ToolEntryToggle(IFigure contents) {
            super(contents);
            setOpaque(false);
            setEnabled(true);
            if (isToolbarItem()) {
                setStyle(Clickable.STYLE_BUTTON | Clickable.STYLE_TOGGLE);
                setBorder(TOOLBAR_ITEM_BORDER);
            }
        }

        @Override
        public boolean containsPoint(int x, int y) {
            applyChange();
            Rectangle rect = getBounds().getCopy();
            if (talendCustomLabel.getBorder() == entryCSSStyleSetting.getIconBorder()) {
                rect.width -= entryCSSStyleSetting.getArrowWidth();
            } else if (talendCustomLabel.getBorder() == entryCSSStyleSetting.getListBorder()) {
                rect.width -= entryCSSStyleSetting.getArrowWidth();
                rect.x += entryCSSStyleSetting.getArrowWidth();
            }
            return rect.contains(x, y);
        }

        @Override
        public IFigure findMouseEventTargetAt(int x, int y) {
            return null;
        }

        @Override
        public IFigure getToolTip() {
            return createToolTip();
        }

        @Override
        public void setEnabled(boolean value) {
            applyChange();
            super.setEnabled(value);
            if (isEnabled()) {
                setRolloverEnabled(true);
                if (getFlag(STYLE_BUTTON)) {
                    setBorder(TOOLBAR_ITEM_BORDER);
                }
                setForegroundColor(null);
            } else {
                if (getFlag(STYLE_BUTTON)) {
                    setBorder(null);
                }
                setRolloverEnabled(false);
                setForegroundColor(entryCSSStyleSetting.getEntryEditPartToolEntryForgroundDisabledColor());
            }
        }

        @Override
        protected void paintFigure(Graphics graphics) {
            applyChange();
            super.paintFigure(graphics);

            if (!isToolbarItem() && isEnabled() && isRolloverEnabled()) {
                ButtonModel model = getModel();
                Rectangle rect = getClientArea();

                // draw top line
                graphics.setForegroundColor(entryCSSStyleSetting.getEntryEditPartToolEntryTopLineColor());
                graphics.drawLine(rect.getTopLeft(), rect.getTopRight());

                if (model.isSelected()) {
                    // graphics.setBackgroundColor(PaletteColorUtil.getSelectedColor());
                    graphics.setBackgroundColor(entryCSSStyleSetting.getEntryEditPartToolEntrySelectedBackgroundColor());
                    graphics.fillRectangle(rect);
                    // graphics.fillRoundRectangle(getSelectionRectangle(getLayoutSetting(), talendCustomLabel), 3, 3);
                    // graphics.fillRoundRectangle(getClientArea(), 3, 3);
                } else if (model.isMouseOver() || showHoverFeedback) {
                    // graphics.setBackgroundColor(PaletteColorUtil.getHoverColor());
                    graphics.setBackgroundColor(entryCSSStyleSetting.getEntryEditPartToolEntryHoverBackgroundColor());
                    graphics.fillRectangle(getClientArea());
                    // graphics.fillRoundRectangle(getSelectionRectangle(getLayoutSetting(), talendCustomLabel), 3, 3);
                    // graphics.fillRoundRectangle(getClientArea(), 3, 3);
                } else {
                    graphics.setBackgroundColor(entryCSSStyleSetting.getEntryEditPartToolEntryBackgroundColor());
                    graphics.fillRectangle(getClientArea());
                }

                // draw bottom line
                graphics.setForegroundColor(entryCSSStyleSetting.getEntryEditPartToolEntryBottomLineColor());
                graphics.drawLine(rect.getBottomLeft().getTranslated(0, -1), rect.getBottomRight().getTranslated(0, -1));
            }
        }

        @Override
        protected void paintBorder(Graphics graphics) {
            if (isEnabled()) {

                if (getBorder() != null) {
                    getBorder().paint(this, graphics, NO_INSETS);
                }
                if (hasFocus()) {
                    applyChange();
                    graphics.setForegroundColor(entryCSSStyleSetting.getEntryEditPartToolEntryBorderFocusForgroundColor());
                    graphics.setBackgroundColor(entryCSSStyleSetting.getEntryEditPartToolEntryHoverBackgroundColor());

                    // Rectangle area = isToolbarItem() ? getClientArea() : getSelectionRectangle(getLayoutSetting(),
                    // talendCustomLabel);
                    Rectangle area = getClientArea();
                    if (isStyle(STYLE_BUTTON)) {
                        graphics.drawFocus(area.x, area.y, area.width, area.height);
                    } else {
                        graphics.drawFocus(area.x, area.y, area.width - 1, area.height - 1);
                    }
                }
            } else {
                super.paintBorder(graphics);
            }
        }

        /**
         * Should hover feedback be shown? Allows other palette entities to control when the hover feedback should be
         * shown on this tool entry.
         *
         * @param showHoverFeedback true if the hover feedback is to be shown; false otherwise.
         */
        public void setShowHoverFeedback(boolean showHoverFeedback) {
            this.showHoverFeedback = showHoverFeedback;
            repaint();
        }
    }

    @Override
    protected void refreshVisuals() {
        applyChange();
        PaletteEntry entry = getPaletteEntry();

        talendCustomLabel.setName(entry.getLabel());
        talendCustomLabel.setDescription(entry.getDescription());
        if (getPreferenceSource().useLargeIcons()) {
            setImageDescriptor(entry.getLargeIcon());
        } else {
            setImageDescriptor(entry.getSmallIcon());
        }
        int layoutMode = getLayoutSetting();
        talendCustomLabel.setLayoutMode(layoutMode);
        if (layoutMode == PaletteViewerPreferences.LAYOUT_COLUMNS) {
            talendCustomLabel.setBorder(entryCSSStyleSetting.getIconBorder());
        } else if (layoutMode == PaletteViewerPreferences.LAYOUT_LIST || layoutMode == PaletteViewerPreferences.LAYOUT_DETAILS) {
            talendCustomLabel.setBorder(entryCSSStyleSetting.getListBorder());
        } else if (layoutMode == PaletteViewerPreferences.LAYOUT_ICONS && !isToolbarItem()) {
            talendCustomLabel.setBorder(entryCSSStyleSetting.getIconBorder());
        } else {
            talendCustomLabel.setBorder(null);
        }

    }

    protected Border getTabbedBorder(Border border) {
        Insets insets = border.getInsets(null);
        return new MarginBorder(insets.top, insets.left + cssStyleSetting.getxOffset() * childLevel
                - entryCSSStyleSetting.getArrowWidth(), insets.bottom, insets.right);

    }

    protected int getChildLevel() {
        PaletteEntry entry = (PaletteEntry) getModel();
        int i = 0;
        while ((entry = entry.getParent()) != null) {
            ++i;
        }
        return i;
    }

    protected int getIncrement() {
        int i = childLevel;
        if (0 < i) {
            --i;
        }
        return i * cssStyleSetting.getColorIncrement();
    }

    protected static Method func_getSelectionRectangle;

    protected static Rectangle getSelectionRectangle(int layoutMode, DetailedLabelFigure labelFigure) {
        Rectangle rect = Rectangle.SINGLETON;

        try {
            if (func_getSelectionRectangle == null) {
                func_getSelectionRectangle = ToolEntryEditPart.class.getDeclaredMethod("getSelectionRectangle", int.class,
                        DetailedLabelFigure.class);
                func_getSelectionRectangle.setAccessible(true);
            }
            rect = (Rectangle) func_getSelectionRectangle.invoke(ToolEntryEditPart.class, layoutMode, labelFigure);
        } catch (Exception e) {
            CommonExceptionHandler.process(e);
        }

        return rect;
    }

    public void applyChange() {
        if (entryCSSStyleSetting.getTimeStamp().compareTo(cssStyleSetting.getTimeStamp()) == 0) {
            return;
        }

        entryCSSStyleSetting.setTimeStamp(cssStyleSetting.getTimeStamp());

        entryCSSStyleSetting.setArrowWidth(cssStyleSetting.getEntryEditPartArrowWidth());
        entryCSSStyleSetting.setIconBorder(cssStyleSetting.getEntryEditPartIconBorder());
        entryCSSStyleSetting.setListBorder(getTabbedBorder(cssStyleSetting.getEntryEditPartListBorder()));
        entryCSSStyleSetting.setInheritFromParent(cssStyleSetting.isEntryEditPartBackgroundColorInheritFromParent());
        if (entryCSSStyleSetting.isInheritFromParent()) {
            int increment = getIncrement();
            entryCSSStyleSetting.setEntryEditPartToolEntryForgroundDisabledColor(cssStyleSetting
                    .getEntryEditPartToolEntryForgroundDisabledColor());
            entryCSSStyleSetting.setEntryEditPartToolEntrySelectedBackgroundColor(TalendPaletteCSSStyleSetting.getSubColor(
                    cssStyleSetting.getExpandedBackgroundColor(), increment));
            entryCSSStyleSetting.setEntryEditPartToolEntryHoverBackgroundColor(TalendPaletteCSSStyleSetting.getSubColor(
                    cssStyleSetting.getMouseOverBackgroundColor1(), increment));
            entryCSSStyleSetting.setEntryEditPartToolEntryBorderFocusForgroundColor(cssStyleSetting
                    .getEntryEditPartToolEntryBorderFocusForgroundColor());
            entryCSSStyleSetting.setEntryEditPartToolEntryBorderFocusBackgroundColor(cssStyleSetting
                    .getEntryEditPartToolEntryBorderFocusBackgroundColor());
            entryCSSStyleSetting.setEntryEditPartToolEntryTopLineColor(TalendPaletteCSSStyleSetting.getSubColor(
                    cssStyleSetting.getCollapseTopBorderForgroundLineColor1(), increment));
            entryCSSStyleSetting.setEntryEditPartToolEntryBottomLineColor(TalendPaletteCSSStyleSetting.getSubColor(
                    cssStyleSetting.getCollapseTopBorderForgroundLineColor1(), increment));
            entryCSSStyleSetting.setEntryEditPartToolEntryBackgroundColor(TalendPaletteCSSStyleSetting.getSubColor(
                    cssStyleSetting.getCollapsedBackgroundColor(), increment));
        } else {
            entryCSSStyleSetting.setEntryEditPartToolEntryForgroundDisabledColor(cssStyleSetting
                    .getEntryEditPartToolEntryForgroundDisabledColor());
            entryCSSStyleSetting.setEntryEditPartToolEntrySelectedBackgroundColor(cssStyleSetting
                    .getEntryEditPartToolEntrySelectedBackgroundColor());
            entryCSSStyleSetting.setEntryEditPartToolEntryHoverBackgroundColor(cssStyleSetting
                    .getEntryEditPartToolEntryHoverBackgroundColor());
            entryCSSStyleSetting.setEntryEditPartToolEntryBorderFocusForgroundColor(cssStyleSetting
                    .getEntryEditPartToolEntryBorderFocusForgroundColor());
            entryCSSStyleSetting.setEntryEditPartToolEntryBorderFocusBackgroundColor(cssStyleSetting
                    .getEntryEditPartToolEntryBorderFocusBackgroundColor());
            entryCSSStyleSetting.setEntryEditPartToolEntryTopLineColor(cssStyleSetting.getEntryEditPartToolEntryTopLineColor());
            entryCSSStyleSetting.setEntryEditPartToolEntryBottomLineColor(cssStyleSetting
                    .getEntryEditPartToolEntryBottomLineColor());
            entryCSSStyleSetting.setEntryEditPartToolEntryBackgroundColor(cssStyleSetting
                    .getEntryEditPartToolEntryBackgroundColor());
        }
    }

    public class TalendEntryCSSStyleSetting extends CommonCSSStyleSetting {

        protected Border iconBorder;

        protected Border listBorder;

        protected Color entryEditPartToolEntryForgroundDisabledColor;

        protected Color entryEditPartToolEntrySelectedBackgroundColor;

        protected Color entryEditPartToolEntryHoverBackgroundColor;

        protected Color entryEditPartToolEntryBorderFocusForgroundColor;

        protected Color entryEditPartToolEntryBorderFocusBackgroundColor;

        protected Color entryEditPartToolEntryTopLineColor;

        protected Color entryEditPartToolEntryBottomLineColor;

        protected Color entryEditPartToolEntryBackgroundColor;

        protected int arrowWidth;

        protected boolean inheritFromParent;

        /**
         * Getter for iconBorder.
         *
         * @return the iconBorder
         */
        public Border getIconBorder() {
            return this.iconBorder;
        }

        /**
         * Sets the iconBorder.
         *
         * @param iconBorder the iconBorder to set
         */
        public void setIconBorder(Border iconBorder) {
            if (this.iconBorder == null) {
                this.iconBorder = new MarginBorder(iconBorder.getInsets(null));
            } else {
                copyBorderSetting(iconBorder, this.iconBorder);
            }
        }

        /**
         * Getter for listBorder.
         *
         * @return the listBorder
         */
        public Border getListBorder() {
            return this.listBorder;
        }

        /**
         * Sets the listBorder.
         *
         * @param listBorder the listBorder to set
         */
        public void setListBorder(Border listBorder) {
            if (this.listBorder == null) {
                this.listBorder = new MarginBorder(listBorder.getInsets(null));
            } else {
                copyBorderSetting(listBorder, this.listBorder);
            }
        }

        /**
         * Getter for entryEditPartToolEntryForgroundDisabledColor.
         *
         * @return the entryEditPartToolEntryForgroundDisabledColor
         */
        public Color getEntryEditPartToolEntryForgroundDisabledColor() {
            return this.entryEditPartToolEntryForgroundDisabledColor;
        }

        /**
         * Sets the entryEditPartToolEntryForgroundDisabledColor.
         *
         * @param entryEditPartToolEntryForgroundDisabledColor the entryEditPartToolEntryForgroundDisabledColor to set
         */
        public void setEntryEditPartToolEntryForgroundDisabledColor(Color entryEditPartToolEntryForgroundDisabledColor) {
            this.entryEditPartToolEntryForgroundDisabledColor = entryEditPartToolEntryForgroundDisabledColor;
        }

        /**
         * Getter for entryEditPartToolEntrySelectedBackgroundColor.
         *
         * @return the entryEditPartToolEntrySelectedBackgroundColor
         */
        public Color getEntryEditPartToolEntrySelectedBackgroundColor() {
            return this.entryEditPartToolEntrySelectedBackgroundColor;
        }

        /**
         * Sets the entryEditPartToolEntrySelectedBackgroundColor.
         *
         * @param entryEditPartToolEntrySelectedBackgroundColor the entryEditPartToolEntrySelectedBackgroundColor to set
         */
        public void setEntryEditPartToolEntrySelectedBackgroundColor(Color entryEditPartToolEntrySelectedBackgroundColor) {
            this.entryEditPartToolEntrySelectedBackgroundColor = entryEditPartToolEntrySelectedBackgroundColor;
        }

        /**
         * Getter for entryEditPartToolEntryHoverBackgroundColor.
         *
         * @return the entryEditPartToolEntryHoverBackgroundColor
         */
        public Color getEntryEditPartToolEntryHoverBackgroundColor() {
            return this.entryEditPartToolEntryHoverBackgroundColor;
        }

        /**
         * Sets the entryEditPartToolEntryHoverBackgroundColor.
         *
         * @param entryEditPartToolEntryHoverBackgroundColor the entryEditPartToolEntryHoverBackgroundColor to set
         */
        public void setEntryEditPartToolEntryHoverBackgroundColor(Color entryEditPartToolEntryHoverBackgroundColor) {
            this.entryEditPartToolEntryHoverBackgroundColor = entryEditPartToolEntryHoverBackgroundColor;
        }

        /**
         * Getter for entryEditPartToolEntryBorderFocusForgroundColor.
         *
         * @return the entryEditPartToolEntryBorderFocusForgroundColor
         */
        public Color getEntryEditPartToolEntryBorderFocusForgroundColor() {
            return this.entryEditPartToolEntryBorderFocusForgroundColor;
        }

        /**
         * Sets the entryEditPartToolEntryBorderFocusForgroundColor.
         *
         * @param entryEditPartToolEntryBorderFocusForgroundColor the entryEditPartToolEntryBorderFocusForgroundColor to
         * set
         */
        public void setEntryEditPartToolEntryBorderFocusForgroundColor(Color entryEditPartToolEntryBorderFocusForgroundColor) {
            this.entryEditPartToolEntryBorderFocusForgroundColor = entryEditPartToolEntryBorderFocusForgroundColor;
        }

        /**
         * Getter for entryEditPartToolEntryBorderFocusBackgroundColor.
         *
         * @return the entryEditPartToolEntryBorderFocusBackgroundColor
         */
        public Color getEntryEditPartToolEntryBorderFocusBackgroundColor() {
            return this.entryEditPartToolEntryBorderFocusBackgroundColor;
        }

        /**
         * Sets the entryEditPartToolEntryBorderFocusBackgroundColor.
         *
         * @param entryEditPartToolEntryBorderFocusBackgroundColor the entryEditPartToolEntryBorderFocusBackgroundColor
         * to set
         */
        public void setEntryEditPartToolEntryBorderFocusBackgroundColor(Color entryEditPartToolEntryBorderFocusBackgroundColor) {
            this.entryEditPartToolEntryBorderFocusBackgroundColor = entryEditPartToolEntryBorderFocusBackgroundColor;
        }

        /**
         * Getter for arrowWidth.
         *
         * @return the arrowWidth
         */
        public int getArrowWidth() {
            return this.arrowWidth;
        }

        /**
         * Sets the arrowWidth.
         *
         * @param arrowWidth the arrowWidth to set
         */
        public void setArrowWidth(int arrowWidth) {
            this.arrowWidth = arrowWidth;
        }

        /**
         * Getter for entryEditPartToolEntryTopLineColor.
         *
         * @return the entryEditPartToolEntryTopLineColor
         */
        public Color getEntryEditPartToolEntryTopLineColor() {
            return this.entryEditPartToolEntryTopLineColor;
        }

        /**
         * Sets the entryEditPartToolEntryTopLineColor.
         *
         * @param entryEditPartToolEntryTopLineColor the entryEditPartToolEntryTopLineColor to set
         */
        public void setEntryEditPartToolEntryTopLineColor(Color entryEditPartToolEntryTopLineColor) {
            this.entryEditPartToolEntryTopLineColor = entryEditPartToolEntryTopLineColor;
        }

        /**
         * Getter for entryEditPartToolEntryBottomLineColor.
         *
         * @return the entryEditPartToolEntryBottomLineColor
         */
        public Color getEntryEditPartToolEntryBottomLineColor() {
            return this.entryEditPartToolEntryBottomLineColor;
        }

        /**
         * Sets the entryEditPartToolEntryBottomLineColor.
         *
         * @param entryEditPartToolEntryBottomLineColor the entryEditPartToolEntryBottomLineColor to set
         */
        public void setEntryEditPartToolEntryBottomLineColor(Color entryEditPartToolEntryBottomLineColor) {
            this.entryEditPartToolEntryBottomLineColor = entryEditPartToolEntryBottomLineColor;
        }

        /**
         * Getter for entryEditPartToolEntryBackgroundColor.
         *
         * @return the entryEditPartToolEntryBackgroundColor
         */
        public Color getEntryEditPartToolEntryBackgroundColor() {
            return this.entryEditPartToolEntryBackgroundColor;
        }

        /**
         * Sets the entryEditPartToolEntryBackgroundColor.
         *
         * @param entryEditPartToolEntryBackgroundColor the entryEditPartToolEntryBackgroundColor to set
         */
        public void setEntryEditPartToolEntryBackgroundColor(Color entryEditPartToolEntryBackgroundColor) {
            this.entryEditPartToolEntryBackgroundColor = entryEditPartToolEntryBackgroundColor;
        }

        /**
         * Getter for inheritFromParent.
         *
         * @return the inheritFromParent
         */
        public boolean isInheritFromParent() {
            return this.inheritFromParent;
        }

        /**
         * Sets the inheritFromParent.
         *
         * @param inheritFromParent the inheritFromParent to set
         */
        public void setInheritFromParent(boolean inheritFromParent) {
            this.inheritFromParent = inheritFromParent;
        }

    }
}
