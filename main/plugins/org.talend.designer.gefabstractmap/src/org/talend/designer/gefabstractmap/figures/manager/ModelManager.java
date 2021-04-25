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
package org.talend.designer.gefabstractmap.figures.manager;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.GraphicalEditPart;
import org.talend.designer.gefabstractmap.editor.MapperGraphicalViewer;
import org.talend.designer.gefabstractmap.manager.AbstractMapperManager;

/**
 * created by Administrator on 2013-1-14 Detailled comment
 *
 */
public class ModelManager {

    private EObject model;

    private GraphicalEditPart editPart;

    public ModelManager(EObject model, GraphicalEditPart editPart) {
        this.model = model;
        this.editPart = editPart;
    }

    public MapperGraphicalViewer getGraphicalViewer() {
        return (MapperGraphicalViewer) editPart.getViewer();
    }

    public AbstractMapperManager getMapperManger() {
        return getGraphicalViewer().getMapperManager();
    }

    public EObject getModel() {
        return this.model;
    }

    public GraphicalEditPart getEditPart() {
        return this.editPart;
    }

}
