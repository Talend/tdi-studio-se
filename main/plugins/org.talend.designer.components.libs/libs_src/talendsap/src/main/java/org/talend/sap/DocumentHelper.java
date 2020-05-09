package org.talend.sap;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class DocumentHelper {

	Document doc = null;
	Element root = null;

	Element input = null;
	Element tables = null;
	Element changing = null;

	Element currentStruct = null;
	Element currentTable = null;
	Element currentRow = null;

	public DocumentHelper() {
		doc = org.dom4j.DocumentHelper.createDocument();
	}

	public void setFunctionName(String name) {
		root = doc.addElement(DocumentExtractor.replaceNamespace(name));
	}

	private void correctInput() {
		if (input == null) {
			input = root.addElement("INPUT");
		}
	}

	private void correctTables() {
		if (tables == null) {
			tables = root.addElement("TABLES");
		}
	}

	private void correctChanging() {
		if (changing == null) {
			changing = root.addElement("CHANGING");
		}
	}

	public void addSingleParameter(String name, String value, SAPParameterType parameter_type) {
		if(value == null) {
			value = "";
		}
		if (parameter_type == SAPParameterType.CHANGING) {
			correctChanging();
			changing.addElement(DocumentExtractor.replaceNamespace(name)).setText(value);
		} else {
			correctInput();
			input.addElement(DocumentExtractor.replaceNamespace(name)).setText(value);
		}
	}

	public void addStructParameter(String name, SAPParameterType parameter_type) {
		if (parameter_type == SAPParameterType.CHANGING) {
			correctChanging();
			currentStruct = changing.addElement(DocumentExtractor.replaceNamespace(name));
		} else {
			correctInput();
			currentStruct = input.addElement(DocumentExtractor.replaceNamespace(name));
		}
	}

	public void addStructChildParameter(String name, String value) {
		if(value == null) {
			value = "";
		}
		currentStruct.addElement(DocumentExtractor.replaceNamespace(name)).setText(value);
	}

	public void addTableParameter(String name, SAPParameterType parameter_type) {
		if (parameter_type == SAPParameterType.CHANGING) {
			correctChanging();
			currentTable = changing.addElement(DocumentExtractor.replaceNamespace(name));
		} else if(parameter_type == SAPParameterType.TABLES) {
			correctTables();
			currentTable = tables.addElement(DocumentExtractor.replaceNamespace(name));
		} else {
			correctInput();
			currentTable = input.addElement(DocumentExtractor.replaceNamespace(name));
		}
	}

	public void addTableRow() {
		currentRow = currentTable.addElement("item");
	}

	public void addTableRowChildParameter(String name, String value) {
		if(value == null) {
			value = "";
		}
		currentRow.addElement(DocumentExtractor.replaceNamespace(name)).setText(value);
	}

	public Document getDocument() {
		return doc;
	}

}
