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
package org.talend.repository.model.migration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
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
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;

/**
 * Migration task for TDQ-16376:
 * - add function categories
 * - migrate some old functions to new ones with different options
 * - EXTRA_PARAMETER and KEEP_EMPTY params are kept as before
 * 
 */
public class MigrateTDataMaskingGuiTDQ16376 extends AbstractJobMigrationTask {

    @Override
    public List<ERepositoryObjectType> getTypes() {
        List<ERepositoryObjectType> toReturn = new ArrayList<ERepositoryObjectType>();
        toReturn.add(ERepositoryObjectType.PROCESS);
        toReturn.add(ERepositoryObjectType.JOBLET);
        toReturn.add(ERepositoryObjectType.TEST_CONTAINER);
        // PROCESS_MR stands for Map/Reduce and Spark.
        toReturn.add(ERepositoryObjectType.PROCESS_MR);
        return toReturn;
    }

    private Map<String, String[]> MASKING_PARAM_MIGRATION = new HashMap<String, String[]>() {

        private static final long serialVersionUID = 1L;

        {
            // Mapping between: ("old function name") => ("new function category", "new function name", "new function method")
            // If "new function name" is null, then keep "old function name" unchanged.

            /**
             * Basic functions
             */
            put("REPLACE_ALL", new String[] { "CHARACTER_HANDLING", null, "RANDOM" });
            put("REPLACE_CHARACTERS", new String[] { "CHARACTER_HANDLING", null, "RANDOM" });
            put("REPLACE_NUMERIC", new String[] { "CHARACTER_HANDLING", null, "RANDOM" });

            put("BETWEEN_INDEXES_REPLACE", new String[] { "CHARACTER_HANDLING", null, "RANDOM" });
            put("REPLACE_FIRST_CHARS", new String[] { "CHARACTER_HANDLING", null, "RANDOM" });
            put("REPLACE_LAST_CHARS", new String[] { "CHARACTER_HANDLING", null, "RANDOM" });
            put("KEEP_FIRST_AND_GENERATE", new String[] { "CHARACTER_HANDLING", null, "RANDOM" });
            put("KEEP_LAST_AND_GENERATE", new String[] { "CHARACTER_HANDLING", null, "RANDOM" });

            put("BETWEEN_INDEXES_KEEP", new String[] { "CHARACTER_HANDLING", null, "EMPTY" });
            put("BETWEEN_INDEXES_REMOVE", new String[] { "CHARACTER_HANDLING", null, "EMPTY" });
            put("REMOVE_FIRST_CHARS", new String[] { "CHARACTER_HANDLING", null, "EMPTY" });
            put("REMOVE_LAST_CHARS", new String[] { "CHARACTER_HANDLING", null, "EMPTY" });

            put("GENERATE_BETWEEN", new String[] { "NUMBER_HANDLING", null, "EMPTY" });
            put("NUMERIC_VARIANCE", new String[] { "NUMBER_HANDLING", null, "EMPTY" });

            put("DATE_VARIANCE", new String[] { "DATE_HANDLING", null, "EMPTY" });
            put("KEEP_YEAR", new String[] { "DATE_HANDLING", null, "EMPTY" });

            /**
             * Generation functions
             */
            put("GENERATE_ACCOUNT_NUMBER", new String[] { "BANK_ACCOUNT_GENERATION", null, "EMPTY" });
            put("GENERATE_ACCOUNT_NUMBER_FORMAT", new String[] { "BANK_ACCOUNT_GENERATION", null, "EMPTY" });

            put("GENERATE_CREDIT_CARD", new String[] { "CREDIT_CARD_GENERATION", null, "EMPTY" });
            put("GENERATE_CREDIT_CARD_FORMAT", new String[] { "CREDIT_CARD_GENERATION", null, "EMPTY" });

            put("GENERATE_FROM_PATTERN", new String[] { "DATA_GENERATION", null, "EMPTY" });
            put("GENERATE_UUID", new String[] { "DATA_GENERATION", null, "EMPTY" });
            put("GENERATE_SEQUENCE", new String[] { "DATA_GENERATION", null, "EMPTY" });
            put("GENERATE_FROM_LIST", new String[] { "DATA_GENERATION", "GENERATE_FROM_LIST_OR_FILE", "RANDOM" });
            put("GENERATE_FROM_FILE", new String[] { "DATA_GENERATION", "GENERATE_FROM_LIST_OR_FILE", "RANDOM" });
            put("GENERATE_FROM_LIST_HASH", new String[] { "DATA_GENERATION", "GENERATE_FROM_LIST_OR_FILE", "CONSISTENT" });
            put("GENERATE_FROM_FILE_HASH", new String[] { "DATA_GENERATION", "GENERATE_FROM_LIST_OR_FILE", "CONSISTENT" });

            put("GENERATE_PHONE_NUMBER_FRENCH", new String[] { "PHONE_GENERATION", null, "EMPTY" });
            put("GENERATE_PHONE_NUMBER_GERMANY", new String[] { "PHONE_GENERATION", null, "EMPTY" });
            put("GENERATE_PHONE_NUMBER_JAPAN", new String[] { "PHONE_GENERATION", null, "EMPTY" });
            put("GENERATE_PHONE_NUMBER_UK", new String[] { "PHONE_GENERATION", null, "EMPTY" });
            put("GENERATE_PHONE_NUMBER_US", new String[] { "PHONE_GENERATION", null, "EMPTY" });

            put("GENERATE_SSN_CHINA", new String[] { "SSN_GENERATION", null, "EMPTY" });
            put("GENERATE_SSN_FRENCH", new String[] { "SSN_GENERATION", null, "EMPTY" });
            put("GENERATE_SSN_GERMANY", new String[] { "SSN_GENERATION", null, "EMPTY" });
            put("GENERATE_SSN_INDIA", new String[] { "SSN_GENERATION", null, "EMPTY" });
            put("GENERATE_SSN_JAPAN", new String[] { "SSN_GENERATION", null, "EMPTY" });
            put("GENERATE_SSN_UK", new String[] { "SSN_GENERATION", null, "EMPTY" });
            put("GENERATE_SSN_US", new String[] { "SSN_GENERATION", null, "EMPTY" });

            /**
             * Masking functions
             */
            put("MASK_ADDRESS", new String[] { "ADDRESS_MASKING", null, "EMPTY" });

            put("MASK_EMAIL", new String[] { "EMAIL_MASKING", "MASK_EMAIL_LOCALPART", "MASK_BY_CHARACTER" });
            put("MASK_EMAIL_LOCALPART_BY_X", new String[] { "EMAIL_MASKING", "MASK_EMAIL_LOCALPART", "MASK_BY_CHARACTER" });
            put("MASK_EMAIL_LOCALPART_RANDOMLY", new String[] { "EMAIL_MASKING", "MASK_EMAIL_LOCALPART", "MASK_FROM_LIST" });
            put("MASK_FULL_EMAIL_DOMAIN_BY_X", new String[] { "EMAIL_MASKING", "MASK_FULL_EMAIL_DOMAIN", "MASK_BY_CHARACTER" });
            put("MASK_FULL_EMAIL_DOMAIN_RANDOMLY", new String[] { "EMAIL_MASKING", "MASK_FULL_EMAIL_DOMAIN", "MASK_FROM_LIST" });
            put("MASK_TOP_LEVEL_EMAIL_DOMAIN_BY_X",
                    new String[] { "EMAIL_MASKING", "MASK_TOP_LEVEL_EMAIL_DOMAIN", "MASK_BY_CHARACTER" });
            put("MASK_TOP_LEVEL_EMAIL_DOMAIN_RANDOMLY",
                    new String[] { "EMAIL_MASKING", "MASK_TOP_LEVEL_EMAIL_DOMAIN", "MASK_FROM_LIST" });

            put("GENERATE_UNIQUE_PHONE_NUMBER_FRENCH", new String[] { "PHONE_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_PHONE_NUMBER_GERMANY", new String[] { "PHONE_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_PHONE_NUMBER_JAPAN", new String[] { "PHONE_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_PHONE_NUMBER_UK", new String[] { "PHONE_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_PHONE_NUMBER_US", new String[] { "PHONE_MASKING", null, "BASIC" });

            put("GENERATE_UNIQUE_SSN_CHINA", new String[] { "SSN_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_SSN_FRENCH", new String[] { "SSN_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_SSN_GERMANY", new String[] { "SSN_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_SSN_INDIA", new String[] { "SSN_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_SSN_JAPAN", new String[] { "SSN_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_SSN_UK", new String[] { "SSN_MASKING", null, "BASIC" });
            put("GENERATE_UNIQUE_SSN_US", new String[] { "SSN_MASKING", null, "BASIC" });

            put("SET_TO_NULL", new String[] { "SET_TO_NULL", null, "EMPTY" });
        }

    };

    @Override
    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2019, 4, 8, 16, 0, 0);
        return gc.getTime();
    }

    @Override
    public ExecutionResult execute(Item item) {
        ProcessType processType = getProcessType(item);
        if (getProject().getLanguage() != ECodeLanguage.JAVA || processType == null) {
            return ExecutionResult.NOTHING_TO_DO;
        }

        List<String> componentsNameToAffect = new ArrayList<>();
        componentsNameToAffect.add("tDataMasking");

        IComponentConversion useOracle11InsteadOfRemovedVersion = new MigrateDataMaskingParameters();

        try {
            for (String componentName : componentsNameToAffect) {
                IComponentFilter componentFilter = new NameComponentFilter(componentName);
                ModifyComponentsAction.searchAndModify(item, processType, componentFilter,
                        Arrays.<IComponentConversion> asList(useOracle11InsteadOfRemovedVersion));
            }

        } catch (PersistenceException e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }

        return ExecutionResult.SUCCESS_NO_ALERT;
    }

    private class MigrateDataMaskingParameters implements IComponentConversion {

        private static final String MASKING_PARAM_TABLE_NAME = "MODIF_TABLE";

        @Override
        public void transform(NodeType node) {

            if (node == null) {
                return;
            }

            List<ElementValueType> maskingParams = new ArrayList<ElementValueType>();
            TalendFileFactory fileFactory = TalendFileFactory.eINSTANCE;

            ElementParameterType parameter = ComponentUtilities.getNodeProperty(node, MASKING_PARAM_TABLE_NAME);

            if (parameter != null) {

                EList<ElementValueType> list = parameter.getElementValue();
                for (ElementValueType e : list) {
                    if (e.getElementRef().equals("CATEGORY")) {
                        return; // nothing to do
                    }
                }

                List<String> inputCols = new ArrayList<String>();
                List<String> functions = new ArrayList<String>();
                List<String> extraParams = new ArrayList<String>();
                List<String> keepFormatSelections = new ArrayList<String>();

                for (ElementValueType e : list) {

                    if (e.getElementRef().equals("INPUT_COLUMN")) {
                        inputCols.add(e.getValue());
                    } else if (e.getElementRef().equals("FUNCTION")) {
                        functions.add(e.getValue());
                    } else if (e.getElementRef().equals("EXTRA_PARAMETER")) {
                        extraParams.add(e.getValue());
                    } else if (e.getElementRef().equals("KEEP_FORMAT")) {
                        keepFormatSelections.add(e.getValue());
                    }
                }

                for (int i = 0; i < inputCols.size(); i++) {
                    final String oldFunction = functions.get(i);
                    final String[] newParams = MASKING_PARAM_MIGRATION.get(oldFunction);

                    String category;
                    String newFunction;
                    String method;
                    if (newParams == null) { // keep old function name
                        category = "";
                        newFunction = oldFunction;
                        method = "EMPTY";
                    } else {
                        category = newParams[0];
                        newFunction = newParams[1] == null ? oldFunction : newParams[1];
                        method = newParams[2];
                    }

                    ElementValueType elementValue = fileFactory.createElementValueType();
                    elementValue.setElementRef("INPUT_COLUMN");
                    elementValue.setValue(inputCols.get(i));
                    maskingParams.add(elementValue);

                    ElementValueType elementValue2 = fileFactory.createElementValueType();
                    elementValue2.setElementRef("CATEGORY"); // new in 7.2
                    elementValue2.setValue(category);
                    maskingParams.add(elementValue2);

                    ElementValueType elementValue3 = fileFactory.createElementValueType();
                    elementValue3.setElementRef("FUNCTION");
                    elementValue3.setValue(newFunction);
                    maskingParams.add(elementValue3);

                    ElementValueType elementValue4 = fileFactory.createElementValueType();
                    elementValue4.setElementRef("METHOD");// new in 7.2
                    elementValue4.setValue(method);
                    maskingParams.add(elementValue4);

                    ElementValueType elementValue5 = fileFactory.createElementValueType();
                    elementValue5.setElementRef("ALPHABET");// new in 7.2
                    elementValue5.setValue("BEST_GUESS");
                    maskingParams.add(elementValue5);

                    ElementValueType elementValue6 = fileFactory.createElementValueType();
                    elementValue6.setElementRef("EXTRA_PARAMETER");
                    elementValue6.setValue(extraParams.get(i));
                    maskingParams.add(elementValue6);

                    ElementValueType elementValue7 = fileFactory.createElementValueType();
                    elementValue7.setElementRef("KEEP_FORMAT");
                    elementValue7.setValue(i < keepFormatSelections.size() ? keepFormatSelections.get(i) : "false");
                    maskingParams.add(elementValue7);
                }

                ComponentUtilities.removeNodeProperty(node, MASKING_PARAM_TABLE_NAME);
                ComponentUtilities.addNodeProperty(node, MASKING_PARAM_TABLE_NAME, "TABLE");
                ComponentUtilities.setNodeProperty(node, MASKING_PARAM_TABLE_NAME, maskingParams);
            }
        }
    }
}
