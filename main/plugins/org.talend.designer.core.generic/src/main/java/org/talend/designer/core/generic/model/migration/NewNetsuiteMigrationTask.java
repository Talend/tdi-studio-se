package org.talend.designer.core.generic.model.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.talend.components.api.properties.ComponentProperties;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.daikon.NamedThing;
import org.talend.daikon.properties.property.Property;
import org.talend.designer.core.generic.model.GenericTableUtils;
import org.talend.designer.core.generic.utils.ParameterUtilTool;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

public class NewNetsuiteMigrationTask extends NewComponentFrameworkMigrationTask {

    private static final Map<String, String> searchOperatorMapping;

    static {
        searchOperatorMapping = new HashMap<String, String>();

        // String Operators
        searchOperatorMapping.put("S-contains", "String.contains");
        searchOperatorMapping.put("S-doesNotContain", "String.doesNotContain");
        searchOperatorMapping.put("S-doesNotStartWith", "String.doesNotStartWith");
        searchOperatorMapping.put("S-empty", "String.empty");
        searchOperatorMapping.put("S-hasKeywords", "String.hasKeywords");
        searchOperatorMapping.put("S-is", "String.is");
        searchOperatorMapping.put("S-isNot", "String.isNot");
        searchOperatorMapping.put("S-notEmpty", "String.notEmpty");
        searchOperatorMapping.put("S-startsWith", "String.startsWith");

        // Numeric Operators
        searchOperatorMapping.put("N-between", "Long.between");
        searchOperatorMapping.put("N-notBetween", "Long.notBetween");
        searchOperatorMapping.put("N-empty", "Long.empty");
        searchOperatorMapping.put("N-equalTo", "Long.equalTo");
        searchOperatorMapping.put("N-greaterThan", "Long.greaterThan");
        searchOperatorMapping.put("N-greaterThanOrEqualTo", "Long.greaterThanOrEqualTo");
        searchOperatorMapping.put("N-lessThan", "Long.lessThan");
        searchOperatorMapping.put("N-lessThanOrEqualTo", "Long.lessThanOrEqualTo");
        searchOperatorMapping.put("N-notEmpty", "Long.notEmpty");
        searchOperatorMapping.put("N-notEqualTo", "Long.notEqualTo");
        searchOperatorMapping.put("N-notGreaterThan", "Long.notGreaterThan");
        searchOperatorMapping.put("N-notGreaterThanOrEqualTo", "Long.notGreaterThanOrEqualTo");
        searchOperatorMapping.put("N-notLessThan", "Long.notLessThan");
        searchOperatorMapping.put("N-notLessThanOrEqualTo", "Long.notLessThanOrEqualTo");

        // Double Operators
        searchOperatorMapping.put("O-between", "Double.between");
        searchOperatorMapping.put("O-notBetween", "Double.notBetween");
        searchOperatorMapping.put("O-empty", "Double.empty");
        searchOperatorMapping.put("O-equalTo", "Double.equalTo");
        searchOperatorMapping.put("O-greaterThan", "Double.greaterThan");
        searchOperatorMapping.put("O-greaterThanOrEqualTo", "Double.greaterThanOrEqualTo");
        searchOperatorMapping.put("O-lessThan", "Double.lessThan");
        searchOperatorMapping.put("O-lessThanOrEqualTo", "Double.lessThanOrEqualTo");
        searchOperatorMapping.put("O-notEmpty", "Double.notEmpty");
        searchOperatorMapping.put("O-notEqualTo", "Double.notEqualTo");
        searchOperatorMapping.put("O-notGreaterThan", "Double.notGreaterThan");
        searchOperatorMapping.put("O-notGreaterThanOrEqualTo", "Double.notGreaterThanOrEqualTo");
        searchOperatorMapping.put("O-notLessThan", "Double.notLessThan");
        searchOperatorMapping.put("O-notLessThanOrEqualTo", "Double.notLessThanOrEqualTo");

        // List Operators
        searchOperatorMapping.put("L-anyOf", "List.anyOf");
        searchOperatorMapping.put("L-noneOf", "List.noneOf");

        // Date Operators
        searchOperatorMapping.put("D-after", "Date.after");
        searchOperatorMapping.put("D-before", "Date.before");
        searchOperatorMapping.put("D-empty", "Date.empty");
        searchOperatorMapping.put("D-notAfter", "Date.notAfter");
        searchOperatorMapping.put("D-notBefore", "Date.notBefore");
        searchOperatorMapping.put("D-notEmpty", "Date.notEmpty");
        searchOperatorMapping.put("D-notOn", "Date.notOn");
        searchOperatorMapping.put("D-notOnOrAfter", "Date.notOnOrAfter");
        searchOperatorMapping.put("D-notOnOrBefore", "Date.notOnOrBefore");
        searchOperatorMapping.put("D-notWithin", "Date.notWithin");
        searchOperatorMapping.put("D-on", "Date.on");
        searchOperatorMapping.put("D-onOrAfter", "Date.onOrAfter");
        searchOperatorMapping.put("D-onOrBefore", "Date.onOrBefore");
        searchOperatorMapping.put("D-within", "Date.within");

        // Boolean Operators
        searchOperatorMapping.put("B-boolean", "Boolean");

    }

