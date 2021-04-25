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
package org.talend.designer.gefabstractmap.dnd;

import java.util.List;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TransferedObject {

    private List toTransfer;

    private TransferdType type;

    public TransferedObject(List toTransfer, TransferdType type) {
        this.toTransfer = toTransfer;
        this.type = type;
    }

    public List getToTransfer() {
        return toTransfer;
    }

    public void setToTransfer(List toTransfer) {
        this.toTransfer = toTransfer;
    }

    public TransferdType getType() {
        return type;
    }

    public void setType(TransferdType type) {
        this.type = type;
    }

}
