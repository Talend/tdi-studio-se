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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.core.ui.component.ComponentsFactoryProvider;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class FileInputRegExpNode extends FileInputNode {

    /**
     * Constructs a new FileInputNode.
     */
    public FileInputRegExpNode(String filename, String rowSep, String regex, int limitRows, int headerRows, int footerRows,
            boolean removeEmptyRow, String encoding) {
        super("tFileInputRegex"); //$NON-NLS-1$
        if (regex.length() > 1) {
            String reg = TalendTextUtils.removeQuotes(regex).replace("\\\\", "\\");
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(""); //$NON-NLS-1$
            int columnCount = matcher.groupCount();
            if (columnCount > 0) {
                this.setColumnNumber(columnCount);
            }
        }

        String[] paramNames = new String[] { "FILENAME", "ROWSEPARATOR", "REGEX", "LIMIT", "HEADER", "FOOTER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                "REMOVE_EMPTY_ROW", "ENCODING" }; //$NON-NLS-1$ //$NON-NLS-2$
        String[] paramValues = new String[] { filename, rowSep, regex, Integer.toString(limitRows), Integer.toString(headerRows),
                Integer.toString(footerRows), Boolean.toString(removeEmptyRow), encoding };

        IComponent component = ComponentsFactoryProvider.getInstance().get("tFileInputRegex", //$NON-NLS-1$
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
