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
package org.talend.designer.core.model.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.system.EnvironmentUtils;
import org.talend.core.PluginChecker;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IConnectionCategory;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.hadoop.distribution.DistributionFactory;
import org.talend.hadoop.distribution.ESparkVersion;
import org.talend.hadoop.distribution.spark.SparkVersionUtil;

/**
 * This class will test an expression in the element parameters. <br>
 * The expression can be for example: <br>
 * ((VAR1 == 'value1' and VAR2 == 'value2') or (VAR3 != 'value3')) or (VAR4 == 'value4') <br>
 * With VAR1, VAR2, VAR3 & VAR4 as the name of differents parameters and 'value1'.. the values to test. (values must be
 * between quotes)<br>
 *
 * $Id$
 *
 */
public final class Expression {

    private Expression leftExpression;

    private Expression rightExpression;

    private String condition; // and / or

    private String expressionString;

    private boolean valid;

    private static final String AND = "and"; //$NON-NLS-1$

    private static final String OR = "or"; //$NON-NLS-1$

    private static final String EQUALS = "=="; //$NON-NLS-1$

    private static final String NOT_EQUALS = "!="; //$NON-NLS-1$

    private static final String GREAT_THAN = ">"; //$NON-NLS-1$

    private static final String LESS_THAN = "<"; //$NON-NLS-1$

    private static final String isShowPrefix = "isShow"; //$NON-NLS-1$

    private static final java.util.regex.Pattern isShowFuncPattern = java.util.regex.Pattern
            .compile(isShowPrefix + "\\[(\\w+)(\\.\\w+)*\\]"); //$NON-NLS-1$

    private static final String sparkVersionPrefix = "SPARK_"; //$NON-NLS-1$

    private static final java.util.regex.Pattern sparkVersionPattern = java.util.regex.Pattern
            .compile("(lt|le|gt|ge|eq|ne)\\s*'(" + sparkVersionPrefix + ".*)'"); //$NON-NLS-1$ //$NON-NLS-2$

    private static Perl5Matcher conditionMatcher = new Perl5Matcher();

    private static Pattern andPattern;

    private static Pattern orPattern;

