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
package org.talend.designer.alfrescooutput.ui;

import java.util.List;

import org.dom4j.Element;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.gmf.util.DisplayUtils;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.model.components.IComponent;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.alfrescooutput.AlfrescoOutputManager;
import org.talend.designer.alfrescooutput.data.AlfrescoModelElements;
import org.talend.designer.alfrescooutput.data.AlfrescoOutputModelManager;
import org.talend.designer.alfrescooutput.i18n.Messages;
import org.talend.designer.alfrescooutput.util.AlfrescoOutputException;

/**
 * Dialog allowing to choose typing, i.e. an Alfresco type and Alfresco aspects within Alfresco model definitions.
 *
 * @author Marc Dutoo - Open Wide SA
 *
 * $Id: AlfrescoModelDialog.java,v 1.1 2008/10/07 21:27:31 mdutoo Exp $
 *
 */
public class AlfrescoModelDialog extends Dialog {

    private AlfrescoOutputManager alfrescoOutputManager;

    // model manager :
    private AlfrescoOutputModelManager modelManager;

    private Composite alfrescoModelComposite;

    // UI model :
    private String chosenModelFilePath;

    // UI :
    private Combo typeCombo;

    private ComboViewer typeComboViewer;

    private Table aspectsTable;

    private Table availableAspectsTable;

    private org.eclipse.swt.widgets.List availableModelsList;

    public AlfrescoModelDialog(Shell parentShell, AlfrescoOutputManager alfrescoOutputManager) {
        super(parentShell);
        this.alfrescoOutputManager = alfrescoOutputManager;
        this.modelManager = alfrescoOutputManager.getModelManager();

        // customizing Dialog
        setBlockOnOpen(false);
        setShellStyle(SWT.APPLICATION_MODAL | SWT.BORDER | SWT.RESIZE | SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.TITLE);
    }

    @Override
    protected void okPressed() {
        // saving to model before closing :
        this.modelManager.save();
        super.okPressed();
        setReturnCode(SWT.OK);
    }

