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
package org.talend.sdk.component.studio.model.parameter;

import java.util.Map;

import org.talend.core.model.process.IElement;
import org.talend.sdk.component.studio.model.action.SuggestionsAction;

/**
 * created by hcyi on Jul 26, 2019
 * Detailled comment
 *
 */
public class TextAreaSelectionParameter extends TaCoKitElementParameter {

    private final SuggestionsAction action;

    public TextAreaSelectionParameter(IElement element, final SuggestionsAction action) {
        super(element);
        this.action = action;
    }

    public Map<String, String> getSuggestionValues() {
        return action.callback();
    }
}
