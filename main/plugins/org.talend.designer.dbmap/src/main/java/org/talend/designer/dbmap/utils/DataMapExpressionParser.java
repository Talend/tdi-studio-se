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
package org.talend.designer.dbmap.utils;

import java.util.ArrayList;
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
import org.talend.commons.utils.data.text.StringHelper;
import org.talend.designer.dbmap.language.IDbLanguage;
import org.talend.designer.dbmap.model.tableentry.TableEntryLocation;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id: DataMapExpressionParser.java 1782 2007-02-03 07:57:38Z bqian $
 *
 */
public class DataMapExpressionParser {

    private static String EXPRESSION = "\\s*(\\w+)\\s*";

    private static String SEPARATOR = "\\.";

    private static String COLUMN_PATTERN = SEPARATOR + EXPRESSION;

    private static String TABLE_PATTERN = EXPRESSION + SEPARATOR;

    private static final String GLOBALMAP_PATTERN = "\\s*(\\(\\s*\\(\\s*String\\s*\\)\\s*globalMap\\s*\\.\\s*get\\s*\\(\\s*\\\"(.+?)\\\"\\s*\\)\\s*\\))\\s*";

    private static final String GLOBALMAP_PATTERN2 = "\\s*(\\(\\s*\\(\\s*[a-zA-Z]+\\s*\\)\\s*globalMap\\s*\\.\\s*get\\s*\\(\\s*\\\"(.+?)\\\"\\s*\\)\\s*\\))\\s*";

    private static final String GLOBALMAP_PATTERN_ALL = "\\s*\\+\\s*(\\(\\w*\\))?globalMap.get\\s*\\(\\s*\\\"(.+?)\\\"\\s*\\)\\s*\\+\\s*";
    
    private static final String GLOBALMAP_TABLE_EXPRESSION = "(" + GLOBALMAP_PATTERN + "\\." + GLOBALMAP_PATTERN + ")|("
            + TABLE_PATTERN + GLOBALMAP_PATTERN + ")|(" + GLOBALMAP_PATTERN + COLUMN_PATTERN + ")|" + GLOBALMAP_PATTERN;;

    // ((String)globalMap.get("schema")).((String)globalMap.get("tableName")).columnName
    private final static String GLOBALMAP_EXPRESSION1 = "(" + GLOBALMAP_PATTERN + "\\." + GLOBALMAP_PATTERN + COLUMN_PATTERN
            + ")";

    // schema.((String)globalMap.get("tableName")).columnName
    private final static String GLOBALMAP_EXPRESSION2 = "(" + TABLE_PATTERN + GLOBALMAP_PATTERN + COLUMN_PATTERN + ")";

    // ((String)globalMap.get("schema")).tableName.columnName
    private final static String GLOBALMAP_EXPRESSION3 = "(" + GLOBALMAP_PATTERN + COLUMN_PATTERN + COLUMN_PATTERN + ")";

    // ((String)globalMap.get("tableName")).columnName
    private final static String GLOBALMAP_EXPRESSION4 = "(" + GLOBALMAP_PATTERN + COLUMN_PATTERN + ")";

    private final static String GLOBALMAP_EXPRESSION5 = "\\s*(\\s*(\\w+)\\s*\\.\\\\\"\\s*(.+?)\\s*\\\\\")\\s*";

    private final static String GLOBALMAP_EXPRESSION = GLOBALMAP_EXPRESSION1 + "|" + GLOBALMAP_EXPRESSION2 + "|"//$NON-NLS-1$//$NON-NLS-2$
            + GLOBALMAP_EXPRESSION3 + "|" + GLOBALMAP_EXPRESSION4 + "|" + GLOBALMAP_EXPRESSION5;//$NON-NLS-1$

    private final static String EXPRESSION_PATTERN = "(\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*)" //$NON-NLS-1$
            + "|(\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*)" //$NON-NLS-1$
            + "|(\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*)" //$NON-NLS-1$
            + "|(\\s*(\\w+)\\s*\\.\\s*(\\w+)\\s*)" + "|" + GLOBALMAP_EXPRESSION;//$NON-NLS-1$//$NON-NLS-2$

    private final String DOT_STR = "."; //$NON-NLS-1$

    private Perl5Matcher matcher = new Perl5Matcher();

    private Perl5Compiler compiler = new Perl5Compiler();

    private Pattern pattern;

    private PatternMatcherInput patternMatcherInput;

    private String locationPattern;

    private IDbLanguage language;

    public DataMapExpressionParser(IDbLanguage language) {
        super();
        this.language = language;
        setLocationPattern(language.getLocationPattern());
    }

