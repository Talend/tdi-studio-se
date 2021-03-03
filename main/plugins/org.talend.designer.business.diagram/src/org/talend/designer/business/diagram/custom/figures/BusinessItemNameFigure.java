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
package org.talend.designer.business.diagram.custom.figures;

import org.eclipse.gmf.runtime.draw2d.ui.figures.WrapLabel;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class BusinessItemNameFigure extends WrapLabel {

    /**
     * DOC mhelleboid BusinessItemNameFigure constructor comment.
     */
    public BusinessItemNameFigure() {
        // fixed bug 10303
        // setTextWrap(true);
    }

}
