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
package org.talend.repository.json.ui.wizards.extraction;

import java.util.ArrayList;
import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.repository.model.json.JSONXPathLoopDescriptor;
import org.talend.repository.model.json.JsonFactory;

/**
 * DOC cantoine class global comment. Detailled comment <br/>
 *
 * $Id: JSONExtractorSchemaModel.java 949 2006-12-11 15:03:40Z cantoine $
 *
 */
public class JSONExtractorLoopModel extends ExtendedTableModel<JSONXPathLoopDescriptor> {

    private JSONXPathLoopDescriptor JSONXPathLoopDescriptor;

    public JSONExtractorLoopModel(String name) {
        super(name);
    }

    public JSONExtractorLoopModel(JSONXPathLoopDescriptor JSONXPathLoopDescriptor, String name) {
        super(name);
        setJSONXPathLoopDescriptor(JSONXPathLoopDescriptor);
    }

    public JSONXPathLoopDescriptor getJSONXPathLoopDescriptor() {
        return this.JSONXPathLoopDescriptor;
    }

    /**
     * set JSONXPathLoopDescriptor.
     *
     * @param JSONXPathLoopDescriptor
     */
    public void setJSONXPathLoopDescriptor(JSONXPathLoopDescriptor JSONXPathLoopDescriptor) {
        List<JSONXPathLoopDescriptor> list = new ArrayList<JSONXPathLoopDescriptor>();
        if (JSONXPathLoopDescriptor != null) {
            this.JSONXPathLoopDescriptor = JSONXPathLoopDescriptor;
            list.add(JSONXPathLoopDescriptor);
            registerDataList(list);
        } else {
            list.add(createJSONXPathLoopDescriptor());
            registerDataList(list);
        }
    }

    /**
     * DOC amaumont Comment method "createSchemaTarget".
     *
     * @return
     */
    public org.talend.repository.model.json.JSONXPathLoopDescriptor createJSONXPathLoopDescriptor() {
        return JsonFactory.eINSTANCE.createJSONXPathLoopDescriptor();
    }

}
