
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour MObjFieldMetadata complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MObjFieldMetadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="displayName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="sourceObject" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="dataType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="size" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="isReadonly" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isUpdateBlocked" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isName" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isPrimaryKey" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isCustom" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="isDynamic" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="dynamicFieldRef" type="{http://www.w3.org/2001/XMLSchema}string"/>
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
@XmlType(name = "MObjFieldMetadata", propOrder = {
    "name",
    "description",
    "displayName",
    "sourceObject",
    "dataType",
    "size",
    "isReadonly",
    "isUpdateBlocked",
    "isName",
    "isPrimaryKey",
    "isCustom",
    "isDynamic",
    "dynamicFieldRef",
    "updatedAt"
})
public class MObjFieldMetadata {

    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true, nillable = true)
    protected String description;
    @XmlElement(required = true, nillable = true)
    protected String displayName;
    @XmlElement(required = true, nillable = true)
    protected String sourceObject;
    @XmlElement(required = true)
    protected String dataType;
    @XmlElement(required = true, type = Integer.class, nillable = true)
    protected Integer size;
    protected boolean isReadonly;
    protected boolean isUpdateBlocked;
    @XmlElement(required = true, type = Boolean.class, nillable = true)
    protected Boolean isName;
    protected boolean isPrimaryKey;
    protected boolean isCustom;
    protected boolean isDynamic;
    @XmlElement(required = true, nillable = true)
    protected String dynamicFieldRef;
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
     * Obtient la valeur de la propriété displayName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Définit la valeur de la propriété displayName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisplayName(String value) {
        this.displayName = value;
    }

    /**
     * Obtient la valeur de la propriété sourceObject.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceObject() {
        return sourceObject;
    }

    /**
     * Définit la valeur de la propriété sourceObject.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceObject(String value) {
        this.sourceObject = value;
    }

    /**
     * Obtient la valeur de la propriété dataType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Définit la valeur de la propriété dataType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataType(String value) {
        this.dataType = value;
    }

    /**
     * Obtient la valeur de la propriété size.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Définit la valeur de la propriété size.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSize(Integer value) {
        this.size = value;
    }

    /**
     * Obtient la valeur de la propriété isReadonly.
     * 
     */
    public boolean isIsReadonly() {
        return isReadonly;
    }

    /**
     * Définit la valeur de la propriété isReadonly.
     * 
     */
    public void setIsReadonly(boolean value) {
        this.isReadonly = value;
    }

    /**
     * Obtient la valeur de la propriété isUpdateBlocked.
     * 
     */
    public boolean isIsUpdateBlocked() {
        return isUpdateBlocked;
    }

    /**
     * Définit la valeur de la propriété isUpdateBlocked.
     * 
     */
    public void setIsUpdateBlocked(boolean value) {
        this.isUpdateBlocked = value;
    }

    /**
     * Obtient la valeur de la propriété isName.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsName() {
        return isName;
    }

    /**
     * Définit la valeur de la propriété isName.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsName(Boolean value) {
        this.isName = value;
    }

    /**
     * Obtient la valeur de la propriété isPrimaryKey.
     * 
     */
    public boolean isIsPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * Définit la valeur de la propriété isPrimaryKey.
     * 
     */
    public void setIsPrimaryKey(boolean value) {
        this.isPrimaryKey = value;
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
     * Obtient la valeur de la propriété isDynamic.
     * 
     */
    public boolean isIsDynamic() {
        return isDynamic;
    }

    /**
     * Définit la valeur de la propriété isDynamic.
     * 
     */
    public void setIsDynamic(boolean value) {
        this.isDynamic = value;
    }

    /**
     * Obtient la valeur de la propriété dynamicFieldRef.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDynamicFieldRef() {
        return dynamicFieldRef;
    }

    /**
     * Définit la valeur de la propriété dynamicFieldRef.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDynamicFieldRef(String value) {
        this.dynamicFieldRef = value;
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
