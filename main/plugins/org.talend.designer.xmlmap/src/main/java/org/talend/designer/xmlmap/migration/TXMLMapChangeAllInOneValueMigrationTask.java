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
package org.talend.designer.xmlmap.migration;

import java.util.Date;
import java.util.GregorianCalendar;

import org.eclipse.emf.common.util.EList;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.xmlmap.model.emf.xmlmap.InputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.OutputXmlTree;
import org.talend.designer.xmlmap.model.emf.xmlmap.TreeNode;
import org.talend.designer.xmlmap.model.emf.xmlmap.XmlMapData;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC talend class global comment. Detailled comment
 */
public class TXMLMapChangeAllInOneValueMigrationTask extends
		AbstractJobMigrationTask {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.talend.core.model.migration.TXMLMapChangeAllInOneValueMigrationTask
	 * (org .talend.core.model.properties.Item)
	 */
	@Override
	public ExecutionResult execute(Item item) {

		IProxyRepositoryFactory factory = CorePlugin.getDefault()
				.getRepositoryService().getProxyRepositoryFactory();
		ProcessType processType = getProcessType(item);
		boolean modified = false;

		if (processType != null) {
			for (Object obj : processType.getNode()) {
				NodeType nodeType = (NodeType) obj;
				if (nodeType.getComponentName().startsWith("tXMLMap")) {
					XmlMapData xmlMapdata = (XmlMapData) nodeType.getNodeData();
					EList<OutputXmlTree> outputTables = xmlMapdata
							.getOutputTrees();
					EList<InputXmlTree> inputTables = xmlMapdata
							.getInputTrees();
					boolean hasDocumentInTheMainInputTable = false;
					for (InputXmlTree inputTable : inputTables) {
						if (!(inputTable.isLookup())) {
							for (TreeNode inputEntry : inputTable.getNodes()) {
								if ("id_Document".equals(inputEntry.getType())) {
									hasDocumentInTheMainInputTable = true;
								}
							}
						}
					}
					if (hasDocumentInTheMainInputTable) {
						for (OutputXmlTree outputTable : outputTables) {
							for (TreeNode outputEntry : outputTable.getNodes()) {
								if ("id_Document".equals(outputEntry.getType())) {
									if (!outputTable.isAllInOne()) {
										outputTable.setAllInOne(true);
										modified = true;
										break;
									}
								}
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
