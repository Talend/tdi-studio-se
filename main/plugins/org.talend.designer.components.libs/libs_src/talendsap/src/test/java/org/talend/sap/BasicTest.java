package org.talend.sap;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;

import org.junit.Test;

public class BasicTest {

	@Test
	public void testSAPXMLCoder() {
		assertEquals("_-CMT_-TLND_TABLE", SAPXMLCoder.encode("/CMT/TLND_TABLE"));
		assertEquals("_0CMT0TLND_TABLE", SAPXMLCoder.encode("0CMT0TLND_TABLE"));

		assertEquals("/CMT/TLND_TABLE", SAPXMLCoder.decode(SAPXMLCoder.encode("/CMT/TLND_TABLE")));
		assertEquals("0CMT0TLND_TABLE", SAPXMLCoder.decode(SAPXMLCoder.encode("0CMT0TLND_TABLE")));

		assertEquals("BAPI_MATERIAL_GET_ALL", SAPXMLCoder.decode(SAPXMLCoder.encode("BAPI_MATERIAL_GET_ALL")));
		assertEquals("2STEP_PICK", SAPXMLCoder.decode(SAPXMLCoder.encode("2STEP_PICK")));
	}
	
	@Test
	public void testDocumentHelperAndExtractor() {
		DocumentHelper helper = new DocumentHelper();
		String function = "/CMT/TLND_TABLE";
		helper.setFunctionName(function);

		helper.addSingleParameter("ID", "1", SAPParameterType.CHANGING);
		helper.addSingleParameter("NAME", "gaoyan", SAPParameterType.IMPORT);

		helper.addStructParameter("INFO", SAPParameterType.CHANGING);
		helper.addStructChildParameter("ID", "2");
		helper.addStructChildParameter("NAME", "wangwei");

		helper.addStructParameter("INFO1", SAPParameterType.IMPORT);
		helper.addStructChildParameter("ID1", "4");
		helper.addStructChildParameter("NAME1", "momo");

		helper.addTableParameter("TABLE1", SAPParameterType.TABLES);
		for (int i = 0; i < 10; i++) {
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

		Document doc = helper.getDocument();

		DocumentExtractor ext = new DocumentExtractor(doc,function);
		String single = ext.getSingleResult("ID");
		assertEquals("1", single);
		
		List<String> struct = ext.getStructureResult("INFO", Arrays.asList("ID","NAME"));
		assertEquals(java.util.Arrays.asList("2", "wangwei"), struct);
		
		List<List<String>> table = ext.getTableResult("TABLE1", Arrays.asList("c1","c2","c3","c4","c5","c6","c7","c8"));
		assertEquals(10, table.size());
	}
}
