<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.1"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="/">
		<xsl:variable name="ucase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
		<xsl:variable name="lcase" select="'abcdefghijklmnopqrstuvwxyz'" />
		<html>
			<head>
				<title>
					<xsl:value-of select="/project/@i18n.compare.generated.documetation" />
				</title>
				<style type="text/css">
					@page { size: letter; }
					SPAN.special { font:12pt
					black; }
					TABLE.properties { width:95%; }
					TD.propname { width:30%;
					font:bold; }
					TR.profont { font:bold; }
					TD.propval { width:70%; }
					TD.dependtype { width:20%;}
					TD.dependloc { width:60%; }
					TABLE.cols
					{width:90%; }
					TD.constraint { width:20%; font:bold; }
					H3.hand {
					cursor:hand; }
					img.bordercolor {border-color:#AFCA00; }
					.FONTSTYLE
					{font-family:Arial, Helvetica, sans-serif;}
					.LINKSTYLE
					{TEXT-DECORATION:none}
					a:hover {TEXT-DECORATION:underline}
					.TITLESTYLE {font-size: 26px; color: #818181;}
					.TOPTITLESTYLE
					{font-size: 40px; color: #818181;}
					.TABLECOLUMNSTYLE {font-family:
					Arial, Helvetica,sans-serif; color: #818181;
					background-color:#E6E6E6;align: center}
					tr {page-break-inside:
					avoid}
				</style>
			</head>
			<body>
				<br />
				<table border="0" cellspacing="0" width="90%" class="FONTSTYLE"
					summary="JobCompare&#160;documentation">
					<tr valign="top">
						<!--<td width="15%" rowspan="2" align="center"></td> -->
						<td align="center">
							<a href="{project/@link}">
								<img src="{project/@logo}" border="0" align="bottom" alt="" />
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
						<td height="20" align="left" valign="top" class="TITLESTYLE">
							<xsl:value-of select="/project/@title" />
						</td>
					</tr>
				</table>
				<br />
				<br />
				<table border="1" width="90%" cellpadding="0" cellspacing="0"
					style="border-collapse: collapse" bordercolor="#111111" class="FONTSTYLE"
					summary="">
					<tr valign="top">
						<td align="center" width="25%" class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.compare.project.name" />
						</td>
						<td align="center" width="25%" class="FONTSTYLE">
							<xsl:value-of select="/project/@name" />
						</td>
						<td align="center" width="25%" class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.compare.generated.date" />
						</td>
						<td align="center" width="25%">
							<xsl:value-of select="/project/@generatedDate" />
						</td>
					</tr>
					<tr>
						<td align="center" width="25%" class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.compare.author" />
						</td>
						<td align="center" width="25%">
							<xsl:value-of select="/project/@author" />
						</td>
						<td align="center" width="25%" class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@versionName" />
						</td>
						<td align="center" width="25%" class="FONTSTYLE">
							<xsl:value-of select="/project/@version" />
						</td>
					</tr>
				</table>
				<h2 class="FONTSTYLE">
					<xsl:value-of select="/project/@i18n.compare.summary" />
				</h2>
				<div class="FONTSTYLE">
					<b>
						<a href="#ProjectDescription">
							<xsl:value-of select="/project/@i18n.compare.project.description" />
						</a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#CurrentJobPreviewPicture">
							<xsl:value-of
								select="/project/@i18n.compare.current.job.preview.picture" />
						</a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#ComparedJobPreviewPicture">
							<xsl:value-of select="/project/@i18n.compare.compare.job.picture" />
						</a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#JobSettingCompare">
							<xsl:value-of
								select="/project/@i18n.compare.compare.job.setting.compare" />
						</a>
					</b>
				</div>
				<div class="FONTSTYLE">
					<b>
						<a href="#ComparedComponentList">
							<xsl:value-of
								select="/project/@i18n.compare.compare.compared.component.list" />
						</a>
					</b>
				</div>
				<br />
				<br />
				<!-- Project Description -->
				<!--HR -->
				<h2 class="FONTSTYLE">
					<a name="#ProjectDescription">
						<xsl:value-of select="/project/@i18n.compare.project.description" />
					</a>
				</h2>
				<br />
				<table border="1" width="90%" class="FONTSTYLE" cellpadding="0"
					cellspacing="0" style="border-collapse: collapse; padding-left:10mm;"
					bordercolor="#111111" summary="">
					<tr>
						<th align="left" width="30%" class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.compare.properties" />
						</th>
						<th align="left" class="TABLECOLUMNSTYLE">
							<xsl:value-of select="/project/@i18n.compare.values" />
						</th>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="/project/@i18n.compare.name" />
						</td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="/project/@name" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="/project/@i18n.compare.language" />
						</td>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="/project/@language" />
						</td>
					</tr>
					<tr>
						<td class="FONTSTYLE" align="left">
							<xsl:value-of select="/project/@i18n.compare.description" />
						</td>
						<td class="FONTSTYLE" align="left">
							<pre>
								<xsl:value-of select="/project/pro-description"
									disable-output-escaping="yes" />
							</pre>
						</td>
					</tr>
				</table>
				<br />
				<br />
				<!-- Project Description -->
				<!--HR -->
				<xsl:variable name="jobCompare" select="/project/jobCompare" />
				<xsl:variable name="currentJobPreviewPicture" select="$jobCompare/curJobPreview" />
				<xsl:if test="string-length($currentJobPreviewPicture/@picture)!=0">
					<h2 class="FONTSTYLE">
						<a name="#CurrentJobPreviewPicture">
							<xsl:value-of
								select="/project/@i18n.compare.current.job.preview.picture" />
						</a>
					</h2>
					<br />
					<table border="0" class="FONTSTYLE" cellpadding="0"
						cellspacing="0" align="left" style="border-collapse: collapse"
						bordercolor="#111111" summary="">
						<tr class="FONTSTYLE">
							<td class="FONTSTYLE">
								<b>
									<xsl:value-of select="$currentJobPreviewPicture/@jobName" />
								</b>
							</td>
						</tr>
					</table>
					<br />
					<div class="FONTSTYLE" align="center">
						<img src="{$currentJobPreviewPicture/@picture}" usemap="#jobComparemap"
							alt="No image available" class="bordercolor" />
						<map id="jobComparemap" name="jobComparemap" />
					</div>
				</xsl:if>
				<br />
				<br />
				<xsl:variable name="comparedJobPreviewPicture" select="$jobCompare/comparedJobPreview" />
				<xsl:if test="string-length($comparedJobPreviewPicture/@picture)!=0">
					<h2 class="FONTSTYLE">
						<a name="#ComparedJobPreviewPicture">
							<xsl:value-of select="/project/@i18n.compare.compare.job.picture" />
						</a>
					</h2>
					<br />
					<table border="0" class="FONTSTYLE" cellpadding="0"
						cellspacing="0" align="center" style="border-collapse: collapse"
						bordercolor="#111111" summary="">
						<tr class="FONTSTYLE">
							<td class="FONTSTYLE">
								<b>
									<xsl:value-of select="$comparedJobPreviewPicture/@jobName" />
								</b>
							</td>
						</tr>
					</table>
					<br />
					<div class="FONTSTYLE" align="center">
						<img src="{$comparedJobPreviewPicture/@picture}" usemap="#jobComparemap"
							alt="No image available" class="bordercolor" />
						<map id="jobComparemap" name="jobComparemap" />
					</div>
				</xsl:if>
				<br />
				<br />

				<!--Job Setting Compare -->
				<!--HR -->
				<xsl:variable name="jobSetting" select="/project/jobSetting" />
				<h2 class="FONTSTYLE">
					<a name="#JobSettingCompare">
						<xsl:value-of select="/project/@i18n.compare.compare.job.setting.compare" />
					</a>
				</h2>
				<xsl:variable name="extraList" select="$jobSetting/extra/extraItem/@param" />
				<xsl:if test="string-length($extraList)!=0">
					<b class="FONTSTYLE">
						<xsl:value-of select="/project/@i18n.compare.extra" />
					</b>
					<br />
					<table width="50%" border="1" cellpadding="0" cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;" bordercolor="#111111"
						frame="box" summary="">
						<tr>
							<th width="33%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.parameter" />
							</th>
							<th width="33%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.current.value" />
							</th>
							<th width="33%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.old.value" />
							</th>
						</tr>
						<xsl:for-each select="$jobSetting/extra/extraItem">
							<tr>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@param" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@currentValue" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@oldValue" />
								</td>
							</tr>
						</xsl:for-each>
					</table>
					<br />
				</xsl:if>
				<xsl:variable name="statsAndLogsList"
					select="$jobSetting/statsAndLog/statsAndLogItem/@param" />
				<xsl:if test="string-length($statsAndLogsList)!=0">
					<b class="FONTSTYLE">
						<xsl:value-of select="/project/@i18n.compare.stats.logs" />
					</b>
					<br />
					<table width="50%" border="1" cellpadding="0" cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;" bordercolor="#111111"
						frame="box" summary="">
						<tr>
							<th width="33%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.parameter" />
							</th>
							<th width="33%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.current.value" />
							</th>
							<th width="33%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.old.value" />
							</th>
						</tr>
						<xsl:for-each select="$jobSetting/statsAndLog/statsAndLogItem">
							<tr>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@param" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@currentValue" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@oldValue" />
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</xsl:if>
				<br />
				<br />

				<!--Compared Component List -->
				<!--HR -->
				<xsl:variable name="component" select="/project/component" />
				<h2 class="FONTSTYLE">
					<a name="#ComparedComponentList">
						<xsl:value-of
							select="/project/@i18n.compare.compare.compared.component.list" />
					</a>
				</h2>
				<xsl:variable name="addComponentList"
					select="$component/addedComponentList/addedItem/@name" />
				<xsl:if test="string-length($addComponentList)!=0">
					<b class="FONTSTYLE">
						<xsl:value-of select="/project/@i18n.compare.added.component" />
					</b>
					<br />
					<table width="40%" border="1" cellpadding="0" cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;" bordercolor="#111111"
						frame="box" summary="">
						<tr>
							<th width="10%" align="left" class="TABLECOLUMNSTYLE">
							</th>
							<th width="45%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.component.name" />
							</th>
							<th width="45%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.component.type" />
							</th>
						</tr>
						<xsl:for-each select="$component/addedComponentList/addedItem">
							<tr>
								<td class="FONTSTYLE" align="left">
									<img src="{@icon}" alt="" />
									&#160;&#160;&#160;&#160;&#160;
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@name" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@type" />
								</td>
							</tr>
						</xsl:for-each>
					</table>
					<br />
				</xsl:if>

				<xsl:variable name="deleteComponentList"
					select="$component/deletedComponentList/deletedItem/@name" />
				<xsl:if test="string-length($deleteComponentList)!=0">
					<b class="FONTSTYLE">
						<xsl:value-of select="/project/@i18n.compare.current.deleted.component" />
					</b>
					<br />
					<table width="40%" border="1" cellpadding="0" cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;" bordercolor="#111111"
						frame="box" summary="">
						<tr>
							<th width="10%" align="left" class="TABLECOLUMNSTYLE">
							</th>
							<th width="45%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.component.name" />
							</th>
							<th width="45%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.component.type" />
							</th>
						</tr>
						<xsl:for-each select="$component/deletedComponentList/deletedItem">
							<tr>
								<td class="FONTSTYLE" align="left">
									<img src="{@icon}" alt="" />
									&#160;&#160;&#160;&#160;&#160;
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@name" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@type" />
								</td>
							</tr>
						</xsl:for-each>
					</table>
					<br />
				</xsl:if>

				<xsl:variable name="modifyComponentList"
					select="$component/modifiedComponentList/modifiedItem/@name" />
				<xsl:if test="string-length($modifyComponentList)!=0">
					<b class="FONTSTYLE">
						<xsl:value-of select="/project/@i18n.compare.added.modified.component" />
					</b>
					<br />
					<table width="40%" border="1" cellpadding="0" cellspacing="0"
						style="border-collapse: collapse; padding-left:10mm;" bordercolor="#111111"
						frame="box" summary="">
						<tr>
							<th width="10%" align="left" class="TABLECOLUMNSTYLE">
							</th>
							<th width="45%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.component.name" />
							</th>
							<th width="45%" align="left" class="TABLECOLUMNSTYLE">
								<xsl:value-of select="/project/@i18n.compare.component.type" />
							</th>
						</tr>
						<xsl:for-each select="$component/modifiedComponentList/modifiedItem">
							<tr>
								<td class="FONTSTYLE" align="center">
									<img src="{@icon}" alt="" />
									&#160;&#160;&#160;&#160;&#160;
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@name" />
								</td>
								<td class="FONTSTYLE" align="left">
									<xsl:value-of select="@type" />
								</td>
							</tr>
						</xsl:for-each>
					</table>
				</xsl:if>
				<br />
				<br />

				<!-- _START_ Compared Component Modified Detailed List -->
				<xsl:variable name="project" select="/project" />
				<xsl:if
					test="string-length($project/modifyComponentListDetail/modifyComponentItem/@componentName)!=0">
					<table border="0" class="FONTSTYLE" cellpadding="0"
						cellspacing="0" style="border-collapse: collapse" bordercolor="#111111"
						summary="">
						<tr bgcolor="#666666" class="FONTSTYLE">
							<td class="FONTSTYLE">
								<b>
									<xsl:value-of
										select="/project/@i18n.compare.current.modified.component.list" />
								</b>
							</td>
						</tr>
					</table>
					<br />
					<xsl:for-each
						select="$project/modifyComponentListDetail/modifyComponentItem">
						<table border="0" class="FONTSTYLE" cellpadding="0"
							cellspacing="0" style="border-collapse: collapse" bordercolor="#111111"
							summary="">
							<tr bgcolor="#888888" class="FONTSTYLE">
								<td class="FONTSTYLE">
									<b>
										<xsl:value-of select="/project/@i18n.compare.components" />
										<xsl:value-of select="@componentName" />
									</b>
								</td>
							</tr>
						</table>
						<br />
						<!-- _START_ paramList -->
						<xsl:for-each select="paramList">
							<table border="0" class="FONTSTYLE" cellpadding="0"
								cellspacing="0" style="border-collapse: collapse" bordercolor="#111111"
								summary="">
								<tr bgcolor="#E6E6E6" class="FONTSTYLE">
									<td class="FONTSTYLE">
										<b>
											<xsl:value-of select="@paraName" />
										</b>
									</td>
								</tr>
							</table>
							<table width="40%" border="1" cellpadding="0" cellspacing="0"
								style="border-collapse: collapse; padding-left:10mm;"
								bordercolor="#111111" frame="box" summary="">
								<tr>
									<th width="33%" align="left" class="TABLECOLUMNSTYLE">
										<xsl:value-of select="/project/@i18n.compare.parameter" />
									</th>
									<th width="33%" align="left" class="TABLECOLUMNSTYLE">
										<xsl:value-of select="/project/@i18n.compare.current.value" />
									</th>
									<th width="33%" align="left" class="TABLECOLUMNSTYLE">
										<xsl:value-of select="/project/@i18n.compare.old.value" />
									</th>
								</tr>
								<xsl:for-each select="paramItem">
									<tr>
										<td class="FONTSTYLE" align="left">
											<xsl:value-of select="@param" />
										</td>
										<td class="FONTSTYLE" align="left">
											<xsl:value-of select="@currentValue" />
										</td>
										<td class="FONTSTYLE" align="left">
											<xsl:value-of select="@oldValue" />
										</td>
									</tr>
								</xsl:for-each>
							</table>
							<br />
						</xsl:for-each>
						<!-- _END_ paramList -->

						<br />
						<!-- _START_ schemaList -->
						<xsl:for-each select="schema/schemaList">
							<table border="0" class="FONTSTYLE" cellpadding="0"
								cellspacing="0" style="border-collapse: collapse" bordercolor="#111111"
								summary="">
								<tr bgcolor="#E6E6E6" class="FONTSTYLE">
									<td class="FONTSTYLE">
										<b>
											<xsl:value-of select="/project/@i18n.compare.schemas" />
											<xsl:value-of select="@schemaName" />
										</b>
									</td>
								</tr>
							</table>
							<table width="40%" border="1" cellpadding="0" cellspacing="0"
								style="border-collapse: collapse; padding-left:10mm;"
								bordercolor="#111111" frame="box" summary="">
								<tr>
									<th width="33%" align="left" class="TABLECOLUMNSTYLE">
										<xsl:value-of select="/project/@i18n.compare.parameter" />
									</th>
									<th width="33%" align="left" class="TABLECOLUMNSTYLE">
										<xsl:value-of select="/project/@i18n.compare.current.value" />
									</th>
									<th width="33%" align="left" class="TABLECOLUMNSTYLE">
										<xsl:value-of select="/project/@i18n.compare.old.value" />
									</th>
								</tr>
								<xsl:for-each select="schemaItem">
									<tr>
										<td class="FONTSTYLE" align="left">
											<xsl:value-of select="@param" />
										</td>
										<td class="FONTSTYLE" align="left">
											<xsl:value-of select="@currentValue" />
										</td>
										<td class="FONTSTYLE" align="left">
											<xsl:value-of select="@oldValue" />
										</td>
									</tr>
								</xsl:for-each>
							</table>
						</xsl:for-each>
						<!-- _END_ schemaList -->

						<br />
						<!-- _START_ tableTypes -->
						<xsl:for-each select="tableType">
							<xsl:if
								test="string-length(table/@name)!=0 or string-length(added/@name)!=0 or string-length(deleted/@name)!=0">
								<table border="0" class="FONTSTYLE" cellpadding="0"
									cellspacing="0" style="border-collapse: collapse" bordercolor="#111111"
									summary="">
									<tr bgcolor="#E6E6E6" class="FONTSTYLE">
										<td class="FONTSTYLE">
											<b>
												<xsl:value-of select="@name" />
											</b>
										</td>
									</tr>
								</table>

								<!-- _START_ Modified tables -->
								<xsl:if test="string-length(table/@name)!=0">
									<xsl:for-each select="table">
										<table border="0" class="FONTSTYLE" cellpadding="0"
											cellspacing="0" style="border-collapse: collapse"
											bordercolor="#111111" summary="">
											<tr bgcolor="#E6E6E6" class="FONTSTYLE">
												<td class="FONTSTYLE">
													<b>
														<xsl:value-of select="/project/@i18n.compare.table" />
														:
														<xsl:value-of select="@name" />
													</b>
												</td>
											</tr>
										</table>

										<!-- _START_ Modified Columns -->
										<xsl:if test="string-length(modified/@name)!=0">
											<xsl:for-each select="modified">
												<table border="0" class="FONTSTYLE" cellpadding="0"
													cellspacing="0" style="border-collapse: collapse"
													bordercolor="#111111" summary="">
													<tr bgcolor="#E6E6E6" class="FONTSTYLE">
														<td class="FONTSTYLE">
															<b>
																<xsl:value-of select="@name" />
															</b>
														</td>
													</tr>
												</table>

												<table width="40%" border="1" cellpadding="0"
													cellspacing="0" style="border-collapse: collapse; padding-left:10mm;"
													bordercolor="#111111" frame="box" summary="">
													<tr>
														<th width="33%" align="left" class="TABLECOLUMNSTYLE">
															<xsl:value-of select="/project/@i18n.compare.column" />
														</th>
														<th width="33%" align="left" class="TABLECOLUMNSTYLE">
															<xsl:value-of select="/project/@i18n.compare.current.value" />
														</th>
														<th width="33%" align="left" class="TABLECOLUMNSTYLE">
															<xsl:value-of select="/project/@i18n.compare.old.value" />
														</th>
													</tr>
													<xsl:for-each select="column">
														<tr>
															<td class="FONTSTYLE" align="left">
																<xsl:value-of select="@name" />
															</td>
															<td class="FONTSTYLE" align="left">
																<xsl:value-of select="@currentValue" />
															</td>
															<td class="FONTSTYLE" align="left">
																<xsl:value-of select="@oldValue" />
															</td>
														</tr>
													</xsl:for-each>
												</table>
												<br />
											</xsl:for-each>
										</xsl:if>
										<!-- _END_ Modified Columns -->
										<br />

										<!-- _START_ Added Columns -->
										<xsl:if test="string-length(added/column/@name)!=0">
											<xsl:for-each select="added">
												<table border="0" class="FONTSTYLE" cellpadding="0"
													cellspacing="0" style="border-collapse: collapse"
													bordercolor="#111111" summary="">
													<tr bgcolor="#E6E6E6" class="FONTSTYLE">
														<td class="FONTSTYLE">
															<b>
																<xsl:value-of select="/project/@i18n.compare.added" />
															</b>
														</td>
													</tr>
												</table>

												<table width="40%" border="1" cellpadding="0"
													cellspacing="0" style="border-collapse: collapse; padding-left:10mm;"
													bordercolor="#111111" frame="box" summary="">
													<tr>
														<th width="33%" align="left" class="TABLECOLUMNSTYLE">
															<xsl:value-of select="/project/@i18n.compare.column" />
														</th>
													</tr>
													<xsl:for-each select="column">
														<tr>
															<td class="FONTSTYLE" align="left">
																<xsl:value-of select="@name" />
															</td>
														</tr>
													</xsl:for-each>
												</table>
												<br />
											</xsl:for-each>
										</xsl:if>
										<!-- _END_ Added Columns -->
										<br />

										<!-- _START_ Deleted Columns -->
										<xsl:if test="string-length(deleted/column/@name)!=0">
											<xsl:for-each select="deleted">
												<table border="0" class="FONTSTYLE" cellpadding="0"
													cellspacing="0" style="border-collapse: collapse"
													bordercolor="#111111" summary="">
													<tr bgcolor="#E6E6E6" class="FONTSTYLE">
														<td class="FONTSTYLE">
															<b>
																<xsl:value-of select="/project/@i18n.compare.deleted" />
															</b>
														</td>
													</tr>
												</table>

												<table width="40%" border="1" cellpadding="0"
													cellspacing="0" style="border-collapse: collapse; padding-left:10mm;"
													bordercolor="#111111" frame="box" summary="">
													<tr>
														<th width="33%" align="left" class="TABLECOLUMNSTYLE">
															<xsl:value-of select="/project/@i18n.compare.column" />
														</th>
													</tr>
													<xsl:for-each select="column">
														<tr>
															<td class="FONTSTYLE" align="left">
																<xsl:value-of select="@name" />
															</td>
														</tr>
													</xsl:for-each>
												</table>
												<br />
											</xsl:for-each>
										</xsl:if>
										<!-- _END_ Deleted Columns -->

									</xsl:for-each>
								</xsl:if>
								<br />
								<!-- _START_ Modified tables -->

								<!-- _START_ Add tables -->
								<xsl:if test="string-length(added/@name)!=0">
									<table width="40%" border="1" cellpadding="0"
										cellspacing="0" style="border-collapse: collapse; padding-left:10mm;"
										bordercolor="#111111" frame="box" summary="">
										<tr bgcolor="#E6E6E6" class="FONTSTYLE">
											<td class="FONTSTYLE">
												<b>
													<xsl:value-of select="/project/@i18n.compare.added.tables" />
												</b>
											</td>
										</tr>
										<xsl:for-each select="added">
											<tr>
												<td class="FONTSTYLE" align="left">
													<xsl:value-of select="@name" />
												</td>
											</tr>
										</xsl:for-each>
									</table>
									<br />
								</xsl:if>
								<!-- _END_ Added tables -->

								<!-- _START_ Deteled tables -->
								<xsl:if test="string-length(deleted/@name)!=0">
									<table width="40%" border="1" cellpadding="0"
										cellspacing="0" style="border-collapse: collapse; padding-left:10mm;"
										bordercolor="#111111" frame="box" summary="">
										<tr bgcolor="#E6E6E6" class="FONTSTYLE">
											<td class="FONTSTYLE">
												<b>
													<xsl:value-of select="/project/@i18n.compare.deleted.tables" />
												</b>
											</td>
										</tr>
										<xsl:for-each select="deleted">
											<tr>
												<td class="FONTSTYLE" align="left">
													<xsl:value-of select="@name" />
												</td>
											</tr>
										</xsl:for-each>
									</table>
									<br />
								</xsl:if>
								<!-- _END_ Deteled tables -->

							</xsl:if>
						</xsl:for-each>
						<!-- _END_ tableType -->

					</xsl:for-each>
				</xsl:if>
				<!-- _END_ Compared Component Modified Detailed List -->
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
