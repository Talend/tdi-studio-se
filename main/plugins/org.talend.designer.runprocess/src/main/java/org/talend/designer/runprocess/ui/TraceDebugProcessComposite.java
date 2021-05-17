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
package org.talend.designer.runprocess.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.EComponentCategory;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.INode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.prefs.ITalendCorePrefConstants;
import org.talend.core.ui.CoreUIPlugin;
import org.talend.core.ui.properties.tab.IDynamicProperty;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.IMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.views.problems.Problems;
import org.talend.designer.runprocess.IProcessMessage;
import org.talend.designer.runprocess.ProcessMessage;
import org.talend.designer.runprocess.ProcessMessage.MsgType;
import org.talend.designer.runprocess.ProcessMessageManager;
import org.talend.designer.runprocess.RunProcessContext;
import org.talend.designer.runprocess.RunProcessPlugin;
import org.talend.designer.runprocess.RunprocessConstants;
import org.talend.designer.runprocess.i18n.Messages;
import org.talend.designer.runprocess.prefs.RunProcessPrefsConstants;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class TraceDebugProcessComposite extends ScrolledComposite implements IDynamicProperty {

    private Button runButton;

    private Button killBtn;

    private ProcessManager manager = ProcessManager.getInstance();

    private ProcessContextComposite contextComposite;

    public void setContextComposite(ProcessContextComposite contextComposite) {
        this.contextComposite = contextComposite;
    }

    private Button clearTracePerfBtn;

    private static RunProcessContext processContext;

    private StyledText consoleText;

    public HashMap<String, IProcessMessage> errorMessMap = new HashMap<String, IProcessMessage>();

    private boolean isAddedStreamListener;

    private IStreamListener streamListener;

    private static final int MINIMUM_HEIGHT = 65;

    private static final int MINIMUM_WIDTH = 530;

    private boolean hideConsoleLine = false;

    private Button enableLineLimitButton;

    private Text lineLimitText;

    private PropertyChangeListener pcl;

    private Double extend = new Double(0);

    /**
     * DOC Administrator DebugProcessComposite constructor comment.
     *
     * @param parent
     * @param style
     */
    public TraceDebugProcessComposite(Composite parent, int style) {
        super(parent, style);
        // TODO Auto-generated constructor stub
        // CSS
        CoreUIPlugin.setCSSClass(this, TraceDebugProcessComposite.class.getSimpleName());
    }

    private void createLineLimitedControl(Composite container) {
        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData());

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 7;
        formLayout.marginHeight = 4;
        formLayout.spacing = 7;
        composite.setLayout(formLayout);

        enableLineLimitButton = new Button(composite, SWT.CHECK);
        enableLineLimitButton.setText(Messages.getString("ProcessComposite.lineLimited")); //$NON-NLS-1$
        FormData formData = new FormData();
        enableLineLimitButton.setLayoutData(formData);
        enableLineLimitButton.setEnabled(false);
        enableLineLimitButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                lineLimitText.setEditable(enableLineLimitButton.getSelection());
                RunProcessPlugin.getDefault().getPluginPreferences()
                        .setValue(RunprocessConstants.ENABLE_CONSOLE_LINE_LIMIT, enableLineLimitButton.getSelection());
            }
        });

        lineLimitText = new Text(composite, SWT.BORDER);
        formData = new FormData();
        formData.width = 120;
        formData.left = new FormAttachment(enableLineLimitButton, 0, SWT.RIGHT);
        lineLimitText.setLayoutData(formData);
        lineLimitText.setEnabled(false);
        lineLimitText.addListener(SWT.Verify, new Listener() {

            // this text only receive number here.
            @Override
            public void handleEvent(Event e) {
                String s = e.text;
                if (!s.equals("")) { //$NON-NLS-1$
                    try {
                        Integer.parseInt(s);
                        RunProcessPlugin.getDefault().getPluginPreferences()
                                .setValue(RunprocessConstants.CONSOLE_LINE_LIMIT_COUNT, lineLimitText.getText() + s);
                    } catch (Exception ex) {
                        e.doit = false;
                    }
                }
            }
        });
        lineLimitText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                RunProcessPlugin.getDefault().getPluginPreferences()
                        .setValue(RunprocessConstants.CONSOLE_LINE_LIMIT_COUNT, lineLimitText.getText());
            }
        });

        boolean enable = RunProcessPlugin.getDefault().getPluginPreferences()
                .getBoolean(RunprocessConstants.ENABLE_CONSOLE_LINE_LIMIT);
        enableLineLimitButton.setSelection(enable);
        lineLimitText.setEditable(enable);
        String count = RunProcessPlugin.getDefault().getPluginPreferences()
                .getString(RunprocessConstants.CONSOLE_LINE_LIMIT_COUNT);
        if (count.equals("")) { //$NON-NLS-1$
            count = "100"; //$NON-NLS-1$
        }
        lineLimitText.setText(count);
    }

    protected void addListeners() {

    }

    public void setProcessContext(RunProcessContext processContext) {
        IPreferenceStore preferenceStore = DesignerPlugin.getDefault().getPreferenceStore();
        String languagePrefix = LanguageManager.getCurrentLanguage().toString() + "_"; //$NON-NLS-1$
        if (this.processContext != null) {
            this.processContext.removePropertyChangeListener(pcl);
        }
        this.processContext = processContext;
        if (processContext != null) {
            processContext.addPropertyChangeListener(pcl);
        }

        boolean disableAll = false;
        if (processContext != null) {
            disableAll = processContext.getProcess().disableRunJobView();

        }
        setRunnable(processContext != null && !processContext.isRunning() && !disableAll);
        killBtn.setEnabled(processContext != null && processContext.isRunning() && !disableAll);
        // previousRow.setEnabled(processContext != null && processContext.isRunning() && !disableAll);
        // nextRow.setEnabled(processContext != null && processContext.isRunning() && !disableAll);
        // nextBreakPoint.setEnabled(processContext != null && processContext.isRunning() && !disableAll);
        // clearBeforeExec.setEnabled(processContext != null);
        // clearBeforeExec.setSelection(processContext != null && processContext.isClearBeforeExec());
        // contextComposite.setProcess(((processContext != null) && !disableAll ? processContext.getProcess() : null));
        fillConsole(processContext != null ? processContext.getMessages() : new ArrayList<IProcessMessage>());
    }

    protected void fillConsole(Collection<IProcessMessage> messages) {
        consoleText.setText(""); //$NON-NLS-1$

        for (IProcessMessage processMessage : messages) {
            doAppendToConsole(processMessage);
        }
        scrollToEnd();
    }

    private void doAppendToConsole(final IProcessMessage message) {
        if (consoleText.isDisposed()) {
            return;
        }
        // see feature 0004895: Font size of the output console are very small
        setConsoleFont();

        String[] rows = message.getContent().split("\n"); //$NON-NLS-1$
        int rowLimit = getConsoleRowLimit();
        String content = null;
        if (rowLimit != SWT.DEFAULT) {
            int currentRows = consoleText.getLineCount();
            // if (consoleText.getText().equals("")) {
            currentRows--;
            // }
            if (currentRows >= rowLimit) {
                return;
            } else if (currentRows + rows.length <= rowLimit) {
                content = message.getContent();
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < rowLimit - currentRows; i++) {
                    sb.append(rows[i]).append("\n"); //$NON-NLS-1$
                }
                content = sb.toString();
            }
        }

        if (content == null) {
            content = message.getContent();
        }

        StyleRange style = new StyleRange();
        style.start = consoleText.getText().length();

        String[] contents = content.split("\n");
        for (String content2 : contents) {
            if (isPattern(content2) || isPatternFor(content2)) {
                consoleText.append(""); //$NON-NLS-1$
                content = ""; //$NON-NLS-1$
            } else {
                consoleText.append(content2);
                consoleText.append("\n");
            }
        }
        style.length = content.length();
        if (message.getType() == MsgType.CORE_OUT || message.getType() == MsgType.CORE_ERR) {
            style.fontStyle = SWT.ITALIC;
        }
        Color color;
        switch ((MsgType) message.getType()) {
        case CORE_OUT:
            color = getDisplay().getSystemColor(SWT.COLOR_BLUE);
            break;
        case CORE_ERR:
            color = getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
            break;
        case STD_ERR:
            color = getDisplay().getSystemColor(SWT.COLOR_RED);
            break;
        case STD_OUT:
        default:
            color = getDisplay().getSystemColor(SWT.COLOR_BLACK);
            break;
        }
        style.foreground = color;

        // added by hyWang for bug 0007411
        if ((style.start + style.length) > consoleText.getCharCount()) {
            style.length = consoleText.getCharCount() - style.start;
        }

        consoleText.setStyleRange(style);
    }

    private int getConsoleRowLimit() {
        if (!enableLineLimitButton.isDisposed()) {
            if (enableLineLimitButton.getSelection()) {
                try {
                    return Integer.parseInt(lineLimitText.getText());
                } catch (Exception e) {
                }
            }
        }
        return SWT.DEFAULT;
    }

    private boolean isPattern(String content) {
        Pattern pattern = Pattern.compile("\\$\\s*\\d+(\\.\\d*)?%"); //$NON-NLS-1$
        Matcher m = pattern.matcher(content);
        return m.find();
    }

    private boolean isPatternFor(String content) {
        Pattern pattern = Pattern.compile("\\[\\s*\\d+(\\.\\d*)?%\\]"); //$NON-NLS-1$
        Matcher m = pattern.matcher(content);
        return m.find();
    }

    private void scrollToEnd() {
        if (consoleText.isDisposed()) {
            return;
        }
        consoleText.setCaretOffset(consoleText.getText().length());
        consoleText.showSelection();
    }

    protected static void setButtonLayoutData(final Button button) {
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        final int widthHint = 80;
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
    }

    protected void appendToConsole(final IProcessMessage message) {
        getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {

                if (message.getType() == MsgType.CORE_OUT || message.getType() == MsgType.CORE_ERR) {
                    // always display job end msgs
                    callViewToDisplayMsg(message);
                } else {
                    if (!isHideConsoleLine()) {
                        callViewToDisplayMsg(message);
                    }
                }
            }

            /**
             * DOC yexiaowei Comment method "callViewToDisplayMsg".
             *
             * @param message
             */
            private void callViewToDisplayMsg(final IProcessMessage message) {
                doAppendToConsole(message);
                scrollToEnd();
            }
        });
    }

    private void runProcessContextChanged(final PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (ProcessMessageManager.PROP_MESSAGE_ADD.equals(propName)
                || ProcessMessageManager.PROP_DEBUG_MESSAGE_ADD.equals(propName)) {
            IProcessMessage psMess = (IProcessMessage) evt.getNewValue();

            if (errorMessMap.size() <= CorePlugin.getDefault().getPreferenceStore()
                    .getInt(ITalendCorePrefConstants.PREVIEW_LIMIT)) {
                if (!(LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL))) {
                    getAllErrorMess(psMess);
                } else {
                    addPerlMark(psMess);
                }
            }
            appendToConsole(psMess);
        } else if (ProcessMessageManager.PROP_MESSAGE_CLEAR.equals(propName)) {
            getShell().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (!consoleText.isDisposed()) {
                        consoleText.setText(""); //$NON-NLS-1$
                    }
                }
            });
        } else if (RunProcessContext.PROP_MONITOR.equals(propName)) {
            // perfBtn.setSelection(((Boolean) evt.getNewValue()).booleanValue());
        } else if (RunProcessContext.TRACE_MONITOR.equals(propName)) {
            // traceBtn.setSelection(((Boolean) evt.getNewValue()).booleanValue());
        } else if (RunProcessContext.PROP_RUNNING.equals(propName)) {
            getShell().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (isDisposed()) {
                        return;
                    }
                    boolean running = ((Boolean) evt.getNewValue()).booleanValue();
                    setRunnable(!running);
                    killBtn.setEnabled(running);
                    // previousRow.setEnabled(running);
                    // nextRow.setEnabled(running);
                    // nextBreakPoint.setEnabled(running);
                }
            });
        }
    }

    private Point computeSize(String text) {
        GC gc = new GC(runButton.getDisplay());
        final Point p = gc.textExtent(text);
        gc.dispose();
        return p;
    }

    public boolean isHideConsoleLine() {
        return this.hideConsoleLine;
    }

    public void setHideconsoleLine(boolean infoview) {
        this.hideConsoleLine = infoview;
    }

    private void checkSaveBeforeRunSelection() {

    }

    protected void setRunnable(boolean runnable) {
        clearTracePerfBtn.setEnabled(runnable);
        // previousRow.setEnabled(runnable);
        // nextRow.setEnabled(runnable);
        // nextBreakPoint.setEnabled(runnable);
        setExecBtn(runnable);
        // contextComposite.setEnabled(runnable);
        // if (argumentsComposite != null) {
        // argumentsComposite.setEnabled(runnable);
        // }
        enableLineLimitButton.setEnabled(runnable);
        lineLimitText.setEnabled(runnable);

    }

    protected void setExecBtn(final boolean runnable) {
        runButton.setEnabled(runnable);

    }

    private boolean setConsoleFont() {
        IPreferenceStore preferenceStore = CorePlugin.getDefault().getPreferenceStore();
        String fontType = preferenceStore.getString(RunProcessPrefsConstants.CONSOLE_FONT);
        if (StringUtils.isNotEmpty(fontType)) {
            FontData fontData = new FontData(fontType);
            if (consoleText.getFont() != null) {
                FontData oldFont = consoleText.getFont().getFontData()[0];
                // font is same
                if (oldFont.equals(fontData)) {
                    return false;
                }
            }
            Font font = new Font(this.getDisplay(), fontData);
            consoleText.setFont(font);
            return true;

        }
        return false;
    }

    public void kill() {
        killBtn.setEnabled(false);
        // previousRow.setEnabled(false);
        // nextRow.setEnabled(false);
        // nextBreakPoint.setEnabled(false);
        setHideconsoleLine(true);
        processContext.kill();
    }

    boolean debugMode = false;

    public void debug() {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getComposite()
     */
    @Override
    public Composite getComposite() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getCurRowSize()
     */
    @Override
    public int getCurRowSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getElement()
     */
    @Override
    public Element getElement() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getHashCurControls()
     */
    @Override
    public BidiMap getHashCurControls() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getPart()
     */
    @Override
    public IMultiPageTalendEditor getPart() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @seeorg.talend.core.properties.tab.IDynamicProperty#getRepositoryAliasName(org.talend.core.model.properties.
     * ConnectionItem)
     */
    @Override
    public String getRepositoryAliasName(ConnectionItem connectionItem) {
        // TODO Auto-generated method stub
        return null;
    }

    /* 16969 */
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryConnectionItemMap()
    // */
    // public Map<String, ConnectionItem> getRepositoryConnectionItemMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryQueryStoreMap()
    // */
    // public Map<String, Query> getRepositoryQueryStoreMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getRepositoryTableMap()
     */
    // public Map<String, IMetadataTable> getRepositoryTableMap() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getSection()
     */
    @Override
    public EComponentCategory getSection() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getTableIdAndDbSchemaMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbSchemaMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#getTableIdAndDbTypeMap()
     */
    @Override
    public Map<String, String> getTableIdAndDbTypeMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#refresh()
     */
    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        if (!isDisposed()) {
            getParent().layout();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.properties.tab.IDynamicProperty#setCurRowSize(int)
     */
    @Override
    public void setCurRowSize(int i) {
        // TODO Auto-generated method stub

    }

    public void getAllErrorMess(IProcessMessage psMess) {
        if (psMess.getType().equals(MsgType.STD_ERR)) {
            String mess = psMess.getContent();
            String[] linesMess = mess.split("\n");//$NON-NLS-1$
            StringBuffer currentMess = new StringBuffer();
            String currenctJobName = processContext.getProcess().getName();
            for (int i = 0; i < linesMess.length; i++) {
                String tRunJobName = currenctJobName;
                String linemess = linesMess[i].trim();
                Pattern pattern = Pattern.compile("^Exception\\s*in\\s*component\\s*(\\w)+_\\d$");//$NON-NLS-1$
                Matcher m = pattern.matcher(linemess);
                if (m.find()) {
                    List<Node> runjobList = getTRunjobList(processContext.getProcess());
                    String[] allwords = linemess.split("\\s"); //$NON-NLS-1$
                    String componentName = allwords[allwords.length - 1];
                    if (runjobList.size() > 0) {
                        int currentI = i;
                        if (currentI + 1 < linesMess.length - 1) {
                            // do {
                            // tRunJobName = linesMess[currentI++];
                            // } while ((tRunJobName.lastIndexOf("(") == -1 || tRunJobName.lastIndexOf(".java") == -1)
                            // && currentI < linesMess.length - 1);
                            for (int j = currentI + 1; j < linesMess.length - 1; j++) {
                                tRunJobName = linesMess[j];
                                if ((tRunJobName.contains(componentName))) {
                                    break;
                                }
                            }
                            if (tRunJobName.lastIndexOf("(") != -1 && tRunJobName.lastIndexOf(".java") != -1) {
                                tRunJobName = tRunJobName.substring(tRunJobName.lastIndexOf("(") + 1,
                                        tRunJobName.lastIndexOf(".java"));
                            } else {
                                tRunJobName = currenctJobName;
                            }
                        }
                    }

                    if (tRunJobName != null && tRunJobName.equals(currenctJobName)) {
                        if (i == 0) {
                            errorMessMap.put(componentName, psMess);
                        } else {
                            for (int j = i; j < linesMess.length; j++) {
                                currentMess.append(linesMess[j] + "\n"); //$NON-NLS-1$
                            }
                            IProcessMessage currentProMess = new ProcessMessage(MsgType.STD_ERR, currentMess.toString());
                            errorMessMap.put(componentName, currentProMess);
                        }
                    }
                    // break;
                }

            }
        }
        refreshNode(psMess);
    }

    public void refreshNode(final IProcessMessage psMess) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if (processContext == null) {
                    return;
                }
                org.talend.core.model.process.IProcess process = processContext.getProcess();
                if (process == null) {
                    return;
                }
                List<INode> nodeList = (List<INode>) process.getGraphicalNodes();
                for (INode inode : nodeList) {
                    if (!inode.isActivate()) {
                        continue;
                    }
                    String nodeUniqueName = inode.getUniqueName();
                    if (LanguageManager.getCurrentLanguage().equals(ECodeLanguage.PERL) && Problems.nodeList.size() > 0) {
                        errorMessMap.clear();
                    }
                    if (errorMessMap.get(nodeUniqueName) != null) {
                        if (inode instanceof Node) {
                            IProcessMessage messPro = errorMessMap.get(nodeUniqueName);
                            Node node = (Node) inode;
                            node.setErrorFlag(true);
                            node.setCompareFlag(false);
                            node.setErrorInfo(messPro.getContent());
                            node.getNodeError().updateState("UPDATE_STATUS", true); //$NON-NLS-1$
                            if (node.isFileScaleComponent()) {
                                refreshProgress(psMess, node, nodeUniqueName);
                            }
                            node.setErrorInfoChange("ERRORINFO", true); //$NON-NLS-1$
                        }
                    } else {
                        if (inode instanceof Node) {
                            Node node = (Node) inode;
                            if (Problems.nodeList.size() > 0) {
                                String befor = "Error in the component's properties:";
                                Iterator<Entry<Node, StringBuffer>> set = Problems.nodeList.entrySet().iterator();
                                while (set.hasNext()) {
                                    Entry<Node, StringBuffer> en = set.next();
                                    Node no = en.getKey();
                                    String des = en.getValue().toString();
                                    if (node == no) {
                                        node.setErrorFlag(true);
                                        node.setCompareFlag(false);
                                        node.setErrorInfo(befor + des);
                                        node.getNodeError().updateState("UPDATE_STATUS", false);//$NON-NLS-1$
                                        node.setErrorInfoChange("ERRORINFO", true);//$NON-NLS-1$
                                    }
                                }
                            } else {
                                if (node.isErrorFlag() == true) {
                                    node.setErrorFlag(false);
                                    node.setCompareFlag(false);
                                    node.setErrorInfo(null);
                                    node.getNodeError().updateState("UPDATE_STATUS", false); //$NON-NLS-1$
                                }

                                if (node.isFileScaleComponent()) {
                                    refreshProgress(psMess, node, nodeUniqueName);
                                }
                                if (node.isErrorFlag() == true) {
                                    node.setErrorInfoChange("ERRORINFO", false); //$NON-NLS-1$
                                }
                            }
                        }
                    }
                }
            }

        });
    }

    public void refreshProgress(IProcessMessage psMess, Node node, String nodeUniqueName) {
        String mess = ""; //$NON-NLS-1$
        String uniqueName = ""; //$NON-NLS-1$
        String[] message = psMess.getContent().split("\n");

        for (String element : message) {
            if (isPattern(element)) {

                int firIndex = element.indexOf("$"); //$NON-NLS-1$
                int secIndex = element.indexOf("%"); //$NON-NLS-1$
                uniqueName = element.substring(0, firIndex);
                mess = element.substring(firIndex + 1, secIndex);
            }

            Double extentPro = new Double(0);
            if ((!"".equals(mess)) && mess != null) { //$NON-NLS-1$
                extentPro = Double.parseDouble(mess);
            }

            if (((extend != extentPro) && nodeUniqueName.equals(uniqueName))) {
                node.getNodeProgressBar().updateState("UPDATE_STATUS", extentPro); //$NON-NLS-1$
                extend = extentPro;
            }

        }

    }

    private List<Node> getTRunjobList(org.talend.core.model.process.IProcess process) {
        List<Node> trunjobList = new ArrayList<Node>();
        if (!(process instanceof Process)) {
            return trunjobList;
        }
        List<INode> nodeList = (List<INode>) ((Process) process).getGraphicalNodes();
        for (INode node : nodeList) {
            if (node.getComponent().getName().equals("tRunJob")) {
                if (node instanceof Node) {
                    trunjobList.add((Node) node);
                }
            }
        }
        return trunjobList;
    }

    protected void addPerlMark(IProcessMessage psMess) {
        if (psMess.getType().equals(MsgType.STD_ERR)) {
            String content = psMess.getContent();
            String path = null;
            String uniName = null;
            int lineNo = -1;
            Pattern errorPattern = Pattern.compile("(.*) at (\\S+) line (\\d+)[\\.,]");//$NON-NLS-1$
            Matcher m = errorPattern.matcher(content);
            String matchContent = null;

            while (m.find()) {
                path = m.group(2);
                lineNo = parseInt(m.group(3));

                matchContent = m.group();

                if ((!("".equals(path)) && path != null) && lineNo > 0) {//$NON-NLS-1$
                    uniName = Problems.setErrorMark(path, lineNo);
                }

                if (uniName != null) {
                    if (!errorMessMap.containsKey(uniName)) {
                        errorMessMap.put(uniName, new ProcessMessage(MsgType.STD_ERR, matchContent));
                    } else {
                        String uniMess = errorMessMap.get(uniName).getContent();
                        errorMessMap.put(uniName, new ProcessMessage(MsgType.STD_ERR, uniMess.concat(matchContent)));
                    }
                }

            }
        }
        refreshNode(psMess);
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * DOC gldu Comment method "hasProcess".
     *
     * @return
     */
    public boolean hasProcess() {
        // TODO Auto-generated method stub
        return processContext != null;
    }
}
