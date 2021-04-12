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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.swt.SWT;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TableEntityPart extends BaseEditPart {

    // for expand and collapse
    protected IFigure rootAnchor;

    public void refreshChildrenSourceConnections(TableEntityPart rootPart, boolean expanded) {
        for (Object obj : getChildren()) {
            if (obj instanceof TableEntityPart) {
                TableEntityPart part = (TableEntityPart) obj;
                if (expanded) {
                    // do collapse
                    part.setRootAnchor(rootPart.getFigure());
                } else {
                    part.setRootAnchor(null);
                }
                if (part.getSourceConnections() != null) {
                    for (Object conn : part.getSourceConnections()) {
                        if (conn instanceof BaseConnectionEditPart) {
                            BaseConnectionEditPart connectionEditPart = (BaseConnectionEditPart) conn;
                            if (connectionEditPart.getFigure() instanceof PolylineConnection) {
                                PolylineConnection connFigure = (PolylineConnection) connectionEditPart.getFigure();
                                if (expanded) {
                                    connectionEditPart.setNodeCollapsed(true);
                                    connFigure.setLineStyle(SWT.LINE_DASHDOTDOT);
                                } else {
                                    connectionEditPart.setNodeCollapsed(false);
                                    if (!connectionEditPart.isDOTStyle()) {
                                        connFigure.setLineStyle(SWT.LINE_SOLID);
                                    } else {
                                        connFigure.setLineStyle(SWT.LINE_DASHDOTDOT);
                                    }
                                }
                            }
                            connectionEditPart.refresh();
                        }
                    }
                }
                part.refreshChildrenSourceConnections(rootPart, expanded);
            }
        }

    }

    public void refreshChildrenTargetConnections(TableEntityPart rootPart, boolean expanded) {
        for (Object obj : getChildren()) {
            if (obj instanceof TableEntityPart) {
                TableEntityPart part = (TableEntityPart) obj;
                if (expanded) {
                    // do collapse
                    part.setRootAnchor(rootPart.getFigure());
                } else {
                    part.setRootAnchor(null);
                }
                if (part.getTargetConnections() != null) {
                    for (Object conn : part.getTargetConnections()) {
                        if (conn instanceof AbstractConnectionEditPart) {
                            AbstractConnectionEditPart connectionEditPart = (AbstractConnectionEditPart) conn;
                            connectionEditPart.refresh();
                            // if (connectionEditPart.getFigure() instanceof PolylineConnection) {
                            // PolylineConnection connFigure = (PolylineConnection) connectionEditPart.getFigure();
                            // if (expanded) {
                            // connFigure.setLineStyle(SWT.LINE_DASHDOTDOT);
                            // } else {
                            // connFigure.setLineStyle(SWT.LINE_SOLID);
                            // }
                            // }
                        }
                    }
                }
                part.refreshChildrenTargetConnections(rootPart, expanded);
            }
        }
    }

    protected IFigure getRootAnchor() {
        return rootAnchor;
    }

    protected void setRootAnchor(IFigure rootAnchor) {
        this.rootAnchor = rootAnchor;
    }

}
