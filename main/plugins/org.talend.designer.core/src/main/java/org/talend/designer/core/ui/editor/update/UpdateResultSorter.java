package org.talend.designer.core.ui.editor.update;

import java.util.Comparator;

import org.talend.core.model.update.EUpdateItemType;
import org.talend.core.model.update.UpdateResult;

public class UpdateResultSorter implements Comparator<UpdateResult> {

    @Override
    public int compare(UpdateResult arg0, UpdateResult arg1) {
        if (arg0.getUpdateType() == EUpdateItemType.CONTEXT_GROUP) {
            return -1;
        }
        return 1;
    }
}
