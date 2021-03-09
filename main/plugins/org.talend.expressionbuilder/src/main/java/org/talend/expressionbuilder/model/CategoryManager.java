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
package org.talend.expressionbuilder.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.commons.utils.generation.JavaUtils;
import org.talend.designer.rowgenerator.data.AbstractFunctionParser;
import org.talend.designer.rowgenerator.data.Function;
import org.talend.designer.rowgenerator.data.FunctionManager;
import org.talend.designer.rowgenerator.data.TalendType;
import org.talend.expressionbuilder.i18n.Messages;

/**
 * yzhang class global comment. Detailled comment <br/>
 *
 * $Id: CategoryManager.java 上午10:06:09 2007-7-24 +0000 (2007-7-24) yzhang $
 *
 */
public class CategoryManager {

    private static final String DEFAULT_CATEGORY = "_default_category"; //$NON-NLS-1$

    private Category defaultCategory;

    private boolean hasPigDataFuCategory = false;

    public java.util.List<Category> getInputCategory(String type) {
        FunctionManager functionManager = null;
        if (JavaUtils.JAVA_PIG_DIRECTORY.equals(type)) {
            functionManager = new FunctionManager(type);
        } else {
            functionManager = new FunctionManager();
        }

        java.util.List<TalendType> talendTypes = functionManager.getTalendTypes();

        List<Category> categories = convertTypesToCategories(talendTypes);

        Category allCategories = new Category();
        allCategories.setName(Messages.getString("CategoryManager.all")); //$NON-NLS-1$
        Category userDefined = new Category();
        userDefined.setName(Messages.getString("CategoryManager.user.defined")); //$NON-NLS-1$

        for (Category category : categories) {
            final List<Function> functions = category.getFunctions();
            allCategories.addFunctions(functions);
            for (Function function : functions) {
                if (function.isUserDefined()) {
                    userDefined.addFunctions(function);
                }
            }
        }

        // remove the default category since it already added into user defined category.
        if (defaultCategory != null && categories.contains(defaultCategory)) {
            userDefined.addFunctions(defaultCategory.getFunctions());
            categories.remove(defaultCategory);
        }

        List<Category> input = new ArrayList<Category>();
        if (!JavaUtils.JAVA_PIG_DIRECTORY.equals(type)) {
            input.add(allCategories);
            input.add(userDefined);
        }
        input.addAll(categories);
        return input;
    }

    /**
     * Converts the structure of talendTypes to Categories.
     *
     * @param talendTypes
     * @return
     */
    private List<Category> convertTypesToCategories(List<TalendType> talendTypes) {
        List<Category> categories = new ArrayList<Category>();

        Map<String, List<Function>> map = new HashMap<String, List<Function>>();

        for (TalendType type : talendTypes) {
            List functions = type.getFunctions();
            for (int i = 0; i < functions.size(); i++) {
                Function func = (Function) functions.get(i);

                // if there's no category defination for the funtion set it as default category.
                if (func.getCategory() == null || AbstractFunctionParser.EMPTY_STRING.equals(func.getCategory())) {
                    func.setCategory(DEFAULT_CATEGORY);
                }

                if (hasPigDataFuCategory && "User Defined".equals(func.getCategory())) {//$NON-NLS-1$
                    continue;
                }

                List<Function> funcs = map.get(func.getCategory());
                if (funcs == null) {
                    funcs = new ArrayList<Function>();
                    map.put(func.getCategory(), funcs);
                }
                if ("StoreFunc".equals(func.getPreview()) || "LoadFunc".equals(func.getPreview())) {
                    continue;
                }
                funcs.add(func);
            }
        }

        for (String categoryName : map.keySet()) {
            Category category = new Category();
            category.setName(categoryName);
            category.setFunctions(map.get(categoryName));
            // only pig var table has the DataFu category
            if (!hasPigDataFuCategory && "Pig DataFu Functions".equals(categoryName)) {//$NON-NLS-1$
                continue;
            }
            categories.add(category);
            if (DEFAULT_CATEGORY.equals(category.getName())) {
                defaultCategory = category;
            }
        }

        return categories;
    }

    /**
     * Getter for hasPigDataFuCategory.
     *
     * @return the hasPigDataFuCategory
     */
    public boolean isHasPigDataFuCategory() {
        return this.hasPigDataFuCategory;
    }

    /**
     * Sets the hasPigDataFuCategory.
     *
     * @param hasPigDataFuCategory the hasPigDataFuCategory to set
     */
    public void setHasPigDataFuCategory(boolean hasPigDataFuCategory) {
        this.hasPigDataFuCategory = hasPigDataFuCategory;
    }

}
