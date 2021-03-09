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
package org.talend.designer.codegen.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;

/**
 * Use to build camel endpoint.</br>The generated endpoint is different from real endpoint query,It generates java
 * source code expression.</br>for example to build endpoint <code>comp,NAME(expression),PATH(expression)</code> with
 * query <code>{P=V,P2=V2}</code></br> It will generate :</br>
 * <code>"comp:"+NAME+":"+PATH+"?"+P+"="+V+"&"+p2+"="+V2</code></br> instead of :</br>
 * <code style="text-decoration: line-through;">comp:name:path?P=V&P2=V2</code> </br>
 * <p>
 * In javadoc of methods, <b>Direct String</b> means String value, eg: hello (without quotes)</br> <b>Expression
 * String</b> means the source code expression to generate a String value, eg: "a"+context.b+"c" (with quotes)
 * </p>
 *
 * @author GaoZone
 * @since 5.6
 */
public class CamelEndpointBuilder {

    private static final String QUOTED_EMPTY = "\"\""/* "" */;

    private String component;

    private String name;

    private List<String> paths;

    private boolean useDoubleSlash = true;

    private boolean useComponentColon = true;

    /** The params map. k-v both are Expression. */
    private final Map<String, String> paramsMap;

    private List<String> conditionsParam = new ArrayList<String>(1);

    CamelEndpointBuilder() {
        paramsMap = new LinkedHashMap<String, String>();
        paths = new ArrayList<String>();
    }

    /**
     * Provider append wrapped expression to {@link StringBuilder}
     */
    private final static class SBTool {

        private static void appendDirectString(StringBuilder sb, String string) {
            sb.append("+\"").append(string).append('\"');
        }

        /**
         * Append expression to {@link StringBuilder}.
         *
         * @param sb the sb
         * @param expression expression represents source to generate a String
         */
        private static void appendExpression(StringBuilder sb, String expression) {
            sb.append("+").append(expression);
        }

        public static void mergeConstantStrings(StringBuilder sb) {
            String str = "\"+\"";
            int index = sb.indexOf(str);
            while (index > 0) {
                if (index > 1 && sb.charAt(index - 1) == '\\') {
                    // handle \"+" case.
                    index = index + str.length();
                } else {
                    // we can merge "...abc"+"def..." to "...abcdef..."
                    sb.delete(index, index + str.length());
                }
                index = sb.indexOf(str, index);
            }
        }

        public static String escapeJavaString(String text) {
            return '"' + StringEscapeUtils.escapeJava(text) + '"';
        }
    }

    /**
     * Some appender may need to override {@link #finish()}.
     */
    private class ParamAppender {

        public CamelEndpointBuilder finish() {
            return CamelEndpointBuilder.this;
        }
    }

    /**
     * Add a param that value is build from a Appendable style.</br> {@link #finish()} must be called at last.
     * <code>.append(xx).append(xx).finish()</code>
     */
    public final class BuildingValueParamAppender extends ParamAppender {

        private final String key;

        private final StringBuilder valueBuilder;

        private BuildingValueParamAppender(String key) {
            this.key = key;
            valueBuilder = new StringBuilder();
        }

        @Override
        public CamelEndpointBuilder finish() {
            if (valueBuilder.length() == 0) {
                return super.finish();
            }
            valueBuilder.deleteCharAt(0);
            // remove the first appended +, because generated exp will be +"dir"+E2+E3
            return addParam(key, valueBuilder.toString());
        }

        public BuildingValueParamAppender appendDirectString(String directString) {
            SBTool.appendDirectString(valueBuilder, directString);
            return this;
        }

        public BuildingValueParamAppender appendExpression(String expression) {
            SBTool.appendExpression(valueBuilder, expression);
            return this;
        }

    }

    public final class NodeParamNotDefaultAppender extends ParamAppender {

        private boolean visibleRequired;

        private INode node;

        private NodeParamNotDefaultAppender(INode node, boolean visibleRequired) {
            this.visibleRequired = visibleRequired;
            this.node = node;
        }

        public NodeParamNotDefaultAppender visibleRequired(boolean isRequired) {
            visibleRequired = isRequired;
            return this;
        }

