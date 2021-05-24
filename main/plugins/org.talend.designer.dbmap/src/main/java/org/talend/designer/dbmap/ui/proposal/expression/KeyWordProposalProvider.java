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
package org.talend.designer.dbmap.ui.proposal.expression;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.talend.commons.runtime.xml.XmlUtil;
import org.talend.commons.ui.swt.colorstyledtext.jedit.Mode;
import org.talend.commons.ui.swt.colorstyledtext.jedit.Modes;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40Z nrousseau $
 *
 */
public class KeyWordProposalProvider implements IContentProposalProvider {

    private String languageMode;

    private String[] keyWorkKeysToLoad;

    /**
     *
     * DOC amaumont KeyWordProposalProvider constructor comment.
     *
     * @param languageMode
     * @param keyWorkKeysToLoad (ex: "KEYWORD1" or "KEYWORD2" or ...)
     */
    public KeyWordProposalProvider(String languageMode, String... keyWorkKeysToLoad) {
        super();
        this.languageMode = languageMode;
        this.keyWorkKeysToLoad = keyWorkKeysToLoad;
    }

    public String[] getKeyWords() {
        Mode mode = Modes.getMode(languageMode + XmlUtil.FILE_XML_SUFFIX);
        Object[] keywords = new Object[0];

        for (int i = 0; i < keyWorkKeysToLoad.length; i++) {
            String keywordTagName = keyWorkKeysToLoad[i];
            String[] keywordsSelected = mode.getDefaultRuleSet().getKeywords().get(keywordTagName); //$NON-NLS-1$
            keywords = ArrayUtils.addAll((Object[]) keywords, (Object[]) keywordsSelected);
        }
        return Arrays.asList(keywords).toArray(new String[0]);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.fieldassist.IContentProposalProvider#getProposals(java.lang.String, int)
     */
    public IContentProposal[] getProposals(String contents, int position) {
        String[] keyWords = getKeyWords();
        ArrayList<IContentProposal> proposals = new ArrayList<IContentProposal>();
        for (int i = 0; i < keyWords.length; i++) {
            String keyWord = keyWords[i];
            proposals.add(new KeyWordContentProposal(keyWord));

        }
        return proposals.toArray(new IContentProposal[0]);
    }

}
