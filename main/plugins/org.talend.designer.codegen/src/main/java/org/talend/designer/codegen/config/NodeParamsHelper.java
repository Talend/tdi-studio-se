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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.talend.core.model.process.ElementParameterParser;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.INode;

/**
 * Use for wrap a {@link INode} to provide faster and convenient methods to get
 * parameters.
 *
 * @author GaoZone
 * @update 2013-9-12 [TESB-11028]
 */
public class NodeParamsHelper {

    /** The node use to wrap. */
    private final INode node;

    /** The parameter map use to cache params by name. */
    private final HashMap<String, IElementParameter> parameterMap;

    /**
     * Use for method return property(K-V) entries in list/map.
     */
    private class PropEntry implements Map.Entry<String, String> {

        /** The key. */
        private final String key;

        /** The value. */
        private final String value;

        /**
         * Instantiates a new prop entry.
         *
         * @param key
         *            the key
         * @param value
         *            the value
         */
        public PropEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Instantiates a new node params helper.
     *
     * @param node
     *            the node
     */
    public NodeParamsHelper(INode node) {
        this.node = node;

        // cache ParameterMap
        parameterMap = new HashMap<String, IElementParameter>();
        for (IElementParameter parameter : node.getElementParametersWithChildrens()) {
            parameterMap.put(parameter.getVariableName(), parameter);
        }
    }

    /**
     * Gets the parameter from cached name map.
     *
     * @param key
     *            the key
     * @return the parameter
     */
    private IElementParameter getParameter(String key) {
        return parameterMap.get(key);
    }

    /**
     * Gets the boolean parameter by {@code key}. If no {@code key} provided
     *
     * @param key
     *            the key
     * @return the boolean parameter
     */
    public boolean getBoolParam(String key) {
        return "true".equals(getStringParam(key));
    }

    /**
     * Gets the string parameter by {@code key}.
     *
     * @param key
     *            the key
     * @return the parameter value in String
     */
    public String getStringParam(String key) {
        // return ElementParameterParser.getValue(node, key);
        try {
            IElementParameter param = getParameter(key);
            return getDisplayValue(param);
        } catch (Throwable e) {
            // ignore and return "";
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Gets string parameter by {@code key}. Especially, if the parameter's
     * controller is not visible, then also return {@code ""}.
     *
     * @param key
     *            the key
     * @return the parameter value
     */
    public String getVisibleStringParam(String key) {
        String value = getStringParam(key);
        if (!("").equals(value)) {
            IElementParameter param = getParameter(key);
            if (param.isShow(node.getElementParametersWithChildrens())) {
                return value;
            }
        }
        return "";
    }

    /**
     * Gets the object param. </br>//TODO Not used name map yet, and be improve
     * by expose methods from {@link ElementParameterParser}
     *
     * @param <T>
     *            the generic type
     * @param key
     *            the key
     * @return the object param
     */
    @SuppressWarnings("unchecked")
    public <T> T getObjectParam(String key) {
        return (T) ElementParameterParser.getObjectValue(node, key);
    }


    /**
     * Gets boolean parameter by {@code key}. Especially, if the parameter's
     * controller is not visible, then also return {@code false}.
     *
     * @param key
     *            the key
     * @return the parameter value
     */
    public boolean getVisibleBoolParam(final String key) {
        if (key == null) {
            return false;
        }
        if (getBoolParam(key)) {
            // value is true, then check visible
            // no need null check, has true value must not null.
            IElementParameter param = getParameter(key);
            return param.isShow(node.getElementParametersWithChildrens());
        }
        return false;
    }

    /**
     * Gets the properties pram.
     *
     * @param key
     *            the key
     * @return the properties pram
     */
    public List<Entry<String, String>> getPropertiesPram(String key) {
        try {
            List<Map<String, String>> paramValue = getObjectParam(key);
            List<Entry<String, String>> properties = new ArrayList<Entry<String, String>>(paramValue.size());
            for (Map<String, String> map : paramValue) {
                properties.add(new PropEntry(map.get("PROP_NAME"), map.get("PROP_VALUE")));
            }
            return properties;
        } catch (Throwable e) {
            // ignore and return empty list.
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
	 * Gets the display value.
	 *
	 * @param param
	 *            the param
	 * @return the display value
	 *         {@code ElementParameterParser.getDisplayValue(final IElementParameter param)}
	 */
    private static String getDisplayValue(final IElementParameter param) {
		if (param == null) {
			return "";
		}
		return ElementParameterParser.getStringElementParameterValue(param);
    }

}
