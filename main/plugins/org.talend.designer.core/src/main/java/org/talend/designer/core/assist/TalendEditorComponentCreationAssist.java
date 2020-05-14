package org.talend.designer.core.assist;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.IContentProposalListener2;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.model.components.StitchPseudoComponent;
import org.talend.designer.core.ui.editor.PaletteComponentFactory;
import org.talend.designer.core.ui.editor.StitchDataLoaderConstants;
import org.talend.designer.core.ui.editor.cmd.CreateNodeContainerCommand;
import org.talend.designer.core.ui.editor.connections.ConnLabelEditPart;
import org.talend.designer.core.ui.editor.connections.ConnectionFigure;
import org.talend.designer.core.ui.editor.connections.ConnectionPart;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.nodes.NodePart;
import org.talend.designer.core.ui.editor.process.Process;

public class TalendEditorComponentCreationAssist {

    protected static Text assistText;

    private GraphicalViewer graphicViewer;

    private Control graphicControl;

    private ContentProposalAdapter contentProposalAdapter;

    /*
     * this used to disable all other key listeners registered on Display during assistant activate
     */
    protected IBindingService bindingService = null;

    protected boolean isKeyFilterEnabled = true;

    private IProcess2 process;

    protected static ConnectionFigure overedConnection = null;

    protected List<IContentProposal> proposalList;

    private String filterText;

    public TalendEditorComponentCreationAssist(String categoryName, GraphicalViewer viewer, CommandStack commandStack,
            IProcess2 process) {
        this.graphicViewer = viewer;
        this.graphicControl = viewer.getControl();
        this.process = process;
        proposalList = new ArrayList<IContentProposal>();

        Object service = DesignerPlugin.getDefault().getWorkbench().getService(IBindingService.class);
        if (service != null && service instanceof IBindingService) {
            bindingService = (IBindingService) service;
            isKeyFilterEnabled = bindingService.isKeyFilterEnabled();
        }
    }

    /**
     * open the creation assist according to the trigger character
     * 
     * @param triggerChar
     */
    public void showComponentCreationAssist(char triggerChar) {

        org.eclipse.swt.graphics.Point cursorRelativePosition = calculatePosition();
        if (cursorRelativePosition == null) {
            return;
        }

        /*
         * only one assist text at the same time in all editors
         */
        disposeAssistText();

        createAssistText(cursorRelativePosition);

        /*
         * add listeners to control when the proposal dialog shows or hide
         */
        // TODO this may need improvement.
        initListeners();

        activateAssist(triggerChar);
    }

    private org.eclipse.swt.graphics.Point calculatePosition() {
        /*
         * calculate the cursor position on current editor
         */
        org.eclipse.swt.graphics.Point cursorAbsLocation = graphicControl.getDisplay().getCursorLocation();
        org.eclipse.swt.graphics.Point cursorRelativePosition = graphicControl.getDisplay().map(null, graphicControl,
                cursorAbsLocation);
        if (!isInsideGraphic(cursorRelativePosition, graphicControl.getSize())) {
            return null;
        }
        return cursorRelativePosition;
    }

    private void activateAssist(char triggerChar) {
        // set init text content
        assistText.setText(triggerChar + "");
        assistText.setSelection(assistText.getText().length());

        // trigger proposal dialog
        Event event = new Event();
        event.character = triggerChar;
        assistText.notifyListeners(SWT.KeyDown, event);
        assistText.notifyListeners(SWT.Modify, event);
    }

    private void createAssistText(org.eclipse.swt.graphics.Point cursorRelativePosition) {
        // disable key event filter on Display
        if (bindingService != null) {
            bindingService.setKeyFilterEnabled(false);
        }

        highlightOveredConnection(cursorRelativePosition);

        // create assist input text
        assistText = new Text((Composite) graphicControl, SWT.BORDER);
        assistText.setLocation(cursorRelativePosition.x, cursorRelativePosition.y - assistText.getLineHeight());
        assistText.setSize(200, assistText.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        assistText.setFocus();

        /*
         * create the proposal by using available components list
         */
        // TODO the trigger way may need improved, currently, any visible character will trigger it
        // TalendEditorComponentProposalProvider proposalProvider = new
        // TalendEditorComponentProposalProvider(components);
        TalendEditorComponentProposalProvider proposalProvider = new TalendEditorComponentProposalProvider(this, proposalList,
                process);
        contentProposalAdapter = new ContentProposalAdapter(assistText, new TextContentAdapter(), proposalProvider, null, null);
        contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        contentProposalAdapter.setLabelProvider(new TalendEditorComponentLabelProvider(assistText));
    }

    private void highlightOveredConnection(org.eclipse.swt.graphics.Point cursorRelativePosition) {
        if (overedConnection != null) {
            overedConnection.setLineWidth(1);
            overedConnection = null;
        }
        EditPart findObjectAt = graphicViewer.findObjectAt(new Point(cursorRelativePosition.x, cursorRelativePosition.y));
        if (findObjectAt instanceof ConnectionPart) {
            overedConnection = (ConnectionFigure) ((ConnectionPart) findObjectAt).getFigure();
        } else if (findObjectAt instanceof ConnLabelEditPart) {
            overedConnection = (ConnectionFigure) ((ConnectionPart) ((ConnLabelEditPart) findObjectAt).getParent()).getFigure();
        }
        if (overedConnection != null) {
            overedConnection.setLineWidth(2);
        }
    }

    private void initListeners() {
        assistText.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.stateMask == SWT.NONE) {
                    if (e.keyCode == SWT.ESC) {
                        disposeAssistText();
                    } else if (e.keyCode == SWT.CR) {
                        acceptProposal();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });
        assistText.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                if (!(contentProposalAdapter.isProposalPopupOpen())) {
                    disposeAssistText();
                }
            }

