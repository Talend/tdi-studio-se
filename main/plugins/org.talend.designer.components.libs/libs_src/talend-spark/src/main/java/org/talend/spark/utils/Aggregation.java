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
package org.talend.spark.utils;

import java.io.Serializable;

public class Aggregation implements Serializable {

	private static final long serialVersionUID = 1L;
	private int outputColumn;
	private Class<?> outputColumnClass;
	private int inputColumn;
	private Class<?> inputColumnClass;
	private Operation operation;

	public Aggregation(int outputColumn, String outputColumnClass,
			int inputColumn, String inputColumnClass, Operation operation) throws ClassNotFoundException {
		this.outputColumn = outputColumn;
		this.outputColumnClass = Class.forName("java.lang." + outputColumnClass);
		this.inputColumn = inputColumn;
		this.inputColumnClass = Class.forName("java.lang." + inputColumnClass);
		this.operation = operation;
	}

	public int getOutputColumn() {
		return outputColumn;
	}

	public void setOutputColumn(int outputColumn) {
		this.outputColumn = outputColumn;
	}

	public int getInputColumn() {
		return inputColumn;
	}

	public void setInputColumn(int inputColumn) {
		this.inputColumn = inputColumn;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public Class<?> getInputColumnClass() {
		return inputColumnClass;
	}

	public void setInputColumnClass(Class<?> inputColumnClass) {
		this.inputColumnClass = inputColumnClass;
	}

	public Class<?> getOutputColumnClass() {
		return outputColumnClass;
	}

	public void setOutputColumnClass(Class<?> outputColumnClass) {
		this.outputColumnClass = outputColumnClass;
	}
}
