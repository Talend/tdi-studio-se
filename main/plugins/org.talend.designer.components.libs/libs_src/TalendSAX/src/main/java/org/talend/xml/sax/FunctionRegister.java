package org.talend.xml.sax;

import java.util.HashMap;
import java.util.Map;

import org.talend.xml.sax.function.Constant;
import org.talend.xml.sax.function.inter.Function;

public class FunctionRegister {

    private HashMap<String, Function> map = new HashMap<String, Function>();

    private String keys[] = null;

    private Function funcs[] = null;

    public FunctionRegister() {

        keys = Constant.FUNCNAMES;
        funcs = Constant.FUNCTIONS;

        registerAllFunc();
    }

    private void registerAllFunc() {
        for (int i = 0; i < keys.length; i++) {
            registerFunc(keys[i], funcs[i]);
        }
    }

    public void registerFunc(String funcName, Function func) {

        if (funcName != null && !funcName.equals("")) {

            if (!isFuncRegistered(funcName)) {
                map.put(funcName, func);
            }
        }
    }

    public Map<String, Function> getFunctions() {
        return map;
    }

    public boolean isFuncRegistered(String funcName) {
        if (map.isEmpty()) {
            return false;
        }

        return map.keySet().contains(funcName);
    }

    public Function getFunction(String funcName) {

        return map.get(funcName);
    }

}
