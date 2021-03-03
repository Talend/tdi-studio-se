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
package org.talend.designer.core.ui.editor.properties.controllers.uidialog.tns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * rshi class global comment. Detailled comment
 */
public class TnsParser {

    private final char leftParenthesis = '(';

    private final char rightParenthesis = ')';

    private final char equalMark = '=';

    private final char blank = ' ';

    private final String remarkString = "#"; //$NON-NLS-1$

    private TnsNodeModel tree = null;

    public TnsNodeModel getTree() {
        return tree;
    }

    public void setTree(TnsNodeModel tree) {
        this.tree = tree;
    }

    public TnsParser(String tnsFile) {
        this(new File(tnsFile));
    }

    public TnsParser(File tnsFile) {
        super();
        // remove all \r\n ' ' from tns file
        StringBuilder contentBuffer = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(tnsFile));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                temp = temp.replaceAll(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
                contentBuffer.append(temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> stack = new ArrayList<String>();
        List<TnsNodeModel> nodeStack = new ArrayList<TnsNodeModel>();
        String valueString = ""; //$NON-NLS-1$
        boolean rootFlag = true;
        int startRootInext = 0;
        int valueIndex = 0;

        tree = new TnsNodeModel("ROOT", 0); //$NON-NLS-1$

        for (int i = 0; i < contentBuffer.length(); i++) {
            if (contentBuffer.charAt(i) == equalMark && rootFlag) {
                TnsNodeModel node = new TnsNodeModel(contentBuffer.substring(startRootInext, i), null, tree.getKey(), 1);
                tree.addChildren(node);
                nodeStack.add(node);
                rootFlag = false;
            }

            if (contentBuffer.charAt(i) == leftParenthesis) {
                // =( push
                int j = i;
                while (contentBuffer.charAt(j) != equalMark) {
                    j++;
                }
                stack.add(contentBuffer.substring(i + 1, j));
                TnsNodeModel node = new TnsNodeModel(contentBuffer.substring(i + 1, j), null, nodeStack.get(nodeStack.size() - 1)
                        .getKey());
                nodeStack.get(nodeStack.size() - 1).addChildren(node);
                nodeStack.add(node);
                i = j;
            }

            if (contentBuffer.charAt(i) == equalMark) {
                valueIndex = i;
            }

            if (contentBuffer.charAt(i) == rightParenthesis) { // ) pop
                valueString = contentBuffer.substring(valueIndex + 1, i).replaceAll("\\)", ""); //$NON-NLS-1$ //$NON-NLS-2$
                if (contentBuffer.charAt(i) != contentBuffer.charAt(i - 1)) {
                    nodeStack.get(nodeStack.size() - 1).setValue(valueString);
                }
                nodeStack.remove(nodeStack.size() - 1);
                stack.remove(stack.size() - 1);

            }

            if (stack.size() == 0 && !rootFlag && contentBuffer.charAt(i - 1) == rightParenthesis) {
                startRootInext = i + 1;
                rootFlag = true;
            }

        }
    }
}
