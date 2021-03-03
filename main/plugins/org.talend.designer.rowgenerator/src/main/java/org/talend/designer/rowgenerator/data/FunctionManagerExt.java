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
package org.talend.designer.rowgenerator.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.rowgenerator.RowGeneratorComponent;
import org.talend.designer.rowgenerator.managers.UIManager;
import org.talend.designer.rowgenerator.ui.editor.MetadataColumnExt;
import org.talend.utils.json.JSONArray;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

/**
 * class global comment. Detailled comment <br/>
 *
 * $Id: FunctionManager.java,v 1.13 2007/01/31 05:20:51 pub Exp $
 *
 */
public class FunctionManagerExt extends FunctionManager {

    private static final String DEFAULT_SELECTED_METHOD = "getAsciiRandomString"; //$NON-NLS-1$

    private static boolean addPreSuffix = true;

    public FunctionManagerExt() {
        super();
    }

    public Function getCurrentFunction(String funLabel, MetadataColumnExt bean) {
        Function currentFun = null;
        List<Function> functions = getFunctionsByType(bean.getTalendType());
        String[] arrayTalendFunctions2 = new String[functions.size()];
        if (functions.isEmpty()) {
            currentFun = new Function();
            currentFun.setDescription(""); //$NON-NLS-1$
            currentFun.setPreview(""); //$NON-NLS-1$
            currentFun.setParameters(new ArrayList<Parameter>());
            bean.setArrayFunctions(arrayTalendFunctions2);
        } else {
            currentFun = getFunnctionByLabel(functions, funLabel);
            bean.setArrayFunctions(getFunctionArrays(functions));
        }
        return currentFun;
    }

    public Function getDefaultFunction(MetadataColumnExt bean, String talendType) {
        Function currentFun = null;
        List<Function> functions = getFunctionsByType(talendType);
        String[] arrayTalendFunctions2 = new String[functions.size()];
        List<String> list = new ArrayList<String>();
        if (functions.isEmpty()) {
            currentFun = new Function();
            currentFun.setDescription(""); //$NON-NLS-1$
            currentFun.setPreview(""); //$NON-NLS-1$
            currentFun.setParameters(new ArrayList<Parameter>());
            bean.setArrayFunctions(arrayTalendFunctions2);
        } else {
            arrayTalendFunctions2 = getFunctionArrays(functions);
            bean.setArrayFunctions(arrayTalendFunctions2);
        }

        return getDefaultFunction(functions);
    }

    private Function getDefaultFunction(List<Function> functions) {
        Function currentFun = null;
        for (Function fun : functions) {
            if (fun.getName().equals(DEFAULT_SELECTED_METHOD)) {
                currentFun = (Function) fun.clone();
                break;
            }
        }
        if (currentFun == null && !functions.isEmpty()) {
            currentFun = (Function) functions.get(0).clone();
        }
        return currentFun;
    }

    public String[] getFunctionArrays(List<Function> functions) {
        List<String> listTalendFunctions = new ArrayList<String>();
        for (int i = 0; i < functions.size(); i++) {
            String functionLable = getFunctionLable(functions.get(i));
            if (!listTalendFunctions.contains(functionLable)) {
                listTalendFunctions.add(functionLable);
            }
        }
        String[] arrayTalendFunctions2 = new String[listTalendFunctions.size()];
        listTalendFunctions.toArray(arrayTalendFunctions2);

        Arrays.sort(arrayTalendFunctions2, new Comparator<String>() {

            @Override
            public int compare(String n1, String n2) {
                return n1.compareTo(n2);
            }

        });
        return arrayTalendFunctions2;
    }

    public Function getFunctionFromColumn(MetadataColumnExt column) {
        Function function = null;
        String functionInfo = column.getFunctionInfo();
        if (functionInfo != null) {
            try {
                JSONObject functionObj = new JSONObject(functionInfo);
                String functionName = functionObj.getString(Function.NAME);
                int functionSize = 0;
                JSONArray parametersArray = functionObj.getJSONArray(Function.PARAMETERS);
                if (parametersArray != null) {
                    functionSize = parametersArray.length();
                }
                List<Function> funcs = getFunctionsByType(column.getTalendType());
                for (Function func : funcs) {
                    if (func.getName().equals(functionName) && func.getParameters().size() == functionSize) {
                        function = func;
                        break;
                    }
                }
                if (function != null) {
                    function = function.clone(parametersArray);
                }
            } catch (JSONException e) {
                ExceptionHandler.process(e);
            }
        }
        return function;
    }

