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
package org.talend.designer.core.generic.model.migration;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.talend.daikon.properties.property.Property.Flags.DESIGN_TIME_ONLY;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.utils.TalendQuoteUtils;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.property.Property;
import org.talend.daikon.properties.property.PropertyValueEvaluator;
import org.talend.daikon.serialize.PostDeserializeSetup;
import org.talend.daikon.serialize.SerializerDeserializer;
import org.talend.designer.core.generic.utils.ComponentsUtils;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * created by pyzhou on Dec 01, 2017 Detailled comment
 *This Migration task aim for remove the DESIGN_TIME_ONLY flag of TDataSetOutputProperties.
 */
public class Dataprep701MigrationTask extends AbstractJobMigrationTask {

    /*
     * (non-Javadoc)
     *
     * @see org.talend.migration.IMigrationTask#getOrder()
     */
    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2017, 11, 01, 12, 0, 0);
        return gc.getTime();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.migration.AbstractItemMigrationTask#execute(org.talend.core.model.properties.Item)
     */
    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }


        IComponentConversion changeJDBCDriverJarType = new IComponentConversion() {

            @Override
            public void transform(NodeType node) {
                ElementParameterType elemParamType = ComponentUtilities.getNodeProperty(node, "PROPERTIES");
                String propertiesString = elemParamType.getValue();
                SerializerDeserializer.Deserialized<ComponentProperties> fromSerialized = Properties.Helper.fromSerializedPersistent(propertiesString,
                        ComponentProperties.class, new PostDeserializeSetup() {

                            @Override
                            public void setup(Object properties) {
                                ((Properties)properties).setValueEvaluator(new PropertyValueEvaluator() {

                                    @Override
                                    public Object evaluate(Property property, Object storedValue) {
                                        return storedValue;
                                    }

                                });
                            }
                        });
                ComponentProperties newProperties = ComponentsUtils.getComponentProperties(node.getComponentName());
                newProperties.copyValuesFrom(fromSerialized.object, true, false);
                NamedThing nt = newProperties.getProperty("dataSetName");
                if (nt != null && nt instanceof Property) {
                    Property moduleNameProperty = (Property) nt;
                    moduleNameProperty.removeFlag(DESIGN_TIME_ONLY);
                    String moduleName = (String) moduleNameProperty.getValue();
                    moduleNameProperty.setStoredValue(moduleName);
                }
                elemParamType.setValue(newProperties.toSerialized());
            }
        };
        String componentsName = "tDatasetOutput"; //$NON-NLS-1$
        boolean modified = false;
        for (Object obj : processType.getNode()) {
            if (obj != null && obj instanceof NodeType) {
                String componentName = ((NodeType) obj).getComponentName();
                if (componentsName.equals(componentName) ) {
                    IComponentFilter filter = new NameComponentFilter(componentName);
                    modified = ModifyComponentsAction.searchAndModify((NodeType) obj, filter,
                            Arrays.<IComponentConversion> asList(changeJDBCDriverJarType))
                            || modified;
                }
            }
        }
        if (modified) {
            try {
                ProxyRepositoryFactory.getInstance().save(item, true);
                return ExecutionResult.SUCCESS_NO_ALERT;
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
        }
        return ExecutionResult.SUCCESS_WITH_ALERT;

    }



}
