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
package org.talend.designer.business.diagram.custom.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.runtime.image.ImageUtils;
import org.talend.commons.ui.runtime.image.ImageUtils.ICON_SIZE;
import org.talend.commons.ui.runtime.image.OverlayImage;
import org.talend.commons.ui.runtime.image.OverlayImage.EPosition;
import org.talend.core.CorePlugin;
import org.talend.core.model.metadata.MetadataToolHelper;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.Query;
import org.talend.core.model.metadata.builder.connection.SAPFunctionUnit;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.JobletProcessItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.repository.ui.view.RepositoryLabelProvider;
import org.talend.core.ui.images.CoreImageProvider;
import org.talend.designer.business.diagram.custom.figures.BusinessTooltipFigure;
import org.talend.designer.business.model.business.BusinessAssignment;
import org.talend.designer.business.model.business.BusinessItem;
import org.talend.designer.business.model.business.Routine;
import org.talend.designer.business.model.business.SQLPattern;
import org.talend.designer.business.model.business.TalendItem;
import org.talend.designer.business.model.business.provider.BusinessAssignmentItemProvider;
import org.talend.designer.business.model.business.provider.BusinessItemProviderAdapterFactory;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class ElementHelper {

    /**
     * DOC mhelleboid Comment method "getElement".
     *
     * @param editPart
     * @return
     */
    public BusinessItem getElement(EditPart editPart) {
        Object model = editPart.getModel();
        if (model instanceof View) {
            View node = (View) model;
            EObject element = node.getElement();
            if (element instanceof BusinessItem) {
                return (BusinessItem) element;
            }
        }
        return null;
    }

    /**
     * DOC Administrator Comment method "getTooltipFigure".
     *
     * @param figure
     */
    public void updateTooltipFigure(Figure figure, BusinessTooltipFigure tooltipFigure, EditPart editPart) {
        EObject element = ((View) editPart.getModel()).getElement();
        if (element instanceof BusinessItem) {
            List assignements = ((BusinessItem) element).getAssignments();
            BusinessItemProviderAdapterFactory adapterFactory = new BusinessItemProviderAdapterFactory();
            BusinessAssignmentItemProvider provider = (BusinessAssignmentItemProvider) adapterFactory
                    .createBusinessAssignmentAdapter();
            Image img = null;
            Label label = null;
            List<Label> labels = new ArrayList();
            try {
                for (Object assignment : assignements) {
                    if (assignment instanceof BusinessAssignment) {
                        TalendItem talendItem = ((BusinessAssignment) assignment).getTalendItem();
                        if (talendItem != null) {
                            IRepositoryViewObject obj = CorePlugin.getDefault().getProxyRepositoryFactory()
                                    .getLastVersion(talendItem.getId());
                            if (obj != null) {
                                ERepositoryObjectType type = obj.getRepositoryObjectType();
                                Item item = obj.getProperty().getItem();
                                if (item instanceof JobletProcessItem) {
                                    JobletProcessItem jobletItem = (JobletProcessItem) item;
                                    Image jobletCustomIcon = RepositoryLabelProvider
                                            .getJobletCustomIcon(jobletItem.getProperty());
                                    if (jobletCustomIcon != null) {
                                        img = ImageUtils.scale(jobletCustomIcon, ICON_SIZE.ICON_16);
                                    }

                                } else {
                                    img = CoreImageProvider.getImage(type);
                                }
                                label = new Label(obj.getLabel() + " (" + provider.getColumnText(assignment, 0) + ")", img); //$NON-NLS-1$ //$NON-NLS-2$
                                labels.add(label);
                            } else {

                                MetadataTable table = MetadataToolHelper.getMetadataTableFromRepository(talendItem.getId());
                                Query query = MetadataToolHelper.getQueryFromRepository(talendItem.getId());
                                SAPFunctionUnit function = MetadataToolHelper.getSAPFunctionFromRepository(talendItem.getId());
                                if (table != null) {
                                    img = ImageDescriptor.createFromFile(ECoreImage.class,
                                            ECoreImage.METADATA_TABLE_ICON.getPath()).createImage();
                                    label = new Label(
                                            talendItem.getLabel() + " (" + provider.getColumnText(assignment, 0) + ")", img); //$NON-NLS-1$ //$NON-NLS-2$
                                    labels.add(label);
                                } else if (query != null) {
                                    img = ImageDescriptor.createFromFile(ECoreImage.class,
                                            ECoreImage.METADATA_QUERY_ICON.getPath()).createImage();
                                    label = new Label(
                                            talendItem.getLabel() + " (" + provider.getColumnText(assignment, 0) + ")", img); //$NON-NLS-1$ //$NON-NLS-2$
                                    labels.add(label);
                                } else if (talendItem instanceof SQLPattern) {

                                    List<IRepositoryViewObject> list = ProxyRepositoryFactory.getInstance().getAll(
                                            ERepositoryObjectType.SQLPATTERNS);
                                    for (IRepositoryViewObject object : list) {
                                        if (talendItem.getLabel().equals(object.getLabel())) {
                                            img = ImageDescriptor.createFromFile(ECoreImage.class,
                                                    ECoreImage.METADATA_SQLPATTERN_ICON.getPath()).createImage();
                                            label = new Label(talendItem.getLabel() + " (" //$NON-NLS-1$
                                                    + provider.getColumnText(assignment, 0) + ")", img); //$NON-NLS-1$
                                            labels.add(label);
                                        }
                                    }

                                } else if (talendItem instanceof Routine) {
                                    List<IRepositoryViewObject> list = ProxyRepositoryFactory.getInstance().getAll(
                                            ERepositoryObjectType.ROUTINES);
                                    for (IRepositoryViewObject object : list) {
                                        if (talendItem.getLabel().equals(object.getLabel())) {
                                            img = ImageDescriptor.createFromFile(ECoreImage.class,
                                                    ECoreImage.ROUTINE_ICON.getPath()).createImage();
                                            label = new Label(talendItem.getLabel() + " (" //$NON-NLS-1$
                                                    + provider.getColumnText(assignment, 0) + ")", img); //$NON-NLS-1$
                                            labels.add(label);
                                        }
                                    }
                                } else if (function != null) {
                                    img = ImageDescriptor.createFromFile(ECoreImage.class,
                                            ECoreImage.METADATA_SAPCONNECTION_ICON.getPath()).createImage();
                                    label = new Label(
                                            talendItem.getLabel() + " (" + provider.getColumnText(assignment, 0) + ")", img); //$NON-NLS-1$ //$NON-NLS-2$
                                    labels.add(label);

                                } else {
                                    img = (Image) provider.getImage(assignment);
                                    img = new OverlayImage(img, ImageProvider.getImageDesc(ECoreImage.DELETED_OVERLAY),
                                            EPosition.BOTTOM_RIGHT).createImage();
                                    String text = provider.getText(assignment)
                                            + " (" + provider.getColumnText(assignment, 0) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
                                    label = new Label(text, img);
                                    labels.add(label);

                                }

                            }
                        }
                    }

                }

                if (tooltipFigure == null) {
                    tooltipFigure = new BusinessTooltipFigure();
                }
                if (labels.size() == 0) {
                    figure.setToolTip(null);
                } else {
                    tooltipFigure.buildFigures(labels);
                    figure.setToolTip(tooltipFigure);
                }

            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            }
        }
    }

}
