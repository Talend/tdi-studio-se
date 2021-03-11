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
package org.talend.designer.xmlmap.ui.tabs;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.ILineSelectionListener;
import org.talend.commons.ui.runtime.swt.tableviewer.selection.LineSelectionEvent;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton;
import org.talend.commons.ui.swt.colorstyledtext.UnnotifiableColorStyledText;
import org.talend.commons.ui.swt.extended.table.ExtendedButtonEvent;
import org.talend.commons.ui.swt.extended.table.IExtendedButtonListener;
import org.talend.commons.ui.swt.tableviewer.TableViewerCreator;
import org.talend.core.CorePlugin;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.core.ui.metadata.editor.MetadataToolbarEditorView;
import org.talend.designer.xmlmap.i18n.Messages;
import org.talend.designer.xmlmap.ui.tabs.table.OutputXmlTreeSchemaTableView;
import org.talend.designer.xmlmap.ui.tabs.table.XmlMapMetadataTableEditorView;
import org.talend.designer.xmlmap.ui.tabs.table.XmlTreeSchemaTableView;
import org.talend.designer.xmlmap.util.XmlMapUtil;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: TabFolderEditors.java 39333 2010-03-30 03:02:10Z wchen $
 *
 */
public class TabFolderEditors extends CTabFolder {

    private TabFolderEditors tabFolderEditors;

    protected int lastSelectedTab;

    private MetadataTableEditorView inputMetaEditor;

    private MetadataTableEditorView outputMetaEditor;

    private XmlTreeSchemaTableView inputTreeSchemaEditor;

    private XmlTreeSchemaTableView outputTreeSchemaEditor;

    public static final int INDEX_TAB_METADATA_EDITOR = 0;

    public static final int INDEX_TAB_EXPRESSION_EDITOR = 1;

    private IExtendedButtonListener beforeCommandListenerForInputButtons;

    private List<ExtendedPushButton> inputToolBarButtons;

    private List<ExtendedPushButton> outputToolBarButtons;

    private IExtendedButtonListener beforeCommandListenerForOutputButtons;

    private RemovePushButtonForExtendedTable removeButton;

    private MapperManager mapperManage;

    private StyledTextHandler styledTextHandler;

    public TabFolderEditors(Composite parent, MapperManager mapperManager, int style) {
        super(parent, style);
        tabFolderEditors = this;
        this.mapperManage = mapperManager;
        createComponents();
    }

