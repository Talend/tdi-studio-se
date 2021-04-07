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

import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.language.ECodeLanguage;
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
 * DOC Administrator class global comment. Detailled comment
 */
public class RemoveDefaultProxyIPTask extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
        String[] componentsName = new String[] { "tFTPConnection", "tFTPDelete", "tFTPFileExist", "tFTPFileList", "tFTPFileProperties", "tFTPGet", "tFTPPut", "tFTPRename", "tFTPTruncate", "tFileFetch", "tRSSInput", "tSOAP", "tSetProxy", "tWebService", "tWebServiceInput" }; //$NON-NLS-1$ //$NON-NLS-2$
        for (String name : componentsName) {
            IComponentFilter filter = new NameComponentFilter(name);
            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter, Arrays
                        .<IComponentConversion> asList(new IComponentConversion() {

                            public void transform(NodeType node) {
                                ElementParameterType wrongName = ComponentUtilities.getNodeProperty(node, "UES_PROXY");
                                ElementParameterType useProxy = ComponentUtilities.getNodeProperty(node, "USE_PROXY");
                                if (wrongName != null) {//$NON-NLS-1$
                                    //change wrong property name
                                    ComponentUtilities.addNodeProperty(node, "USE_PROXY", "CHECK");//$NON-NLS-1$ //$NON-NLS-2$
                                    ComponentUtilities.getNodeProperty(node, "USE_PROXY").setValue(wrongName.getValue());
                                    useProxy = ComponentUtilities.getNodeProperty(node, "USE_PROXY");

                                }
                                if (useProxy == null) {//$NON-NLS-1$
                                    //if no "option/checkbox" => no change (to not break any user job already working)
                                    return;
                                }

                                ElementParameterType proxyHost =
                                        ComponentUtilities.getNodeProperty(node, "PROXY_HOST"); //$NON-NLS-1$
                                if (useProxy.getValue().equals("false") && "\"61.163.92.4\"".equals(proxyHost.getValue())) { //$NON-NLS-1$
                                    proxyHost.setValue("\"127.0.0.1\"");//$NON-NLS-1$
                                }
                            }
                        }));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_WITH_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2021, 2, 8, 14, 0, 0);
        return gc.getTime();
    }
}
