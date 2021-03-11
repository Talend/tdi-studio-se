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
package org.talend.designer.fileoutputxml.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.utils.NodeUtil;
import org.talend.designer.fileoutputxml.managers.FOXManager;

/**
 * wzhang class global comment. Detailled comment
 */
public class FOXMultiSchemaUI extends FOXUI {

    private Map<String, List<String>> contents = new HashMap<String, List<String>>();

    private ComboViewer metaTableViewer;

    /**
     * wzhang FOXMultiSchemaUI constructor comment.
     *
     * @param parent
     * @param foxManager
     */
    public FOXMultiSchemaUI(Composite parent, FOXManager foxManager) {
        super(parent, foxManager);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.designer.fileoutputxml.ui.FOXUI#createCombo(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createCombo(Composite parent) {
        metaTableViewer = new ComboViewer(parent, SWT.NONE);
        metaTableViewer.setContentProvider(new ArrayContentProvider());
        metaTableViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IConnection) {
                    return ((IConnection) element).getUniqueName();
                }
                return super.getText(element);
            }

        });

        List<? extends IConnection> incomingConnections = NodeUtil.getIncomingConnections(externalNode, IConnectionCategory.FLOW);
        metaTableViewer.setInput(incomingConnections);

        final Combo combo = metaTableViewer.getCombo();
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        combo.select(0);
        combo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) metaTableViewer.getSelection();
                Object obj = selection.getFirstElement();
                if (obj instanceof IConnection) {
                    foxManager.setCurrentSchema(combo.getText());
                    // xmlViewer.setInput(foxManager.getTreeData());
                    xmlViewer.refresh();
                    schemaViewer.setInput(((IConnection) obj).getMetadataTable().getListColumns());
                    // record the schema
                    // refreshXMLViewer(null);
                    redrawLinkers();
                }
            }
        });

    }

    public IConnection getConnection() {
        ISelection selection = metaTableViewer.getSelection();
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof IConnection) {
                return ((IConnection) element);
            }
        }
        return null;
    }
}
