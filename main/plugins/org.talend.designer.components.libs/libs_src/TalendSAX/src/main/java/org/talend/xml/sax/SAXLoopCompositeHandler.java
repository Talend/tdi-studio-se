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
package org.talend.xml.sax;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

/**
 * DOC s class global comment. Detailled comment
 */
public class SAXLoopCompositeHandler extends DefaultHandler2 {

    private List<DefaultHandler2> handlerList = new ArrayList<DefaultHandler2>();

    public void register(DefaultHandler2 handler) {
        handlerList.add(handler);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.helpers.DefaultHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        for (DefaultHandler handler : handlerList) {
            handler.startDocument();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.helpers.DefaultHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        for (DefaultHandler handler : handlerList) {
            handler.endDocument();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     * org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        for (DefaultHandler handler : handlerList) {
            handler.startElement(uri, localName, qName, attributes);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    public void characters(char ch[], int start, int length) throws SAXException {
        for (DefaultHandler handler : handlerList) {
            handler.characters(ch, start, length);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        for (DefaultHandler handler : handlerList) {
            handler.endElement(uri, localName, qName);
        }
    }

    public void startCDATA() throws SAXException {
        for (DefaultHandler2 handler : handlerList) {
            handler.startCDATA();
        }
    }

    public void endCDATA() throws SAXException {
        for (DefaultHandler2 handler : handlerList) {
            handler.endCDATA();
        }
    }
}
