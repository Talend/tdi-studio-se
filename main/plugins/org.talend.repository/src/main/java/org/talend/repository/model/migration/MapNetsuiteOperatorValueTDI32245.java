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

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

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
import org.talend.designer.core.model.utils.emf.talendfile.ElementParameterType;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.impl.ElementValueTypeImpl;

/**
 * DOC lwang class global comment. Detailled comment
 */
public class MapNetsuiteOperatorValueTDI32245 extends AbstractJobMigrationTask {

	public MapNetsuiteOperatorValueTDI32245() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ExecutionResult execute(Item item) {
		ProcessType processType = getProcessType(item);

		if (getProject().getLanguage() != ECodeLanguage.JAVA
				|| processType == null) {
			return ExecutionResult.NOTHING_TO_DO;
		}

		IComponentConversion changeDeletEmptyFileValue = new IComponentConversion() {
			public void transform(NodeType node) {

				Map<String, String> operatorMap = new HashMap<String, String>();

				operatorMap.put("contains", "S-contains");
				operatorMap.put("doesNotContain", "S-doesNotContain");
				operatorMap.put("doesNotStartWith", "S-doesNotStartWith");
				operatorMap.put("emptyS", "S-empty");
				operatorMap.put("hasKeywords", "S-hasKeywords");
				operatorMap.put("is", "S-is");
				operatorMap.put("isNot", "S-isNot");
				operatorMap.put("notEmptyS", "S-notEmpty");
				operatorMap.put("startsWith", "S-startsWith");
				operatorMap.put("between", "N-between");
				operatorMap.put("notBetween", "N-notBetween");
				operatorMap.put("emptyN", "N-empty");
				operatorMap.put("equalTo", "N-equalTo");
				operatorMap.put("greaterThan", "N-greaterThan");
				operatorMap.put("greaterThanOrEqualTo", "N-greaterThanOrEqualTo");
				operatorMap.put("lessThan", "N-lessThan");
				operatorMap.put("lessThanOrEqualTo", "N-lessThanOrEqualTo");
				operatorMap.put("notEmptyN", "N-notEmpty");
				operatorMap.put("notEqualTo", "N-notEqualTo");
				operatorMap.put("notGreaterThan", "N-notGreaterThan");
				operatorMap.put("notGreaterThanOrEqualTo", "N-notGreaterThanOrEqualTo");
				operatorMap.put("notLessThan", "N-notLessThan");
				operatorMap.put("notLessThanOrEqualTo", "N-notLessThanOrEqualTo");
				operatorMap.put("anyOf", "L-anyOf");
				operatorMap.put("noneOf", "L-noneOf");
				operatorMap.put("after", "D-after");
				operatorMap.put("before", "D-before");
				operatorMap.put("emptyD", "D-empty");
				operatorMap.put("notAfter", "D-notAfter");
				operatorMap.put("notBefore", "D-notBefore");
				operatorMap.put("notEmptyD", "D-notEmpty");
				operatorMap.put("notOn", "D-notOn");
				operatorMap.put("notOnOrAfter", "D-notOnOrAfter");
				operatorMap.put("notOnOrBefore", "D-notOnOrBefore");
				operatorMap.put("notWithin", "D-notWithin");
				operatorMap.put("on", "D-on");
				operatorMap.put("onOrAfter", "D-onOrAfter");
				operatorMap.put("onOrBefore", "D-onOrBefore");
				operatorMap.put("within", "D-within");
				operatorMap.put("boolean", "B-boolean");

				ElementParameterType criteriaTable = ComponentUtilities.getNodeProperty(node, "CONDITIONS");

				if(criteriaTable != null){
					for(Object o : criteriaTable.getElementValue()){
						ElementValueTypeImpl el = (ElementValueTypeImpl)o;
						if(el.getElementRef().equals("OPERATOR")){
							String oldValue = el.getValue();
							String newValue = operatorMap.get(oldValue);

							el.setValue(newValue);
						}
					}
				}

			}
		};

		IComponentFilter filter = new NameComponentFilter("tNetsuiteInput"); //$NON-NLS-1$
		try {
			ModifyComponentsAction
					.searchAndModify(
							item,
							processType,
							filter,
							Arrays.<IComponentConversion> asList(changeDeletEmptyFileValue));
		} catch (PersistenceException e) {
			ExceptionHandler.process(e);
			return ExecutionResult.FAILURE;
		}

		return ExecutionResult.SUCCESS_WITH_ALERT;
	}

	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2015, 05, 25, 12, 0, 0);
		return gc.getTime();
	}

}
