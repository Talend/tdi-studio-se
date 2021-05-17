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
package org.talend.designer.business.diagram.custom.commands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.FeatureMapUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.emf.clipboard.core.ClipboardSupportUtil;
import org.eclipse.gmf.runtime.emf.commands.core.commands.DuplicateEObjectsCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.DuplicateElementsRequest;
import org.talend.designer.business.model.business.BusinessAssignment;
import org.talend.designer.business.model.business.BusinessItem;

/**
 * DOC wchen class global comment. Detailled comment
 */
public class DuplicateAnythingCommand extends DuplicateEObjectsCommand {

    /**
     * DOC wchen DuplicateAnythingCommand constructor comment.
     *
     * @param editingDomain
     * @param label
     * @param objectsToBeDuplicated
     */
    public DuplicateAnythingCommand(TransactionalEditingDomain editingDomain, DuplicateElementsRequest req) {
        super(editingDomain, req.getLabel(), req.getElementsToBeDuplicated(), req.getAllDuplicatedElementsMap());

    }

    @Override
    protected CommandResult doExecuteWithResult(IProgressMonitor progressMonitor, IAdaptable info) throws ExecutionException {
        // Remove elements whose container is getting copied.
        ClipboardSupportUtil.getCopyElements(getObjectsToBeDuplicated());

        // Perform the copy and update the references.
        EcoreUtil.Copier copier = new EcoreUtil.Copier();
        copier.copyAll(getObjectsToBeDuplicated());
        copier.copyReferences();

        // Update the map with all elements duplicated.
        getAllDuplicatedObjectsMap().putAll(copier);

        Map targetAndSource = new HashMap();

        // Add the duplicates to the original's container.
        for (Iterator i = getObjectsToBeDuplicated().iterator(); i.hasNext();) {
            EObject original = (EObject) i.next();
            EObject duplicate = (EObject) copier.get(original);
            targetAndSource.put(duplicate, original);

            EReference reference = original.eContainmentFeature();
            if (reference != null && FeatureMapUtil.isMany(original.eContainer(), reference)
                    && ClipboardSupportUtil.isOkToAppendEObjectAt(original.eContainer(), reference, duplicate)) {

                ClipboardSupportUtil.appendEObjectAt(original.eContainer(), reference, duplicate);
            }
        }

        for (Object obj : targetAndSource.keySet()) {
            if (obj instanceof BusinessItem) {
                BusinessItem item = (BusinessItem) obj;
                Object sourceObject = targetAndSource.get(item);
                if (sourceObject instanceof BusinessItem) {
                    BusinessItem sourceItem = (BusinessItem) sourceObject;
                    List targetAssignments = item.getAssignments();
                    List sourceAssignments = sourceItem.getAssignments();

                    for (int i = 0; i < targetAssignments.size() && i < sourceAssignments.size(); i++) {
                        BusinessAssignment targetAssign = (BusinessAssignment) targetAssignments.get(i);
                        targetAssign.setComment(((BusinessAssignment) sourceAssignments.get(i)).getComment());
                        targetAssign.setTalendItem(((BusinessAssignment) sourceAssignments.get(i)).getTalendItem());
                    }
                }
            }
        }
        return null;
    }

}
