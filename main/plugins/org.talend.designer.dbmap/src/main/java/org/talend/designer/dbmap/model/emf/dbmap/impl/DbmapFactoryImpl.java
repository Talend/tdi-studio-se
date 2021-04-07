/**
 * <copyright> </copyright>
 *
 * $Id$
 */
package org.talend.designer.dbmap.model.emf.dbmap.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.talend.designer.dbmap.model.emf.dbmap.*;
import org.talend.designer.dbmap.model.emf.dbmap.AbstaceDBInOutTable;
import org.talend.designer.dbmap.model.emf.dbmap.AbstractDBDataMapTable;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapData;
import org.talend.designer.dbmap.model.emf.dbmap.DBMapperTableEntry;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapFactory;
import org.talend.designer.dbmap.model.emf.dbmap.DbmapPackage;
import org.talend.designer.dbmap.model.emf.dbmap.FilterEntry;
import org.talend.designer.dbmap.model.emf.dbmap.InputTable;
import org.talend.designer.dbmap.model.emf.dbmap.OutputTable;
import org.talend.designer.dbmap.model.emf.dbmap.VarTable;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!-- end-user-doc -->
 * @generated
 */
public class DbmapFactoryImpl extends EFactoryImpl implements DbmapFactory {

    /**
     * Creates the default factory implementation.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public static DbmapFactory init() {
        try {
            DbmapFactory theDbmapFactory = (DbmapFactory)EPackage.Registry.INSTANCE.getEFactory(DbmapPackage.eNS_URI);
            if (theDbmapFactory != null) {
                return theDbmapFactory;
            }
        }
        catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new DbmapFactoryImpl();
    }

    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    public DbmapFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case DbmapPackage.DB_MAP_DATA: return createDBMapData();
            case DbmapPackage.DB_MAPPER_TABLE_ENTRY: return createDBMapperTableEntry();
            case DbmapPackage.ABSTRACT_DB_DATA_MAP_TABLE: return createAbstractDBDataMapTable();
            case DbmapPackage.ABSTACE_DB_IN_OUT_TABLE: return createAbstaceDBInOutTable();
            case DbmapPackage.VAR_TABLE: return createVarTable();
            case DbmapPackage.INPUT_TABLE: return createInputTable();
            case DbmapPackage.OUTPUT_TABLE: return createOutputTable();
            case DbmapPackage.FILTER_ENTRY: return createFilterEntry();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public DBMapData createDBMapData() {
        DBMapDataImpl dbMapData = new DBMapDataImpl();
        return dbMapData;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public DBMapperTableEntry createDBMapperTableEntry() {
        DBMapperTableEntryImpl dbMapperTableEntry = new DBMapperTableEntryImpl();
        return dbMapperTableEntry;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public AbstractDBDataMapTable createAbstractDBDataMapTable() {
        AbstractDBDataMapTableImpl abstractDBDataMapTable = new AbstractDBDataMapTableImpl();
        return abstractDBDataMapTable;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public AbstaceDBInOutTable createAbstaceDBInOutTable() {
        AbstaceDBInOutTableImpl abstaceDBInOutTable = new AbstaceDBInOutTableImpl();
        return abstaceDBInOutTable;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public VarTable createVarTable() {
        VarTableImpl varTable = new VarTableImpl();
        return varTable;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public InputTable createInputTable() {
        InputTableImpl inputTable = new InputTableImpl();
        return inputTable;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public OutputTable createOutputTable() {
        OutputTableImpl outputTable = new OutputTableImpl();
        return outputTable;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public FilterEntry createFilterEntry() {
        FilterEntryImpl filterEntry = new FilterEntryImpl();
        return filterEntry;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    public DbmapPackage getDbmapPackage() {
        return (DbmapPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    @Deprecated
    public static DbmapPackage getPackage() {
        return DbmapPackage.eINSTANCE;
    }

} // DbmapFactoryImpl