    static {
        Perl5Compiler compiler = new Perl5Compiler();
        // example for the reg exp: (.*)[')][ ]*or[ ]*[\w(](.*)
        String prefixReg = "(.*)[') ][ ]*"; //$NON-NLS-1$
        String suffixReg = "[ ]*[ (](.*)"; //$NON-NLS-1$
        try {
            andPattern = compiler.compile(prefixReg + AND + suffixReg);
            orPattern = compiler.compile(prefixReg + OR + suffixReg);
        } catch (MalformedPatternException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String CONTAINS = "CONTAINS"; //$NON-NLS-1$

    private Expression(String expressionString) {
        this.expressionString = expressionString;
    }

    private String getExpressionString() {
        return this.expressionString;
    }

    private void setExpressionString(String value) {
        this.expressionString = value;
    }

    private String getCondition() {
        return this.condition;
    }

    private void setCondition(String condition) {
        this.condition = condition;
    }

    private Expression getLeftExpression() {
        return this.leftExpression;
    }

    private void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    private Expression getRightExpression() {
        return this.rightExpression;
    }

    private void setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
    }

    private boolean isValid() {
        return this.valid;
    }

    private void setValid(boolean value) {
        this.valid = value;
    }

    public static boolean evaluate(final String string, List<? extends IElementParameter> listParam) {
        return evaluate(string, listParam, null);
    }

    public static boolean evaluate(final String string, List<? extends IElementParameter> listParam,
            ElementParameter curParam) {
        if (Boolean.FALSE.toString().equals(string)) {
            return false;
        }
        if (Boolean.TRUE.toString().equals(string)) {
            return true;
        }

        if (string.contains("(") //$NON-NLS-1$
                && (isThereCondition(string, AND) || isThereCondition(string, OR))) {
            return evaluateExpression(new Expression(string), listParam, curParam).isValid();
        } else {
            String newValue; // remove brackets
            newValue = string.replace("(", ""); //$NON-NLS-1$ //$NON-NLS-2$
            newValue = newValue.replace(")", ""); //$NON-NLS-1$ //$NON-NLS-2$
            return evaluateSimpleExpression(newValue, listParam, curParam);
        }

    }

    protected static boolean isThereCondition(String expression, String condition) {
        expression = expression.toLowerCase();
        if (!expression.contains(condition)) {
            return false;
        }
        if (AND.equals(condition) && conditionMatcher.matches(expression, andPattern)) {
            return true;
        }
        if (OR.equals(condition) && conditionMatcher.matches(expression, orPattern)) {
            return true;
        }
        return false;
    }

    /**
     * Only works for any check in the schema actually, and only for DB_TYPE. Syntax should be like:
     *
     * SCHEMA.DB_TYPE IN ['BLOB','CLOB']
     *
     *
     * @param simpleExpression
     * @return
     */
    private static boolean evaluateInExpression(String simpleExpression, List<? extends IElementParameter> listParam) {
        String[] strings = null;
        if (simpleExpression.contains(" IN [")) { //$NON-NLS-1$
            strings = simpleExpression.split(" IN \\["); //$NON-NLS-1$
        } else {
            strings = simpleExpression.split(" IN\\["); //$NON-NLS-1$
        }
        String[] splittedString = strings[0].split("\\."); //$NON-NLS-1$
        String parameterName = splittedString[0]; // take parameter name (SCHEMA in example here)
        String variableToTest = splittedString[1]; // we take only the value DB_TYPE
        IElementParameter currentParam = null;
        for (IElementParameter param : listParam) {
            if (param.getName().equals(parameterName)) {
                currentParam = param;
                break;
            }
        }
        if (currentParam == null) {
            currentParam = listParam.get(0); // take the first one, in all case we only
        }
        // want to get the element linked

        String valuesToTest = strings[1].substring(0, strings[1].length() - 1); // string must be like:
                                                                                // 'BLOB','CLOB']
                                                                                // so remove the last ]
        String[] values = valuesToTest.split("'"); //$NON-NLS-1$
        if (values.length > 1) { // in this case we have something like: 'A','B','C' first, then values will be
            // like: <empty> / A / , / B / , / C / <empty>
            if ("SCHEMA".equals(parameterName) && variableToTest.contains("DB_TYPE")) { //$NON-NLS-1$ //$NON-NLS-2$
                IElement element = currentParam.getElement();
                if (element == null || (!(element instanceof INode))) {
                    throwUnsupportedExpression(simpleExpression, currentParam);
                    return false;
                }
                INode node = (INode) element;
                for (String value : values) {
                    if (value.isEmpty() || value.trim().equals(",")) { //$NON-NLS-1$
                        continue;
                    }
                    for (IMetadataTable table : node.getMetadataList()) {
                        for (IMetadataColumn column : table.getListColumns()) {
                            if (column.getType() != null && column.getType().equals(value)) {
                                return true;
                            }
                        }
                    }
                }
            } else if (currentParam.getFieldType() == EParameterFieldType.TABLE) {
                if (ArrayUtils.contains(currentParam.getListItemsDisplayCodeName(), variableToTest)) {
                    List<Map<String, Object>> allLines = (List<Map<String, Object>>) currentParam.getValue();
                    for (Map<String, Object> line : allLines) {
                        // for each line, check if the column we want have one of the value defined in the "IN".
                        Object o = line.get(variableToTest);
                        if (o != null && (o instanceof String)) {
                            String currentValue = (String) o;
                            for (String value : values) {
                                if (value.isEmpty() || value.trim().equals(",")) { //$NON-NLS-1$
                                    continue;
                                }
                                if (currentValue.equals(value)) {
                                    return true;
                                }
                            }

                        }
                    }
                }
            } else {
                throwUnsupportedExpression(simpleExpression, currentParam);
            }
        }
        return false;
    }

    private static void throwUnsupportedExpression(String simpleExpression, IElementParameter currentParam) {
        if (currentParam != null && currentParam.getElement() != null) {
            ExceptionHandler.process(new Exception("Element: '" + currentParam.getElement().getElementName() //$NON-NLS-1$
                    + "' does not support expression '" + simpleExpression + "'")); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            ExceptionHandler.process(new Exception("Expression '" + simpleExpression + "' not supported")); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    private static boolean evaluateSimpleExpression(String simpleExpression,
            List<? extends IElementParameter> listParam, ElementParameter currentParam) {
        boolean showParameter = false;
        String test = null;
        if (simpleExpression.contains(EQUALS)) {
            test = EQUALS;
        } else if (simpleExpression.contains(NOT_EQUALS)) {
            test = NOT_EQUALS;
        } else if (simpleExpression.contains(GREAT_THAN)) {
            test = GREAT_THAN;
        } else if (simpleExpression.contains(LESS_THAN)) {
            test = LESS_THAN;
        }
        if ((simpleExpression.contains(" IN [") || //$NON-NLS-1$
                simpleExpression.contains(" IN[")) && simpleExpression.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
            return evaluateInExpression(simpleExpression, listParam);
        }

        if ((simpleExpression.contains("DISTRIB["))) { //$NON-NLS-1$
            return evaluateDistrib(simpleExpression, listParam, currentParam);
        }
        if ((simpleExpression.contains("SPARK_VERSION["))) { //$NON-NLS-1$
            return evaluateSparkVersion(simpleExpression, listParam, currentParam);
        }
        
        if ((simpleExpression.contains(CONTAINS))) { //$NON-NLS-1$
            return evaluateContains(simpleExpression, listParam);
        }

        List<String> paraNames = getParaNamesFromIsShowFunc(simpleExpression);
        if (paraNames.size() > 0) {
            // Here only be one isShow() function since it has been already split.
            String paraName = paraNames.get(0);
            try {
                checkIsShowLoop(paraName, simpleExpression, listParam, currentParam, null);
            } catch (Exception e) {
                ExceptionHandler.process(e);
                return false;
            }
            for (IElementParameter param : listParam) {
                if (paraName != null && paraName.equals(param.getName())) {
                    if (simpleExpression.startsWith("!")) { //$NON-NLS-1$
                        return !param.isShow(param.getShowIf(), param.getNotShowIf(), listParam);
                    } else {
                        return param.isShow(param.getShowIf(), param.getNotShowIf(), listParam);
                    }
                }
            }
        }

        if (test == null) {
            throwUnsupportedExpression(simpleExpression, currentParam);
            return false;
        }

        String[] strings = simpleExpression.split(test);

        String variableName = null, variableValue = null;

        for (String string2 : strings) {
            String string = string2.trim();
            if (string.contains("'")) { // value //$NON-NLS-1$
                variableValue = string;
                variableValue = variableValue.substring(1, string.lastIndexOf("'")); //$NON-NLS-1$
            } else {
                variableName = string;
            }
        }
        /*
         * this is only for Current OS condition.
         */
        if (variableName != null && EParameterName.CURRENT_OS.getName().equals(variableName)) {
            if (variableValue != null) {
                if (EQUALS.endsWith(test)) {
                    return checkCurrentOS(variableValue);
                } else if (NOT_EQUALS.equals(test)) {
                    return !checkCurrentOS(variableValue);
                }
            }
        }
        if (listParam == null) {
            return false;
        }

        /*
         * this only used to check is EE version or not
         */
        if ("IS_STUDIO_EE_VERSION".equals(variableName)) { //$NON-NLS-1$
            boolean isTIS = PluginChecker.isTIS();
            if ("true".equals(variableValue)) { //$NON-NLS-1$
                return isTIS;
            } else {
                return !isTIS;
            }
        }

        /*
         * this only used to check is IPAAS Components are Loaded or not
         */
        if ("IS_STUDIO_IPAAS_VERSION".equals(variableName)) { //$NON-NLS-1$
            boolean isIPaas = PluginChecker.isIPaasPluginLoaded();
            if ("true".equals(variableValue)) { //$NON-NLS-1$
                return isIPaas;
            } else {
                return !isIPaas;
            }
        }

        // 3 levels of variable name accepted maximum (ex: MY_VAR.TABLE.FIELD == 'test')
        String[] varNames;
        varNames = StringUtils.split(variableName, '.');

        // wyang: only for bug:9055, to search the related Node, for example tFTPGet wants to check tFTPConnection info
        // variableName like: #LINK@NODE.CONNECTION.SFTP ----->it is a checkbox in tFTPConnection
        // #LINK@NODE, #PREVIOUS@NODE, #NEXT@NODE ----->implement them later

        if ((variableName != null) && (variableValue != null)) {
            if (varNames[0].equals("#LINK@NODE")) { //$NON-NLS-1$
                INode node = null;
                if (currentParam != null && currentParam.getElement() instanceof INode) {
                    node = (INode) currentParam.getElement();
                } else if (currentParam == null) {
                    if (listParam != null && listParam.size() > 0) {
                        IElement element = listParam.get(0).getElement();
                        if (element instanceof INode) {
                            node = (INode) element;
                        }
                    }
                }
                if (node != null) {
                    String relatedNodeName = ElementParameterParser.getValue(node, "__" + varNames[1] + "__"); //$NON-NLS-1$ //$NON-NLS-2$
                    // if relatedNodeName is empty, maybe means this property have not been setted
                    if (relatedNodeName != null && !relatedNodeName.trim().isEmpty()) {
                        List<? extends INode> generatingNodes = node.getProcess().getGeneratingNodes();
                        for (INode aNode : generatingNodes) {
                            if (aNode.getUniqueName().equals(relatedNodeName)) {
                                simpleExpression = simpleExpression.replace(varNames[0] + "." + varNames[1] + ".", ""); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
                                List<? extends IElementParameter> elementParameters = aNode.getElementParameters();
                                // let's supose the currentParam = null, there won't want deal with the TABLE field,
                                // only deal with LIST/CHECKBOX
                                return evaluate(simpleExpression, elementParameters);

                            }
                        }
                    }
                }
            }
            /*
             * TESB-6240 GangLiu Test the connection configuration.
             */
            else if ("#LINK@CONNECTOR".equals(varNames[0])) { //$NON-NLS-1$
                if (listParam != null && listParam.size() > 0) {
                    IElement element = listParam.get(0).getElement();
                    if (element != null && element instanceof INode) {
                        INode node = (INode) element;
                        if (varNames.length > 2 && varNames[1] != null && varNames[2] != null) {
                            // read in/out connection type accounts
                            List<? extends IConnection> connections = new ArrayList<IConnection>();
                            if ("IN".equals(varNames[1]) || "OUT".equals(varNames[1])) { //$NON-NLS-1$ //$NON-NLS-2$
                                if ("IN".equals(varNames[1])) { //$NON-NLS-1$
                                    if ("ANY".equals(varNames[2])) { //$NON-NLS-1$
                                        connections = node.getIncomingConnections();
                                    } else {
                                        connections = node.getIncomingConnections(EConnectionType.valueOf(varNames[2]));
                                    }
                                } else {
                                    if ("ANY".equals(varNames[2])) { //$NON-NLS-1$
                                        connections = node.getOutgoingConnections();
                                    } else {
                                        connections = node.getOutgoingConnections(EConnectionType.valueOf(varNames[2]));
                                    }
                                }
                                try {
                                    int connSize = connections.size();
                                    int targetNumber = Integer.parseInt(variableValue);
                                    if (GREAT_THAN.equals(test)) {
                                        return connSize > targetNumber;
                                    } else if (LESS_THAN.equals(test)) {
                                        return connSize < targetNumber;
                                    } else if (EQUALS.equals(test)) {
                                        return connSize == targetNumber;
                                    } else if (NOT_EQUALS.equals(test)) {
                                        return connSize != targetNumber;
                                    }
                                } catch (Exception e) {
                                }
                            } else {
                                // read specific connection parameter
                                connections = node.getOutgoingConnections(EConnectionType.valueOf(varNames[1]));
                                for (IConnection c : connections) {
                                    IElementParameter elementParameter = c.getElementParameter(varNames[2]);
                                    if (variableValue.equals(elementParameter.getValue())) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            } // End of TESB-6240
            else if ("#NODE@IN".equals(varNames[0])) { //$NON-NLS-1$
                if (listParam != null && listParam.size() > 0) {
                    IElement element = listParam.get(0).getElement();
                    if (element != null && element instanceof IConnection) {
                        INode sourceNode = ((IConnection) element).getSource();
                        // change from: #NODE@IN.SUBTREE_START == 'false'
                        // to: SUBTREE_START == 'false'
                        simpleExpression = simpleExpression.replace(varNames[0] + ".", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        return evaluate(simpleExpression, sourceNode.getElementParameters());
                    }
                }
            } else if ("#NODE@OUT".equals(varNames[0])) { //$NON-NLS-1$
                if (listParam != null && listParam.size() > 0) {
                    IElement element = listParam.get(0).getElement();
                    if (element != null && element instanceof IConnection) {
                        INode sourceNode = ((IConnection) element).getTarget();
                        // change from: #NODE@OUT.END_OF_FLOW == 'false'
                        // to: END_OF_FLOW == 'false'
                        simpleExpression = simpleExpression.replace(varNames[0] + ".", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        return evaluate(simpleExpression, sourceNode.getElementParameters());
                    }
                }
            }
        }

        if ((variableName != null) && (variableValue != null)) {
            for (IElementParameter param : listParam) {

                if (param.getName().equals(varNames[0])) {
                    IElementParameter testedParameter = param;
                    Object value = null;
                    boolean found = false;
                    if (EParameterFieldType.TABLE.equals(param.getFieldType())) {
                        List<Map<String, Object>> tableValues = (List<Map<String, Object>>) param.getValue();
                        if (currentParam == null) {
                            continue;
                        }
                        if (tableValues.size() <= currentParam.getCurrentRow()) {
                            break;
                        }
                        Map<String, Object> currentRow = tableValues.get(currentParam.getCurrentRow());
                        if (currentRow.containsKey(varNames[1])) {
                            for (Object curObj : param.getListItemsValue()) {
                                if (curObj instanceof IElementParameter) {
                                    IElementParameter testParam = (IElementParameter) curObj;
                                    if (testParam.getName().equals(varNames[1])) {
                                        testedParameter = testParam;
                                        break;
                                    }
                                }
                            }
                            if (varNames.length == 2) { // simple value
                                value = currentRow.get(varNames[1]);
                            } else {
                                if ("TYPE".equals(varNames[2])) { //$NON-NLS-1$
                                    IMetadataTable baseTable = null;
                                    IMetadataColumn baseColumn = null;
                                    INode node;
                                    Object obj = currentRow.get(testedParameter.getName());
                                    String columnName = ""; //$NON-NLS-1$
                                    if (obj instanceof String) {
                                        columnName = (String) obj;
                                    } else if (obj instanceof Integer) {
                                        int index = (Integer) obj;
                                        if (index < testedParameter.getListItemsDisplayName().length && index >= 0) {
                                            columnName = testedParameter.getListItemsDisplayName()[(Integer) obj];
                                        }
                                    }
                                    if (currentParam.getElement() instanceof INode) {
                                        node = (INode) currentParam.getElement();

                                        switch (testedParameter.getFieldType()) {
                                        case COLUMN_LIST:
                                            baseTable = node.getMetadataList().get(0);
                                            break;
                                        case PREV_COLUMN_LIST:
                                            IConnection connection = null;
                                            for (int i = 0; i < node.getIncomingConnections().size(); i++) {
                                                IConnection curConnec = node.getIncomingConnections().get(i);
                                                if (curConnec.getLineStyle() == EConnectionType.FLOW_MAIN) {
                                                    connection = curConnec;
                                                    break;
                                                }
                                            }
                                            if (connection != null) {
                                                baseTable = connection.getMetadataTable();
                                            }
                                            break;
                                        case LOOKUP_COLUMN_LIST:
                                            List<IConnection> refConnections = new ArrayList<IConnection>();
                                            for (int i = 0; i < node.getIncomingConnections().size(); i++) {
                                                IConnection curConnec = node.getIncomingConnections().get(i);
                                                if (curConnec.getLineStyle() == EConnectionType.FLOW_REF) {
                                                    refConnections.add(curConnec);
                                                }
                                            }
                                            for (IConnection curConnec : refConnections) {
                                                IMetadataTable table = curConnec.getMetadataTable();
                                                for (IMetadataColumn column : table.getListColumns()) {
                                                    String name = curConnec.getName() + "." + column.getLabel(); //$NON-NLS-1$
                                                    if (name.equals(columnName)) {
                                                        baseColumn = column;
                                                    }
                                                }
                                            }
                                            break;
                                        default:
                                        }

                                        if (baseTable != null) {
                                            for (IMetadataColumn column : baseTable.getListColumns()) {
                                                if (column.getLabel().equals(columnName)) {
                                                    baseColumn = column;
                                                    break;
                                                }
                                            }
                                        }

                                        if (baseColumn != null) {
                                            switch (LanguageManager.getCurrentLanguage()) {
                                            case JAVA:
                                                value =
                                                        JavaTypesManager.getTypeToGenerate(baseColumn.getTalendType(),
                                                                baseColumn.isNullable());
                                                // PARTICULAR_SURVIVORSHIP and SURVIVORSHIP_JOIN_KEY are the table names
                                                // which will use the filter
                                                IElementParameter filterParameter =
                                                        node.getElementParameter(param.getName() + "_FILTER_PARAMETER"); //$NON-NLS-1$
                                                if (filterParameter != null
                                                        && "true".equals(filterParameter.getValue().toString()) //$NON-NLS-1$
                                                        // Number is from DefaultSurvivorShipDataTypeEnum in
                                                        // record.linkage plugin
                                                        && "Number".equals(variableValue) //$NON-NLS-1$
                                                        && JavaTypesManager.isNumber(baseColumn.getTalendType())) {
                                                    value = variableValue;
                                                }
                                                break;
                                            default:
                                                value = baseColumn.getTalendType();
                                            }

                                            if (value.equals(variableValue)) {
                                                found = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (param.getFieldType() != null
                            && (param.getFieldType().equals(EParameterFieldType.PROPERTY_TYPE)
                                    || param.getFieldType().equals(EParameterFieldType.SCHEMA_TYPE)
                                    || param.getFieldType().equals(EParameterFieldType.SCHEMA_REFERENCE)
                                    || param.getFieldType().equals(EParameterFieldType.QUERYSTORE_TYPE)
                                    || param.getFieldType().equals(EParameterFieldType.ENCODING_TYPE))) {

                        boolean child = false;
                        Map<String, IElementParameter> childParameters = param.getChildParameters();

                        if ("PROPERTY".equals(param.getName()) || EParameterFieldType.PROPERTY_TYPE == param.getFieldType()) { //$NON-NLS-1$
                            if (childParameters != null) {
                                IElementParameter iElementParameter = childParameters.get("PROPERTY_TYPE"); //$NON-NLS-1$
                                if (iElementParameter != null) {
                                    Object value2 = iElementParameter.getValue();
                                    if (variableValue.equals(value2.toString())) {
                                        child = true;
                                        found = true;
                                        value = value2.toString();
                                    }
                                }
                            }
                        }

                        if (varNames.length > 1 && varNames[1] != null) {
                            IElementParameter tempParam = childParameters.get(varNames[1]);
                            if (tempParam != null) {
                                value = tempParam.getValue();
                                if (value.equals(variableValue)) {
                                    found = true;
                                }
                                child = true;
                            }
                        }
                        if (!child) {
                            value = testedParameter.getValue();
                        }
                    } else {
                        value = testedParameter.getValue();
                    }
                    if (value instanceof Integer) {
                        if (((Integer) value) < testedParameter.getListItemsValue().length) {
                            value = testedParameter.getListItemsValue()[(Integer) value];
                        }
                    }
                    if (value instanceof String) {
                        if (variableValue.equals(value)) {
                            found = true;
                        } else if (testedParameter.getListItemsValue() instanceof Object[]) {
                            Object[] values = testedParameter.getListItemsValue();
                            for (int i = 0; i < values.length && !found; i++) {
                                if (value.equals(values[i])) {
                                    String[] listItemsDisplayCodeName = testedParameter.getListItemsDisplayCodeName();
                                    if(listItemsDisplayCodeName!=null) {
                                        String variableCode = listItemsDisplayCodeName[i];
                                        if (variableCode.equals(variableValue)) {
                                            found = true;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (value instanceof Boolean) {
                        Boolean tmpValue = new Boolean(variableValue);
                        if (tmpValue.equals(value)) {
                            found = true;
                        }
                    }

                    if (found) {
                        if (test.equals(EQUALS)) {
                            showParameter = true;
                        }
                    } else {
                        if (test.equals(NOT_EQUALS)) {
                            showParameter = true;
                        }
                    }

                    break;
                }
            }
            if (currentParam != null && "INCOMING_LINK_TYPE".equals(variableName)) {//$NON-NLS-1$
                IElement element = currentParam.getElement();
                if (element != null && element instanceof INode) {
                    INode node = (INode) element;
                    if (node.getComponent() != null && "tPigLoad".equals(node.getComponent().getName())) { //$NON-NLS-1$
                        List<IConnection> connectionsInputs = (List<IConnection>) node.getIncomingConnections();
                        for (IConnection connection : connectionsInputs) {
                            if (connection.isActivate()
                                    && connection.getLineStyle().hasConnectionCategory(IConnectionCategory.MAIN)
                                    && variableValue.toUpperCase().equals(connection.getConnectorName())) {
                                showParameter = true;
                            }
                        }
                    }
                }
            }
        }
        return showParameter;
    }

    private static INode retrieveNodeElementFromParameter(ElementParameter currentParam,
            List<? extends IElementParameter> listParam) {
        if (currentParam != null && currentParam.getElement() instanceof INode) {
            return (INode) currentParam.getElement();
        } else if (currentParam != null && currentParam.getElement() instanceof Process) {
            List<INode> sparkConfiguration = ((Process)currentParam.getElement()).getNodesOfType("tSparkConfiguration");
            return sparkConfiguration.size() > 0 ? (INode) sparkConfiguration.get(0) : null;
        } else if (currentParam == null) {
            if (listParam != null && listParam.size() > 0) {
                IElement element = listParam.get(0).getElement();
                if (element instanceof INode) {
                    return (INode) element;
                }
            }
        }
        return null;
    }

    private static String retrieveLinkedParamName(String parameter) {
        return parameter.replace(parameter.split("\\.")[0] + "." + parameter.split("\\.")[1] + ".", ""); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    private static List<? extends IElementParameter> retrieveLinkedParameters(ElementParameter currentParam,
            List<? extends IElementParameter> listParam, String distributionParam) {
        INode node = retrieveNodeElementFromParameter(currentParam, listParam);

        if (node != null) {
            String relatedNodeName =
                    ElementParameterParser.getValue(node, "__" + distributionParam.split("\\.")[1] + "__"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            // if relatedNodeName is empty, maybe means this property have not been setted
            if (relatedNodeName != null && !relatedNodeName.trim().isEmpty()) {
                for (INode aNode : node.getProcess().getGeneratingNodes()) {
                    if (aNode.getUniqueName().equals(relatedNodeName)) {
                        return aNode.getElementParameters();
                    }
                }
            }
        }
        return listParam;
    }

    // should be private, but need to unitary tested
    public static boolean evaluateDistrib(String simpleExpression, List<? extends IElementParameter> listParam,
            ElementParameter currentParam) {
        boolean positiveAssertion = !simpleExpression.trim().startsWith("!"); //$NON-NLS-1$
        String args = (simpleExpression.split("\\[")[1]).split("\\]")[0]; //$NON-NLS-1$ //$NON-NLS-2$
        String distributionParam = args.split(",")[0].trim(); //$NON-NLS-1$
        String versionParam = args.split(",")[1].trim(); //$NON-NLS-1$

        // both distributionParam and versionParam are simple or both are link. Everything else is an error.
        if (distributionParam.startsWith("#LINK@NODE") != versionParam.startsWith("#LINK@NODE")) { //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }

        List<? extends IElementParameter> effectiveListParam = listParam;
        if (distributionParam.startsWith("#LINK@NODE") && versionParam.startsWith("#LINK@NODE")) { //$NON-NLS-1$ //$NON-NLS-2$
            // Handle the #LINK@NODE
            effectiveListParam = retrieveLinkedParameters(currentParam, listParam, distributionParam);
            distributionParam = retrieveLinkedParamName(distributionParam);
            versionParam = retrieveLinkedParamName(versionParam);

        }

        // distributionParam and versionParam should not be null, but I prefer to be sure
        String distribution = null;
        String version = null;
        if ((distributionParam != null) && (versionParam != null)) {
            // Handle the normal case
            for (IElementParameter param : effectiveListParam) {
                if (distributionParam.equals(param.getName())) {
                    distribution = (String) param.getValue();
                }
                if (versionParam.equals(param.getName())) {
                    version = (String) param.getValue();
                }
            }
        }

        // No distribution found => error case
        if (distribution == null) {
            return false;
        }

        String methodName = simpleExpression.split("\\].")[1].split("\\[")[0]; //$NON-NLS-1$ //$NON-NLS-2$
        return executeBooleanMethod(methodName, distribution, version, positiveAssertion);
    }

    // should be private, but need to unitary tested
    public static boolean evaluateSparkVersion(String simpleExpression, List<? extends IElementParameter> listParam,
            ElementParameter currentParam) {
        if (!simpleExpression.contains(sparkVersionPrefix)) {
            return false;
        }
        INode node = retrieveNodeElementFromParameter(currentParam, listParam);
        ESparkVersion version = SparkVersionUtil.getSparkVersion(node);
        if (version == null) {
            return false;
        }
        Matcher m = sparkVersionPattern.matcher(simpleExpression);
        if (m.find()) {
            ESparkVersion versionToTest = ESparkVersion.valueOf(m.group(2));
            switch (m.group(1)) {
            case "lt": //$NON-NLS-1$
                return version.compareTo(versionToTest) < 0;
            case "le": //$NON-NLS-1$
                return version.compareTo(versionToTest) <= 0;
            case "gt": //$NON-NLS-1$
                return version.compareTo(versionToTest) > 0;
            case "ge": //$NON-NLS-1$
                return version.compareTo(versionToTest) >= 0;
            case "eq": //$NON-NLS-1$
                return version.compareTo(versionToTest) == 0;
            case "ne": //$NON-NLS-1$
                return version.compareTo(versionToTest) != 0;
            }
        }

        return false;
    }

    /**
     * Execute a methode for a given distribution and version. This function must return a booelan
     *
     * @param methodName the name of the method
     * @param distribution the name of the distribution
     * @param version the name of the version
     * @positiveAssertion if we are on a positive assertion. A negative one will inverse the return of the method,
     * except on an error case.
     * @return
     */
    private static boolean executeBooleanMethod(String methodName, String distribution, String version,
            boolean positiveAssertion) {
        try {
            boolean ret = DistributionFactory.executeBooleanMethod(methodName, distribution, version);
            return positiveAssertion ? ret : !ret;
        } catch (Exception e) {
            org.talend.commons.exception.ExceptionHandler.process(e);
        }
        // return false on error case, even when we are and a negative assertion
        return false;
    }

    private static List<String> getParaNamesFromIsShowFunc(String expr) {
        List<String> paraNames = new ArrayList<String>();
        if (expr == null || !expr.contains(isShowPrefix)) {
            return paraNames;
        }
        Matcher matcher = isShowFuncPattern.matcher(expr);
        while (matcher.find()) {
            String paraName = matcher.group(1) + (matcher.group(2) == null ? "" : matcher.group(2)); //$NON-NLS-1$
            if (!paraNames.contains(paraName)) {
                paraNames.add(paraName);
            }
        }

        return paraNames;
    }

    private static void checkIsShowLoop(String testParamName, String expression,
            List<? extends IElementParameter> listParam, IElementParameter currentParam, List<String> testedParaNames)
            throws Exception {
        List<String> paraNames = testedParaNames;
        if (paraNames == null) {
            paraNames = new ArrayList<String>();
        }
        String currentParaName = null;
        if (currentParam != null) {
            currentParaName = currentParam.getName();
            if (!paraNames.contains(currentParaName)) {
                paraNames.add(currentParaName);
            }
        }

        if (paraNames.contains(testParamName)) {
            if (currentParam != null) {
                throw new Exception("Expression \"" + expression + "\" of parameter \"" + currentParam.getName() //$NON-NLS-1$ //$NON-NLS-2$
                        + "\" bring an endless loop by parameter \"" + testParamName + "\" in the element \"" //$NON-NLS-1$ //$NON-NLS-2$
                        + currentParam.getElement().getElementName() + "\". Please check and amend it!"); //$NON-NLS-1$
            } else {
                throw new Exception(
                        "Expression \"" + expression + "\" bring an endless loop by parameter \"" + testParamName //$NON-NLS-1$ //$NON-NLS-2$
                                + "\". Please check and amend it!"); //$NON-NLS-1$
            }
        } else {
            paraNames.add(testParamName);
            for (IElementParameter param : listParam) {
                if (testParamName != null && testParamName.equals(param.getName())) {
                    List<String> paramNames = getParaNamesFromIsShowFunc(param.getShowIf());
                    for (String paramName : paramNames) {
                        checkIsShowLoop(paramName, expression, listParam, currentParam, paraNames);
                    }
                }
            }
        }
    }

    private static Expression evaluateExpression(Expression expression, List<? extends IElementParameter> listParam,
            ElementParameter currentParam) {
        int indexBegining = 0, indexEnd;
        int expressionLevel = 0;
        String string = expression.getExpressionString();
        boolean conditionFound = false;

        // if there's no braket then there should be only simple expression
        // or only one expression.
        for (int i = 0; i < string.length() && !conditionFound; i++) {
            if (string.charAt(i) == '(') {
                if (expressionLevel == 0) {
                    indexBegining = i + 1;
                }
                expressionLevel++;
            } else if (string.charAt(i) == ')') {
                expressionLevel--;
                indexEnd = i;

                if (expressionLevel == 0) {
                    if (isThereCondition(string, AND) || isThereCondition(string, OR)) {
                        String leftString = string.substring(indexBegining, indexEnd).trim();
                        if (isThereCondition(leftString, AND) || isThereCondition(leftString, OR)) {
                            Expression leftExpression = new Expression(leftString);
                            expression.setLeftExpression(leftExpression);
                            evaluateExpression(leftExpression, listParam, currentParam);
                        } else {
                            Expression leftExpression = new Expression(leftString);
                            expression.setLeftExpression(leftExpression);
                            leftExpression.setValid(evaluateSimpleExpression(leftString, listParam, currentParam));
                            // debug: System.out.println(leftString + " => " +
                            // leftExpression.isValid());
                        }
                    } else {
                        String newValue; // remove brackets
                        newValue = string.replace("(", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        newValue = newValue.replace(")", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        expression.setExpressionString(newValue);
                        expression.setValid(evaluateSimpleExpression(newValue, listParam, currentParam));
                    }
                }
            } else if (expressionLevel == 0) {
                if ((string.indexOf(AND, i) == i) || (string.indexOf(AND.toUpperCase(), i) == i)) {
                    String subStr = string.substring(i - 3, i + 5);
                    if (isThereCondition(subStr, AND)) {
                        expression.setCondition(AND);
                        conditionFound = true;
                    }
                } else if ((string.indexOf(OR, i) == i) || (string.indexOf(OR.toUpperCase(), i) == i)) {
                    String subStr = string.substring(i - 3, i + 5);
                    if (isThereCondition(subStr, OR)) {
                        expression.setCondition(OR);
                        conditionFound = true;
                    }
                }
            }
            if (conditionFound) {
                if (expression.getLeftExpression() == null) { // no bracket ==
                    // evaluate
                    // expression
                    String leftString = string.substring(0, i - 1).trim();
                    Expression leftExpression = new Expression(leftString);
                    expression.setLeftExpression(leftExpression);
                    leftExpression.setValid(evaluateSimpleExpression(leftString, listParam, currentParam));
                    // debug: System.out.println(leftString + " => " +
                    // leftExpression.isValid());
                }

                boolean needValidRightExpression = true;
                if (expression.getCondition().equals(AND) && !expression.getLeftExpression().isValid()) {
                    // if left expression is already false, then needn't continue to validate right expression
                    expression.setValid(false);
                    needValidRightExpression = false;
                } else if (expression.getCondition().equals(OR) && expression.getLeftExpression().isValid()) {
                    // if left expression is already true, then need't continue to validate right expression
                    expression.setValid(true);
                    needValidRightExpression = false;
                }

                if (needValidRightExpression) {
                    String rightString = string.substring(i + 3, string.length()).trim();
                    Expression rightExpression = new Expression(rightString);
                    expression.setRightExpression(rightExpression);
                    if (rightString.contains("(") //$NON-NLS-1$
                            || isThereCondition(rightString, AND) || isThereCondition(rightString, OR)) {
                        evaluateExpression(rightExpression, listParam, currentParam);
                    } else { // no bracket == evaluate expression
                        rightExpression.setValid(evaluateSimpleExpression(rightString, listParam, currentParam));
                        // debug: System.out.println(rightString + " => " +
                        // rightExpression.isValid());
                    }
                    if (expression.getCondition().equals(AND)) {
                        if (expression.getLeftExpression().isValid() && expression.getRightExpression().isValid()) {
                            expression.setValid(true);
                        } else {
                            expression.setValid(false);
                        }
                    } else if (expression.getCondition().equals(OR)) {
                        if (expression.getLeftExpression().isValid() || expression.getRightExpression().isValid()) {
                            expression.setValid(true);
                        } else {
                            expression.setValid(false);
                        }
                    }
                }
            }
        }
        // if after an expression between bracket there's no other expression,
        // then the validation of the expression
        // will depends on the "left" expression.
        if ((expression.getRightExpression() == null) && (expression.getLeftExpression() != null)) {
            expression.setValid(expression.getLeftExpression().isValid());
        }
        // debug: System.out.println(expression.getExpressionString() + " => " +
        // expression.isValid());
        return expression;
    }

    /*
     * check this OS
     */
    private static boolean checkCurrentOS(final String osName) {
        if (osName == null) {
            return false;
        }
        final String tmpOSName = osName.toLowerCase();
        // check windows
        if (EnvironmentUtils.isWindowsSystem() && tmpOSName.startsWith("windows")) { //$NON-NLS-1$
            return true;
        }
        // check MacOS
        if (EnvironmentUtils.isMacOsSytem() && tmpOSName.startsWith("mac")) { //$NON-NLS-1$
            return true;
        }
        // check linux and unix
        if (EnvironmentUtils.isLinuxUnixSystem()) {
            if (tmpOSName.startsWith("unix") || tmpOSName.startsWith("linux")) { //$NON-NLS-1$ //$NON-NLS-2$
                return true;
            }
        }
        return false;
    }
    
    public static boolean evaluateContains(String simpleExpression, List<? extends IElementParameter> listParam) {
        //Split to get param name and param value to look after
        String[] splitted = simpleExpression.split(CONTAINS);
        if (splitted.length != 2) {
            return false;
        }
        String paramName = splitted[0].trim();
        String paramValue = splitted[1].trim();
        
        //Look for the param name in list
        IElementParameter param = listParam.stream()
                .filter(p -> paramName.equals(p.getName()))
                .findAny()
                .orElse(null);
        if (param == null || ! EParameterFieldType.TABLE.equals(param.getFieldType())) {
            return false;
        }
        
        // Check if we can find paraValue among table lines
        return ((List<Map<String, Object>>) param.getValue()).stream()
                .anyMatch(line -> paramValue.equals(line.toString()));
    }    

}
