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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.gef.commands.Command;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.model.metadata.IMetadataColumn;
import org.xml.sax.SAXException;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2007-3-27 上午11:01:03 (星期五, 29 九月 2006) qzhang $
 *
 */
public class MetadataImportXmlCommandExt extends Command {

    private File file;

    private MetadataTableEditorExt extendedTableModel;

    private List<IMetadataColumn> removed;

    /**
     * amaumont MetadataPasteCommand constructor comment.
     *
     * @param extendedTableModel
     * @param extendedTable
     * @param validAssignableType
     * @param indexStartAdd
     */
    public MetadataImportXmlCommandExt(MetadataTableEditorExt extendedTableModel, File file) {
        super();
        this.file = file;
        this.extendedTableModel = extendedTableModel;
    }

    /*
     * (non-Java)
     *
     * @see org.talend.commons.ui.command.CommonCommand#execute()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        try {
            // load the schema
            removed = new ArrayList<IMetadataColumn>(extendedTableModel.getBeansList());
            extendedTableModel.removeAll(removed);

            MetadataSchemaExt ext = new MetadataSchemaExt(extendedTableModel.getRowGenUI().getFunctionManager());
            List<IMetadataColumn> metadataColumns = ext.initializeAllColumns(file);
            extendedTableModel.addAll(metadataColumns);

        } catch (ParserConfigurationException e) {
            ExceptionHandler.process(e);
        } catch (SAXException e) {
            // bug 17654:import the xml file as the schema will throw error.
            ExceptionHandler.processForSchemaImportXml(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }
}
