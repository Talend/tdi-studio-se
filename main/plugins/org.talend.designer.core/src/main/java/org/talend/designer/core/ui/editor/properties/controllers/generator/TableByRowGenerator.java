package org.talend.designer.core.ui.editor.properties.controllers.generator;

import org.talend.core.properties.tab.IDynamicProperty;
import org.talend.designer.core.ui.editor.properties.TableByRowController;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractElementPropertySectionController;

public class TableByRowGenerator implements IControllerGenerator {

    private IDynamicProperty dp;

    @Override
    public AbstractElementPropertySectionController generate() {
        return new TableByRowController(dp);
    }

    @Override
    public void setDynamicProperty(IDynamicProperty dp) {
        this.dp = dp;
    }

}
