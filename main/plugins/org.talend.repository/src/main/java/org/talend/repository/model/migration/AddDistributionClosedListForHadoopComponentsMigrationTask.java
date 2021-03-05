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

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * rdubois class global comment. Detailled comment
 */
public class AddDistributionClosedListForHadoopComponentsMigrationTask extends
		AbstractJobMigrationTask {

	@Override
	public ExecutionResult execute(Item item) {
		try {
			addDistributionClosedList(item);
			return ExecutionResult.SUCCESS_NO_ALERT;
		} catch (Exception e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}
	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2012, 7, 16, 12, 0, 0);
		return gc.getTime();
	}

	private void addDistributionClosedList(Item item) throws Exception {
		ProcessType processType = getProcessType(item);
		java.util.List<IComponentFilter> filters = new java.util.ArrayList<IComponentFilter>();
		filters.add(new NameComponentFilter("tHDFSConnection"));
		filters.add(new NameComponentFilter("tHDFSInput"));
		filters.add(new NameComponentFilter("tHDFSOutput"));
		filters.add(new NameComponentFilter("tHDFSPut"));
		filters.add(new NameComponentFilter("tHDFSGet"));
		filters.add(new NameComponentFilter("tHDFSDelete"));
		filters.add(new NameComponentFilter("tPigLoad"));
		filters.add(new NameComponentFilter("tHiveConnection"));
		filters.add(new NameComponentFilter("tHiveInput"));
		filters.add(new NameComponentFilter("tHiveRow"));
		filters.add(new NameComponentFilter("tHBaseConnection"));
		filters.add(new NameComponentFilter("tHBaseInput"));
		filters.add(new NameComponentFilter("tHBaseOutput"));
		filters.add(new NameComponentFilter("tHCatalogInput"));
		filters.add(new NameComponentFilter("tHCatalogLoad"));
		filters.add(new NameComponentFilter("tHCatalogOutput"));
		filters.add(new NameComponentFilter("tHCatalogOperation"));
		IComponentConversion addOption = new AddDistribution();
		java.util.Iterator<IComponentFilter> iter = filters.iterator();
		while (iter.hasNext()) {
			IComponentFilter filter = (IComponentFilter) iter.next();
			ModifyComponentsAction.searchAndModify(item, processType, filter,
					Arrays.<IComponentConversion> asList(addOption));
		}
	}

	/**
	 *
	 * rdubois AddConnectionMode class global comment. Detailled comment
	 */
	private class AddDistribution implements IComponentConversion {

		private String field = "CLOSED_LIST"; //$NON-NLS-1$

		private String name = "DISTRIBUTION"; //$NON-NLS-1$

		public AddDistribution() {
			super();
		}

		public void transform(NodeType node) {

			if (ComponentUtilities.getNodeProperty(node, name) == null) {
				ComponentUtilities.addNodeProperty(node, name, field);
			}

			for (Object o : node.getElementParameter()) {
				ElementParameterType para = (ElementParameterType) o;
				if ("DB_VERSION".equals(para.getName())) { //$NON-NLS-1$
					if ("hadoop-core-1.0.0.jar;commons-logging-1.1.1.jar;commons-configuration-1.6.jar;commons-lang-2.4.jar;hdp-dummy.jar"
							.equals(para.getValue())) {
						ComponentUtilities.setNodeValue(node, name,
								"HORTONWORKS");
					} else if ("hadoop-0.20.2-cdh3u1-core.jar;commons-logging-1.0.4.jar"
							.equals(para.getValue())
							|| "hadoop-hdfs-2.0.0-cdh4.0.1.jar;hadoop-common-2.0.0-cdh4.0.1.jar;hadoop-auth-2.0.0-cdh4.0.1.jar;commons-configuration-1.6.jar;commons-lang-2.5.jar;commons-logging-1.1.1.jar;guava-11.0.2.jar;log4j-1.2.15.jar;protobuf-java-2.4.0a.jar;slf4j-api-1.6.1.jar;slf4j-log4j12-1.6.1.jar"
									.equals(para.getValue())) {
						ComponentUtilities.setNodeValue(node, name, "CLOUDERA");
					} else if ("hadoop-0.20.2-dev-core.jar;commons-logging-1.0.4.jar;maprfs-0.1.jar;zookeeper-3.3.2.jar"
							.equals(para.getValue())) {
						ComponentUtilities.setNodeValue(node, name, "MAPR");
					} else if ("hadoop-core-1.0.0.jar;commons-logging-1.1.1.jar;commons-configuration-1.6.jar;commons-lang-2.4.jar"
							.equals(para.getValue())
							|| "hadoop-core-0.20.204.0.jar;commons-logging-1.1.1.jar;commons-configuration-1.6.jar;commons-lang-2.4.jar"
									.equals(para.getValue())
							|| "hadoop-0.20.2-core.jar;commons-logging-1.0.4.jar"
									.equals(para.getValue())) {
						ComponentUtilities.setNodeValue(node, name, "APACHE");
					}
					break;
				}
				if ("PIG_VERSION".equals(para.getName())
						|| "HBASE_VERSION".equals(para.getName())
						|| "HCAT_VERSION".equals(para.getName())
						|| "HIVE_VERSION".equals(para.getName())) {
					if ("HDP_1_0".equals(para.getValue())) {
						ComponentUtilities.setNodeValue(node, name,
								"HORTONWORKS");
					} else if ("APACHE_0_20_2".equals(para.getValue())
							|| "APACHE_1_0_0".equals(para.getValue())
							|| "APACHE_0_20_203".equals(para.getValue())) {
						ComponentUtilities.setNodeValue(node, name, "APACHE");
					} else if ("CLOUDERA_0.20_CDH3U1".equals(para.getValue())
							|| "Cloudera_CDH4".equalsIgnoreCase(para.getValue())) {
						ComponentUtilities.setNodeValue(node, name, "CLOUDERA");
					} else if ("MAPR".equals(para.getValue())) {
						ComponentUtilities.setNodeValue(node, name, "MAPR");
					}
				}
			}

		}
	}
}
