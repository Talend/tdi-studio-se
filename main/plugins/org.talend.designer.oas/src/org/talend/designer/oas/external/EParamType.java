package org.talend.designer.oas.external;

/**
 * MediaTypes *\/* application/xml text/xml application/json
 */
public enum EParamType {

    BOOLEAN("boolean | Boolean", Boolean.class),
    BYTE("byte | Byte", Byte.class),
    // BYTE_ARRAY("byte[]"),
    CHARACTER("char | Character", Character.class),
    DATE("Date", java.util.Date.class),
    DOUBLE("double | Double", Double.class),
    FLOAT("float | Float", Float.class),
    BIG_DECIMAL("BigDecimal", java.math.BigDecimal.class),
    INTEGER("int | Integer", Integer.class),
    LONG("long | Long", Long.class),
    OBJECT("Object", Object.class),
    SHORT("short | Short", Short.class),
    STRING("String", String.class),
    // LIST("List"),
    // DYNAMIC("Dynamic"),
    // DOCUMENT("Document")
    ;

    private String label;

    private String talendTypeId;

    /**
     * DOC dsergent MediaTypes constructor comment.
     * 
     * @param label
     */
    private EParamType(String label, Class<?> nullableClass) {
        this.label = label;
        this.talendTypeId = "id_" + nullableClass.getSimpleName();

    }

    /**
     * Getter for label.
     * 
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    public String getTalendTypeId() {
        return talendTypeId;
    }

    public static EParamType fromString(String text) {
        for (EParamType mt : EParamType.values()) {
            if (mt.label.equalsIgnoreCase(text)) {
                return mt;
            }
        }
        return null;
    }

}
