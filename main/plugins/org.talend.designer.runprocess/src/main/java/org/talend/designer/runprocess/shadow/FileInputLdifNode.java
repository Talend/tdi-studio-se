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

import java.io.IOException;
import java.util.List;

import netscape.ldap.util.LDIF;
import netscape.ldap.util.LDIFAttributeContent;
import netscape.ldap.util.LDIFContent;
import netscape.ldap.util.LDIFRecord;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.utils.TalendQuoteUtils;

/**
 * DOC chuger class global comment. Detailled comment <br/>
 *
 * $Id: FileInputDelimitedNode.java 93 2006-10-04 10:02:12 +0000 (mer., 04 oct. 2006) mhirt $
 *
 */
public class FileInputLdifNode extends FileInputNode {

    private List<IMetadataTable> metadatas = null;

    /**
     * Constructs a new FileInputNode.
     */
    public FileInputLdifNode(String filename, List<IMetadataTable> metadatas, String encoding) {
        super("tFileInputLDIF"); //$NON-NLS-1$

        try {
            LDIF ldif = new LDIF(trimParameter(filename));
            int columnCount = 0;
            LDIFRecord record = ldif.nextRecord();
            int limit = 50;
            while (record != null && limit > 0) {
                LDIFContent content = record.getContent();
                if (content.getType() == LDIFContent.ATTRIBUTE_CONTENT) {
                    LDIFAttributeContent attrContent = (LDIFAttributeContent) content;
                    int length = attrContent.getAttributes().length;
                    if (length > columnCount) {
                        columnCount = length;
                        limit--;
                    }
                }
                record = ldif.nextRecord();
            }
            this.setColumnNumber(columnCount);
        } catch (IOException e) {
            // e.printStackTrace();
            ExceptionHandler.process(e);
        }

        String[] paramNames = new String[] { "FILENAME", "ENCODING", "VALUE_SEPARATOR" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String[] paramValues = new String[] { filename, encoding, TalendQuoteUtils.addQuotes(",") }; //$NON-NLS-1$

        IComponent component = ComponentsFactoryProvider.getInstance().get("tFileInputLDIF", //$NON-NLS-1$
                ComponentCategory.CATEGORY_4_DI.getName());
        this.setElementParameters(component.createElementParameters(this));
        for (int i = 0; i < paramNames.length; i++) {
            if (paramValues[i] != null) {
                IElementParameter param = this.getElementParameter(paramNames[i]);
                if (param != null) {
                    param.setValue(paramValues[i]);
                }
            }
        }
        setMetadataList(metadatas);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#getMetadataList()
     */
    @Override
    public List<IMetadataTable> getMetadataList() {
        return metadatas;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.runprocess.shadow.ShadowNode#setMetadataList(java.util.List)
     */
    @Override
    public void setMetadataList(List<IMetadataTable> metadataList) {
        this.metadatas = metadataList;
    }
}
