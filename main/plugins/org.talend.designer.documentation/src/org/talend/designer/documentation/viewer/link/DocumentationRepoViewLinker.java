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
package org.talend.designer.documentation.viewer.link;

import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.eclipse.ui.part.EditorPart;
import org.talend.core.repository.link.AbstractEditorInputWithItemIdLinker;
import org.talend.designer.documentation.utils.DocumentationConstants;

/**
 * DOC ggu class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 55206 2011-02-15 17:32:14Z mhirt $
 *
 */
public class DocumentationRepoViewLinker extends AbstractEditorInputWithItemIdLinker {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractEditorInputWithItemIdLinker#getPartItemIdKey()
     */
    @Override
    protected String getPartItemIdKey() {
        return DocumentationConstants.KEY_ITEM_ID;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.repository.link.AbstractEditorInputWithItemIdLinker#isValidEditor(org.eclipse.ui.part.
     * EditorPart)
     */
    @Override
    protected boolean isValidEditor(EditorPart editorPart) {
        return editorPart != null && editorPart.getSite() != null
                && WebBrowserEditor.WEB_BROWSER_EDITOR_ID.equals(editorPart.getSite().getId()); // WebBrowserEditor
    }

}
