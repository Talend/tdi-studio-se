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
package org.talend.designer.xmlmap.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.KeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.talend.designer.gefabstractmap.dnd.MapperDragSourceListener;
import org.talend.designer.gefabstractmap.dnd.MapperDropTargetListener;
import org.talend.designer.gefabstractmap.editor.MapperGraphicalEditor;
import org.talend.designer.gefabstractmap.editor.MapperGraphicalViewer;
import org.talend.designer.xmlmap.dnd.XmlDragSourceListener;
import org.talend.designer.xmlmap.dnd.XmlDropTargetListener;
import org.talend.designer.xmlmap.editor.actions.AddChoiceAction;
import org.talend.designer.xmlmap.editor.actions.CreateAttributeAction;
import org.talend.designer.xmlmap.editor.actions.CreateElementAction;
import org.talend.designer.xmlmap.editor.actions.CreateNameSpaceAction;
import org.talend.designer.xmlmap.editor.actions.DeleteTreeNodeAction;
import org.talend.designer.xmlmap.editor.actions.FixValueAction;
import org.talend.designer.xmlmap.editor.actions.ImportTreeFromRepository;
import org.talend.designer.xmlmap.editor.actions.ImportTreeFromXml;
import org.talend.designer.xmlmap.editor.actions.RenameTreeNodeAction;
import org.talend.designer.xmlmap.editor.actions.SetAggregateAction;
import org.talend.designer.xmlmap.editor.actions.SetGroupAction;
import org.talend.designer.xmlmap.editor.actions.SetLoopAction;
import org.talend.designer.xmlmap.editor.actions.SetLoopOptional;
import org.talend.designer.xmlmap.editor.actions.SetSubstitutionAction;
import org.talend.designer.xmlmap.parts.OutputTreeNodeEditPart;
import org.talend.designer.xmlmap.parts.TreeNodeEditPart;
import org.talend.designer.xmlmap.ui.tabs.MapperManager;

/**
 * wchen class global comment. Detailled comment
 */
public class XmlMapEditor extends MapperGraphicalEditor {

    private KeyHandler sharedKeyHandler;

