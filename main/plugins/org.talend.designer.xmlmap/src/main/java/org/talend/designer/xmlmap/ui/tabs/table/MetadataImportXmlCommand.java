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
import java.util.ArrayList;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.commons.utils.data.list.ListenableList;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.MetadataSchema;
import org.talend.designer.xmlmap.XmlMapPlugin;

/**
 * created by Administrator on 2012-12-7 Detailled comment
 *
 */
public class MetadataImportXmlCommand extends org.talend.core.ui.metadata.extended.command.MetadataImportXmlCommand {

    /**
     * DOC Administrator MetadataImportXmlCommand constructor comment.
     *
     * @param extendedTableModel
     * @param file
     */
    public MetadataImportXmlCommand(ExtendedTableModel extendedTableModel, File file) {
        super(extendedTableModel, file);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.ui.metadata.extended.command.MetadataImportXmlCommand#execute()
     */
    @Override
    public void execute() {
        try {
            removed = new ArrayList<IMetadataColumn>(extendedTableModel.getBeansList());
            extendedTableModel.removeAll(removed);
            added = MetadataSchema.initializeColumns(file);
            extendedTableModel.addAll(added, true, false);
            if (extendedTableModel.getBeansList() instanceof ListenableList) {
                ListenableList beanList = (ListenableList) extendedTableModel.getBeansList();
                beanList.fireReplacedEvent(0, removed, added, false);
            }

        } catch (Exception e) {
            new ErrorDialogWidthDetailArea(null, XmlMapPlugin.PLUGIN_ID, "Can not import schema from invalid xml",
                    ExceptionUtils.getFullStackTrace(e));
            ExceptionHandler.process(e);
        }
    }

}
