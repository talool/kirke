<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method='xml' indent='no'/>

	<xsl:template match="/Fundraisers">
		<Talool xmlns="http://www.talool.com/Talool">
			<xsl:apply-templates select="Fundraiser"/>
		</Talool>
	</xsl:template>
	
	<xsl:template match="Fundraiser">
		<Merchant name="{Name}" category="Fun" fundraiser="true" fundraiser_percentage="50">
			<Locations>
				<Location name="" url="" phone="(253)854-9074" address1="105 N. Central Ave."
					address2="" city="Kent" state="WA" zip="98032" country="US"
					latitude="47.38178" longitude="-122.23108">
					<Image type="logo"
						url="https://print.entertainment.com/medias/sys_master/celum_assets/86c/1bc/1ec/82c/8821607497758_C000000000000DAR.png" />
				</Location>
			</Locations>
		</Merchant>
	</xsl:template>
	
</xsl:stylesheet>