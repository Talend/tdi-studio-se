package org.talend.designer.oas.external;

public enum EESBMediaType {

    XML_OR_JSON("XML-JSON"),
    XML("XML"),
    JSON("JSON"),
    HTML("HTML"),
    FORM("FORM"),
    MULTIPART("MULTIPART"),
    ANY("ANY");

    private String label;

    /**
     * DOC dsergent MediaTypes constructor comment.
     * 
     * @param label
     */
    private EESBMediaType(String label) {
        this.label = label;
    }

    /**
     * Getter for label.
     * 
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    public static EESBMediaType fromString(String text) {
        for (EESBMediaType mt : EESBMediaType.values()) {
            if (mt.label.equalsIgnoreCase(text)) {
                return mt;
            }
        }
        return null;
    }

}
