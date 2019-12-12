// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.dbmap.ui.tabs;

import java.util.List;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.talend.commons.ui.swt.advanced.dataeditor.control.ExtendedPushButton;
import org.talend.commons.ui.swt.colorstyledtext.UnnotifiableColorStyledText;
import org.talend.commons.ui.swt.extended.table.ExtendedButtonEvent;
import org.talend.commons.ui.swt.extended.table.IExtendedButtonListener;
import org.talend.core.CorePlugin;
import org.talend.core.ui.metadata.editor.MetadataTableEditorView;
import org.talend.core.ui.metadata.editor.MetadataToolbarEditorView;
import org.talend.designer.dbmap.MapperMain;
import org.talend.designer.dbmap.i18n.Messages;
import org.talend.designer.dbmap.managers.MapperManager;
import org.talend.designer.dbmap.model.table.InputTable;
import org.talend.designer.dbmap.model.table.OutputTable;
import org.talend.designer.dbmap.ui.visualmap.table.DataMapTableView;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: TabFolderEditors.java 2053 2007-02-13 15:41:54Z amaumont $
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

    private StyledText styledSqlText;

    private String dbmsId;

    private List<ExtendedPushButton> inputToolBarButtons;

    private List<ExtendedPushButton> outputToolBarButtons;

    public TabFolderEditors(Composite parent, int style, MapperManager mapperManager, String dbmsId) {
        super(parent, style);
        tabFolderEditors = this;
        this.mapperManager = mapperManager;
        this.dbmsId = dbmsId;
        createComponents();
    }

    public TabFolderEditors(Composite parent, int style, MapperManager mapperManager) {
        this(parent, style, mapperManager, null);
    }


    /**
     * DOC amaumont Comment method "createComponents".
     */
    private void createComponents() {

        setSimple(false);
        // TableEditorCompositeBase metaDatasDescriptorView3 = new
        // TableEditorCompositeBase(tabFolder1);
        // item.setControl(metaDatasDescriptorView3);

        createMetadataEditorTab();

        createExpressionEditorTab();

        createSqlViewerTab();

        tabFolderEditors.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                lastSelectedTab = tabFolderEditors.getSelectionIndex();
                mapperManager.getUiManager().refreshSqlExpression();
            }
        });
        tabFolderEditors.setSelection(0);
    }

    /**
     * DOC amaumont Comment method "createSqlViewerTab".
     */
    private void createSqlViewerTab() {
        CTabItem item;
        item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.SqlSelectQuery", new Object[] { "" })); //$NON-NLS-1$ //$NON-NLS-2$
        styledSqlText = createStyledText(item);
        styledSqlText.setWordWrap(true);
        styledSqlText.setEditable(false);
        styledSqlText.setEnabled(true);
    }

    /**
     * DOC amaumont Comment method "createExpressionEditorTab".
     */
    private void createExpressionEditorTab() {
        CTabItem item;
        item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.expressionEditor")); //$NON-NLS-1$
        StyledText styledText = createStyledText(item);
        this.styledTextHandler = new StyledTextHandler(styledText, mapperManager);
    }

    /**
     * DOC amaumont Comment method "createMetadataEditor".
     */
    private void createMetadataEditorTab() {
        CTabItem item = new CTabItem(tabFolderEditors, SWT.BORDER);
        item.setText(Messages.getString("TabFolderEditors.schemaEditor")); //$NON-NLS-1$

        SashForm inOutMetaEditorContainer = new SashForm(tabFolderEditors, SWT.SMOOTH | SWT.HORIZONTAL | SWT.SHADOW_OUT);
        inOutMetaEditorContainer.setLayout(new RowLayout(SWT.HORIZONTAL));
        item.setControl(inOutMetaEditorContainer);

        CommandStack commandStack = mapperManager.getCommandStack();

        inputMetaEditor = new MetadataTableEditorView(inOutMetaEditorContainer, SWT.BORDER);
        inputMetaEditor.setCurrentDbms(dbmsId);
        inputMetaEditor.setShowDbTypeColumn(true, false, true);
        inputMetaEditor.setShowDbColumnName(true, false);
        inputMetaEditor.setShowPatternColumn(false);
        inputMetaEditor.setShowTalendTypeColumn(false);
        inputMetaEditor.initGraphicComponents();
        inputMetaEditor.getExtendedTableViewer().setCommandStack(commandStack);

        addListenersToInputButtons();

        outputMetaEditor = new MetadataTableEditorView(inOutMetaEditorContainer, SWT.BORDER);
        outputMetaEditor.setCurrentDbms(dbmsId);
        outputMetaEditor.setShowDbTypeColumn(true, false, true);
        outputMetaEditor.setShowDbColumnName(true, false);
        outputMetaEditor.setShowTalendTypeColumn(false);
        outputMetaEditor.setShowPatternColumn(false);
        outputMetaEditor.initGraphicComponents();
        outputMetaEditor.getExtendedTableViewer().setCommandStack(commandStack);

        addListenersToOutputButtons();
    }

    private void addListenersToInputButtons() {
        MetadataToolbarEditorView toolBar = inputMetaEditor.getToolBar();
        inputToolBarButtons = toolBar.getButtons();
        IExtendedButtonListener afterCommandListener = new IExtendedButtonListener() {

            public void handleEvent(ExtendedButtonEvent event) {
                List<InputTable> inputTablesList = mapperManager.getInputTables();
                for (InputTable inputTable : inputTablesList) {
                    DataMapTableView view = mapperManager.retrieveIDataMapTableView(inputTable);
                    view.getTableViewerCreatorForColumns().getTableViewer().refresh();
                }
            }
        };

        for (ExtendedPushButton extendedPushButton : inputToolBarButtons) {
            extendedPushButton.addListener(afterCommandListener, false);
        }
    }

    private void addListenersToOutputButtons() {
        MetadataToolbarEditorView toolBar = outputMetaEditor.getToolBar();
        outputToolBarButtons = toolBar.getButtons();
        IExtendedButtonListener afterCommandListener = new IExtendedButtonListener() {

            public void handleEvent(ExtendedButtonEvent event) {
                List<OutputTable> outputTablesList = mapperManager.getOutputTables();
                for (OutputTable outputTable : outputTablesList) {
                    DataMapTableView view = mapperManager.retrieveIDataMapTableView(outputTable);
                    view.getTableViewerCreatorForColumns().getTableViewer().refresh();
                }
            }
        };

        for (ExtendedPushButton extendedPushButton : outputToolBarButtons) {
            extendedPushButton.addListener(afterCommandListener, false);
        }
    }

    private StyledText createStyledText(CTabItem item) {
        StyledText styledText = null;
        if (MapperMain.isStandAloneMode()) {
            styledText = new StyledText(tabFolderEditors, SWT.V_SCROLL | SWT.H_SCROLL);
        } else {
            IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
            styledText = new UnnotifiableColorStyledText(tabFolderEditors, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL,
                    preferenceStore, "tsql"); //$NON-NLS-1$
        }
        styledText.setEnabled(false);
        item.setControl(styledText);
        return styledText;
    }

    public MetadataTableEditorView getInputMetaEditor() {
        return this.inputMetaEditor;
    }

    public MetadataTableEditorView getOutputMetaEditor() {
        return this.outputMetaEditor;
    }

    public StyledTextHandler getStyledTextHandler() {
        return this.styledTextHandler;
    }

    /**
     * Getter for styledSqlText.
     *
     * @return the styledSqlText
     */
    public StyledText getStyledSqlText() {
        return this.styledSqlText;
    }

}
