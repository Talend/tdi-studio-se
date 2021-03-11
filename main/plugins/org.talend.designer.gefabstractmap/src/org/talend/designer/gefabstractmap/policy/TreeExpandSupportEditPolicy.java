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
package org.talend.designer.gefabstractmap.policy;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.talend.designer.gefabstractmap.figures.manager.EntitySelectManager;
import org.talend.designer.gefabstractmap.figures.table.entity.ExpandCollapseFigure;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityElement;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityFigure;
import org.talend.designer.gefabstractmap.figures.table.entity.TableTreeEntityFigure;
import org.talend.designer.gefabstractmap.part.InputTablePart;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.gefabstractmap.part.OutputTablePart;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.utils.MapperUtils;

/**
 * wchen class global comment. Detailled comment
 */
public class TreeExpandSupportEditPolicy extends GraphicalEditPolicy {

    private boolean expanded = true;

    private Listener l = new Listener();

    public static final String EXPAND_SUPPORT = "EXPAND_SUPPORT";

    public static final String SIZE_CHANGED = "SIZE_CHANGED";

    @Override
    public EditPart getTargetEditPart(Request request) {
        return super.getTargetEditPart(request);
    }

    private class Listener extends MouseListener.Stub {

        @Override
        public void mousePressed(MouseEvent me) {
            if (me.getSource() instanceof ExpandCollapseFigure) {
                ExpandCollapseFigure ecFigure = (ExpandCollapseFigure) me.getSource();
                EntitySelectManager manager = EntitySelectManager.getManager();
                if (ecFigure.getParent().getParent() instanceof TableEntityElement) {
                    TableEntityElement rowFigure = (TableEntityElement) ecFigure.getParent().getParent();
                    manager.setSelection(rowFigure); // select first
                    TableTreeEntityFigure treeNodeFigure = ecFigure.getParent().getEntityFigure();
                    if (treeNodeFigure != null) {
                        expanded = treeNodeFigure.isExpanded();
                        treeNodeFigure.doExpandCollapse();
                    }
                }
            }

            MapperTablePart tablePart = MapperUtils.getMapperTablePart((TableEntityPart) getHost());
            if (tablePart instanceof OutputTablePart) {
                TableEntityPart host = (TableEntityPart) getHost();
                host.refreshChildrenTargetConnections(host, expanded);
            } else if (getHost() instanceof InputTablePart) {
                TableEntityPart host = (TableEntityPart) getHost();
                host.refreshChildrenSourceConnections(host, expanded);
            }

        }

    }

    @Override
    public void activate() {
        super.activate();

        TableEntityPart host = (TableEntityPart) getHost();
        if (host.getFigure() instanceof TableEntityFigure) {
            TableTreeEntityFigure treeNodeFigure = (TableTreeEntityFigure) host.getFigure();
            if (treeNodeFigure.getTreeBranch() != null) {
                ExpandCollapseFigure ecFigure = treeNodeFigure.getTreeBranch().getExpandCollapseFigure();
                if (ecFigure != null) {
                    ecFigure.addMouseListener(l);
                }

            }
        }

    }

    @Override
    public void deactivate() {
        TableEntityPart host = (TableEntityPart) getHost();
        if (host.getFigure() instanceof TableTreeEntityFigure) {
            TableTreeEntityFigure treeNodeFigure = (TableTreeEntityFigure) host.getFigure();
            if (treeNodeFigure.getTreeBranch() != null) {
                ExpandCollapseFigure ecFigure = treeNodeFigure.getTreeBranch().getExpandCollapseFigure();
                if (ecFigure != null) {
                    ecFigure.removeMouseListener(l);
                }
            }

        }
        super.deactivate();
    }

}
