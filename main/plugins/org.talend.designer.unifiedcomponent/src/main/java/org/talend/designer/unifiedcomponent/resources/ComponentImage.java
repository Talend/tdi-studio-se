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
package org.talend.designer.unifiedcomponent.resources;

import org.talend.commons.ui.runtime.image.IImage;

/**
 * created by wchen on Dec 4, 2017 Detailled comment
 *
 */
public enum ComponentImage implements IImage {
    tDBInput("/icons/tDBInput_icon32.png"),
    tDBOutput("/icons/tDBOutput_icon32.png"),
    tDBClose("/icons/tDBClose_icon32.png"),
    tDBColumnList("/icons/tDBColumnList_icon32.png"),
    tDBCommit("/icons/tDBCommit_icon32.png"),
    tDBConnection("/icons/tDBConnection_icon32.png"),
    tDBRollback("/icons/tDBRollback_icon32.png"),
    tDBRow("/icons/tDBRow_icon32.png"),
    tDBSCDELT("/icons/tDBSCDELT_icon32.png"),
    tDBTableList("/icons/tDBTableList_icon32.png"),
    tDBBulkExec("/icons/tDBBulkExec_icon32.png"),
    tDBCDC("/icons/tDBCDC_icon32.png"),
    tDBInvalidRows("/icons/tDBInvalidRows_icon32.png"),
    tDBLastInsertId("/icons/tDBLastInsertId_icon32.png"),
    tDBOutputBulk("/icons/tDBOutputBulk_icon32.png"),
    tDBOutputBulkExec("/icons/tDBOutputBulkExec_icon32.png"),
    tDBSCD("/icons/tDBSCD_icon32.png"),
    tDBSP("/icons/tDBSP_icon32.png"),
    tDBValidRows("/icons/tDBValidRows_icon32.png"),
    tDBCDCOutput("/icons/tDBCDCOutput_icon32.png");

    private String path;

    ComponentImage(String path) {
        this.path = path;
    }

    /**
     * Getter for path.
     *
     * @return the path
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * Getter for clazz.
     *
     * @return the clazz
     */
    @Override
    public Class getLocation() {
        return ComponentImage.class;
    }

}
