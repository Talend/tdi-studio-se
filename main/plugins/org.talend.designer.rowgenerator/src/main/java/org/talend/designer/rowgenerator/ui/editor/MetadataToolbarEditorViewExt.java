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
package org.talend.designer.rowgenerator.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.swt.tableviewer.TableViewerCreatorColumnNotModifiable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.AddPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ExportPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ExportPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ImportPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ImportPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveDownPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveDownPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveUpPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.MoveUpPushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.PastePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.RemovePushButtonForExtendedTable;
import org.talend.commons.ui.swt.advanced.dataeditor.button.SaveAsGenericSchemaPushButton;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.process.IProcess;
import org.talend.core.ui.metadata.editor.MetadataToolbarEditorView;
import org.talend.core.ui.proposal.TalendProposalUtils;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.rowgenerator.i18n.Messages;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: MetadataToolbarEditorViewExt.java,v 1.9 2007/01/31 07:33:40 pub Exp $
 *
 */
public class MetadataToolbarEditorViewExt extends MetadataToolbarEditorView {

    private MetadataTableEditorViewExt genTableEditor2;

    /**
     * qzhang MetadataToolbarEditorViewExt constructor comment.
     *
     * @param parent
     * @param style
     * @param extendedTableViewer
     */
    public MetadataToolbarEditorViewExt(Composite parent, int style, AbstractExtendedTableViewer extendedTableViewer,
            MetadataTableEditorViewExt editor2) {
        super(parent, style, extendedTableViewer);
        this.genTableEditor2 = editor2;
        initStrings();
        createColumns();
        columnsListmenu.getItems();
        createNumberRows();
    }

    /**
     * qzhang Comment method "initStrings".
     */
    private void initStrings() {
        items = new ArrayList<String>();
        ids = new ArrayList<String>();
        List<TableViewerCreatorColumnNotModifiable> columns = genTableEditor2.getTableViewerCreator().getColumns();
        for (TableViewerCreatorColumnNotModifiable column : columns) {
            if ((column.getId() != null && !"".equals(column.getId())) //$NON-NLS-1$
                    && (column.getTitle() != null && !"".equals(column.getTitle()))) { //$NON-NLS-1$
                ids.add(column.getId());
                items.add(column.getTitle());
            }
        }
    }

    private Composite numRowComposite;

    private Text numRowText;

