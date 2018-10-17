package org.talend.designer.oas.external;
/**
 * MediaTypes *\/* application/xml text/xml application/json
 */
public enum EParamKind {

    PATH("path"),
    QUERY("query"),
    FORM("form"),
    HEADER("header"),
    MATRIX("matrix"),
    MULTIPART("multipart")
    
    ;

    private String label;

    /**
     * DOC dsergent MediaTypes constructor comment.
     * 
     * @param label
     */
    private EParamKind(String label) {
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

    public static EParamKind fromString(String text) {
        for (EParamKind mt : EParamKind.values()) {
            if (mt.label.equalsIgnoreCase(text)) {
                return mt;
            }
        }
        return null;
    }

}
