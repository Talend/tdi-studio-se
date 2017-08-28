
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsGetMObjects complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsGetMObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="externalKey" type="{http://www.marketo.com/mktows/}Attrib" minOccurs="0"/>
 *         &lt;element name="mObjCriteriaList" type="{http://www.marketo.com/mktows/}ArrayOfMObjCriteria" minOccurs="0"/>
 *         &lt;element name="mObjAssociationList" type="{http://www.marketo.com/mktows/}ArrayOfMObjAssociation" minOccurs="0"/>
 *         &lt;element name="includeDetails" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="streamPosition" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsGetMObjects", propOrder = {
    "type",
    "id",
    "externalKey",
    "mObjCriteriaList",
    "mObjAssociationList",
    "includeDetails",
    "streamPosition"
})
@XmlRootElement(name = "paramsGetMObjects")
public class ParamsGetMObjects {

    @XmlElement(required = true)
    protected String type;
    protected Integer id;
    protected Attrib externalKey;
    protected ArrayOfMObjCriteria mObjCriteriaList;
    protected ArrayOfMObjAssociation mObjAssociationList;
    protected Boolean includeDetails;
    protected String streamPosition;

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
     * Obtient la valeur de la propriété mObjCriteriaList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMObjCriteria }
     *     
     */
    public ArrayOfMObjCriteria getMObjCriteriaList() {
        return mObjCriteriaList;
    }

    /**
     * Définit la valeur de la propriété mObjCriteriaList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMObjCriteria }
     *     
     */
    public void setMObjCriteriaList(ArrayOfMObjCriteria value) {
        this.mObjCriteriaList = value;
    }

    /**
     * Obtient la valeur de la propriété mObjAssociationList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMObjAssociation }
     *     
     */
    public ArrayOfMObjAssociation getMObjAssociationList() {
        return mObjAssociationList;
    }

    /**
     * Définit la valeur de la propriété mObjAssociationList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMObjAssociation }
     *     
     */
    public void setMObjAssociationList(ArrayOfMObjAssociation value) {
        this.mObjAssociationList = value;
    }

    /**
     * Obtient la valeur de la propriété includeDetails.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIncludeDetails() {
        return includeDetails;
    }

    /**
     * Définit la valeur de la propriété includeDetails.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIncludeDetails(Boolean value) {
        this.includeDetails = value;
    }

    /**
     * Obtient la valeur de la propriété streamPosition.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStreamPosition() {
        return streamPosition;
    }

    /**
     * Définit la valeur de la propriété streamPosition.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStreamPosition(String value) {
        this.streamPosition = value;
    }

}
