<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template name="TransferLF2BR">
		<xsl:param name="input" />
		<xsl:choose>
			<xsl:when test="contains($input, '&#10;')">
				<xsl:value-of select="substring-before($input, '&#10;')" />
				<br />
				<xsl:call-template name="TransferLF2BR">
					<xsl:with-param name="input"
						select="substring-after($input, '&#10;')" />
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$input" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="/">
		<xsl:variable name="ucase"
			select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
		<xsl:variable name="lcase"
			select="'abcdefghijklmnopqrstuvwxyz'" />
		<html>
			<head>
				<title><xsl:value-of select="/project/@i18n.job.generated.documetation" /></title>
				<style type="text/css">
					@page { size: letter; } 
					SPAN.special { font:12pt black; } 
					TABLE.properties { width:95%; } 
					TD.propname { width:30%; font:bold; } 
					TR.profont { font:bold; }
					TD.propval { width:70%; } 
					TD.dependtype { width:20%;} 
					TD.dependloc { width:60%; } 
					TABLE.cols {width:90%; } 
					TD.constraint { width:20%; font:bold; }
					H3.hand { cursor:hand; } 
					img.bordercolor {border-color:#AFCA00; } 
					.FONTSTYLE {font-family:Arial, Helvetica, sans-serif;} 
					.LINKSTYLE {TEXT-DECORATION:none}
					a:hover {TEXT-DECORATION:underline}
					.TITLESTYLE {font-size: 26px; color: #818181;}
					.TOPTITLESTYLE {font-size: 40px; color: #818181;}
					.TABLECOLUMNSTYLE {font-size: 14px; font-family: Arial, Helvetica,sans-serif; color: #818181; background-color:#E6E6E6;align: center} 
					.CODESTYLE {font-size: 14px; font-family: Arial, Helvetica,sans-serif; color: #00e; background-color:#E6E6E6;} 
					tr {page-break-inside: avoid}
				</style>
			</head>
			<body>
				<br />
				<table border="0" cellspacing="0" width="90%"
					class="FONTSTYLE" summary="Job&#160;documentation">
					<tr valign="top">
						<!--<td width="15%" rowspan="2" align="center"></td> -->
						<td  align="center">
							<a href="{project/@link}">
								<img src="{project/@logo}" border="0"
									align="bottom" alt="" />
							</a>
						</td>
						<!-- <td width="1%" rowspan="2" align="center"></td> -->
						<td class="TOPTITLESTYLE">
							<xsl:value-of select="/project/@docType" />
						</td>
					</tr>
					<tr valign="top">
						<td height="20" align="center" valign="top" class="TITLESTYLE">
							<xsl:value-of select="/project/@company" />
						</td>
						<td height="20" align="left" valign="top"
							class="TITLESTYLE">
							<xsl:value-of select="/project/@title" />
						</td>
					</tr>
				</table>
				<br />
				<br />
				<table border="1" width="90%" cellpadding="0"
					cellspacing="0" style="border-collapse: collapse"
					bordercolor="#111111" class="FONTSTYLE" summary="">
					<tr valign="top">
						<td align="center" width="25%"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.project.name" />
						</td>
						<td align="center" width="25%"
							class="FONTSTYLE">
							<xsl:value-of select="/project/@name" />
						</td>
						<td align="center" width="25%"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.generated.date" />
						</td>
						<td align="center" width="25%">
							<xsl:value-of
								select="/project/@generatedDate" />
						</td>
					</tr>
					<tr>
						<td align="center" width="25%"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.author" />
						</td>
						<td align="center" width="25%">
							<xsl:value-of select="/project/job/@author" />
						</td>
						<td align="center" width="25%"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of
								select="/project/@versionName" />
						</td>
						<td align="center" width="25%"
							class="FONTSTYLE">
							<xsl:value-of select="/project/@version" />
						</td>
					</tr>
				</table>
				<h2 class="FONTSTYLE"><xsl:value-of select="/project/@i18n.job.summary" /></h2>
				<div class="FONTSTYLE">
					<b>
						<a href="#ProjectDescription">
						<xsl:value-of select="/project/@i18n.job.project.description" />
						</a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#JobDescription"><xsl:value-of select="/project/@i18n.job.job.description" /></a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#JobPreviewPicture">
						<xsl:value-of select="/project/@i18n.job.job.preview.picture" />
						</a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#Job_settings"><xsl:value-of select="/project/@i18n.job.job.setting" /></a>
					</b>
				</div>
				<xsl:if test="/project/job/@type = 'route' ">
					<div class="FONTSTYLE">
						<b>
							<a href="#MANIFEST">MANIFEST</a>
						</b>
					</div>
					<div class="FONTSTYLE">
						<b>
							<a href="#Spring">Spring</a>
						</b>
					</div>
					<div class="FONTSTYLE">
						<b>
							<a href="#Resources">Resources</a>
						</b>
					</div>
				</xsl:if>
				<div class="FONTSTYLE">
					<b>
						<a href="#Context List"><xsl:value-of select="/project/@i18n.job.context.list" /></a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#ComponentList"><xsl:value-of select="/project/@i18n.job.component.list" /></a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#ComponentsDescription">
						<xsl:value-of select="/project/@i18n.job.components.description" />
						</a>
					</b>
				</div>
				<xsl:variable name="job" select="/project/job" />
				<xsl:variable name="code" select="$job/sourcecodes"></xsl:variable>
				<xsl:if test="$code">
				<div class="FONTSTYLE">
					<b>
						<a href="#SourceCode"><xsl:value-of select="/project/@i18n.job.source.code" /></a>
					</b>
				</div>
				</xsl:if>
				<br />
				<br />
				<!-- Project Description-->
				<!--HR-->
				<h2 class="FONTSTYLE">
					<a id="ProjectDescription" name="#ProjectDescription">
					<xsl:value-of select="/project/@i18n.job.project.description" />
					</a>
				</h2>
				<br />
				<table border="1" width="90%" class="FONTSTYLE"
					cellpadding="0" cellspacing="0"
					style="border-collapse: collapse; padding-left:10mm;"
					bordercolor="#111111" summary="">
					<tr>
						<th align="left" width="30%"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.properties" />
						</th>
						<th align="left" class="TABLECOLUMNSTYLE">
						<xsl:value-of select="/project/@i18n.job.values" />
						</th>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.name" /></td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="/project/@name" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.language" /></td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="/project/@language" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left">
						<xsl:value-of select="/project/@i18n.job.description" />
						</td>
						<td class="FONTSTYLE" align="left">
								<xsl:value-of
									select="/project/pro-description" disable-output-escaping="yes" />
						</td>
					</tr>
				</table>
				<br />
				<br />
				<h2 class="FONTSTYLE">
					<a id="JobDescription" name="#JobDescription"><xsl:value-of select="/project/@i18n.job.job.description" /></a>
				</h2>
				<br />
				<table border="1" width="90%" cellpadding="0"
					cellspacing="0"
					style="border-collapse: collapse; padding-left:10mm;"
					bordercolor="#111111" frame="box" summary="">
					<tr>
						<th align="left" width="30%"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.properties" />
						</th>
						<th align="left" class="TABLECOLUMNSTYLE">
						<xsl:value-of select="/project/@i18n.job.values" />
						</th>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.name" /></td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="$job/@name" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.author.min" /></td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="$job/@author" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.version" /></td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="$job/@version" />
						</td>
					</tr>
					<xsl:if test="$job/@type = 'route' ">
						<tr>
							<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.type" /></td>
							<td class="FONTSTYLE" align="left">
								<xsl:value-of select="$job/@type" />
							</td>
						</tr>
					</xsl:if>
					<tr>
						<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.purpose" /></td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="$job/@purpose" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.status" /></td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="$job/@status" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left">
						<xsl:value-of select="/project/@i18n.job.description" />
						</td>
						<td class="FONTSTYLE" align="left">
								<xsl:value-of select="$job/description"  disable-output-escaping="yes"/>
							
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left"><xsl:value-of select="/project/@i18n.job.creation" /></td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="$job/@creation" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left">
						<xsl:value-of select="/project/@i18n.job.modification" />
						</td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="$job/@modification" />
						</td>
					</tr>
				</table>
				<br />
				<!-- Project Description-->
				<!--HR-->
				<xsl:variable name="jobPreviewPicture"
					select="$job/preview/@picture" />
				<xsl:if test="string-length($jobPreviewPicture)!=0">
					<h2 class="FONTSTYLE">
						<a id="JobPreviewPicture" name="#JobPreviewPicture">
						<xsl:value-of select="/project/@i18n.job.job.preview.picture" />
						</a>
					</h2>
					<br />
					<div class="FONTSTYLE" align="center">
						<img src="{$jobPreviewPicture}" usemap="#jobmap"
							alt="No image available" class="bordercolor" />
					</div>
					<map id="jobmap" name="jobmap">
						<xsl:for-each
							select="$job/componentList/componentItem">
							<area shape="rect"
								coords="{@leftTopX},{@leftTopY},{@rightBottomX},{@rightBottomY}"
								href="#{@link}" alt="{@name}" />
						</xsl:for-each>
					</map>
				</xsl:if>
				<br />
				<br />
				<!-- Job settings -->
				<!--HR-->
				<h2 class="FONTSTYLE">
					<a id="Job_settings" name="#Job settings"><xsl:value-of select="/project/@i18n.job.job.setting" /></a>
				</h2>
				<xsl:if test="$job/@i18n.job.extract.settings.type = 'i18n.job.extract.settings' ">
				<table border="0" width="90%" class="FONTSTYLE"
					cellpadding="0" cellspacing="0" style="border-collapse: collapse"
					bordercolor="#111111" summary="">
					<tr bgcolor="#E6E6E6" class="FONTSTYLE">
						<td class="FONTSTYLE">
							<b><xsl:value-of select="/project/@i18n.job.extract.settings" /></b>
						</td>
					</tr>
				</table>
				<br />
				<xsl:variable name="extra"
					select="$job/jobSetting/extra">
				</xsl:variable>
				<table width="90%" border="1" cellpadding="0"
					cellspacing="0"
					style="border-collapse: collapse; padding-left:10mm;"
					bordercolor="#111111" frame="box" summary="">
					<tr>
						<th width="50%" align="left"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.name" />
						</th>
						<th width="50%" align="left"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.value" />
						</th>
					</tr>
					<xsl:for-each select="$extra/jobParameter">
						<tr>
							<td class="FONTSTYLE" align="left">
								<xsl:value-of select="@name" />
							</td>
							<td class="FONTSTYLE" align="left">
								<xsl:value-of select="@value" />
							</td>
						</tr>
					</xsl:for-each>
				</table>
				<br />
				<br />
				</xsl:if>
                <xsl:if test="$job/@i18n.job.stats.logs.type = 'i18n.job.stats.logs' ">
				<table border="0" width="90%" class="FONTSTYLE"
					cellpadding="0" cellspacing="0" style="border-collapse: collapse"
					bordercolor="#111111" summary="">
					<tr bgcolor="#E6E6E6" class="FONTSTYLE">
						<td class="FONTSTYLE">
							<b><xsl:value-of select="/project/@i18n.job.stats.logs" /></b>
						</td>
					</tr>
				</table>
				<br />
				<xsl:variable name="statsAndLog"
					select="$job/jobSetting/Statslogs">
				</xsl:variable>
				<table width="90%" border="1" cellpadding="0"
					cellspacing="0"
					style="border-collapse: collapse; padding-left:10mm;"
					bordercolor="#111111" frame="box" summary="">
					<tr>
						<th width="50%" align="left"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.name" />
						</th>
						<th width="50%" align="left"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.value" />
						</th>
					</tr>
					<xsl:for-each select="$statsAndLog/jobParameter">
						<tr>
							<td class="FONTSTYLE" align="left">
								<xsl:value-of select="@name" />
							</td>
							<td class="FONTSTYLE" align="left">
								<xsl:value-of select="@value" />
							</td>
						</tr>
					</xsl:for-each>
				</table>
				<br />
				<br />
				</xsl:if>
				<!-- Context List-->
				<!--HR-->
				<h2 class="FONTSTYLE">
					<a id="Context List" name="#Context List"><xsl:value-of select="/project/@i18n.job.context.list" /></a>
				</h2>
				<xsl:for-each select="$job/contextList/context">
					<table border="0" width="90%" class="FONTSTYLE"
						cellpadding="0" cellspacing="0" style="border-collapse: collapse"
						bordercolor="#111111" summary="">
						<tr bgcolor="#E6E6E6" class="FONTSTYLE">
							<td class="FONTSTYLE">
								<b>
									<xsl:value-of select="/project/@i18n.job.context" />
									<xsl:value-of select="@name" />
								</b>
							</td>
						</tr>
					</table>
					<br />
					<table width="90%" border="1" cellpadding="0"
						cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;"
						bordercolor="#111111" frame="box" summary="">
						<tr>
							<th width="20%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.name" />
							</th>
							<th width="25%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.promt" />
							</th>
							<th width="20%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.need.promt" />
							</th>
							<th width="10%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.type" />
							</th>
							<th width="15%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.value" />
							</th>
							<th width="10%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.source" />
							</th>
						</tr>
						<xsl:for-each
							select="contextParameter">
							<tr>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@name" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@prompt" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of
										select="@promptNeeded" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@type" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@value" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@source" />
								</td>
							</tr>
						</xsl:for-each>
					</table>
					<br />
					<br />
				</xsl:for-each>
				<br />
				<br />
				
				<!-- Route Special Part -->
				<xsl:if test="$job/@type = 'route' ">
				
					<!-- Route MANIFEST -->
					<h2 class="FONTSTYLE">
						<a id="MANIFEST" name="#MANIFEST">MANIFEST</a>
					</h2>
					<table width="90%" border="1" cellpadding="0"
						cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;"
						bordercolor="#111111" frame="box" summary="">
						<tr>
							<th width="30%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.route.manifest.type" />
							</th>
							<th width="70%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.route.manifest.value" />
							</th>
						</tr>
						<tr>
							<td class="FONTSTYLE" align="left">
								Import-Package(s)
							</td>
							<td class="FONTSTYLE" align="left">
								<pre>
									<xsl:call-template name="TransferLF2BR">
										<xsl:with-param name="input" select="$job/RouteManifest/Import-Package/text()"/>
									</xsl:call-template>
								</pre>
							</td>
						</tr>
						<tr>
							<td class="FONTSTYLE" align="left">
								Export-Package(s)
							</td>
							<td class="FONTSTYLE" align="left">
								<pre>
									<xsl:call-template name="TransferLF2BR">
										<xsl:with-param name="input" select="$job/RouteManifest/Export-Package/text()"/>
									</xsl:call-template>
								</pre>
							</td>
						</tr>
						<tr>
							<td class="FONTSTYLE" align="left">
								Required-Bundle(s)
							</td>
							<td class="FONTSTYLE" align="left">
								<pre>
									<xsl:call-template name="TransferLF2BR">
										<xsl:with-param name="input" select="$job/RouteManifest/Require-Bundle/text()"/>
									</xsl:call-template>
								</pre>
							</td>
						</tr>
						<tr>
							<td class="FONTSTYLE" align="left">
								Bundle-Classpath(s)
							</td>
							<td class="FONTSTYLE" align="left">
								<pre>
									<xsl:call-template name="TransferLF2BR">
										<xsl:with-param name="input" select="$job/RouteManifest/Bundle-ClassPath/text()"/>
									</xsl:call-template>
								</pre>
							</td>
						</tr>
					</table>
					
					<!-- Route Spring -->
					<h2 class="FONTSTYLE">
						<a id="Spring" name="#Spring">Spring</a>
						<pre class="CODESTYLE">
							<xsl:call-template name="TransferLF2BR">
								<xsl:with-param name="input" select="$job/RouteSpring/text()"/>
							</xsl:call-template>
						</pre>
					</h2>
					
					<!-- Route Resources -->
					<h2 class="FONTSTYLE">
						<a id="Resources" name="#Resources">Resources</a>
					</h2>
					<table width="90%" border="1" cellpadding="0"
						cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;"
						bordercolor="#111111" frame="box" summary="">
						<tr>
							<th width="30%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.route.resource.name" />
							</th>
							<th width="30%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.route.resource.version" />
							</th>
							<th width="40%" align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.route.resource.path" />
							</th>
						</tr>
						<xsl:for-each select="$job/RouteResources/Resource">
							<tr>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@name"/>
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@version"/>
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@path"/>
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</xsl:if>
				
				<!-- Component List-->
				<!--HR-->
				<h2 class="FONTSTYLE">
					<a id="ComponentList" name="#ComponentList"><xsl:value-of select="/project/@i18n.job.component.list" /></a>
				</h2>
				<table width="90%" border="1" cellpadding="0"
					cellspacing="0"
					style="border-collapse: collapse; padding-left:10mm;"
					bordercolor="#111111" frame="box" summary="">
					<tr>
						<th width="50%" align="left"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.component.name" />
						</th>
						<th width="50%" align="left"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.component.type" />
						</th>
					</tr>
					<xsl:for-each
						select="$job/componentList/componentItem">
						<tr>
							<td class="FONTSTYLE" align="left">
								<a href="#{@link}" class="LINKSTYLE">
									<xsl:value-of select="@name" />
								</a>
							</td>
							<td class="FONTSTYLE" align="left">
								<xsl:value-of select="@type" />
							</td>
						</tr>
					</xsl:for-each>
				</table>
				<br />
				<!-- Components Description-->
				<!--HR-->
				<h2 class="FONTSTYLE">
					<a id="ComponentsDescription" name="#ComponentsDescription">
					<xsl:value-of select="/project/@i18n.job.components.description" />
					</a>
				</h2>
				<xsl:for-each
					select="$job/internalNodeComponents/component">
					<a name="{@uniqueName}" />
					<table border="0" width="90%" class="FONTSTYLE"
						cellpadding="0" cellspacing="0" style="border-collapse: collapse"
						bordercolor="#111111" summary="">
						<tr bgcolor="#E6E6E6" class="FONTSTYLE">
							<td class="FONTSTYLE">
								<b>
									<xsl:value-of select="/project/@i18n.job.component" />&#160;&#160;
									<xsl:value-of
										select="componentType" />
								</b>
							</td>
						</tr>
					</table>
					<BR />
					<table border="1" width="90%" class="FONTSTYLE"
						cellpadding="0" cellspacing="0" style="border-collapse: collapse"
						bordercolor="#111111" summary="">
						<tr>
							<td class="cols" align="center" rowspan="2"
								width="10%">
								<img src="{@icon}" alt="" />
								&#160;&#160;&#160;&#160;&#160;
							</td>
							<td align="left" class="TABLECOLUMNSTYLE"
								width="15%">
								<xsl:value-of select="/project/@i18n.job.unique.name" />
							</td>
							<td class="FONTSTYLE" align="left"
								width="15%">
								<xsl:value-of select="@uniqueName" />
							</td>
							<td align="left" class="TABLECOLUMNSTYLE"
								width="15%">
								<xsl:value-of select="/project/@i18n.job.input" />
							</td>
							<td class="FONTSTYLE" align="left">
								<xsl:for-each select="input">
									<xsl:choose>
										<xsl:when
											test="text() = 'none'">
											<xsl:value-of
												select="text()" />
										</xsl:when>
										<xsl:otherwise>
											<a href="#{@link}"
												style="LINKSTYLE">
												<xsl:value-of
													select="text()" />
											</a>
											<xsl:if
												test="position()!= last()">
												<xsl:text>,&#160;&#160;</xsl:text>
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</td>
						</tr>
						<tr>
							<td align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.label" />
							</td>
							<td align="left">
								<xsl:value-of select="@label" />
							</td>
							<td align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.output" />
							</td>
							<td class="FONTSTYLE" align="left">
								<xsl:for-each select="output">
									<xsl:choose>
										<xsl:when
											test="text() = 'none'">
											<xsl:value-of
												select="text()" />
										</xsl:when>
										<xsl:otherwise>
											<a href="#{@link}"
												style="LINKSTYLE">
												<xsl:value-of
													select="text()" />
											</a>
											<xsl:if
												test="position()!= last()">
												<xsl:text>,&#160;&#160;</xsl:text>
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</td>
						</tr>
					</table>
					<xsl:variable name="previewPicture"
						select="@preview" />
					<xsl:if test="string-length($previewPicture)!=0">
						<br />
						<div class="FONTSTYLE">
							<img src="{$previewPicture}"
								usemap="#jobmap" alt="No image available" class="bordercolor" />
						</div>
					</xsl:if>
					<br />
					
					<b class="FONTSTYLE"><xsl:value-of select="/project/@i18n.job.component.parameters" /></b>
					<br />
					<table class="cols" width="90%" border="1"
						cellpadding="0" cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;"
						bordercolor="#111111" frame="box" summary="">
						<tr>
							<th align="left" width="30%"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.properties" />
							</th>
							<th align="left" width="70%"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.values" />
							</th>
						</tr>
						<xsl:for-each select="parameters/column">
							<tr>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@name" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@value" />
								</td>
							</tr>
						</xsl:for-each>
					</table>
					<br />
					<xsl:for-each select="schemas/schema">
						<b class="FONTSTYLE">
							<xsl:value-of select="/project/@i18n.job.schema.for" />
							<xsl:value-of select="@name" />
							:
						</b>
						<br />
						<table class="cols" width="90%" border="1"
							cellpadding="0" cellspacing="0" style="border-collapse: collapse"
							bordercolor="#111111" frame="box" summary="">
							<tr class="profont">
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.column" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.key" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.type" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.length" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.precision" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.nullable" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="16%">
									<xsl:value-of select="/project/@i18n.job.comment" />
								</th>
							</tr>
							<xsl:for-each select="column">
								<tr class="FONTSTYLE">
									<td align="left">
										<xsl:value-of select="@name" />
									</td>
									<td align="left">
										<xsl:value-of select="@key" />
									</td>
									<td align="left">
										<xsl:value-of select="@type" />
									</td>
									<td align="left">
										<xsl:value-of select="@length" />
									</td>
									<td align="left">
										<xsl:value-of
											select="@precision" />
									</td>
									<td align="left">
										<xsl:value-of
											select="@nullable" />
									</td>
									<td align="left">
										<xsl:value-of select="@comment" />
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</xsl:for-each>
					<br />
					<b class="FONTSTYLE"><xsl:value-of select="/project/@i18n.job.original.function.parameters" /></b>
					<br/>
					<xsl:comment><xsl:value-of select="@uniqueName" /><xsl:value-of select="/project/@i18n.job.ended" /></xsl:comment>
				</xsl:for-each>
				<xsl:for-each
					select="$job/externalNodeComponents/component">
					<a name="{@uniqueName}" />
					<table border="0" width="90%" class="FONTSTYLE"
						cellpadding="0" cellspacing="0" style="border-collapse: collapse"
						bordercolor="#111111" summary="">
						<tr bgcolor="#E6E6E6">
							<td class="FONTSTYLE">
								<b>
									<xsl:value-of select="/project/@i18n.job.component" />&#160;&#160;
									<xsl:value-of
										select="componentType" />
								</b>
							</td>
						</tr>
					</table>
					<BR />
					<table border="1" width="90%" class="FONTSTYLE"
						cellpadding="0" cellspacing="0" style="border-collapse: collapse"
						bordercolor="#111111" summary="">
						<tr>
							<td class="cols" align="center" rowspan="2"
								width="10%">
								<img src="{@icon}" alt="" />
								&#160;&#160;&#160;&#160;&#160;
							</td>
							<td align="left" class="TABLECOLUMNSTYLE"
								width="15%">
								<xsl:value-of select="/project/@i18n.job.unique.name" />
							</td>
							<td class="FONTSTYLE" align="left"
								width="15%">
								<xsl:value-of select="@uniqueName" />
							</td>
							<td align="left" class="TABLECOLUMNSTYLE"
								width="15%">
								<xsl:value-of select="/project/@i18n.job.input" />
							</td>
							<td class="FONTSTYLE" align="left">
								<xsl:for-each select="input">
									<xsl:choose>
										<xsl:when
											test="text() = 'none'">
											<xsl:value-of
												select="text()" />
										</xsl:when>
										<xsl:otherwise>
											<a href="#{@link}"
												style="LINKSTYLE">
												<xsl:value-of
													select="text()" />
											</a>
											<xsl:if
												test="position()!= last()">
												<xsl:text>,&#160;&#160;</xsl:text>
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</td>
						</tr>
						<tr>
							<td align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.label" />
							</td>
							<td align="left">
								<xsl:value-of select="@label" />
							</td>
							<td align="left"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.output" />
							</td>
							<td class="FONTSTYLE" align="left">
								<xsl:for-each select="output">
									<xsl:choose>
										<xsl:when
											test="text() = 'none'">
											<xsl:value-of
												select="text()" />
										</xsl:when>
										<xsl:otherwise>
											<a href="#{@link}"
												style="LINKSTYLE">
												<xsl:value-of
													select="text()" />
											</a>
											<xsl:if
												test="position()!= last()">
												<xsl:text>,&#160;&#160;</xsl:text>
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:for-each>
							</td>
						</tr>
					</table>
					<xsl:variable name="previewPicture"
						select="@preview" />
					<xsl:if test="string-length($previewPicture)!=0">
						<br />
						<div class="FONTSTYLE">
							<img src="{$previewPicture}"
								usemap="#jobmap" alt="No image available" class="bordercolor" />
						</div>
					</xsl:if>
					<br />
					<xsl:for-each select="parameters">
					<b class="FONTSTYLE"><xsl:value-of select="/project/@i18n.job.component.parameters" /></b>
					<br />
					<table width="90%" class="FONTSTYLE" border="1"
						cellpadding="0" cellspacing="0" style="border-collapse: collapse"
						bordercolor="#111111" frame="box" summary="">
						<tr>
							<th align="left" width="30%"
								class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.job.properties" />
							</th>
							<th align="left" width="70%"
								class="TABLECOLUMNSTYLE">
								&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;<xsl:value-of select="/project/@i18n.job.values" />
							</th>
						</tr>
						<xsl:for-each select="column">
							<tr>
								<td class="FONTSTYLE" align="left">
									<xsl:variable name="propname"
										select="@name" />
									<xsl:value-of select="$propname" />
								</td>
								<td class="FONTSTYLE" align="left">
									&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;
									<xsl:value-of select="text()" />
								</td>
							</tr>
						</xsl:for-each>
					</table>
					</xsl:for-each>
					<br />
					<xsl:for-each select="schemas/schema">
						<b class="FONTSTYLE">
							<xsl:value-of select="/project/@i18n.job.schema.for" />
							<xsl:value-of select="@name" />
							:
						</b>
						<br />
						<table class="cols" border="1" width="90%"
							cellpadding="0" cellspacing="0" style="border-collapse: collapse"
							bordercolor="#111111" frame="box" summary="">
							<tr class="profont">
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.column" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.key" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.type" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.length" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.precision" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="14%">
									<xsl:value-of select="/project/@i18n.job.nullable" />
								</th>
								<th class="TABLECOLUMNSTYLE"
									width="16%">
									<xsl:value-of select="/project/@i18n.job.comment" />
								</th>
							</tr>
							<xsl:for-each select="column">
								<tr class="FONTSTYLE">
									<td align="left">
										<xsl:value-of select="@name" />
									</td>
									<td align="left">
										<xsl:value-of select="@key" />
									</td>
									<td align="left">
										<xsl:value-of select="@type" />
									</td>
									<td align="left">
										<xsl:value-of select="@length" />
									</td>
									<td align="left">
										<xsl:value-of
											select="@precision" />
									</td>
									<td align="left">
										<xsl:value-of
											select="@nullable" />
									</td>
									<td align="left">
										<xsl:value-of select="@comment" />
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</xsl:for-each>
					<br />
					<xsl:comment><xsl:value-of select="@uniqueName" /><xsl:value-of select="/project/@i18n.job.ended" /></xsl:comment>
					<xsl:text /><!--before: $job/externalNodeComponents/component/@uniqueName -->
				</xsl:for-each>
					<!-- Source Code-->
				<!--HR-->
				<xsl:if test="$code">
				<h2 class="FONTSTYLE">
					<a name="#SourceCode"><xsl:value-of select="/project/@i18n.job.source.code" /></a>
				</h2>
				<table width="90%" border="1" cellpadding="0"
					cellspacing="0"
					style="border-collapse: collapse; padding-left:10mm;"
					bordercolor="#111111" frame="box" summary="">
					<tr>
						<th width="50%" align="left"
							class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.job.content" />
						</th>
					</tr>
					<tr>
					<td bgcolor="gray" style="word-break:break-all;word-wrap:break-word;">
						<xsl:for-each select="$code/code" >
							<xsl:value-of select="@content" />
							<br />
						</xsl:for-each></td>
					</tr>
				</table>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
