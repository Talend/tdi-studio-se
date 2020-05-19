package org.talend.sap;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

public class DocumentExtractor {
	
	Document doc = null;
	final String finalFunction;
	StringBuilder sb = new StringBuilder();
	
	public DocumentExtractor(Document doc,String function) {
		this.doc = doc;
		this.finalFunction = SAPXMLCoder.encode(function);
	}
	
	public String getSingleResult(String name) {
	    final String finalName = SAPXMLCoder.encode(name);
	    
		XPath xpath = org.dom4j.DocumentHelper.createXPath(
			sb.append("/").append(finalFunction).append("/OUTPUT/").append(finalName)
			.append("|")
			.append("/").append(finalFunction).append("/CHANGING/").append(finalName)
			.toString()
		);
		sb.setLength(0);
		Node node = xpath.selectSingleNode(doc);
		if(node == null) {
			return null;
		}
		return node.getText();
	}
	
	public List<String> getStructureResult(String structureName,List<String> names) {
	    final String finalStructureName = SAPXMLCoder.encode(structureName);
	    
		XPath xpath = org.dom4j.DocumentHelper.createXPath(
			sb.append("/").append(finalFunction).append("/OUTPUT/").append(finalStructureName)
			.append("|")
			.append("/").append(finalFunction).append("/CHANGING/").append(finalStructureName)
			.toString()
		);
		sb.setLength(0);
		Node node = xpath.selectSingleNode(doc);
		
		if(node == null) {
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		
		for(String name : names) {
		    final String finalName = SAPXMLCoder.encode(name);
			Node subNode = node.selectSingleNode(finalName);
			if(subNode == null) {
				result.add(null);
			} else {
				result.add(subNode.getText());
			}
		}
		
		return result;
	}
	
	public List<List<String>> getTableResult(String tableName,List<String> names) {
	    final String finalTableName = SAPXMLCoder.encode(tableName);
	    
		List<List<String>> result = new ArrayList<List<String>>();
		
		Element functionElement = doc.getRootElement();
		if (functionElement == null) {
			return result;
		}

		List<Element> tablesAndChangingElements = new ArrayList<Element>(3);
		tablesAndChangingElements.add(functionElement.element("TABLES"));
		tablesAndChangingElements.add(functionElement.element("CHANGING"));
		tablesAndChangingElements.add(functionElement.element("OUTPUT"));

		for(Element tablesOrChangingElement : tablesAndChangingElements) {
			if (tablesOrChangingElement == null) {
				continue;
			}
			
			Element tableElement = tablesOrChangingElement.element(finalTableName);
		
			if (tableElement == null) {
				continue;
			}
			
			List<Element> elements = tableElement.elements("item");
			
			if(elements == null) {
				continue;
			}
			
			for(Element element : elements) {
				List<String> row = new ArrayList<String>();
				for(String name : names) {
				    final String finalName = SAPXMLCoder.encode(name);
					Element subElement = element.element(finalName);
					if(subElement == null) {
						row.add(null);
					} else {
						row.add(subElement.getText());
					}
				}
				result.add(row);
			}
			
			if(!result.isEmpty()) {
				return result;
			}
		}
		
		return result;
	}
}
