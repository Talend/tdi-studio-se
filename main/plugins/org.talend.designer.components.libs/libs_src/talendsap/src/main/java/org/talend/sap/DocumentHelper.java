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
		root = doc.addElement(name);
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
			changing.addElement(name).setText(value);
		} else {
			correctInput();
			input.addElement(name).setText(value);
		}
	}

	public void addStructParameter(String name, SAPParameterType parameter_type) {
		if (parameter_type == SAPParameterType.CHANGING) {
			correctChanging();
			currentStruct = changing.addElement(name);
		} else {
			correctInput();
			currentStruct = input.addElement(name);
		}
	}

	public void addStructChildParameter(String name, String value) {
		if(value == null) {
			value = "";
		}
		currentStruct.addElement(name).setText(value);
	}

	public void addTableParameter(String name, SAPParameterType parameter_type) {
		if (parameter_type == SAPParameterType.CHANGING) {
			correctChanging();
			currentTable = changing.addElement(name);
		} else if(parameter_type == SAPParameterType.TABLES) {
			correctTables();
			currentTable = tables.addElement(name);
		} else {
			correctInput();
			currentTable = input.addElement(name);
		}
	}

	public void addTableRow() {
		currentRow = currentTable.addElement("item");
	}

	public void addTableRowChildParameter(String name, String value) {
		if(value == null) {
			value = "";
		}
		currentRow.addElement(name).setText(value);
	}

	public Document getDocument() {
		return doc;
	}

	public static void main(String[] args) throws IOException {
		DocumentHelper helper = new DocumentHelper();
		helper.setFunctionName("READ_TABLE_FUNCTION");

		helper.addSingleParameter("ID", "1", SAPParameterType.CHANGING);
		helper.addSingleParameter("NAME", "gaoyan", SAPParameterType.IMPORT);

		helper.addStructParameter("INFO", SAPParameterType.CHANGING);
		helper.addStructChildParameter("ID", "2");
		helper.addStructChildParameter("NAME", "wangwei");

		helper.addStructParameter("INFO1", SAPParameterType.IMPORT);
		helper.addStructChildParameter("ID1", "4");
		helper.addStructChildParameter("NAME1", "momo");

		helper.addTableParameter("TABLE1", SAPParameterType.TABLES);
		for (int i = 0; i < 200000; i++) {
			helper.addTableRow();
			helper.addTableRowChildParameter("c1", i + "");
			helper.addTableRowChildParameter("c2", "wangwei" + i);
			helper.addTableRowChildParameter("c3", "wangwei" + i);
			helper.addTableRowChildParameter("c4", "wangwei" + i);
			helper.addTableRowChildParameter("c5", "wangwei" + i);
			helper.addTableRowChildParameter("c6", "wangwei" + i);
			helper.addTableRowChildParameter("c7", "wangwei" + i);
			helper.addTableRowChildParameter("c8", "wangwei" + i);
		}

		helper.addTableParameter("TABLE2", SAPParameterType.TABLES);
		for (int i = 0; i < 2; i++) {
			helper.addTableRow();
			helper.addTableRowChildParameter("ID4", i + "");
			helper.addTableRowChildParameter("NAME4", "gaoyan" + i);
		}

//		StringWriter sw = new StringWriter();
//		OutputFormat format = OutputFormat.createPrettyPrint();
//		XMLWriter writer = new XMLWriter(sw, format);
		Document doc = helper.getDocument();
//		writer.write(doc);
//		System.out.println(sw.toString());
		
		DocumentExtractor ext = new DocumentExtractor(doc,"READ_TABLE_FUNCTION");
		String single = ext.getSingleResult("ID");
		System.out.println(single);
		
		List<String> struct = ext.getStructureResult("INFO", Arrays.asList("ID","NAME"));
		System.out.println(struct);
		
		List<List<String>> table = ext.getTableResult("TABLE1", Arrays.asList("c1","c2","c3","c4","c5","c6","c7","c8"));
		for(List<String> row : table) {
			System.out.println(row);
		}

	}
}
