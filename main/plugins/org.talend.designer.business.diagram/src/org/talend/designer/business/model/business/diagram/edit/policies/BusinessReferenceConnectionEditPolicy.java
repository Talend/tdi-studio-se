package org.talend.designer.business.model.business.diagram.edit.policies;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.commands.UnexecutableCommand;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.gmf.runtime.diagram.core.commands.DeleteCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.l10n.DiagramUIMessages;
import org.eclipse.gmf.runtime.diagram.ui.preferences.IPreferenceConstants;
import org.eclipse.gmf.runtime.diagram.ui.requests.EditCommandRequestWrapper;
import org.eclipse.gmf.runtime.diagram.ui.requests.GroupRequestViaKeyboard;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;

/**
 * @generated
 */
public class BusinessReferenceConnectionEditPolicy extends ConnectionEditPolicy {

    /**
     * @generated
     */
    private static final String DELETE_FROM_DIAGRAM_DLG_TITLE = DiagramUIMessages.PromptingDeleteAction_DeleteFromDiagramDialog_Title;

    /**
     * @generated
     */
    private static final String DELETE_FROM_DIAGRAM_DLG_MESSAGE = DiagramUIMessages.PromptingDeleteAction_DeleteFromDiagramDialog_Message;

    /**
     * @generated
     */
    private static final String DELETE_FROM_MODEL_DLG_TOGGLE_LABEL = DiagramUIMessages.MessageDialogWithToggle_DoNotPromptAgainToggle_label;

    /**
     * @generated
     */
    protected final Command getDeleteCommand(GroupRequest deleteRequest) {
        boolean isDeleteFromKeyBoard = deleteRequest instanceof GroupRequestViaKeyboard;
        if (shouldDeleteSemantic()) {
            return createDeleteSemanticCommand(deleteRequest);
        } else {
            boolean proceedToDeleteView = true;
            if (isDeleteFromKeyBoard) {
                GroupRequestViaKeyboard groupRequestViaKeyboard = (GroupRequestViaKeyboard) deleteRequest;
                if (groupRequestViaKeyboard.isShowInformationDialog()) {
                    proceedToDeleteView = showPrompt();
                    groupRequestViaKeyboard.setShowInformationDialog(false);
                    if (!(proceedToDeleteView))
                        return UnexecutableCommand.INSTANCE;
                }
            }
            return createDeleteViewCommand(deleteRequest);
        }
    }

    /**
     * @generated
     */
    protected boolean shouldDeleteSemantic() {
        Assert.isTrue(getHost() instanceof AbstractConnectionEditPart);
        AbstractConnectionEditPart cep = (AbstractConnectionEditPart) getHost();
        boolean isCanonical = false;
        if (cep.getSource() != null)
            isCanonical = IsCanonical(cep.getSource());
        if (cep.getTarget() != null)
            return isCanonical ? isCanonical : IsCanonical(cep.getTarget());
        return isCanonical;
    }

    /**
     * @generated
     */
    private boolean IsCanonical(EditPart ep) {
        EditPart parent = ep.getParent();
        return parent instanceof GraphicalEditPart ? ((GraphicalEditPart) parent).isCanonical() : false;
    }

    /**
     * @generated
     */
    protected Command createDeleteViewCommand(GroupRequest deleteRequest) {
        TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
        return new ICommandProxy(new DeleteCommand(editingDomain, (View) getHost().getModel()));
    }

    /**
     * @generated
     */
    protected Command createDeleteSemanticCommand(GroupRequest deleteRequest) {
        TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost()).getEditingDomain();
        EditCommandRequestWrapper semReq = new EditCommandRequestWrapper(
                new DestroyElementRequest(editingDomain, false), deleteRequest.getExtendedData());
        Command semanticCmd = getHost().getCommand(semReq);
        if (semanticCmd != null && semanticCmd.canExecute()) {
            CompoundCommand cc = new CompoundCommand();
            cc.add(semanticCmd);
            return cc;
        }
        return null;
    }

    /**
     * @generated
     */
    private boolean showPrompt() {
        boolean prompt = ((IPreferenceStore) ((IGraphicalEditPart) getHost()).getDiagramPreferencesHint()
                .getPreferenceStore()).getBoolean(IPreferenceConstants.PREF_PROMPT_ON_DEL_FROM_DIAGRAM);
        if (prompt)
            if (showMessageDialog())
                return true;
            else
                return false;
        return true;
    }

    /**
     * @generated
     */
    private boolean showMessageDialog() {
        MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(Display.getCurrent()
                .getActiveShell(), DELETE_FROM_DIAGRAM_DLG_TITLE, DELETE_FROM_DIAGRAM_DLG_MESSAGE,
                DELETE_FROM_MODEL_DLG_TOGGLE_LABEL, false, (IPreferenceStore) ((IGraphicalEditPart) getHost())
                        .getDiagramPreferencesHint().getPreferenceStore(),
                IPreferenceConstants.PREF_PROMPT_ON_DEL_FROM_DIAGRAM);
        if (dialog.getReturnCode() == IDialogConstants.YES_ID)
            return true;
        else
            return false;
    }

    /**
     * @generated
     */
    public Command getCommand(Request request) {
        if (request instanceof GroupRequestViaKeyboard) {
            return getDeleteCommand((GroupRequest) request);
        }
        return super.getCommand(request);
    }
}
