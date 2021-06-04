// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.talend.core.CorePlugin;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.designer.core.IDesignerCoreService;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.runprocess.ItemCacheManager;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * OSGi DataSource
 */
public class DataSourceConfig {

    private static final String TRUN_JOB = "tRunJob"; //$NON-NLS-1$

    private static final Set<String> PROCESSES_SUB_JOB = new HashSet<>();

    private DataSourceConfig() {
    }

    public static Collection<String> getAliases(IProcess processItem) {
        PROCESSES_SUB_JOB.clear();
        return getAliases(processItem, new HashSet<String>());
    }

    private static Collection<String> getAliases(IProcess processItem, Collection<String> jobNameVersionChecked) {
        if (processItem == null) {
            return Collections.emptyList();
        }
        String jobName = processItem.getName();
        String jobVersion = processItem.getVersion();
        String id = jobName + '_' + jobVersion;
        if (!jobNameVersionChecked.add(id)) {
            return Collections.emptyList(); // no need to add more to the list,
                                            // just return the empty list
        }
        Collection<String> aliases = new HashSet<String>();

//        List<? extends INode> generatingNodes = processItem.getGeneratingNodes();
//
//
//        for (INode node : generatingNodes) {
//            getDatasourceAliasesFrom(node, aliases, "SPECIFY_DATASOURCE_ALIAS", "DATASOURCE_ALIAS");
//            getDatasourceAliasesFrom(node, aliases, "useDataSource", "dataSource");
//        }

        getAliases(processItem.getId(), aliases);
        return aliases;
    }

    /**
     * DOC sunchaoqun Comment method "getAliases".
     * 
     * @param id
     * @param ds
     * @return
     */
    private static void getAliases(String id, Collection<String> ds) {
        ProcessItem pi = ItemCacheManager.getProcessItem(id);
        for (Iterator<?> e = pi.getProcess().getNode().iterator(); e.hasNext();) {
            NodeType node = (NodeType) e.next();
            if (TRUN_JOB.equals(node.getComponentName())) {
                for (Iterator<?> iterator = node.getElementParameter().iterator(); iterator.hasNext();) {
                    ElementParameterType elementParameter = (ElementParameterType) iterator.next();
                    if (StringUtils.equals(elementParameter.getName(), "PROCESS:PROCESS_TYPE_PROCESS")) {
                        getAliases((String) elementParameter.getValue(), ds);
                    }
                }
            } else {

                boolean useDS = false;
                String value = null;

                for (Iterator<?> iterator = node.getElementParameter().iterator(); iterator.hasNext();) {
                    ElementParameterType elementParameter = (ElementParameterType) iterator.next();

                    if (StringUtils.equals(elementParameter.getName(), "PROPERTIES")) {
                        JSONObject val = null;
                        try {
                            val = new JSONObject(elementParameter.getValue());
                            if (val != null && val.get("dataSource") instanceof JSONObject) {
                                JSONObject dataSource = (JSONObject) val.get("dataSource");
                                if (dataSource != null) {
                                    String storeValue = (String) dataSource.get("storedValue");
                                    if (StringUtils.isNoneBlank(storeValue)) {
                                        useDS = true;
                                        value = storeValue;
                                    }
                                }
                            }
                        } catch (JSONException ee) {
                            ee.printStackTrace();
                        }
                    }

                    if ((StringUtils.equals(elementParameter.getName(), "SPECIFY_DATASOURCE_ALIAS")
                            || StringUtils.equals(elementParameter.getName(), "useDataSource"))
                            && BooleanUtils.toBoolean(elementParameter.getValue()) && elementParameter.isShow()) {
                        useDS = true;
                    }

                    if (StringUtils.equals(elementParameter.getName(), "DATASOURCE_ALIAS")
                            || StringUtils.equals(elementParameter.getName(), "dataSource")) {
                        String result = elementParameter.getValue() == null ? null : elementParameter.getValue().toString();
                        value = TalendQuoteUtils.removeQuotes(result.trim());
                    }

                    if (useDS && StringUtils.isNotBlank(value)) {
                        ds.add(value);
                    }
                }
            }
        }
    }

    @Deprecated
    private static String getDatasourceAliasFrom(INode node) {
        if (!node.isActivate()) {
            return null;
        }
        IElementParameter isSpecifyAlias = node.getElementParameter("SPECIFY_DATASOURCE_ALIAS"); //$NON-NLS-1$
        if (isSpecifyAlias == null || Boolean.FALSE.equals(isSpecifyAlias.getValue())) {
            return null;
        }
        IElementParameter aliasParam = node.getElementParameter("DATASOURCE_ALIAS"); //$NON-NLS-1$
        if (aliasParam == null) {
            return null;
        }
        IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
        boolean evaluate = designerCoreService.evaluate(isSpecifyAlias.getShowIf(), node.getElementParameters());
        if (!evaluate) {
            return null;
        }
        String result = aliasParam.getValue() == null ? null : aliasParam.getValue().toString();
        return TalendQuoteUtils.removeQuotes(result.trim());
    }

    // TESB-17238:Include sub jobs datasource alias
    private static void getDatasourceAliasesFrom(INode node, Collection<String> ds, 
    		String specifyDataSourceAlias, String dataSourceAlias) {
        if (!node.isActivate()) {
            return;
        }
        if (TRUN_JOB.equals(node.getComponent().getName())) {
            String processId = (String) node.getElementParameter("PROCESS:PROCESS_TYPE_PROCESS").getValue();
            if (PROCESSES_SUB_JOB.contains(processId)) {
                return;
            }
            PROCESSES_SUB_JOB.add(processId);

            List<? extends INode> subNodes = getSubProcessNodesFromTRunjob(node);
            if (subNodes != null) {
                for (INode n : subNodes) {
                    getDatasourceAliasesFrom(n, ds, specifyDataSourceAlias, dataSourceAlias);
                }
            }
        }
        IElementParameter isSpecifyAlias = node.getElementParameter(specifyDataSourceAlias); //$NON-NLS-1$
        if (isSpecifyAlias == null || Boolean.FALSE.equals(isSpecifyAlias.getValue())) {
            return;
        }
        IElementParameter aliasParam = node.getElementParameter(dataSourceAlias); //$NON-NLS-1$
        if (aliasParam == null) {
            return;
        }
        IDesignerCoreService designerCoreService = CorePlugin.getDefault().getDesignerCoreService();
        
        if (isSpecifyAlias.getShowIf() != null) {
	        boolean evaluate = designerCoreService.evaluate(isSpecifyAlias.getShowIf(), node.getElementParameters());
	        if (!evaluate) {
	            return;
	        }
        }
        String result = aliasParam.getValue() == null ? null : aliasParam.getValue().toString();

        ds.add(TalendQuoteUtils.removeQuotes(result.trim()));
    }

    private static List<? extends INode> getSubProcessNodesFromTRunjob(INode tRunJobNode) {
        String processId = (String) tRunJobNode.getElementParameter("PROCESS:PROCESS_TYPE_PROCESS").getValue(); //$NON-NLS-1$
        if (processId != null && !"".equals(processId)) { //$NON-NLS-1$
            ProcessItem processItem = ItemCacheManager.getProcessItem(processId);
            IDesignerCoreService service = CorePlugin.getDefault().getDesignerCoreService();
            if (processItem != null && service != null) {
                return service.getProcessFromItem(processItem).getGeneratingNodes();
            }
        }
        return null;
    }
}
