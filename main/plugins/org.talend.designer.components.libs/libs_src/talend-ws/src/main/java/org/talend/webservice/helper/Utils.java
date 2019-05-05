package org.talend.webservice.helper;

import org.apache.commons.lang.StringUtils;
import org.talend.webservice.jaxb.JAXBUtils;

public class Utils {

    public final String LIST_SIZE_SYMBOL = ".size";

    public final String LEFT_SQUARE_BRACKET = "[";

    public final String RIGHT_SQUARE_BRACKET = "]";

    public final String ALL_LIST_SYMBOL = "[*]";

    public void resolveInputPath(java.util.Map<String, Object> inputMap) {
        java.util.Map<String, Object> tempStoreMap = new java.util.HashMap<String, Object>();
        java.util.List<String> tempRemovePath = new java.util.ArrayList<String>();
        for (String key : inputMap.keySet()) {
            if (key.indexOf(ALL_LIST_SYMBOL) != -1) {
                String listHeadPath = key.substring(0, key.indexOf(ALL_LIST_SYMBOL));
                String listFootPath = key.substring(key.indexOf(ALL_LIST_SYMBOL) + ALL_LIST_SYMBOL.length());
                java.util.List listElement = (java.util.List) inputMap.get(key);
                // if the list is null, ignore it but remove the
                // original key
                if (listElement != null) {
                    for (int i = 0; i < listElement.size(); i++) {
                        tempStoreMap.put(listHeadPath + LEFT_SQUARE_BRACKET + i + RIGHT_SQUARE_BRACKET + listFootPath,
                                listElement.get(i));
                    }
                }
                tempRemovePath.add(key);
            }
        }
        inputMap.putAll(tempStoreMap);
        for (String removePath : tempRemovePath) {
            inputMap.remove(removePath);
        }
    }

    public String removePunctuation(String path) {
        String[] strings = PathUtil.splitPath(path, ".");
        for (int i = 0; i < strings.length; i++) {
            strings[i] = JAXBUtils.removePunctuation(strings[i]);
        }
        return StringUtils.join(strings, ".");
    }

    public Object getValue(java.util.Map<String, Object> map, String path) {
        if (path == null || "".equals(path)) {
            return null;
        }
        if (map == null || map.isEmpty()) {
            return null;
        }
        java.util.List<String> paths = new java.util.ArrayList<String>();
        resolvePath(map, path, paths);
        if (paths.size() > 0) {
            if (path.indexOf(ALL_LIST_SYMBOL) == -1) {
                return map.get(paths.get(0));
            } else {
                int size = paths.size();
                java.util.List<Object> out = new java.util.ArrayList<Object>(size);
                for (int i = 0; i < size; i++) {
                    out.add(map.get(paths.get(i)));
                }
                return out;
            }
        } else {
            return null;
        }
    }

    public void resolvePath(java.util.Map<String, Object> map, String path, java.util.List<String> paths) {
        String listHeadPath = "";
        String listFootPath = "";
        int size = 0;
        String tempPath = "";
        if (path.indexOf(ALL_LIST_SYMBOL) != -1) {
            listHeadPath = path.substring(0, path.indexOf(ALL_LIST_SYMBOL));
            listFootPath = path.substring(path.indexOf(ALL_LIST_SYMBOL) + ALL_LIST_SYMBOL.length());
            if (map.get(listHeadPath) == null && map.get(listHeadPath + LIST_SIZE_SYMBOL) != null) {
                size = Integer.parseInt(map.get(listHeadPath + LIST_SIZE_SYMBOL).toString());
                for (int i = 0; i < size; i++) {
                    tempPath = listHeadPath + LEFT_SQUARE_BRACKET + i + RIGHT_SQUARE_BRACKET + listFootPath;
                    if (tempPath.indexOf(ALL_LIST_SYMBOL) != -1) {
                        resolvePath(map, tempPath, paths);
                    } else {
                        paths.add(tempPath);
                    }
                }
            }
        } else {
            paths.add(path);
        }
    }

    public java.util.List<Object> normalize(String inputValue, String delimiter) {
        if (inputValue == null || "".equals(inputValue)) {
            return null;
        }
        Object[] inputValues = inputValue.split(delimiter);
        return java.util.Arrays.asList(inputValues);

    }

    public String denormalize(java.util.List inputValues, String delimiter) {
        if (inputValues == null || inputValues.isEmpty()) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (Object o : inputValues) {
            sb.append(String.valueOf(o));
            sb.append(delimiter);
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - delimiter.length(), sb.length());
        }
        return sb.toString();
    }

}
