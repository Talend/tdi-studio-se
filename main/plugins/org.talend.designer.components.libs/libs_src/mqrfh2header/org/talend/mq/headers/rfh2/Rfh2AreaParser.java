package org.talend.mq.headers.rfh2;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Rfh2AreaParser {
    private static class Property {
        public String name;
        public StringBuffer value = new StringBuffer();
        public String dataType;
        public boolean isNull;
}

private static class SaxHandler extends DefaultHandler {
        
        private RFH2Area area;
        private Property currentProperty;
        
        public RFH2Area getArea() {
                return area;
        }
        
        public void startElement(String uri, String localName, String name,
                        Attributes attributes) throws SAXException {
                if(area == null) {
                        // root element
                        if(name.equals("jms")) {
                                area = new JmsArea();
                        } else if(name.equals("mcd")) {
                                area = new McdArea();
                        } else {
                                area = new UsrArea(name);
                        }
                } else {
                        if(currentProperty == null) {
                                currentProperty = new Property();
                                currentProperty.name = name;
                                
                                if("true".equals(attributes.getValue("xsi:nil"))) {
                                        currentProperty.isNull = true;
                                }
                                
                                currentProperty.dataType = attributes.getValue("dt");
                        }
                }
        }

        public void characters(char[] ch, int start, int length)
                        throws SAXException {
                if(currentProperty != null) {
                        // TODO use start and length
                        for(int i = start; i<start+length; i++) {
                                currentProperty.value.append(ch[i]);
                        }
                }
        }

        public void endElement(String uri, String localName, String name)
                        throws SAXException {
                if(currentProperty != null) {
                        String valueAsString = currentProperty.value.toString();
                        Object value;
                        if(currentProperty.isNull) {
                                value = null;
                        } else if(currentProperty.dataType == null) {
                                // String
                                value = valueAsString;
                        } else if("i4".equals(currentProperty.dataType)) {
                                value = Integer.valueOf(valueAsString);
                        } else if("i8".equals(currentProperty.dataType)) {
                                value = Long.valueOf(valueAsString);
                        } else if("i2".equals(currentProperty.dataType)) {
                                value = Short.valueOf(valueAsString);
                        } else if("i1".equals(currentProperty.dataType)) {
                                value = Byte.valueOf(valueAsString);
                        } else if("boolean".equals(currentProperty.dataType)) {
                                if("1".equals(valueAsString)) {
                                        value = Boolean.TRUE;
                                } else {
                                        value = Boolean.FALSE;
                                }
                        } else if("r4".equals(currentProperty.dataType)) {
                                value = Float.valueOf(valueAsString);
                        } else if("r8".equals(currentProperty.dataType)) {
                                value = Double.valueOf(valueAsString);
                        } else {
                                throw new RuntimeException("Unknown data type: " + currentProperty.dataType);
                        }
                        
                        area.setProperty(currentProperty.name, value);
                                
                        currentProperty = null;
                }
        }
}

public RFH2Area parse(String stringToParse) {
        SAXParser parser;
        try {
                parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
                throw new RuntimeException("Failed to create XML parser, can not parse RFH2 areas", e);
        }
        
        SaxHandler handler = new SaxHandler();
        
        try {
                parser.parse(new InputSource(new StringReader(stringToParse)), handler);
        } catch (SAXException e) {
                throw new RuntimeException("Invalid RFH2 header", e);
        } catch (IOException e) {
                throw new RuntimeException("Invalid RFH2 header", e);
        }
        
        return handler.getArea();
}

}
