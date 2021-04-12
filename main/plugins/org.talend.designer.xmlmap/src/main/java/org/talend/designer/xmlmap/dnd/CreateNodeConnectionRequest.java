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
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.CreationFactory;
import org.talend.designer.xmlmap.model.emf.xmlmap.AbstractNode;

/**
 * wchen class global comment. Detailled comment
 */
public class CreateNodeConnectionRequest extends Request {

    private EditPart targetEditPart;

    private Point location;

    private CreationFactory creationFactory;

    private List newObjects = new ArrayList();

    public CreateNodeConnectionRequest(EditPart targetEditPart) {
        this.targetEditPart = targetEditPart;
    }

    public EditPart getTargetEditPart() {
        return this.targetEditPart;
    }

    public AbstractNode getNewObject() {
        if (getFactory() instanceof NewNodeCreationFactory) {
            AbstractNode newObject = ((NewNodeCreationFactory) getFactory()).getNewObject();
            newObjects.add(newObject);
            return newObject;
        }
        return null;
    }

    public List getNewObjects() {
        return this.newObjects;
    }

    public DropType getNewObjectType() {
        if (getFactory() instanceof NewNodeCreationFactory) {
            return ((NewNodeCreationFactory) getFactory()).getObjectType();
        }
        return null;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    public Point getLocation() {
        return this.location;
    }

    protected CreationFactory getFactory() {
        return creationFactory;
    }

    public void setFactory(CreationFactory factory) {
        creationFactory = factory;
    }

}
