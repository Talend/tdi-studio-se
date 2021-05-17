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
package org.talend.sqlbuilder.sqleditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.talend.sqlbuilder.Messages;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.dbstructure.nodes.TableNode;
import org.talend.sqlbuilder.sessiontree.model.utility.Dictionary;

/**
 * UnsignedWordRule. <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 * @author qiang.zhang
 */
public class UnsignedWordRule implements IRule {

    protected static final int UNDEFINED = -1;

    /* The word detector used by this rule */
    protected IWordDetector fDetector;

    /**
     * The default token to be returned on success and if nothing else has been specified.
     */
    protected IToken fDefaultToken;

    /* The column constraint */
    protected int fColumn = UNDEFINED;

    /* The table of predefined words and token for this rule */
    protected Map fWords = new HashMap();

    private StringBuffer fBuffer = new StringBuffer();

    private IToken fTableToken;

    private IToken fColumnToken;

    private Dictionary dictionary;

    public UnsignedWordRule(IWordDetector detector) {
        this(detector, Token.UNDEFINED, null, null, null);

    }

    // public UnsignedWordRule(IWordDetector detector, IToken defaultToken) {
    public UnsignedWordRule(IWordDetector detector, IToken defaultToken, IToken tableToken, IToken columnToken,
            Dictionary dictionary) {

        Assert.isNotNull(detector);
        Assert.isNotNull(defaultToken);

        fDetector = detector;
        fDefaultToken = defaultToken;
        fTableToken = tableToken;
        fColumnToken = columnToken;
        this.dictionary = dictionary;

    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void addWord(String word, IToken token) {
        Assert.isNotNull(word);
        Assert.isNotNull(token);
        if (word != null) {
            word = word.toLowerCase();
        }
        fWords.put(word, token);
    }

    public void setColumnConstraint(int column) {
        if (column < 0) {
            column = UNDEFINED;
        }
        fColumn = column;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public IToken evaluate(ICharacterScanner scanner) {
        int c = scanner.read();
        if (fDetector.isWordStart((char) c)) {
            if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

                fBuffer.setLength(0);
                do {
                    fBuffer.append((char) c);
                    c = scanner.read();
                } while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
                scanner.unread();

                String tokenName = fBuffer.substring(0, fBuffer.length()).toLowerCase();

                IToken token = (IToken) fWords.get(tokenName);
                if (token != null) {

                    if ((token == fTableToken) && (dictionary != null)) {

                        ArrayList list = (ArrayList) dictionary.getByTableName(tokenName);
                        if (list != null) {
                            for (int j = 0; j < list.size(); j++) {

                                TableNode nd = (TableNode) list.get(j);
                                ArrayList ls = null;
                                try {
                                    ls = (ArrayList) nd.getColumnNames();
                                } catch (Throwable e) {
                                    SqlBuilderPlugin.log(Messages.getString("UnsignedWordRule.logMessage"), e); //$NON-NLS-1$
                                }
                                if (ls != null) {
                                    TreeSet colTree = (TreeSet) dictionary.getColumnListByTableName(tokenName);
                                    if (colTree == null && j == 0) {
                                        colTree = new TreeSet();
                                        dictionary.putColumnsByTableName(tokenName, colTree);
                                        for (int i = 0; i < ls.size(); i++) {
                                            String lo = ((String) ls.get(i));
                                            addWord(lo, fColumnToken);
                                            colTree.add(lo);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return token;
                }

                if (fDefaultToken.isUndefined()) {
                    unreadBuffer(scanner);
                }

                return fDefaultToken;
            }
        }

        scanner.unread();
        return Token.UNDEFINED;
    }

    public void unreadBuffer(ICharacterScanner scanner) {
        for (int i = fBuffer.length() - 1; i >= 0; i--) {
            scanner.unread();
        }
    }

    public int getMapSize() {
        return this.fWords.size();
    }

}
