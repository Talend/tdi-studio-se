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
package org.talend.designer.gefabstractmap.figures.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.AbstractHintLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.designer.gefabstractmap.editor.MapperGraphicalViewer;
import org.talend.designer.gefabstractmap.figures.sash.SashSeparator;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapDataLayout extends AbstractHintLayout {

    private MapperGraphicalViewer viewer;

    private int zoneWidth;

    org.eclipse.swt.graphics.Point previousAvilableSize;

    /** The layout contraints */
    protected Map constraints = new HashMap();

    public XmlMapDataLayout(MapperGraphicalViewer viewer) {
        this.viewer = viewer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.draw2d.LayoutManager#layout(org.eclipse.draw2d.IFigure)
     */
    @Override
    public void layout(IFigure parent) {
        List children = parent.getChildren();
        int numChildren = children.size();
        org.eclipse.swt.graphics.Point avilableSize = viewer.getControl().getSize();

        double shrinkWidth = 1;
        if (previousAvilableSize != null && previousAvilableSize != avilableSize) {
            shrinkWidth = (double) avilableSize.x / (double) previousAvilableSize.x;
            previousAvilableSize = avilableSize;
        } else {
            previousAvilableSize = avilableSize;
        }

        // get the number of separators and the total width, the width of separator for zone is fixed
        int separatorWith = 0;
        int separatorNum = 0;
        for (int i = 0; i < numChildren; i++) {
            IFigure child = (IFigure) children.get(i);
            if (child instanceof SashSeparator) {
                separatorNum++;
                SashSeparator separator = (SashSeparator) child;
                separatorWith = separatorWith + separator.getSashWidth();
            }
        }

        zoneWidth = (avilableSize.x - separatorWith) / (numChildren - separatorNum);
        // zoneWidth = (avilableSize.x - separatorWith - 1) / (numChildren - separatorNum);
        Rectangle clientArea = parent.getClientArea();
        int x = clientArea.x;
        int y = clientArea.y;
        Point offset = parent.getClientArea().getLocation();

        int toltalWidth = 0;
        for (int i = 0; i < numChildren; i++) {
            IFigure f = (IFigure) children.get(i);
            Rectangle bounds = (Rectangle) getConstraint(f);
            if (bounds == null) {
                Rectangle newBounds = null;
                if (f instanceof SashSeparator) {
                    SashSeparator separator = (SashSeparator) f;
                    newBounds = new Rectangle(x, y, separator.getSashWidth(), avilableSize.y);
                    f.setBounds(newBounds);
                    x = x + separator.getSashWidth();
                } else {
                    newBounds = new Rectangle(x, y, zoneWidth, avilableSize.y);
                    f.setBounds(newBounds);
                    x = x + zoneWidth;
                }
                setConstraint(f, newBounds);
                continue;

            }
            bounds = bounds.getCopy();

            // avialable size changed.
            if (shrinkWidth != 1) {
                bounds.x = x;
                if (!(f instanceof SashSeparator)) {
                    int w = (int) Math.floor(bounds.width * shrinkWidth);
                    bounds.width = w;
                }
                x = x + bounds.width;
            }

            if (bounds.width == -1 || bounds.height == -1) {
                Dimension preferredSize = f.getPreferredSize(bounds.width, bounds.height);
                bounds = bounds.getCopy();
                if (bounds.width == -1) {
                    bounds.width = preferredSize.width;
                }
                if (bounds.height == -1) {
                    bounds.height = preferredSize.height;
                }
            }
            bounds = bounds.getTranslated(offset);
            bounds.height = avilableSize.y;
            toltalWidth = toltalWidth + bounds.width;
            f.setBounds(bounds);
            setConstraint(f, bounds);

        }

        // in case some blank width
        if (toltalWidth != 0) {
            int diff = avilableSize.x - toltalWidth;
            if (diff < 0) {
                diff = 0;
            }
            int avg = diff / (numChildren - separatorNum);
            int remainder = diff % (numChildren - separatorNum);
            if (avg != 0) {
                x = clientArea.x;
                for (int i = 0; i < numChildren; i++) {
                    IFigure f = (IFigure) children.get(i);
                    Rectangle bounds = f.getBounds();
                    bounds.x = x;
                    if (!(f instanceof SashSeparator)) {
                        bounds.width = bounds.width + avg;
                    }
                    f.setBounds(bounds);
                    x = x + bounds.width;
                }
            }
            if (remainder != 0) {
                IFigure lastChild = (IFigure) children.get(children.size() - 1);
                final Rectangle bounds = lastChild.getBounds();
                bounds.width = bounds.width + remainder;
                lastChild.setBounds(bounds);
            }
        }

    }

    @Override
    protected Dimension calculatePreferredSize(IFigure f, int wHint, int hHint) {

        return new Dimension(viewer.getControl().getSize());
    }

    /**
     * @see LayoutManager#remove(IFigure)
     */
    @Override
    public void remove(IFigure figure) {
        super.remove(figure);
        constraints.remove(figure);
    }

    @Override
    public Object getConstraint(IFigure figure) {
        return constraints.get(figure);
    }

    /**
     * Sets the layout constraint of the given figure. The constraints can only be of type {@link Rectangle}.
     *
     * @see LayoutManager#setConstraint(IFigure, Object)
     * @since 2.0
     */
    @Override
    public void setConstraint(IFigure figure, Object newConstraint) {
        super.setConstraint(figure, newConstraint);
        if (newConstraint != null) {
            constraints.put(figure, newConstraint);
        }
    }
}
