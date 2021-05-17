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
package org.talend.designer.gefabstractmap.figures;

import org.eclipse.draw2d.Label;
import org.talend.designer.gefabstractmap.figures.cells.ITextCell;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class VarNodeTextLabel extends Label implements ITextCell {

    public VarNodeTextLabel() {
        setDirectEditType(DirectEditType.NODE_NAME);
    }

    private DirectEditType type;

    @Override
    public void setDirectEditType(DirectEditType type) {
        this.type = type;
    }

    @Override
    public DirectEditType getDirectEditType() {
        return this.type;
    }

}
