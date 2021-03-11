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
package org.talend.sqlbuilder.sqleditor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

/**
 * Detailled comment for this class. <br/> $Id: AbstractSQLScanner.java,v 1.2 2006/10/31 09:54:18 peiqin.hou Exp $
 *
 */

public abstract class AbstractSQLScanner extends BufferedRuleBasedScanner {

    private IColorManager fColorManager;

    private IPreferenceStore fPreferenceStore;

    private final Map fTokenMap = new HashMap();

    private String[] fPropertyNamesColor;

    private String[] fPropertyNamesStyle;

    /**
     * Returns the list of preference keys which define the tokens used in the rules of this scanner.
     *
     * @return String[]
     */
    protected abstract String[] getTokenProperties();

    /**
     * Creates the list of rules controlling this scanner.
     *
     * @return List
     */
    protected abstract List createRules();

    /**
     * Creates an abstract Java scanner.
     */
    public AbstractSQLScanner(final IColorManager manager, final IPreferenceStore store) {
        super();
        fColorManager = manager;
        fPreferenceStore = store;
    }

    /**
     * Must be called after the constructor has been called.
     */
    public final void initialize() {

        fPropertyNamesColor = getTokenProperties();
        final int length = fPropertyNamesColor.length;
        fPropertyNamesStyle = new String[length];
        for (int i = 0; i < length; i++) {
            fPropertyNamesStyle[i] = fPropertyNamesColor[i] + "_bold"; //$NON-NLS-1$
            addToken(fPropertyNamesColor[i], fPropertyNamesStyle[i]);
        }

        initializeRules();
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private void addToken(final String colorKey, final String styleKey) {
        final RGB rgb = PreferenceConverter.getColor(fPreferenceStore, colorKey);
        if (fColorManager instanceof IColorManagerExtension) {
            final IColorManagerExtension ext = (IColorManagerExtension) fColorManager;
            ext.unbindColor(colorKey);
            ext.bindColor(colorKey, rgb);
        }

        final boolean bold = fPreferenceStore.getBoolean(styleKey);
        fTokenMap.put(colorKey, new Token(new TextAttribute(fColorManager.getColor(colorKey), null, bold ? SWT.BOLD
                : SWT.NORMAL)));
    }

    protected Token getToken(final String key) {
        return (Token) fTokenMap.get(key);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private void initializeRules() {
        final List rules = createRules();
        if (rules != null) {
            final IRule[] result = new IRule[rules.size()];
            rules.toArray(result);
            setRules(result);
        }
    }

    private int indexOf(final String property) {
        if (property != null) {
            final int length = fPropertyNamesColor.length;
            for (int i = 0; i < length; i++) {
                if (property.equals(fPropertyNamesColor[i]) || property.equals(fPropertyNamesStyle[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean affectsBehavior(final PropertyChangeEvent event) {
        return indexOf(event.getProperty()) >= 0;
    }

    public void adaptToPreferenceChange(final PropertyChangeEvent event) {
        final String p = event.getProperty();
        final int index = indexOf(p);
        final Token token = getToken(fPropertyNamesColor[index]);
        if (fPropertyNamesColor[index].equals(p)) {
            adaptToColorChange(token, event);
        } else {
            adaptToStyleChange(token, event);
        }
    }

    private void adaptToColorChange(final Token token, final PropertyChangeEvent event) {
        RGB rgb = null;

        final Object value = event.getNewValue();
        if (value instanceof RGB) {
            rgb = (RGB) value;
        } else if (value instanceof String) {
            rgb = StringConverter.asRGB((String) value);
        }

        if (rgb != null) {

            final String property = event.getProperty();

            if (fColorManager instanceof IColorManagerExtension) {
                final IColorManagerExtension ext = (IColorManagerExtension) fColorManager;
                ext.unbindColor(property);
                ext.bindColor(property, rgb);
            }

            final Object data = token.getData();
            if (data instanceof TextAttribute) {
                final TextAttribute oldAttr = (TextAttribute) data;
                token.setData(new TextAttribute(fColorManager.getColor(property), oldAttr.getBackground(), oldAttr
                        .getStyle()));
            }
        }
    }

    private void adaptToStyleChange(final Token token, final PropertyChangeEvent event) {
        boolean bold = false;
        final Object value = event.getNewValue();
        if (value instanceof Boolean) {
            bold = ((Boolean) value).booleanValue();
        } else if (value instanceof String) {
            final String s = (String) value;
            if (IPreferenceStore.TRUE.equals(s)) {
                bold = true;
            } else if (IPreferenceStore.FALSE.equals(s)) {
                bold = false;
            }
        }

        final Object data = token.getData();
        if (data instanceof TextAttribute) {
            final TextAttribute oldAttr = (TextAttribute) data;
            final boolean isBold = (oldAttr.getStyle() == SWT.BOLD);
            if (isBold != bold) {
                token.setData(new TextAttribute(oldAttr.getForeground(), oldAttr.getBackground(), bold ? SWT.BOLD
                        : SWT.NORMAL));
            }
        }
    }
}