    /*
     * (non-Javadoc) Method declared on Dialog.
     */
    protected Control createDialogArea(Composite parent) {
        // create composite
        alfrescoModelComposite = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(3, false);
        layout.marginWidth = 12;
        alfrescoModelComposite.setLayout(layout);

        Label typeLabel = new Label(alfrescoModelComposite, SWT.NULL);
        GridData typeLabelGridData = new GridData(GridData.FILL_BOTH);
        typeLabelGridData.horizontalSpan = 3;
        typeLabel.setLayoutData(typeLabelGridData);
        typeLabel.setText(Messages.getString("AlfrescoModelDialog.type")); //$NON-NLS-1$
        typeCombo = new Combo(alfrescoModelComposite, SWT.NULL);
        GridData typeComboGridData = new GridData(GridData.FILL_BOTH);
        typeComboGridData.horizontalSpan = 3;
        typeCombo.setLayoutData(typeComboGridData);
        typeComboViewer = new ComboViewer(typeCombo);
        typeComboViewer.setContentProvider(new AlfrescoModelContentProvider() {

            public void alfrescoModelElementAdded(Element alfrescoModelElement) {
                typeComboViewer.add(alfrescoModelElement);
            }

            public void alfrescoModelElementRemoved(Element alfrescoModelElement) {
                typeComboViewer.remove(alfrescoModelElement);
            }
        });
        typeComboViewer.setLabelProvider(new AlfrescoModelLabelProvider());
        typeComboViewer.setInput(modelManager.getAvailableTypes());
        typeCombo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                ISelection selection = typeComboViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    Object selected = ((IStructuredSelection) selection).getFirstElement();
                    if (selected instanceof Element) {
                        Element newType = (Element) selected;
                        if (modelManager.getType() == null
                                || !newType.attributeValue("name").equals(modelManager.getType().attributeValue("name"))) { //$NON-NLS-1$ //$NON-NLS-2$
                            modelManager.setType(newType);
                            AlfrescoModelDialog.this.updateMetadata();
                        }
                    } else if (modelManager.getType() != null) {
                        modelManager.setType(null);
                        AlfrescoModelDialog.this.updateMetadata();
                    }
                }
            }
        });

        Label aspectsLabel = new Label(alfrescoModelComposite, SWT.NULL);
        aspectsLabel.setText(Messages.getString("AlfrescoModelDialog.aspects")); //$NON-NLS-1$

        new Composite(alfrescoModelComposite, SWT.NULL); // filler

        Label availableAspectsLabel = new Label(alfrescoModelComposite, SWT.NULL);
        availableAspectsLabel.setText(Messages.getString("AlfrescoModelDialog.availableAspects")); //$NON-NLS-1$

        // create table
        aspectsTable = createAlfrescoModelElementTable(alfrescoModelComposite, modelManager.getAspects());
        GridData aspectsTableGridData = new GridData(GridData.FILL_BOTH);
        // aspectsTableGridData.grabExcessVerticalSpace = true;
        // aspectsTableGridData.grabExcessHorizontalSpace = true;
        aspectsTableGridData.heightHint = 300;
        aspectsTable.setLayoutData(aspectsTableGridData);

        Composite aspectButtonsComposite = new Composite(alfrescoModelComposite, SWT.NULL);
        GridLayout aspectButtonsLayout = new GridLayout();
        aspectButtonsLayout.marginWidth = 12;
        aspectButtonsComposite.setLayout(aspectButtonsLayout);

        Button addAspectButton = new Button(aspectButtonsComposite, SWT.NULL);
        addAspectButton.setText("+"); //$NON-NLS-1$
        addAspectButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                TableItem[] selectedItems = availableAspectsTable.getSelection();
                for (int i = 0; i < selectedItems.length; i++) {
                    Object selectedData = selectedItems[i].getData();
                    if (selectedData instanceof Element) {
                        Element alfrescoModelElement = (Element) selectedData;
                        modelManager.addAspect(alfrescoModelElement);
                    }
                }
                AlfrescoModelDialog.this.updateMetadata();
            }
        });
        Button removeAspectButton = new Button(aspectButtonsComposite, SWT.NULL);
        removeAspectButton.setText("-"); //$NON-NLS-1$
        removeAspectButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                TableItem[] selectedItems = aspectsTable.getSelection();
                for (int i = 0; i < selectedItems.length; i++) {
                    Object selectedData = selectedItems[i].getData();
                    if (selectedData instanceof Element) {
                        Element alfrescoModelElement = (Element) selectedData;
                        modelManager.removeAspect(alfrescoModelElement);
                    }
                }
                AlfrescoModelDialog.this.updateMetadata();
            }
        });

        // create table
        availableAspectsTable = createAlfrescoModelElementTable(alfrescoModelComposite, modelManager.getAvailableAspects());
        // RowData availableAspectsTableRowData = new RowData();
        // availableAspectsTableRowData.height = 200;
        // availableAspectsTable.setLayoutData(availableAspectsTableRowData);
        GridData availableAspectsTableGridData = new GridData(GridData.FILL_BOTH);
        // availableAspectsTableGridData.grabExcessVerticalSpace = true;
        // availableAspectsTableGridData.grabExcessHorizontalSpace = true;
        availableAspectsTableGridData.heightHint = 300;
        availableAspectsTable.setLayoutData(availableAspectsTableGridData);

        Label availableModelsLabel = new Label(alfrescoModelComposite, SWT.NULL);
        GridData availableModelsLabelGridData = new GridData(GridData.FILL_BOTH);
        availableModelsLabelGridData.horizontalSpan = 3;
        availableModelsLabel.setLayoutData(availableModelsLabelGridData);
        availableModelsLabel.setText("Available Models"); //$NON-NLS-1$
        availableModelsList = new org.eclipse.swt.widgets.List(alfrescoModelComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData availableModelsListGridData = new GridData(GridData.FILL_BOTH);
        // availableModelsListGridData.grabExcessVerticalSpace = true;
        // availableModelsListGridData.grabExcessHorizontalSpace = true;
        availableModelsListGridData.horizontalSpan = 3;
        availableModelsListGridData.heightHint = 200;
        availableModelsList.setLayoutData(availableModelsListGridData);
        availableModelsList.setItems(new String[0]); // init

        Button addAvailableModelButton = new Button(alfrescoModelComposite, SWT.NULL);
        addAvailableModelButton.setText(Messages.getString("AlfrescoModelDialog.add")); //$NON-NLS-1$
        addAvailableModelButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                // opens a dialog to select the model file
                FileDialog dialog = new FileDialog(alfrescoModelComposite.getShell(), SWT.OPEN); // shell
                dialog.setFileName(AlfrescoModelDialog.this.chosenModelFilePath);
                dialog.setFilterExtensions(new String[] { "*.xml", "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
                AlfrescoModelDialog.this.chosenModelFilePath = dialog.open();
                if (AlfrescoModelDialog.this.chosenModelFilePath == null) {
                    return;
                }
                try {
                    modelManager.addModel(AlfrescoModelDialog.this.chosenModelFilePath);
                } catch (AlfrescoOutputException aoex) {
                    MessageDialog.openError(DisplayUtils.getDefaultShell(false), Messages
                            .getString("AlfrescoModelDialog.addModelFailed"), aoex.getMessage()); //$NON-NLS-1$
                    return;
                }
                // let's refresh the list :
                availableModelsList.setItems(modelManager.getAvailableModels().toArray(new String[0]));
                AlfrescoModelDialog.this.updateMetadata();
            }
        });
        Button removeAvailableModelButton = new Button(alfrescoModelComposite, SWT.NULL);
        removeAvailableModelButton.setText(Messages.getString("AlfrescoModelDialog.remove")); //$NON-NLS-1$
        removeAvailableModelButton.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event e) {
                String[] selectedItems = availableModelsList.getSelection();
                for (String selectedItem : selectedItems) {
                    try {
                        modelManager.removeModel(selectedItem);
                    } catch (AlfrescoOutputException aoex) {
                        MessageDialog.openError(DisplayUtils.getDefaultShell(false), Messages
                                .getString("AlfrescoModelDialog.removeModelFailed"), aoex.getMessage()); //$NON-NLS-1$
                        return;
                    }
                    // let's refresh the list :
                    availableModelsList.setItems(modelManager.getAvailableModels().toArray(new String[0]));
                }
                AlfrescoModelDialog.this.updateMetadata();
            }
        });

        // complete init (all other views are in sync with the model) :
        // type
        Element type = modelManager.getType();
        if (type != null) {
            this.typeComboViewer.setSelection(new StructuredSelection(type));
        } else {
            this.typeComboViewer.setSelection(new StructuredSelection());
        }
        // available models
        availableModelsList.setItems(modelManager.getAvailableModels().toArray(new String[0]));
        // chosen model filepath
        if (!this.modelManager.getAvailableModels().isEmpty()) {
            this.chosenModelFilePath = this.modelManager.getAvailableModels().get(
                    this.modelManager.getAvailableModels().size() - 1);
        }

        applyDialogFont(alfrescoModelComposite);
        return alfrescoModelComposite;
    }

    protected void updateMetadata() {
        modelManager.getMetadataManager().updateMetadata();
        List<String> missingTypeNames = modelManager.getMetadataManager().getMissingTypeNames();
        List<String> missingAspectNames = modelManager.getMetadataManager().getMissingAspectNames();
        if (!missingTypeNames.isEmpty()) {
            MessageDialog.openError(DisplayUtils.getDefaultShell(false), Messages
                    .getString("AlfrescoModelDialog.missingTypeDefinitions.title"), Messages.getString( //$NON-NLS-1$
                    "AlfrescoModelDialog.missingTypeDefinitions.msg", missingTypeNames)); //$NON-NLS-1$
        }
        if (!missingAspectNames.isEmpty()) {
            MessageDialog.openError(DisplayUtils.getDefaultShell(false),
                    Messages.getString("AlfrescoModelDialog.missingAspectDefinitions.title"), Messages.getString( //$NON-NLS-1$
                            "AlfrescoModelDialog.missingAspectDefinitions.msg", missingAspectNames)); //$NON-NLS-1$
        }
        // NB. model is only saved on closing the dialog with OK
    }

    private Table createAlfrescoModelElementTable(Composite composite, AlfrescoModelElements modelElements) {
        Table table = new Table(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);

        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        TableColumn titleColumn = new TableColumn(table, SWT.LEFT, 0);
        titleColumn.setText(Messages.getString("AlfrescoModelDialog.modelTable.title")); //$NON-NLS-1$
        titleColumn.setWidth(300);
        TableColumn nameColumn = new TableColumn(table, SWT.LEFT, 1);
        nameColumn.setText(Messages.getString("AlfrescoModelDialog.modelTable.name")); //$NON-NLS-1$
        nameColumn.setWidth(300);

        // createTableViewer
        final TableViewer tableViewer = new TableViewer(table);
        tableViewer.setUseHashlookup(true);
        String[] tableColumnNames = new String[] { Messages.getString("AlfrescoModelDialog.modelTable.title"), //$NON-NLS-1$
                Messages.getString("AlfrescoModelDialog.modelTable.name") }; //$NON-NLS-1$
        tableViewer.setColumnProperties(tableColumnNames);

        // NB. no cell editors for tableViewer
        tableViewer.setContentProvider(new AlfrescoModelContentProvider() {

            public void alfrescoModelElementAdded(Element alfrescoModelElement) {
                tableViewer.add(alfrescoModelElement);
            }

            public void alfrescoModelElementRemoved(Element alfrescoModelElement) {
                tableViewer.remove(alfrescoModelElement);
            }
        });
        tableViewer.setLabelProvider(new AlfrescoModelTableLabelProvider());
        tableViewer.setInput(modelElements);

        return table;

    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        IComponent component = alfrescoOutputManager.getAlfrescoOutputComponent().getComponent();
        Image createImage = CoreImageProvider.getComponentIcon(component, ICON_SIZE.ICON_32);
        newShell.setImage(createImage);
        newShell.setText(alfrescoOutputManager.getAlfrescoOutputComponent().getUniqueName());
        Rectangle boundsRG = new Rectangle(50, 50, 800, 600);
        newShell.setBounds(boundsRG);
        // newShell.setMinimumSize(600, 600);
    }

}
