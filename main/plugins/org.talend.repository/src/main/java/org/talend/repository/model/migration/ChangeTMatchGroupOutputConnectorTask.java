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
package org.talend.repository.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * The migration task which will chage connector name from FLOW to FLOW_OUTPUT
 */
public class ChangeTMatchGroupOutputConnectorTask extends AbstractJobMigrationTask {

    private ProcessType processType = null;

    private final String OLDCONNECTORNAME = "FLOW"; //$NON-NLS-1$

    private final String NEWCONNECTORNAME = "FLOW_OUTPUT"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 12, 7, 0, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        processType = getProcessType(item);
        try {
            IComponentFilter filter = new NameComponentFilter("tMatchGroup"); //$NON-NLS-1$
            IComponentConversion checkGIDType = new ChangeMetadataName();
            ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays.<IComponentConversion> asList(checkGIDType));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    private class ChangeMetadataName implements IComponentConversion {

        private MetadataType newMT = null;

        @Override
        public void transform(NodeType node) {
            newMT = null;
            String metadataName = StringUtils.EMPTY;
            for (Object o : node.getMetadata()) {
                MetadataType mt = (MetadataType) o;
                String connector = mt.getConnector();
                if (OLDCONNECTORNAME.equals(connector)) {
                    metadataName = mt.getName();
                    createNewMetadata(node, mt);
                    changeConnection(metadataName);
                }
            }
            if (newMT != null) {
                node.getMetadata().add(newMT);
            }
        }

        /**
         * change connnection name
         *
         * @param metadataName
         */
        private void changeConnection(String metadataName) {
            for (Object o : processType.getConnection()) {
                ConnectionType ct = (ConnectionType) o;
                if (metadataName.equals(ct.getMetaname()) && OLDCONNECTORNAME.equals(ct.getConnectorName())) {
                    ct.setConnectorName(NEWCONNECTORNAME);
                    ct.setMetaname(NEWCONNECTORNAME);
                    break;
                }
            }

        }

        /**
         * Create new metadata
         */
        private void createNewMetadata(NodeType node, MetadataType mt) {
            newMT = EcoreUtil.copy(mt);
            newMT.setConnector(NEWCONNECTORNAME);
            newMT.setName(NEWCONNECTORNAME);

        }
    }

}
