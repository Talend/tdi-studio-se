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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Util;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.utils.data.text.StringHelper;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.core.model.components.ModifyComponentsAction;
import org.talend.core.model.components.conversions.IComponentConversion;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.migration.AbstractJobMigrationTask;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.ConnectionType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.ElementValueType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;

/**
 * Use to replace .
 *
 */
public class AddPerlRefArrayMigrationTask extends AbstractJobMigrationTask {

    @Override
	public ExecutionResult execute(Item item) {
        try {
        	ProcessType processType = getProcessType(item);
			if (getProject().getLanguage() == ECodeLanguage.JAVA
					|| processType == null) {

                return ExecutionResult.NOTHING_TO_DO;

            } else {

                List<String> namesList = new ArrayList<String>();

                for (Object o : processType.getNode()) {
                    NodeType nt = (NodeType) o;
                    namesList.add(ComponentUtilities.getNodeUniqueName(nt));
                }
                for (Object o : processType.getConnection()) {
                    ConnectionType currentConnection = (ConnectionType) o;
                    int lineStyle = currentConnection.getLineStyle();
                    EConnectionType connectionType = EConnectionType.getTypeFromId(lineStyle);
                    if (connectionType.hasConnectionCategory(EConnectionType.FLOW)) {
                        namesList.add(currentConnection.getLabel());
                    }
                }
                final String[] namesArrays = namesList.toArray(new String[0]);

                IComponentFilter filter1 = new IComponentFilter() {

                    /*
                     * (non-Javadoc)
                     *
                     * @see org.talend.core.model.components.filters.IComponentFilter#accept(org.talend.designer.core.model.utils.emf.talendfile.NodeType)
                     */
                    public boolean accept(NodeType node) {
                        return true;
                    }

                };

                IComponentConversion componentConversion = new IComponentConversion() {

                    RefArraySyntaxReplacerForPerl parser = new RefArraySyntaxReplacerForPerl();

                    /*
                     * (non-Javadoc)
                     *
                     * @see org.talend.core.model.components.conversions.IComponentConversion#transform(org.talend.designer.core.model.utils.emf.talendfile.NodeType)
                     */
                    public void transform(NodeType node) {

                        for (Object o : node.getElementParameter()) {
                            ElementParameterType pType = (ElementParameterType) o;
                            if (pType.getField().equals("TABLE")) { //$NON-NLS-1$
                                for (ElementValueType elementValue : (List<ElementValueType>) pType.getElementValue()) {
                                    elementValue.getValue();
                                    String value = elementValue.getValue();
                                    if (value != null) {
                                        String newValue = parser.processReplacementOperations(value, namesArrays);
                                        elementValue.setValue(newValue);
                                    }
                                }
                            } else {
                                String value = pType.getValue();
                                if (value != null) {
                                    String newValue = parser.processReplacementOperations(value, namesArrays);
                                    pType.setValue(newValue);
                                }
                            }
                        }

                    }

                };

                ModifyComponentsAction.searchAndModify(item, processType, filter1, Arrays.<IComponentConversion> asList(componentConversion));

                return ExecutionResult.SUCCESS_WITH_ALERT;
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            return ExecutionResult.FAILURE;
        }
    }

    /**
     *
     * DOC amaumont AddPerlRefArrayPointer class global comment. Detailled comment
     */
    public class RefArraySyntaxReplacerForPerl {

        // private String expression;
        private Perl5Matcher matcher = new Perl5Matcher();

        private Perl5Compiler compiler = new Perl5Compiler();

        private Set<TableEntryLocation> resultList = new HashSet<TableEntryLocation>();

        private Pattern pattern;

        private PatternMatcherInput patternMatcherInput;

        private String locationPattern;

        public RefArraySyntaxReplacerForPerl() {
            super();
        }

        /**
         * DOC amaumont Comment method "setRegexpPattern".
         *
         * @param regexpPattern
         */
        public void setLocationPattern(String locationPattern) {
            this.locationPattern = locationPattern;
        }

        public TableEntryLocation[] parseTableEntryLocations(String expression) {
            resultList.clear();
            if (expression != null) {
                matcher.setMultiline(true);
                if (patternMatcherInput == null) {
                    patternMatcherInput = new PatternMatcherInput(expression);
                } else {
                    patternMatcherInput.setInput(expression);
                }

                recompilePatternIfNecessary(locationPattern);

                while (matcher.contains(patternMatcherInput, pattern)) {
                    MatchResult matchResult = matcher.getMatch();
                    resultList.add(new TableEntryLocation(matchResult.group(1), matchResult.group(2)));
                }
            }
            return resultList.toArray(new TableEntryLocation[0]);
        }

        private void recompilePatternIfNecessary(String regexpPattern) {
            if (pattern == null || !regexpPattern.equals(pattern.getPattern())) {
                try {
                    pattern = compiler.compile(regexpPattern);
                } catch (MalformedPatternException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public String processReplacementOperations(String expression, String[] tableNames) {
            String returnedExpression = expression;

            for (int i = 0; i < tableNames.length; i++) {
                String connectionName = tableNames[i];

                recompilePatternIfNecessary(StringHelper.replacePrms("\\$[\\s\\r\\n]*({0})[\\s\\r\\n]*\\[", //$NON-NLS-1$
                        new Object[] { connectionName }));
                if (returnedExpression != null) {
                    matcher.setMultiline(true);
                    Perl5Substitution substitution = new Perl5Substitution("\\$" + "$1->" //$NON-NLS-1$ //$NON-NLS-2$
                            + "[", Perl5Substitution.INTERPOLATE_ALL); //$NON-NLS-1$
                    returnedExpression = Util.substitute(matcher, pattern, substitution, returnedExpression, Util.SUBSTITUTE_ALL);
                }

                recompilePatternIfNecessary(StringHelper.replacePrms("@[\\s\\r\\n]*({0})\\b", new Object[] { connectionName })); //$NON-NLS-1$
                if (returnedExpression != null) {
                    matcher.setMultiline(true);
                    Perl5Substitution substitution = new Perl5Substitution("@\\$" + "$1" //$NON-NLS-1$ //$NON-NLS-2$
                    , Perl5Substitution.INTERPOLATE_ALL);
                    returnedExpression = Util.substitute(matcher, pattern, substitution, returnedExpression, Util.SUBSTITUTE_ALL);
                }

            }
            return returnedExpression;
        }

    }

    /**
     *
     * DOC amaumont AddPerlRefArrayPointer class global comment. Detailled comment
     */
    public class TableEntryLocation {

        public String tableName;

        public String columnName;

        public TableEntryLocation() {
            super();
        }

        /**
         * DOC amaumont Couple constructor comment.
         *
         * @param tableName
         * @param columnName
         */
        public TableEntryLocation(String tableName, String columnName) {
            this.tableName = tableName;
            this.columnName = columnName;
        }

        /**
         * DOC amaumont Couple constructor comment.
         *
         * @param tableName
         * @param columnName
         */
        public TableEntryLocation(TableEntryLocation tableEntryLocation) {
            this.tableName = tableEntryLocation.tableName;
            this.columnName = tableEntryLocation.columnName;
        }

        @Override
		public String toString() {
            return "{tableName=" + this.tableName + ", columnName=" + this.columnName + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.columnName == null) ? 0 : this.columnName.hashCode());
            result = prime * result + ((this.tableName == null) ? 0 : this.tableName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TableEntryLocation other = (TableEntryLocation) obj;
            if (this.columnName == null) {
                if (other.columnName != null) {
                    return false;
                }
            } else if (!this.columnName.equals(other.columnName)) {
                return false;
            }
            if (this.tableName == null) {
                if (other.tableName != null) {
                    return false;
                }
            } else if (!this.tableName.equals(other.tableName)) {
                return false;
            }
            return true;
        }

    }

    public Date getOrder() {
        GregorianCalendar gc = new GregorianCalendar(2008, 2, 17, 12, 0, 0);
        return gc.getTime();
    }
}
