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
package org.talend.designer.core.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.talend.core.model.process.EParameterFieldType;
import org.talend.core.model.process.IElement;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.model.components.ElementParameter;
import org.talend.designer.core.model.components.Expression;

/**
 * created by hcyi on Jun 13, 2017
 * Detailled comment
 *
 */
public final class UpdateParameterUtils {

    /**
     * Reset the values to default only for combo boxes, if the values set are not valid.<br>
     * This avoids to have some invalid setup after drag&drop / setup a component from repository.
     *
     * @param currentParam Current parameter that has been modified in the interface
     * @param testedParam Tested parameter, to know if there is a link for the default values between this parameter and
     * the current.
     */
    public static void setDefaultValues(IElementParameter testedParam, IElement referenceNode) {
        List<? extends IElementParameter> elementParameters = referenceNode.getElementParameters();
        if (elementParameters == null) {
            return;
        }
        // if not a combo box or if this parameter is linked to repository, just skip
        if (!testedParam.getFieldType().equals(EParameterFieldType.CLOSED_LIST) || testedParam.getRepositoryValue() != null) {
            return;
        }
        boolean contains = false;

        for (IElementParameter currentParam : elementParameters) {
                // check if current parameter for combo is valid for new parameters value
                // if not, it will try to choose another value from combo box list.
                boolean isCurrentComboValid = true;
                if (testedParam.getListItemsShowIf() != null || testedParam.getListItemsNotShowIf() != null) {
                    String value = (String) testedParam.getValue();
                    int index = ArrayUtils.indexOf(testedParam.getListItemsValue(), value);
                    // TUP-671:if find this testParam's value,just do show if
                    if (index != -1) {
                        if (testedParam.getListItemsShowIf() != null) {
                            String conditionShowIf = testedParam.getListItemsShowIf()[index];
                            if (conditionShowIf != null) {
                                isCurrentComboValid = Expression.evaluate(conditionShowIf, elementParameters);
                            }
                        }
                        if (testedParam.getListItemsNotShowIf() != null) {
                            String conditionNotShowIf = testedParam.getListItemsNotShowIf()[index];
                            if (conditionNotShowIf != null) {
                                isCurrentComboValid = !Expression.evaluate(conditionNotShowIf, elementParameters);
                            }
                        }
                    }
                }
                if (!isCurrentComboValid && testedParam.getListItemsShowIf() != null) {
                    for (String condition : testedParam.getListItemsShowIf()) {
                        if (condition != null && condition.contains(currentParam.getName())) {
                            boolean isValid = Expression.evaluate(condition, elementParameters);
                            if (isValid) {
                                int index = ArrayUtils.indexOf(testedParam.getListItemsShowIf(), condition);
                                testedParam.setValue(testedParam.getListItemsValue()[index]);
                            isCurrentComboValid = true;
                                break;
                            }
                        }
                    }
                }
                if (!isCurrentComboValid && !contains && testedParam.getListItemsNotShowIf() != null) {
                    for (String condition : testedParam.getListItemsNotShowIf()) {
                        if (condition != null && condition.contains(currentParam.getName())) {
                            boolean isValid = !Expression.evaluate(condition, elementParameters);
                            if (isValid) {
                                int index = ArrayUtils.indexOf(testedParam.getListItemsNotShowIf(), condition);
                                testedParam.setValue(testedParam.getListItemsValue()[index]);
                                break;
                            }
                        }
                    }
                }
        }
    }

    public static void deepCopy(IElementParameter from, IElementParameter to) {
        Set<IElementParameter> copiedList = new HashSet<IElementParameter>();
        deepCopy(from, to, copiedList);
    }

    public static void deepCopy(IElementParameter from, IElementParameter to, Set<IElementParameter> copiedList) {
        if (copiedList.contains(from)) {
            return;
        } else {
            copiedList.add(from);
        }
        to.setValue(from.getValue());
        Map<String, IElementParameter> fromChildParamMap = from.getChildParameters();
        if (fromChildParamMap != null && !fromChildParamMap.isEmpty()) {
            Map<String, IElementParameter> toChildParamMap = to.getChildParameters();
            for (Map.Entry<String, IElementParameter> fromEntry : fromChildParamMap.entrySet()) {
                String key = fromEntry.getKey();
                IElementParameter fromValue = fromEntry.getValue();
                if (fromValue == null) {
                    toChildParamMap.put(key, null);
                } else {
                    IElementParameter toValue = toChildParamMap.get(key);
                    if (toValue == null) {
                        toValue = new ElementParameter(to.getElement());
                        toChildParamMap.put(key, toValue);
                    }
                    deepCopy(fromValue, toValue, copiedList);
                }
            }
        }
    }

}
