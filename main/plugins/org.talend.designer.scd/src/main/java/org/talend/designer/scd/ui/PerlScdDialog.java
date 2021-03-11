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
package org.talend.designer.scd.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.i18n.Messages;
import org.talend.designer.scd.model.Versioning;
import org.talend.designer.scd.util.SWTResourceManager;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class PerlScdDialog extends AbstractScdDialog {

    /**
     * DOC hcw PerlScdDialog constructor comment.
     *
     * @param shell
     * @param scdManager
     */
    public PerlScdDialog(Shell shell, ScdManager scdManager) {
        super(shell);
        this.scdManager = scdManager;
    }

    /**
     * DOC hcw Comment method "createContents".
     *
     * @param composite
     */
    @Override
    Control createScdContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).spacing(20, 10).applyTo(composite);
        GridDataFactory.fillDefaults().applyTo(composite);

        Composite filterUnusedComposite = new Composite(composite, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).applyTo(filterUnusedComposite);
        GridDataFactory.swtDefaults().grab(true, true).applyTo(filterUnusedComposite);

        ViewerFilter filter = createFilter(filterUnusedComposite);

        unusedFields = new FieldSection(filterUnusedComposite, scdManager, false, false);
        unusedFields.setTitle("Unused", SWTResourceManager.getColor(198, 195, 198)); //$NON-NLS-1$
        unusedFields.setTableInput(scdManager.getUnusedFields());
        unusedFields.getTableViewer().addFilter(filter);
        GridDataFactory.swtDefaults().grab(true, true).applyTo(unusedFields.getControl());
        addContextHelp(unusedFields.getTableViewer().getTable(), "org.talend.designer.scd.unused"); //$NON-NLS-1$

        type1Fields = new FieldSection(composite, scdManager, false, false);
        type1Fields.setTitle("Type 1 fields", SWTResourceManager.getColor(255, 203, 0)); //$NON-NLS-1$
        GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(type1Fields.getControl());
        type1Fields.setTableInput(scdManager.getType1Table());
        addContextHelp(type1Fields.getTableViewer().getTable(), "org.talend.designer.scd.type1"); //$NON-NLS-1$

        sourceKeys = new FieldSection(composite, scdManager, false, false);
        sourceKeys.setTitle(Messages.getString("PerlScdDialog.sourceKey"), SWTResourceManager.getColor(156, 0, 255)); //$NON-NLS-1$
        GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(sourceKeys.getControl());
        sourceKeys.setTableInput(scdManager.getSourceKeys());
        addContextHelp(sourceKeys.getTableViewer().getTable(), "org.talend.designer.scd.sourceKey"); //$NON-NLS-1$

        type2Fields = new Type2Section(composite, scdManager);
        type2Fields.setTitle(Messages.getString("PerlScdDialog.typeField"), SWTResourceManager.getColor(255, 255, 0)); //$NON-NLS-1$
        GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(type2Fields.getControl());
        type2Fields.setTableInput(scdManager.getType2Table());
        type2Fields.setSupportCreationType(false);
        if (scdManager.getVersionData() != null) {
            type2Fields.setVersionInput(scdManager.getVersionData());
        } else {
            type2Fields.setVersionInput(new Versioning());
        }
        addContextHelp(type2Fields.getTableViewer().getTable(), "org.talend.designer.scd.type2"); //$NON-NLS-1$

        scdManager.setUnusedFieldsSource(unusedFields);
        ScdSection[] sections = { sourceKeys, type1Fields, type2Fields };
        for (ScdSection scd : sections) {
            scdManager.addUnusedFieldsTarget(scd);
        }

        return composite;
    }

    @Override
    protected void okPressed() {
        saveState();
        super.okPressed();
    }

    @Override
    public void saveState() {
        scdManager.saveUIData(unusedFields.getTableData(), sourceKeys.getTableData(), null, null, type1Fields.getTableData(),
                type2Fields.getTableData(), type2Fields.getVersionData(), null);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        // return new Point(810, 590);
        return super.getInitialSize();
    }

}
