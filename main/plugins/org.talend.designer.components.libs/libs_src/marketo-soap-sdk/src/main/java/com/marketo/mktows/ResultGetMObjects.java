
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultGetMObjects complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultGetMObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="returnCount" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="hasMore" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="newStreamPosition" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="mObjectList" type="{http://www.marketo.com/mktows/}ArrayOfMObject" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultGetMObjects", propOrder = {
    "returnCount",
    "hasMore",
    "newStreamPosition",
    "mObjectList"
})
public class ResultGetMObjects {

    protected int returnCount;
    protected boolean hasMore;
    @XmlElement(required = true)
    protected String newStreamPosition;
    protected ArrayOfMObject mObjectList;

    /**
     * Obtient la valeur de la propriété returnCount.
     * 
     */
    public int getReturnCount() {
        return returnCount;
    }

    /**
     * Définit la valeur de la propriété returnCount.
     * 
     */
    public void setReturnCount(int value) {
        this.returnCount = value;
    }

    /**
     * Obtient la valeur de la propriété hasMore.
     * 
     */
    public boolean isHasMore() {
        return hasMore;
    }

    /**
     * Définit la valeur de la propriété hasMore.
     * 
     */
    public void setHasMore(boolean value) {
        this.hasMore = value;
    }

    /**
     * Obtient la valeur de la propriété newStreamPosition.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewStreamPosition() {
        return newStreamPosition;
    }

    /**
     * Définit la valeur de la propriété newStreamPosition.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewStreamPosition(String value) {
        this.newStreamPosition = value;
    }

    /**
     * Obtient la valeur de la propriété mObjectList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfMObject }
     *     
     */
    public ArrayOfMObject getMObjectList() {
        return mObjectList;
    }

    /**
     * Définit la valeur de la propriété mObjectList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfMObject }
     *     
     */
    public void setMObjectList(ArrayOfMObject value) {
        this.mObjectList = value;
    }

}
