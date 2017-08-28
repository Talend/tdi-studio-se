
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour MObject complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="MObject">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="externalKey" type="{http://www.marketo.com/mktows/}Attrib" minOccurs="0"/>
 *         &lt;element name="createdAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="updatedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="attribList" type="{http://www.marketo.com/mktows/}ArrayOfAttrib" minOccurs="0"/>
 *         &lt;element name="typeAttribList" type="{http://www.marketo.com/mktows/}ArrayOfTypeAttrib" minOccurs="0"/>
 *         &lt;element name="associationList" type="{http://www.marketo.com/mktows/}ArrayOfMObjAssociation" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MObject", propOrder = {
    "type",
    "id",
    "externalKey",
    "createdAt",
    "updatedAt",
    "attribList",
    "typeAttribList",
    "associationList"
})
public class MObject {

    @XmlElement(required = true)
    protected String type;
    protected Integer id;
    protected Attrib externalKey;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createdAt;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updatedAt;
    protected ArrayOfAttrib attribList;
    protected ArrayOfTypeAttrib typeAttribList;
    protected ArrayOfMObjAssociation associationList;

    /**
     * Obtient la valeur de la propriété type.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Définit la valeur de la propriété type.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Obtient la valeur de la propriété id.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getId() {
        return id;
    }

    /**
     * Définit la valeur de la propriété id.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setId(Integer value) {
        this.id = value;
    }

    /**
     * Obtient la valeur de la propriété externalKey.
     * 
     * @return
     *     possible object is
     *     {@link Attrib }
     *     
     */
    public Attrib getExternalKey() {
        return externalKey;
    }

    /**
     * Définit la valeur de la propriété externalKey.
     * 
     * @param value
     *     allowed object is
     *     {@link Attrib }
     *     
     */
    public void setExternalKey(Attrib value) {
        this.externalKey = value;
    }

    /**
     * Obtient la valeur de la propriété createdAt.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreatedAt() {
        return createdAt;
    }

    /**
     * Définit la valeur de la propriété createdAt.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreatedAt(XMLGregorianCalendar value) {
        this.createdAt = value;
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

    /**
     * Obtient la valeur de la propriété attribList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAttrib }
     *     
     */
    public ArrayOfAttrib getAttribList() {
        return attribList;
    }

    /**
     * Définit la valeur de la propriété attribList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAttrib }
     *     
     */
    public void setAttribList(ArrayOfAttrib value) {
        this.attribList = value;
    }

    /**
     * Obtient la valeur de la propriété typeAttribList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfTypeAttrib }
     *     
     */
    public ArrayOfTypeAttrib getTypeAttribList() {
        return typeAttribList;
    }

    /**
     * Définit la valeur de la propriété typeAttribList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfTypeAttrib }
     *     
     */
    public void setTypeAttribList(ArrayOfTypeAttrib value) {
        this.typeAttribList = value;
    }

    /**
     * Obtient la valeur de la propriété associationList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMObjAssociation }
     *     
     */
    public ArrayOfMObjAssociation getAssociationList() {
        return associationList;
    }

    /**
     * Définit la valeur de la propriété associationList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMObjAssociation }
     *     
     */
    public void setAssociationList(ArrayOfMObjAssociation value) {
        this.associationList = value;
    }

}
