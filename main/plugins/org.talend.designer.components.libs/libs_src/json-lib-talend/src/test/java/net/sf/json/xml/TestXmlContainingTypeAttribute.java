package net.sf.json.xml;

import net.sf.json.JSON;
import org.custommonkey.xmlunit.XMLTestCase;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TestXmlContainingTypeAttribute extends XMLTestCase {

   public void testXmlWithTypeAttribute() throws IOException, SAXException, ParserConfigurationException {
      final String xml = "<data type=\"someType\"><nested type=\"some_other_type\">value</nested></data>";

      final XMLSerializer tested = new XMLSerializer();
      tested.setTypeHintsEnabled( false );
      tested.setTypeHintsCompatibility( false );
      tested.setRootName("data");

      final JSON jsonRepresentation = tested.read( xml );

      final String result = tested.write( jsonRepresentation );

      assertXMLEqual( xml, result );

   }
}
