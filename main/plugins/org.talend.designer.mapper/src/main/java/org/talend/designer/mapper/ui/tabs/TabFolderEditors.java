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
package org.talend.designer.mapper.ui.tabs;

import java.util.List;

import org.eclipse.gef.commands.CommandStack;
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
import org.talend.core.model.utils.NodeUtil;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.core.ui.metadata.editor.MetadataToolbarEditorView;
import org.talend.designer.mapper.MapperMain;
import org.talend.designer.mapper.i18n.Messages;
import org.talend.designer.mapper.managers.MapperManager;
import org.talend.designer.mapper.model.table.InputTable;
import org.talend.designer.mapper.model.table.OutputTable;
import org.talend.designer.mapper.ui.visualmap.table.DataMapTableView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class TabFolderEditors extends CTabFolder {

    private TabFolderEditors tabFolderEditors;

    private MapperManager mapperManager;

    protected int lastSelectedTab;

    private MetadataTableEditorView inputMetaEditor;

    private MetadataTableEditorView outputMetaEditor;

    public static final int INDEX_TAB_METADATA_EDITOR = 0;

    public static final int INDEX_TAB_EXPRESSION_EDITOR = 1;

    private StyledTextHandler styledTextHandler;

    private IExtendedButtonListener beforeCommandListenerForInputButtons;

    private List<ExtendedPushButton> inputToolBarButtons;

    private List<ExtendedPushButton> outputToolBarButtons;

    private IExtendedButtonListener beforeCommandListenerForOutputButtons;

    private RemovePushButtonForExtendedTable removeButton;

    public TabFolderEditors(Composite parent, int style, MapperManager mapperManager) {
        super(parent, style);
        tabFolderEditors = this;
        this.mapperManager = mapperManager;
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
        item.setText(Messages.getString("TabFolderEditors.schemaEditor")); //$NON-NLS-1$

        SashForm inOutMetaEditorContainer = new SashForm(tabFolderEditors, SWT.SMOOTH | SWT.HORIZONTAL | SWT.SHADOW_OUT);
        inOutMetaEditorContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
        item.setControl(inOutMetaEditorContainer);

        CommandStack commandStack = mapperManager.getCommandStack();

        inputMetaEditor = new MetadataTableEditorView(inOutMetaEditorContainer, SWT.BORDER);
        inputMetaEditor.setMetadataTalendTypeFilter(NodeUtil.createMetadataTalendTypeFilter(mapperManager
                .getAbstractMapComponent()));
        inputMetaEditor.initGraphicComponents();
        inputMetaEditor.getExtendedTableViewer().setCommandStack(commandStack);

        addListenersToInputButtons();

        outputMetaEditor = new MetadataTableEditorView(inOutMetaEditorContainer, SWT.BORDER);
        outputMetaEditor.setMetadataTalendTypeFilter(NodeUtil.createMetadataTalendTypeFilter(mapperManager
                .getAbstractMapComponent()));
        outputMetaEditor.initGraphicComponents();
        outputMetaEditor.getExtendedTableViewer().setCommandStack(commandStack);

        addListenersToOutputButtons();

        item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.expressionEditor")); //$NON-NLS-1$

        StyledText styledText = createStyledText(item);

        this.styledTextHandler = new StyledTextHandler(styledText, mapperManager);

        tabFolderEditors.addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
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

            public void handleEvent(ExtendedButtonEvent event) {
                TableViewerCreator tableViewerCreator = mapperManager.getUiManager().getCurrentSelectedInputTableView()
                        .getTableViewerCreatorForColumns();
                if (tableViewerCreator != null) {
                    tableViewerCreator.applyActivatedCellEditor();
                }
            }

        };

        IExtendedButtonListener afterCommandListener = new IExtendedButtonListener() {

            public void handleEvent(ExtendedButtonEvent event) {
                List<InputTable> inputTablesList = mapperManager.getInputTables();
                for (InputTable inputTable : inputTablesList) {
                    DataMapTableView view = mapperManager.retrieveAbstractDataMapTableView(inputTable);
                    view.getTableViewerCreatorForColumns().getTableViewer().refresh();
                }
            }
        };

        for (ExtendedPushButton extendedPushButton : inputToolBarButtons) {
            extendedPushButton.addListener(beforeCommandListenerForInputButtons, true);
            extendedPushButton.addListener(afterCommandListener, false);
        }

        this.addDisposeListener(new DisposeListener() {

            /*
             * (non-Javadoc)
             *
             * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
             */
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

            public void handleEvent(ExtendedButtonEvent event) {
                TableViewerCreator tableViewerCreator = mapperManager.getUiManager().getCurrentSelectedOutputTableView()
                        .getTableViewerCreatorForColumns();
                if (tableViewerCreator != null) {
                    tableViewerCreator.applyActivatedCellEditor();
                }
            }

        };

        IExtendedButtonListener afterCommandListener = new IExtendedButtonListener() {

            public void handleEvent(ExtendedButtonEvent event) {
                List<OutputTable> outputTablesList = mapperManager.getOutputTables();
                for (OutputTable outputTable : outputTablesList) {
                    DataMapTableView view = mapperManager.retrieveAbstractDataMapTableView(outputTable);
                    view.getTableViewerCreatorForColumns().getTableViewer().refresh();
                }
            }
        };

        for (ExtendedPushButton extendedPushButton : outputToolBarButtons) {
            extendedPushButton.addListener(beforeCommandListenerForOutputButtons, true);
            extendedPushButton.addListener(afterCommandListener, false);
            if (extendedPushButton instanceof RemovePushButton && !mapperManager.componentIsReadOnly()) {
                removeButton = (RemovePushButtonForExtendedTable) extendedPushButton;
            }
        }

        if (removeButton != null) {
            final TableViewerCreator tableViewerCreator = removeButton.getExtendedTableViewer().getTableViewerCreator();
            tableViewerCreator.getSelectionHelper().addAfterSelectionListener(new ILineSelectionListener() {

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
            public void widgetDisposed(DisposeEvent e) {
                for (ExtendedPushButton extendedPushButton : outputToolBarButtons) {
                    extendedPushButton.removeListener(beforeCommandListenerForOutputButtons, true);
                }

            }

        });
    }

    private StyledText createStyledText(CTabItem item) {
        StyledText styledText = null;
        if (MapperMain.isStandAloneMode()) {
            styledText = new StyledText(tabFolderEditors, SWT.V_SCROLL | SWT.H_SCROLL);
        } else {
            RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                    Context.REPOSITORY_CONTEXT_KEY);
            ECodeLanguage language = repositoryContext.getProject().getLanguage();
            IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
            // styledText = new ColorStyledText(tabFolderEditors, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL,
            // colorManager, language.getName());
            styledText = new UnnotifiableColorStyledText(tabFolderEditors, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL,
                    preferenceStore, language.getName());
        }
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

    public StyledTextHandler getStyledTextHandler() {
        return this.styledTextHandler;
    }

}
