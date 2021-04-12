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
package org.talend.designer.gefabstractmap.figures.var;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.CompoundBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.metadata.types.JavaType;
import org.talend.core.model.metadata.types.JavaTypesManager;
import org.talend.designer.gefabstractmap.figures.ComboCellLabel;
import org.talend.designer.gefabstractmap.figures.VarNodeExpression;
import org.talend.designer.gefabstractmap.figures.VarNodeTextLabel;
import org.talend.designer.gefabstractmap.figures.borders.ColumnBorder;
import org.talend.designer.gefabstractmap.figures.borders.RowBorder;
import org.talend.designer.gefabstractmap.figures.manager.TableEntityManager;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityElement;
import org.talend.designer.gefabstractmap.figures.table.entity.TableEntityFigure;
import org.talend.designer.gefabstractmap.part.directedit.DirectEditType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public abstract class VarEntityFigure extends TableEntityFigure {

    protected Label expression;

    private ComboCellLabel type;

    private ImageFigure checkImage;

    protected VarNodeTextLabel variableLabel;

    public VarEntityFigure(TableEntityManager entityManager) {
        super(entityManager);
    }

    @Override
    protected void createEntityItems(TableEntityElement entityElement) {
        expression = new VarNodeExpression();
        expression.setText(getExpressionText());

        type = new ComboCellLabel();
        type.setDirectEditType(DirectEditType.VAR_NODE_TYPE);
        type.setText(getTypeDisplayValue(getType(), isNullable()));
        type.setLabelAlignment(PositionConstants.LEFT);

        type.setBorder(new CompoundBorder(new ColumnBorder(), new RowBorder(2, 5, 2, -1)));

        entityElement.add(expression);
        entityElement.add(type);
        entityElement.add(createVariableContainer());
    }

    protected Figure createVariableContainer() {
        Figure container = new Figure();
        // container.setBorder(new RowBorder());
        ToolbarLayout manager = new ToolbarLayout() {

            @Override
            public void layout(IFigure parent) {
                super.layout(parent);
                List children = parent.getChildren();
                if (children.size() == 2) {
                    Figure checkImageFigure = (Figure) children.get(0);
                    Figure variableFigure = (Figure) children.get(1);
                    Rectangle varFigureBounds = variableFigure.getBounds().getCopy();
                    int avialableWith = parent.getBounds().width - checkImageFigure.getBounds().width;
                    if (avialableWith > varFigureBounds.width) {
                        varFigureBounds.width = avialableWith;
                    }
                    variableFigure.setBounds(varFigureBounds);
                }

            }
        };
        manager.setVertical(false);
        manager.setStretchMinorAxis(false);
        container.setLayoutManager(manager);
        // if(varNode.)
        Label nullableToolTip = new Label("Nullable");
        if (!isNullable()) {
            checkImage = new ImageFigure(ImageProvider.getImage(EImage.UNCHECKED_ICON));
        } else {
            checkImage = new ImageFigure(ImageProvider.getImage(EImage.CHECKED_ICON));
        }
        checkImage.setToolTip(nullableToolTip);
        variableLabel = new VarNodeTextLabel();
        variableLabel.setText(getVarName());
        variableLabel.setLabelAlignment(PositionConstants.LEFT);
        variableLabel.setBorder(new MarginBorder(2, 5, 2, -1));

        container.add(checkImage);
        container.add(variableLabel);
        addCheckListener();
        return container;
    }

    private void addCheckListener() {
        checkImage.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent me) {

                checkImage.setBackgroundColor(ColorConstants.button);
            }

            @Override
            public void mousePressed(MouseEvent me) {
                boolean isCheck = checkImage.getImage().equals(ImageProvider.getImage(EImage.CHECKED_ICON)) ? false : true;
                setNullable(isCheck);
                // varNode.setNUllable();
                checkImage.setBackgroundColor(ColorConstants.buttonDarkest);

            }

            @Override
            public void mouseDoubleClicked(MouseEvent me) {

            }
        });
    }

    private String getTypeDisplayValue(String type, boolean nullable) {
        JavaType javaType = JavaTypesManager.getJavaTypeFromId(type);
        Class primitiveClass = javaType.getPrimitiveClass();
        String displayedValue = null;
        if (primitiveClass != null && !Boolean.TRUE.equals(nullable)) {
            displayedValue = primitiveClass.getSimpleName();
        } else if (type.equals(JavaTypesManager.DIRECTORY.getId()) || type.equals(JavaTypesManager.FILE.getId())
                || type.equals(JavaTypesManager.VALUE_LIST.getId())) {
            displayedValue = javaType.getLabel();
        } else {
            displayedValue = javaType.getNullableClass().getSimpleName();
        }
        if (displayedValue == null) {
            displayedValue = JavaTypesManager.getDefaultJavaType().getLabel();
        }
        return displayedValue;
    }

    public Label getExpression() {
        return this.expression;
    }

    public ComboCellLabel getTypeFigure() {
        return type;
    }

    public void updateVarNodeType(String type, boolean nullable) {
        this.type.setText(getTypeDisplayValue(type, nullable));
    }

    public VarNodeTextLabel getVariableLabel() {
        return this.variableLabel;
    }

    public ImageFigure getCheckImage() {
        return this.checkImage;
    }

    public abstract void setNullable(boolean isCheck);

    public abstract boolean isNullable();

    public abstract String getVarName();

    public abstract String getExpressionText();

    public abstract String getType();

}
