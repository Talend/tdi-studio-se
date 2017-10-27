// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.componentdesigner.model.componentpref;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.talend.componentdesigner.ComponentDesigenerPlugin;
import org.talend.componentdesigner.PluginConstant;
import org.talend.componentdesigner.preference.PreferenceConstant;

/**
 * This class is a collection of ComponentPref. It can be add/remove/save the ComponentPref.
 * <br/>
 *
 */
public final class ComponentPrefCollection {

    private static final String PREF_DELIMITER = "|"; //$NON-NLS-1$
    
    private List<ComponentPref> componentPrefSet;
    
//    private List<String> componentNameSet;

    /** The preference store manipulated. */
    private IPreferenceStore store;
    
    private static ComponentPrefCollection componentPrefCollection = new ComponentPrefCollection();

    public static ComponentPrefCollection getInstance() {
        return componentPrefCollection;
    }

    private ComponentPrefCollection() {
        initComponentsPref();
    }
    
    private void initComponentsPref() {
        store = ComponentDesigenerPlugin.getDefault().getPreferenceStore();
        this.componentPrefSet = componentPrefs();
    } 
    

    /**
     * Read the componentPrefs from the preferencestore.
     * @return
     */
    private List<ComponentPref> componentPrefs() {
        List<ComponentPref> componentPrefList = new ArrayList<ComponentPref>();
        for (String currentConnectionToLoad : readStringArray(PreferenceConstant.COMPONENTS_PROPERTY)) {
            componentPrefList.add(ComponentPref.writeFromString(currentConnectionToLoad));
        }
        return componentPrefList;
    }
    
    /**
     * Get the componentPref list.
     * @return
     */
    public ComponentPref[] getComponentPrefs() {
        ComponentPref[] componentPrefs = new ComponentPref[componentPrefSet.size()];
        return componentPrefSet.toArray(componentPrefs);
    }
    
 
    /**
     * Save a string array in the preference store.
     * 
     * @param prefArray Preferences to be saved.
     * @param prefName Name of the preference to be saved.
     */
    private void saveStringArray(final String[] prefArray, String prefName) {
        StringBuffer prefs = new StringBuffer(256);
        for (int i = 0; i < prefArray.length; i++) {
            prefs.append(prefArray[i]);
            if (i < prefArray.length - 1) {
                prefs.append(PREF_DELIMITER);
            }
        }
        store.setValue(prefName, prefs.toString());
    }
    
    /**
     * Read a string array in the preference store.
     * 
     * @param prefName Name of the preference to be read.
     * @return an array of strings.
     */
    private String[] readStringArray(final String prefName) {
        List<String> strings = readStringList(prefName);

        String[] array = new String[strings.size()];
        array = strings.toArray(array);
        return array;
    }
    
    private List<String> readStringList(final String prefName) {
        String prefs = store.getString(prefName);
        List<String> strings = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(prefs, PREF_DELIMITER);
        while (st.hasMoreTokens()) {
            strings.add(st.nextToken());
        }

        return strings;
    }

   
    
    /**
     * Get  the componentPref by the special ComponentPref name.
     * @param componentName
     * @return
     */
    public ComponentPref getComponentPref(String componentName) {
        ComponentPref matchcomponentPref = null;
        for (ComponentPref componentPref : componentPrefSet) {
            if (componentName.equals(componentPref.getName())) {
                matchcomponentPref = componentPref;
                break;
            }
        }
        return matchcomponentPref;
    } 
        
    
    public void save(ComponentPref componentPref) {
		this.addComponentPref(componentPref);
		this.saveComponentPrefs();
	}
    
    private void addComponentPref(ComponentPref componentPref) {
		checkAllRmoveComponents();
        if (!componentPrefSet.contains(componentPref) && getComponentPref(componentPref.getName()) == null) {
            componentPrefSet.add(componentPref);
        }
    }
    
    private void checkAllRmoveComponents() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				PluginConstant.COMPONENT_PROJECT);
		Iterator<ComponentPref> it = componentPrefSet.iterator();
        while (it.hasNext()) {
        	ComponentPref cf = it.next();
			if (!project.getFolder(cf.getName()).exists()) {
				it.remove();
			}
		}
    }
    
    /**
     * Save for the componentPrefs.
     */
    private void saveComponentPrefs() {
        String[] prefArray = new String[this.componentPrefSet.size()];
        int i = 0;
        for (ComponentPref componentPref : this.componentPrefSet) {
            prefArray[i++] = componentPref.readToString();
        }
        saveStringArray(prefArray, PreferenceConstant.COMPONENTS_PROPERTY);
    }
}

