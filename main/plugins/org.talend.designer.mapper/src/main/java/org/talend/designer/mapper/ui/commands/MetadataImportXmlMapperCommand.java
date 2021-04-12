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
package org.talend.designer.mapper.ui.commands;

import java.io.File;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.core.ui.metadata.extended.command.MetadataImportXmlCommand;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public class MetadataImportXmlMapperCommand extends MetadataImportXmlCommand {

    /**
     * DOC amaumont MetadataImportXmlMapperCommand constructor comment.
     *
     * @param extendedTableModel
     * @param file
     */
    public MetadataImportXmlMapperCommand(ExtendedTableModel extendedTableModel, File file) {
        super(extendedTableModel, file);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.extended.command.MetadataImportXmlCommand#execute()
     */
    @Override
    public void execute() {
        super.execute();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#redo()
     */
    @Override
    public void redo() {
        super.redo();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gef.commands.Command#undo()
     */
    @Override
    public void undo() {
        super.undo();
    }

}