    public XmlMapEditor(MapperManager mapperManager) {
        super(mapperManager);
    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#createActions()
     */
    @Override
    protected void createActions() {

        ImportTreeFromXml importAction = new ImportTreeFromXml(this, getGraphicalViewer().getControl().getShell());
        importAction.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(importAction);
        getSelectionActions().add(importAction.getId());

        CreateAttributeAction createAttribute = new CreateAttributeAction(this, getGraphicalViewer());
        createAttribute.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(createAttribute);
        getSelectionActions().add(createAttribute.getId());

        CreateElementAction createElement = new CreateElementAction(this, getGraphicalViewer());
        createElement.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(createElement);
        getSelectionActions().add(createElement.getId());

        DeleteTreeNodeAction deleteAction = new DeleteTreeNodeAction(this);
        deleteAction.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(deleteAction);
        getSelectionActions().add(deleteAction.getId());

        SetLoopAction loopAction = new SetLoopAction(this);
        loopAction.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(loopAction);
        getSelectionActions().add(loopAction.getId());

        IAction groupAction = new SetGroupAction(this);
        getActionRegistry().registerAction(groupAction);
        getSelectionActions().add(groupAction.getId());

        ImportTreeFromRepository importFromRepository = new ImportTreeFromRepository(this, getGraphicalViewer().getControl()
                .getShell());
        importFromRepository.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(importFromRepository);
        getSelectionActions().add(importFromRepository.getId());

        CreateNameSpaceAction createNameSpaceInput = new CreateNameSpaceAction(this, getGraphicalViewer());
        createNameSpaceInput.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(createNameSpaceInput);
        getSelectionActions().add(createNameSpaceInput.getId());

        FixValueAction fixValueAction = new FixValueAction(this);
        getActionRegistry().registerAction(fixValueAction);
        getSelectionActions().add(fixValueAction.getId());
        fixValueAction.setMapperManager(getMapperManager());

        SetAggregateAction aggreagteAction = new SetAggregateAction(this);
        aggreagteAction.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(aggreagteAction);
        getSelectionActions().add(aggreagteAction.getId());

        RenameTreeNodeAction renameTreeNodeAction = new RenameTreeNodeAction(this);
        renameTreeNodeAction.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(renameTreeNodeAction);
        getSelectionActions().add(renameTreeNodeAction.getId());

        AddChoiceAction createChoice = new AddChoiceAction(this, getGraphicalViewer());
        createChoice.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(createChoice);
        getSelectionActions().add(createChoice.getId());

        SetSubstitutionAction setSubs = new SetSubstitutionAction(this, getGraphicalViewer());
        setSubs.setMapperManager(getMapperManager());
        getActionRegistry().registerAction(setSubs);
        getSelectionActions().add(setSubs.getId());

        SetLoopOptional setOptional = new SetLoopOptional(this);
        getActionRegistry().registerAction(setOptional);
        getSelectionActions().add(setOptional.getId());

    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
     */
    @Override
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
        getGraphicalViewer().setEditPartFactory(new XmlMapPartFactory());

    }

    @Override
    public MapperManager getMapperManager() {
        return (MapperManager) super.getMapperManager();
    }

    @Override
    public void buildContextMenu(IMenuManager menu) {
        // context menu should only display in the document tree
        List selectedEditParts = getGraphicalViewer().getSelectedEditParts();
        if (selectedEditParts != null && !selectedEditParts.isEmpty()) {
            List output = new ArrayList();
            List input = new ArrayList();
            for (Object selected : selectedEditParts) {
                if (selected instanceof OutputTreeNodeEditPart) {
                    output.add(selected);
                } else if (selected instanceof TreeNodeEditPart) {
                    input.add(selected);
                }
            }
            // if selected parts contians input node and also output node , don't show any menu

            if (!output.isEmpty() && !input.isEmpty()) {
                return;
            }
            boolean isInput = !input.isEmpty();
            ImportTreeFromXml importAction = (ImportTreeFromXml) getActionRegistry().getAction(ImportTreeFromXml.ID);
            importAction.update(selectedEditParts);
            importAction.setInput(isInput);
            if (importAction.isEnabled()) {
                menu.add(importAction);
            }

            CreateElementAction createElement = (CreateElementAction) getActionRegistry().getAction(CreateElementAction.ID);
            createElement.update(selectedEditParts);
            createElement.setInput(isInput);
            if (createElement.isEnabled()) {
                menu.add(createElement);
            }

            CreateAttributeAction createAttribute = (CreateAttributeAction) getActionRegistry().getAction(
                    CreateAttributeAction.ID);
            createAttribute.update(selectedEditParts);
            createAttribute.setInput(isInput);
            if (createAttribute.isEnabled()) {
                menu.add(createAttribute);
            }

            CreateNameSpaceAction createNameSpace = (CreateNameSpaceAction) getActionRegistry().getAction(
                    CreateNameSpaceAction.ID);
            createNameSpace.update(selectedEditParts);
            createNameSpace.setInput(isInput);
            if (createNameSpace.isEnabled()) {
                menu.add(createNameSpace);
            }

            FixValueAction fixValueAction = (FixValueAction) getActionRegistry().getAction(FixValueAction.ID);
            fixValueAction.update(selectedEditParts);
            if (fixValueAction.isEnabled()) {
                menu.add(fixValueAction);
            }

            AddChoiceAction addChoice = (AddChoiceAction) getActionRegistry().getAction(AddChoiceAction.ID);
            addChoice.update(selectedEditParts);
            addChoice.setInput(isInput);
            if (addChoice.isEnabled()) {
                menu.add(addChoice);
            }

            SetSubstitutionAction setSubs = (SetSubstitutionAction) getActionRegistry().getAction(SetSubstitutionAction.ID);
            setSubs.update(selectedEditParts);
            setSubs.setInput(isInput);
            if (setSubs.isEnabled()) {
                menu.add(setSubs);
            }

            SetLoopAction loopAction = (SetLoopAction) getActionRegistry().getAction(SetLoopAction.ID);
            loopAction.update(selectedEditParts);
            loopAction.setInput(isInput);
            if (loopAction.isEnabled()) {
                menu.add(loopAction);
            }

            SetLoopOptional loopOptinalAction = (SetLoopOptional) getActionRegistry().getAction(SetLoopOptional.ID);
            loopOptinalAction.update(selectedEditParts);
            if (loopOptinalAction.isEnabled()) {
                menu.add(loopOptinalAction);
            }

            SetGroupAction grouptAction = (SetGroupAction) getActionRegistry().getAction(SetGroupAction.ID);
            grouptAction.update(selectedEditParts);
            if (grouptAction.isEnabled()) {
                menu.add(grouptAction);
            }

            ImportTreeFromRepository importFromRepository = (ImportTreeFromRepository) getActionRegistry().getAction(
                    ImportTreeFromRepository.ID);
            importFromRepository.update(selectedEditParts);
            importFromRepository.setInput(isInput);
            if (importFromRepository.isEnabled()) {
                menu.add(importFromRepository);
            }

            SetAggregateAction aggreateAction = (SetAggregateAction) getActionRegistry().getAction(SetAggregateAction.ID);
            aggreateAction.update(selectedEditParts);
            if (aggreateAction.isEnabled()) {
                menu.add(aggreateAction);
            }

            RenameTreeNodeAction renameTreeNodeAction = (RenameTreeNodeAction) getActionRegistry().getAction(
                    RenameTreeNodeAction.ID);
            renameTreeNodeAction.update(selectedEditParts);
            if (renameTreeNodeAction.isEnabled()) {
                menu.add(renameTreeNodeAction);
            }

            DeleteTreeNodeAction action = (DeleteTreeNodeAction) getActionRegistry().getAction(DeleteTreeNodeAction.ID);
            action.update(selectedEditParts);
            action.setInput(isInput);
            if (action.isEnabled()) {
                menu.add(action);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.editor.MapperGraphicalEditor#createMapperGraphicalViewer()
     */
    @Override
    protected MapperGraphicalViewer createMapperGraphicalViewer() {
        return new XmlMapGraphicViewer();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.editor.MapperGraphicalEditor#createDragSourceListener()
     */
    @Override
    protected MapperDragSourceListener createDragSourceListener() {
        return new XmlDragSourceListener(getGraphicalViewer());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.newabstractmap.editor.MapperGraphicalEditor#createDropTargetListener()
     */
    @Override
    protected MapperDropTargetListener createDropTargetListener() {
        return new XmlDropTargetListener(getGraphicalViewer());
    }

}
