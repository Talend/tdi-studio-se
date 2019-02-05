package org.talend.xml.sax.function;

import org.talend.xml.sax.function.inter.Function;

public class Constant {

    public static final String FUNCNAMES[] = { "name", };

    public static final Function FUNCTIONS[] = { new NameFunction(), };
}
