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
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id: FileOutputXmlNode.java 4749 2007-07-24 05:37:22Z ftang $
 *
 */
public class FileOutputDelimitedNode extends ShadowNode {

    /**
     * Constructs a new FileOutputXmlNode.
     */
    public FileOutputDelimitedNode(String filename, String encoding) {
        super("tFileOutputDelimited"); //$NON-NLS-1$

        TextElementParameter param0 = new TextElementParameter("CSV_OPTION", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param1 = new TextElementParameter("FILENAME", filename); //$NON-NLS-1$
        TextElementParameter param2 = new TextElementParameter("ENCODING", encoding); //$NON-NLS-1$
        TextElementParameter param3 = new TextElementParameter("CSVROWSEPARATOR", "\"\\n\""); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param4 = new TextElementParameter("FIELDSEPARATOR", TalendTextUtils.addQuotes(";")); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param5 = new TextElementParameter("LIMIT", ""); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param6 = new TextElementParameter("HEADER", ""); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param7 = new TextElementParameter("FOOTER", ""); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param8 = new TextElementParameter("ESCAPE_CHAR", TalendTextUtils.addQuotes("\\\\")); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param9 = new TextElementParameter("TEXT_ENCLOSURE", TalendTextUtils.addQuotes("\"")); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param10 = new TextElementParameter("REMOVE_EMPTY_ROW", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param11 = new TextElementParameter("SPLIT", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param12 = new TextElementParameter("SPLIT_EVERY", "1000"); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param13 = new TextElementParameter("CREATE", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        TextElementParameter param14 = new TextElementParameter("SCHEMA_OPT_NUM", "90"); //$NON-NLS-1$ //$NON-NLS-2$

        addParameter(param0);
        addParameter(param1);
        addParameter(param2);
        addParameter(param3);
        addParameter(param4);
        addParameter(param5);
        addParameter(param6);
        addParameter(param7);
        addParameter(param8);
        addParameter(param9);
        addParameter(param10);
        addParameter(param11);
        addParameter(param12);
        addParameter(param13);
        addParameter(param14);

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isStart()
     */
    @Override
    public boolean isStart() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#isThereLinkWithHash()
     */
    @Override
    public boolean isThereLinkWithHash() {
        // TODO Auto-generated method stub
        return false;
    }

}
