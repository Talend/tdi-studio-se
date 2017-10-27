// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.hl7.ui.header;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.properties.ByteArray;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.designer.hl7.managers.HL7Manager;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

/**
 * DOC gcui class global comment. Detailled comment. parse HL7 file.
 */
public class HL7Parse {

    /**
     * DOC gcui Comment method "doParse".
     * 
     * @param filePath
     * @return
     */
    public List<Message> doParse(String filePath, String startChar, String endChar) {
        List<Message> messageList = new ArrayList<Message>();
        String filePathNoQuotes = TalendTextUtils.removeQuotes(filePath);
        File file = Path.fromOSString(filePathNoQuotes).toFile();
        if (file.exists()) {
            ByteArray array = PropertiesFactory.eINSTANCE.createByteArray();
            try {
                array.setInnerContentFromFile(file);
                TalendHL7Reader talendHL7Reader = new TalendHL7Reader(new java.io.FileInputStream(file), "ISO-8859-15");
                if (startChar != null) {
                    talendHL7Reader.setStartMsgChar(stringParse2Char(startChar));
                }
                if (endChar != null) {
                    talendHL7Reader.setEndMsgChar(stringParse2Char(endChar));
                }
                String HL7InputTem = null;
                String messageText = "";

                while ((HL7InputTem = talendHL7Reader.getMessage()) != null) {
                    Message message = getHL7MessageInput(HL7InputTem);
                    messageList.add(message);
                    messageText = messageText + HL7InputTem + "\r";
                }
                if (messageText == null || "".equals(messageText)) {
                    messageText = new String(array.getInnerContent());
                }
                talendHL7Reader.close();
            } catch (IOException ex) {
                ExceptionHandler.process(ex);
            }
        } else {
            MessageBox message = new MessageBox(new Shell(), SWT.APPLICATION_MODAL | SWT.OK);
            message.setText("The file is not exist"); //$NON-NLS-1$
            message.setMessage("Please check the file path and select the file again"); //$NON-NLS-1$
            if (message.open() == SWT.OK) {
                message.getParent().getShell().close();
            }
        }
        return messageList;
    }

    /**
     * DOC gcui Comment method "doParse".
     * 
     * @param msgList
     * @return
     */
    public List<Message> doParse(List<String> msgList) {
        List<Message> messageList = new ArrayList<Message>();
        for (int i = 0; i < msgList.size(); i++) {
            Message message = getHL7MessageInput(msgList.get(i));
            messageList.add(message);
        }
        return messageList;
    }

    /**
     * DOC gcui Comment method "doParse".
     * 
     * @param filePath
     * @param hl7Manager
     * @param msgContentList
     * @return
     */
    public List<Message> doParse(String filePath, HL7Manager hl7Manager, List<String> msgContentList, String startChar,
            String endChar) {
        List<Message> messageList = new ArrayList<Message>();
        String filePathNoQuotes = TalendTextUtils.removeQuotes(filePath);
        File file = Path.fromOSString(filePathNoQuotes).toFile();
        if (file.exists()) {
            msgContentList.clear();
            ByteArray array = PropertiesFactory.eINSTANCE.createByteArray();
            try {
                array.setInnerContentFromFile(file);
                TalendHL7Reader talendHL7Reader = new TalendHL7Reader(new java.io.FileInputStream(file), "ISO-8859-15");
                String HL7InputTem = null;
                if (startChar != null) {
                    talendHL7Reader.setStartMsgChar(stringParse2Char(startChar));
                }
                if (endChar != null) {
                    talendHL7Reader.setEndMsgChar(stringParse2Char(endChar));
                }

                while ((HL7InputTem = talendHL7Reader.getMessage()) != null) {
                    msgContentList.add(HL7InputTem);
                    Message message = getHL7MessageInput(HL7InputTem);
                    messageList.add(message);
                }
                if (msgContentList == null || msgContentList.size() == 0) {
                    String msgText = new String(array.getInnerContent());
                    msgContentList.add(msgText);
                    Message message = getHL7MessageInput(msgText);
                    messageList.add(message);

                }
                hl7Manager.getUiManager().getHl7UI().getContentProvider().getAllSegmentsForMessage().clear();
                hl7Manager.getUiManager().getHl7UI().getMessageViewer().setInput(messageList.toArray());
                hl7Manager.getUiManager().getHl7UI().getHeader().setMsgIsChange(true);
                hl7Manager.getUiManager().getHl7UI().initSchemaCombo(null);
                hl7Manager.getUiManager().getHl7UI().getMetadataEditor().removeAll();
                hl7Manager.getUiManager().getHl7UI().redrawLinkers();
            } catch (IOException ex) {
                ExceptionHandler.process(ex);
            }
        }
        return messageList;
    }

    public List<Message> doParse(String filePath, TreeViewer messageViewerStep1, List<String> msgContentList, String startChar,
            String endChar) {
        List<Message> messageList = new ArrayList<Message>();
        String filePathNoQuotes = TalendTextUtils.removeQuotes(filePath);
        File file = Path.fromOSString(filePathNoQuotes).toFile();
        if (file.exists()) {
            msgContentList.clear();
            ByteArray array = PropertiesFactory.eINSTANCE.createByteArray();
            try {
                array.setInnerContentFromFile(file);
                TalendHL7Reader talendHL7Reader = new TalendHL7Reader(new java.io.FileInputStream(file), "ISO-8859-15"); //$NON-NLS-1$
                String hl7InputTem = null;
                if (startChar != null) {
                    talendHL7Reader.setStartMsgChar(stringParse2Char(startChar));
                }
                if (endChar != null) {
                    talendHL7Reader.setEndMsgChar(stringParse2Char(endChar));
                }

                while ((hl7InputTem = talendHL7Reader.getMessage()) != null) {
                    msgContentList.add(hl7InputTem);
                    Message message = getHL7MessageInput(hl7InputTem);
                    messageList.add(message);
                }
                if (msgContentList == null || msgContentList.size() == 0) {
                    String msgText = new String(array.getInnerContent());
                    msgContentList.add(msgText);
                    Message message = getHL7MessageInput(msgText);
                    messageList.add(message);
                }
                messageViewerStep1.setInput(messageList.toArray());
            } catch (IOException ex) {
                ExceptionHandler.process(ex);
            }
        }
        return messageList;
    }

    /**
     * DOC gcui Comment method "getHL7MessageInput".
     * 
     * @param messageText
     * @return
     */
    public Message getHL7MessageInput(String messageText) {
        GenericParser p = new GenericParser();
        p.setValidationContext(new NoValidation()); // force use novalidation

        Message message = null;
        try {
            if (messageText != null) {
                messageText = messageText.replace('\n', '\r');
                message = p.parse(messageText);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
            MessageBox errorBox = new MessageBox(new Shell(), SWT.APPLICATION_MODAL | SWT.OK);
            errorBox.setText("Parse error"); //$NON-NLS-1$
            errorBox.setMessage("The content can't be parsed correctly,please check the file"); //$NON-NLS-1$
            if (errorBox.open() == SWT.OK) {
                errorBox.getParent().getShell().close();
            }
        }
        return message;
    }

    public char stringParse2Char(String string) {
        char c = '\u000b';
        String s = TalendTextUtils.removeQuotes(string, "'");
        if (s.startsWith("\\u")) {
            s = s.substring(2);
            if (s.length() == 4) {
                try {
                    c = (char) Integer.parseInt(s, 16);
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }
        } else if (s.length() == 1) {
            c = s.charAt(0);
        }
        return c;
    }

}
