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

//import org.eclipse.jdt.internal.ui.text.HTMLTextPresenter;
import net.sourceforge.sqlexplorer.IConstants;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;

/**
 * SQLSourceViewerConfiguration.
 * <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public class SQLSourceViewerConfiguration extends SourceViewerConfiguration {

	InformationPresenter iPresenter;

	private SQLTextTools fSQLTextTools;

	private IDocumentPartitioner docPartitioner;

	private DefaultTextDoubleClickStrategy defaultTextDoubleClickStrategy = new DefaultTextDoubleClickStrategy();

	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer, String contentType) {
		return defaultTextDoubleClickStrategy;

	}

	/**
	 * Creates a new SQL source viewer configuration for viewers in the given
	 * editor using the given SQLTextTools.
	 *
	 * @param tools
	 *            the SQL tools to be used
	 */
	public SQLSourceViewerConfiguration(SQLTextTools tools) {
		fSQLTextTools = tools;
		docPartitioner = tools.createDocumentPartitioner();
	}

	public IDocumentPartitioner getDocumentPartitioner() {
		return docPartitioner;
	}

	public RuleBasedScanner getCodeScanner() {
		return fSQLTextTools.getCodeScanner();
	}

	protected RuleBasedScanner getMultilineCommentScanner() {
		return fSQLTextTools.getMultilineCommentScanner();
	}

	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fSQLTextTools.getSinglelineCommentScanner();
	}

	protected RuleBasedScanner getStringScanner() {
		return fSQLTextTools.getStringScanner();
	}

	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {

		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getMultilineCommentScanner());
		reconciler.setDamager(dr, IConstants.SQL_MULTILINE_COMMENT);
		reconciler.setRepairer(dr, IConstants.SQL_MULTILINE_COMMENT);

		dr = new DefaultDamagerRepairer(getSinglelineCommentScanner());
		reconciler.setDamager(dr, IConstants.SQL_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, IConstants.SQL_SINGLE_LINE_COMMENT);

		dr = new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, IConstants.SQL_STRING);
		reconciler.setRepairer(dr, IConstants.SQL_STRING);

		return reconciler;
	}

	public int getTabWidth(ISourceViewer sourceViewer) {
		return 4;
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

		ContentAssistant assistant = new ContentAssistant() {
			public void uninstall() {
				SQLCompletionProcessor p1 = (SQLCompletionProcessor) getContentAssistProcessor(IConstants.SQL_STRING);
				SQLCompletionProcessor p2 = (SQLCompletionProcessor) getContentAssistProcessor(IConstants.SQL_SINGLE_LINE_COMMENT);
				p1.dispose();
				p2.dispose();
				super.uninstall();
			}
		};

		SQLCompletionProcessor processor = new SQLCompletionProcessor(
				fSQLTextTools.getDictionary());
		assistant.setContentAssistProcessor(processor,
				IDocument.DEFAULT_CONTENT_TYPE);
		// Register the same processor for strings and single line comments to
		// get code completion at the start of those partitions.
		assistant.setContentAssistProcessor(processor, IConstants.SQL_STRING);
		assistant.setContentAssistProcessor(processor,
				IConstants.SQL_SINGLE_LINE_COMMENT);
		assistant.enableAutoActivation(true);

		assistant.setAutoActivationDelay(500);
		assistant.enableAutoInsert(true);
		assistant.enableAutoActivation(true);
		processor
				.setCompletionProposalAutoActivationCharacters(".".toCharArray()); //$NON-NLS-1$

		/*
		 * assistant.setProposalSelectorForeground(c); c= getColor(store,
		 * PROPOSALS_BACKGROUND, manager);
		 * assistant.setProposalSelectorBackground(c);
		 *
		 * c= getColor(store, PARAMETERS_FOREGROUND, manager);
		 * assistant.setContextInformationPopupForeground(c);
		 * assistant.setContextSelectorForeground(c);
		 *
		 * c= getColor(store, PARAMETERS_BACKGROUND, manager);
		 * assistant.setContextInformationPopupBackground(c);
		 * assistant.setContextSelectorBackground(c);
		 */

		// ContentAssistPreference.configure(assistant, getPreferenceStore());
		assistant
				.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_ABOVE);
		assistant
				.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getInformationPresenter(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IInformationPresenter getInformationPresenter(
			ISourceViewer sourceViewer) {
		if (iPresenter == null) {
			IInformationControlCreator informationControlCreator = new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					// boolean cutDown= false;
					// int style= cutDown ? SWT.NONE : (SWT.V_SCROLL |
					// SWT.H_SCROLL);
					return new DefaultInformationControl(parent);
																	// SWT.RESIZE,,
																	// style,
																	// new
																	// HTMLTextPresenter(cutDown));
				}
			};

			iPresenter = new InformationPresenter(informationControlCreator);
			iPresenter.setSizeConstraints(60, 10, true, true);

		}
		return iPresenter;

	}

}
