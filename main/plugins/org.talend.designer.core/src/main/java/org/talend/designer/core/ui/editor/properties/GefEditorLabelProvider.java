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
package org.talend.designer.core.ui.editor.properties;

import java.util.Iterator;

import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.Connection;
import org.talend.designer.core.ui.editor.connections.ConnectionLabel;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainerPart;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodeLabelEditPart;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.notes.Note;
import org.talend.designer.core.ui.editor.notes.NoteEditPart;
import org.talend.designer.core.ui.editor.outline.NodeReturnsTreeEditPart;
import org.talend.designer.core.ui.editor.outline.NodeTreeEditPart;
import org.talend.designer.core.ui.editor.outline.ProcessTreeEditPart;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.process.ProcessPart;

/**
 * Label provider for the title bar for the tabbed property sheet page. <br/>
 *
 * $Id$
 *
 */
public class GefEditorLabelProvider extends LabelProvider {

    private ITypeMapper typeMapper;

    private Node lastNode = null;

    public GefEditorLabelProvider() {
        super();
        typeMapper = new GefEditorTypeMapper();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object objects) {
        Node node = null;
        if (objects == null || objects.equals(StructuredSelection.EMPTY)) {
            return null;
        }
        if (!(objects instanceof IStructuredSelection)) {
            return null;
        }
        final boolean[] multiple = { false };
        Object object = getObject(objects, multiple);
        if (object == null) {
            return null;
        }

        if ((object instanceof NodeTreeEditPart)) {
            node = (Node) ((NodeTreeEditPart) object).getModel();
        } else {
            if (object instanceof NodeReturnsTreeEditPart) {
                node = lastNode;
            } else {
                if (object instanceof ProcessPart) {
                    return ImageProvider.getImage(ECoreImage.PROCESS_ICON);
                }
                if (object instanceof ConnectionPart) {
                    return ImageProvider.getImage(EImage.RIGHT_ICON);
                }
                if (object instanceof NoteEditPart) {
                    return ImageProvider.getImage(ECoreImage.CODE_ICON);
                }
                if (object instanceof ConnLabelEditPart) {
                    return ImageProvider.getImage(EImage.RIGHT_ICON);
                }
                if ((object instanceof NodeLabelEditPart)) {
                    node = ((NodeContainer) ((NodeLabelEditPart) object).getParent().getModel()).getNode();
                }
                if (!(object instanceof NodePart)) {
                    return null;
                }
                if (node == null) {
                    node = (Node) ((NodePart) object).getModel();
                }
            }
        }
        if (lastNode != node) {
            lastNode = node;
        }
        return CoreImageProvider.getComponentIcon(node.getComponent(), ICON_SIZE.ICON_24);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    public String getText(Object objects) {
        Node node = null;
        if (objects == null || objects.equals(StructuredSelection.EMPTY)) {
            return "No items selected"; //$NON-NLS-1$
        }
        if (!(objects instanceof IStructuredSelection)) {
            return null;
        }
        final boolean[] multiple = { false };
        Object object = getObject(objects, multiple);
        if (object == null/* || ((IStructuredSelection) objects).size() > 1 */) {
            return "No items selected"; //$NON-NLS-1$
        } else {
            if (object instanceof NodeContainerPart) {
                NodeContainerPart nContainer = (NodeContainerPart) object;
                Process process = (Process) nContainer.getParent().getModel();
                return process.getName();
            } else if (object instanceof ProcessPart) {
                Process process = (Process) ((ProcessPart) object).getModel();
                return process.getLabel();
            } else if (object instanceof ProcessTreeEditPart) {
                Process process = (Process) ((ProcessTreeEditPart) object).getModel();
                return process.getName();
            }
            if (object instanceof ConnectionPart) {
                Connection conn = (Connection) ((ConnectionPart) object).getModel();
                return conn.getName();
            }
            if (object instanceof NoteEditPart) {
                return Note.class.getSimpleName();
            }
            if (object instanceof ConnLabelEditPart) {
                Connection conn = (Connection) ((ConnectionLabel) ((ConnLabelEditPart) object).getModel()).getConnection();
                return conn.getName();
            }
            if (object instanceof NodeTreeEditPart) {
                node = (Node) ((NodeTreeEditPart) object).getModel();
            } else {
                if (object instanceof NodeReturnsTreeEditPart) {
                    node = lastNode;
                } else {
                    if (object instanceof NodeLabelEditPart) {
                        node = ((NodeContainer) ((NodeLabelEditPart) object).getParent().getModel()).getNode();
                    }
                    if (!(object instanceof NodePart)) {
                        return null;
                    }
                    if (node == null) {
                        node = (Node) ((NodePart) object).getModel();
                    }
                }
            }
            if (lastNode != node) {
                lastNode = node;
            }
            String name = node.getUniqueName();
            // if (!node.getComponent().getTranslatedName().equals(node.getComponent().getName())) {
            // name += " (" + node.getComponent().getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            // }
            return name;
        }
    }

    /**
     * Determine if a multiple object selection has been passed to the label provider. If the objects is a
     * IStructuredSelection, see if all the objects in the selection are the same and if so, we want to provide labels
     * for the common selected element.
     *
     * @param objects a single object or a IStructuredSelection.
     * @param multiple first element in the array is true if there is multiple unequal selected elements in a
     * IStructuredSelection.
     * @return the object to get labels for.
     */
    private Object getObject(Object objects, boolean[] multiple) {
        Assert.isNotNull(objects);
        Object object = null;
        if (objects instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) objects;
            object = selection.getFirstElement();
            if (selection.size() == 1) {
                // one element selected
                multiple[0] = false;
                return object;
            }
            // multiple elements selected
            multiple[0] = true;
            Class firstClass = typeMapper.mapType(object);
            // determine if all the objects in the selection are the same type
            if (selection.size() > 1) {
                for (Iterator i = selection.iterator(); i.hasNext();) {
                    Object next = i.next();
                    Class nextClass = typeMapper.mapType(next);
                    if (!nextClass.equals(firstClass)) {
                        multiple[0] = false;
                        object = null;
                        break;
                    }
                }
            }
        } else {
            multiple[0] = false;
            object = objects;
        }
        return object;
    }

}