    public Function getFuntionFromArray(MetadataColumnExt bean, RowGeneratorComponent externalNode, int index) {
        String value = externalNode.getColumnValue(bean, index);
        List<Function> functions = getFunctionsByType(bean.getTalendType());
        Function currentFun = getAvailableFunFromValue(bean, value, functions);

        if (currentFun == null) {
            currentFun = getDefaultFunction(functions);
        }

        return currentFun;

    }

    /**
     * qzhang Comment method "isAvailableSubValue".
     *
     * @param value
     * @return
     */
    private Function getAvailableFunFromValue(MetadataColumnExt bean, String value, List<Function> funs) {

        Function currentFun = null;
        // for bug 0017094

        if (("id_Date").equals(bean.getTalendType()) && value != null && value.split("\\.").length > 3
                && value.split("\\(").length > 3) {
            int index = -1;
            int k = 0;
            for (int i = 0; i < funs.size(); i++) { // && !isExsit
                Function function = funs.get(i);
                int indexOf = value.indexOf(function.getName());
                if (index == -1 || (indexOf > -1 && index > indexOf)) {
                    index = indexOf;
                    k = i;
                }
            }
            int firstIndex = value.indexOf("(");
            int lastIndex = value.lastIndexOf(")");
            if (firstIndex < lastIndex && lastIndex < value.length()) {
                String str = value.substring(firstIndex + 1, lastIndex);
                String[] split = str.split(" ,");
                if (funs.get(k).getParameters().size() == split.length) {
                    currentFun = funs.get(k).clone(split);
                    if (currentFun != null) {
                        return currentFun;
                    }
                }
            }
        }

        boolean isExsit = false;
        for (int i = 0; i < funs.size() && !isExsit; i++) {
            Function function = funs.get(i);
            if (value != null && value.indexOf(function.getName()) != -1) {
                isExsit = true;
            }
        }
        if (value != null) {
            boolean isPure = true;
            int paramLength = value.length() - 2;
            if (UIManager.isJavaProject()) {
                isPure = value.indexOf(".") != -1 && value.indexOf("(") > value.indexOf(".") && value.endsWith(")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                paramLength = value.length() - 1;
            } else {
                isPure = value.startsWith(PERL_FUN_PREFIX) && value.endsWith(PERL_FUN_SUFFIX);
            }
            if (isPure && isExsit) {
                for (Function function : funs) {
                    int indexOf = value.indexOf(function.getName());
                    if (indexOf != -1) {
                        String para = value.substring(indexOf + function.getName().length() + 1, paramLength);
                        if ("".equals(para)) { //$NON-NLS-1$
                            if (function.getParameters().size() == 0) {
                                currentFun = (Function) function.clone();
                            }
                        } else {
                            // add by wzhang to fix bug 8732.
                            try {
                                //                                Pattern regex = Pattern.compile("(\\.|\\w)+\\(([^()]|\\(([^()])*\\))*\\)|\".*?\"|\\w+", //$NON-NLS-1$
                                // Pattern.CANON_EQ);
                                Pattern regex = Pattern.compile("[^,]+", //$NON-NLS-1$
                                        Pattern.CANON_EQ);
                                Matcher m = regex.matcher(para);
                                List<String> strs = new ArrayList<String>();
                                while (m.find()) {
                                    strs.add(m.group());
                                }
                                String[] ps = strs.toArray(new String[strs.size()]);
                                if (ps.length == function.getParameters().size()) {
                                    currentFun = function.clone(ps);
                                }
                            } catch (PatternSyntaxException ex) {
                                // Syntax error in the regular expression
                            }
                        }
                    }
                }
            } else {
                currentFun = createPureFunctions(value, funs, currentFun);
            }

        }
        return currentFun;
    }

    /**
     * qzhang Comment method "createPureFunctions".
     *
     * @param value
     * @param funs
     * @param currentFun
     * @return
     */
    private Function createPureFunctions(String value, List<Function> funs, Function currentFun) {
        for (Function function : funs) {
            if (function.getName().equals(PURE_PERL_NAME)) {
                currentFun = (Function) function.clone();
                ((StringParameter) currentFun.getParameters().get(0)).setValue(value);
            }
        }
        return currentFun;
    }

    @SuppressWarnings("unchecked")
    public static String getOneColData(MetadataColumnExt bean) {
        return getOneColData(bean, false);
    }

    @SuppressWarnings("unchecked")
    public static String getOneColData(MetadataColumnExt bean, boolean replace) {
        if (bean != null && bean.getFunction() != null) {
            String newValue = addPreSuffix ? PERL_FUN_PREFIX : ""; //$NON-NLS-1$
            String name = bean.getFunction().getName();
            if (PURE_PERL_NAME.equals(name)) {
                newValue = ((StringParameter) bean.getFunction().getParameters().get(0)).getValue();
            } else {
                if (name == null || "".equals(name)) { //$NON-NLS-1$
                    return ""; //$NON-NLS-1$
                }
                final List<Parameter> parameters = bean.getFunction().getParameters();
                if (UIManager.isJavaProject()) {
                    String className = bean.getFunction().getClassName();
                    String fullName = className + "." + name;//$NON-NLS-1$
                    //                    String fullName = RoutineFunctionParser.getTypeMethods().get(bean.getTalendType() + "." + name); //$NON-NLS-1$
                    newValue = fullName + "("; //$NON-NLS-1$
                    for (Parameter pa : parameters) {
                        if (replace) {
                            newValue += "${0}" + FUN_PARAM_SEPARATED;
                            replace = false;
                        } else {
                            newValue += pa.getValue() + FUN_PARAM_SEPARATED;
                        }
                    }
                    if (!parameters.isEmpty()) {
                        newValue = newValue.substring(0, newValue.length() - 1);
                    }
                    newValue += ")"; //$NON-NLS-1$

                } else {
                    newValue += name + "("; //$NON-NLS-1$
                    for (Parameter pa : parameters) {
                        newValue += pa.getValue() + FUN_PARAM_SEPARATED;
                    }
                    newValue = newValue.substring(0, newValue.length() - 1);

                    newValue += addPreSuffix ? PERL_FUN_SUFFIX : ")"; //$NON-NLS-1$

                }
            }
            return newValue;
        }
        return null;
    }

    public static String getOneColData(MetadataColumnExt bean, boolean f, boolean replace) {
        addPreSuffix = f;
        String str = getOneColData(bean, replace);
        addPreSuffix = true;
        return str;
    }

    public static String getFunctionLable(Function function) {
        StringBuffer sb = new StringBuffer();
        sb.append(function.getClassName() == null ? function.getName()
                : (function.getClassName() + FunctionManager.JAVA_METHOD_SEPARATED) + function.getName());
        if (!PURE_PERL_NAME.equals(function.getName())) {
            sb.append(FunctionManager.FUN_PREFIX);
            int i = 0;
            for (Object objectParameter : function.getParameters()) {
                if (objectParameter instanceof Parameter) {
                    sb.append(((Parameter) objectParameter).getType());
                }
                i++;
                if (i != function.getParameters().size()) {
                    sb.append(FunctionManager.FUN_PARAM_SEPARATED);
                }
            }
            sb.append(FunctionManager.FUN_SUFFIX);
        }
        return sb.toString();
    }

    private Function getFunnctionByLabel(List<Function> functions, String value) {
        Function func = null;
        for (Function fun : functions) {
            // see bug 8055,remove the getLastName() method in TDQ,it has the same name as in TIS.
            if (value.equals(getFunctionLable(fun))) {
                func = (Function) fun.clone();
                break;
            }
        }
        // adapt to some old versions in case className is not saved
        if (func == null) {
            for (Function fun : functions) {
                if (value.equals(fun.getName())) {
                    func = (Function) fun.clone();
                    break;
                }
            }
        }

        return func;
    }

}