    /**
     * DOC amaumont Comment method "setRegexpPattern".
     *
     * @param regexpPattern
     */
    private void setLocationPattern(String locationPattern) {
        this.locationPattern = locationPattern;
    }

    public TableEntryLocation[] parseTableEntryLocations(String expression) {
        Set<TableEntryLocation> resultList = new HashSet<TableEntryLocation>();
        if (expression != null) {
            matcher.setMultiline(true);
            if (patternMatcherInput == null) {
                patternMatcherInput = new PatternMatcherInput(expression);
            } else {
                patternMatcherInput.setInput(expression);
            }
            recompilePatternIfNecessary(EXPRESSION_PATTERN);
            while (matcher.contains(patternMatcherInput, pattern)) {
                MatchResult matchResult = matcher.getMatch();
                TableEntryLocation location = null;
                if (matchResult.group(1) != null) {
                    // context.schema.context.table.comlumn
                    location = new TableEntryLocation(matchResult.group(2) + DOT_STR + matchResult.group(3) + DOT_STR
                            + matchResult.group(4) + DOT_STR + matchResult.group(5), matchResult.group(6));
                } else if (matchResult.group(7) != null) {
                    // context.schema.table.comlumn | schema.context.table.comlumn
                    location = new TableEntryLocation(matchResult.group(8) + DOT_STR + matchResult.group(9) + DOT_STR
                            + matchResult.group(10), matchResult.group(11));
                } else if (matchResult.group(12) != null) {
                    // schema.table.column
                    location = new TableEntryLocation(matchResult.group(13) + DOT_STR + matchResult.group(14),
                            matchResult.group(15));
                } else if (matchResult.group(16) != null) {
                    // table.column
                    location = new TableEntryLocation(matchResult.group(17), matchResult.group(18));
                } else if (matchResult.group(19) != null) {
                    // ((String)globalMap.get("schema")).((String)globalMap.get("tableName")).columnName
                    location = new TableEntryLocation(matchResult.group(20) + DOT_STR + matchResult.group(22),
                            matchResult.group(24));
                } else if (matchResult.group(25) != null) {
                    // schema.((String)globalMap.get("tableName")).columnName
                    location = new TableEntryLocation(matchResult.group(26) + DOT_STR + matchResult.group(27),
                            matchResult.group(29));
                } else if (matchResult.group(30) != null) {
                    // ((String)globalMap.get("schema")).tableName.columnName
                    location = new TableEntryLocation(matchResult.group(31) + DOT_STR + matchResult.group(33),
                            matchResult.group(34));
                } else if (matchResult.group(35) != null) {
                    // ((String)globalMap.get("tableName")).columnName
                    location = new TableEntryLocation(matchResult.group(36), matchResult.group(38));
                }else if (matchResult.group(39) != null) {
                 // table.\"column\"
                    // Case NVL(Keys.\"ORG_SCD_ID\",-99) When -99 Then 'N' Else 'Y' End
                    location = new TableEntryLocation(matchResult.group(40), matchResult.group(41));
                }
                if (location != null) {
                    resultList.add(location);
                }
            }
        }
        return resultList.toArray(new TableEntryLocation[resultList.size()]);
    }

    private Pattern recompilePatternIfNecessary(String regexpPattern) {
        if (pattern == null || !regexpPattern.equals(pattern.getPattern())) {
            try {
                pattern = compiler.compile(regexpPattern);
            } catch (MalformedPatternException e) {
                throw new RuntimeException(e);
            }
        }
        return pattern;
    }

    public String addTablePrefixToColumnName(String expression, TableEntryLocation[] locations) {
        String returnedExpression = expression;
        for (TableEntryLocation location : locations) {
            recompilePatternIfNecessary(StringHelper.replacePrms(language.getSubstPatternForPrefixColumnName(), new Object[] {
                    location.tableName, location.columnName }));
            if (returnedExpression != null) {
                matcher.setMultiline(true);
                Perl5Substitution substitution = new Perl5Substitution(
                        language.getPrefixTableRegexp() + "$1" //$NON-NLS-1$
                                + language.getPrefixFieldRegexp() + "$1__$2" + language.getSuffixFieldRegexp(), Perl5Substitution.INTERPOLATE_ALL); //$NON-NLS-1$
                returnedExpression = Util.substitute(matcher, pattern, substitution, returnedExpression, Util.SUBSTITUTE_ALL);
            }
        }
        return returnedExpression;
    }

