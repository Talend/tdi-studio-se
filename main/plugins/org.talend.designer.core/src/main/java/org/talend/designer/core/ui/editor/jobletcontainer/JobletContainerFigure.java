package org.talend.designer.core.ui.editor.jobletcontainer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.process.Problem.ProblemStatus;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.core.utils.DesignerColorUtils;

public class JobletContainerFigure extends Figure {

    public static final String KEY_REDUCE = "reduce_"; //$NON-NLS-1$

    public static final String KEY_MAP = "map_"; //$NON-NLS-1$

    private ImageFigure errorFigure, warningFigure;

    private SimpleHtmlFigure htmlStatusHint;

    private JobletContainer jobletContainer;

    private RoundedRectangle outlineFigure;

    private SimpleHtmlFigure titleFigure;

    private GreenRectangle rectFig;

    private ImageFigure markFigure;

    private ImageFigure infoFigure;

    private JobletCollapseFigure collapseFigure;

    private boolean showTitle;

    private String title;

    private RGB red = new RGB(250, 72, 80);

    private RGB green = new RGB(130, 240, 100);

    private RGB mrGroupColor = null;

    private RGB white = new RGB(255, 255, 255);

    private Map<String, SimpleHtmlFigure> mrFigures = new HashMap<String, SimpleHtmlFigure>();

    private boolean isSubjobDisplay = true;

    private int ALPHA_VALUE = 100;

    private IFigure parentMRFigure;

    private boolean dispose = false;

    private boolean showCompareMark;

    private Image errorMarkImage = ImageProvider.getImage(EImage.Error_Mark);

