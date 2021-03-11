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
package org.talend.designer.business.diagram.viewer.label;

import org.eclipse.swt.graphics.Font;
import org.talend.repository.viewer.label.RepositoryViewLabelProvider;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class BusinessDiagramLabelProvider extends RepositoryViewLabelProvider {

    @Override
    public Font getFont(Object element) {
        // don't bold the top node?
        // return JFaceResources.getFontRegistry().defaultFont();
        return super.getFont(element);
    }

}
