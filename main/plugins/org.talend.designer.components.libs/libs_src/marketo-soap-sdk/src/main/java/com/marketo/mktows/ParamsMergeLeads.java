
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsMergeLeads complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsMergeLeads">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="winningLeadKeyList" type="{http://www.marketo.com/mktows/}ArrayOfAttribute"/>
 *         &lt;element name="losingLeadKeyLists" type="{http://www.marketo.com/mktows/}ArrayOfKeyList"/>
 *         &lt;element name="mergeInSales" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsMergeLeads", propOrder = {
    "winningLeadKeyList",
    "losingLeadKeyLists",
    "mergeInSales"
})
@XmlRootElement(name = "paramsMergeLeads")
public class ParamsMergeLeads {

    @XmlElement(required = true)
    protected ArrayOfAttribute winningLeadKeyList;
    @XmlElement(required = true)
    protected ArrayOfKeyList losingLeadKeyLists;
    protected Boolean mergeInSales;

    /**
     * Obtient la valeur de la propriété winningLeadKeyList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfAttribute }
     *     
     */
    public ArrayOfAttribute getWinningLeadKeyList() {
        return winningLeadKeyList;
    }

    /**
     * Définit la valeur de la propriété winningLeadKeyList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfAttribute }
     *     
     */
    public void setWinningLeadKeyList(ArrayOfAttribute value) {
        this.winningLeadKeyList = value;
    }

    /**
     * Obtient la valeur de la propriété losingLeadKeyLists.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfKeyList }
     *     
     */
    public ArrayOfKeyList getLosingLeadKeyLists() {
        return losingLeadKeyLists;
    }

    /**
     * Définit la valeur de la propriété losingLeadKeyLists.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfKeyList }
     *     
     */
    public void setLosingLeadKeyLists(ArrayOfKeyList value) {
        this.losingLeadKeyLists = value;
    }

    /**
     * Obtient la valeur de la propriété mergeInSales.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isMergeInSales() {
        return mergeInSales;
    }

    /**
     * Définit la valeur de la propriété mergeInSales.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setMergeInSales(Boolean value) {
        this.mergeInSales = value;
    }

}