    public String replaceLocation(String expression, TableEntryLocation oldLocation, TableEntryLocation newLocation) {
        String returnedExpression = expression;
        String oldTName = oldLocation.tableName;
        if (oldTName.matches(GLOBALMAP_TABLE_EXPRESSION)) {
            oldTName = oldTName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("\\\"", "\\\\\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        }
        String tempPattern = StringHelper.replacePrms(language.getSubstPatternForReplaceLocation(), new Object[] { oldTName,
                oldLocation.columnName });
        recompilePatternIfNecessary(tempPattern);
        if (returnedExpression != null) {
            matcher.setMultiline(true);
            Perl5Substitution substitution = new Perl5Substitution(language.getPrefixTableRegexp()
                    + "$1" + newLocation.tableName + "$2" //$NON-NLS-1$ //$NON-NLS-2$
                    + language.getSuffixTableRegexp()
                    + "$3" + language.getPrefixFieldRegexp() + "$4" + newLocation.columnName + "$5" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + language.getSuffixFieldRegexp(), Perl5Substitution.INTERPOLATE_ALL);
            returnedExpression = Util.substitute(matcher, pattern, substitution, returnedExpression, Util.SUBSTITUTE_ALL);
        }
        return returnedExpression;
    }

    public Set<String> getGlobalMapSet(String sqlQuery) {
        Set<String> resultList = new HashSet<String>();
        if (sqlQuery != null) {
            matcher.setMultiline(true);
            if (patternMatcherInput == null) {
                patternMatcherInput = new PatternMatcherInput(sqlQuery);
            } else {
                patternMatcherInput.setInput(sqlQuery);
            }
            recompilePatternIfNecessary(GLOBALMAP_PATTERN2);
            while (matcher.contains(patternMatcherInput, pattern)) {
                MatchResult matchResult = matcher.getMatch();
                if (matchResult.group(1) != null) {
                    String matchGroup = matchResult.group(1);
                    resultList.add(matchGroup);
                }
            }
        }
        return resultList;
    }
    
    public boolean isContainsGlobalMapExpression(String sqlQuery) {
        if (sqlQuery != null) {
            recompilePatternIfNecessary(GLOBALMAP_PATTERN_ALL);
            while (matcher.contains(sqlQuery, pattern)) {
                return true;
            }
            recompilePatternIfNecessary(GLOBALMAP_PATTERN);
            while (matcher.contains(sqlQuery, pattern)) {
                return true;
            }
        }
        return false;
    }

    public String getGlobalMapExpressionRegex(String expression) {
        String[] specialChars = new String[] { "\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|" };
        String[] specialCharsRegex = new String[] { "\\\\", "\\$", "\\(", "\\)", "\\*", "\\+", "\\.", "\\[", "\\]", "\\?", "\\^",
                "\\{", "\\}", "\\|" };
        String regexExpression = expression;
        for (int i = 0; i < specialChars.length; i++) {
            int indexOf = regexExpression.indexOf(specialChars[i]);
            if (indexOf != -1) {
                regexExpression = regexExpression.replaceAll(specialCharsRegex[i], "\\\\" + specialCharsRegex[i]);
            }
        }

        return regexExpression;
    }

    public String getGlobalMapReplacement(String expression) {
        String[] specialChars = new String[] { "\\", "$" };
        String[] specialCharsRegex = new String[] { "\\\\", "\\$" };
        String[] specialCharsReplacement = new String[] { "\\\\\\\\", "\\\\\\$" };
        String replacement = expression;
        for (int i = 0; i < specialChars.length; i++) {
            int indexOf = replacement.indexOf(specialChars[i]);
            if (indexOf != -1) {
                replacement = replacement.replaceAll(specialCharsRegex[i], specialCharsReplacement[i]);
            }
        }

        return replacement;
    }

    public boolean isComplexValue(String value) {
        List<Integer> quoteLocations = getQuoteLocations(value, 0);
        for (int i = 1; i < quoteLocations.size(); i++) {
            if (i + 1 < quoteLocations.size()) {
                Integer start = quoteLocations.get(i);
                Integer end = quoteLocations.get(i + 1);
                int indexOf = value.substring(start, end).indexOf("+");
                if (indexOf != -1) {
                    return true;
                }
                i = i + 2;
            }
        }
        return false;
    }

    private List<Integer> getQuoteLocations(String text, int index) {
        List<Integer> locations = new ArrayList<Integer>();
        if (index > text.length()) {
            return locations;
        }
        int indexOf = text.indexOf("\"", index);
        if (indexOf != -1) {
            boolean isQuote = true;
            if (indexOf > 0) {
                char slash = '\\';
                char charAt = text.charAt(indexOf - 1);
                if (charAt == slash) {
                    isQuote = false;
                }
            }
            if (isQuote) {
                locations.add(indexOf);
                indexOf = indexOf + 1;
            }
            if (indexOf < text.length()) {
                locations.addAll(getQuoteLocations(text, indexOf));
            }

        }
        return locations;
    }

}