    /**
     * DOC amaumont Comment method "createComponents".
     */
    private void createComponents() {

        setSimple(false);
        // TableEditorCompositeBase metaDatasDescriptorView3 = new TableEditorCompositeBase(tabFolder1);
        // item.setControl(metaDatasDescriptorView3);

        CTabItem item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.SchemaEditor.Title")); //$NON-NLS-1$

        SashForm inOutMetaEditorContainer = new SashForm(tabFolderEditors, SWT.SMOOTH | SWT.HORIZONTAL | SWT.SHADOW_OUT);
        inOutMetaEditorContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
        item.setControl(inOutMetaEditorContainer);

        // input metadata table view
        inputMetaEditor = new XmlMapMetadataTableEditorView(inOutMetaEditorContainer, SWT.BORDER);
        inputMetaEditor.initGraphicComponents();
        // inputMetaEditor.getExtendedTableViewer().setCommandStack(commandStack);
        ILineSelectionListener metadataEditorViewerSelectionChangedListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                if (inputMetaEditor.getTableViewerCreator() == e.source && mapperManage.getGraphicalViewer() != null) {
                    if (inputMetaEditor.getExtendedTableViewer().isExecuteSelectionEvent()) {
                        mapperManage.selectLinkedInputTableEntries(inputMetaEditor.getTableViewerCreator().getTable()
                                .getSelectionIndices(), false);

                    }
                }
            }

        };

        inputMetaEditor.getTableViewerCreator().getSelectionHelper()
                .addAfterSelectionListener(metadataEditorViewerSelectionChangedListener);

        addListenersToInputButtons();

        // output metadata table view
        outputMetaEditor = new XmlMapMetadataTableEditorView(inOutMetaEditorContainer, SWT.BORDER);
        outputMetaEditor.initGraphicComponents();
        // outputMetaEditor.getExtendedTableViewer().setCommandStack(commandStack);
        metadataEditorViewerSelectionChangedListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                if (outputMetaEditor.getTableViewerCreator() == e.source && mapperManage.getGraphicalViewer() != null) {
                    if (outputMetaEditor.getExtendedTableViewer().isExecuteSelectionEvent()) {
                        mapperManage.selectLinkedOutputTableEntries(outputMetaEditor.getTableViewerCreator().getTable()
                                .getSelectionIndices(), false);

                    }
                }
            }

        };
        outputMetaEditor.getTableViewerCreator().getSelectionHelper()
                .addAfterSelectionListener(metadataEditorViewerSelectionChangedListener);

        addListenersToOutputButtons();

        item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.TreeSchemaEditor.Title")); //$NON-NLS-1$

        SashForm xmlTreeEditorContainer = new SashForm(tabFolderEditors, SWT.SMOOTH | SWT.HORIZONTAL | SWT.SHADOW_OUT);
        xmlTreeEditorContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
        item.setControl(xmlTreeEditorContainer);
        // input tree schema table view
        inputTreeSchemaEditor = new XmlTreeSchemaTableView(xmlTreeEditorContainer, SWT.BORDER);
        inputTreeSchemaEditor.initGraphicComponents();

        metadataEditorViewerSelectionChangedListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                if (inputTreeSchemaEditor.getTableViewerCreator() == e.source && mapperManage.getGraphicalViewer() != null) {
                    if (inputTreeSchemaEditor.getExtendedTableViewer().isExecuteSelectionEvent()) {
                        mapperManage.selectLinkedInputTableEntries(inputTreeSchemaEditor.getTableViewerCreator().getTable()
                                .getSelectionIndices(), true);

                    }
                }
            }

        };
        inputTreeSchemaEditor.getTableViewerCreator().getSelectionHelper()
                .addAfterSelectionListener(metadataEditorViewerSelectionChangedListener);

        // output tree schema table view
        outputTreeSchemaEditor = new OutputXmlTreeSchemaTableView(xmlTreeEditorContainer, SWT.BORDER);
        outputTreeSchemaEditor.initGraphicComponents();

        metadataEditorViewerSelectionChangedListener = new ILineSelectionListener() {

            @Override
            public void handle(LineSelectionEvent e) {
                if (outputTreeSchemaEditor.getTableViewerCreator() == e.source && mapperManage.getGraphicalViewer() != null) {
                    if (outputTreeSchemaEditor.getExtendedTableViewer().isExecuteSelectionEvent()) {
                        mapperManage.selectLinkedOutputTableEntries(outputTreeSchemaEditor.getTableViewerCreator().getTable()
                                .getSelectionIndices(), true);

                    }
                }
            }

        };
        outputTreeSchemaEditor.getTableViewerCreator().getSelectionHelper()
                .addAfterSelectionListener(metadataEditorViewerSelectionChangedListener);

        item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.ExpressionEditor.Title")); //$NON-NLS-1$

        StyledText styledText = createStyledText(item);

        this.styledTextHandler = new StyledTextHandler(styledText, mapperManage);

        tabFolderEditors.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                mapperManage.fireCurrentDirectEditApply();
                // TDI-18185
                if (styledTextHandler.getSelectedNode() != null
                        && XmlMapUtil.DOCUMENT.equals(styledTextHandler.getSelectedNode().getType())) {
                    styledTextHandler.setTextWithoutNotifyListeners("");
                    styledTextHandler.getStyledText().setEnabled(false);
                    styledTextHandler.getStyledText().setEditable(false);
                    styledTextHandler.getStyledText().setText("");
                }
                lastSelectedTab = tabFolderEditors.getSelectionIndex();

            }
        });
        tabFolderEditors.setSelection(0);
    }

    /**
     * DOC amaumont Comment method "addListenersToInputButtons".
     */
    private void addListenersToInputButtons() {
        MetadataToolbarEditorView toolBar = inputMetaEditor.getToolBar();
        inputToolBarButtons = toolBar.getButtons();
        beforeCommandListenerForInputButtons = new IExtendedButtonListener() {

            @Override
            public void handleEvent(ExtendedButtonEvent event) {
                // TableViewerCreator tableViewerCreator =
                // mapperManager.getUiManager().getCurrentSelectedInputTableView()
                // .getTableViewerCreatorForColumns();
                // if (tableViewerCreator != null) {
                // tableViewerCreator.applyActivatedCellEditor();
                // }
            }

        };

        for (ExtendedPushButton extendedPushButton : inputToolBarButtons) {
            extendedPushButton.addListener(beforeCommandListenerForInputButtons, true);
        }

        this.addDisposeListener(new DisposeListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
            @Override
            public void widgetDisposed(DisposeEvent e) {
                for (ExtendedPushButton extendedPushButton : inputToolBarButtons) {
                    extendedPushButton.removeListener(beforeCommandListenerForInputButtons, true);
                }

            }

        });
    }

    /**
     * DOC amaumont Comment method "addListenersToInputButtons".
     */
    private void addListenersToOutputButtons() {
        MetadataToolbarEditorView toolBar = outputMetaEditor.getToolBar();
        outputToolBarButtons = toolBar.getButtons();
        beforeCommandListenerForOutputButtons = new IExtendedButtonListener() {

            @Override
            public void handleEvent(ExtendedButtonEvent event) {
                // TableViewerCreator tableViewerCreator =
                // mapperManager.getUiManager().getCurrentSelectedOutputTableView()
                // .getTableViewerCreatorForColumns();
                // if (tableViewerCreator != null) {
                // tableViewerCreator.applyActivatedCellEditor();
                // }
            }

        };

        for (ExtendedPushButton extendedPushButton : outputToolBarButtons) {
            extendedPushButton.addListener(beforeCommandListenerForOutputButtons, true);
            if (extendedPushButton instanceof RemovePushButton) {
                removeButton = (RemovePushButtonForExtendedTable) extendedPushButton;
            }
        }

        if (removeButton != null) {
            final TableViewerCreator tableViewerCreator = removeButton.getExtendedTableViewer().getTableViewerCreator();
            tableViewerCreator.getSelectionHelper().addAfterSelectionListener(new ILineSelectionListener() {

                @Override
                public void handle(LineSelectionEvent e) {

                    for (TableItem item : tableViewerCreator.getTable().getSelection()) {
                        if (item.getData() instanceof MetadataColumn) {
                            MetadataColumn column = (MetadataColumn) item.getData();
                            removeButton.getButton().setEnabled(!column.isCustom());
                            break;
                        }
                    }

                }

            });
        }

        this.addDisposeListener(new DisposeListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
            @Override
            public void widgetDisposed(DisposeEvent e) {
                for (ExtendedPushButton extendedPushButton : outputToolBarButtons) {
                    extendedPushButton.removeListener(beforeCommandListenerForOutputButtons, true);
                }

            }

        });
    }

    private StyledText createStyledText(CTabItem item) {
        StyledText styledText = null;

        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        ECodeLanguage language = repositoryContext.getProject().getLanguage();
        IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
        styledText = new UnnotifiableColorStyledText(tabFolderEditors, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, preferenceStore,
                language.getName());
        styledText.setEnabled(false);
        item.setControl(styledText);
        return styledText;
    }

    public MetadataTableEditorView getInputMetaEditorView() {
        return this.inputMetaEditor;
    }

    public MetadataTableEditorView getOutputMetaEditorView() {
        return this.outputMetaEditor;
    }

    public XmlTreeSchemaTableView getInputTreeSchemaEditor() {
        return inputTreeSchemaEditor;
    }

    public XmlTreeSchemaTableView getOutputTreeSchemaEditor() {
        return outputTreeSchemaEditor;
    }

    public StyledTextHandler getStyledTextHandler() {
        return this.styledTextHandler;
    }

    public int getLastSelectedTab() {
        return this.lastSelectedTab;
    }

}
