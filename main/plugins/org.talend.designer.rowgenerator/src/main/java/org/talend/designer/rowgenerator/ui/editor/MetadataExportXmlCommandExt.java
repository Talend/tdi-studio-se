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
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.gef.commands.Command;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.ui.metadata.editor.MetadataTableEditor;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2007-3-27 上午11:00:36 (星期五, 29 九月 2006) qzhang $
 *
 */
public class MetadataExportXmlCommandExt extends Command {

    private File file;

    private MetadataTableEditorExt extendedTableModel;

    /**
     * amaumont MetadataPasteCommand constructor comment.
     *
     * @param extendedTableModel
     * @param extendedTable
     * @param validAssignableType
     * @param indexStartAdd
     */
    public MetadataExportXmlCommandExt(MetadataTableEditorExt extendedTableModel, File file) {
        super();
        this.file = file;
        this.extendedTableModel = extendedTableModel;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.commons.ui.command.CommonCommand#execute()
     */
    @Override
    public void execute() {
        try {
            if (file != null) {
                file.createNewFile();
                if (extendedTableModel != null) {
                    IMetadataTable currentTable = extendedTableModel.getMetadataTable();
                    // get all the columns from the table
                    if (currentTable != null) {
                        MetadataSchemaExt ext = new MetadataSchemaExt(extendedTableModel.getRowGenUI().getFunctionManager());
                        ext.saveColumnsToFile(file, currentTable);
                    }
                }
            }
        } catch (IOException e) {
            ExceptionHandler.process(e);
        } catch (ParserConfigurationException e) {
            ExceptionHandler.process(e);
        }

    }

}
