<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method='xml' indent='no'/>
	
	<xsl:variable name="defaultState">CO</xsl:variable>
	<xsl:variable name="defaultExpDate">2015-09-01</xsl:variable>
	
	<xsl:template match="/Talool">
		<Talool xmlns="http://www.talool.com/Talool">
			<xsl:apply-templates/>
		</Talool>
	</xsl:template>
	
	<xsl:template match="Merchant">
		<Merchant name="{@name}" category="{@category}">
			<Tags><Tag><xsl:value-of select="@tag"/></Tag></Tags>
			<xsl:apply-templates/>
		</Merchant>
	</xsl:template>
	
	<xsl:template match="Location">
	
		<xsl:variable name="state">
			<xsl:choose>
				<xsl:when test="@state = ''"><xsl:value-of select="$defaultState"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="@state"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="url">
			<xsl:choose>
				<xsl:when test="@url = ''"></xsl:when>
				<xsl:otherwise>http://<xsl:value-of select="@url"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<Location name="" 
			      url="{$url}"
			      phone="{@phone}"
				  address1="{@address1}" 
				  address2="{@address2}" 
				  city="{@city}" 
				  state="{$state}" 
				  zip="{@zip}" 
				  country="US" 
				  latitude="{@lat}" 
				  longitude="{@long}">
		</Location>
		
	</xsl:template>
	
	<xsl:template match="Deal">
		<Deal title="{Title}" expires="{$defaultExpDate}">
			<xsl:apply-templates select="Summary"/>
			<xsl:apply-templates select="Details"/>
		</Deal>
	</xsl:template>
	
	<xsl:template match="Details">
		<xsl:variable name="defaultDetails"> May not be combined with any other offer, discount or promotion. Not valid on holidays, and subject to rules of use.</xsl:variable>
		
		<Details>
			<xsl:value-of select="."/> <xsl:value-of select="$defaultDetails"/>
		</Details>
		
		<Tags><Tag><xsl:value-of select="../../../@tag"/></Tag></Tags>
		
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:copy><xsl:apply-templates/></xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>