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
package org.talend.designer.core.ui.views.problems;

import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.process.Problem;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.core.model.process.Problem.ProblemType;

/**
 * DOC bqian class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ææäº, 29 ä¹æ 2006) nrousseau $
 *
 */
public class ProblemViewProvider extends LabelProvider implements ITableLabelProvider, ITreeContentProvider {

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        Problem problem = (Problem) element;
        switch (columnIndex) {
        case 0:
            if (problem.isConcrete()) {
                if (problem.getStatus().equals(ProblemStatus.WARNING)) {
                    return ImageProvider.getImage(EImage.WARNING_SMALL);
                } else if (problem.getStatus().equals(ProblemStatus.ERROR)) {
                    return ImageProvider.getImage(EImage.ERROR_SMALL);
                } else if (problem.getStatus().equals(ProblemStatus.INFO)) {
                    return ImageProvider.getImage(EImage.INFORMATION_ICON);
                }

            } else {
                return ImageProvider.getImage(EImage.HIERARCHY_ICON);
            }
            break;
        case 1:
            if (problem.getType().equals(ProblemType.JOB)) {
                return ImageProvider.getImage(ECoreImage.PROCESS_ICON);
            } else if (problem.getType().equals(ProblemType.ROUTINE)) {
                return ImageProvider.getImage(ECoreImage.ROUTINE_ICON);
            } else {
                return null;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
        Problem problem = (Problem) element;
        switch (columnIndex) {
        case 0:
            return problem.getDescription();
        case 1:
            return problem.getProblemResource();
        default:
            break;
        }
        return Problem.EMPTY_STRING;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        Problem problem = (Problem) parentElement;
        return problem.getChildren();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length != 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof List) {
            return ((List) inputElement).toArray();
        }
        if (inputElement instanceof Object[]) {
            return (Object[]) inputElement;
        }
        return Problem.EMPTY_PROBLEM_ARRAY;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }

}
