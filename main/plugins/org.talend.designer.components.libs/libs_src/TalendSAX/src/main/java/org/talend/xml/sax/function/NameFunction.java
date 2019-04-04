package org.talend.xml.sax.function;

import java.util.List;
import java.util.Map;

import org.talend.xml.sax.function.inter.Function;

public class NameFunction implements Function {

    String result = "";

    public void call(List<Map<String, Object>> args) {

        if (args != null && args.size() > 0) {
            evaluate(args);
        }

    }

    public String getResult() {
        return result;
    }

    public void evaluate(List<Map<String, Object>> args) {
        for (int i = 0; i < args.size(); i++) {
            Map<String, Object> map = args.get(i);
            String loopPath = map.get("loopPath").toString();
            String currentPath = map.get("column").toString();
            if (!currentPath.equals(loopPath)) {
                int num = 0;
                if (currentPath.indexOf(loopPath) == 0) {
                    num = currentPath.substring(loopPath.length()).split("/").length - 2;
                }
                if (map.get("value") != null && !"".equals(map.get("value").toString().trim())) {
                    if (map.get("value") instanceof String) {
                        for (int j = 0; j < num; j++) {
                            result = result + "\t";
                        }
                        result = result + map.get("value") + "\n";
                    }
                }
            }
        }
    }

    public String toString() {
        return "Function:name()";
    }

}
