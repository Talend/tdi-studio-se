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

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ChangeOracleJarName4OracleDriver extends AbstractJobMigrationTask {

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        String[] compNames = { "tELTJDBCMap", "tJDBCConnection", "tJDBCInput", "tJDBCOutput", "tJDBCRow", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                "tJDBCSP", "tLibraryLoad"}; //$NON-NLS-1$ //$NON-NLS-2$

        IComponentConversion changeOracleDriverJarType = new IComponentConversion() {

            public void transform(NodeType node) {

                if("tLibraryLoad".equals(node.getComponentName())) { //$NON-NLS-1$
                    ElementParameterType moduleLib = ComponentUtilities.getNodeProperty(node, "LIBRARY"); //$NON-NLS-1$
                    if (moduleLib != null) {
                        moduleLib.setValue(convert(moduleLib.getValue()));
                    }
                    ElementParameterType hotLibs = ComponentUtilities.getNodeProperty(node, "HOTLIBS"); //$NON-NLS-1$
                    if (hotLibs != null) {
	                    for(Object element : hotLibs.getElementValue()) {
	                        if(element == null) {
	                            continue;
	                        }
	                        ElementValueType valueType = (ElementValueType)element;
	                        if("LIBPATH".equals(valueType.getElementRef())) { //$NON-NLS-1$
	                            valueType.setValue(convert(valueType.getValue()));
	                        }
	                    }
                    }
                } else {
                    ElementParameterType driverJars = ComponentUtilities.getNodeProperty(node, "DRIVER_JAR"); //$NON-NLS-1$
                    if (driverJars != null) {
	                    for(Object element : driverJars.getElementValue()) {
	                        if(element == null) {
	                            continue;
	                        }
	                        ElementValueType valueType = (ElementValueType)element;
	                        if("JAR_NAME".equals(valueType.getElementRef())) { //$NON-NLS-1$
	                            valueType.setValue(convert2(valueType.getValue()));
	                        }
	                    }
                    }
                }

            }

            /**
             * convert the string value that contain double quote text closure.
             * @param source
             * @return
             */
            String convert(String source) {
                if ("\"ojdbc6-11g.jar\"".equalsIgnoreCase(source)) { //$NON-NLS-1$
                    return "\"ojdbc6.jar\""; //$NON-NLS-1$
                } else if ("\"ojdbc5-11g.jar\"".equalsIgnoreCase(source)) { //$NON-NLS-1$
                    return "\"ojdbc5.jar\""; //$NON-NLS-1$
                } else if ("\"ojdbc14-10g.jar\"".equalsIgnoreCase(source)) { //$NON-NLS-1$
                    return "\"ojdbc14.jar\""; //$NON-NLS-1$
                } else if ("\"ojdbc12-8i.jar\"".equalsIgnoreCase(source)) { //$NON-NLS-1$
                    return "\"ojdbc12.jar\""; //$NON-NLS-1$
                }
                return source;
            }

            /**
             * convert the string value that not contain double quote text closure.
             * @param source
             * @return
             */
            String convert2(String source) {
                if ("ojdbc6-11g.jar".equalsIgnoreCase(source)) { //$NON-NLS-1$
                    return "ojdbc6.jar"; //$NON-NLS-1$
                } else if ("ojdbc5-11g.jar".equalsIgnoreCase(source)) { //$NON-NLS-1$
                    return "ojdbc5.jar"; //$NON-NLS-1$
                } else if ("ojdbc14-10g.jar".equalsIgnoreCase(source)) { //$NON-NLS-1$
                    return "ojdbc14.jar"; //$NON-NLS-1$
                } else if ("ojdbc12-8i.jar".equalsIgnoreCase(source)) { //$NON-NLS-1$
                    return "ojdbc12.jar"; //$NON-NLS-1$
                }
                return source;
            }

        };

        for (String name : compNames) {
            IComponentFilter filter = new NameComponentFilter(name);

            try {
                ModifyComponentsAction.searchAndModify(item, processType, filter,
                        Arrays.<IComponentConversion> asList(changeOracleDriverJarType));
            } catch (PersistenceException e) {
                // TODO Auto-generated catch block
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }

        return ExecutionResult.SUCCESS_NO_ALERT;

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2012, 10, 20, 10, 0, 0);
        return gc.getTime();
    }
}
