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
package org.talend.designer.rowgenerator.ui.editor;

import java.util.List;

import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataSchema;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;
import org.talend.designer.rowgenerator.data.Parameter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * qzhang class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2007-3-27 上午11:16:57 (星期五, 29 九月 2006) qzhang $
 *
 */
public class MetadataSchemaExt extends MetadataSchema {

    private FunctionManagerExt funManager;

    /**
     * qzhang MetadataSchemaExt constructor comment.
     */
    protected MetadataSchemaExt(FunctionManagerExt funManager) {
        super();
        this.funManager = funManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected IMetadataColumn initializeOneColumn(final IMetadataColumn metadataColumn, final NamedNodeMap nodeMap) {
        IMetadataColumn column = super.initializeOneColumn(metadataColumn, nodeMap);
        MetadataColumnExt columnExt = new MetadataColumnExt((MetadataColumn) column);
        Node function = nodeMap.getNamedItem("function"); //$NON-NLS-1$
        if (function != null) {
            Node parameter = nodeMap.getNamedItem("parameter"); //$NON-NLS-1$
            Node preview = nodeMap.getNamedItem("preview"); //$NON-NLS-1$
            Function function2 = funManager.getCurrentFunction(function.getNodeValue(), columnExt);
            List<Parameter> parms = function2.getParameters();
            String[] paraStr = parameter.getNodeValue().split(";"); //$NON-NLS-1$
            for (String string : paraStr) {
                String[] nv = string.split("=>"); //$NON-NLS-1$
                for (Parameter para : parms) {
                    if (para.getName().equals(nv[0].trim())) {
                        para.setValue(nv[1]);
                        break;
                    }
                }
            }
            columnExt.setFunction(function2);
            columnExt.setPreview(preview.getNodeValue());
        } else {
            Function function2 = funManager.getDefaultFunction(columnExt, columnExt.getTalendType());
            columnExt.setFunction(function2);
            columnExt.setPreview(""); //$NON-NLS-1$
        }
        return columnExt;
    }

    @Override
    protected void saveOneColumn(Document document, IMetadataColumn metadataColumn, Element column) {
        super.saveOneColumn(document, metadataColumn, column);
        if (metadataColumn instanceof MetadataColumnExt) {
            MetadataColumnExt columnExt = (MetadataColumnExt) metadataColumn;
            Attr function = document.createAttribute("function"); //$NON-NLS-1$
            function.setNodeValue(FunctionManagerExt.getFunctionLable(columnExt.getFunction()));
            column.setAttributeNode(function);

            Attr parameter = document.createAttribute("parameter"); //$NON-NLS-1$
            parameter.setNodeValue(columnExt.getParameter());
            column.setAttributeNode(parameter);

            Attr preview = document.createAttribute("preview"); //$NON-NLS-1$
            preview.setNodeValue(columnExt.getPreview());
            column.setAttributeNode(preview);
        }

    }
}
