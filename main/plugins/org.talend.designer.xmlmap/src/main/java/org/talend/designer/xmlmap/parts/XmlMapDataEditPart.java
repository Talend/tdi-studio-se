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
package org.talend.designer.xmlmap.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.talend.designer.gefabstractmap.figures.layout.ZoneContentLayout;
import org.talend.designer.gefabstractmap.figures.treetools.zone.InputZoneToolBar;
import org.talend.designer.gefabstractmap.figures.treetools.zone.OutputZoneToolBar;
import org.talend.designer.gefabstractmap.figures.treetools.zone.SearchZoneToolBar;
import org.talend.designer.gefabstractmap.part.MapperRootEditPart;
import org.talend.designer.gefabstractmap.part.directedit.XmlMapNodeCellEditorLocator;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.figures.layout.XmlMapTreeContainerLayout;
import org.talend.designer.xmlmap.figures.treetools.zone.XmlMapInputZoneToolBar;
import org.talend.designer.xmlmap.figures.treetools.zone.XmlMapOutputZoneToolBar;
import org.talend.designer.xmlmap.figures.treetools.zone.XmlMapSearchZoneToolBar;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.designer.xmlmap.parts.directedit.XmlMapNodeDirectEditManager;
import org.talend.designer.xmlmap.policy.XmlDirectEditPolicy;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapDataEditPart extends MapperRootEditPart {

    private XmlMapNodeDirectEditManager directEditManager;

    @Override
    public List getModelChildren() {
        List children = new ArrayList();
        children.addAll(((XmlMapData) getModel()).getInputTrees());
        children.addAll(((XmlMapData) getModel()).getOutputTrees());
        children.addAll(((XmlMapData) getModel()).getVarTables());
        return children;
    }

    @Override
    protected XmlMapDataManager createRootModelManager() {
        return new XmlMapDataManager((XmlMapData) getModel(), this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.part.MapperRootEditPart#createOutputZoneToolBar()
     */
    @Override
    protected OutputZoneToolBar createOutputZoneToolBar() {
        return new XmlMapOutputZoneToolBar((XmlMapDataManager) getRootModelManager());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.part.MapperRootEditPart#createInputZoneToolBar()
     */
    @Override
    protected InputZoneToolBar createInputZoneToolBar() {
        return new XmlMapInputZoneToolBar((XmlMapDataManager) getRootModelManager());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.part.MapperRootEditPart#createTreeContainerLayout()
     */
    @Override
    protected ZoneContentLayout createZoneContentLayout() {
        return new XmlMapTreeContainerLayout();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.part.MapperRootEditPart#createSearchZoneToolBar()
     */
    @Override
    protected SearchZoneToolBar createSearchZoneToolBar() {
        return new XmlMapSearchZoneToolBar((XmlMapDataManager) getRootModelManager());
    }

    @Override
    public void performRequest(Request req) {
        if (RequestConstants.REQ_DIRECT_EDIT.equals(req.getType())) {
            DirectEditRequest drequest = (DirectEditRequest) req;
            Point figureLocation = drequest.getLocation();
            IFigure findFigureAt = getFigure().findFigureAt(figureLocation.x, figureLocation.y);
            if (findFigureAt != null && findFigureAt instanceof Label) {
                directEditManager = new XmlMapNodeDirectEditManager(this, new XmlMapNodeCellEditorLocator((Figure) findFigureAt));
                directEditManager.show();
            }
            if (directEditManager != null) {
                if (findFigureAt != null && findFigureAt instanceof Label) {
                    if (figure.containsPoint(figureLocation)) {
                        directEditManager.show();
                        ((XmlMapGraphicViewer) getViewer()).getMapperManager().setCurrentDirectEditManager(directEditManager);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.gefabstractmap.part.BaseEditPart#createEditPolicies()
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new XmlDirectEditPolicy());
    }

    // @Override
    // public XmlMapDataManager getRootModelManager() {
    // return (XmlMapDataManager) super.getRootModelManager();
    // }

}
