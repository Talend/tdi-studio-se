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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.talend.commons.runtime.xml.XmlNodeRetriever;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.swt.proposal.xpath.XPathContentProposal;
import org.talend.repository.ui.wizards.metadata.connection.files.json.AbstractTreePopulator;
import org.talend.repository.ui.wizards.metadata.connection.files.json.EJsonReadbyMode;
import org.talend.repository.ui.wizards.metadata.connection.files.json.JsonTreeNode;
import org.talend.repository.ui.wizards.metadata.connection.files.json.JsonTreePopulator;
import org.w3c.dom.Node;

/**
 * ContentProposalProvider which proposes child nodes. <br/>
 *
 * $Id: ExpressionProposalProvider.java 311 2006-11-03 07:00:19 +0000 (ven., 03 nov. 2006) amaumont $
 *
 */
public class JSONXPathProposalProvider implements IContentProposalProvider {

    public static final String EMPTY_STRING = ""; //$NON-NLS-1$

    public static final String SLASH = "/"; //$NON-NLS-1$

    private static final String PIPE = "|"; //$NON-NLS-1$

    private IContentProposalProvider[] otherContentProposalProviders;

    private JSONToXPathLinker linker;

    private boolean isRelativeTable;

    private String readbyMode;

    /**
     * Constructs a new ProcessProposalProvider.
     *
     * @param linker
     *
     * @param tables
     * @param control
     */
    public JSONXPathProposalProvider(JSONToXPathLinker linker, boolean isRelative) {
        super();
        this.readbyMode = EJsonReadbyMode.XPATH.getValue();
        this.linker = linker;
        this.isRelativeTable = isRelative;
    }

    public void init() {

    }

    public void setReadbyMode(String readbyMode) {
        this.readbyMode = readbyMode;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposalProvider#getProposals(java.lang.String, int)
     */
    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        if (EJsonReadbyMode.JSONPATH.getValue().equals(readbyMode)) {
            return getProposals4JsonPath(contents, position);
        } else {
            return getProposals4XPath(contents, position);
        }
    }

    private IContentProposal[] getProposals4JsonPath(String contents, int position) {
        AbstractTreePopulator treePopulator = linker.getTreePopulator();
        if (!(treePopulator instanceof JsonTreePopulator)) {
            return null;
        }

        XmlNodeRetriever nodeRetriever = linker.getNodeRetriever();
        if (!(nodeRetriever instanceof JsonNodeRetriever)) {
            return null;
        }
        JsonNodeRetriever jsonNodeRetriever = (JsonNodeRetriever) nodeRetriever;

        String beforeCursorExp = null;
        boolean isAbsoluteExpression = contents.trim().startsWith(linker.getRootSeperator());
        beforeCursorExp = contents.substring(0, position);
        int lastIndexFieldSeperator = beforeCursorExp.lastIndexOf(linker.getFieldSeperator());

        String currentExpr = null;
        if (0 <= lastIndexFieldSeperator) {
            currentExpr = beforeCursorExp.substring(0, lastIndexFieldSeperator);
        } else {
            currentExpr = beforeCursorExp;
        }

        currentExpr = currentExpr.trim();
        String currentWord = extractLastWord(beforeCursorExp);

        if (!isAbsoluteExpression && lastIndexFieldSeperator < 0) {
            currentWord = currentExpr;
            currentExpr = "";
        }
        if (currentWord != null) {
            currentWord = currentWord.trim();
        }

        List<JsonTreeNode> proposalNodes = jsonNodeRetriever.retrieveProposalJsonTreeNode((JsonTreePopulator) treePopulator,
                currentExpr, currentWord, isRelativeTable, isAbsoluteExpression);

        List<IContentProposal> proposals = new ArrayList<IContentProposal>();
        if (proposalNodes != null && !proposalNodes.isEmpty()) {
            Iterator<JsonTreeNode> iter = proposalNodes.iterator();
            while (iter.hasNext()) {
                JsonTreeNode jsonTreeNode = iter.next();
                JsonPathContentProposal proposal = new JsonPathContentProposal(jsonTreeNode.getLabel());
                proposals.add(proposal);
            }
        }

        return proposals.toArray(new IContentProposal[proposals.size()]);
    }

