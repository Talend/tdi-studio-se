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
package org.talend.designer.business.model.business.diagram.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.common.ui.action.global.GlobalActionId;
import org.eclipse.gmf.runtime.common.ui.services.action.global.IGlobalActionContext;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramCommandStack;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.diagram.ui.providers.DiagramGlobalActionHandler;
import org.eclipse.gmf.runtime.diagram.ui.requests.PasteViewRequest;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.designer.business.diagram.custom.commands.GmfPastCommand;
import org.talend.designer.business.model.business.BusinessAssignment;
import org.talend.designer.business.model.business.BusinessItem;
import org.talend.designer.business.model.business.BusinessProcess;
import org.talend.designer.business.model.business.TalendItem;
import org.talend.designer.business.model.business.diagram.edit.parts.BusinessProcessEditPart;

/**
 * wchen class global comment. Detailled comment
 */
public class ClipboardActionHandler extends DiagramGlobalActionHandler {

    private static boolean isCut = false;

    private static IDiagramWorkbenchPart older;

    // this variable is only for copy, because: GmfPastCommand.GmfPastCommand(), the parameter: addNew = this.isCut |
    // inEditors, so this varible orginalCopyFrom only affects the inEditors
    private static IDiagramWorkbenchPart orginalCopyFrom;

    private static Map cutItemIds;

    // save source process items list for cut and in case of pasting in editors
    private static List<BusinessItem> clonedSourceProcessItemsList;

    @Override
    public ICommand getCommand(IGlobalActionContext cntxt) {

        IWorkbenchPart part = cntxt.getActivePart();
        if (!(part instanceof IDiagramWorkbenchPart)) {
            return null;
        }

        IDiagramWorkbenchPart workbenchPart = (IDiagramWorkbenchPart) part;
        DiagramEditPart diagramEditPart = workbenchPart.getDiagramEditPart();
        ICommand command = null;

        String actionId = cntxt.getActionId();
        if (actionId.equals(GlobalActionId.COPY)) {
            command = getCopyCommand(cntxt, workbenchPart, false);
            transfer(cntxt.getSelection());
            isCut = false;
            older = workbenchPart;
            orginalCopyFrom = workbenchPart;
            clonedSourceProcessItemsList = new ArrayList<BusinessItem>(
                    ((BusinessProcess) ((Diagram) diagramEditPart.getModel()).getElement()).getBusinessItems());
        } else if (actionId.equals(GlobalActionId.CUT) && cntxt.getSelection() != null) {
            saveCut(cntxt.getSelection());
            command = getCutCommand(cntxt, workbenchPart);
            transfer(cntxt.getSelection());
            isCut = true;
            older = workbenchPart;
            clonedSourceProcessItemsList = new ArrayList<BusinessItem>(
                    ((BusinessProcess) ((Diagram) diagramEditPart.getModel()).getElement()).getBusinessItems());
        } else if (actionId.equals(GlobalActionId.SAVE)) {
            if (workbenchPart instanceof IEditorPart) {
                IEditorPart editorPart = (IEditorPart) workbenchPart;
                if (editorPart.isDirty()) {
                    IWorkbenchPage page = editorPart.getSite().getPage();
                    page.saveEditor(editorPart, false);
                }
            }
        }
        if (actionId.equals(GlobalActionId.PASTE)) {

            // diagramPart.getDiagramGraphicalViewer().setSelection(new
            // StructuredSelection(diagramPart.getDiagramEditPart()));

            PasteViewRequest pasteReq = createPasteViewRequest();
            CommandStack cs = workbenchPart.getDiagramEditDomain().getDiagramCommandStack();

            IStructuredSelection selection = (IStructuredSelection) cntxt.getSelection();
            if (!(selection.getFirstElement() instanceof BusinessProcessEditPart)) {

                selection = new StructuredSelection(workbenchPart.getDiagramEditPart());
            }

            Object[] objects = selection.toArray();
            Collection returnValues = null;
            if (objects.length == 1) {
                Command paste = ((EditPart) objects[0]).getCommand(pasteReq);
                if (paste != null) {

                    cs.execute(paste);
                    workbenchPart.getDiagramEditPart().getFigure().invalidate();
                    workbenchPart.getDiagramEditPart().getFigure().validate();
                    returnValues = DiagramCommandStack.getReturnValues(paste);
                    // selectAddedObject(diagramPart.getDiagramGraphicalViewer(), returnValues);

                }
            }

            Object elements = TemplateTransfer.getInstance().getObject();

            if (elements instanceof List) {
                List<BusinessItem> list = (List<BusinessItem>) elements;

                boolean inEditors = false;
                if (older != workbenchPart) {
                    inEditors = true;
                } else if (!this.isCut && orginalCopyFrom != workbenchPart) {
                    // bug 16065 fixed, by xtan. to resolve the copy(A)/parse(B)/parse(B)/parse(B)... problem.
                    inEditors = true;
                }

                // always keep the last one as the current selection.
                older = workbenchPart;

                GmfPastCommand pastBusiness = new GmfPastCommand(
                        (BusinessProcess) ((Diagram) diagramEditPart.getModel()).getElement(), list, diagramEditPart,
                        this.cutItemIds, this.isCut | inEditors);
                pastBusiness.setClonedSourceProcessItemsList(clonedSourceProcessItemsList);
                try {
                    pastBusiness.execute(null, null);
                } catch (ExecutionException e) {
                    ExceptionHandler.process(e);
                }

            }
            if (returnValues != null) {
                selectAddedObject(workbenchPart.getDiagramGraphicalViewer(), returnValues);
            }
            return null;
        }

        return command;
    }

    private void transfer(ISelection object) {
        if (object instanceof IStructuredSelection) {
            List<BusinessItem> selections = new ArrayList();
            for (Object obj : ((IStructuredSelection) object).toList()) {

                if (obj instanceof AbstractEditPart) {
                    AbstractEditPart editPart = (AbstractEditPart) obj;
                    EObject element = ((View) editPart.getModel()).getElement();
                    if (element instanceof BusinessItem) {
                        selections.add((BusinessItem) element);
                    }
                }
            }
            TemplateTransfer.getInstance().setObject(selections);
        }
    }

    private void saveCut(ISelection object) {
        cutItemIds = new HashMap();
        if (object instanceof IStructuredSelection) {

            for (Object obj : ((IStructuredSelection) object).toList()) {
                if (obj instanceof AbstractEditPart) {
                    AbstractEditPart editPart = (AbstractEditPart) obj;
                    EObject element = ((View) editPart.getModel()).getElement();
                    if (element instanceof BusinessItem) {
                        BusinessItem businessItem = (BusinessItem) element;
                        List assignments = new ArrayList();
                        for (Object assignment : businessItem.getAssignments()) {
                            BusinessAssignment ba = (BusinessAssignment) assignment;
                            TalendItem item = ba.getTalendItem();
                            if (item != null) {
                                assignments.add(item.getId());
                            }
                        }
                        cutItemIds.put(businessItem, assignments);

                    }
                }
            }
        }
    }

    @Override
    protected boolean canCut(IGlobalActionContext cntxt) {
        List elements = getSelectedViews(cntxt.getSelection());
        if (elements.isEmpty()) {
            return false;
        }

        return true;
    }
}
