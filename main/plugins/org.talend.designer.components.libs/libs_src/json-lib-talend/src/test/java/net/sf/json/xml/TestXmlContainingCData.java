package net.sf.json.xml;

import net.sf.json.JSON;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TestXmlContainingCData extends XMLTestCase {

   public void testXmlWithTypeAttribute() throws IOException, SAXException, ParserConfigurationException {
      final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<data><nested><![CDATA[/////wAAAAAAAAAA]]></nested></data>";

      final XMLSerializer tested = new XMLSerializer();
      tested.setKeepCData(true);
      tested.setTypeHintsEnabled(false);
      tested.setRootName("data");

      final JSON jsonRepresentation = tested.read( xml );

      final String result = tested.write( jsonRepresentation );

      assertXMLEqual( xml, result );

   }
}
