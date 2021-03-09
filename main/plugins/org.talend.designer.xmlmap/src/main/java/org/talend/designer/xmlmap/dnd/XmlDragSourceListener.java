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
package org.talend.designer.xmlmap.dnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.talend.designer.gefabstractmap.dnd.MapperDragSourceListener;
import org.talend.designer.gefabstractmap.dnd.TransferdType;
import org.talend.designer.gefabstractmap.dnd.TransferedObject;
import org.talend.designer.gefabstractmap.figures.sash.ISash;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.utils.MapperUtils;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.parts.VarNodeEditPart;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlDragSourceListener extends MapperDragSourceListener {

    public XmlDragSourceListener(EditPartViewer viewer) {
        super(viewer);
    }

    /**
     *
     * DOC talend Comment method "getTemplate".
     *
     * @param event
     * @return the validate drag able node list
     */
    @Override
    protected Object getTemplate(DragSourceEvent event) {
        final RootEditPart rootEditPart = getViewer().getRootEditPart();
        if (rootEditPart instanceof AbstractGraphicalEditPart) {
            AbstractGraphicalEditPart graphicPart = (AbstractGraphicalEditPart) rootEditPart;
            final IFigure figure = graphicPart.getFigure();
            final IFigure findFigureAt = figure.findFigureAt(new Point(event.x, event.y));
            if (findFigureAt instanceof ISash) {
                return findFigureAt;
            }
        }
        List<EditPart> filtedSelection = new ArrayList<EditPart>();
        for (Object part : getViewer().getSelectedEditParts()) {
            if (part instanceof TreeNodeEditPart || part instanceof VarNodeEditPart) {
                filtedSelection.add((EditPart) part);
            }
        }

        if (filtedSelection == null || filtedSelection.isEmpty()) {
            return null;
        }
        List toTransfer = new ArrayList();
        TransferdType type = null;
        List<TableEntityPart> partList = new ArrayList<TableEntityPart>();
        EditPart lastSelection = filtedSelection.get(filtedSelection.size() - 1);
        if (lastSelection instanceof TreeNodeEditPart && !(lastSelection instanceof OutputTreeNodeEditPart)) {
            type = TransferdType.INPUT;
        } else if (lastSelection instanceof VarNodeEditPart) {
            type = TransferdType.VAR;
        }

        if (type != null) {
            if (filtedSelection.size() > 1) {
                partList.addAll(lastSelection.getParent().getChildren());
                Map<EditPart, Integer> partAndIndex = new HashMap<EditPart, Integer>();
                if (type == TransferdType.INPUT) {
                    for (EditPart treePart : filtedSelection) {
                        if (!XmlMapUtil.isDragable((TreeNode) treePart.getModel())) {
                            return null;
                        }
                    }

                    MapperTablePart abstractInOutTreePart = MapperUtils.getMapperTablePart((TableEntityPart) lastSelection);
                    if (abstractInOutTreePart != null) {
                        partList = MapperUtils.getFlatChildrenPartList(abstractInOutTreePart);
                    }
                } else {
                    partList.addAll(lastSelection.getParent().getChildren());
                }

                for (EditPart selected : filtedSelection) {
                    int indexOf = partList.indexOf(selected);
                    if (indexOf != -1) {
                        partAndIndex.put(selected, indexOf);
                        int index = 0;
                        for (int i = 0; i < toTransfer.size(); i++) {
                            if (indexOf > partAndIndex.get(toTransfer.get(i))) {
                                index = i + 1;
                            }
                        }
                        toTransfer.add(index, selected);
                    }
                }

            } else {
                if (lastSelection.getModel() instanceof TreeNode && !XmlMapUtil.isDragable((TreeNode) lastSelection.getModel())) {
                    return null;
                }
                toTransfer.add(lastSelection);
            }
            return new TransferedObject(toTransfer, type);
        }
        return null;

    }
}