    @Override
    public Date getOrder() {
        return new GregorianCalendar(2017, 2, 24, 10, 0, 0).getTime();
    }

    @Override
    protected Properties getPropertiesFromFile() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("NewNetsuiteMigrationTask.properties");//$NON-NLS-1$
        try {
            props.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return props;
    }

    @Override
    protected IComponentConversion getComponentConversion(ProcessType processType, ComponentCategory componentCategory,
            Properties props) {

        return new ComponentConversion(processType, componentCategory, props) {

            @Override
            protected void processElementParameter(ElementParameterContext ctx, NamedThing target) {
                if ("MODULENAME".equals(ctx.getOldParamName())) {
                    Property<Object> property = (Property<Object>) target;
                    Object value = ParameterUtilTool.convertParameterValue(ctx.getParamType());
                    property.setValue("\"" + value + "\"");

                } else if ("tNetsuiteInput".equals(ctx.getComponentName()) && "CONDITIONS".equals(ctx.getOldParamName())) {
                    processSearchConditionsTable(ctx, target);

                } else if ("tNetsuiteOutput".equals(ctx.getComponentName()) && "ACTION".equals(ctx.getOldParamName())) {
                    Property<Object> property = (Property<Object>) target;
                    Object value = ParameterUtilTool.convertParameterValue(ctx.getParamType());
                    // Map INSERT action to ADD, other actions are mapped as is
                    if ("INSERT".equals(value)) {
                        value = "ADD";
                    }
                    property.setValue(value);

                } else {
                    super.processElementParameter(ctx, target);
                }
            }

        };
    }

    private void processSearchConditionsTable(ElementParameterContext ctx, NamedThing target) {
        ComponentProperties searchQueryProps = (ComponentProperties) target;
        ElementParameterType paramType = ctx.getParamType();

        List<ElementValueType> columns = paramType.getElementValue();

        if (!(columns != null && columns.size() > 0)) {
            return;
        }

        Map<String, String> columnMapping = new HashMap<String, String>();
        columnMapping.put("INPUT_COLUMN", "field");
        columnMapping.put("OPERATOR", "operator");
        columnMapping.put("RVALUE", "value1");
        columnMapping.put("RVALUE2", "value2");

        List<String> fieldPropPossibleValues = new ArrayList<String>();

        List<Map<String, Object>> tableEntries = new ArrayList<Map<String, Object>>();
        Map<String, Object> tableEntry = null;
        for (ElementValueType column : columns) {
            String sourceName = column.getElementRef();

            if ("INPUT_COLUMN".equals(sourceName)) {
                if (tableEntry != null) {
                    tableEntries.add(tableEntry);
                }
                tableEntry = new HashMap<String, Object>();
            }

            String targetName = columnMapping.get(sourceName);
            Object targetValue = column.getValue();

            if ("field".equals(targetName)) {
                String mappedFieldName = toInitialLower(targetValue.toString());
                targetValue = mappedFieldName;
                fieldPropPossibleValues.add(mappedFieldName);

            } else if ("operator".equals(targetName)) {
                String mappedOperatorName = searchOperatorMapping.get(targetValue.toString());
                if (mappedOperatorName != null) {
                    targetValue = mappedOperatorName;
                }
            }

            tableEntry.put(targetName, targetValue);
        }
        tableEntries.add(tableEntry);

        GenericTableUtils.setTableValues(searchQueryProps, tableEntries, ctx.getParam());

        Property<String> fieldProp = (Property<String>) searchQueryProps.getProperty("field");
        fieldProp.setPossibleValues(Arrays.asList("type"));

        Property<String> operatorProp = (Property<String>) searchQueryProps.getProperty("operator");
        List<String> operatorPropPossibleValues = new ArrayList<String>(searchOperatorMapping.values());
        Collections.sort(operatorPropPossibleValues);
        operatorProp.setPossibleValues(operatorPropPossibleValues);
    }

    public static String toInitialLower(String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

}
