package org.talend.xml.sax.function.inter;

import java.util.List;
import java.util.Map;

public interface Function {

    public void call(List<Map<String, Object>> args);

    public String getResult();

}
