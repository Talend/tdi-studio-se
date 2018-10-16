package org.talend.designer.oas.external;

public class APIParam {

    private String name;

    private EParamType type;

    private String pattern;

    private EParamKind kind;

    private boolean required = false;

    private Integer length;

    private String defaultValue;

    public APIParam(String name, EParamType type, EParamKind paramKind) {
        super();
        this.name = name;
        this.type = type;
        this.kind = paramKind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EParamType getType() {
        return type;
    }

    public void setType(EParamType type) {
        this.type = type;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public EParamKind getParamKind() {
        return kind;
    }

    public void setParamKind(EParamKind source) {
        this.kind = source;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        APIParam other = (APIParam) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
