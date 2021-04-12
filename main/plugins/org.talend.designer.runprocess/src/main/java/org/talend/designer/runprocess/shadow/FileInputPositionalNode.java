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
package org.talend.designer.runprocess.shadow;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.component.ComponentsFactoryProvider;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class FileInputPositionalNode extends FileInputNode {

    /**
     * Constructs a new FileInputPositionalNode.
     */
    public FileInputPositionalNode(String filename, String rowSep, String pattern, int headerRows, int footerRows, int limitRows,
            boolean removeEmptyRow, String encoding) {
        super("tFileInputPositional", pattern.split(",").length); //$NON-NLS-1$ //$NON-NLS-2$

        String[] paramNames = new String[] {
                "FILENAME", "ROWSEPARATOR", "PATTERN", "HEADER", "FOOTER", "LIMIT", "REMOVE_EMPTY_ROW", "ENCODING" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
        String[] paramValues = new String[] { filename, rowSep, pattern, Integer.toString(headerRows),
                Integer.toString(footerRows), Integer.toString(limitRows), Boolean.toString(removeEmptyRow), encoding };

        IComponent component = ComponentsFactoryProvider.getInstance().get("tFileInputPositional", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        this.setElementParameters(component.createElementParameters(this));
        for (int i = 0; i < paramNames.length; i++) {
            if (paramValues[i] != null) {
                IElementParameter param = this.getElementParameter(paramNames[i]);
                if (param != null) {
                    param.setValue(paramValues[i]);
                }
            }
        }
    }
}
