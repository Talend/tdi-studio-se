// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.model.repository.SVNConstant;
import org.talend.repository.i18n.Messages;

/**
 * DOC zyuan class global comment. Detailled comment
 */
public class BranchesSelectionDialog extends Dialog {

    private FilteredTree branchesTree;

    private List<String> remoteBranches;

    private String selectedBranch;

    Image branchImage, tagImage;

    public BranchesSelectionDialog(Shell parentShell, String currentBranch, List<String> remoteBranches,
            boolean isSupportRemoteMode) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
        this.remoteBranches = remoteBranches;
    }

    private void initUiData() {
        branchesTree.getViewer().setInput(generateBranchInfos(remoteBranches));
    }

    private List<BranchInfo> generateBranchInfos(List<String> brancheNames) {
        List<BranchInfo> roots = new ArrayList<>();
        BranchInfo branchesRoot = new BranchInfo();
        BranchInfo tagsRoot = new BranchInfo();
        List<BranchInfo> childrenBranches = new ArrayList<>();
        List<BranchInfo> childrenTags = new ArrayList<>();
        int namemaxlen = 0;

        tagImage = ImageProvider.getImage(EImage.BRANCHES_ICON);
        branchImage = ImageProvider.getImage(EImage.BRANCHES_ICON);
        branchesRoot.setImage(branchImage);
        branchesRoot.setDisplayName(Messages.getString("RemoteBranchesDialog.tree.branch") + "                ");
        branchesRoot.setOriginalName(Messages.getString("RemoteBranchesDialog.tree.branch"));

        tagsRoot.setImage(tagImage);
        tagsRoot.setDisplayName(Messages.getString("RemoteBranchesDialog.tree.tag"));
        tagsRoot.setOriginalName(Messages.getString("RemoteBranchesDialog.tree.tag"));
        if (brancheNames != null) {
            for (String branchName : brancheNames) {
                namemaxlen = Math.max(namemaxlen, branchName.length());
                if (branchName.startsWith(SVNConstant.NAME_TAGS + SVNConstant.SEP_CHAR)) {
                    BranchInfo tagLeaf = new BranchInfo();
                    tagLeaf.setParent(tagsRoot);
                    tagLeaf.setImage(tagImage);
                    tagLeaf.setDisplayName(branchName);
                    tagLeaf.setOriginalName(branchName);
                    childrenTags.add(tagLeaf);
                } else {
                    BranchInfo branchLeaf = new BranchInfo();
                    branchLeaf.setParent(branchesRoot);
                    branchLeaf.setImage(branchImage);
                    branchLeaf.setDisplayName(branchName);
                    branchLeaf.setOriginalName(branchName);
                    childrenBranches.add(branchLeaf);
                }
            }
        }
        //for incomplete display on Mac OS
        branchesRoot.setDisplayName(StringUtils.rightPad(Messages.getString("RemoteBranchesDialog.tree.branch"), namemaxlen + 20));
        branchesRoot.setChildren(childrenBranches);
        tagsRoot.setChildren(childrenTags);
        roots.add(branchesRoot);
        roots.add(tagsRoot);
        return roots;
    }
    
    private void addListeners() {
        branchesTree.getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                onBranchesTreeSelection(event);
            }
        });

    }

    private void onBranchesTreeSelection(SelectionChangedEvent event) {
        Object firstElement = event.getStructuredSelection().getFirstElement();
        if (firstElement instanceof BranchInfo) {
            BranchInfo bi = (BranchInfo) firstElement;
            if (bi.getChildren() != null) {
                getButton(IDialogConstants.OK_ID).setEnabled(false);
            } else {
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            }
        }
    }
    
    public String getSelection() {
        return selectedBranch;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Shell shell = this.getShell();
        if (shell != null) {
            shell.setText(Messages.getString("RemoteBranchesDialog.title"));
        }
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayoutData(new GridData(GridData.FILL_BOTH));

        final int margin = 0;
        FormLayout layout = new FormLayout();
        layout.marginHeight = margin;
        layout.marginWidth = margin;
        panel.setLayout(layout);

        branchesTree = new BranchesFilteredTree(panel, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE, new PatternFilter(),
                true);
        FormData branchesTreeLayoutData = new FormData();
        branchesTreeLayoutData.top = new FormAttachment(0, 0);
        branchesTreeLayoutData.left = new FormAttachment(0, 0);
        branchesTreeLayoutData.right = new FormAttachment(200, 0);
        branchesTreeLayoutData.bottom = new FormAttachment(100, 0);
        branchesTree.setLayoutData(branchesTreeLayoutData);
        TreeViewer branchesViewer = branchesTree.getViewer();
        branchesViewer.setContentProvider(new BranchesContentProvider());
        branchesViewer.setLabelProvider(new BranchesLabelProvider());

        applyDialogFont(panel);
        addListeners();
        initUiData();
        return panel;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    protected void okPressed() {
        Object selection = this.branchesTree.getViewer().getStructuredSelection().getFirstElement();
        BranchInfo bi = (BranchInfo) selection;
        if (bi != null && bi.getChildren() == null) {
            selectedBranch = bi.getDisplayName();
        }
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        try {
            return new Point(500, 400);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return super.getInitialSize();
    }

    @Override
    public boolean close() {
        if (branchImage != null) {
            branchImage.dispose();
            branchImage = null;
        }
        if (tagImage != null) {
            tagImage.dispose();
            tagImage = null;
        }
        return super.close();
    }

    private class BranchInfo {

        private String originalName;

        private String displayName;

        private BranchInfo parent;

        private List<BranchInfo> children;

        private Image image;

        public String getOriginalName() {
            return originalName;
        }

        public void setOriginalName(String originalName) {
            this.originalName = originalName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Image getImage() {
            return image;
        }

        public void setImage(Image image) {
            this.image = image;
        }

        public BranchInfo getParent() {
            return parent;
        }

        public void setParent(BranchInfo parent) {
            this.parent = parent;
        }

        public List<BranchInfo> getChildren() {
            return children;
        }

        public void setChildren(List<BranchInfo> children) {
            this.children = children;
        }

        public boolean hasChildren() {
            if (children != null && !children.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }

    }

    private class BranchesContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Collection) {
                return ((Collection) inputElement).toArray();
            } else if (inputElement instanceof Object[]) {
                return (Object[]) inputElement;
            }
            return new Object[] { inputElement };
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof BranchInfo) {
                List<BranchInfo> children = ((BranchInfo) parentElement).getChildren();
                if (children != null) {
                    return children.toArray();
                }
            }
            return null;
        }

        @Override
        public Object getParent(Object element) {
            if (element instanceof BranchInfo) {
                return ((BranchInfo) element).getParent();
            }
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof BranchInfo) {
                return ((BranchInfo) element).hasChildren();
            }
            return false;
        }

    }

    private class BranchesLabelProvider extends LabelProvider {

        @Override
        public Image getImage(Object element) {
            if (element instanceof BranchInfo) {
                return ((BranchInfo) element).getImage();
            }
            return super.getImage(element);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof BranchInfo) {
                return ((BranchInfo) element).getDisplayName();
            }
            return ""; //$NON-NLS-1$
        }

    }

    // FilteredTree can not display well when PlatformUI.isWorkbenchRunning() returns false.
    // Use user defined filtered tree in this scenario.
    private class BranchesFilteredTree extends FilteredTree {

        BranchesSelectionFilter textFilter = new BranchesSelectionFilter();

        public BranchesFilteredTree(Composite parent, int treeStyle, PatternFilter filter, boolean useNewLook) {
            super(parent, treeStyle, filter, useNewLook);
            if (!PlatformUI.isWorkbenchRunning())
                treeViewer.setFilters(textFilter);
        }

        @Override
        protected void textChanged() {
            super.textChanged();
            if (!PlatformUI.isWorkbenchRunning()) {
                if (StringUtils.equals(getFilterString(), this.getInitialText()))
                    return;
                if ( !filterText.isFocusControl()) return;
                textFilter.setText(getFilterString());
                treeViewer.refresh();
                if (!StringUtils.isBlank(getFilterString())) {
                    treeViewer.expandAll();
                }
            }
        }
    }

    private class BranchesSelectionFilter extends ViewerFilter {

        private String pattern = null;

        public void setText(String pattern) {
            if (pattern != null) {
                try {
                    Pattern.compile(".*" + pattern + ".*");
                    this.pattern = ".*" + pattern + ".*";
                } catch (PatternSyntaxException e) {
                    this.pattern = pattern;
                }
            }
        }

        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            if (pattern == null || pattern.equals("") || pattern.equals("*")) { //$NON-NLS-1$
                return true;
            }
            if (!(element instanceof BranchInfo)) {
                return true;
            }
            BranchInfo branchInfo = (BranchInfo) element;
            if (branchInfo == null || branchInfo.getChildren() != null) {
                return true;
            }
            try {
                Pattern.compile(pattern);
            } catch (PatternSyntaxException e) {
                return false;
            }
            return branchInfo.getDisplayName().matches(pattern);
        }
    }
}
