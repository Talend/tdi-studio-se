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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.talend.commons.runtime.xml.XmlNodeRetriever;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.metadata.managment.ui.utils.ConnectionContextHelper;
import org.talend.repository.model.json.JSONXPathLoopDescriptor;
import org.talend.repository.model.json.SchemaTarget;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;

/**
 * created by cmeng on Jul 6, 2015 Detailled comment
 *
 */
public class JSONToJsonPathLinker extends JSONToXPathLinker {

    /**
     * DOC cmeng JSONToJsonPathLinker constructor comment.
     *
     * @param commonParent
     */
    public JSONToJsonPathLinker(Composite commonParent) {
        super(commonParent);
    }

    @Override
    protected JSONXPathProposalProvider getJSONXPathProposalProvider(JSONToXPathLinker linker, boolean isRelative) {
        JSONXPathProposalProvider provider = new JSONXPathProposalProvider(linker, isRelative);
        provider.setReadbyMode(EJsonReadbyMode.JSONPATH.getValue());
        return provider;
    }

    @Override
    protected XmlNodeRetriever getXmlNodeRetriever(String filePath, String loopXPath) {
        return new JsonNodeRetriever(filePath, loopXPath);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.json.ui.wizards.extraction.JSONToXPathLinker#validateXPathExpression(java.lang.String)
     */
    @Override
    public String validateXPathExpression(String xpathExpression) {
        if (treePopulator.getTreeItem(xpathExpression) == null) {
            return "The current JsonPath expression is invalid";
        } else {
            return null;
        }
    }

    @Override
    protected TreeItem getTreeItem(Tree tree, Object dataOfTreeItem, Object dataOfTableItem) {
        String path = null;
        if (dataOfTableItem instanceof SchemaTarget) {
            SchemaTarget target = (SchemaTarget) dataOfTableItem;
            path = target.getRelativeXPathQuery();
        } else if (dataOfTableItem instanceof JSONXPathLoopDescriptor) {
            JSONXPathLoopDescriptor target = (JSONXPathLoopDescriptor) dataOfTableItem;
            path = target.getAbsoluteXPathQuery();
            if (target.getConnection().isContextMode()) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(target.getConnection(), target
                        .getConnection().getContextName());
                path = TalendQuoteUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, path));
            }
        }
        if (path == null) {
            return super.getTreeItem(tree, dataOfTreeItem, dataOfTableItem);
        }

        boolean expressionIsAbsolute = false;
        if (path.trim().startsWith(getRootSeperator())) {
            expressionIsAbsolute = true;
        }

        String fullPath = ""; //$NON-NLS-1$
        if (!expressionIsAbsolute) {
            if (0 < loopXpathNodes.size()) {
                fullPath = loopXpathNodes.get(0) + getFieldSeperator();
            }
            fullPath = fullPath + path;
        } else {
            fullPath = path;
        }
        TreeItem treeItem = treePopulator.getTreeItem(fullPath);
        if (treeItem != null) {
            return treeItem;
        } else {
            return super.getTreeItem(tree, dataOfTreeItem, dataOfTableItem);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.repository.json.ui.wizards.extraction.JSONToXPathLinker#createFieldLinks(java.lang.String,
     * org.eclipse.swt.widgets.TableItem, org.eclipse.core.runtime.IProgressMonitor,
     * org.talend.repository.model.json.SchemaTarget)
     */
    @Override
    protected void createFieldLinks(String relativeXpathPrm, TableItem tableItemTarget, IProgressMonitor progressMonitor,
            SchemaTarget schemaTarget) {
        if (relativeXpathPrm == null || relativeXpathPrm.trim().length() == 0) {
            return;
        }

        boolean expressionIsAbsolute = false;
        if (relativeXpathPrm.trim().startsWith(getRootSeperator())) {
            expressionIsAbsolute = true;
        }

        String fullPath = "";
        if (!expressionIsAbsolute) {
            if (0 < loopXpathNodes.size()) {
                fullPath = loopXpathNodes.get(0) + getFieldSeperator();
            }
            fullPath = fullPath + relativeXpathPrm;
        } else {
            fullPath = relativeXpathPrm;
        }
        TreeItem treeItemFromAbsoluteXPath = treePopulator.getTreeItem(fullPath);
        if (treeItemFromAbsoluteXPath != null) {
            addFieldLink(treeItemFromAbsoluteXPath, treeItemFromAbsoluteXPath.getData(), tableItemTarget.getParent(),
                    (SchemaTarget) tableItemTarget.getData());
        }
    }

    @Override
    protected void initListeners() {
        super.initListeners();
        jsonDndHandler.setReadbyMode(EJsonReadbyMode.JSONPATH.getValue());
    }

    @Override
    public String getFieldSeperator() {
        return "."; //$NON-NLS-1$
    }

    @Override
    public String getRootSeperator() {
        return "$"; //$NON-NLS-1$
    }
}
