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
package org.talend.designer.core.ui.editor.update;

import java.util.ArrayList;
import java.util.List;

import org.talend.core.model.update.EUpdateResult;
import org.talend.core.model.update.IUpdateItemType;
import org.talend.core.model.update.UpdateResult;

/**
 * DOC ggu class global comment. Detailled comment
 */

/**
 *
 * ggu Job class global comment. Detailled comment
 */
class Job {

    private String name;

    private List<Category> categories = new ArrayList<Category>();

    /**
     * only used for image.
     */
    private boolean isJoblet = true;

    private boolean isSparkJoblet = false;

    private boolean isSparkStreamingJoblet = false;

    private boolean isMR = true;

    private boolean isStreaming = true;

    private boolean readOnlyProcess = false;

    private org.talend.core.model.properties.Item modelItem;

    /**
     * Getter for readOnlyProcess.
     *
     * @return the readOnlyProcess
     */
    public boolean isReadOnlyProcess() {
        return this.readOnlyProcess;
    }

    public Job(String name) {
        this.name = name;
    }

    public boolean addCategory(Category category) {
        if (!categories.contains(category)) {
            return categories.add(category);
        }
        return false;
    }

    public Category getCategory(String name) {
        if (name == null) {
            return null;
        }
        for (Category category : categories) {
            if (name.equals(category.getName())) {
                return category;
            }
        }
        return null;
    }

    public List<Category> getCategories() {
        return this.categories;
    }

    public String getName() {
        return this.name;
    }

    public boolean isJoblet() {
        return this.isJoblet;
    }

    public void setJoblet(boolean isJoblet) {
        this.isJoblet = isJoblet;
    }

    public boolean isSparkJoblet() {
        return this.isSparkJoblet;
    }

    public void setSparkJoblet(boolean isSparkJoblet) {
        this.isSparkJoblet = isSparkJoblet;
    }

    public boolean isSparkStreamingJoblet() {
        return this.isSparkStreamingJoblet;
    }

    public void setSparkStreamingJoblet(boolean isSparkStreamingJoblet) {
        this.isSparkStreamingJoblet = isSparkStreamingJoblet;
    }

    public boolean isMR() {
        return this.isMR;
    }

    public void setMR(boolean isMR) {
        this.isMR = isMR;
    }

    public boolean isStreaming() {
        return this.isStreaming;
    }

    public void setStreaming(boolean isStreaming) {
        this.isStreaming = isStreaming;
    }

    /**
     * DOC Administrator Comment method "setReadOnlyProcess".
     *
     * @param readOnlyProcess
     */
    public void setReadOnlyProcess(boolean readOnlyProcess) {
        this.readOnlyProcess = readOnlyProcess;

    }

    public org.talend.core.model.properties.Item getModelItem() {
        return this.modelItem;
    }

    public void setModelItem(org.talend.core.model.properties.Item item) {
        this.modelItem = item;
    }

}

/**
 *
 * ggu Item class global comment. Detailled comment.
 *
 */
class Category {

    private Job parent;

    private String name;

    private List<Item> items = new ArrayList<Item>();

    /**
     * only used for image.
     */
    private IUpdateItemType type;

    /**
     * only used for node or nodeType icon.
     */
    private Object node;

    public Category(Job parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean addItem(Item item) {
        if (!items.contains(item)) {
            return items.add(item);
        }
        return false;
    }

    public List<Item> getItems() {
        return this.items;
    }

    public Job getParent() {
        return this.parent;
    }

    public IUpdateItemType getType() {
        return this.type;
    }

    public void setType(IUpdateItemType type) {
        this.type = type;
    }

    public Object getNode() {
        return this.node;
    }

    public void setNode(Object node) {
        this.node = node;
    }

}

/**
 *
 * ggu Detail class global comment. Detailled comment.
 */
class Item {

    private Category parent;

    private UpdateResult result;

    public Item(Category parent, UpdateResult resultObject) {
        this.parent = parent;
        this.result = resultObject;
    }

    public EUpdateResult getOperation() {
        if (getResultObject() != null) {
            return getResultObject().getResultType();
        }
        return null;
    }

    public String getRemark() {
        if (getResultObject() != null) {
            return getResultObject().getRemark();
        }
        return null;
    }

    public String getProperty() {
        if (getResultObject() != null) {
            return getResultObject().getName();
        }
        return null;
    }

    public Category getParent() {
        return this.parent;
    }

    public UpdateResult getResultObject() {
        return this.result;
    }

    public boolean isChecked() {
        if (getResultObject() != null) {
            return getResultObject().isChecked();
        }
        return false;
    }

    public void setChecked(boolean state) {
        if (getResultObject() != null) {
            getResultObject().setChecked(state);
        }
    }

}
