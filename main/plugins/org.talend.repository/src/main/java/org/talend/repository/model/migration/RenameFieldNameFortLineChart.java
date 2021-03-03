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

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.core.model.utils.emf.talendfile.ColumnType;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.MetadataType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ColumnTypeImpl;

/**
 * rename the fixed field "serie" into "series", since it is wrong word
 * @author ytao
 *
 */
public class RenameFieldNameFortLineChart extends AbstractJobMigrationTask {

	private List<String> listSourceComponent = new ArrayList<String>();
	private List<String> listComponent = new ArrayList<String>();

	@Override
	public ExecutionResult execute(Item item) {
		try {
			renameField(item);
			ProxyRepositoryFactory.getInstance().save(item, true);
			return ExecutionResult.SUCCESS_NO_ALERT;
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
	}

	// search all components that tLineChart linked directly or indirectly
	private void searchSourceComponents(Item item) {
		ProcessType processType = getProcessType(item);

		for (Object o : processType.getConnection()) {
			if (o instanceof ConnectionType) {
				ConnectionType currentConnection = (ConnectionType) o;
				String connectorName = currentConnection.getConnectorName();

				if (listComponent.contains(currentConnection.getTarget())){
					String sourceComponentName = currentConnection.getSource();

					if (!"ITERATE".equals(connectorName) &&
						!"SUBJOB_OK".equals(connectorName) &&
						!"SUBJOB_ERROR".equals(connectorName) &&
						!"COMPONENT_OK".equals(connectorName) &&
						!"COMPONENT_ERROR".equals(connectorName) &&
						!"RUN_IF".equals(connectorName)){

						if (!listSourceComponent.contains(sourceComponentName)){
							listSourceComponent.add(sourceComponentName);
						}

						if (!listComponent.contains(sourceComponentName)){
							listComponent.add(sourceComponentName);
							searchSourceComponents(item);
						}
					}
				}
			}
		} // end search
	}

	private void renameField(Item item){
		ProcessType processType = getProcessType(item);
		String uniqueComponentName = null;

		// search tLineChart components
		for (Object o : processType.getNode()) {

			if (o instanceof NodeType) {
				NodeType currentNode = (NodeType) o;
				String currentNodeName = currentNode.getComponentName();

				if ("tLineChart".equals(currentNodeName)){

					if (currentNode.getMetadata().size() > 0){

						for (Object e : currentNode.getElementParameter()) {
							ElementParameterType p = (ElementParameterType) e;

							if ("UNIQUE_NAME".equals(p.getName())) {
								uniqueComponentName = p.getValue();
								listComponent.add(uniqueComponentName);
							}
						}
					}
				}
			}
		} // end search
		searchSourceComponents(item);

		// correct the word
		for (Object o : processType.getNode()) {
			if (o instanceof NodeType) {
				NodeType currentNode = (NodeType) o;

				for (MetadataType metadata : (EList<MetadataType>) currentNode.getMetadata()){

					for (Object e : currentNode.getElementParameter()) {
						ElementParameterType p = (ElementParameterType) e;

						if ("UNIQUE_NAME".equals(p.getName())) {
							uniqueComponentName = p.getValue();

							if (listComponent.contains(uniqueComponentName)) {
								EList<ColumnType> columns =  metadata.getColumn();

								for (ColumnType column : columns) {
									if ("serie".equals(column.getName())){
										column.setName("series");
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2010, 01, 28, 17, 47, 0);
		return gc.getTime();
	}
}