    /**
     * DOC cmeng Comment method "getProposals4XPath".
     *
     * @param contents
     * @param position
     * @return
     */
    private IContentProposal[] getProposals4XPath(String contents, int position) {
        int nodeFieldMax = 500;
        int nodeLoopMax = 3000;
        int nodeLoopNumberLimit = 10;
        int nodeFieldNumberLimit = 10;
        int currentNodeNumber = 0;

        List<IContentProposal> proposals = new ArrayList<IContentProposal>();

        String beforeCursorExp = null;
        boolean isRelativeExpression = !contents.trim().startsWith(SLASH);
        if (isRelativeTable && isRelativeExpression) {
            // beforeCursorExp = linker.getCurrentLoopXPath() + SLASH + contents;
            beforeCursorExp = contents;
        } else {
            beforeCursorExp = contents.substring(0, position);
        }
        int lastIndexSlash = beforeCursorExp.lastIndexOf(SLASH);
        int lastIndexPipe = beforeCursorExp.lastIndexOf(PIPE);

        String currentExpr = null;
        if (isRelativeTable && isRelativeExpression) {
            currentExpr = beforeCursorExp;
        }
        if (lastIndexSlash == -1 || lastIndexSlash < lastIndexPipe && lastIndexPipe != -1) {
            currentExpr = EMPTY_STRING;
        } else if (lastIndexPipe < lastIndexSlash && lastIndexPipe != -1) {
            currentExpr = beforeCursorExp.substring(lastIndexPipe + 1, lastIndexSlash + 1);
        } else if (lastIndexSlash != -1) {
            currentExpr = beforeCursorExp.substring(0, lastIndexSlash + 1);
            // currentExpr = beforeCursorExp;
        } else {
            currentExpr = beforeCursorExp;
        }

        currentExpr = currentExpr.trim();

        String currentWord = extractLastWord(beforeCursorExp);

        boolean expressionIsEmpty = currentExpr.trim().length() == 0;

        // String xPathExpression =
        //
        // // + " | " +
        // createXPathExpression(beforeCursorExp)
        // ;
        // System.out.println("#############################");
        // System.out.println("currentExpr='" + currentExpr + "'");
        // System.out.println("beforeCursorExp='"+beforeCursorExp+"'");
        // System.out.println("currentWord='"+currentWord+"'");
        // System.out.println("1");

        ArrayList<Node> allLoopNodes = linker.getAllLoopNodes();

        boolean resultsMayBeIncomplete = false;
        boolean breakAll = false;

        Set<String> alreadyAdded = new HashSet<String>();

        // ///////////////////////////////////////////////////////////////////////////////////////////////
        // ///////////////////////////////////////////////////////////////////////////////////////////////
        // XPath requests for relative XPath
        //
        if (isRelativeTable && (isRelativeExpression || expressionIsEmpty)) {
            int allNodesLoopSize = allLoopNodes.size();

            // System.out.println("nodeLoop size list : " + allNodesLoopSize);

            currentNodeNumber += allNodesLoopSize;

            if (allNodesLoopSize > nodeLoopMax) {

                resultsMayBeIncomplete = true;

            } else {

                int nodeLoopNumberOfLoop = allNodesLoopSize;
                if (allNodesLoopSize > nodeLoopNumberLimit) {
                    nodeLoopNumberOfLoop = nodeLoopNumberLimit;
                    resultsMayBeIncomplete = true;
                }

                for (int i = 0; i < nodeLoopNumberOfLoop; i++) {
                    Node nodeLoop = allLoopNodes.get(i);

                    // System.out.println("nodeLoop : " + i);

                    List<Node> nodeList = null;

                    try {

                        nodeList = this.linker.getNodeRetriever().retrieveNodeListFromNode(
                                modifyXpathToSearchAllChildren(currentExpr, true), nodeLoop);
                    } catch (XPathExpressionException e) {
                        ExceptionHandler.process(e);
                    }
                    if (nodeList != null && nodeList.size() == 0) {
                        try {
                            nodeList = linker.getNodeRetriever().retrieveNodeListFromNode(
                                    modifyXpathToSearchAllChildren(beforeCursorExp, true), nodeLoop);
                        } catch (XPathExpressionException e) {
                            ExceptionHandler.process(e);
                        }
                    }
                    // System.out.println("nodeList.size()="+nodeList.size());

                    if (nodeList != null) {

                        int allNodesFieldsLoopSize = nodeList.size();
                        currentNodeNumber += allNodesFieldsLoopSize;
                        if (allNodesFieldsLoopSize > nodeFieldMax) {

                            resultsMayBeIncomplete = true;
                            breakAll = true;

                        } else {

                            int nodeFieldNumberOfLoop = allNodesFieldsLoopSize;
                            if (allNodesFieldsLoopSize > nodeFieldMax) {
                                nodeFieldNumberOfLoop = nodeFieldNumberLimit;
                                resultsMayBeIncomplete = true;
                            }

                            for (int j = 0; j < nodeFieldNumberOfLoop; ++j) {
                                // System.out.println("nodeField : " + j);
                                Node node = nodeList.get(j);
                                String nodeName = node.getNodeName();
                                String absoluteXPathFromNode = linker.getNodeRetriever().getAbsoluteXPathFromNode(node);
                                if ((currentWord.length() > 0 && nodeName.startsWith(currentWord) || currentWord.length() == 0 || currentWord
                                        .equals("/")) //$NON-NLS-1$
                                        && !alreadyAdded.contains(absoluteXPathFromNode)) {
                                    XPathContentProposal contentProposal = new XPathContentProposal(node);
                                    if (isRelativeTable && isRelativeExpression) {
                                        contentProposal.setRelative(isRelativeTable);
                                        contentProposal.setFirstRelativeNode(contents.indexOf(SLASH) == -1);
                                    }
                                    proposals.add(contentProposal);
                                    alreadyAdded.add(absoluteXPathFromNode);
                                }

                            } // for (int j = 0; j < nodeListLength; ++j) {

                        }
                        if (breakAll) {
                            break;
                        }

                    } // if (nodeList != null) {

                } // for (int i = 0; i < lstSize; i++) {

            } // } else {

        } // if (!estimationError) {
          // ///////////////////////////////////////////////////////////////////////////////////////////////
          // ///////////////////////////////////////////////////////////////////////////////////////////////

        // ///////////////////////////////////////////////////////////////////////////////////////////////
        // ///////////////////////////////////////////////////////////////////////////////////////////////
        // XPath requests for absolute XPath
        //
        List<Node> nodeList = null;

        if (!expressionIsEmpty) {
            try {

                nodeList = this.linker.getNodeRetriever().retrieveNodeList(modifyXpathToSetFirstAscendant(currentExpr));
            } catch (XPathExpressionException e) {
                ExceptionHandler.process(e);
            }
        }

        if (nodeList != null || expressionIsEmpty) {
            if (!expressionIsEmpty && nodeList.size() > nodeLoopMax) {

                resultsMayBeIncomplete = true;

            } else {
                try {
                    nodeList = this.linker.getNodeRetriever()
                            .retrieveNodeList(modifyXpathToSearchAllChildren(currentExpr, false));
                } catch (XPathExpressionException e) {
                    ExceptionHandler.process(e);
                }
            }

            if (nodeList != null) {

                for (int j = 0; j < nodeList.size(); ++j) {
                    // System.out.println("nodeField : " + j);
                    Node node = nodeList.get(j);
                    String nodeName = node.getNodeName();
                    String absoluteXPathFromNode = linker.getNodeRetriever().getAbsoluteXPathFromNode(node);
                    if ((currentWord.length() > 0 && nodeName.startsWith(currentWord) || currentWord.length() == 0 || currentWord
                            .equals("/")) //$NON-NLS-1$
                            && !alreadyAdded.contains(absoluteXPathFromNode)) {
                        // System.out.println(absoluteXPathFromNode);
                        XPathContentProposal contentProposal = new XPathContentProposal(node);
                        proposals.add(contentProposal);
                        alreadyAdded.add(absoluteXPathFromNode);
                    }

                } // for (int j = 0; j < nodeListLength; ++j) {

            } // if (nodeList != null) {

        }
        // ///////////////////////////////////////////////////////////////////////////////////////////////
        // ///////////////////////////////////////////////////////////////////////////////////////////////

        if (resultsMayBeIncomplete) {
            addTooManyNodesContentProposal(proposals);
        }

        IContentProposal[] res = new IContentProposal[proposals.size()];

        res = proposals.toArray(res);
        return res;
    }

