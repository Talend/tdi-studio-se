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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.core.model.runprocess.shadow.ObjectElementParameter;
import org.talend.core.model.runprocess.shadow.TextElementParameter;
import org.talend.core.model.utils.TalendTextUtils;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class FileOutputXmlNode extends ShadowNode {

    /**
     * Constructs a new FileOutputXmlNode.
     */
    public FileOutputXmlNode(String filename, String encoding) {
        super("tFileOutputShadow"); //$NON-NLS-1$
        List<Map<String, String>> rootTags = new ArrayList<Map<String, String>>();
        TextElementParameter param1 = new TextElementParameter("FILENAME", filename); //$NON-NLS-1$
        TextElementParameter param2 = new TextElementParameter("ENCODING", encoding); //$NON-NLS-1$
        ObjectElementParameter param3 = new ObjectElementParameter("ROOT_TAGS", rootTags); //$NON-NLS-1$
        TextElementParameter param4 = new TextElementParameter("ROW_TAG", TalendTextUtils.addQuotes("row")); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param5 = new TextElementParameter("COLNAME_AS_TAGNAME", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param6 = new TextElementParameter("FIELD_TAG", TalendTextUtils.addQuotes("field")); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param7 = new TextElementParameter("SPLIT", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param8 = new TextElementParameter("SPLIT_EVERY", "1000"); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param9 = new TextElementParameter("REMOVE_NULL_FIELD", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        addParameter(param1);
        addParameter(param2);
        addParameter(param3);
        addParameter(param4);
        addParameter(param5);
        addParameter(param6);
        addParameter(param7);
        addParameter(param8);
        addParameter(param9);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isStart()
     */
    public boolean isStart() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isThereLinkWithHash()
     */
    public boolean isThereLinkWithHash() {
        // TODO Auto-generated method stub
        return false;
    }

}
