
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour MObjectMetadata complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MObjectMetadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="isCustom" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isVirtual" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="fieldList" type="{http://www.marketo.com/mktows/}ArrayOfMObjFieldMetadata"/>
 *         &lt;element name="updatedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MObjectMetadata", propOrder = {
    "name",
    "description",
    "isCustom",
    "isVirtual",
    "fieldList",
    "updatedAt"
})
public class MObjectMetadata {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true, nillable = true)
    protected String description;
    protected boolean isCustom;
    protected boolean isVirtual;
    @XmlElement(required = true)
    protected ArrayOfMObjFieldMetadata fieldList;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updatedAt;

    /**
     * Obtient la valeur de la propriété name.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Définit la valeur de la propriété name.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Obtient la valeur de la propriété description.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Définit la valeur de la propriété description.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Obtient la valeur de la propriété isCustom.
     * 
     */
    public boolean isIsCustom() {
        return isCustom;
    }

    /**
     * Définit la valeur de la propriété isCustom.
     * 
     */
    public void setIsCustom(boolean value) {
        this.isCustom = value;
    }

    /**
     * Obtient la valeur de la propriété isVirtual.
     * 
     */
    public boolean isIsVirtual() {
        return isVirtual;
    }

    /**
     * Définit la valeur de la propriété isVirtual.
     * 
     */
    public void setIsVirtual(boolean value) {
        this.isVirtual = value;
    }

    /**
     * Obtient la valeur de la propriété fieldList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMObjFieldMetadata }
     *     
     */
    public ArrayOfMObjFieldMetadata getFieldList() {
        return fieldList;
    }

    /**
     * Définit la valeur de la propriété fieldList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMObjFieldMetadata }
     *     
     */
    public void setFieldList(ArrayOfMObjFieldMetadata value) {
        this.fieldList = value;
    }

    /**
     * Obtient la valeur de la propriété updatedAt.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Définit la valeur de la propriété updatedAt.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUpdatedAt(XMLGregorianCalendar value) {
        this.updatedAt = value;
    }

}