    /**
     * DOC amaumont Comment method "modifyXpathToSetFirstAscendant".
     *
     * @param currentExpr
     * @return
     */
    private String modifyXpathToSetFirstAscendant(String currentExpr) {

        if (currentExpr.trim().length() > 0 && !currentExpr.equals("/")) { //$NON-NLS-1$

            do {

                currentExpr = currentExpr.trim().substring(0, currentExpr.length() - 1);

            } while (currentExpr.trim().endsWith("/")); //$NON-NLS-1$
        }

        return currentExpr;
    }

    /**
     * DOC amaumont Comment method "addTooManyNodesContentProposal".
     *
     * @param proposals
     */
    private void addTooManyNodesContentProposal(List<IContentProposal> proposals) {
        XPathContentProposal contentProposal = new XPathContentProposal(
                "<< Too many nodes, proposed values may be incomplete ... >>");
        proposals.add(contentProposal);
    }

    /**
     * Extract last word of an expression, the last character must be a letter or a number.
     *
     * @param currentExpr
     * @return
     */
    public static String extractLastWord(String currentExpr) {
        int size = currentExpr.length();
        for (int i = size - 1; i >= 0; i--) {
            if (!("" + currentExpr.charAt(i)).matches("\\w")) { //$NON-NLS-1$ //$NON-NLS-2$
                return currentExpr.substring(i + 1, currentExpr.length());
            }
        }
        return currentExpr;
    }

