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
package org.talend.designer.core.ui.editor.properties.controllers.uidialog.tns;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * rshi class global comment. Detailled comment
 */
public class TnsNodeModel {

    public TnsNodeModel(String key, int level) {
        super();
        this.key = key;
        this.level = level;
    }

    public TnsNodeModel(String key, String value, String parentNodeKey) {
        super();
        this.key = key;
        this.value = value;
        this.parentNodeKey = parentNodeKey;
    }

    public TnsNodeModel(String key, String value, String parentNodeKey, int level) {
        super();
        this.key = key;
        this.value = value;
        this.parentNodeKey = parentNodeKey;
        this.level = level;
    }

    public TnsNodeModel findChildByName(String childrenName) {
        List<TnsNodeModel> tnsNodestack = new ArrayList<TnsNodeModel>();
        tnsNodestack.addAll(children);
        while (tnsNodestack.size() > 0) {
            if (((TnsNodeModel) tnsNodestack.get(tnsNodestack.size() - 1)).getKey().equalsIgnoreCase(childrenName)) {
                return (TnsNodeModel) tnsNodestack.get(tnsNodestack.size() - 1);
            } else {
                List<TnsNodeModel> children = tnsNodestack.get(tnsNodestack.size() - 1).children;
                tnsNodestack.remove(tnsNodestack.size() - 1);
                tnsNodestack.addAll(children);
            }
        }
        return null;
    }

    private String parentNodeKey = null;

    private String key = null;

    private String value = null;

    private int level = -1; // if Root is 0, if the second level is 1 others -1

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    private List<TnsNodeModel> children = new ArrayList<TnsNodeModel>();

    public List<TnsNodeModel> getChildren() {
        return children;
    }

    public void setChildren(List<TnsNodeModel> children) {
        this.children = children;
    }

    public void addChildren(TnsNodeModel child) {
        this.children.add(child);
    }

    public void removeChildren(TnsNodeModel child) {
        this.children.remove(child);
    }

    public String getParentNodeKey() {
        return parentNodeKey;
    }

    public void setParentNodeKey(String parentNodeKey) {
        this.parentNodeKey = parentNodeKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean hasChildren() {
        if (children.size() > 0)
            return true;
        return false;
    }

}
