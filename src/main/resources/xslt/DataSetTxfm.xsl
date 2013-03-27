<xsl:stylesheet version="2.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xs="http://www.w3.org/2001/XMLSchema">
   <xsl:output method="text"/>
   <xsl:strip-space elements="*"/>

<xsl:template match="/">
STARTTOKENEVENT, BEFORE, DURING, EQUAL, FINISH, MEET, OVERLAP, START DATAMARKER
   <xsl:for-each select="node()/child::node()[name()='event']">
EVENTID-<xsl:value-of select="@id"/>,<xsl:value-of select="./child::node()[@relation='BEFORE']/attribute::targetId"/>,<xsl:value-of select="./child::node()[@relation='DURING']/attribute::targetId"/>,<xsl:value-of select="./child::node()[@relation='EQUAL']/attribute::targetId"/>,<xsl:value-of select="./child::node()[@relation='FINISH']/attribute::targetId"/>,<xsl:value-of select="./child::node()[@relation='MEET']/attribute::targetId"/>,<xsl:value-of select="./child::node()[@relation='OVERLAP']/attribute::targetId"/>,<xsl:value-of select="./child::node()[@relation='START']/attribute::targetId"/>
<xsl:text>&#10;</xsl:text>
</xsl:for-each>ENDTOKEN

All Event Descriptions:
=======================
<xsl:for-each select="node()">
<xsl:text>&#10;</xsl:text>
   <xsl:for-each select="child::node()[name()='event']">
EVENT-DESC-EVENT # <xsl:value-of select="@id"/> ==> <xsl:value-of select="@name"/>
      		[Type = <xsl:value-of select="./node()[name()='eventProperty'][@name='eventType']"/>][Computed Time Value = <xsl:value-of select="./node()[name()='eventProperty'][@name='time']"/>]
   </xsl:for-each>
</xsl:for-each>
<xsl:text>&#10;</xsl:text>
All Asserted and Inferred Relations:
====================================
<xsl:for-each select="node()">
   <xsl:for-each select="child::node()[name()='eventStmt']">
EVENT-RELATION # <xsl:value-of select="position()"/>: ( <xsl:value-of select="substring(@sourceEvent, 1, 48)"/> ...)==>[ <xsl:value-of select="@relation"/> ]==>( <xsl:value-of select="substring(@targetEvent, 1, 48)"/> ...)
   </xsl:for-each>
</xsl:for-each>
Timeline Buckets:
================
<xsl:for-each select="node()">
   <xsl:for-each select="child::node()[name()='timelineEntry']">
EVENT-TimeLineEntry # <xsl:value-of select="position()"/>: <xsl:value-of select="."/>
   </xsl:for-each>
</xsl:for-each>
</xsl:template>
</xsl:stylesheet>
