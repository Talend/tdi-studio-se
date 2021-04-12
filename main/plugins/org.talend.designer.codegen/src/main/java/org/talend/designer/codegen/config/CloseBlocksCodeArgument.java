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
package org.talend.designer.codegen.config;

import java.util.List;

import org.talend.core.model.process.BlockCode;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public class CloseBlocksCodeArgument {

    private List<BlockCode> blocksCodeToClose;

    /**
     * DOC amaumont Comment method "setCloseBlocksCount".
     *
     * @param closeBlocksCode
     */
    public void setBlocksCodeToClose(List<BlockCode> closeBlocksCode) {
        this.blocksCodeToClose = closeBlocksCode;
    }

    public List<BlockCode> getBlocksCodeToClose() {
        return this.blocksCodeToClose;
    }

}
