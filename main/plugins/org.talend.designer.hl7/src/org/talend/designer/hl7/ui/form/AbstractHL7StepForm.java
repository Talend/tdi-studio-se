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
package org.talend.designer.hl7.ui.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.HL7Connection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.RepositoryManagerHelper;
import org.talend.designer.hl7.ui.data.HL7TreeNode;
import org.talend.metadata.managment.ui.utils.FileConnectionContextUtils.EFileParamName;
import org.talend.metadata.managment.ui.utils.OtherConnectionContextUtils.EParamName;
import org.talend.metadata.managment.ui.wizard.AbstractForm;

/**
 * DOC gcui class global comment. Detailled comment
 */
public abstract class AbstractHL7StepForm extends AbstractForm {

    protected int maximumRowsToPreview = RepositoryManagerHelper.getMaximumRowsToPreview();

    protected HL7Connection connection;

    public AbstractHL7StepForm(Composite parent, ConnectionItem connectionItem, String[] existingNames) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
    }

    public AbstractHL7StepForm(Composite parent, ConnectionItem connectionItem) {
        this(parent, connectionItem, null);
    }

    public AbstractHL7StepForm(Composite parent, ConnectionItem connectionItem, MetadataTable metadataTable,
            String[] existingNames) {
        super(parent, SWT.NONE, existingNames);
        setConnectionItem(connectionItem);
    }

    protected HL7Connection getConnection() {
        return (HL7Connection) connectionItem.getConnection();
    }

    @Override
    protected void exportAsContext() {
        collectConnParams();
        super.exportAsContext();
    }

    protected void collectConnParams() {
        addContextParams(EFileParamName.File, true);
        addContextParams(EFileParamName.Encoding, true);
        addContextParams(EParamName.XPathQuery, true);
    }

    /**
     * Subclass can implement this method to set the current text selected. Added by Marvin Wang on Sep 11, 2012.
     * 
     * @param label
     */
    public void setSelectedText(String label) {
    }

    public List<HL7TreeNode> getTreeData() {
        return new ArrayList<HL7TreeNode>();
    }

    public Map<String, HL7TreeNode> getContents() {
        return null;
    }
}