    private void createNumberRows() {
        numRowComposite = new Composite(toolbar, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.horizontalSpacing = 4;
        layout.marginBottom = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginTop = 0;
        layout.marginWidth = 0;

        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.CENTER;
        numRowComposite.setLayout(layout);
        final Label numRowLabel = new Label(numRowComposite, SWT.NONE);
        numRowLabel.setText(Messages.getString("MetadataToolbarEditorViewExt.RowNum.LabelText")); //$NON-NLS-1$
        numRowText = new Text(numRowComposite, SWT.BORDER);

        GridDataFactory.swtDefaults().hint(rowNumberWidth(), SWT.DEFAULT).applyTo(numRowText);

        numRowText.setBackground(ColorConstants.white);
        numRowText.setEnabled(!extendedTableViewer.isReadOnly());

        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        IProcess process = ((AbstractMultiPageTalendEditor) editor).getTalendEditor().getProcess();
        TalendProposalUtils.installOn(numRowText, process);
    }

    /**
     *
     * yexiaowei Comment method "rowNumberWidth".
     *
     * @return
     */
    private int rowNumberWidth() {
        String widthS = Messages.getString("MetadataToolbarEditorViewExt.RowNum.Text.width"); //$NON-NLS-1$
        if (widthS == null || widthS.equals("")) { //$NON-NLS-1$
            return 200;
        } else {
            try {
                int width = Integer.parseInt(widthS);
                return width < 200 ? 200 : width;
            } catch (Exception e) {
                return 200;
            }
        }
    }

    private Menu columnsListmenu;

    private ToolBar columnSelector;

    private List<String> items;

    private List<String> ids;

    /**
     * qzhang Comment method "createColumns".
     */
    private void createColumns() {
        columnSelector = new ToolBar(toolbar, SWT.HORIZONTAL);
        final ToolItem columns = new ToolItem(columnSelector, SWT.DROP_DOWN);
        columns.setText(Messages.getString("MetadataToolbarEditorViewExt.Columns.Text")); //$NON-NLS-1$
        createMenu(columns);
        columns.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Java)
             *
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent event) {
                // If they clicked the arrow, we show the list
                // if (event.detail == SWT.ARROW) {
                // Determine where to put the dropdown list
                ToolItem item = (ToolItem) event.widget;
                Rectangle rect = item.getBounds();
                Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
                columnsListmenu.setLocation(pt.x, pt.y + rect.height);
                columnsListmenu.setVisible(true);
                // }
            }
        });
    }

    /**
     * qzhang Comment method "notuse".
     */
    private void createMenu(final ToolItem columns) {
        columnsListmenu = new Menu(columns.getParent().getShell());
        for (int i = 0; i < items.size(); i++) {
            MenuItem item = new MenuItem(columnsListmenu, SWT.CHECK);
            item.setText(items.get(i));
            item.setData(ids.get(i));
            item.setSelection(true);
            final int j = i;
            item.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    MenuItem item = (MenuItem) e.widget;
                    genTableEditor2.updateHeader(ids.get(j), items.get(j), !item.getSelection());
                }
            });
        }
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createAddPushButton()
     */
    @Override
    protected AddPushButton createAddPushButton() {
        return new AddPushButtonForExtendedTable(this.toolbar, getExtendedTableViewer()) {

            @Override
            protected Object getObjectToAdd() {
                MetadataTableEditorExt tableEditorModel = (MetadataTableEditorExt) getExtendedTableViewer()
                        .getExtendedControlModel();
                return tableEditorModel.createNewMetadataColumn();
            }
        };
    }

    /*
     * (non-Java)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#createRemovePushButton()
     */
    @Override
    protected RemovePushButton createRemovePushButton() {
        RemovePushButtonForExtendedTable removeButton2 = new RemovePushButtonForExtendedTable(toolbar, extendedTableViewer);
        return removeButton2;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createPastButton()
     */
    @Override
    public PastePushButton createPastePushButton() {
        return new PastePushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, Integer indexWhereInsert) {
                return new MetadataExtPasteCommand(extendedTableModel, indexWhereInsert, genTableEditor2.getFunctionManager());
            }

        };
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createExportPushButton()
     */
    @Override
    protected ExportPushButton createExportPushButton() {
        return new ExportPushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, File file) {
                MetadataTableEditorExt tableEditorModel = (MetadataTableEditorExt) getExtendedTableViewer()
                        .getExtendedControlModel();
                return new MetadataExportXmlCommandExt(tableEditorModel, file);
            }

        };
    }

    /*
     * (non-Java)
     *
     * @see org.talend.core.ui.extended.ExtendedToolbarView#createPastButton()
     */
    @Override
    public ImportPushButton createImportPushButton() {
        return new ImportPushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, File file) {
                MetadataTableEditorExt tableEditorModel = (MetadataTableEditorExt) getExtendedTableViewer()
                        .getExtendedControlModel();
                return new MetadataImportXmlCommandExt(tableEditorModel, file);
            }

        };
    }

    /*
     * (non-Java)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#createMoveDownPushButton()
     */
    @Override
    protected MoveDownPushButton createMoveDownPushButton() {
        return new MoveDownPushButtonForExtendedTable(toolbar, extendedTableViewer);
    }

    /*
     * (non-Java)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#createMoveUpPushButton()
     */
    @Override
    protected MoveUpPushButton createMoveUpPushButton() {
        return new MoveUpPushButtonForExtendedTable(toolbar, extendedTableViewer);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.commons.ui.swt.advanced.dataeditor.ExtendedToolbarView#createSaveAsGenericSchemaButton()
     */
    @Override
    protected SaveAsGenericSchemaPushButton createSaveAsGenericSchemaButton() {
        // TODO Auto-generated method stub
        return super.createSaveAsGenericSchemaButton(null);
    }

    public Menu getColumnsListmenu() {
        return this.columnsListmenu;
    }

    /**
     * qzhang Comment method "updateColumnsList".
     *
     * @param hideColumnsList
     */
    public void updateColumnsList(String[] hideColumnsList) {
        for (String string : hideColumnsList) {
            for (MenuItem item : columnsListmenu.getItems()) {
                if (item.getData().equals(string)) {
                    item.setSelection(false);
                }
            }
        }
    }

    public ToolBar getColumnSelector() {
        return this.columnSelector;
    }

    public String getNumRows() {
        return this.numRowText.getText();
    }

    /**
     * qzhang Comment method "updateComponentsSize".
     */
    public void updateComponentsSize() {
        String number = genTableEditor2.getGeneratorUI().getGeneratorManager().getRowGeneratorComponent().getNumber();
        if (number == null || "".equals(number)) { //$NON-NLS-1$
            number = "100"; //$NON-NLS-1$
        }
        if (number.startsWith("'")) { //$NON-NLS-1$
            number = number.substring(1);
        }
        if (number.endsWith("'")) { //$NON-NLS-1$
            number = number.substring(0, number.length() - 1);
        }
        numRowText.setText(number);
    }

}
