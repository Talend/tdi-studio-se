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
package org.talend.designer.dbmap.ui.visualmap.link;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.talend.commons.ui.swt.drawing.link.BezierHorizontalLink;
import org.talend.commons.ui.swt.drawing.link.ExtremityEastArrow;
import org.talend.commons.ui.swt.drawing.link.ExtremityWestArrow;
import org.talend.commons.ui.swt.drawing.link.IDrawableLink;
import org.talend.commons.ui.swt.drawing.link.IStyleLink;
import org.talend.commons.ui.swt.drawing.link.LineLinkWithHorizontalConnectors;
import org.talend.commons.ui.swt.drawing.link.StyleLink;
import org.talend.commons.ui.swt.drawing.link.VerticalRoundedCornerLink;
import org.talend.designer.abstractmap.model.tableentry.ITableEntry;
import org.talend.designer.abstractmap.ui.properties.LINK_STYLE;
import org.talend.designer.abstractmap.ui.visualmap.link.ILinkState;
import org.talend.designer.abstractmap.ui.visualmap.link.IMapperLink;
import org.talend.designer.abstractmap.ui.visualmap.link.LinkState;
import org.talend.designer.abstractmap.ui.visualmap.link.PointLinkDescriptor;
import org.talend.designer.dbmap.model.tableentry.FilterTableEntry;
import org.talend.designer.dbmap.ui.color.ColorInfo;
import org.talend.designer.dbmap.ui.color.ColorProviderMapper;
import org.talend.designer.dbmap.ui.visualmap.zone.Zone;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 * $Id$
 *
 */
public class StyleLinkFactory {

    // private static DrawableLinkFactory instance;

    private IStyleLink selectedZoneToZoneStyle;

    private IStyleLink selectedFilterStyle;

    private IStyleLink unselectedFilterStyle;

    private IStyleLink selectedSameInputZoneStyle;

    private IStyleLink unselectedSameZoneStyle;

    private IStyleLink unselectedZoneToZoneStyle;

    private LINK_STYLE linkStyle;

    /**
     * DOC amaumont LinkFactory constructor comment.
     */
    public StyleLinkFactory(LINK_STYLE linkStyle) {
        super();
        this.linkStyle = linkStyle;
        init();
    }

    /**
     * DOC amaumont Comment method "init".
     */
    private void init() {

        selectedZoneToZoneStyle = getSelectedZoneToOtherZoneStyle(ColorProviderMapper
                .getColor(ColorInfo.COLOR_SELECTED_ZONE_TO_ZONE_LINK));
        unselectedZoneToZoneStyle = getUnselectedZoneToZoneStyle();

        selectedFilterStyle = getSelectedFilterStyle();
        unselectedFilterStyle = getUnselectedFilterStyle();

        selectedSameInputZoneStyle = getSelectedSameInputZoneStyle();
        unselectedSameZoneStyle = getUnselectedSameZoneStyle();

    }

