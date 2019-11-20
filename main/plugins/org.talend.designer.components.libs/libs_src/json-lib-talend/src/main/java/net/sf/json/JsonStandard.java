package net.sf.json;

public enum JsonStandard {
    /**
     * Out of date standard used to be default before
     */
    LEGACY,

    /**
     * Updated standard due to RFC 7159 to not unwrap "null" strings (keep quotations)
     */
    WRAP_NULL_STRINGS

}
