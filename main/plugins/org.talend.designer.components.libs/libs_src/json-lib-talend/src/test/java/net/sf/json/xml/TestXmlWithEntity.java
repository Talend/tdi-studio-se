package net.sf.json.xml;

import net.sf.json.JSON;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TestXmlWithEntity extends XMLTestCase {
   public void test_convert_xml_with_entity() throws IOException, SAXException, ParserConfigurationException {
      String fixture = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<Description>" +
            "<Thumbnails>" +
            "<image>/&#xA;9j/4AAQSkZJRgABAgEASABIAAD/7QAsUGhvdG9zaG9wIDMuMAA4QklNA+0AAAAAABAASAAAAAEA&#xA;AQBIAAAAAQAB/</image>" +
            "</Thumbnails>" +
            "</Description>";

      XMLSerializer xmlSerializer = new XMLSerializer();
      xmlSerializer.setEscapeLowerChars(true);
      xmlSerializer.setRootName("Description");
      xmlSerializer.setTypeHintsEnabled(false);
      final JSON json = xmlSerializer.read( fixture );

      final String result = xmlSerializer.write( json );

      assertXMLEqual(fixture, result);
   }
}
