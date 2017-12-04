// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
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
import org.talend.designer.hl7.managers.HL7Manager;
import org.talend.designer.hl7.managers.HL7OutputManager;

/**
 * DOC hwang class global comment. Detailled comment
 */
public class HL7OutputLinkUI extends HL7OutputUI {

    private ComboViewer metaTableViewer;

    private HL7OutputManager hl7Manager;

    /**
     * DOC hwang HL7OutputLinkUI constructor comment.
     * 
     * @param parent
     * @param hl7Manager
     */
    public HL7OutputLinkUI(Composite parent, HL7Manager hl7Manager) {
        super(parent, hl7Manager);
        if (hl7Manager instanceof HL7OutputManager) {
            this.hl7Manager = (HL7OutputManager) hl7Manager;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.hl7.ui.HL7UI#createCombo(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createCombo(Composite mainComposite) {
        metaTableViewer = new ComboViewer(mainComposite, SWT.NONE | SWT.READ_ONLY);
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
                if (hl7Manager != null) {
                    ((HL7OutputManager) hl7Manager).setCurrentSchema(combo.getText());
                }
                IStructuredSelection selection = (IStructuredSelection) metaTableViewer.getSelection();
                Object obj = selection.getFirstElement();
                xmlViewer.refresh();
                xmlViewer.expandAll();
                schemaViewer.setInput(((IConnection) obj).getMetadataTable().getListColumns());
                initlinkers();
            }

        });
    }
}
