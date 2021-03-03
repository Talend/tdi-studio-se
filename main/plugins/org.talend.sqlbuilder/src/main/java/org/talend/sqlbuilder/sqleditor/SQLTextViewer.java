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


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.talend.sqlbuilder.IConstants;
import org.talend.sqlbuilder.SqlBuilderPlugin;
import org.talend.sqlbuilder.sessiontree.model.utility.Dictionary;

/**
 * SQLTextViewer.
 * <br/>
 *
 * $Id: talend-code-templates.xml 1 2006-09-29 17:06:40 +0000 (Fri, 29 Sep 2006) nrousseau $
 *
 */
public class SQLTextViewer extends SourceViewer {

    IPresentationReconciler fPresentationReconciler;

    private IDocumentPartitioner partitioner;

    SQLTextTools sqlTextTools;

    IPreferenceStore store;

    IContentAssistant contentAssistant;

    private Dictionary dictionary;

    SQLSourceViewerConfiguration configuration;

    TextViewerUndoManager undoManager = new TextViewerUndoManager(50);


    public void setDocument(IDocument dc) {

        IDocument previous = this.getDocument();
        if (previous != null) {

            partitioner.disconnect();
        }
        super.setDocument(dc);
        if (dc != null) {

            partitioner.connect(dc);
            dc.setDocumentPartitioner(partitioner);
            undoManager.connect(this);
            this.setUndoManager(undoManager);
        }

    }


    public SQLTextViewer(Composite parent, int style, IPreferenceStore store, final Dictionary dictionary, IVerticalRuler ruler) {

        super(parent, ruler, style);
        this.store = store;
        this.dictionary = dictionary;

        sqlTextTools = new SQLTextTools(store, dictionary);
        this.getControl().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent event) {
                sqlTextTools.dispose();
                fPresentationReconciler.uninstall();
            }
        });
        store.addPropertyChangeListener(fPreferenceListener);
        FontData[] fData = PreferenceConverter.getFontDataArray(store, IConstants.FONT);

        if (fData.length > 0) {
            JFaceResources.getFontRegistry().put(fData[0].toString(), fData);
            this.getControl().setFont(JFaceResources.getFontRegistry().get(fData[0].toString()));
        }

        configuration = new SQLSourceViewerConfiguration(sqlTextTools);

        fPresentationReconciler = configuration.getPresentationReconciler(null);
        if (dictionary != null) {
            contentAssistant = configuration.getContentAssistant(null);
            if (contentAssistant != null) {
                contentAssistant.install(this);
            }

        }

        if (fPresentationReconciler != null) {
            fPresentationReconciler.install(this);
        }
        partitioner = configuration.getDocumentPartitioner();
        parent.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent event) {
                if (dictionary != null) {
                    if (contentAssistant != null) {
                        try {
                            contentAssistant.uninstall();
                        } catch (Exception e) {
                            SqlBuilderPlugin.log("", e); //$NON-NLS-1$
                        }
                    }
                }
            }
        });
        fInformationPresenter = configuration.getInformationPresenter(this);

        if (fInformationPresenter != null) {
        	fInformationPresenter.install(this);
        }
        this.setAnnotationHover(new IAnnotationHover() {

            public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {

                return "hover info"; //$NON-NLS-1$
            }
        });
        setHoverControlCreator(new IInformationControlCreator() {

            public IInformationControl createInformationControl(Shell parent) {
                return new DefaultInformationControl(parent);
            }
        });

        String[] contentTypes = {IDocument.DEFAULT_CONTENT_TYPE, IConstants.SQL_SINGLE_LINE_COMMENT, IConstants.SQL_STRING,
                IConstants.SQL_MULTILINE_COMMENT};
        for (int i = 0; i < contentTypes.length; i++) {

            super.setTextHover(new ITextHover() {

                public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
                    //
                    return ""; //$NON-NLS-1$
                }


                public IRegion getHoverRegion(ITextViewer textViewer, int offset) {

                    return new Region(offset, 1);
                }

            }, contentTypes[i]);
        }
        super.activatePlugins();

    }


    public void setNewDictionary(Dictionary newDictionary) {

        dictionary = newDictionary;

        if (contentAssistant != null) {
            contentAssistant.uninstall();
            contentAssistant = null;
        }

        sqlTextTools = new SQLTextTools(store, dictionary);
        configuration = new SQLSourceViewerConfiguration(sqlTextTools);

        fPresentationReconciler = configuration.getPresentationReconciler(null);

        if (fPresentationReconciler != null) {
            fPresentationReconciler.install(this);
        }

        if (dictionary != null) {
                 contentAssistant = configuration.getContentAssistant(null);
                 if (contentAssistant != null) {
                     contentAssistant.install(this);
                 }
        }
    }


    public SQLTextViewer(Composite parent, int style, IPreferenceStore store, final Dictionary dictionary) {

        this(parent, style, store, dictionary, new VerticalRuler(0));

    }

    /**
     * PreferenceListener.
     *
     */
    private class PreferenceListener implements IPropertyChangeListener {

        public void propertyChange(PropertyChangeEvent event) {
            adaptToPreferenceChange(event);
        }
    };


    void adaptToPreferenceChange(PropertyChangeEvent event) {
        if (event.getProperty().equals(IConstants.FONT)) {
            FontData[] fData = PreferenceConverter.getFontDataArray(store, IConstants.FONT);
            String des = store.getString(IConstants.FONT);
            JFaceResources.getFontRegistry().put(des, fData);
            Control ctrl = this.getControl();
            if (ctrl != null) {
                ctrl.setFont(JFaceResources.getFontRegistry().get(des));
            }

        }
    }

    PreferenceListener fPreferenceListener = new PreferenceListener();


    public void showAssistance() {

        if (dictionary != null) {
        	contentAssistant.showPossibleCompletions();
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.text.source.ISourceViewer#setDocument(org.eclipse.jface.text.IDocument,
     *      org.eclipse.jface.text.source.IAnnotationModel)
     */
    public void setDocument(IDocument document, IAnnotationModel annotationModel) {
        setDocument(document);
        if (annotationModel != null && document != null) {
        	annotationModel.connect(document);
        }
    }


    /**
     *
     */
    public void clearText() {
        getTextWidget().setText(""); //$NON-NLS-1$
    }


	/**
	 * Getter for dictionary.
	 * @return the dictionary
	 */
	public Dictionary getDictionary() {
		return this.dictionary;
	}


	/**
	 * Sets the dictionary.
	 * @param dictionary the dictionary to set
	 */
	public void setDictionary(Dictionary dictionary) {
		this.dictionary = dictionary;
	}
}