    /**
     * DOC hwang JobletContainerFigure constructor comment.
     *
     * @param model
     */
    public JobletContainerFigure(final JobletContainer jobletContainer, IFigure parentMRFigure) {
        setLayoutManager(new FreeformLayout());
        this.parentMRFigure = parentMRFigure;
        this.jobletContainer = jobletContainer;
        if (this.jobletContainer.getSubjobContainer() != null) {
            isSubjobDisplay = this.jobletContainer.getSubjobContainer().isDisplayed();
        }
        outlineFigure = new RoundedRectangle();
        rectFig = new GreenRectangle();
        rectFig.setAlpha(ALPHA_VALUE);
        titleFigure = new SimpleHtmlFigure();
        collapseFigure = new JobletCollapseFigure();

        collapseFigure.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                doCollapse();
            }
        });

        warningFigure = new ImageFigure();
        warningFigure.setImage(ImageProvider.getImage(EImage.WARNING_SMALL));
        warningFigure.setVisible(false);
        warningFigure.setSize(warningFigure.getPreferredSize());
        this.add(warningFigure);

        errorFigure = new ImageFigure();
        errorFigure.setImage(ImageProvider.getImage(EImage.ERROR_SMALL));
        errorFigure.setVisible(false);
        errorFigure.setSize(errorFigure.getPreferredSize());
        this.add(errorFigure);

        infoFigure = new ImageFigure();
        infoFigure.setImage(ImageProvider.getImage(EImage.INFORMATION_SMALL));
        infoFigure.setVisible(false);
        infoFigure.setSize(infoFigure.getPreferredSize());
        this.add(infoFigure);

        markFigure = new ImageFigure();
        markFigure.setImage(errorMarkImage);
        this.add(markFigure, null, 0);

        initMRFigures();

        htmlStatusHint = new SimpleHtmlFigure();

        initJobletContainerColor();

        updateData();
        initializejobletContainer(jobletContainer.getJobletContainerRectangle());
        rectFig.setVisible(false);
        markFigure.setVisible(false);
        if (jobletContainer.getNode().isMapReduceStart()) {
            refreshNodes(false);
            refreshMRstatus();
        }

    }

    public JobletContainer getJobletContainer() {
        return this.jobletContainer;
    }

    public Map<String, SimpleHtmlFigure> getMrFigures() {
        return this.mrFigures;
    }

    public void doCollapse() {
        if (jobletContainer != null && !jobletContainer.isReadOnly()) {
            PropertyChangeCommand ppc = new PropertyChangeCommand(jobletContainer, EParameterName.COLLAPSED.getName(),
                    !jobletContainer.isCollapsed());
            IProcess ipro = jobletContainer.getNode().getProcess();
            ppc.execute();
            reSelection();
        }
    }

    private void reSelection() {
        // select the start node.
        if (jobletContainer.isCollapsed()) {
            IProcess2 process = jobletContainer.getProcess();
            if (process == null) {
                return;
            }
            AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) process.getEditor();
            Node startNode = jobletContainer.getJobletStartNode();
            if (startNode != null && editor != null) {
                editor.selectNode(startNode);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paint(Graphics graphics) {
        graphics.setAlpha(255);
        if (errorFigure.isVisible()) {
            errorFigure.setLocation(jobletContainer.getErrorLocation());
        }
        if (warningFigure.isVisible()) {
            warningFigure.setLocation(jobletContainer.getWarningLocation());
        }
        if (infoFigure.isVisible()) {
            infoFigure.setLocation(jobletContainer.getInfoLocation());
        }
        if (rectFig.isVisible()) {
            rectFig.setLocation(jobletContainer.getMarkLocation());
        }
        if (markFigure.isVisible()) {
            markFigure.setLocation(jobletContainer.getErrorMarkLocation());
        }
        Iterator it = getChildren().iterator();
        if (!jobletContainer.getNode().isJoblet()) {
            while (it.hasNext()) {
                Figure fig = (Figure) it.next();
                if (mrFigures.isEmpty() && (fig instanceof GreenRectangle || fig instanceof RoundedRectangle)) {
                    it.remove();
                    continue;
                }
            }
        }
        if (this.jobletContainer.getNode().isJoblet()) {
            refreshForJoblet();
        }
        initializejobletContainer(getBounds());
        super.paint(graphics);
    }

    boolean lastJobletRedState = false;

    public void refreshNodes(boolean isClear) {
        if (this.jobletContainer.getNode().isJoblet()) {
            refreshForJoblet();
        } else if (this.jobletContainer.getNode().isMapReduce()) {
            refreshMRFigures();
            if (!this.jobletContainer.getNode().isMapReduceStart()) {
                if (rectFig.isVisible()) {
                    rectFig.setVisible(false);
                }
                if (outlineFigure.isVisible()) {
                    outlineFigure.setVisible(false);
                }

                removeFromParentMRFigure();
                mrFigures.clear();
                return;
            }

            if (rectFig != null) {
                if (isSubjobDisplay) {
                    Color color = ColorUtils.getCacheColor(mrGroupColor);
                    if (rectFig.getBackgroundColor() == null || !rectFig.getBackgroundColor().equals(color)) {
                        rectFig.setBackgroundColor(color);
                    }
                } else {
                    Color color = ColorUtils.getCacheColor(white);
                    if (rectFig.getBackgroundColor() == null || !rectFig.getBackgroundColor().equals(color)) {
                        rectFig.setBackgroundColor(color);
                    }
                }
                if (!rectFig.isVisible()) {
                    rectFig.setVisible(true);
                }
            }

            if (outlineFigure != null && outlineFigure.isVisible()) {
                outlineFigure.setVisible(false);
            }

            Iterator<Entry<String, SimpleHtmlFigure>> ite = mrFigures.entrySet().iterator();
            while (ite.hasNext()) {
                Entry<String, SimpleHtmlFigure> entry = ite.next();
                String key = entry.getKey();
                SimpleHtmlFigure value = entry.getValue();
                Double percent = new Double(0);

                if (!this.jobletContainer.getNode().isProgressBarNeeded()) {
                    value.setVisible(false);
                    continue;
                }

                if (key.startsWith(KEY_MAP)) {
                    // if (!"".equals(jobletContainer.getMrName()) && jobletContainer.getMrName() != null) {
                    percent = jobletContainer.getPercentMap() * 10;
                    if (isSubjobDisplay && !value.isVisible()) {
                        value.setVisible(true);
                    }

                    // }
                }

                if (key.startsWith(KEY_REDUCE)) {
                    percent = jobletContainer.getPercentReduce() * 10;
                    if (this.jobletContainer.isMRGroupContainesReduce() && isSubjobDisplay) {
                        if (!value.isVisible()) {
                            value.setVisible(true);
                        }
                    } else if (value.isVisible()) {
                        value.setVisible(false);
                    }
                }

                if (value.isVisible()) {
                    Integer i = Integer.parseInt(key.substring(key.indexOf("_") + 1)) + 1;
                    boolean hun = percent.equals(new Double(10));
                    Integer mrCount = this.jobletContainer.getNode().getMrJobInGroupCount();
                    boolean refreshPro = hun && (mrCount.toString().equals(jobletContainer.getMrName()));
                    if (i.toString().equals(jobletContainer.getMrName()) || isClear || refreshPro) {
                        List object = value.getChildren();
                        for (Object o : object) {
                            if (o instanceof RectangleFigure) {
                                setProgressData((RectangleFigure) o, percent, 8);
                            }
                        }
                    }
                }

            }

        }
    }

    public void refreshMRstatus() {
        if (this.jobletContainer.getNode().isMapReduceStart()) {
            Iterator<Entry<String, SimpleHtmlFigure>> ite = mrFigures.entrySet().iterator();
            while (ite.hasNext()) {
                Entry<String, SimpleHtmlFigure> entry = ite.next();
                SimpleHtmlFigure value = entry.getValue();
                for (Object obj : value.getChildren()) {
                    if (obj instanceof Figure) {
                        if (this.jobletContainer.getSubjobContainer() != null
                                && this.jobletContainer.getSubjobContainer().isCollapsed()) {
                            ((Figure) obj).setVisible(false);
                        } else {
                            ((Figure) obj).setVisible(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * DOC nrousseau Comment method "refreshForJoblet".
     */
    private void refreshForJoblet() {
        boolean isRed = new JobletUtil().isRed(this.jobletContainer);
        if (jobletContainer.isFromRef() && !this.jobletContainer.isCollapsed()) {
            isRed = true;
        }
        if (this.jobletContainer.isCollapsed() && lastJobletRedState == isRed) {
            return;
        }
        lastJobletRedState = isRed;
        if (isSubjobDisplay) {
            if (isRed && rectFig != null) {
                rectFig.setBackgroundColor(ColorUtils.getCacheColor(red));
            } else if (rectFig != null) {
                rectFig.setBackgroundColor(ColorUtils.getCacheColor(green));
            }
        } else {
            if (rectFig != null) {
                rectFig.setBackgroundColor(ColorUtils.getCacheColor(white));
            }
        }

        if (isRed && outlineFigure != null) {
            outlineFigure.setBackgroundColor(ColorUtils.getCacheColor(red));
        } else if (outlineFigure != null) {
            outlineFigure.setBackgroundColor(ColorUtils.getCacheColor(green));
        }

        if (!jobletContainer.isCollapsed()) {
            for (Object ele : jobletContainer.getElements()) {
                if (ele instanceof Node) {
                    ((Node) ele).setReadOnly(isRed);
                }
            }
        }
    }

    private Point lastLocation = null;

    public void initializejobletContainer(Rectangle rectangle) {
        Point location = this.getLocation();
        if (location.equals(lastLocation) && !jobletContainer.getNode().isMapReduceStart() && mrFigures.isEmpty()) {
            // avoid to calculate locations for nothing
            return;
        }
        lastLocation = location;
        collapseFigure.setCollapsed(jobletContainer.isCollapsed());
        collapseFigure.setVisible(this.jobletContainer.getNode().isJoblet());
        titleFigure.setText("<b> " + title + "</b>"); //$NON-NLS-1$ //$NON-NLS-2$
        Dimension preferedSize = titleFigure.getPreferredSize();
        preferedSize = preferedSize.getExpanded(0, 3);

        collapseFigure.setLocation(new Point(location.x, location.y));
        collapseFigure.setSize(preferedSize.height, preferedSize.height);

        titleFigure.setSize(preferedSize.width, preferedSize.height - 2);
        titleFigure.setLocation(new Point((rectangle.width - preferedSize.width) / 2 + location.x, location.y));
        titleFigure.setVisible(showTitle);

        outlineFigure.setLocation(new Point(location.x, location.y));
        outlineFigure.setVisible(showTitle);
        outlineFigure.setForegroundColor(ColorUtils.getCacheColor(new RGB(220, 120, 120)));
        outlineFigure.setSize(rectangle.width, preferedSize.height);

        refreshMRFigures();

        Iterator<Entry<String, SimpleHtmlFigure>> ite = mrFigures.entrySet().iterator();
        int i = 0;
        while (ite.hasNext()) {
            Entry<String, SimpleHtmlFigure> entry = ite.next();
            String key = entry.getKey();
            SimpleHtmlFigure value = entry.getValue();
            int progressHeight = value.getBounds().height + 3;
            Integer count = this.jobletContainer.getNode().getMrJobInGroupCount();
            i = Integer.parseInt(key.substring(key.indexOf("_") + 1));
            int mry = progressHeight * i;
            int proWidth = value.getBounds().width;
            int jcWidth = this.jobletContainer.getJobletContainerRectangle().width;
            if (isSubjobDisplay) {
                if (!this.jobletContainer.isMRGroupContainesReduce()) {
                    if (key.startsWith(KEY_MAP)) {
                        if (jcWidth > proWidth + 12) {
                            value.setLocation(new Point(location.x + jcWidth / 2 - proWidth / 2 - 6, location.y
                                    + rectangle.height - count * progressHeight + mry));
                        } else if (jcWidth > proWidth) {
                            value.setLocation(new Point(location.x + jcWidth / 2 - proWidth / 2, location.y + rectangle.height
                                    - count * progressHeight + mry));
                        } else {
                            value.setLocation(new Point(location.x, location.y + rectangle.height - count * progressHeight + mry));
                        }
                    }

                } else {
                    if (jcWidth / 2 >= 120) {
                        if (key.startsWith(KEY_MAP)) {
                            value.setLocation(new Point(location.x + jcWidth / 2 - 120, location.y + rectangle.height - count
                                    * progressHeight + mry));
                        }
                        if (key.startsWith(KEY_REDUCE)) {
                            value.setLocation(new Point(location.x + jcWidth / 2, location.y + rectangle.height - count
                                    * progressHeight + mry));
                        }
                    } else if (jcWidth / 2 > proWidth) {
                        if (key.startsWith(KEY_MAP)) {
                            value.setLocation(new Point(location.x + jcWidth / 2 - proWidth, location.y + rectangle.height
                                    - count * progressHeight + mry));
                        }
                        if (key.startsWith(KEY_REDUCE)) {
                            value.setLocation(new Point(location.x + jcWidth / 2, location.y + rectangle.height - count
                                    * progressHeight + mry));
                        }
                    } else {
                        if (key.startsWith(KEY_MAP)) {
                            value.setLocation(new Point(location.x, location.y + rectangle.height - count * progressHeight + mry));
                        }
                        if (key.startsWith(KEY_REDUCE)) {
                            value.setLocation(new Point(location.x + 110, location.y + rectangle.height - count * progressHeight
                                    + mry));
                        }
                    }

                }

            }
        }

        rectFig.setLocation(new Point(location.x, /* preferedSize.height + */location.y));
        rectFig.setSize(new Dimension(rectangle.width, rectangle.height /*- preferedSize.height*/));
        if (this.jobletContainer.getNode().isJoblet()) {
            if (new JobletUtil().isRed(this.jobletContainer)) {
                if (isSubjobDisplay) {
                    rectFig.setBackgroundColor(ColorUtils.getCacheColor(red));
                } else {
                    rectFig.setBackgroundColor(ColorUtils.getCacheColor(white));
                }
                outlineFigure.setBackgroundColor(ColorUtils.getCacheColor(red));
            } else {
                if (isSubjobDisplay) {
                    rectFig.setBackgroundColor(ColorUtils.getCacheColor(green));
                } else {
                    rectFig.setBackgroundColor(ColorUtils.getCacheColor(white));
                }
                outlineFigure.setBackgroundColor(ColorUtils.getCacheColor(green));
            }
        } else {
            if (isSubjobDisplay) {
                rectFig.setBackgroundColor(ColorUtils.getCacheColor(mrGroupColor));
            } else {
                rectFig.setBackgroundColor(ColorUtils.getCacheColor(white));
            }
            outlineFigure.setBackgroundColor(ColorUtils.getCacheColor(mrGroupColor));
            // progressFigure.setBackgroundColor(new Color(Display.getDefault(), red));
            if (!this.jobletContainer.getNode().isMapReduceStart()) {
                rectFig.setVisible(false);
                outlineFigure.setVisible(false);
            }

        }
        if (isSubjobDisplay) {
            if (this.jobletContainer.getNode().isMapReduce()) {
                rectFig.setForegroundColor(ColorUtils.getCacheColor(new RGB(121, 157, 175)));
            } else {
                rectFig.setForegroundColor(ColorUtils.getCacheColor(new RGB(220, 120, 120)));
            }
        } else {
            rectFig.setForegroundColor(ColorUtils.getCacheColor(white));
        }
        markFigure.setSize(errorMarkImage.getImageData().width, errorMarkImage.getImageData().height);
    }

    public void dispose() {
        // if (this.parentMRFigure != null) {
        // this.parentMRFigure.getChildren().clear();
        // }
        removeFromParentMRFigure();
        this.mrFigures.clear();
        dispose = true;
    }

    /**
     * 
     * hwang Comment method "updateData".
     */
    public void updateData() {

        showTitle = !jobletContainer.isCollapsed();

        title = (String) jobletContainer.getPropertyValue(EParameterName.SUBJOB_TITLE.getName());

        this.getChildren().remove(outlineFigure);
        this.getChildren().remove(rectFig);
        outlineFigure.getChildren().clear();
        rectFig.getChildren().clear();
        if (dispose) {
            return;
        }

        if (showTitle) {
            outlineFigure.add(titleFigure);
            outlineFigure.add(collapseFigure);
            add(rectFig, null, 0);
            add(outlineFigure, null, 1);
        } else {
            outlineFigure.add(titleFigure);
            rectFig.add(collapseFigure);
            add(outlineFigure, null, 0);
            add(rectFig, null, 1);
        }
        refreshMRFigures();

        if (jobletContainer.getSubjobContainer() != null && !jobletContainer.getSubjobContainer().isCollapsed()) {
            if (this.jobletContainer.getNode().isMapReduceStart()) {
                addToParentMRFigure();
            }
        }

        if (!this.jobletContainer.getNode().isMapReduceStart() && !this.jobletContainer.getNode().isJoblet()) {
            rectFig.setVisible(false);
            outlineFigure.setVisible(false);
        }
    }

    public void updateStatus(int status, boolean showInfoFlag) {
        if ((status & Process.ERROR_STATUS) != 0) {
            warningFigure.setVisible(false);
            errorFigure.setVisible(true);
        } else {
            errorFigure.setVisible(false);
            errorFigure.setToolTip(null);
        }

        if (((status & Process.WARNING_STATUS) != 0) && !errorFigure.isVisible()) {
            warningFigure.setVisible(true);
        } else {
            warningFigure.setVisible(false);
            warningFigure.setToolTip(null);
        }

        if (((status & Process.INFO_STATUS) != 0) && !errorFigure.isVisible() && !warningFigure.isVisible() && showInfoFlag) {
            warningFigure.setVisible(false);
            errorFigure.setVisible(false);
            infoFigure.setVisible(true);
        } else {
            infoFigure.setVisible(false);
        }

        if (warningFigure.isVisible() || errorFigure.isVisible() || infoFigure.isVisible()) {
            List<String> problemsList;

            String text = "<b>" + jobletContainer.getNode().getUniqueName() + "</b><br><br>"; //$NON-NLS-1$ //$NON-NLS-2$

            if ((status & Process.WARNING_STATUS) != 0) {
                text += "<i>Warnings:</i><br>"; //$NON-NLS-1$

                problemsList = Problems.getStatusList(ProblemStatus.WARNING, jobletContainer.getNode());
                for (String str : problemsList) {
                    text += "\t- " + str + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            if ((status & Process.ERROR_STATUS) != 0) {
                text += "<i>Errors:</i><br>"; //$NON-NLS-1$
                problemsList = Problems.getStatusList(ProblemStatus.ERROR, jobletContainer.getNode());
                for (String str : problemsList) {
                    text += "\t- " + str + "<br>"; //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            htmlStatusHint.setText(text);
            if (errorFigure.isVisible()) {
                errorFigure.setToolTip(htmlStatusHint);
            } else if (warningFigure.isVisible()) {
                warningFigure.setToolTip(htmlStatusHint);
            }
        } else {
            errorFigure.setVisible(false);
            errorFigure.setToolTip(null);
        }
    }

    public void updateStatus(int status) {
        updateStatus(status, false);
    }

    private void initMRFigures() {
        Node node = this.jobletContainer.getNode();
        if (!node.isMapReduceStart()) {
            return;
        }
        if (node.getMrGroupId() == null) {
            return;
        }
        if (dispose) {
            return;
        }
        Integer mrCount = node.getMrJobInGroupCount();
        if (mrCount == null) {
            mrCount = 1;
        }

        Image image = ImageProvider.getImage(ECoreImage.MRGREEBAR);
        // Image map = ImageProvider.getImage(ECoreImage.MRMAP);
        // Image reduce = ImageProvider.getImage(ECoreImage.MRREDUCE);

        int progressHeight = image.getBounds().height;
        int progressWidth = image.getBounds().width;
        for (int i = 0; i < mrCount; i++) {
            SimpleHtmlFigure progressMap = new SimpleHtmlFigure();
            // progressMap.setOutline(false);
            progressMap.setOpaque(false);
            Label mapTip = new Label();
            mapTip.setText("Map ");
            progressMap.setToolTip(mapTip);
            progressMap.setLayoutManager(new ToolbarLayout(true));
            progressMap.setVisible(false);

            SimpleHtmlFigure mapTitle = new SimpleHtmlFigure();
            mapTitle.setText("<b>Map</b> ");
            mapTitle.setSize(mapTitle.getPreferredSize());
            mapTitle.setOpaque(false);

            RectangleFigure mapGreen = new RectangleFigure();
            mapGreen.setSize(progressWidth, progressHeight);
            mapGreen.setPreferredSize(progressWidth, progressHeight + 5);
            mapGreen.setBorder(new LineBorder(ColorConstants.black, 1));
            mapGreen.setLayoutManager(new ToolbarLayout(true));
            mapGreen.setLocation(new Point(progressMap.getLocation().x + mapTitle.getPreferredSize().width, progressMap
                    .getLocation().y));
            mapGreen.setOpaque(true);
            mapGreen.setAlpha(255);
            mapGreen.setVisible(true);
            progressMap.add(mapTitle, 0);
            progressMap.add(mapGreen, 1);

            progressMap.setSize(mapTitle.getPreferredSize().width + mapGreen.getPreferredSize().width, progressHeight + 2);
            progressMap.setPreferredSize(mapTitle.getPreferredSize().width + mapGreen.getPreferredSize().width,
                    progressHeight + 2);
            mrFigures.put(KEY_MAP + i, progressMap);
            // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            SimpleHtmlFigure progressReduce = new SimpleHtmlFigure();
            // progressReduce.setOutline(false);
            progressReduce.setOpaque(false);
            Label reduceTip = new Label();
            reduceTip.setText("Reduce ");
            progressReduce.setToolTip(reduceTip);
            progressReduce.setLayoutManager(new ToolbarLayout(true));
            progressReduce.setVisible(false);

            SimpleHtmlFigure reduceTitle = new SimpleHtmlFigure();
            reduceTitle.setText("<b>Reduce</b> ");
            reduceTitle.setSize(reduceTitle.getPreferredSize());
            reduceTitle.setOpaque(false);

            RectangleFigure reduceGreen = new RectangleFigure();
            reduceGreen.setSize(progressWidth, progressHeight);
            reduceGreen.setPreferredSize(progressWidth, progressHeight + 5);
            reduceGreen.setBorder(new LineBorder(ColorConstants.black, 1));
            reduceGreen.setLayoutManager(new ToolbarLayout(true));
            reduceGreen.setLocation(new Point(progressReduce.getLocation().x + reduceTitle.getPreferredSize().width,
                    progressReduce.getLocation().y));
            reduceGreen.setOpaque(true);
            reduceGreen.setAlpha(255);
            reduceGreen.setVisible(true);
            progressReduce.add(reduceTitle, 0);
            progressReduce.add(reduceGreen, 1);

            progressReduce.setSize(reduceTitle.getPreferredSize().width + reduceGreen.getPreferredSize().width,
                    progressHeight + 2);
            progressReduce.setPreferredSize(reduceTitle.getPreferredSize().width + reduceGreen.getPreferredSize().width,
                    progressHeight + 2);
            mrFigures.put(KEY_REDUCE + i, progressReduce);
        }
    }

    public void setProgressData(RectangleFigure progressBarFigure, Double extentString, int extent) {
        progressBarFigure.getChildren().clear();
        int nodeX = progressBarFigure.getLocation().x;
        int nodeY = progressBarFigure.getLocation().y;

        if (extentString == 10) {
            ImageFigure progressDataFigure = new ImageFigure();
            Image image = ImageProvider.getImage(ECoreImage.MRGREEBAR);
            progressDataFigure.setImage(image);
            progressDataFigure.setVisible(true);
            progressBarFigure.add(progressDataFigure);
        } else if (extentString > 0 && extentString < 10) {
            for (int i = 0; i < extentString; i++) {
                ImageFigure progressDataFigure = new ImageFigure();
                Image image = ImageProvider.getImage(ECoreImage.MRREDBAR);
                progressDataFigure.setImage(image);
                progressDataFigure.setVisible(true);
                progressBarFigure.add(progressDataFigure);
                int imageWith = image.getImageData().width;
                if (i != 0) {
                    Point point = new Point(nodeX + i * imageWith, nodeY);
                    progressDataFigure.setLocation(point);
                }
            }
            for (int j = 0; j < (10 - extentString); j++) {
                ImageFigure progressDataFigure = new ImageFigure();
                Image image = ImageProvider.getImage(ECoreImage.MRGRAYBAR);
                progressDataFigure.setImage(image);
                progressDataFigure.setVisible(true);
                progressBarFigure.add(progressDataFigure);
                int imageWith = image.getImageData().width;
                if (j != 0) {
                    Point point = new Point(nodeX + extentString * imageWith + j * imageWith, nodeY);
                    progressDataFigure.setLocation(point);
                }
            }
        } else if (extentString == 0) {
            progressBarFigure.getChildren().clear();
        }

    }

    class GreenRectangle extends RoundedRectangle {

        @Override
        protected void fillShape(Graphics graphics) {
            if (isSubjobDisplay) {
                super.fillShape(graphics);
            }
        }

        @Override
        protected void outlineShape(Graphics graphics) {
            if (isSubjobDisplay) {
                super.outlineShape(graphics);
            }
        }
    }

    @Override
    public void add(IFigure figure, Object constraint, int index) {
        if (figure instanceof GreenRectangle) {
            super.add(figure, constraint, 0);
        } else {
            super.add(figure, constraint, index);
        }
    }

    public void updateJobletContainerColor() {
        initJobletContainerColor();
        updateData();
    }

    private void initJobletContainerColor() {
        if (this.jobletContainer.getNode().isMapReduce()) {
            RGB defaultSubjobColor = DesignerColorUtils.getPreferenceMRGroupRGB(DesignerColorUtils.MRGROUP_COLOR_NAME,
                    DesignerColorUtils.MR_COLOR);
            if (defaultSubjobColor != null) {
                mrGroupColor = defaultSubjobColor;
            } else {
                mrGroupColor = green;
            }
        } else {
            RGB defaultSubjobColor = DesignerColorUtils.getPreferenceMRGroupRGB(DesignerColorUtils.JOBLET_COLOR_NAME,
                    DesignerColorUtils.JOBLET_COLOR);
            if (defaultSubjobColor != null) {
                mrGroupColor = defaultSubjobColor;
            } else {
                mrGroupColor = green;
            }
        }
    }

    private void refreshMRFigures() {
        if (!this.jobletContainer.getNode().isMapReduce()) {
            return;
        }
        if (this.jobletContainer.getNode().isMapReduceStart()) {
            if (mrFigures.isEmpty()) {
                initMRFigures();
            } else if (mrFigures.size() / 2 != this.jobletContainer.getNode().getMrJobInGroupCount()) {
                removeFromParentMRFigure();
                mrFigures.clear();
                initMRFigures();
            }

        }
        if (!this.jobletContainer.getNode().isMapReduceStart() && !mrFigures.isEmpty()) {
            removeFromParentMRFigure();
            mrFigures.clear();
        }
    }

    private void removeFromParentMRFigure() {
        if (this.parentMRFigure == null) {
            return;
        }
        Iterator<Entry<String, SimpleHtmlFigure>> ite = mrFigures.entrySet().iterator();
        while (ite.hasNext()) {
            Entry<String, SimpleHtmlFigure> entry = ite.next();
            // String key = entry.getKey();
            SimpleHtmlFigure value = entry.getValue();
            if (this.parentMRFigure.getChildren().contains(value)) {
                this.parentMRFigure.remove(value);
            }
        }
    }

    private void addToParentMRFigure() {
        if (this.parentMRFigure == null) {
            return;
        }
        if (!this.jobletContainer.getNode().isMapReduceStart()) {
            return;
        }
        if (dispose) {
            return;
        }
        Iterator<Entry<String, SimpleHtmlFigure>> ite = mrFigures.entrySet().iterator();
        while (ite.hasNext()) {
            Entry<String, SimpleHtmlFigure> entry = ite.next();
            SimpleHtmlFigure value = entry.getValue();
            if (!this.parentMRFigure.getChildren().contains(value)) {
                this.parentMRFigure.add(value);
            }
        }
    }

    public void updateErrorFlag(boolean flag) {
        rectFig.setVisible(flag);
        markFigure.setVisible(flag);
    }

    public boolean isShowCompareMark() {
        return this.showCompareMark;
    }

    public void setShowCompareMark(boolean showCompareMark) {
        this.showCompareMark = showCompareMark;
    }

    public void setInfoHint(String hintText) {
        if (infoFigure.isVisible()) {
            if (hintText.equals("") || hintText == null) { //$NON-NLS-1$
                infoFigure.setToolTip(null);
            } else {
                htmlStatusHint.setText(hintText);
                infoFigure.setToolTip(htmlStatusHint);
            }
        } else {
            infoFigure.setToolTip(null);
        }
    }
}