        /**
         * Adds the param.
         *
         * @param key the param name
         * @param nodeParamKey the node param key, <code>__XXX__</code> style.
         * @param defaultValue the default value, will skip add if value equals default.
         * @return the node param not default appender
         */
        public NodeParamNotDefaultAppender addParam(String key, String nodeParamKey, String defaultValue) {
            if (node == null) {
                return this;
            }
            IElementParameter param = node.getElementParameter(nodeParamKey);
            if (param == null) {
                return this;
            }
            if (visibleRequired && !param.isShow(node.getElementParametersWithChildrens())) {
                return this;
            }
            String value = ElementParameterParser.getStringElementParameterValue(param);
            addParamInConditions(key, value, false, defaultValue);
            return this;
        }

        public NodeParamNotDefaultAppender addParam(String key, String nodeParamKey) {
            return addParam(key, nodeParamKey, null);
        }

        @SuppressWarnings("unchecked")
        public NodeParamNotDefaultAppender addListMapParams(String nodeParamKey) {
            Collection<Map<String, String>> tableValues = (List<Map<String, String>>) ElementParameterParser.getObjectValue(node,
                    nodeParamKey);
            addParams(tableValues);
            return this;
        }
    }

    public final class ConditionParamAppender extends ParamAppender {

        private final String condition;

        private final Map<String, String> paramsMapWhenTrue;

        private final Map<String, String> paramsMapWhenFalse;

        public ConditionParamAppender(String conditionExp) {
            condition = conditionExp;
            paramsMapWhenTrue = new LinkedHashMap<String, String>();
            paramsMapWhenFalse = new LinkedHashMap<String, String>();
        }

        @Override
        public CamelEndpointBuilder finish() {
            StringBuilder sb = new StringBuilder();
            sb.append("+(");
            sb.append(condition);
            sb.append("?");
            appendMap(sb, paramsMapWhenTrue);
            sb.append(":");
            appendMap(sb, paramsMapWhenFalse);
            sb.append(")");
            conditionsParam.add(sb.toString());
            return super.finish();
        }

        private final void appendMap(StringBuilder sb, Map<String, String> map) {
            if (map.isEmpty()) {
                sb.append(QUOTED_EMPTY);
            } else {
                sb.append("(").append(QUOTED_EMPTY);
                boolean first = true;
                for (Entry<String, String> e : map.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        SBTool.appendDirectString(sb, "&");
                    }
                    SBTool.appendExpression(sb, e.getKey());
                    SBTool.appendDirectString(sb, "=");
                    SBTool.appendExpression(sb, e.getValue());
                }
                sb.append(")");
            }
        }

        public ConditionParamAppender appendParamWhenTrue(String key, String valueExp) {
            return appendParam(true, key, valueExp);
        }

        public ConditionParamAppender appendParamWhenFalse(String key, String valueExp) {
            return appendParam(false, key, valueExp);
        }

