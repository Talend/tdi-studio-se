/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.talend.designer.mapper.model.emf.mapper.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.talend.designer.mapper.model.emf.mapper.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class MapperFactoryImpl extends EFactoryImpl implements MapperFactory {
    /**
     * Creates the default factory implementation.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static MapperFactory init() {
        try {
            MapperFactory theMapperFactory = (MapperFactory)EPackage.Registry.INSTANCE.getEFactory(MapperPackage.eNS_URI);
            if (theMapperFactory != null) {
                return theMapperFactory;
            }
        }
        catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new MapperFactoryImpl();
    }

    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MapperFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case MapperPackage.MAPPER_DATA: return createMapperData();
            case MapperPackage.MAPPER_TABLE_ENTRY: return createMapperTableEntry();
            case MapperPackage.UI_PROPERTIES: return createUiProperties();
            case MapperPackage.VAR_TABLE: return createVarTable();
            case MapperPackage.OUTPUT_TABLE: return createOutputTable();
            case MapperPackage.INPUT_TABLE: return createInputTable();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object createFromString(EDataType eDataType, String initialValue) {
        switch (eDataType.getClassifierID()) {
            case MapperPackage.SIZE_STATE:
                return createSizeStateFromString(eDataType, initialValue);
            case MapperPackage.OPERATOR:
                return createOperatorFromString(eDataType, initialValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public String convertToString(EDataType eDataType, Object instanceValue) {
        switch (eDataType.getClassifierID()) {
            case MapperPackage.SIZE_STATE:
                return convertSizeStateToString(eDataType, instanceValue);
            case MapperPackage.OPERATOR:
                return convertOperatorToString(eDataType, instanceValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MapperData createMapperData() {
        MapperDataImpl mapperData = new MapperDataImpl();
        return mapperData;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MapperTableEntry createMapperTableEntry() {
        MapperTableEntryImpl mapperTableEntry = new MapperTableEntryImpl();
        return mapperTableEntry;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public UiProperties createUiProperties() {
        UiPropertiesImpl uiProperties = new UiPropertiesImpl();
        return uiProperties;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public VarTable createVarTable() {
        VarTableImpl varTable = new VarTableImpl();
        return varTable;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public OutputTable createOutputTable() {
        OutputTableImpl outputTable = new OutputTableImpl();
        return outputTable;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public InputTable createInputTable() {
        InputTableImpl inputTable = new InputTableImpl();
        return inputTable;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public SizeState createSizeStateFromString(EDataType eDataType, String initialValue) {
        SizeState result = SizeState.get(initialValue);
        if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
        return result;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertSizeStateToString(EDataType eDataType, Object instanceValue) {
        return instanceValue == null ? null : instanceValue.toString();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Operator createOperatorFromString(EDataType eDataType, String initialValue) {
        Operator result = Operator.get(initialValue);
        if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
        return result;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public String convertOperatorToString(EDataType eDataType, Object instanceValue) {
        return instanceValue == null ? null : instanceValue.toString();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public MapperPackage getMapperPackage() {
        return (MapperPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    @Deprecated
    public static MapperPackage getPackage() {
        return MapperPackage.eINSTANCE;
    }

} //MapperFactoryImpl
