// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

import java.util.List;

import org.apache.avro.Schema;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.utils.ContextParameterUtils;
import org.talend.core.runtime.evaluator.AbstractPropertyValueEvaluator;
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
            return storedValue;
        }
        if (storedValue instanceof Schema || storedValue instanceof List || storedValue instanceof Enum
                || storedValue instanceof Boolean) {
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
            stringStoredValue = ContextParameterUtils.parseScriptContextCode(stringStoredValue, context);
        }
        return getTypedValue(property, stringStoredValue);
    }

}
