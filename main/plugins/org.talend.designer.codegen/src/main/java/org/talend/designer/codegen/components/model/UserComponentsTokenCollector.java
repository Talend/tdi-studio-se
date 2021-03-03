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
package org.talend.designer.codegen.components.model;

import java.util.List;

import org.talend.core.model.components.IComponent;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.token.AbstractTokenCollector;
import org.talend.core.ui.token.TokenKey;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONObject;

/**
 * ggu class global comment. Detailled comment
 *
 * collect the user and exchange components
 */
public class UserComponentsTokenCollector extends AbstractTokenCollector {

    private static final TokenKey USER_COMPONENTS = new TokenKey("user.components"); //$NON-NLS-1$

    /**
     * ggu UserComponentTokenCollector constructor comment.
     */
    public UserComponentsTokenCollector() {
        //
    }

    /* (non-Javadoc)
     * @see org.talend.core.ui.token.AbstractTokenCollector#collect()
     */
    @Override
    public JSONObject collect() throws Exception {
        JSONObject object = new JSONObject();
        List<IComponent> customComponents = ComponentsFactoryProvider.getInstance().getCustomComponents();
        JSONArray customComponentsArray = new JSONArray();
        if (customComponents != null) {
            for (int i = 0; i < customComponents.size(); i++) {
                customComponentsArray.put(customComponents.get(i).getName());
            }
        }
        object.put(USER_COMPONENTS.getKey(), customComponentsArray);
        return object;
    }

}
