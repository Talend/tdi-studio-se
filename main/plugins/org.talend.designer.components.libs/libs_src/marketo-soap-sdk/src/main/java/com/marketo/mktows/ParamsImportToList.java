
package com.marketo.mktows;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour ParamsImportToList complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ParamsImportToList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="programName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="importFileHeader" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="importFileRows" type="{http://www.marketo.com/mktows/}ArrayOfString"/>
 *         &lt;element name="importListMode" type="{http://www.marketo.com/mktows/}ImportToListModeEnum"/>
 *         &lt;element name="listName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="clearList" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="campaignName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParamsImportToList", propOrder = {
    "programName",
    "importFileHeader",
    "importFileRows",
    "importListMode",
    "listName",
    "clearList",
    "campaignName"
})
@XmlRootElement(name = "paramsImportToList")
public class ParamsImportToList {

    @XmlElement(required = true)
    protected String programName;
    @XmlElement(required = true)
    protected String importFileHeader;
    @XmlElement(required = true)
    protected ArrayOfString importFileRows;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ImportToListModeEnum importListMode;
    @XmlElement(required = true)
    protected String listName;
    protected boolean clearList;
    @XmlElement(required = true, nillable = true)
    protected String campaignName;

    /**
     * Obtient la valeur de la propriété programName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProgramName() {
        return programName;
    }

    /**
     * Définit la valeur de la propriété programName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProgramName(String value) {
        this.programName = value;
    }

    /**
     * Obtient la valeur de la propriété importFileHeader.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImportFileHeader() {
        return importFileHeader;
    }

    /**
     * Définit la valeur de la propriété importFileHeader.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImportFileHeader(String value) {
        this.importFileHeader = value;
    }

    /**
     * Obtient la valeur de la propriété importFileRows.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfString }
     *     
     */
    public ArrayOfString getImportFileRows() {
        return importFileRows;
    }

    /**
     * Définit la valeur de la propriété importFileRows.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfString }
     *     
     */
    public void setImportFileRows(ArrayOfString value) {
        this.importFileRows = value;
    }

    /**
     * Obtient la valeur de la propriété importListMode.
     * 
     * @return
     *     possible object is
     *     {@link ImportToListModeEnum }
     *     
     */
    public ImportToListModeEnum getImportListMode() {
        return importListMode;
    }

    /**
     * Définit la valeur de la propriété importListMode.
     * 
     * @param value
     *     allowed object is
     *     {@link ImportToListModeEnum }
     *     
     */
    public void setImportListMode(ImportToListModeEnum value) {
        this.importListMode = value;
    }

    /**
     * Obtient la valeur de la propriété listName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getListName() {
        return listName;
    }

    /**
     * Définit la valeur de la propriété listName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setListName(String value) {
        this.listName = value;
    }

    /**
     * Obtient la valeur de la propriété clearList.
     * 
     */
    public boolean isClearList() {
        return clearList;
    }

    /**
     * Définit la valeur de la propriété clearList.
     * 
     */
    public void setClearList(boolean value) {
        this.clearList = value;
    }

    /**
     * Obtient la valeur de la propriété campaignName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCampaignName() {
        return campaignName;
    }

    /**
     * Définit la valeur de la propriété campaignName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCampaignName(String value) {
        this.campaignName = value;
    }

}
