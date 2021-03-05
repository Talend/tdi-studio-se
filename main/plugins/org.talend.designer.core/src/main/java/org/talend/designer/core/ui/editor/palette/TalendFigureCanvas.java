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
package org.talend.designer.core.ui.editor.palette;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.ScrollPaneSolver;
import org.eclipse.draw2d.UpdateListener;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.talend.core.ui.CoreUIPlugin;

/**
 * cli class global comment. Detailled comment
 */
public class TalendFigureCanvas extends FigureCanvas {

    private static Logger log = Logger.getLogger(TalendFigureCanvas.class);

    private int cashedToolHeight = 24;

    protected Control toolControl;

    static final int DEFAULT_STYLES = SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND | SWT.V_SCROLL | SWT.H_SCROLL;

    // reflection method to invoke a package private method
    private static Method setIgnoreResizeMethod;

    // reflection field to access a private field
    private static Field listenersField;

    // reflection is used to work our way around some limitation in the Figurecanvas upper class
    // retreive method and field in a static way to avoid doing it everytime they are called and improve perf
    static {
        try {
            listenersField = UpdateManager.class.getDeclaredField("listeners"); //$NON-NLS-1$
            listenersField.setAccessible(true);
            setIgnoreResizeMethod = LightweightSystem.class.getDeclaredMethod("setIgnoreResize", boolean.class); //$NON-NLS-1$
            setIgnoreResizeMethod.setAccessible(true);
        } catch (SecurityException e) {
            handleReflectionFailure(e);
        } catch (NoSuchMethodException e) {
            handleReflectionFailure(e);
        } catch (NoSuchFieldException e) {
            handleReflectionFailure(e);
        }
    }

    public TalendFigureCanvas(Composite parent, LightweightSystem lws, TalendPaletteViewer toolViewer) {
        this(SWT.DOUBLE_BUFFERED, parent, lws, toolViewer);
    }

    public TalendFigureCanvas(int style, Composite parent, LightweightSystem lws, TalendPaletteViewer toolViewer) {
        super(style | DEFAULT_STYLES, parent, lws);

        if (toolViewer != null) {
            toolControl = toolViewer.creatToolControl(this);
            CoreUIPlugin.setCSSClass(toolControl, "PaletteSearchBar"); //$NON-NLS-1$
        }

        if (toolControl != null && toolViewer != null) {
            org.eclipse.swt.graphics.Point bounds = toolControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            if (cashedToolHeight < bounds.y) {
                cashedToolHeight = bounds.y;
            }
        }
        // FIXME ggu
        // use this hook to override the method layoutViewport in super calss FigureCanvas
        setupNewLayoutHook();
    }

    /**
     * Layout Viewport is not overridable in the FigureCanvas so we use a work around to setup our own layout viewport
     */
    private void setupNewLayoutHook() {
        try {
            // reset the listener of the update manager using reflection
            Object emptyArray = Array.newInstance(UpdateListener.class, 0);
            listenersField.set(getLightweightSystem().getUpdateManager(), emptyArray);
            // setup our own layout
            getLightweightSystem().getUpdateManager().addUpdateListener(new UpdateListener() {

                @Override
                public void notifyPainting(Rectangle damage, java.util.Map dirtyRegions) {
                    if (toolControl != null) {
                        toolControl.setBounds(0, 0, toolControl.getBounds().width, cashedToolHeight);
                    }
                }

                @Override
                public void notifyValidating() {
                    if (!isDisposed()) {
                        layoutViewport2();
                    }
                }
            });
        } catch (IllegalAccessException iae) {
            handleReflectionFailure(iae);
        }
    }

    /**
     * log the exception and throw a Runtime exception cause this is serious.
     *
     * @param iae
     */
    private static void handleReflectionFailure(Exception e) {
        // our hook is not working so say it
        log.error("Draw2D Canvas hook failed", e);
        throw new RuntimeException(e);

    }

    protected void layoutViewport2() {
        ScrollPaneSolver.Result result;
        int viewPortY = 0;
        if (toolControl != null) {
            viewPortY = cashedToolHeight;
        }
        result = ScrollPaneSolver.solve(new Rectangle(getBounds()).setLocation(0, viewPortY), getViewport(),
                getHorizontalScrollBarVisibility(), getVerticalScrollBarVisibility(), computeTrim(0, 0, 0, 0).width,
                computeTrim(0, 0, 0, 0).height);
        try {
            // reflection call to a package priveate method
            setIgnoreResizeMethod.invoke(getLightweightSystem(), true);
        } catch (IllegalArgumentException e) {
            handleReflectionFailure(e);
        } catch (IllegalAccessException e) {
            handleReflectionFailure(e);
        } catch (InvocationTargetException e) {
            handleReflectionFailure(e);
        }
        try {
            if (getHorizontalBar().getVisible() != result.showH) {
                getHorizontalBar().setVisible(result.showH);
            }
            if (getVerticalBar().getVisible() != result.showV) {
                getVerticalBar().setVisible(result.showV);
            }
            Rectangle r = new Rectangle(getClientArea());
            if (toolControl != null) {
                toolControl.setBounds(0, 0, r.width, cashedToolHeight);
                r.height -= cashedToolHeight;
                r.setLocation(0, cashedToolHeight);
            } else {
                r.setLocation(0, 0);
            }
            getLightweightSystem().getRootFigure().setBounds(r);
        } finally {
            try {
                // reflection call to a package priveate method
                setIgnoreResizeMethod.invoke(getLightweightSystem(), false);
            } catch (IllegalArgumentException e) {
                handleReflectionFailure(e);
            } catch (IllegalAccessException e) {
                handleReflectionFailure(e);
            } catch (InvocationTargetException e) {
                handleReflectionFailure(e);
            }
        }
    }

}
