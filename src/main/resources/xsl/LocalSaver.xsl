<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method='xml' indent='no'/>
	
	<xsl:template match="/DataSphereFeed">
		<Talool nextPage="{NextPage}" xmlns="http://www.talool.com/Talool">
			<xsl:apply-templates select="Records"/>
		</Talool>
	</xsl:template>
	
	
	
	<xsl:template match="Offer">
	
		<xsl:variable name="taloolCategoryName">
			<xsl:apply-templates select="Business/Category"/>
		</xsl:variable>
		
		<Merchant name="{Business/Name}" category="{$taloolCategoryName}">
			<Tags>
				<xsl:if test="Business/SubCategory != ''">
					<Tag><xsl:value-of select="Business/SubCategory"/></Tag>
				</xsl:if>
			</Tags>
			<Locations>
				<xsl:apply-templates select="Business"/>
			</Locations>
			<Deals>
				<Deal title="{MiniTitle}" expires="{ExpireDate}">
					<Summary><xsl:value-of select="Description"/></Summary>
               		<Details><xsl:value-of select="Limitations"/></Details>
					<Tags>
						<xsl:if test="Business/SubCategory != ''">
							<Tag><xsl:value-of select="Business/SubCategory"/></Tag>
						</xsl:if>
					</Tags>
				</Deal>
			</Deals>
		</Merchant>
		
	</xsl:template>
	
	
	
	<xsl:template match="Business">
		<Location name="" 
			      url=""
			      phone="{Phonenumber}"
				  address1="{Location/StreetLine1}" 
				  address2="{Location/StreetLine2}" 
				  city="{Location/City}" 
				  state="{Location/State}" 
				  zip="{Location/PostalCode}" 
				  country="US" 
				  latitude="{Location/Lat}" 
				  longitude="{Location/Long}">
			<xsl:if test="UrlLogo != ''"> 
				<Image type="logo" url="{UrlLogo}"/>
			</xsl:if>
		</Location>
	</xsl:template>
	
	
	<!-- let Categories that don't match a test pass through so we can log it when kirke runs -->
	<xsl:template match="Category">
		<xsl:choose>
			<xsl:when test="current() = 'Food &amp; Dining'">Food</xsl:when>
			<xsl:when test="current() = 'Autos' 
						 or current() = 'Legal &amp; Financial'
						 or current() = 'Health &amp; Beauty'
						 or current() = 'Professional Services'
						 or current() = 'Real Estate'
						 or current() = 'Pets &amp; Animals'
						 or current() = 'Retail Shopping'
						 or current() = 'Business Services'
						 or current() = 'Home &amp; Garden'
						 or current() = 'Travel &amp; Lodging'
						 or current() = 'Education'">Shopping Services</xsl:when>
			<xsl:when test="current() = 'Events &amp; Attractions'
						 or current() = 'Recreation'
						 or current() = 'Entertainment &amp; Arts'
						 or current() = 'Community'">Fun</xsl:when>
			<xsl:otherwise><xsl:value-of select="current()"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	
</xsl:stylesheet>