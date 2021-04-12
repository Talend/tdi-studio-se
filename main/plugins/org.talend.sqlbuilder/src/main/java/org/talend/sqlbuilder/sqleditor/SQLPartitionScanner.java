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
import java.util.List;

import net.sourceforge.sqlexplorer.IConstants;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * SQLPartitionScanner. <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public class SQLPartitionScanner extends RuleBasedPartitionScanner {

    // private final static String SKIP= "__skip"; //$NON-NLS-1$

    /**
     * Detector for empty comments.
     */
    static class EmptyCommentDetector implements IWordDetector {

        public boolean isWordStart(char c) {
            return (c == '/');
        }

        public boolean isWordPart(char c) {
            return (c == '*' || c == '/');
        }
    };

    /**
     * EmptyCommentRule.
     */
    static class EmptyCommentRule extends WordRule implements IPredicateRule {

        private IToken fSuccessToken;

        /**
         * Constructor for EmptyCommentRule.
         *
         * @param defaultToken
         */
        public EmptyCommentRule(IToken successToken) {
            super(new EmptyCommentDetector());
            fSuccessToken = successToken;
            addWord("/**/", fSuccessToken); //$NON-NLS-1$
        }

        /*
         * @see IPredicateRule#evaluate(ICharacterScanner, boolean)
         */
        public IToken evaluate(org.eclipse.jface.text.rules.ICharacterScanner scanner, boolean resume) {
            return evaluate(scanner);
        }

        /*
         * @see IPredicateRule#getSuccessToken()
         */
        public IToken getSuccessToken() {
            return fSuccessToken;
        }
    };

    /**
     * Creates the partitioner and sets up the appropriate rules.
     */
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public SQLPartitionScanner() {
        super();

        // IToken skip= new Token(SKIP);
        IToken string = new Token(IConstants.SQL_STRING);
        IToken multiLineComment = new Token(IConstants.SQL_MULTILINE_COMMENT);
        IToken singleLineComment = new Token(IConstants.SQL_SINGLE_LINE_COMMENT);

        List rules = new ArrayList();

        // Add rule for single line comments.
        rules.add(new EndOfLineRule("--", singleLineComment)); //$NON-NLS-1$

        // Add rule for strings.
        rules.add(new SingleLineRule("'", "'", string)); //$NON-NLS-2$ //$NON-NLS-1$

        // Add special case word rule.
        EmptyCommentRule wordRule = new EmptyCommentRule(multiLineComment);
        rules.add(wordRule);

        // Add rules for multi-line comments and javadoc.
        rules.add(new MultiLineRule("/*", "*/", multiLineComment)); //$NON-NLS-1$ //$NON-NLS-2$

        IPredicateRule[] result = new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);

    }
}
