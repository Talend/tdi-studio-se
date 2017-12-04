// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.ui.swt.drawing.link.BezierHorizontalLink;
import org.talend.commons.ui.swt.drawing.link.ExtremityEastArrow;
import org.talend.commons.ui.swt.drawing.link.ExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IExtremityLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.commons.ui.swt.drawing.link.LinkDescriptor;
import org.talend.commons.ui.swt.drawing.link.StyleLink;
import org.talend.commons.ui.swt.drawing.link.TreeExtremityDescriptor;
import org.talend.commons.ui.swt.linking.TreeToTablesLinker;
import org.talend.designer.hl7.managers.HL7Manager;
import org.talend.designer.hl7.ui.HL7UI;
import org.talend.designer.hl7.ui.view.HL7MetadataEmfTableEditorView;
import org.talend.metadata.managment.ui.wizard.metadata.xml.XmlExtractorBgRefresher;
import org.talend.repository.i18n.Messages;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class HL7Tree2SchemaLinker extends TreeToTablesLinker<Object, Object> {

    private Table target;

    private Tree source;

    private HL7Manager manager;

    private TreeViewer messageViewer;

    private HL7MetadataEmfTableEditorView hl7SchemaTableEditorView;

    private Color selectedRelativeLinkColor;

    private HL7UI mainui;

    private boolean isRepository;

    /**
     * DOC hywang HL7Tree2SchemaLinker constructor comment.
     * 
     * @param commonParent
     */
    public HL7Tree2SchemaLinker(Composite commonParent, boolean isRepository) {
        super(commonParent);
        this.isRepository = isRepository;
    }

    public void init(TreeViewer sourceTrees, HL7MetadataEmfTableEditorView hl7SchemaTableEditorView) {
        init(sourceTrees.getTree(), new Table[] { hl7SchemaTableEditorView.getTable() }, new XmlExtractorBgRefresher(this));
        this.target = hl7SchemaTableEditorView.getTable();
        this.source = sourceTrees.getTree();
        this.messageViewer = sourceTrees;
        this.hl7SchemaTableEditorView = hl7SchemaTableEditorView;
        // TextCellEditorWithProposal xPathCellEditor = hl7SchemaTableEditorView.getXPathCellEditor();
        // xPathCellEditor.setContentProposalProvider(new HL7SchemaProposalProvider(this, false));
        init();
    }

    private void init() {
        Display display = getBgDrawableComposite().getDisplay();
        initColors(display);
        // StyleLink unselectedStyleLink = new StyleLink();
        // unselectedStyleLink.setDrawableLink(new BezierHorizontalLink(unselectedStyleLink));
        // unselectedStyleLink.setForegroundColor(display.getSystemColor(SWT.COLOR_GRAY));
        // unselectedStyleLink.setLineWidth(2);
        // int xOffset = WindowSystem.isGTK() ? 2 : -2;
        // int yOffset = WindowSystem.isGTK() ? -1 : 0;
        // unselectedStyleLink.setExtremity2(new ExtremityEastArrow(unselectedStyleLink, -ExtremityEastArrow.WIDTH_ARROW
        // + xOffset,
        // yOffset));
        // setUnselectedStyleLink(unselectedStyleLink);
        // getSelectedRelativeStyleLink();

        initListeners();

    }

    private void initColors(Display display) {
        // selectedLoopLinkColor = new Color(display, 255, 131, 0);
        selectedRelativeLinkColor = new Color(display, 110, 168, 255);
        getTree().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                // selectedLoopLinkColor.dispose();
                selectedRelativeLinkColor.dispose();
                getTree().removeDisposeListener(this);
            }

        });

    }

    // private void getSelectedRelativeStyleLink() {
    // StyleLink selectedStyleLink = new StyleLink();
    // selectedStyleLink.setDrawableLink(new BezierHorizontalLink(selectedStyleLink));
    // selectedStyleLink.setForegroundColor(selectedRelativeLinkColor);
    // selectedStyleLink.setLineWidth(2);
    // int xOffset = WindowSystem.isGTK() ? 2 : -2;
    // int yOffset = WindowSystem.isGTK() ? -1 : 0;
    // selectedStyleLink.setExtremity2(new ExtremityEastArrow(selectedStyleLink, -ExtremityEastArrow.WIDTH_ARROW +
    // xOffset,
    // yOffset));
    // setSelectedStyleLink(selectedStyleLink);
    // }

    public void updateLinksStyleAndControlsSelection(Control currentControl, Boolean flag) {

        boolean selectedControlIsTable = false;
        if (currentControl instanceof Table) {
            selectedControlIsTable = true;
        } else if (currentControl instanceof Tree) {
            selectedControlIsTable = false;
        } else {
            throw new IllegalArgumentException(Messages.getString("XmlToXPathLinker.illegalArgumentException")); //$NON-NLS-1$
        }

        HashSet selectedItems = new HashSet();
        Map itemsToSelect = new HashMap();

        if (selectedControlIsTable) {
            // for (Table table : getTables()) {
            // if (table != currentControl) {
            // table.deselectAll();
            // if (isFieldsTable(table)) {
            // fieldsTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
            // }
            // }
            // }

            TableItem[] selection = ((Table) currentControl).getSelection();
            for (int i = 0; i < selection.length; i++) {
                TableItem tableItem = selection[i];
                selectedItems.add(tableItem.getData());
            }

        } else {

            TreeItem[] selection = getTree().getSelection();
            for (int i = 0; i < selection.length; i++) {
                TreeItem treeItem = selection[i];
                selectedItems.add(treeItem.getData());
            }

        }

        List<LinkDescriptor<TreeItem, Object, Table, Object>> links = linksManager.getLinks();
        for (LinkDescriptor<TreeItem, Object, Table, Object> link : links) {
            IStyleLink styleLink = null;
            IExtremityLink extremity = null;
            IExtremityLink otherExtremity = null;
            if (selectedControlIsTable) {
                extremity = link.getExtremity2();
                otherExtremity = link.getExtremity1();
            } else {
                extremity = link.getExtremity1();
                otherExtremity = link.getExtremity2();
            }
            boolean currentItemIsSelected = selectedItems.contains(extremity.getDataItem());

            // if (extremity.getGraphicalObject() == loopTableEditorView.getTableViewerCreator().getTable()
            // || otherExtremity.getGraphicalObject() == loopTableEditorView.getTableViewerCreator().getTable()) {
            // styleLink = getSelectedLoopStyleLink();
            // } else {
            //
            // if (currentItemIsSelected) {
            // styleLink = getSelectedStyleLink();
            // if (selectedControlIsTable) {
            //
            // itemsToSelect.put(otherExtremity.getGraphicalObject(), null);
            //
            // } else {
            //
            // Table currentTable = (Table) otherExtremity.getGraphicalObject();
            // List<TableItem> tableItemsToSelect = (List<TableItem>) itemsToSelect.get(currentTable);
            //
            // if (tableItemsToSelect == null) {
            // tableItemsToSelect = new ArrayList<TableItem>();
            // itemsToSelect.put(currentTable, tableItemsToSelect);
            // }
            // TableItem tableItem = TableUtils.getTableItem(currentTable, otherExtremity.getDataItem());
            // tableItemsToSelect.add(tableItem);
            // }
            // } else {
            // styleLink = getUnselectedStyleLink();
            // }
            // }
            if (styleLink == null) {
                styleLink = createStandardLink(new Color(getBgDrawableComposite().getDisplay(), 255, 102, 102));
            }
            link.setStyleLink(styleLink);

        }
        if (selectedControlIsTable) {
            (getTree()).setSelection((TreeItem[]) itemsToSelect.keySet().toArray(new TreeItem[0]));
        } else {
            Set<Table> set = itemsToSelect.keySet();
            if (set.size() > 0) {
                for (Table table : set) {
                    ArrayList<TableItem> tableItemsToSelect = (ArrayList<TableItem>) itemsToSelect.get(table);
                    table.deselectAll();
                    TableItem[] tableItems = tableItemsToSelect.toArray(new TableItem[0]);
                    table.setSelection(tableItems);
                }
            } else {
                // loopTableEditorView.getTable().deselectAll();
                // fieldsTableEditorView.getTable().deselectAll();
            }
            // fieldsTableEditorView.getExtendedToolbar().updateEnabledStateOfButtons();
        }
        getLinksManager().sortLinks(getDrawingLinksComparator());
        getBackgroundRefresher().refreshBackground();
    }

    public void removeAllLinks() {
        getLinksManager().clearLinks();
    }

    private void initListeners() {
        // initLoopListeners();
        new HL7Message2SchemaDragAndDropHandler(this);
    }

    public void addLinks(TreeItem itemSource, Object data1, Table tableTarget, Object data2) {
        LinkDescriptor<TreeItem, Object, Table, Object> link = new LinkDescriptor<TreeItem, Object, Table, Object>(
                new TreeExtremityDescriptor(itemSource, data1), new ExtremityLink<Table, Object>(tableTarget, data2));
        link.setStyleLink(createStandardLink(new Color(getBgDrawableComposite().getDisplay(), 255, 102, 102)));
        getLinksManager().addLink(link);
    }

    protected StyleLink createStandardLink(Color color) {
        StyleLink styleLink = new StyleLink();
        BezierHorizontalLink link = new BezierHorizontalLink(styleLink);
        // LineLinkWithHorizontalConnectors link = new LineLinkWithHorizontalConnectors(styleLink);
        // link.setConnectorWidth(40);
        styleLink.setDrawableLink(link);
        styleLink.setForegroundColor(color);
        styleLink.setLineWidth(2);
        styleLink.setExtremity2(new ExtremityEastArrow(styleLink, -5, 0));
        return styleLink;
    }

    public Table getTarget() {
        return this.target;
    }

    public Tree getSource() {
        return this.source;
    }

    public HL7Manager getManager() {
        return this.manager;
    }

    public void setManager(HL7Manager manager) {
        this.manager = manager;
    }

    public TreeViewer getMessageViewer() {
        return this.messageViewer;
    }

    public HL7MetadataEmfTableEditorView getHl7SchemaTableEditorView() {
        return this.hl7SchemaTableEditorView;
    }

    public HL7UI getMainui() {
        return this.mainui;
    }

    public void setMainui(HL7UI mainui) {
        this.mainui = mainui;
    }

    public boolean isRepository() {
        return this.isRepository;
    }

    public void setRepository(boolean isRepository) {
        this.isRepository = isRepository;
    }

}
