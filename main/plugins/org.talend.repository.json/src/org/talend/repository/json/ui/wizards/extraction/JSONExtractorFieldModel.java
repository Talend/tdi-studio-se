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

import java.util.List;

import org.talend.commons.ui.swt.extended.table.ExtendedTableModel;
import org.talend.repository.model.json.JsonFactory;
import org.talend.repository.model.json.SchemaTarget;

/**
 * DOC cantoine class global comment. Detailled comment <br/>
 *
 * $Id: JSONExtractorFieldModel.java 38013 2010-03-05 14:21:59Z mhirt $
 *
 */
public class JSONExtractorFieldModel extends ExtendedTableModel<SchemaTarget> {

    public JSONExtractorFieldModel(String name) {
        super(name);
    }

    public JSONExtractorFieldModel(List schemaTargetList, String name) {
        super(name);
        setJSONXPathLoopDescriptor(schemaTargetList);
    }

    /**
     * set XmlXPathLoopDescriptor.
     *
     * @param schemaTargetList
     */
    public void setJSONXPathLoopDescriptor(List<SchemaTarget> schemaTargetList) {
        registerDataList((List<SchemaTarget>) schemaTargetList);
    }

    /**
     * DOC amaumont Comment method "createSchemaTarget".
     *
     * @return
     */
    public SchemaTarget createNewSchemaTarget() {
        return JsonFactory.eINSTANCE.createSchemaTarget();
    }

}
