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

import org.talend.commons.runtime.xml.XmlNodeRetriever;
import org.talend.repository.ui.wizards.metadata.connection.files.json.JsonTreeNode;
import org.talend.repository.ui.wizards.metadata.connection.files.json.JsonTreePopulator;
import org.talend.repository.ui.wizards.metadata.connection.files.json.SchemaPopulationUtil;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class JsonNodeRetriever extends XmlNodeRetriever {

    /**
     * DOC amaumont XMLNodeRetriever constructor comment.
     *
     * @param string
     */
    public JsonNodeRetriever(String filePath, String loopXPath) {
        super();
        this.currentLoopXPath = loopXPath;
    }

    /**
     * Default constructor.
     */
    public JsonNodeRetriever() {
        super();
    }

    public List<JsonTreeNode> retrieveProposalJsonTreeNode(JsonTreePopulator populator, String jsonPath, String word,
            boolean isRelativeTable, boolean isAbsolutePath) {
        List<JsonTreeNode> rootJsonTreeNodes = populator.getRootJsonTreeNodes();
        if (rootJsonTreeNodes == null || rootJsonTreeNodes.isEmpty()) {
            return null;
        }
        List<JsonTreeNode> proposalNodes = new ArrayList<JsonTreeNode>();
        String fullPath = null;
        if (isRelativeTable) {
            if (isAbsolutePath) {
                fullPath = jsonPath;
            } else {
                fullPath = currentLoopXPath;
                if (jsonPath != null && !jsonPath.isEmpty()) {
                    fullPath = fullPath + "." + jsonPath; //$NON-NLS-1$
                }
            }
        } else {
            if (isAbsolutePath) {
                fullPath = jsonPath;
            } else {
                return rootJsonTreeNodes;
            }
        }

        JsonTreeNode treeNode = SchemaPopulationUtil.getJsonTreeNodeByJsonPath(rootJsonTreeNodes.toArray(),
                SchemaPopulationUtil.getFilteredJsonPath(fullPath));
        Object[] treeNodeChildren = treeNode.getChildren();
        if (treeNodeChildren == null || treeNodeChildren.length <= 0) {
            return null;
        }
        if (word != null && !word.isEmpty()) {
            word = word.toLowerCase();
        } else {
            word = null;
        }
        for (Object obj : treeNodeChildren) {
            if (!(obj instanceof JsonTreeNode)) {
                continue;
            }
            JsonTreeNode proposalNode = (JsonTreeNode) obj;
            if (word != null) {
                String name = proposalNode.getLabel();
                if (name != null && name.toLowerCase().startsWith(word)) {
                    proposalNodes.add(proposalNode);
                }
            } else {
                proposalNodes.add(proposalNode);
            }
        }
        return proposalNodes;
    }
}