            @Override
            public void focusGained(FocusEvent e) {

            }
        });
        contentProposalAdapter.addContentProposalListener(new IContentProposalListener2() {

            @Override
            public void proposalPopupOpened(ContentProposalAdapter adapter) {

            }

            @Override
            public void proposalPopupClosed(ContentProposalAdapter adapter) {
                filterText = assistText.getText();
                if (assistText != null && !assistText.isFocusControl()) {
                    disposeAssistText();
                }
            }
        });
        contentProposalAdapter.addContentProposalListener(new IContentProposalListener() {

            @Override
            public void proposalAccepted(IContentProposal proposal) {
                acceptProposal();
            }
        });
    }

    /**
     * create component at current position, according to select proposal label DOC talend2 Comment method
     * "createComponent".
     * 
     * @param componentName
     * @param location
     */
    protected void acceptProposal() {
        String componentName = assistText.getText().trim();
        Iterator<IContentProposal> iter = proposalList.iterator();
        IComponent component = null;
        while (iter.hasNext()) {
            IContentProposal proposal = iter.next();
            if (proposal instanceof ComponentContentProposal && componentName.equals(proposal.getLabel())) {
                component = ((ComponentContentProposal) proposal).getComponent();
                break;
            }
        }
        acceptProposal(component);
    }

    protected void acceptProposal(IComponent component) {
        if (StitchDataLoaderConstants.CONNECTOR_FAMILY_NAME.equals(component.getOriginalFamilyName())) {
            disposeAssistText();
            openBrowser(component);
        } else {
            org.eclipse.swt.graphics.Point componentLocation = assistText.getLocation();
            componentLocation.y += assistText.getLineHeight();
            disposeAssistText();
            Object createdNode = createComponent(component, componentLocation);
            selectComponent(createdNode);
        }
    }

    private void openBrowser(IComponent component) {
        if (component != null && component instanceof StitchPseudoComponent) {
            StitchPseudoComponent stitchPseudoComponent = (StitchPseudoComponent) component;
            try {
                final URL compURL =
                        new URL(stitchPseudoComponent.getConnectorURL() + StitchDataLoaderConstants.UTM_PARAM_SUFFIX);
                PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(compURL);
            } catch (PartInitException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
	}

	private void selectComponent(Object createdNode) {
        Object nodePart = graphicViewer.getEditPartRegistry().get(createdNode);
        if (nodePart != null && nodePart instanceof NodePart) {
            graphicViewer.select((EditPart) nodePart);
        }
    }

    protected Object createComponent(IComponent component, org.eclipse.swt.graphics.Point location) {
        if (component == null) {
            return null;
        }
        Object newNode;
        /*
         * TODO support to insert the component on Connection
         */
        Event e = new Event();
        e.x = location.x;
        e.y = location.y;
        e.button = 1;
        e.count = 1;
        e.stateMask = 0;
        e.widget = graphicControl;
        MouseEvent mouseEvent = new MouseEvent(e);

        TalendAssistantCreationTool creationTool = new TalendAssistantCreationTool(new PaletteComponentFactory(component,
                filterText));

        newNode = creationTool.getCreateRequest().getNewObject();
        if (!canCreateAt(newNode, new Point(e.x, e.y))) {
            return null;
        }

        creationTool.mouseMove(mouseEvent, graphicViewer);

        graphicViewer.getEditDomain().setActiveTool(creationTool);

        graphicViewer.getEditDomain().mouseMove(mouseEvent, graphicViewer);
        graphicViewer.getEditDomain().mouseDown(mouseEvent, graphicViewer);
        graphicViewer.getEditDomain().mouseUp(mouseEvent, graphicViewer);
        return newNode;
    }

    public boolean canCreateAt(Object node, Point location) {
        if (node == null || location == null) {
            MessageDialog.openWarning(graphicControl.getShell(), "Failed", "Component can't be created here");
            return false;
        }
        if (process instanceof Process && node instanceof Node) {
            NodeContainer nc = ((Process) process).loadNodeContainer((Node) node, false);
            boolean canExecute = new CreateNodeContainerCommand((Process) process, nc, location).canExecute();
            if (!canExecute) {
                MessageDialog.openWarning(graphicControl.getShell(), "Failed", "Component can't be created here");
            }
            return canExecute;
        }
        return true;
    }

    /*
     * this used to judge the cursor is inside the editor or not
     */
    private boolean isInsideGraphic(org.eclipse.swt.graphics.Point cursorPosition, org.eclipse.swt.graphics.Point graphicSize) {
        if (cursorPosition.x < 0 || cursorPosition.y < 0) {
            return false;
        }
        if (cursorPosition.x > graphicSize.x || cursorPosition.y > graphicSize.y) {
            return false;
        }
        return true;
    }

    protected void disposeAssistText() {
        if (assistText != null && !assistText.isDisposed()) {
            assistText.dispose();
        }
        assistText = null;
        // restore key event filter on Display
        if (bindingService != null) {
            bindingService.setKeyFilterEnabled(isKeyFilterEnabled);
        }
        if (overedConnection != null) {
            overedConnection.setLineWidth(1);
            overedConnection = null;
        }
    }

    protected List<IComponent> filterComponents(List<IComponent> components) {
        return components;
    }

    class TalendAssistantCreationTool extends CreationTool {

        private CreateRequest request = null;

        public TalendAssistantCreationTool(CreationFactory aFactory) {
            super(aFactory);
        }

        @Override
        public CreateRequest getCreateRequest() {
            if (request == null) {
                request = super.getCreateRequest();
            }
            return request;
        }
    }

}
