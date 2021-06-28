package org.talend.repository.generic.model;

import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.repository.IExtendRepositoryNode;
import org.talend.core.runtime.services.IGenericService;

/**
 * 
 * created by ycbai on 2015年12月7日 Detailled comment
 *
 */
public class FakeGenericExtendNode implements IExtendRepositoryNode {

    public FakeGenericExtendNode() {
        initGenericTypes();
    }

    private void initGenericTypes() {
        IGenericService wizardService = IGenericService.getService();
        if (wizardService != null) {
            wizardService.createNodesFromComponentService(null); // Just use to init generic types.
        }
    }

    @Override
    public IImage getNodeImage() {
        return null;
    }

    @Override
    public int getOrdinal() {
        return 1000;
    }

    @Override
    public Object[] getChildren() {
        return null;
    }

}
