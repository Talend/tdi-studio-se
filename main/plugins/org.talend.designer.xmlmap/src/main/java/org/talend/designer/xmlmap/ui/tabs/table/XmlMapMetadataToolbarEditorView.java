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
package org.talend.designer.xmlmap.ui.tabs.table;

import java.io.File;

import org.eclipse.gef.commands.Command;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ImportPushButton;
import org.talend.commons.ui.swt.advanced.dataeditor.button.ImportPushButtonForExtendedTable;
import org.talend.commons.ui.swt.extended.table.AbstractExtendedTableViewer;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.ui.metadata.editor.MetadataToolbarEditorView;

/**
 * created by Administrator on 2012-12-7 Detailled comment
 *
 */
public class XmlMapMetadataToolbarEditorView extends MetadataToolbarEditorView {

    /**
     * DOC Administrator XmlMapMetadataToolbarEditorView constructor comment.
     *
     * @param parent
     * @param style
     * @param extendedTableViewer
     */
    public XmlMapMetadataToolbarEditorView(Composite parent, int style, AbstractExtendedTableViewer extendedTableViewer,
            String dbmsId) {
        super(parent, style, extendedTableViewer, dbmsId);
    }

    @Override
    public ImportPushButton createImportPushButton() {
        return new ImportPushButtonForExtendedTable(toolbar, extendedTableViewer) {

            @Override
            protected Command getCommandToExecute(ExtendedTableModel extendedTableModel, File file) {
                return new MetadataImportXmlCommand(extendedTableModel, file);
            }

        };
    }

}
