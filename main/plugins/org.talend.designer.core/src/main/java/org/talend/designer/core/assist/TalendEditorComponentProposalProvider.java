package org.talend.designer.core.assist;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.process.IProcess2;
import org.talend.core.ui.IJobletProviderService;
import org.talend.core.ui.component.ComponentPaletteUtilities;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.core.ui.component.TalendCreationToolEntry;
import org.talend.designer.core.model.components.DummyComponent;
import org.talend.designer.core.ui.editor.PaletteComponentFactory;
import org.talend.designer.core.ui.editor.TalendEditorPaletteFactory;

class TalendEditorComponentProposalProvider implements IContentProposalProvider {

    protected IComponentsFactory componentsFactory = ComponentsFactoryProvider.getInstance();

    protected List<IContentProposal> proposalList;

    protected TalendEditorComponentCreationAssist componentAssistant;

    protected IProcess2 process;

    /**
     * Construct a TalendEditorComponentProposalProvider whose content proposals are always the specified set of
     * Component.
     * 
     * @param components the Map of Components to be returned whenever proposals are requested.
     */
    public TalendEditorComponentProposalProvider(TalendEditorComponentCreationAssist componentAssist,
            List<IContentProposal> proposalList, IProcess2 process) {
        super();
        this.componentAssistant = componentAssist;
        this.proposalList = proposalList;
        this.process = process;
    }

    /**
     * Return an array of Objects representing the valid content proposals for a field.
     * 
     * @param contents the current contents of the field (only consulted if filtering is set to <code>true</code>)
     * @param position the current cursor position within the field (ignored)
     * @return the array of Objects that represent valid proposals for the field given its current content.
     */
    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        List<IComponent> relatedComponent = TalendEditorPaletteFactory.getRelatedComponents(componentsFactory, contents);
        if (componentAssistant != null) {
            relatedComponent = componentAssistant.filterComponents(relatedComponent);
        }
        proposalList.clear();
        // add joblet components in joblet editor.
        if (isJobletItem()) {
            List<PaletteEntry> extraPaletteEntry = ComponentPaletteUtilities.getExtraPaletteEntry();
            if (extraPaletteEntry != null) {
                for (PaletteEntry entry : extraPaletteEntry) {
                    if (entry != null && entry.isVisible() && entry instanceof TalendCreationToolEntry) {
                        TalendCreationToolEntry tool = (TalendCreationToolEntry) entry;
                        CreationFactory creationFactory = tool.getFactory();
                        if (creationFactory != null && creationFactory instanceof PaletteComponentFactory) {
                            PaletteComponentFactory componentFactory = (PaletteComponentFactory) creationFactory;
                            proposalList.add(new ComponentContentProposal(componentFactory.getComponent()));
                        }
                    }
                }
            }
        }

        if (relatedComponent != null && !relatedComponent.isEmpty()) {
            Iterator<IComponent> iter = relatedComponent.iterator();
            while (iter.hasNext()) {
                IComponent component = iter.next();
                // proposalList.add(new ComponentContentProposal(component.getName(), component.getLongName(),
                // component));
                proposalList.add(new ComponentContentProposal(component));
            }
        }

        /**
         * Always add Note
         */
        DummyComponent noteComponent = new DummyComponent("Note"); //$NON-NLS-1$
        noteComponent.setIcon16(ImageProvider.getImageDesc(ECoreImage.NOTE_SMALL_ICON));
        noteComponent.setOriginalFamilyName("Misc"); //$NON-NLS-1$
        proposalList.add(new ComponentContentProposal(noteComponent));

        return proposalList.toArray(new IContentProposal[0]);

    }

    private boolean isJobletItem() {
        if (process != null && process.getProperty() != null
                && GlobalServiceRegister.getDefault().isServiceRegistered(IJobletProviderService.class)) {
            IJobletProviderService jobletservice = (IJobletProviderService) GlobalServiceRegister.getDefault().getService(
                    IJobletProviderService.class);
            return jobletservice.isJobletItem(process.getProperty().getItem());
        }
        return false;
    }
}
