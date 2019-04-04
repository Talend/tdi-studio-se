// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.ui.editor.nodes.Node;

/**
 * DOC hwang  class global comment. Detailled comment
 */
public class UpgradeElementHelperTest {
    
    @Test
    public void renameDataTest() {
        IComponent testComponent = ComponentsFactoryProvider.getInstance().get("tRunJob",
                ComponentCategory.CATEGORY_4_DI.getName());
        INode node = new Node(testComponent);
        IElementParameter ePara = node.getElementParameter("CONTEXTPARAMS");
        if(ePara != null) {
            List<Map<String, String>> tableValues = new ArrayList<>();
            Map<String, String> lMap = new LinkedHashMap<String, String>();
            
            LinkedHashMap<String, String> map1 = new LinkedHashMap<String, String>();
            map1.put("PARAM_NAME_COLUMN", "par1");
            LinkedHashMap<String, String> map2 = new LinkedHashMap<String, String>();
            map2.put("PARAM_VALUE_COLUMN", "row1.col1");
            
            lMap.putAll(map1);
            lMap.putAll(map2);
            
            tableValues.add(lMap);
            
            ePara.setValue(tableValues);
            
            UpgradeElementHelper.renameData(node, "row1", "row3");
            List<Map<String, String>> values = (List<Map<String, String>>) ePara.getValue();
            for (Map<String, String> line : values) {
                assertEquals("row3.col1", line.get("PARAM_VALUE_COLUMN"));
            }
            
        }
        
    }

}
