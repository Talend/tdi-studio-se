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
package org.talend.repository.generic.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.designer.core.generic.utils.SchemaUtils;
import org.talend.repository.generic.model.genericMetadata.SubContainer;
import org.talend.repository.generic.ui.common.GenericWizardPage;

/**
 *
 * created by ycbai on 2015年9月21日 Detailled comment
 *
 */
public class GenericSchemaWizardPage extends GenericWizardPage {

    private MetadataTable metadataTable;

    public GenericSchemaWizardPage(ConnectionItem connectionItem, boolean isRepositoryObjectEditable, MetadataTable metadataTable) {
        super(connectionItem, isRepositoryObjectEditable);
        this.metadataTable = metadataTable;
    }

    @Override
    public void createControl(final Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        container.setLayout(new GridLayout());
        setControl(container);
        List<MetadataTable> metadataTables = SchemaUtils.getMetadataTables(getConnection(), SubContainer.class);
        List<String> existingTableLabels = new ArrayList<>();
        for (MetadataTable metaTable : metadataTables) {
            String label = metaTable.getLabel();
            if (!label.equals(metadataTable.getLabel())) {
                existingTableLabels.add(label);
            }
        }
        String[] existingTableLabelsArray = existingTableLabels.toArray(new String[0]);
        GenericSchemaForm schemaForm = new GenericSchemaForm(container, connectionItem, metadataTable, existingTableLabelsArray);
        schemaForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        schemaForm.setReadOnly(!isRepositoryObjectEditable);
        addCheckListener(schemaForm.getChecker());
    }

    @Override
    public void setVisible(boolean visible) {
        getControl().setVisible(visible);
    }

}