    // public static DrawableLinkFactory getInstance() {
    // if (instance == null) {
    // instance = new DrawableLinkFactory();
    // }
    // return instance;
    // }
    //
    // public IDrawableLink getDrawableLink(IMapperLink link) {
    // IDrawableLink drawableLink = null;
    // PointLinkDescriptor pointLinkDescriptorSource = link.getPointLinkDescriptorSource();
    // PointLinkDescriptor pointLinkDescriptorTarget = link.getPointLinkDescriptorTarget();
    // if (pointLinkDescriptorSource.getZone() != pointLinkDescriptorTarget.getZone()) {
    // drawableLink = new HorizontalBezierLink();
    // } else if (pointLinkDescriptorSource.getZone() == pointLinkDescriptorTarget.getZone()) {
    // drawableLink = new VerticalRoundedCornerLink();
    // }
    // return drawableLink;
    // }
    //
    public IStyleLink getStyleLink(IMapperLink link) {
        ILinkState linkState = link.getState();
        PointLinkDescriptor pointLinkDescriptorSource = link.getPointLinkDescriptor1();
        PointLinkDescriptor pointLinkDescriptorTarget = link.getPointLinkDescriptor2();
        ITableEntry targetTableEntry = pointLinkDescriptorTarget.getTableEntry();

        boolean targetIsConstraint = false;
        if (targetTableEntry instanceof FilterTableEntry) {
            targetIsConstraint = true;
        }

        IStyleLink style = null;
        if (pointLinkDescriptorSource.getZone() != pointLinkDescriptorTarget.getZone()) {
            if (linkState == LinkState.SELECTED) {
                if (targetIsConstraint) {
                    style = selectedFilterStyle;
                } else {
                    style = selectedZoneToZoneStyle;
                }
            } else if (linkState == LinkState.UNSELECTED) {
                if (targetIsConstraint) {
                    style = unselectedFilterStyle;
                } else {
                    style = unselectedZoneToZoneStyle;
                }
            }
        } else if ((pointLinkDescriptorSource.getZone() == Zone.INPUTS || pointLinkDescriptorSource.getZone() == Zone.OUTPUTS)
                && pointLinkDescriptorSource.getZone() == pointLinkDescriptorTarget.getZone()) {
            if (linkState == LinkState.SELECTED) {
                style = selectedSameInputZoneStyle;
            } else if (linkState == LinkState.UNSELECTED) {
                style = unselectedSameZoneStyle;
            }
        }
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedFilterStyle".
     *
     * @return
     */
    private IStyleLink getSelectedFilterStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getZoneToZoneLink(style));
        ExtremityEastArrow eastArrowSource = new ExtremityEastArrow(style);
        style.setExtremity1(eastArrowSource);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_FILTER_LINK));
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedFilterStyle".
     *
     * @return
     */
    private IStyleLink getUnselectedFilterStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getZoneToZoneLink(style));
        // ExtremityEastArrow eastArrowSource = new ExtremityEastArrow(style);
        // style.setExtremity1(eastArrowSource);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_FILTER_LINK));
        return style;
    }

    private IStyleLink getSelectedZoneToOtherZoneStyle(Color foregroundColor) {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getZoneToZoneLink(style));
        ExtremityEastArrow eastArrowSource = new ExtremityEastArrow(style);
        style.setExtremity1(eastArrowSource);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(foregroundColor);
        return style;
    }

    /**
     * DOC amaumont Comment method "getNotSelectedSameZoneStyle".
     *
     * @return
     */
    private IStyleLink getUnselectedZoneToZoneStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getZoneToZoneLink(style));
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_ZONE_TO_ZONE_LINK));
        return style;
    }

    /**
     * DOC amaumont Comment method "getNotSelectedSameZoneStyle".
     *
     * @return
     */
    private IStyleLink getUnselectedSameZoneStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        style.setDrawableLink(getSameZoneLink(style));
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_UNSELECTED_LOOKUP_LINKS));
        return style;
    }

    /**
     * DOC amaumont Comment method "getSelectedSameZoneStyle".
     *
     * @param foregroundColor
     * @return
     */
    public IStyleLink getSelectedSameInputZoneStyle() {
        StyleLink style = new StyleLink();
        setCommonsStyleProperties(style);
        IDrawableLink sameZoneLink = getSameZoneLink(style);
        style.setDrawableLink(sameZoneLink);
        ExtremityWestArrow westArrow = new ExtremityWestArrow(style);
        style.setExtremity1(westArrow);
        ExtremityEastArrow eastArrowTarget = new ExtremityEastArrow(style);
        eastArrowTarget.setXOffset(-eastArrowTarget.getSize().x);
        style.setExtremity2(eastArrowTarget);
        style.setForegroundColor(ColorProviderMapper.getColor(ColorInfo.COLOR_SELECTED_LOOKUP_LINKS));
        return style;
    }

    /**
     * DOC amaumont Comment method "setCommonsStyleProperties".
     *
     * @param style
     */
    private void setCommonsStyleProperties(StyleLink style) {
        style.setLineWidth(2);
        style.setLineCap(SWT.CAP_SQUARE);
        style.setLineJoin(SWT.JOIN_ROUND);
        // style.setLineStyle(SWT.LINE_DASH);
        // style.setLineDash(SWT.JOIN_BEVEL);
    }

    /**
     * DOC amaumont Comment method "getSameZoneLink".
     *
     * @param style
     * @return
     */
    private IDrawableLink getSameZoneLink(StyleLink style) {
        return new VerticalRoundedCornerLink(style);
    }

    /**
     * DOC amaumont Comment method "getZoneToZoneLink".
     *
     * @return
     */
    private IDrawableLink getZoneToZoneLink(StyleLink style) {
        if (linkStyle == LINK_STYLE.BEZIER_CURVE) {
            return new BezierHorizontalLink(style);
        } else {
            return new LineLinkWithHorizontalConnectors(style);
        }
    }

}
