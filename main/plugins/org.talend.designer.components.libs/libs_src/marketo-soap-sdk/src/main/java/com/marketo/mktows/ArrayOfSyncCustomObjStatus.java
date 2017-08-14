
package com.marketo.mktows;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ArrayOfSyncCustomObjStatus complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfSyncCustomObjStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="syncCustomObjStatus" type="{http://www.marketo.com/mktows/}SyncCustomObjStatus" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfSyncCustomObjStatus", propOrder = {
    "syncCustomObjStatuses"
})
public class ArrayOfSyncCustomObjStatus {

    @XmlElement(name = "syncCustomObjStatus")
    protected List<SyncCustomObjStatus> syncCustomObjStatuses;

    /**
     * Gets the value of the syncCustomObjStatuses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the syncCustomObjStatuses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSyncCustomObjStatuses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SyncCustomObjStatus }
     * 
     * 
     */
    public List<SyncCustomObjStatus> getSyncCustomObjStatuses() {
        if (syncCustomObjStatuses == null) {
            syncCustomObjStatuses = new ArrayList<SyncCustomObjStatus>();
        }
        return this.syncCustomObjStatuses;
    }

}
