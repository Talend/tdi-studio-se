package net.sf.json.xml;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

public class TestElementShouldNotBeArray extends TestCase {

   public void test_element_should_not_be_mistaken_as_array() {
      final XMLSerializer xmlSerializer = new XMLSerializer();
      xmlSerializer.setKeepCData(true);
      JSONObject actual = (JSONObject) xmlSerializer.read( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<Document DOMVersion=\"8.0\" Self=\"d\">" +
            "<TinDocumentDataObject>\n" +
            "<Properties>\n" +
            "<GaijiRefMaps><![CDATA[/////wAAAAAAAAAA]]></GaijiRefMaps>\n" +
            "</Properties>\n" +
            "</TinDocumentDataObject>\n" +
            "</Document>\n" );

      final JSONObject expected = JSONObject.fromObject( "{@DOMVersion:\"8.0\", @Self:\"d\", TinDocumentDataObject:{" +
            "Properties:{" +
            "GaijiRefMaps:\"<![CDATA[/////wAAAAAAAAAA]]>\"} } }" );

      assertEquals(expected, actual);
   }
}
