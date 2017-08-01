
package com.marketo.mktows;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Classe Java pour ResultGetImportToListStatus complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="ResultGetImportToListStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="status" type="{http://www.marketo.com/mktows/}ImportToListStatusEnum"/>
 *         &lt;element name="startedTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="endedTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="estimatedTime" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="estimatedRows" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="rowsImported" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="rowsFailed" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="rowsIgnored" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="importSummary" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResultGetImportToListStatus", propOrder = {
    "status",
    "startedTime",
    "endedTime",
    "estimatedTime",
    "estimatedRows",
    "rowsImported",
    "rowsFailed",
    "rowsIgnored",
    "importSummary"
})
public class ResultGetImportToListStatus {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ImportToListStatusEnum status;
    @XmlElementRef(name = "startedTime", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> startedTime;
    @XmlElementRef(name = "endedTime", type = JAXBElement.class, required = false)
    protected JAXBElement<XMLGregorianCalendar> endedTime;
    @XmlElementRef(name = "estimatedTime", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> estimatedTime;
    @XmlElementRef(name = "estimatedRows", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> estimatedRows;
    @XmlElementRef(name = "rowsImported", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> rowsImported;
    @XmlElementRef(name = "rowsFailed", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> rowsFailed;
    @XmlElementRef(name = "rowsIgnored", type = JAXBElement.class, required = false)
    protected JAXBElement<Integer> rowsIgnored;
    @XmlElementRef(name = "importSummary", type = JAXBElement.class, required = false)
    protected JAXBElement<String> importSummary;

    /**
     * Obtient la valeur de la propriété status.
     * 
     * @return
     *     possible object is
     *     {@link ImportToListStatusEnum }
     *     
     */
    public ImportToListStatusEnum getStatus() {
        return status;
    }

    /**
     * Définit la valeur de la propriété status.
     * 
     * @param value
     *     allowed object is
     *     {@link ImportToListStatusEnum }
     *     
     */
    public void setStatus(ImportToListStatusEnum value) {
        this.status = value;
    }

    /**
     * Obtient la valeur de la propriété startedTime.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getStartedTime() {
        return startedTime;
    }

    /**
     * Définit la valeur de la propriété startedTime.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setStartedTime(JAXBElement<XMLGregorianCalendar> value) {
        this.startedTime = value;
    }

    /**
     * Obtient la valeur de la propriété endedTime.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public JAXBElement<XMLGregorianCalendar> getEndedTime() {
        return endedTime;
    }

    /**
     * Définit la valeur de la propriété endedTime.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}
     *     
     */
    public void setEndedTime(JAXBElement<XMLGregorianCalendar> value) {
        this.endedTime = value;
    }

    /**
     * Obtient la valeur de la propriété estimatedTime.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getEstimatedTime() {
        return estimatedTime;
    }

    /**
     * Définit la valeur de la propriété estimatedTime.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setEstimatedTime(JAXBElement<Integer> value) {
        this.estimatedTime = value;
    }

    /**
     * Obtient la valeur de la propriété estimatedRows.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getEstimatedRows() {
        return estimatedRows;
    }

    /**
     * Définit la valeur de la propriété estimatedRows.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setEstimatedRows(JAXBElement<Integer> value) {
        this.estimatedRows = value;
    }

    /**
     * Obtient la valeur de la propriété rowsImported.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getRowsImported() {
        return rowsImported;
    }

    /**
     * Définit la valeur de la propriété rowsImported.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setRowsImported(JAXBElement<Integer> value) {
        this.rowsImported = value;
    }

    /**
     * Obtient la valeur de la propriété rowsFailed.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getRowsFailed() {
        return rowsFailed;
    }

    /**
     * Définit la valeur de la propriété rowsFailed.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setRowsFailed(JAXBElement<Integer> value) {
        this.rowsFailed = value;
    }

    /**
     * Obtient la valeur de la propriété rowsIgnored.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public JAXBElement<Integer> getRowsIgnored() {
        return rowsIgnored;
    }

    /**
     * Définit la valeur de la propriété rowsIgnored.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Integer }{@code >}
     *     
     */
    public void setRowsIgnored(JAXBElement<Integer> value) {
        this.rowsIgnored = value;
    }

    /**
     * Obtient la valeur de la propriété importSummary.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getImportSummary() {
        return importSummary;
    }

    /**
     * Définit la valeur de la propriété importSummary.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setImportSummary(JAXBElement<String> value) {
        this.importSummary = value;
    }

}
