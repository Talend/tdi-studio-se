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

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;
import org.talend.designer.rowgenerator.ui.RowGeneratorUI;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: MetadataTableEditorExt.java,v 1.3 2007/01/31 05:20:52 pub Exp $
 *
 */
public class MetadataTableEditorExt extends MetadataTableEditor {

    private RowGeneratorUI ui;

    private FunctionManagerExt functionManagerExt;

    /**
     * qzhang MetadataTableEditorExt constructor comment.
     */
    public MetadataTableEditorExt(IMetadataTable metadataTable, String titleName, FunctionManagerExt functionManagerExt) {
        super(metadataTable, titleName);
        this.functionManagerExt = functionManagerExt;
    }

    public IMetadataColumn createNewMetadataColumn() {
        final MetadataColumnExt metadataColumnExt = new MetadataColumnExt((MetadataColumn) super.createNewMetadataColumn());
        metadataColumnExt.setFunction(functionManagerExt.getDefaultFunction(metadataColumnExt, metadataColumnExt
                .getTalendType()));
        return metadataColumnExt;
    }

    public void setRowGenUI(RowGeneratorUI ui) {
        this.ui = ui;
    }

    public RowGeneratorUI getRowGenUI() {
        return this.ui;
    }

}
