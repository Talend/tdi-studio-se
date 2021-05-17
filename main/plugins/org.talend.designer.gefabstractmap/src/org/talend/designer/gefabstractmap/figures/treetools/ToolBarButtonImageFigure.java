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
package org.talend.designer.gefabstractmap.figures.treetools;

import org.eclipse.draw2d.AbstractBackground;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.CompoundBorder;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.TextUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class ToolBarButtonImageFigure extends ImageFigure {

    protected Image oldImage;

    protected Image enabledImage;

    protected Image disabledImage;

    private int style = Clickable.STYLE_BUTTON;

    private boolean selected;

    protected MouseMotionListener listener;

    private int iconTextGap = 3;

    private Dimension textSize;

    private String text = "";

    private Image img;

    private Dimension size = new Dimension();

    private int alignment;

    public ToolBarButtonImageFigure(Image image) {
        setImage(image);
        setOpaque(true);
        initlistener();
        setBorder(new MarginBorder(2));
        if (image != null) {
            this.enabledImage = image;
        }
    }

    /**
     * DOC Administrator Comment method "initlistener".
     */
    protected void initlistener() {
        listener = new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent me) {

            }

            @Override
            public void mouseHover(MouseEvent me) {
                addLineBorder();
                setHoverColor();
            }

            @Override
            public void mouseExited(MouseEvent me) {
                if (Clickable.STYLE_TOGGLE == getStyle()) {
                    if (!isSelected()) {
                        removeLineBorder();
                        setBackgroundColor(getContainerBgColor(ToolBarButtonImageFigure.this));
                    }
                } else {
                    removeLineBorder();
                    setBackgroundColor(getContainerBgColor(ToolBarButtonImageFigure.this));
                }
            }

            @Override
            public void mouseEntered(MouseEvent me) {

            }

            @Override
            public void mouseDragged(MouseEvent me) {

            }
        };
        this.addMouseMotionListener(listener);

        this.addMouseListener(new MouseListener() {

            @Override
            public void mousePressed(MouseEvent me) {
                toolBarButtonPressed(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                // TODO Auto-generated method stub
            }

            @Override
            public void mouseDoubleClicked(MouseEvent me) {
                // TODO Auto-generated method stub

            }

        });
    }

    public void toolBarButtonPressed(MouseEvent me) {
        setSelected(!isSelected());
    }

    private void addLineBorder() {
        if (getBorder() instanceof MarginBorder) {
            LineBorder outer = new LineBorder(ColorProviderMapper.getColor(ColorInfo.COLOR_TREE_BORDER));
            CustomCompoundBorder border = new CustomCompoundBorder(outer, getBorder());
            setBorder(border);
        }
    }

    private void removeLineBorder() {
        if (getBorder() instanceof CustomCompoundBorder) {
            CustomCompoundBorder compound = (CustomCompoundBorder) getBorder();
            setBorder(compound.getInnerBorder());
        }
    }

    private void setHoverColor() {
        setBackgroundColor(ColorConstants.buttonLightest);
    }

    private Color getContainerBgColor(IFigure figure) {
        if (figure == null) {
            return ColorConstants.menuBackground;
        }
        if (figure.getParent() != null) {
            if (figure.getParent().isOpaque()) {
                return figure.getParent().getBackgroundColor();
            } else {
                return getContainerBgColor(figure.getParent());
            }
        } else {
            return ColorConstants.menuBackground;
        }

    }

    @Override
    public void setEnabled(boolean value) {
        super.setEnabled(value);
        changeImage();
    }

    private void changeImage() {
        if (isEnabled()) {
            if (getImage() != enabledImage) {
                setImage(enabledImage);
            }
        } else {
            this.disabledImage = ImageProviderMapper.getDisabledImage(enabledImage);
            if (getImage() != disabledImage) {
                setImage(disabledImage);
            }
        }

    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (getStyle() == Clickable.STYLE_TOGGLE && selected) {
            addLineBorder();
            setHoverColor();
        }
    }

    /**
     * @return The Image that this Figure displays
     */
    @Override
    public Image getImage() {
        return img;
    }

    /**
     * Calculates the necessary size to display the Image within the figure's client area.
     *
     * @see org.eclipse.draw2d.Figure#getPreferredSize(int, int)
     */
    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {

        Insets i = getInsets();
        Dimension dimension = new Dimension(size);
        Dimension labelSize = calculateLabelSize(getTextSize());
        dimension.width = dimension.width + labelSize.width;
        dimension.height = Math.max(dimension.height, labelSize.height);

        return dimension.getExpanded(i.getWidth(), i.getHeight());

    }

    protected Dimension calculateLabelSize(Dimension txtSize) {
        int gap = getIconTextGap();
        if (getText().equals("")) {
            gap = 0;
        }
        Dimension d = new Dimension(0, 0);
        d.width = gap + txtSize.width;
        d.height = txtSize.height;

        return d;
    }

    /**
     * @see org.eclipse.draw2d.Figure#paintFigure(Graphics)
     */
    @Override
    protected void paintFigure(Graphics graphics) {
        if (isOpaque()) {
            graphics.fillRectangle(getBounds());
        }
        if (getBorder() instanceof AbstractBackground) {
            ((AbstractBackground) getBorder()).paintBackground(this, graphics, NO_INSETS);
        }

        int x, y;
        Rectangle area = getClientArea();
        y = area.y;
        x = area.x;
        // switch (alignment & PositionConstants.NORTH_SOUTH) {
        // case PositionConstants.NORTH:
        // break;
        // case PositionConstants.SOUTH:
        // y = area.y + area.height - size.height;
        // break;
        // default:
        // y = (area.height - size.height) / 2 + area.y;
        // break;
        // }
        // switch (alignment & PositionConstants.EAST_WEST) {
        // case PositionConstants.EAST:
        // x = area.x + area.width - size.width;
        // break;
        // case PositionConstants.WEST:
        // break;
        // default:
        // x = (area.width - size.width) / 2 + area.x;
        // break;
        // }
        if (getImage() != null) {
            graphics.drawImage(getImage(), x, y);
            x = x + size.width + iconTextGap;
        }
        if (getText() != null) {
            graphics.drawText(getText(), x, y);
        }

    }

    /**
     * Sets the alignment of the Image within this Figure. The alignment comes into play when the ImageFigure is larger
     * than the Image. The alignment could be any valid combination of the following:
     *
     * <UL>
     * <LI>PositionConstants.NORTH</LI>
     * <LI>PositionConstants.SOUTH</LI>
     * <LI>PositionConstants.EAST</LI>
     * <LI>PositionConstants.WEST</LI>
     * <LI>PositionConstants.CENTER or PositionConstants.NONE</LI>
     * </UL>
     *
     * @param flag A constant indicating the alignment
     */
    @Override
    public void setAlignment(int flag) {
        alignment = flag;
    }

    /**
     * Sets the Image that this ImageFigure displays.
     * <p>
     * IMPORTANT: Note that it is the client's responsibility to dispose the given image.
     *
     * @param image The Image to be displayed. It can be <code>null</code>.
     */
    @Override
    public void setImage(Image image) {
        if (img == image) {
            return;
        }
        img = image;
        if (img != null) {
            size = new Rectangle(image.getBounds()).getSize();
        } else {
            size = new Dimension();
        }
        revalidate();
        notifyImageChanged();
        repaint();
    }

    public void setIconTextGap(int gap) {
        if (iconTextGap == gap) {
            return;
        }
        iconTextGap = gap;
        repaint();
        revalidate();
    }

    public int getIconTextGap() {
        return iconTextGap;
    }

    protected Dimension getTextSize() {
        if (textSize == null) {
            textSize = calculateTextSize();
        }
        return textSize;
    }

    protected Dimension calculateTextSize() {
        return TextUtilities.INSTANCE.getTextExtents(getText(), getFont());
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    class CustomCompoundBorder extends CompoundBorder {

        public CustomCompoundBorder(Border outer, Border inner) {
            super(outer, inner);
        }

        @Override
        public Insets getInsets(IFigure figure) {
            Insets insets = null;
            if (inner != null) {
                insets = inner.getInsets(figure);
            } else if (outer != null) {
                Insets moreInsets = outer.getInsets(figure);
                insets = new Insets();
                insets = insets.getAdded(moreInsets);
            }
            return insets;
        }
    }

}
