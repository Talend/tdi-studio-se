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
package org.talend.xml.sax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.xml.sax.function.inter.Function;

/**
 * This is the result class.
 *
 * $Id: LoopEntry.java,v 1.1 2008/03/21 07:20:39 xzhang Exp $
 *
 */
public class LoopEntry {

    // loop full path
    private String loop;

    // node full paths
    private List<String> paths = new ArrayList<String>();

    // ==================add for the feature10170==========================
    private List<Boolean> asXMLs = new ArrayList<Boolean>();

    // ==========================end========================================

    private List<Boolean> isDots = new ArrayList<Boolean>();

    // node original path
    private List<String> originalPaths = new ArrayList<String>();

    // result of read, notice the last element of row is a count of sub loop row.
    private List<String[]> rows = new ArrayList<String[]>();;

    // sub loop entry, make this class as a linkedlist
    private LoopEntry subLoop;

    public LoopEntry(String loop) {
        this.loop = loop;
    }

    // =================add for bug7632 start========================
    private String originalLoopPath = null;

    public void setOriginalLoopPath(String originalLoopPath) {
        this.originalLoopPath = originalLoopPath;
    }

    public String getOriginalLoopPath() {
        return this.originalLoopPath;
    }

    // =====================bug7632 end==============================

    /**
     * This use in init LoopEntry object. DOC s Comment method "addPath".
     *
     * @param path
     * @param originalPath
     */
    public void addPath(String path, String originalPath) {
        paths.add(path);
        originalPaths.add(originalPath);
    }

    public void addPath(String path, String originalPath, Boolean asXML, boolean isDot) {
        paths.add(path);
        originalPaths.add(originalPath);
        // ===========add for feature10170===================
        asXMLs.add(asXML);
        // ===============end=================================
        isDots.add(isDot);
    }

    public List<Boolean> getIsDots() {
    	 if (this.isDots.size() > 0) {
             return this.isDots;
         } else {
             for (int i = 0; i < this.paths.size(); i++) {
                 this.isDots.add(false);
             }
             return this.isDots;
         }
    }

    public void setAsXMLs(boolean[] asXML) {
        for (int i = 0; i < asXML.length; i++) {
            this.asXMLs.add(asXML[i]);
        }
    }

    public List<Boolean> getAsXMLs() {
        if (this.asXMLs.size() > 0) {
            return this.asXMLs;
        } else {
            for (int i = 0; i < this.paths.size(); i++) {
                this.asXMLs.add(false);
            }
            return this.asXMLs;
        }
    }

    /**
     * Getter for loop.
     *
     * @return the loop
     */
    public String getLoop() {
        return this.loop;
    }

    /**
     * Getter for subLoop.
     *
     * @return the subLoop
     */
    public LoopEntry getSubLoop() {
        return this.subLoop;
    }

    /**
     * Sets the subLoop.
     *
     * @param subLoop the subLoop to set
     */
    public void setSubLoop(LoopEntry subLoop) {
        this.subLoop = subLoop;
    }

    /**
     * Getter for paths.
     *
     * @return the paths
     */
    public List<String> getPaths() {
        return this.paths;
    }

    /**
     * Getter for originalPaths.
     *
     * @return the originalPaths
     */
    public List<String> getOriginalPaths() {
        return this.originalPaths;
    }

    /**
     * Getter for rows.
     *
     * @return the rows
     */
    public List<String[]> getRows() {
        return this.rows;
    }

    private Map<String, Function> listFuncs = new HashMap<String, Function>();

    private List<Map<String, String>> results = new ArrayList<Map<String, String>>();

    public void addFunction(String originalPath, Function func) {
        listFuncs.put(originalPath, func);
    }

    public Map<String, Function> getFunctions() {
        return listFuncs;
    }

    public boolean hasFunctions() {
        return listFuncs.size() > 0;
    }

    /**
     * run all the exist functions in the loop
     *
     * @author wliu
     * @param args
     */
    public void execFunctions(List<Map<String, Object>> args) {
        for (String funcName : listFuncs.keySet()) {
            listFuncs.get(funcName).call(args);
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", funcName);
            map.put("value", listFuncs.get(funcName).getResult());
            results.add(map);
        }
    }

    public List<Map<String, String>> getFunctionResults() {
        return results;
    }

}