    /**
     * DOC amaumont Comment method "createXPathExpression".
     *
     * @param currentExpr
     * @param isRelative TODO
     * @param slash
     *
     * @return
     */
    private String modifyXpathToSearchAllChildren(String currentExpr, boolean isRelative) {
        String xPathExpression;
        String slash = SLASH;

        if (currentExpr.trim().equals("") && isRelative) { //$NON-NLS-1$
            currentExpr = "."; //$NON-NLS-1$
        }

        if (currentExpr.endsWith(SLASH)) {
            slash = EMPTY_STRING;
        }

        xPathExpression = currentExpr + slash + "*" + " | " + currentExpr + slash + "@*"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // System.out.println("xPathExpression='"+xPathExpression+"'");
        return xPathExpression;
    }

    /**
     * Getter for otherContentProposalProviders.
     *
     * @return the otherContentProposalProviders
     */
    public IContentProposalProvider[] getOtherContentProposalProviders() {
        return this.otherContentProposalProviders;
    }

    /**
     * Sets the otherContentProposalProviders.
     *
     * @param otherContentProposalProviders the otherContentProposalProviders to set
     */
    public void setOtherContentProposalProviders(IContentProposalProvider[] otherContentProposalProviders) {
        this.otherContentProposalProviders = otherContentProposalProviders;
    }

}
