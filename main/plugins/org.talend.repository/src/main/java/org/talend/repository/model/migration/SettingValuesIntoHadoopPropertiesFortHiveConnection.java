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

import org.eclipse.emf.common.util.EList;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 *
 * created by jjzhou on 2012-12-21 Detailled comment
 *
 */

public class SettingValuesIntoHadoopPropertiesFortHiveConnection extends AbstractJobMigrationTask {

    public ExecutionResult execute(Item item) {
        final ProcessType processType = getProcessType(item);
        try {
        	IComponentFilter filter = new NameComponentFilter("tELTHiveMap"); //$NON-NLS-1$
        	ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                    .<IComponentConversion> asList(new IComponentConversion() {
                    	public void transform(NodeType node) {
                    		ElementParameterType useExistingConn = ComponentUtilities.getNodeProperty(node, "USE_EXISTING_CONNECTION"); //$NON-NLS-1$ //$NON-NLS-2$
                    		if (useExistingConn != null && "true".equalsIgnoreCase(useExistingConn.getValue())) {
                    			ElementParameterType connNamePara = ComponentUtilities.getNodeProperty(node, "CONNECTION"); //$NON-NLS-1$
                    			if (connNamePara != null) {
                    				NodeType tHiveConnNode = searchNodeTypeByUniqName(processType, connNamePara.getValue());
                    				moveValuesFromHiveMap2HiveConn(node, tHiveConnNode);
                    			}

                    		}
                    	}
                    }));
            return ExecutionResult.SUCCESS_NO_ALERT;
        } catch (PersistenceException e) {
            // TODO Auto-generated catch block
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /*
     * seach the Node in the process according to the unique name of the node
     */
	private NodeType searchNodeTypeByUniqName(ProcessType processType, String uniqName) {
		if (processType == null || (uniqName == null || "".equals(uniqName.trim())) ) {
			return null;
		}
		NodeType searchNode = null;
		EList<NodeType> nodes = processType.getNode();
		for (NodeType node : nodes) {
			if (node.getComponentName().equals("tHiveConnection")) { //$NON-NLS-1$
				ElementParameterType uniqNameParam =ComponentUtilities.getNodeProperty(node, "UNIQUE_NAME");
				if (uniqNameParam != null && uniqNameParam.getValue().equals(uniqName)) {
					searchNode = node;
					break;
				}
			}
		}

		return searchNode;
	}

	/* <p>
	 * Copy the values of these parameters:SET_MAPRED_JT, MAPRED_JT, SET_FS_DEFAULT_NAME, FS_DEFAULT_NAME and HADOOP_ADVANCED_PROPERTIES
	 * From the tELTHiveMap to the tHiveConnection when the "Use Existing Connection" in tELTHiveMap is ticked.
	 * </p>
	 */
	private void moveValuesFromHiveMap2HiveConn(NodeType tHiveMapNode, NodeType tHiveConnNode) {
		if (tHiveMapNode == null || tHiveConnNode == null) {
			return;
		}

		ElementParameterType setMapredJt = ComponentUtilities.getNodeProperty(tHiveMapNode, "SET_MAPRED_JT"); //$NON-NLS-1$
		ElementParameterType mapredJt = ComponentUtilities.getNodeProperty(tHiveMapNode, "MAPRED_JT"); //$NON-NLS-1$
		if ((setMapredJt != null && "true".equalsIgnoreCase(setMapredJt.getValue())) && mapredJt != null) {
			ElementParameterType setMapredJt2 = ComponentUtilities.getNodeProperty(tHiveConnNode, "SET_MAPRED_JT"); //$NON-NLS-1$
			ElementParameterType mapredJt2 = ComponentUtilities.getNodeProperty(tHiveConnNode, "MAPRED_JT"); //$NON-NLS-1$
			if (setMapredJt2 == null) {
				ComponentUtilities.addNodeProperty(tHiveConnNode, "SET_MAPRED_JT", "CHECK");
			}
			ComponentUtilities.setNodeValue(tHiveConnNode, "SET_MAPRED_JT", "true");
			if (mapredJt2 == null) {
				ComponentUtilities.addNodeProperty(tHiveConnNode, "MAPRED_JT", "TEXT");
			}
			ComponentUtilities.setNodeValue(tHiveConnNode, "MAPRED_JT", mapredJt.getValue());
		}

		ElementParameterType setFsDefaultName = ComponentUtilities.getNodeProperty(tHiveMapNode, "SET_FS_DEFAULT_NAME"); //$NON-NLS-1$
		ElementParameterType fsDefaultName = ComponentUtilities.getNodeProperty(tHiveMapNode, "FS_DEFAULT_NAME"); //$NON-NLS-1$
		if ((setFsDefaultName != null && "true".equalsIgnoreCase(setFsDefaultName.getValue())) && fsDefaultName != null) {
			ElementParameterType setFsDefaultName2 = ComponentUtilities.getNodeProperty(tHiveConnNode, "SET_FS_DEFAULT_NAME"); //$NON-NLS-1$
			ElementParameterType fsDefaultName2 = ComponentUtilities.getNodeProperty(tHiveConnNode, "FS_DEFAULT_NAME"); //$NON-NLS-1$
			if (setFsDefaultName2 == null) {
				ComponentUtilities.addNodeProperty(tHiveConnNode, "SET_FS_DEFAULT_NAME", "CHECK");
			}
			ComponentUtilities.setNodeValue(tHiveConnNode, "SET_FS_DEFAULT_NAME", "true");
			if (fsDefaultName2 == null) {
				ComponentUtilities.addNodeProperty(tHiveConnNode, "FS_DEFAULT_NAME", "TEXT");
			}
			ComponentUtilities.setNodeValue(tHiveConnNode, "FS_DEFAULT_NAME", fsDefaultName.getValue());
		}

		ElementParameterType hadoopAdvancedPropertiesParam =
			ComponentUtilities.getNodeProperty(tHiveMapNode, "HADOOP_ADVANCED_PROPERTIES"); //$NON-NLS-1$
        if (hadoopAdvancedPropertiesParam != null) { //$NON-NLS-1$
            EList<ElementValueType> hadoopAdvancedProperties = hadoopAdvancedPropertiesParam.getElementValue();
            ElementValueType columnNamePropertyElement = null;
            ElementValueType columnNameValueElement = null;
            if (hadoopAdvancedProperties != null && hadoopAdvancedProperties.size() > 0) {
            	ElementParameterType hadoopAdvancedPropertiesParam2 =
            		ComponentUtilities.getNodeProperty(tHiveConnNode, "HADOOP_ADVANCED_PROPERTIES"); //$NON-NLS-1$
            	if (hadoopAdvancedPropertiesParam2 == null) {
            		ComponentUtilities.addNodeProperty(tHiveConnNode, "HADOOP_ADVANCED_PROPERTIES", "TABLE");
            		hadoopAdvancedPropertiesParam2 = ComponentUtilities.getNodeProperty(tHiveConnNode, "HADOOP_ADVANCED_PROPERTIES");
            	}
                for (int i = 0; i < hadoopAdvancedProperties.size(); i = i+2) {
                    columnNamePropertyElement = TalendFileFactory.eINSTANCE.createElementValueType();
                    columnNamePropertyElement.setElementRef("PROPERTY"); //$NON-NLS-1$
                    columnNamePropertyElement.setValue(hadoopAdvancedProperties.get(i).getValue());
                    hadoopAdvancedPropertiesParam2.getElementValue().add(columnNamePropertyElement);
                    columnNameValueElement = TalendFileFactory.eINSTANCE.createElementValueType();
                    columnNameValueElement.setElementRef("VALUE"); //$NON-NLS-1$
                    columnNameValueElement.setValue(hadoopAdvancedProperties.get(i+1).getValue());
                    hadoopAdvancedPropertiesParam2.getElementValue().add(columnNameValueElement);
                }
            }
        }
    }



    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 11, 21, 12, 41, 10);
        return gc.getTime();
    }
}
