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
package org.talend.designer.core.ui.editor.palette;

import java.util.Date;

import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.talend.core.model.components.EComponentType;
import org.talend.core.model.components.IComponent;
import org.talend.designer.core.ui.editor.PaletteComponentFactory;
import org.talend.designer.core.ui.editor.jobletcontainer.JobletUtil;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class TalendCombinedTemplateCreationEntry extends CombinedTemplateCreationEntry {

    protected Date timestemp;

    protected IComponent component;

    /**
     * DOC cmeng TalendCombinedTemplateCreationEntry constructor comment.
     *
     * @param label
     * @param shortDesc
     * @param template
     * @param factory
     * @param iconSmall
     * @param iconLarge
     */
    public TalendCombinedTemplateCreationEntry(String label, String shortDesc, Object template, CreationFactory factory,
            ImageDescriptor iconSmall, ImageDescriptor iconLarge) {
        super(label, shortDesc, template, factory, iconSmall, iconLarge);
    }

    /**
     * DOC cmeng TalendCombinedTemplateCreationEntry constructor comment.
     *
     * @param label
     * @param shortDesc
     * @param template
     * @param factory
     * @param iconSmall
     * @param iconLarge
     */
    public TalendCombinedTemplateCreationEntry(String label, String shortDesc, Object template, IComponent component,
            ImageDescriptor iconSmall, ImageDescriptor iconLarge) {
        super(label, shortDesc, template, new PaletteComponentFactory(component), iconSmall, iconLarge);
        this.component = component;
        reSetLabel();
    }

    public TalendCombinedTemplateCreationEntry(String label, String shortDesc, Object template, IComponent component,
            ImageDescriptor iconSmall, ImageDescriptor iconLarge, String filter) {
        super(label, shortDesc, template, new PaletteComponentFactory(component, filter), iconSmall, iconLarge);
        this.component = component;
        reSetLabel();
    }

    public Date getTimestemp() {
        return this.timestemp;
    }

    public void setTimestemp(Date timestemp) {
        this.timestemp = timestemp;
    }

    public void reSetLabel() {
        if (this.component == null) {
            return;
        }
        String s = this.getLabel();
        if (!new JobletUtil().matchExpression(s)) {
            return;
        }
        if (component.getComponentType() != EComponentType.JOBLET) {
            return;
        }
        String[] names = s.split(":"); //$NON-NLS-1$
        s = names[1] + "(" + names[0] + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        this.setLabel(s);
    }

    public String getComponentName() {
        return component.getName();
    }

    public IComponent getComponent() {
        return component;
    }
}
