<?xml version="1.0" encoding="utf-8"?>
<!-- edited with XMLSpy v2007 sp1 (http://www.altova.com) by  () -->
<xsl:stylesheet version="1.1"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <xsl:variable name="rootTable"
            select="externalNode/mapperTable" />
        <xsl:variable name="subRootTable"
            select="externalNode/mapperTable/rootTable/table" />

        <b class="FONTSTYLE"><xsl:value-of select="/externalNode/@i18n.job.component.parameters" /></b>
                    <br />
                    <table class="cols" width="90%" border="1"
                        cellpadding="0" cellspacing="0"
                        style="border-collapse: collapse; padding-left:10mm;"
                        bordercolor="#111111" frame="box" summary="">
                        <tr>
                            <th align="left" width="30%"
                                class="TABLECOLUMNSTYLE">
                                <xsl:value-of select="/externalNode/@i18n.job.properties" />
                            </th>
                            <th align="left" width="70%"
                                class="TABLECOLUMNSTYLE">
                                <xsl:value-of select="/externalNode/@i18n.job.values" />
                            </th>
                        </tr>
                        <xsl:for-each select="/externalNode/parameters/column">
                            <tr>
                                <td class="FONTSTYLE" align="left">
                                    <xsl:variable name="propname"
                                        select="@name" />
                                    <xsl:value-of select="$propname" />
                                </td>
                                <td class="FONTSTYLE" align="left">
                                 <pre>
                                    <xsl:value-of select="text()" disable-output-escaping="yes"/>
                                    </pre>
                                </td>
                            </tr>
                        </xsl:for-each>
                    </table>
                    <br />
        
        
        <xsl:for-each select="$rootTable">
            <b class="FONTSTYLE">
                <xsl:value-of select="/externalNode/@i18n.mapper.table.for" />
                <xsl:value-of select="/externalNode/@name" />
                (
                <xsl:value-of select="@type" />
                ):
            </b>
            <br />
            <xsl:for-each select="table">
                <table border="0" width="96%" cellpadding="0"
                    cellspacing="0" summary="">
                    <tr>
                        <td width="15"></td>
                        <td>
                            <b class="FONTSTYLE">
                                <xsl:value-of select="/externalNode/@i18n.mapper.table.properties" />(
                                <xsl:value-of select="@name" />
                                ):
                            </b>
                            <table class="cols" border="1" width="90%"
                                cellpadding="0" cellspacing="0"
                                style="border-collapse: collapse; padding-left:10mm;" bordercolor="#111111"
                                frame="box" summary="">
                                <tr>
                                    <th align="left" width="30%"
                                        class="TABLECOLUMNSTYLE">
                                        <xsl:value-of select="/externalNode/@i18n.job.properties" />
                                    </th>
                                    <th align="left" width="70%"
                                        class="TABLECOLUMNSTYLE">
                                        <xsl:value-of select="/externalNode/@i18n.job.values" />
                                    </th>
                                </tr>
                                <tr>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of select="/externalNode/@i18n.job.name" />
                                    </td>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of select="@name" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of select="/externalNode/@i18n.tmap.matching.mode" />
                                    </td>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of
                                            select="@matching-mode" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of select="/externalNode/@i18n.mapper.table.isminimized" />
                                    </td>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of
                                            select="@isMinimized" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of select="/externalNode/@i18n.tmap.isreject" />
                                    </td>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of
                                            select="@isReject" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of select="/externalNode/@i18n.tmap.isrejectinnnerjoin" />
                                    </td>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of
                                            select="@isRejectInnerJoin" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of select="/externalNode/@i18n.tmap.isinnerjoin" />
                                    </td>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of
                                            select="@isInnerJoin" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of select="/externalNode/@i18n.tmap.expressionfilter" />
                                    </td>
                                    <td class="FONTSTYLE"
                                        align="left">
                                        <xsl:value-of
                                            select="@expressionFilter" />
                                    </td>
                                </tr>
                            </table>
                            <br />
                            <b class="FONTSTYLE">
                                <xsl:value-of select="/externalNode/@i18n.mapper.table.metadata.entries" />(
                                <xsl:value-of select="@name" />
                                ):
                            </b>
                            <br />
                            <table class="cols" width="90%"
                                cellpadding="0" border="1" cellspacing="0"
                                style="border-collapse: collapse;table-layout:fixed; word-break:break-all;word-wrap:break-word;white-space:pre-wrap" bordercolor="#111111"
                                frame="box" summary="">
                                <tr class="profont">
                                    <th class="TABLECOLUMNSTYLE" 
                                        width="25%">
                                        <xsl:value-of select="/externalNode/@i18n.job.name" />
                                    </th>
                                    <th class="TABLECOLUMNSTYLE"
                                        width="25%">
                                        <xsl:value-of select="/externalNode/@i18n.job.type" />
                                    </th>
                                    <th class="TABLECOLUMNSTYLE"
                                        width="25%">
                                        <xsl:value-of select="/externalNode/@i18n.mapper.table.expression" />
                                    </th>
                                    <th class="TABLECOLUMNSTYLE"
                                        width="25%">
                                        <xsl:value-of select="/externalNode/@i18n.tmap.isnullable" />
                                    </th>
                                </tr>
                                <xsl:for-each
                                    select="metadataTableEntries/entry">
                                    <tr class="FONTSTYLE">
                                        <td align="left">
                                           <xsl:value-of
                                                select="@name" />
                                        </td>
                                        <td align="left" >
                                            <xsl:value-of
                                                select="@type" />
                                        </td>
                                        <td align="left" >
                                            <xsl:value-of
                                                select="@expression" />
                                        </td>
                                        <td align="left" >
                                            <xsl:value-of
                                                select="@isNullable" />
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </table>
                            <br />
                        </td>
                    </tr>
                </table>
                <br />
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
