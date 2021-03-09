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
package org.talend.designer.mapper.migration;

import java.io.StringReader;
import java.util.Date;
import java.util.GregorianCalendar;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.mapper.external.data.ExternalMapperData;
import org.talend.designer.mapper.model.emf.mapper.MapperData;
import org.talend.designer.mapper.model.emf.mapper.MapperFactory;
import org.talend.designer.mapper.utils.MapperHelper;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TMapSaveToEmfMigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {

        IProxyRepositoryFactory factory = CorePlugin.getDefault().getRepositoryService().getProxyRepositoryFactory();
        ProcessType processType = getProcessType(item);
        boolean modified = false;
        if (processType != null) {
            for (Object obj : processType.getNode()) {
                NodeType nodeType = (NodeType) obj;
                StringReader stringReader = null;
                if (nodeType.getStringData() != null && !"".equals(nodeType.getStringData())) {
                    stringReader = new StringReader(nodeType.getStringData());
                }
                final String componentName = nodeType.getComponentName();
                if (stringReader != null) {
                    if ("tMap".equals(componentName)) {
                        modified = true;
                        Unmarshaller unmarshaller = new Unmarshaller(ExternalMapperData.class);
                        unmarshaller.setWhitespacePreserve(true);
                        try {
                            ExternalMapperData externalData = (ExternalMapperData) unmarshaller.unmarshal(stringReader);
                            final MapperData emfMapperData = MapperFactory.eINSTANCE.createMapperData();
                            MapperHelper.saveDataToEmf(externalData, emfMapperData);
                            nodeType.setNodeData(emfMapperData);
                            nodeType.setStringData("");
                        } catch (MarshalException e) {
                            ExceptionHandler.process(e);
                            return ExecutionResult.FAILURE;
                        } catch (ValidationException e) {
                            ExceptionHandler.process(e);
                            return ExecutionResult.FAILURE;
                        } finally {
                            if (stringReader != null) {
                                stringReader.close();
                            }
                        }
                    }

                }

            }
        }
        try {
            if (modified) {
                factory.save(item, true);
                return ExecutionResult.SUCCESS_WITH_ALERT;
            } else {
                return ExecutionResult.SUCCESS_NO_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IProjectMigrationTask#getOrder()
     */
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2010, 11, 10, 12, 0, 0);
        return gc.getTime();
    }

}
