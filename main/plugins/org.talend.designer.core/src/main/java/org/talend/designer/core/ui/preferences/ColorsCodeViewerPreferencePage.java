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
package org.talend.designer.core.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.commons.ui.swt.colorstyledtext.ColorManager;
import org.talend.commons.ui.swt.colorstyledtext.prefs.BooleanColorFieldEditor;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.i18n.Messages;

/**
 * Preference for the colors of the code view.
 *
 * $Id$
 *
 */
public class ColorsCodeViewerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ColorsCodeViewerPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        Composite p = getFieldEditorParent();
        String b = Messages.getString("ColorsCodeViewerPreferencePage.fieldTip"); //$NON-NLS-1$
        addField(new BooleanColorFieldEditor(ColorManager.NULL_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.defaultColor"), ColorManager.NULL_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.KEYWORD1_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.keyword1Color"), ColorManager.KEYWORD1_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.KEYWORD2_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.keyword2Color"), ColorManager.KEYWORD2_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.KEYWORD3_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.keyword3Color"), ColorManager.KEYWORD3_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.COMMENT1_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.Comment1Color"), ColorManager.COMMENT1_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.COMMENT2_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.Comment2Color"), ColorManager.COMMENT2_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.LITERAL1_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.Literal1Color"), ColorManager.LITERAL1_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.LITERAL2_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.Literal2Color"), ColorManager.LITERAL2_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.LABEL_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.LabelColor"), ColorManager.LABEL_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.FUNCTION_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.FunctionColor"), ColorManager.FUNCTION_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.MARKUP_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.MarkupColor"), ColorManager.MARKUP_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.OPERATOR_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.OperatorColor"), ColorManager.OPERATOR_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.DIGIT_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.DigitColor"), ColorManager.DIGIT_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
        addField(new BooleanColorFieldEditor(ColorManager.INVALID_COLOR,
                Messages.getString("ColorsCodeViewerPreferencePage.InvalidColor"), ColorManager.INVALID_COLOR //$NON-NLS-1$
                        + ColorManager.BOLD_SUFFIX, b, p));
    }

    @Override
    public void init(IWorkbench workbench) {
    }

}
