/**
 * <copyright> </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Output Table</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.talend.designer.dbmap.model.emf.dbmap.OutputTable#getFilterEntries <em>Filter Entries</em>}</li>
 * </ul>
 *
 * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getOutputTable()
 * @model
 * @generated
 */
public interface OutputTable extends AbstaceDBInOutTable {

    /**
     * Returns the value of the '<em><b>Filter Entries</b></em>' containment reference list.
     * The list contents are of type {@link org.talend.designer.dbmap.model.emf.dbmap.FilterEntry}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Filter Entries</em>' containment reference list isn't clear, there really should be
     * more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Filter Entries</em>' containment reference list.
     * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage#getOutputTable_FilterEntries()
     * @model containment="true"
     * @generated
     */
    EList<FilterEntry> getFilterEntries();

} // OutputTable
