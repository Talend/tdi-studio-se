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
package org.talend.repository.ui.login.connections;


/**
 * DOC hwang class global comment. Detailled comment
 */
public class SVNBranchPerReader {

    // private static SVNBranchPerReader svnReader = new SVNBranchPerReader();
    //
    // // private static ConnectionUserPerReader con = new ConnectionUserPerReader();
    //    private String perfileName = "SVN.userprop"; //$NON-NLS-1$
    //
    // private String path = null;
    //
    // private File perfile = null;
    //
    // private Properties proper = null;
    //
    // private boolean isRead;
    //
    // private SVNBranchPerReader() {
    // proper = new Properties();
    // isRead = false;
    // Project project = ProjectManager.getInstance().getCurrentProject();
    // IWorkspace workspace = ResourcesPlugin.getWorkspace();
    // IProject eclipseProject = workspace.getRoot().getProject(project.getTechnicalLabel());
    //
    // String urlPath = eclipseProject.getLocation().toOSString();
    // path = new Path(urlPath).toFile().getAbsolutePath();
    // String tmp = String.valueOf(path.charAt(path.length() - 1));
    // if (!tmp.equals(File.separator)) {
    // perfile = new File(path + File.separator + perfileName);
    // } else {
    // perfile = new File(path + perfileName);
    // }
    // }
    //
    // public static SVNBranchPerReader getInstance() {
    // synchronized (svnReader) {
    // if (svnReader == null)
    // svnReader = new SVNBranchPerReader();
    // return svnReader;
    // }
    // }
    //
    // public void saveBranch(String branch) {
    // if (!isHaveUserPer()) {
    // createPropertyFile();
    // }
    //        proper.setProperty("SVN.lastBranch", branch);//$NON-NLS-1$
    // FileOutputStream out;
    // try {
    // out = new FileOutputStream(perfile);
    // proper.store(out, null);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    //
    // public String getBranch() {
    // if (!isRead)
    // readProperties();
    // String branch = proper.getProperty("SVN.lastBranch");
    // return branch;
    // }
    //
    // private void readProperties() {
    // try {
    // proper.load(new FileInputStream(perfile));
    // isRead = true;
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // // return this;
    // }
    //
    // public void createPropertyFile() {
    // File fatherFloder = new File(path);
    // if (!fatherFloder.exists()) {
    // fatherFloder.mkdirs();
    // }
    // try {
    // if (!perfile.exists()) {
    // perfile.createNewFile();
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    //
    // }
    //
    // public boolean isHaveUserPer() {
    // return perfile.exists();
    // }
    //
    // public void deleteFile() {
    // if (perfile.exists()) {
    // perfile.delete();
    // }
    // }

}
