/**
 * <copyright> </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a create method for each non-abstract class of
 * the model. <!-- end-user-doc -->
 * @see org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage
 * @generated
 */
public interface DbmapFactory extends EFactory {

    /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    DbmapFactory eINSTANCE = org.talend.designer.dbmap.model.emf.dbmap.impl.DbmapFactoryImpl.init();

    /**
     * Returns a new object of class '<em>DB Map Data</em>'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return a new object of class '<em>DB Map Data</em>'.
     * @generated
     */
    DBMapData createDBMapData();

    /**
     * Returns a new object of class '<em>DB Mapper Table Entry</em>'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return a new object of class '<em>DB Mapper Table Entry</em>'.
     * @generated
     */
    DBMapperTableEntry createDBMapperTableEntry();

    /**
     * Returns a new object of class '<em>Abstract DB Data Map Table</em>'. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @return a new object of class '<em>Abstract DB Data Map Table</em>'.
     * @generated
     */
    AbstractDBDataMapTable createAbstractDBDataMapTable();

    /**
     * Returns a new object of class '<em>Abstace DB In Out Table</em>'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return a new object of class '<em>Abstace DB In Out Table</em>'.
     * @generated
     */
    AbstaceDBInOutTable createAbstaceDBInOutTable();

    /**
     * Returns a new object of class '<em>Var Table</em>'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return a new object of class '<em>Var Table</em>'.
     * @generated
     */
    VarTable createVarTable();

    /**
     * Returns a new object of class '<em>Input Table</em>'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return a new object of class '<em>Input Table</em>'.
     * @generated
     */
    InputTable createInputTable();

    /**
     * Returns a new object of class '<em>Output Table</em>'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return a new object of class '<em>Output Table</em>'.
     * @generated
     */
    OutputTable createOutputTable();

    /**
     * Returns a new object of class '<em>Filter Entry</em>'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return a new object of class '<em>Filter Entry</em>'.
     * @generated
     */
    FilterEntry createFilterEntry();

    /**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
    DbmapPackage getDbmapPackage();

} // DbmapFactory
