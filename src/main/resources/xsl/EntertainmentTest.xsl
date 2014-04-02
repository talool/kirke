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
							<Tag><xsl:apply-templates select="subcategory"/></Tag>
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
								  latitude="{latitude div 10000}" 
								  longitude="{longitude div 10000}">
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
									<Tag><xsl:apply-templates select="subcategory"/></Tag>
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
	
	<!-- let subcategory that don't match a test pass through so we can log it when kirke runs -->
	<xsl:template match="subcategory">
		<xsl:choose>
			<xsl:when test="current() = 'Bakeries/Donuts/Bagels'">Bakery</xsl:when>
			<xsl:when test="current() = 'Pubs &amp; Taverns'">Pub</xsl:when>
			<xsl:when test="current() = 'Smoothie &amp; Juice Bars'">Juice Bar</xsl:when>
			<xsl:when test="current() = 'Sub Shops/Deli'">Deli</xsl:when>
			
			<xsl:when test="current() = 'Family Fun Center'
						 or current() = 'Theme Parks'">Amusement Center</xsl:when>
			<xsl:when test="current() = 'Fitness'">Gym</xsl:when>
			<xsl:when test="current() = 'More Sports'
						 or current() = 'More Sports &amp; Activities'">Sporting Events</xsl:when>
			<xsl:when test="current() = 'More Shopping'
			             or current() = 'More Retail'
			             or current() = 'More Services'">Shopping</xsl:when>
			<xsl:when test="current() = 'Performance &amp; Theatre'">Theater</xsl:when>
			<xsl:when test="current() = 'Skating Rinks'">Skating</xsl:when>
			<xsl:when test="current() = 'Skiing'">Skiing / Boarding</xsl:when>
			<xsl:when test="current() = 'Zoos/Museums/Aquariums'">Museum</xsl:when>
			
			<xsl:when test="current() = 'Apparel &amp; Accessories'">Accessories Store</xsl:when>
			<xsl:when test="current() = 'Appliances &amp; Electronics'">Electronics Store</xsl:when>
			<xsl:when test="current() = 'Florists'">Flower Shop</xsl:when>
			<xsl:when test="current() = 'Home &amp; Garden'">Garden Center</xsl:when>
			<xsl:when test="current() = 'Jewelry'">Jewelry Store</xsl:when>
			<xsl:when test="current() = 'Music/Books/Video'">Bookstore</xsl:when>
			<xsl:when test="current() = 'Pet Supplies'">Pet Store</xsl:when>
			<xsl:when test="current() = 'Shoes'">Shoe Store</xsl:when>
			<xsl:when test="current() = 'Sporting Goods'">Sporting Goods Shop</xsl:when>
			<xsl:when test="current() = 'Toys &amp; Hobbies'">Toy / Game Store</xsl:when>
			<xsl:when test="current() = 'Dry Cleaners'">Dry Cleaning</xsl:when>
			<xsl:when test="current() = 'Hair &amp; Nail Salon'">Salon</xsl:when>
			<xsl:when test="current() = 'Learning Centers'">Lessons</xsl:when>
			<xsl:when test="current() = 'Services/Limo Service'">Limo Service</xsl:when>
			<xsl:when test="current() = 'Tanning'">Tanning Salon</xsl:when>
			
			<xsl:otherwise><xsl:value-of select="current()"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>