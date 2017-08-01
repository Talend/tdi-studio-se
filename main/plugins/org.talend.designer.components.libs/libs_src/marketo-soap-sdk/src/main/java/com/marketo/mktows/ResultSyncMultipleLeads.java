
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ResultSyncMultipleLeads complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultSyncMultipleLeads">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="syncStatusList" type="{http://www.marketo.com/mktows/}ArrayOfSyncStatus"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultSyncMultipleLeads", propOrder = {
    "syncStatusList"
})
public class ResultSyncMultipleLeads {

    @XmlElement(required = true)
    protected ArrayOfSyncStatus syncStatusList;

    /**
     * Obtient la valeur de la propriété syncStatusList.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfSyncStatus }
     *     
     */
    public ArrayOfSyncStatus getSyncStatusList() {
        return syncStatusList;
    }

    /**
     * Définit la valeur de la propriété syncStatusList.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfSyncStatus }
     *     
     */
    public void setSyncStatusList(ArrayOfSyncStatus value) {
        this.syncStatusList = value;
    }

}
