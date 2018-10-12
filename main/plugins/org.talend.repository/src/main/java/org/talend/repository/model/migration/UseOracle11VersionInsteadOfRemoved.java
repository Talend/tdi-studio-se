package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class UseOracle11VersionInsteadOfRemoved extends AbstractJobMigrationTask {
    private static final String ORACLE_VERSION_PROPERTY_NAME = "DB_VERSION";
    private static final String REMOVED_ORACLE_VERSION = "ORACLE_11-6";

    private static final String USE_CONNECTION_PROPERTY_NAME = "USE_EXISTING_CONNECTION";

    private static final String MOVE_TO_DIR_PROPERTY_NAME = "MOVE_TO_THE_CURRENT_DIRECTORY";

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2018, 10, 4, 16, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }
            List<String> componentsNameToAffect = new ArrayList<>();
            componentsNameToAffect.add("tOracleCDC");
            componentsNameToAffect.add("tOracleSP");
            componentsNameToAffect.add("tOracleSCDELT");
            componentsNameToAffect.add("tOracleSCD");
            componentsNameToAffect.add("tOracleRow");
            componentsNameToAffect.add("tOracleOutputBulkExec");
            componentsNameToAffect.add("tOracleOutput");
            componentsNameToAffect.add("tOracleInput");
            componentsNameToAffect.add("tOracleConnection");
            componentsNameToAffect.add("tOracleBulkExec");
            componentsNameToAffect.add("tOracleCDC");
            
            componentsNameToAffect.add("tELTOracleMap");
            
            componentsNameToAffect.add("tAmazonOracleRow");
            componentsNameToAffect.add("tAmazonOracleOutput");
            componentsNameToAffect.add("tAmazonOracleInput");
            componentsNameToAffect.add("tAmazonOracleConnection");

            IComponentConversion useOracle11InsteadOfRemovedVersion = new OracleVersionConversion();
            IComponentConversion useOracle11InsteadOfRemovedVersionCreateTable = new CreateTableOracleVersionConversion();

            try {
                for (String componentName : componentsNameToAffect) {
                    IComponentFilter componentFilter = new NameComponentFilter(componentName);
                    ModifyComponentsAction.searchAndModify(item, processType, componentFilter,
                            Arrays.<IComponentConversion> asList(useOracle11InsteadOfRemovedVersion));
                }
                
                
                ModifyComponentsAction.searchAndModify(item, processType, new NameComponentFilter("tCreateTable"),
                        Arrays.<IComponentConversion> asList(useOracle11InsteadOfRemovedVersionCreateTable));
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
                return ExecutionResult.FAILURE;
            }
            
            return ExecutionResult.SUCCESS_NO_ALERT;
    }
    
    private class OracleVersionConversion implements IComponentConversion {
        @Override
        public void transform(NodeType node) {
            if ((!"true".equals(ComponentUtilities.getNodePropertyValue(node, USE_CONNECTION_PROPERTY_NAME)))) {
                String oracleVersion =  ComponentUtilities.getNodePropertyValue(node, ORACLE_VERSION_PROPERTY_NAME);
                if (REMOVED_ORACLE_VERSION.equals(oracleVersion)) {
                    ComponentUtilities.setNodeValue(node, ORACLE_VERSION_PROPERTY_NAME, "ORACLE_11");
                }
            }

        }
    }
    
    private class CreateTableOracleVersionConversion extends OracleVersionConversion {
        private final static String DB_TYPE_PROPERTY_NAME = "DBTYPE";
        private final static String DB_TYPE_ORACLE_PROPERTY_VALUE = "DBORACLE";
        @Override
        public void transform(NodeType node) {
            if (DB_TYPE_ORACLE_PROPERTY_VALUE.equals(ComponentUtilities.getNodePropertyValue(node, DB_TYPE_PROPERTY_NAME))) {
                super.transform(node);
            }
        }
    }
    
}
