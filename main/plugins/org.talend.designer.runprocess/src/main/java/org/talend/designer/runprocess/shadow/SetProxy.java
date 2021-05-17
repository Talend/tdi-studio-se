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

import org.talend.core.model.runprocess.shadow.TextElementParameter;
import org.talend.core.model.utils.TalendTextUtils;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class SetProxy extends ShadowNode {

    /**
     * DOC Administrator SetProxy constructor comment.
     *
     * @param componentName
     */
    public SetProxy(String type, String host, String port, String username, String pass, String nonproxy) {
        super("tSetProxy");
        TextElementParameter param0 = new TextElementParameter("PROXY_TYPE", TalendTextUtils.addQuotes(type)); //$NON-NLS-1$
        TextElementParameter param1 = new TextElementParameter("PROXY_HOST", TalendTextUtils.addQuotes(host)); //$NON-NLS-1$
        TextElementParameter param2 = new TextElementParameter("PROXY_PORT", TalendTextUtils.addQuotes(port)); //$NON-NLS-1$
        TextElementParameter param3 = new TextElementParameter("PROXY_USERNAME", TalendTextUtils.addQuotes(username)); //$NON-NLS-1$
        TextElementParameter param4 = new TextElementParameter("PROXY_PASSWORD", TalendTextUtils.addQuotes(pass)); //$NON-NLS-1$
        TextElementParameter param5 = new TextElementParameter("NONPROXYHOSTS", TalendTextUtils.addQuotes(nonproxy)); //$NON-NLS-1$ //$NON-NLS-2$

        addParameter(param0);
        addParameter(param1);
        addParameter(param2);
        addParameter(param3);
        addParameter(param4);
        addParameter(param5);
    }

    @Override
    public boolean isStart() {
        return true;
    }

}
