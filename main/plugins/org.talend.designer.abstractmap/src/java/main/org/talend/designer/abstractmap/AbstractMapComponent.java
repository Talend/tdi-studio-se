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
package org.talend.designer.abstractmap;

import java.util.HashMap;
import java.util.Map;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.Perl5Substitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.context.UpdateContextVariablesHelper;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.node.IExternalMapEntry;
import org.talend.core.model.process.node.MapperExternalNode;
import org.talend.designer.abstractmap.ui.prefs.MapPrefsConstants;

/**
 * DOC amaumont class global comment. Detailled comment <br/>
 *
 */
public abstract class AbstractMapComponent extends MapperExternalNode {

    /**
     * DOC amaumont AbstractMapComponent constructor comment.
     */
    public AbstractMapComponent() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.IExternalNode#initialize()
     */
    @Override
    public void initialize() {
        initElementParameters();
    }

    /**
     * DOC amaumont Comment method "initElementParameters".
     */
    private void initElementParameters() {
        IElementParameter elementParameter = getElementParameter(MapPrefsConstants.LINK_STYLE);
        if (elementParameter != null && ((String) elementParameter.getValue()).trim().equals("")) { //$NON-NLS-1$
            elementParameter.setValue(MapPlugin.getDefault().getPreferenceStore().getString(MapPrefsConstants.LINK_STYLE));
        }
    }

    /**
     * Restore mapper model from internal stored data.
     */
    public void restoreMapperModelFromInternalData() {

    }

    protected String renameDataIntoExpression(Pattern pattern, PatternMatcher matcher, Substitution substitution,
            String expression) {
        String replacedExpression = Util.substitute(matcher, pattern, substitution, expression, Util.SUBSTITUTE_ALL);
        return replacedExpression;
    }

    protected boolean hasDataIntoExpression(Pattern pattern, PatternMatcher matcher, String expression) {
        if (expression != null) {
            if (matcher.matches(expression, pattern)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#useData(java.lang.String)
     */
    @Override
    public boolean useData(String name) {
        if (super.useData(name)) {
            return true;
        }
        if (hasOrRenameData(name, null, false)) {
            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.model.process.INode#renameData(java.lang.String, java.lang.String)
     */
    @Override
    public void renameData(String oldName, String newName) {
        super.renameData(oldName, newName);

        hasOrRenameData(oldName, newName, true);

    }

    private static Map<String, Pattern> patternsCache = new HashMap<String, Pattern>();

    protected final Pattern getRenamePattern(String oldName) {
        if (patternsCache.containsKey(oldName)) {
            return patternsCache.get(oldName);
        }
        PatternCompiler compiler = new Perl5Compiler();
        Pattern pattern = null;
        try {
            pattern = compiler.compile("\\b(" + UpdateContextVariablesHelper.replaceSpecialChar(oldName) + ")(\\b|\\_)"); //$NON-NLS-1$ //$NON-NLS-2$
            patternsCache.put(oldName, pattern);
            return pattern;
        } catch (MalformedPatternException e) {
            ExceptionHandler.process(e);
            return null;
        }
    }

    private static Map<String, Perl5Substitution> substitutionsCache = new HashMap<String, Perl5Substitution>();

    protected final Perl5Substitution getRenameSubstitution(String newName) {
        if (substitutionsCache.containsKey(newName)) {
            return substitutionsCache.get(newName);
        }
        Perl5Substitution ps = new Perl5Substitution(newName + "$2", Perl5Substitution.INTERPOLATE_ALL); //$NON-NLS-1$
        substitutionsCache.put(newName, ps);
        return ps;
    }

    /**
     *
     * ggu Comment method "hasOrRenameEntry".
     *
     */
    protected boolean hasOrRenameEntry(IExternalMapEntry entry, String oldName, String newName, boolean renameAction) {
        if (entry == null || oldName == null || newName == null && renameAction) {
            throw new NullPointerException();
        }

        if (entry.getExpression() != null) {
            Pattern pattern = getRenamePattern(oldName);
            if (pattern != null) {
                PatternMatcher matcher = new Perl5Matcher();
                ((Perl5Matcher) matcher).setMultiline(true);

                if (renameAction) {
                    Perl5Substitution substitution = getRenameSubstitution(newName);
                    String expression = renameDataIntoExpression(pattern, matcher, substitution, entry.getExpression());
                    entry.setExpression(expression);
                } else {
                    if (hasDataIntoExpression(pattern, matcher, entry.getExpression())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected abstract boolean hasOrRenameData(String oldName, String newName, boolean renameAction);

}
