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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.api.java.function.Function;
import org.talend.spark.utils.Aggregation;
import org.talend.spark.utils.Operation;
import org.talend.spark.utils.Utils;

public class AggregateFunction implements
		Function<Iterable<List<Object>>, List<Object>> {

	private static final long serialVersionUID = 1L;
	List<Aggregation> operationList;

	public AggregateFunction(List<Aggregation> agg) {
		this.operationList = agg;
	}

	public List<Object> call(Iterable<List<Object>> values) throws Exception {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < this.operationList.size(); i++) {
			list.add(null);
		}

		int count = -1;
		int index = 0;

		for (Aggregation operation : operationList) {
			if (operation.getOperation() == Operation.COUNT) {
				count = count(values);
				list.set(index++, count);
			} else if (operation.getOperation() == Operation.PASS_THROUGH) {
				Object data = passThrough(values, operation.getInputColumn());
				list.set(index++, data);
			} else if (operation.getOperation() == Operation.MIN) {
				Object min = min(values, operation.getInputColumn(),
						operation.getInputColumnClass(),
						operation.getOutputColumnClass());
				list.set(index++, min);
			} else if (operation.getOperation() == Operation.MAX) {
				Object max = max(values, operation.getInputColumn(),
						operation.getInputColumnClass(),
						operation.getOutputColumnClass());
				list.set(index++, max);
			} else if (operation.getOperation() == Operation.SUM) {
				Object sum = sum(values, operation.getInputColumn(),
						operation.getInputColumnClass(),
						operation.getOutputColumnClass());
				list.set(index++, sum);
			} else if (operation.getOperation() == Operation.AVG) {
				if (count == -1) {
					count = count(values);
				}
				Object sum = sum(values, operation.getInputColumn(),
						operation.getInputColumnClass(),
						operation.getOutputColumnClass());
				String outputClassName = operation.getOutputColumnClass()
						.getName();
				if (outputClassName.equals("java.lang.Integer")
						|| outputClassName.equals("java.lang.Long")
						|| outputClassName.equals("java.lang.Short")) {
					list.set(index++, (Long) sum / count);
				} else {
					list.set(index++, (Double) sum / count);
				}
			}
		}

		return list;
	}

	private Object passThrough(Iterable<List<Object>> values, int index) {
		Iterator<List<Object>> valuesIterator = values.iterator();
		Object data = null;
		if (valuesIterator.hasNext())
			data = valuesIterator.next().get(index);
		return data;
	}

	public int count(Iterable<List<Object>> values) {
		Iterator<List<Object>> valuesIterator = values.iterator();
		int count = 0;
		while (valuesIterator.hasNext()) {
			valuesIterator.next();
			count++;
		}
		return count;
	}

	public Object min(Iterable<List<Object>> values, int index,
			Class<?> inputColumnClass, Class<?> outputColumnClass)
			throws InstantiationException, IllegalAccessException,
			SecurityException, NoSuchMethodException, IllegalArgumentException,
			InvocationTargetException {
		Iterator<List<Object>> valuesIterator = values.iterator();
		Object min = null;

		while (valuesIterator.hasNext()) {
			Object data = valuesIterator.next().get(index);
			if (min == null || Utils.compareTo(min, data, outputColumnClass) > 0)
				min = data;
		}
		return min;
	}

	private Object max(Iterable<List<Object>> values, int index,
			Class<?> inputColumnClass, Class<?> outputColumnClass)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Iterator<List<Object>> valuesIterator = values.iterator();
		Object max = null;

		while (valuesIterator.hasNext()) {
			Object data = valuesIterator.next().get(index);
			if (max == null || Utils.compareTo(max, data, outputColumnClass) < 0)
				max = data;
		}
		return max;
	}

	private Object sum(Iterable<List<Object>> values, int index,
			Class<?> inputColumnClass, Class<?> outputColumnClass)
			throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException,
			SecurityException, NoSuchMethodException {
		Iterator<List<Object>> valuesIterator = values.iterator();
		Constructor<?> outputConstructor = outputColumnClass
				.getConstructor(String.class);
		Constructor<?> inputConstructor = inputColumnClass
				.getConstructor(String.class);

		String outputClassName = outputColumnClass.getName();

		Object sum = outputConstructor.newInstance("0");

		while (valuesIterator.hasNext()) {
			if (outputClassName.equals("java.lang.Integer")
					|| outputClassName.equals("java.lang.Long")
					|| outputClassName.equals("java.lang.Short")) {
				sum = ((Number) sum).longValue()
						+ ((Number) inputConstructor.newInstance(valuesIterator
								.next().get(index).toString())).longValue();
			} else {
				sum = ((Number) sum).doubleValue()
						+ ((Number) inputConstructor.newInstance(valuesIterator
								.next().get(index).toString())).doubleValue();
			}
		}

		return outputConstructor.newInstance(sum.toString());
	}
}
