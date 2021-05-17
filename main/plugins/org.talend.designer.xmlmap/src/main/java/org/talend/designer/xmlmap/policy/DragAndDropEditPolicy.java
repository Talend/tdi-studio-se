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
package org.talend.designer.xmlmap.policy;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.swt.graphics.Color;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.designer.gefabstractmap.dnd.TransferedObject;
import org.talend.designer.gefabstractmap.part.MapperTablePart;
import org.talend.designer.gefabstractmap.part.TableEntityPart;
import org.talend.designer.gefabstractmap.resource.ColorInfo;
import org.talend.designer.gefabstractmap.resource.ColorProviderMapper;
import org.talend.designer.xmlmap.commands.CreateDocChildrenCommand;
import org.talend.designer.xmlmap.commands.InsertNewColumnCommand;
import org.talend.designer.xmlmap.commands.UpdateExpressionCommand;
import org.talend.designer.xmlmap.commands.UpdateFilterExpressionCommand;
import org.talend.designer.xmlmap.dnd.CreateNodeConnectionRequest;
import org.talend.designer.xmlmap.dnd.DropType;
import org.talend.designer.xmlmap.editor.XmlMapGraphicViewer;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputTreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.VarNode;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;

/**
 * wchen class global comment. Detailled comment
 */
public class DragAndDropEditPolicy extends GraphicalEditPolicy {

    private InsertionIndicator indicator;

    @Override
    public EditPart getTargetEditPart(Request request) {
        if (request instanceof CreateNodeConnectionRequest) {
            return getHost();
        }
        return null;

    }

    @Override
    public Command getCommand(Request request) {
        if (request instanceof CreateNodeConnectionRequest) {
            CreateNodeConnectionRequest rq = (CreateNodeConnectionRequest) request;
            EditPart targetEditPart = rq.getTargetEditPart();
            Command command = null;
            if (targetEditPart != null && TemplateTransfer.getInstance().getObject() instanceof TransferedObject) {
                TransferedObject toDrop = (TransferedObject) TemplateTransfer.getInstance().getObject();
                MapperManager manager = ((XmlMapGraphicViewer) targetEditPart.getViewer()).getMapperManager();
                if (manager != null && manager.getExternalData() != null) {
                    DropType dropType = rq.getNewObjectType();
                    if (dropType != null) {
                        switch (dropType) {
                        case DROP_FILTER:
                            if (targetEditPart instanceof MapperTablePart) {
                                command = new UpdateFilterExpressionCommand(toDrop, (MapperTablePart) targetEditPart,
                                        manager.getExternalData());
                            }
                            break;
                        case DROP_EXPRESSION:
                            if (targetEditPart instanceof TableEntityPart) {
                                command = new UpdateExpressionCommand(toDrop, (TableEntityPart) targetEditPart, manager);
                            }
                            break;
                        case DROP_OUTPUT_DOC_CHILD:
                            if (targetEditPart instanceof OutputTreeNodeEditPart && rq.getNewObject() instanceof OutputTreeNode) {
                                command = new CreateDocChildrenCommand(toDrop, (OutputTreeNodeEditPart) targetEditPart, rq,
                                        manager);
                            }
                            break;
                        case DROP_INSERT_OUTPUT:
                        case DROP_INSERT_VAR:
                        case DROP_INSERT_INPUT:
                            command = new InsertNewColumnCommand(toDrop, targetEditPart, rq, manager, dropType);
                        default:
                            break;
                        }
                    }
                }
            }

            if (command != null) {
                return command;
            }
            // drop expression
            // boolean update = rq.getDropType() == CreateNodeConnectionRequest.DROP_EXPRESSION ? true : false;
            // return new CreateNodeAndConnectionCommand(rq.getNewObject(), rq.getTargetEditPart(), update);
        }
        return super.getCommand(request);
    }

    @Override
    public void showTargetFeedback(Request request) {
        if (request instanceof CreateNodeConnectionRequest) {
            CreateNodeConnectionRequest rq = (CreateNodeConnectionRequest) request;
            AbstractGraphicalEditPart targetEditPart = (AbstractGraphicalEditPart) rq.getTargetEditPart();
            // show feedback when insert new column
            if (rq.getNewObjectType() != null && targetEditPart != null) {

                Object model = targetEditPart.getModel();
                switch (rq.getNewObjectType()) {
                case DROP_INSERT_OUTPUT:
                case DROP_INSERT_VAR:
                    if (model instanceof OutputTreeNode || model instanceof VarNode) {
                        IFigure targetFigure = targetEditPart.getFigure();
                        if (targetFigure != null) {
                            if (indicator == null) {
                                indicator = new InsertionIndicator();
                            }

                            Rectangle copy = targetFigure.getBounds().getCopy();
                            Rectangle bounds = new Rectangle();
                            bounds.x = copy.x - 5;
                            bounds.y = copy.y - 5;
                            bounds.width = copy.width + 10;
                            bounds.height = 10;
                            indicator.setBounds(bounds);
                            getFeedbackLayer().add(indicator);

                        }
                    } else {
                        if (indicator != null && getFeedbackLayer() != null && indicator.getParent() == getFeedbackLayer()) {
                            getFeedbackLayer().remove(indicator);
                        }
                    }
                    break;
                }
            }

        }
    }

    @Override
    public void eraseTargetFeedback(Request request) {
        if (indicator != null) {
            if (request instanceof CreateNodeConnectionRequest) {
                CreateNodeConnectionRequest rq = (CreateNodeConnectionRequest) request;
                AbstractGraphicalEditPart targetEditPart = (AbstractGraphicalEditPart) rq.getTargetEditPart();
                if (targetEditPart != null && getFeedbackLayer() != null && indicator.getParent() == getFeedbackLayer()) {
                    try {
                        getFeedbackLayer().remove(indicator);
                    } catch (Exception e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
    }

    class InsertionIndicator extends Shape {

        @Override
        protected void fillShape(Graphics graphics) {

        }

        @Override
        protected void outlineShape(Graphics graphics) {
            Color color = ColorProviderMapper.getColor(ColorInfo.COLOR_DRAGGING_INSERTION_INDICATOR);
            // Color color = ColorConstants.red;

            Point start = new Point(getBounds().x + 5, getBounds().y + 5);
            Point end = new Point(getBounds().x + getBounds().width - 5, getBounds().y + 5);

            graphics.setBackgroundColor(color);
            graphics.setForegroundColor(color);

            graphics.drawLine(start, end);
            graphics.fillPolygon(new int[] { start.x, start.y, start.x - 5, start.y + 5, start.x - 5, start.y - 5 });
            graphics.fillPolygon(new int[] { end.x, end.y, end.x + 5, end.y - 5, end.x + 5, end.y + 5 });
        }
    }
}
