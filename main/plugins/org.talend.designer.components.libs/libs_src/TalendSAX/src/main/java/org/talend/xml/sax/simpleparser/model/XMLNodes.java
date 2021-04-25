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
package org.talend.xml.sax.simpleparser.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class XMLNodes {

    private Map<String, XMLNode> nodes = new HashMap<String, XMLNode>();

    private String loopPath = null;

    private String originLoopPath = null;

    public void addNode(XMLNode node) {
        if (node != null) {
            nodes.put(node.originPath, node);
        }
    }

    public XMLNode getNode(String nodePath) {
        return nodes.get(nodePath);
    }

    public Map<String, XMLNode> getNodes() {
        return nodes;
    }

    public Collection<XMLNode> getNodesCollection() {
        return nodes.values();
    }

    public String getLoopPath() {
        if (this.loopPath != null)
            return this.loopPath;

        for (XMLNode node : nodes.values()) {
            if (node.loopPath != null) {
                this.loopPath = node.loopPath;
                return node.loopPath;
            }
        }

        return null;
    }

    public void resetAll() {
        for (XMLNode node : nodes.values()) {
            if (node.loopPath != null) {
                node.isLooping = false;
                node.outputText = false;
                node.hasValue = false;
                node.resetValue();// value=null
            }
        }
    }

    public int size() {
        return nodes.size();
    }

    /**
     * DOC talend2 Comment method "getOriginalLoopPath".
     * @return
     */
    public String getOriginalLoopPath() {
        if (this.originLoopPath != null)
            return this.originLoopPath;

        for (XMLNode node : nodes.values()) {
            if (node.originLoopPath != null) {
                this.originLoopPath = node.originLoopPath;
                return node.originLoopPath;
            }
        }

        return null;
    }

}
