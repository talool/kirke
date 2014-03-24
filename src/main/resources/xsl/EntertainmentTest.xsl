<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:entertainment="http://www.entertainment.com">
	<xsl:output method='xml' indent='no'/>
	<xsl:template match="/">
		<Talool xmlns="http://www.talool.com/Talool">
			<xsl:for-each select="/entertainment:offerReport/offer_detail">
				<xsl:variable name="taloolCategoryName">
					<xsl:choose>
						<xsl:when test="category = 'Dining'">Food</xsl:when>
						<xsl:when test="category = 'Shopping'">Shopping Services</xsl:when>
						<xsl:when test="category = 'Events &amp; Attractions'">Fun</xsl:when>
						<xsl:otherwise>Food</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<Merchant name="{locationname}" category="{$taloolCategoryName}" fundraiser="false">
					<Tags>
						<xsl:if test="subcategory != ''">
							<Tag><xsl:value-of select="subcategory"/></Tag>
						</xsl:if>
						<xsl:if test="cuisineType != ''">
							<Tag><xsl:value-of select="cuisineType"/></Tag>
						</xsl:if>
					</Tags>
					<Locations>
						<Location name="" 
							      url="{url}"
							      phone="{phone}"
								  address1="{address}" 
								  address2="" 
								  city="{city}" 
								  state="{state}" 
								  zip="{zipcode}" 
								  country="{countryabbreviation}" 
								  latitude="{latitude}" 
								  longitude="{longitude}">
							<xsl:if test="logoUrl != ''"> 
								<Image type="logo" url="{logoUrl}"/>
							</xsl:if>
							<xsl:if test="photo != ''"> 
								<Image type="photo" url="{photo}"/>
							</xsl:if>
						</Location>
					</Locations>
					<Deals>
						<Deal title="{offerTextShort}" expires="{expireDate}" rating="{rating}" value="{dollarvalue}">
							<Summary><xsl:value-of select="offerTextLong"/></Summary>
	                		<Details><xsl:value-of select="offerQualifier"/></Details>
	                		<xsl:if test="photo != ''"> 
								<Image type="photo" url="{photo}"/>
							</xsl:if>
							<Tags>
								<xsl:if test="subcategory != ''">
									<Tag><xsl:value-of select="subcategory"/></Tag>
								</xsl:if>
								<xsl:if test="cuisineType != ''">
									<Tag><xsl:value-of select="cuisineType"/></Tag>
								</xsl:if>
							</Tags>
						</Deal>
					</Deals>
				</Merchant>
			</xsl:for-each>
		</Talool>
	</xsl:template>
</xsl:stylesheet>