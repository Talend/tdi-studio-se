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
package org.talend.designer.hl7.dnd;

import org.talend.designer.hl7.model.PrimitiveModel;

/**
 * DOC hywang class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class TransferableSegmentEntry {

    private PrimitiveModel pm;

    public TransferableSegmentEntry(PrimitiveModel pm) {
        this.pm = pm;
    }

    public PrimitiveModel getPm() {
        return this.pm;
    }

    public void setPm(PrimitiveModel pm) {
        this.pm = pm;
    }

}
