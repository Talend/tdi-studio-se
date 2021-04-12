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
package org.talend.designer.xmlmap.figures;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.emf.common.util.EList;
import org.talend.designer.gefabstractmap.figures.ExpressionFigure;
import org.talend.designer.gefabstractmap.figures.TextCellLabel;
import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityElement;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityFigure;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;
import org.talend.designer.gefabstractmap.resource.ImageInfo;
import org.talend.designer.gefabstractmap.resource.ImageProviderMapper;
import org.talend.designer.xmlmap.figures.table.GlobalMapKeysTable;
import org.talend.designer.xmlmap.figures.treeNode.GlobalMapNodeEntityManager;
import org.talend.designer.xmlmap.model.emf.xmlmap.GlobalMapNode;

/**
 * DOC hcyi class global comment. Detailled comment
 */
public class GlobalMapKeysEntityFigure extends TableEntityFigure {

    private Label expression;

    private TextCellLabel globalMapKey;

    private ImageFigure deleteImage;

    private GlobalMapNode globalMapNode;

    public GlobalMapKeysEntityFigure(TableEntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected GlobalMapNodeEntityManager getEntityManager() {
        return (GlobalMapNodeEntityManager) super.getEntityManager();
    }

    @Override
    protected void createEntityItems(TableEntityElement entityElement) {
        this.globalMapNode = getEntityManager().getModel();

        expression = new ExpressionFigure();
        expression.setText(globalMapNode.getExpression());

        globalMapKey = new TextCellLabel();
        globalMapKey.setDirectEditType(DirectEditType.NODE_NAME);
        globalMapKey.setText(globalMapNode.getName());
        globalMapKey.setLabelAlignment(PositionConstants.LEFT);

        deleteImage = new ImageFigure(ImageProviderMapper.getImage(ImageInfo.DELETE));

        entityElement.add(expression);
        entityElement.add(globalMapKey);
        entityElement.add(deleteImage);
        addCheckListener();
    }

    private void addCheckListener() {
        deleteImage.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent me) {
            }

            @Override
            public void mousePressed(MouseEvent me) {
                if (me.getSource() != null && me.getSource() instanceof ImageFigure) {
                    ImageFigure imageFigure = (ImageFigure) me.getSource();
                    GlobalMapKeysTable globalMapKeysTable = getGlobalMapKeysTable(imageFigure);
                    if (globalMapKeysTable != null && globalMapKeysTable.getInputxmlTree() != null) {
                        EList<GlobalMapNode> globalMapKeysValues = globalMapKeysTable.getInputxmlTree().getGlobalMapKeysValues();
                        if (globalMapKeysValues.contains(globalMapNode)) {
                            globalMapKeysValues.remove(globalMapNode);
                        }
                    }
                }
            }

            @Override
            public void mouseDoubleClicked(MouseEvent me) {

            }
        });
    }

    public GlobalMapKeysTable getGlobalMapKeysTable(IFigure iFigure) {
        if (iFigure instanceof GlobalMapKeysTable) {
            return (GlobalMapKeysTable) iFigure;
        } else {
            return getGlobalMapKeysTable(iFigure.getParent());
        }
    }

    public Label getExpression() {
        return this.expression;
    }

    public TextCellLabel getGlobalMapKeyFigure() {
        return this.globalMapKey;
    }

    public ImageFigure getCheckImage() {
        return this.deleteImage;
    }

    public GlobalMapNode getGlobalMapNode() {
        return this.globalMapNode;
    }
}
