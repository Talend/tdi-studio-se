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
package org.talend.designer.core.utils;

import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.utils.ParameterValueUtil;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC xye class global comment. Detailled comment
 */
public final class UpgradeElementHelper {

    public static boolean isUseData(final IElement element, final String name) {
        if (element == null || element.getElementParameters() == null) {
            return false;
        }
        for (IElementParameter param : element.getElementParameters()) {
            if (param.getName().equals(EParameterName.UNIQUE_NAME.getName())) {
                continue;
            }
            if (param.getName().equals(EParameterName.VALUES.getName())) {
                continue;
            }
            if (param.getName().equals("LENGTHS")) {
                continue;
            }
            if (ParameterValueUtil.isUseData(param, name)) {
                return true;
            }
        }
        return false;
    }

    public static void renameData(final IElement element, final String oldName, final String newName) {

        if (element == null) {
            return;
        }

        if (element.getElementParameters() == null || element.getElementParameters().isEmpty()) {
            return;
        }

        if (oldName.equals(newName)) {
            return;
        }

        // see bug 4733 & 5167
        for (IElementParameter param : element.getElementParameters()) {
            if (param.getName().equals(EParameterName.UNIQUE_NAME.getName())) {
                /* || isSQLQueryParameter(param) */
                // bug 17979 modify: tJavaRow in Joblet
                /* || isJavaRowCodeParameter(element, param) ) { */
                continue;
            }
            ParameterValueUtil.renameValues(param, oldName, newName);
        }
    }

    /**
     *
     * DOC YeXiaowei Comment method "isSQLQueryParameter".
     *
     * @param parameter
     * @return
     */
    private static boolean isSQLQueryParameter(final IElementParameter parameter) {
        return parameter.getFieldType().equals(EParameterFieldType.MEMO_SQL) && parameter.getName().equals("QUERY"); //$NON-NLS-1$
    }

    /**
     *
     * DOC xye Comment method "iJavaRowCodeParameter".
     * <p>
     *
     * @see bug 5167
     * @param node
     * @param parameter
     * @return
     */
    private static boolean isJavaRowCodeParameter(final IElement node, final IElementParameter parameter) {
        if (node instanceof Node) {
            if (((Node) node).getUniqueName().contains("tJavaRow")) { //$NON-NLS-1$
                return parameter.getFieldType().equals(EParameterFieldType.MEMO_JAVA) && parameter.getName().equals("CODE"); //$NON-NLS-1$
            }
        }
        return false;
    }

}
