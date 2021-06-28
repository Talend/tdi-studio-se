package org.talend.repository.generic.model;

import org.talend.commons.CommonsPlugin;
import org.talend.commons.ui.runtime.image.IImage;
import org.talend.commons.ui.runtime.repository.IExtendRepositoryNode;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.runtime.services.IGenericWizardService;

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
    	if (CommonsPlugin.isHeadless()) {
    		return;
    	}
        IGenericWizardService wizardService = null;
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IGenericWizardService.class)) {
            wizardService = (IGenericWizardService) GlobalServiceRegister.getDefault().getService(IGenericWizardService.class);
        }
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
