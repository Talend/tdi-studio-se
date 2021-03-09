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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.talend.core.CorePlugin;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.designer.core.i18n.Messages;

public class AppearancePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private final String groupName = "Font"; //$NON-NLS-1$

    private FontFieldEditor memoFontEditor = null;

    private FontFieldEditor consoleFontField = null;

    //    public static final String CONSOLE_FONT = "talendOutputConsoleFont"; //$NON-NLS-1$

    public AppearancePreferencePage() {
        super(GRID);
        IPreferenceStore store = CorePlugin.getDefault().getPreferenceStore();
        setPreferenceStore(store);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        addField(new BooleanFieldEditor(ITalendCorePrefConstants.CONTEXT_GROUP_BY_SOURCE,
                Messages.getString("AppearancePreferencePage.groupBySource"), //$NON-NLS-1$
                getFieldEditorParent()));
        Composite parent = getFieldEditorParent();
        addFields(parent);
    }

    protected void addFields(Composite parent) {
        Composite main = createPageLayout(parent);
        createFontAndColorGroup(main);
    }

    private Composite createPageLayout(Composite parent) {
        Composite main = new Composite(parent, SWT.NULL);
        main.setLayout(new GridLayout());
        main.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        return main;
    }

    protected Composite createFontAndColorGroup(Composite parent) {

        Group group = new Group(parent, SWT.NONE);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout(3, false));
        Composite composite = new Composite(group, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        composite.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = 3;
        composite.setLayoutData(gridData);
        group.setText(groupName);

        addFontAndColorFields(composite);

        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 8;
        composite.setLayout(layout);

        return group;
    }

    protected void addFontAndColorFields(Composite composite) {
        memoFontEditor = new FontFieldEditor(TalendDesignerPrefConstants.MEMO_TEXT_FONT,
                Messages.getString("AppearancePreferencePage.textFont"), composite); //$NON-NLS-1$
        addField(memoFontEditor);

        consoleFontField = new FontFieldEditor(TalendDesignerPrefConstants.CONSOLT_TEXT_FONT,
                Messages.getString("AppearancePreferencePage.consoleFont"), composite); //$NON-NLS-1$
        addField(consoleFontField);

    }

    public void init(IWorkbench workbench) {
    }

    @Override
    public void dispose() {
        super.dispose();
        // TDI-21143 : Studio repository view : remove all refresh call to repo view
        // IRepositoryView view = RepositoryManagerHelper.findRepositoryView();
        // if (view != null) {
        // view.refresh();
        // }
    }

    @Override
    public boolean performOk() {
        boolean ok = super.performOk();
        if (ok) {
            CorePlugin.getDefault().getDesignerCoreService().switchToCurContextsView();
        }
        return ok;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.gmf.runtime.common.ui.preferences.AbstractPreferencePage#initHelp()
     */

}
