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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.designer.scd.ScdManager;
import org.talend.designer.scd.ScdParameterConstants;
import org.talend.designer.scd.i18n.Messages;
import org.talend.designer.scd.model.Versioning;
import org.talend.designer.scd.util.SWTResourceManager;

/**
 * DOC hcw class global comment. Detailled comment
 */
public class JavaScdDialog extends AbstractScdDialog {

    /**
     * DOC hcw JavaScdDialog constructor comment.
     *
     * @param shell
     * @param scdManager
     */
    public JavaScdDialog(Shell shell, ScdManager scdManager) {
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
        SashForm sashForm = new SashForm(parent, SWT.VERTICAL | SWT.SMOOTH);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(sashForm);
        sashForm.setLayout(new GridLayout(1, true));
        sashForm.SASH_WIDTH = 1;

        Composite sashPart1 = new Composite(sashForm, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).spacing(20, 10).applyTo(sashPart1);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(sashPart1);

        Composite filterUnusedComposite = new Composite(sashPart1, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).spacing(0, 0).applyTo(filterUnusedComposite);
        GridDataFactory.fillDefaults().span(1, 2).grab(true, true).applyTo(filterUnusedComposite);

        ViewerFilter filter = createFilter(filterUnusedComposite);

        unusedFields = new FieldSection(filterUnusedComposite, scdManager, false, false);
        unusedFields.setTitle(Messages.getString("JavaScdDialog.unUsed"), SWTResourceManager.getColor(198, 195, //$NON-NLS-1$
                198));
        unusedFields.setTableInput(scdManager.getUnusedFields());
        unusedFields.getTableViewer().addFilter(filter);
        unusedFields.setSortable(true);
        GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(unusedFields.getControl());
        addContextHelp(unusedFields.getTableViewer().getTable(), "org.talend.designer.scd.unused"); //$NON-NLS-1$

        type0Fields = new FieldSection(sashPart1, scdManager, false, false);
        type0Fields.setTitle(Messages.getString("JavaScdDialog.type0Field"), SWTResourceManager.getColor(255, //$NON-NLS-1$
                146, 0));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(type0Fields.getControl());
        GridData fieldsData = new GridData(GridData.FILL_BOTH);
        fieldsData.heightHint = 100;
        type0Fields.getControl().setLayoutData(fieldsData);
        type0Fields.setTableInput(scdManager.getType0Table());
        addContextHelp(type0Fields.getTableViewer().getTable(), "org.talend.designer.scd.type0"); //$NON-NLS-1$

        type1Fields = new FieldSection(sashPart1, scdManager, false, false, ScdParameterConstants.DROP_COPY_TYPE1FIELDS);
        type1Fields.setTitle(Messages.getString("JavaScdDialog.type1Field"), SWTResourceManager.getColor(255, //$NON-NLS-1$
                203, 0));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(type1Fields.getControl());
        type1Fields.getControl().setLayoutData(fieldsData);
        type1Fields.setTableInput(scdManager.getType1Table());
        addContextHelp(type1Fields.getTableViewer().getTable(), "org.talend.designer.scd.type1"); //$NON-NLS-1$

        Composite sashPart2 = new Composite(sashForm, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).spacing(20, 10).applyTo(sashPart2);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(sashPart2);

        sourceKeys = new FieldSection(sashPart2, scdManager, false, false, ScdParameterConstants.DROP_COPY_SOURCEKEYS);
        sourceKeys.setTitle(Messages.getString("JavaScdDialog.sourceKey"), SWTResourceManager.getColor(156, 0, //$NON-NLS-1$
                255));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(sourceKeys.getControl());
        sourceKeys.setTableInput(scdManager.getSourceKeys());
        addContextHelp(sourceKeys.getTableViewer().getTable(), "org.talend.designer.scd.sourceKey"); //$NON-NLS-1$

        type2Fields = new Type2Section(sashPart2, scdManager, ScdParameterConstants.DROP_COPY_TYPE2FIELDS);
        type2Fields.setTitle(Messages.getString("JavaScdDialog.type2Field"), SWTResourceManager.getColor(255, //$NON-NLS-1$
                255, 0));
        GridDataFactory.swtDefaults().span(1, 2).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(type2Fields.getControl());
        type2Fields.setTableInput(scdManager.getType2Table());
        if (scdManager.getVersionData() != null) {
            type2Fields.setVersionInput(scdManager.getVersionData());
        } else {
            type2Fields.setVersionInput(new Versioning());
        }
        addContextHelp(type2Fields.getTableViewer().getTable(), "org.talend.designer.scd.type2"); //$NON-NLS-1$

        surrogateKeys = new SurrogateSection(sashPart2, scdManager);
        surrogateKeys.setTitle(Messages.getString("JavaScdDialog.surrogateKey"), SWTResourceManager.getColor( //$NON-NLS-1$
                214, 40, 255));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(surrogateKeys.getControl());
        surrogateKeys.setTableInput(scdManager.getSurrogateKeys());
        surrogateKeys.addContextHelp(this);

        Composite sashPart3 = new Composite(sashForm, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).spacing(20, 10).applyTo(sashPart3);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(sashPart3);

        Label placeHolder = new Label(sashPart3, SWT.NONE);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(placeHolder);

        type3Fields = new Type3Section(sashPart3, scdManager);
        type3Fields.setTitle(Messages.getString("JavaScdDialog.type3Key"), SWTResourceManager.getColor(24, //$NON-NLS-1$
                182, 255));
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(type3Fields.getControl());
        type3Fields.setTableInput(scdManager.getType3Table());
        addContextHelp(type3Fields.getTable(), "org.talend.designer.scd.type3"); //$NON-NLS-1$

        scdManager.setUnusedFieldsSource(unusedFields);
        ScdSection[] sections = { surrogateKeys, sourceKeys, type0Fields, type1Fields, type2Fields, type3Fields };
        for (ScdSection scd : sections) {
            scdManager.addUnusedFieldsTarget(scd);
        }
        sashForm.setSashWidth(3);

        // the following codes are used to adjust the heights
        int totalHeight = getDialogSize().y;
        sashForm.setWeights(new int[] { totalHeight / 4, totalHeight / 2, totalHeight / 4 });
        return sashForm;
    }

    @Override
    protected void okPressed() {
        saveState();
        super.okPressed();
    }

    @Override
    public void saveState() {

        scdManager.saveUIData(unusedFields.getTableData(), sourceKeys.getTableData(), surrogateKeys.getTableData(),
                type0Fields.getTableData(), type1Fields.getTableData(), type2Fields.getTableData(), type2Fields.getVersionData(),
                type3Fields.getTableData());

    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return getDialogSize();
    }

    protected Point getDialogSize() {
        Rectangle rect = Display.getCurrent().getClientArea();
        return new Point((int) (rect.width * 1.0 / 2), (int) (rect.height * 17.0 / 20));
    }
}
