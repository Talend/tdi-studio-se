
package com.marketo.mktows;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ArrayOfMObjAssociation complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfMObjAssociation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mObjAssociation" type="{http://www.marketo.com/mktows/}MObjAssociation" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfMObjAssociation", propOrder = {
    "mObjAssociations"
})
public class ArrayOfMObjAssociation {

    @XmlElement(name = "mObjAssociation")
    protected List<MObjAssociation> mObjAssociations;

    /**
     * Gets the value of the mObjAssociations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mObjAssociations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMObjAssociations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MObjAssociation }
     * 
     * 
     */
    public List<MObjAssociation> getMObjAssociations() {
        if (mObjAssociations == null) {
            mObjAssociations = new ArrayList<MObjAssociation>();
        }
        return this.mObjAssociations;
    }

}
