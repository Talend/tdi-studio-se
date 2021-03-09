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
package org.talend.designer.core.ui.viewer.proposal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 *
 */
public class TalendCompletionProposal implements ICompletionProposal {

    public static final String NODE_RETURN = "NODE_RETURN"; //$NON-NLS-1$

    public static final String ROUTINE = "ROUTINE"; //$NON-NLS-1$

    public static final String SNIPPET = "SNIPPET";//$NON-NLS-1$

    public static final String CONTEXT = "CONTEXT"; //$NON-NLS-1$

    public static final String VARIABLE = "VARIABLE"; //$NON-NLS-1$

    /** The string to be displayed in the completion proposal popup. */
    private String fDisplayString;

    /** The replacement string. */
    private String fReplacementString;

    /** The replacement offset. */
    protected int fReplacementOffset;

    /** The replacement length. */
    protected int fReplacementLength;

    /** The cursor position after this proposal has been applied. */
    private int fCursorPosition;

    /** The image to be displayed in the completion proposal popup. */
    private Image fImage;

    /** The context information of this proposal. */
    private IContextInformation fContextInformation;

    /** The additional info of this proposal. */
    private String fAdditionalProposalInfo;

    private String type;

    /**
     * Creates a new completion proposal based on the provided information. The replacement string is considered being
     * the display string too. All remaining fields are set to <code>null</code>.
     *
     * @param replacementString the actual string to be inserted into the document
     * @param replacementOffset the offset of the text to be replaced
     * @param replacementLength the length of the text to be replaced
     * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
     */
    public TalendCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition) {
        this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
    }

    /**
     * Creates a new completion proposal. All fields are initialized based on the provided information.
     *
     * @param replacementString the actual string to be inserted into the document
     * @param replacementOffset the offset of the text to be replaced
     * @param replacementLength the length of the text to be replaced
     * @param cursorPosition the position of the cursor following the insert relative to replacementOffset
     * @param image the image to display for this proposal
     * @param displayString the string to be displayed for the proposal
     * @param contextInformation the context information associated with this proposal
     * @param additionalProposalInfo the additional information associated with this proposal
     */
    public TalendCompletionProposal(String replacementString, int replacementOffset, int replacementLength, int cursorPosition,
            Image image, String displayString, IContextInformation contextInformation, String additionalProposalInfo) {
        Assert.isNotNull(replacementString);
        Assert.isTrue(replacementOffset >= 0);
        Assert.isTrue(replacementLength >= 0);
        Assert.isTrue(cursorPosition >= 0);

        fReplacementString = replacementString;
        fReplacementOffset = replacementOffset;
        fReplacementLength = replacementLength;
        fCursorPosition = cursorPosition;
        fImage = image;
        fDisplayString = displayString;
        fContextInformation = contextInformation;
        fAdditionalProposalInfo = additionalProposalInfo;
    }

    /*
     * @see ICompletionProposal#apply(IDocument)
     */
    public void apply(IDocument document) {
        try {
            document.replace(fReplacementOffset, fReplacementLength, fReplacementString);
        } catch (BadLocationException e) {
            ExceptionHandler.process(e);
        }
    }

    /*
     * @see ICompletionProposal#getSelection(IDocument)
     */
    public Point getSelection(IDocument document) {
        return new Point(fReplacementOffset + fCursorPosition, 0);
    }

    /*
     * @see ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation() {
        return fContextInformation;
    }

    /*
     * @see ICompletionProposal#getImage()
     */
    public Image getImage() {
        return fImage;
    }

    /*
     * @see ICompletionProposal#getDisplayString()
     */
    public String getDisplayString() {
        if (fDisplayString != null)
            return fDisplayString;
        return fReplacementString;
    }

    /*
     * @see ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo() {
        return fAdditionalProposalInfo;
    }

    /**
     * Getter for type.
     *
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type.
     *
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return fDisplayString;
    }
}