        private ConditionParamAppender appendParam(boolean whenTrue, String key, String valueExp) {
            Map<String, String> map = whenTrue ? paramsMapWhenTrue : paramsMapWhenFalse;
            map.put('"' + key + '"', valueExp);
            return this;
        }
    }

    /**
     * Gets the builder.
     *
     * @return the builder
     */
    public static CamelEndpointBuilder getBuilder() {
        return new CamelEndpointBuilder();
    }

    public String build() {
        StringBuilder sb = new StringBuilder();

        sb.append('\"').append(component);

        if (useComponentColon) {
            sb.append(":");
        }

        if (useDoubleSlash) {
            sb.append("//");
        }
        sb.append('\"');
        // "comp:"
        if (name != null) {
            SBTool.appendExpression(sb, name);
            // "comp:"+name
            // SBTool.appendDirectString(sb, ":");
            // no trailing : after name.
        }
        // "component:"+name"
        SBTool.mergeConstantStrings(sb);
        sb.append(buildPath()).append(buildParamQuery());
        // comp+":"+name+":"+path+"?p="+v+"&p2="+v2
        if (!conditionsParam.isEmpty()) {
            for (String string : conditionsParam) {
                sb.append(string);
            }
        }
        return sb.toString();
    }

    /**
     * Builds the path.
     *
     * @return the char sequence, eg: P1+"/"+P2+"/"+P3
     */
    private CharSequence buildPath() {
        if (paths.isEmpty()) {
            return "";
        }
        return "+" + StringUtils.join(paths, "+\"/\"+"/* "/" */);
    }

    /**
     * Builds the param query.
     *
     * @return the char sequence, eg: +"?p="+v+"&p2="+v2
     */
    private CharSequence buildParamQuery() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String> entry : paramsMap.entrySet()) {
            StringBuilder paramSB = new StringBuilder();
            SBTool.appendDirectString(paramSB, "&");
            SBTool.appendExpression(paramSB, entry.getKey());
            SBTool.appendDirectString(paramSB, "=");
            SBTool.appendExpression(paramSB, entry.getValue());
            SBTool.mergeConstantStrings(paramSB);
            sb.append(paramSB);
        }
        if (sb.length() > 0) {
            // fix first & to ?.
            sb.setCharAt(sb.indexOf("&"), '?');
        }
        return sb;
    }

    public CamelEndpointBuilder addParamInConditions(String key, String valueExp, boolean skipWhenKeyExist,
            String skipWhenValueIsThis) {
        key = StringUtils.trimToNull(key);
        if (key == null) {
            return this;
        }
        String keyExp = '"' + key + '"';// quoted key, forming to expression.
        return addExpressionParamInConditions(keyExp, valueExp, skipWhenKeyExist, skipWhenValueIsThis);
    }

    /**
     * Adds the or update param.
     *
     * @param key the key , <b>Direct String</b>
     * @param value source expression to build value. <b>Expression String</b>
     * @return the camel endpoint builder
     */
    public CamelEndpointBuilder addParam(String key, String value) {
        return addParamInConditions(key, value, false, null);
    }

    public CamelEndpointBuilder addExpressionParamInConditions(String keyExp, String valueExp, boolean skipWhenKeyExist,
            String skipWhenValueIsThis) {
        keyExp = StringUtils.trimToNull(keyExp);
        if (keyExp == null || keyExp.equals(QUOTED_EMPTY)) {
            return this;
        }
        valueExp = StringUtils.trimToNull(valueExp);
        if (valueExp == null || valueExp.equals(QUOTED_EMPTY)) {
            return this;
        }
        if (skipWhenKeyExist && paramsMap.containsKey(keyExp)) {
            return this;
        }
        if (valueExp.equals(skipWhenValueIsThis)) {
            return this;
        }
        paramsMap.put(keyExp, valueExp);
        return this;
    }

    public CamelEndpointBuilder addParamIfNotDefault(String key, String value, String defaultValue) {
        return addParamInConditions(key, value, false, defaultValue);
    }

    public CamelEndpointBuilder addParamIfNotExist(String key, String value) {
        return addParamInConditions(key, value, true, null);
    }

    public CamelEndpointBuilder addParams(Collection<Map<String, String>> tableValues) {
        if (tableValues == null) {
            return this;
        }
        for (Map<String, String> map : tableValues) {
            addExpressionParamInConditions(map.get("NAME"), map.get("VALUE"), false, null);
        }
        return this;
    }

    /**
     * Escape flat text value to java string literal
     *
     * @param key
     * @param value
     * @return
     */
    public CamelEndpointBuilder addParamsWithEscapeValue(String key, String value) {
        value = value.replaceAll("\\r|\\n", "");
        return addParam(key, SBTool.escapeJavaString(value));
    }

    /**
     * Sets the component.
     *
     * @param component the component name, <b>Direct String</b>.
     * @return
     */
    public CamelEndpointBuilder setComponent(String component) {
        this.component = component;
        return this;
    }

    /**
     * Sets the name.
     *
     * @param name the name, <b>Expression String</b>
     * @return
     */
    public CamelEndpointBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     *
     * @param useDoubleSlash if set to {@code false} (default = true) result endpoint will start with
     * {@code componentName:Category[?options]} <br/>
     * @return
     */
    public CamelEndpointBuilder useDoubleSlash(boolean useDoubleSlash) {
        this.useDoubleSlash = useDoubleSlash;
        return this;
    }

    /**
     *
     * @param useComponentColon if set to {@code false} (default = true) result endpoint will start with
     * {@code componentNameCategory[?options]} <br/>
     * @return
     */
    public CamelEndpointBuilder useComponentColon(boolean useComponentColon) {
        this.useComponentColon = useComponentColon;
        return this;
    }

    public CamelEndpointBuilder setDirectName(String directName) {
        this.name = '\"' + directName + '\"';
        return this;
    }

    public CamelEndpointBuilder appendPath(String pathExpression) {
        paths.add(pathExpression);
        return this;
    }

    public NodeParamNotDefaultAppender getNodeParamNotDefaultAppender(INode node) {
        return new NodeParamNotDefaultAppender(node, false);
    }

    public BuildingValueParamAppender getBuildingValueParamAppender(String paramName) {
        return new BuildingValueParamAppender(paramName);
    }

    public ConditionParamAppender getConditionParamAppender(String conditionExp) {
        return new ConditionParamAppender(conditionExp);
    }

}
