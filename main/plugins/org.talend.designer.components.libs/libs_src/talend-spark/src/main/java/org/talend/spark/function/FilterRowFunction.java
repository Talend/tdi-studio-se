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
package org.talend.spark.function;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.api.java.function.Function;
import org.talend.spark.utils.FilterObject;
import org.talend.spark.utils.Utils;

public class FilterRowFunction implements Function<List<Object>, Boolean> {
	private static final long serialVersionUID = 1L;
	private List<FilterObject> filters;

	public FilterRowFunction(List<FilterObject> list) {
		this.filters = list;
	}

	public Boolean call(List<Object> row) throws Exception {
		Boolean result = true;
		if (filters == null || filters.isEmpty()) {
			return result;
		}
		for (int i = 0; i < filters.size(); i++) {
			FilterObject filter = filters.get(i);
			boolean tempResult = true;
			if (row.get(filter.idCol) == null && !filter.isNullable) {
				throw new Exception(
						"Null value found in a non-nullable column.");
			}
			switch (filter.op) {
			case EQUAL:
				tempResult = (Utils.compareTo(row.get(filter.idCol),
						filter.value, filter.javaType) == 0);
				break;
			case NEQUAL:
				tempResult = (Utils.compareTo(row.get(filter.idCol),
						filter.value, filter.javaType) != 0);
				break;
			case GREATER:
				tempResult = (Utils.compareTo(row.get(filter.idCol),
						filter.value, filter.javaType) > 0);
				break;
			case GREATER_EQUAL:
				tempResult = (Utils.compareTo(row.get(filter.idCol),
						filter.value, filter.javaType) >= 0);
				break;
			case LESS:
				tempResult = (Utils.compareTo(row.get(filter.idCol),
						filter.value, filter.javaType) < 0);
				break;
			case LESS_EQUAL:
				tempResult = (Utils.compareTo(row.get(filter.idCol),
						filter.value, filter.javaType) <= 0);
				break;
			case STARTS_WITH:
				tempResult = ((String) row.get(filter.idCol))
						.startsWith(filter.value.toString());
				break;
			case ENDS_WITH:
				tempResult = ((String) row.get(filter.idCol))
						.endsWith(filter.value.toString());
				break;
			case CONTAINS:
				tempResult = ((String) row.get(filter.idCol))
						.contains(filter.value.toString());
				break;
			case MATCHES:
				Pattern pattern = Pattern.compile(filter.value.toString());
				Matcher matcher = pattern.matcher((String) row.get(filter.idCol));
				tempResult = matcher.matches();
				break;
			}
			if (i == 0) {
				result = tempResult;
			} else {
				if (filter.logicOp == FilterObject.LogicOp.OR) {
					result = result || tempResult;
				} else {
					result = result && tempResult;
				}
			}

		}
		return result;
	}
}
