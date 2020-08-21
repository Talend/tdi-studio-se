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
package org.talend.designer.core.generic.context;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.runtime.evaluator.AbstractPropertyValueEvaluator;
import org.talend.core.runtime.util.GenericTypeUtils;
import org.talend.daikon.properties.property.Property;

/**
 * created by ycbai on 2016年2月6日 Detailled comment
 *
 */
public class ComponentContextPropertyValueEvaluator extends AbstractPropertyValueEvaluator {

    private INode node;

    public ComponentContextPropertyValueEvaluator(INode node) {
        this.node = node;
    }

    @Override
    public Object evaluate(Property property, Object storedValue) {
        if (storedValue == null) {
            if (GenericTypeUtils.isBooleanType(property)) {
                return false;
            }
            return storedValue;
        }
        if (storedValue instanceof Schema || storedValue instanceof Enum || storedValue instanceof Boolean) {
            return storedValue;
        }
        IContext context = null;
        if (node != null) {
            IProcess process = node.getProcess();
            if (process != null) {
                IContextManager cm = process.getContextManager();
                if (cm != null) {
                    context = cm.getDefaultContext();
                }
            }
        }
        String stringStoredValue = String.valueOf(storedValue);
        if (context != null && ContextParameterUtils.isContainContextParam(stringStoredValue)) {
            if (storedValue instanceof List) {
                boolean isDrivers = property.getName() != null && property.getName().equals("drivers");
                List newList = new ArrayList<>();
                for(Object v : (List)storedValue){
                	String vString = String.valueOf(v);
                    if (ContextParameterUtils.isContainContextParam(vString) && isDrivers) {
                        newList.addAll(ContextParameterUtils.parseScriptContextCodeList(storedValue, context, isDrivers));
                	}else{
                		newList.add(v);
                	}
                }
                return getTypedValue(property, newList);
            } else {
                // the simple convert which only can process the simple case like : context.var or
                // context.getProperty(context.getProperty("a")) or the link case : context.var1 = context.var2, context.var2 =
                // "value", then get context.var1 = "value"
                String simpleConvertResult = ContextParameterUtils.parseScriptContextCode(stringStoredValue, context);
                // if the simple convert can't process the var which is more complex like : "str1" + context.var1 + "str2"+
                // context.var2 + "str3", we will use the converter below
                // will not consider the old usage context.getProperty("") and the link case
                if (ContextParameterUtils.isContainContextParam(simpleConvertResult)) {
                    stringStoredValue = ContextParameterUtils.convertContext2Literal4AnyVar(stringStoredValue, context);
                } else {
                    stringStoredValue = simpleConvertResult;
                }
            }
        } else if (storedValue instanceof List) {
            return storedValue;
        }
        return getTypedValue(property, storedValue, stringStoredValue);
    }

}
