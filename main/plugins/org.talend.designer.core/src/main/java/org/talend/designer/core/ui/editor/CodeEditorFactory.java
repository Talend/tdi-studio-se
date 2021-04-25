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
package org.talend.designer.core.ui.editor;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;

/**
 * DOC Administrator class global comment. Detailled comment <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (星期五, 29 九月 2006) nrousseau $
 *
 */
public final class CodeEditorFactory {

    private static CodeEditorFactory instance = new CodeEditorFactory();

    private CodeEditorFactory() {
    }

    public static CodeEditorFactory getInstance() {
        return instance;
    }

    public AbstractDecoratedTextEditor getCodeEditor(ECodeLanguage language, IProcess2 process) {
        return new TalendJavaEditor(process);
    }

    public ISyntaxCheckableEditor getCodeEditor(IProcess2 process) {
        ISyntaxCheckableEditor codeEditor = null;
        IEditorPart part = process.getEditor();
        if (part instanceof AbstractMultiPageTalendEditor) {
            codeEditor = ((AbstractMultiPageTalendEditor) part).getCodeEditor();
        }
        return codeEditor;
    }
}
