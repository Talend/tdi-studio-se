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
package org.talend.designer.components.exchange.ui.views;

import org.eclipse.ui.internal.part.NullEditorInput;

/**
 * DOC talend class global comment. Detailled comment
 */
public class ExchangeEditorInput extends NullEditorInput {

    @Override
    public String getName() {
        return "org.talend.designer.components.exchange.ui.views.ExchangeView";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExchangeEditorInput) {
            ExchangeEditorInput input = (ExchangeEditorInput) obj;
            if (input.getName() != null && input.getName().equals(this.getName())) {
                return true;
            }
        }
        return false;
    }
}
