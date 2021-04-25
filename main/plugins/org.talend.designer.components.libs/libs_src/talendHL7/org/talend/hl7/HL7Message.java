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
package org.talend.hl7;

import java.util.ArrayList;
import java.util.List;


public class HL7Message {

    private List<Segment> segments;

    private String delimiter = "|";

    private String encoding = "^~\\&";

    private String version = "";

    public HL7Message() {
        segments = new ArrayList<Segment>();
    }

    public void addSegment(Segment seg) {
        if (seg == null) {
            return;
        }
        segments.add(seg);
    }

    public String message() throws Exception {
        char fieldSep = '|';
        char repSep = '~';
        char compSep = '^';
        char subCompSep = '&';
        StringBuffer message = new StringBuffer();
        for (int currSeg = 0; currSeg < segments.size(); currSeg++) {
            Segment seg = segments.get(currSeg);
            message.append(seg.getSegmentType()); // append the segment header like MSH

            if (currSeg == 0) {
                if (!"MSH".equals(seg.getSegmentType())) {
                    throw new Exception("MSH should be the first input connector");
                }

            }
            if (currSeg == 0 && "MSH".equals(seg.getSegmentType())) {
                // get the delimiter
                Field deliField = seg.getField(1, 0, 1, 1); // MSH-1: field Separator
                if (deliField == null) {
                    deliField = new Field(1);
                    seg.addField(deliField);
                }
                if (!"".equals(deliField.getValue())) {
                    this.delimiter = deliField.getValue();
                } else {
                    deliField.setValue(this.delimiter);
                }

                // get the delimiter
                Field encodingField = seg.getField(2, 0, 1, 1); // MSH-2: encoding Separator
                if (encodingField == null) {
                    encodingField = new Field(2);
                    seg.addField(encodingField);
                }
                if (!"".equals(encodingField.getValue())) {
                    this.encoding = encodingField.getValue();
                } else {
                    encodingField.setValue(this.encoding);
                }

                // set the custom version
                Field versionField = seg.getField(12, 0, 1, 1);
                if (versionField == null) {
                    versionField = new Field(12, 0, 1, 1);
                    seg.addField(versionField);
                }
                if ("".equals(versionField.getValue())) {
                    versionField.setValue(this.version);
                }

                // get the separators
                fieldSep = this.delimiter.charAt(0);
                compSep = this.encoding.charAt(0);
                repSep = this.encoding.charAt(1);
                subCompSep = this.encoding.charAt(3);
            }

            // generate the segment field text
            Field preField = new Field(0, 1, 0, 0);
            for (int curNField = 0; curNField < seg.getFields().size(); curNField++) {
                Field currField = seg.getFields().get(curNField);
                if (curNField == 0 && "MSH".equals(seg.getSegmentType())) {
                    // MSH-1
                } else {
                    // different field
                    if (preField.field != currField.field) {
                        for (int i = 0; i < currField.field - preField.field; i++) {
                            message.append(fieldSep);
                        }
                    } else if (preField.rep != currField.rep) { // have rep
                        for (int i = 0; i < currField.rep - preField.rep; i++) {
                            message.append(repSep);
                        }
                    } else if (preField.component != currField.component) { // have component
                        for (int i = 0; i < currField.component - preField.component; i++) {
                            message.append(compSep);
                        }
                    } else if (preField.subComponent != currField.subComponent) { // have subComponent
                        for (int i = 0; i < currField.subComponent - preField.subComponent; i++) {
                            message.append(subCompSep);
                        }
                    }
                    message.append(currField.getValue());

                }
                preField = currField;
            }

            message.append("\r");
            // System.out.println(message);
        }
        return message.toString();
    }

    private int accurateLoops(Field preField, Field currField) {

        int numLoop = 0;

        return numLoop;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        this.segments = segments;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
