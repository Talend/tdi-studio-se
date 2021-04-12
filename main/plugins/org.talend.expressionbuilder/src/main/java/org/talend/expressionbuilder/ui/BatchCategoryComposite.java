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
package org.talend.expressionbuilder.ui;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.utils.generation.JavaUtils;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManagerExt;
import org.talend.expressionbuilder.i18n.Messages;
import org.talend.expressionbuilder.model.Category;
import org.talend.expressionbuilder.model.CategoryManager;
import org.talend.expressionbuilder.test.shadow.VirtualMetadataColumn;

/**
 *
 * created by hcyi on Feb 15, 2017 Detailled comment
 *
 */
public class BatchCategoryComposite extends CategoryComposite {

    public BatchCategoryComposite(Composite parent, int style, CategoryManager manager) {
        super(parent, style);
        this.manager = manager;
        setLayout(new FillLayout());

        final SashForm sashForm = new SashForm(this, SWT.NONE);
        final SashForm sashForm1 = new SashForm(sashForm, SWT.HORIZONTAL);

        final Composite composite = new Composite(sashForm1, SWT.BORDER);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        final CLabel categoryLabel = new CLabel(composite, SWT.SHADOW_OUT);
        categoryLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        categoryLabel.setText(Messages.getString("CategoryComposite.category")); //$NON-NLS-1$

        final ListViewer categoryViewer = new ListViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL);
        categoryViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((Category) element).getName();
            }
        });
        categoryViewer.setContentProvider(new ArrayContentProvider());
        categoryViewer.setSorter(new ViewerSorter());
        categoryList = categoryViewer.getList();
        categoryList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        final Composite composite1 = new Composite(sashForm1, SWT.BORDER);
        final GridLayout gridLayout1 = new GridLayout();
        gridLayout1.verticalSpacing = 0;
        gridLayout1.horizontalSpacing = 0;
        gridLayout1.marginWidth = 0;
        gridLayout1.marginHeight = 0;
        composite1.setLayout(gridLayout1);

        final CLabel functionLabel = new CLabel(composite1, SWT.SHADOW_OUT);
        functionLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        functionLabel.setText(Messages.getString("CategoryComposite.functions")); //$NON-NLS-1$

        functionViewer = new ListViewer(composite1, SWT.V_SCROLL | SWT.H_SCROLL);
        functionViewer.setContentProvider(new ArrayContentProvider());
        functionViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return ((Function) element).getFunctionString();
            }
        });
        functionViewer.setSorter(new ViewerSorter());
        functionList = functionViewer.getList();
        functionList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        // create DND in Expression Editor. This is CategoryComposite drag.
        DragSource source = new DragSource(functionList, DND.DROP_MOVE | DND.DROP_COPY);
        source.setTransfer(new Transfer[] { textTransfer });
        source.addDragListener(new DragSourceListener() {

            @Override
            public void dragStart(DragSourceEvent event) {
                if (functionList.getSelection().equals("")) {
                    event.doit = false;
                }
            }

            @Override
            public void dragSetData(DragSourceEvent event) {
                if (textTransfer.isSupportedType(event.dataType)) {
                    Function function = (Function) ((IStructuredSelection) functionViewer.getSelection()).getFirstElement();
                    VirtualMetadataColumn column = new VirtualMetadataColumn();
                    column.setTalendType(function.getTalendType().getName());
                    column.setFunction(function);
                    event.data = (FunctionManagerExt.getOneColData(column, false, true));
                }
            }

            @Override
            public void dragFinished(DragSourceEvent event) {

            }
        });

        sashForm1.setWeights(new int[] { 1, 1 });

        Composite docComposite = new Composite(sashForm, SWT.BORDER);
        GridLayout docLayout = new GridLayout();
        docLayout.marginTop = 0;
        docLayout.marginBottom = 0;
        docLayout.marginHeight = 0;
        docLayout.marginLeft = 0;
        docLayout.marginWidth = 0;
        docLayout.marginHeight = 0;
        docComposite.setLayout(docLayout);

        Label docLabel = new Label(docComposite, SWT.NONE);
        docLabel.setText(Messages.getString("CategoryComposite.Help")); //$NON-NLS-1$

        if (TalendPropertiesUtil.isEnabledUseBrowser()) {
            final Browser docDisplayer = new Browser(docComposite, SWT.BORDER);
            docDisplayer.setText(Messages.getString("CategoryComposite.SelectCategoryAndFunction")); //$NON-NLS-1$
            docDisplayer.setLayoutData(new GridData(GridData.FILL_BOTH));
            sashForm.setWeights(new int[] { 2, 1 });
            new UIRelationShipLinker(categoryViewer, functionViewer, docDisplayer);
        } else {
            final Text descriptionText = new Text(docComposite, SWT.BORDER | SWT.WRAP);
            descriptionText.setText(Messages.getString("CategoryComposite.SelectCategoryAndFunction")); //$NON-NLS-1$
            GridData gd = new GridData(GridData.FILL_BOTH);
            descriptionText.setLayoutData(gd);
            sashForm.setWeights(new int[] { 2, 1 });
            new UIRelationShipLinker(categoryViewer, functionViewer, descriptionText);
        }
        initializeData(categoryViewer);
        categoryViewer.getList().setFocus();
    }

    private void initializeData(ListViewer categoryViewer) {
        categoryViewer.setInput(manager.getInputCategory(JavaUtils.JAVA_DIRECTORY));
    }
}