<!-- Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.  --> 
<!-- $Id$ -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:output method="html" indent="yes"/>
<xsl:decimal-format decimal-separator="." grouping-separator="," />
<xsl:template match="contestStandings">
    <HTML>
        <HEAD>
<TITLE>
Summary - <xsl:value-of select="/contestStandings/standingsHeader/@title"/>
</TITLE>
<META HTTP-EQUIV="EXPIRES" CONTENT="0"/>
<META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE"/>
<META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE"/>
        </HEAD>
        <BODY>
            <TABLE border="0">
                <xsl:call-template name="rankRow"/>
                <xsl:call-template name="colorRow"/>
                <xsl:call-template name="teamStanding"/>
                <xsl:call-template name="rankRow"/>
                <xsl:call-template name="colorRow"/>
                <xsl:call-template name="summary"/>
            </TABLE>
<p>
Created by <A HREF="https://pc2ccs.github.io/">CSUS PC^2 <xsl:value-of select="/contestStandings/standingsHeader/@systemVersion"/></A><br/>
<A HREF="https://pc2ccs.github.io/">https://pc2ccs.github.io/</A><br/>
Last updated
<xsl:value-of select="/contestStandings/standingsHeader/@currentDate"/>
</p>
        </BODY>
    </HTML>
</xsl:template>

        <xsl:template name="summary">
            <xsl:for-each select="standingsHeader">
                <tr>
<td></td>
<td>Submitted/1st Yes/Total Yes</td>
<td></td>
<td></td>
                <xsl:call-template name="problemsummary"/>
<td><xsl:value-of select="@totalAttempts"/>/<xsl:value-of select="@totalSolved"/></td>
                </tr>
            </xsl:for-each>
        </xsl:template>
        <xsl:template name="problemsummary">
            <xsl:for-each select="/contestStandings/standingsHeader/problem">
<!-- <problemsummary attempts="246" bestSolutionTime="25" id="1" lastsolutionTime="283" numberSolved="81" title="A+ Consanguine Calculations"/> -->
<td>
<xsl:value-of select="@attempts"/>/<xsl:if test="@numberSolved &lt; '1'">--</xsl:if>
<xsl:if test="@bestSolutionTime"><xsl:value-of select="@bestSolutionTime"/></xsl:if>/<xsl:value-of select="@numberSolved"/>
</td>
            </xsl:for-each>
        </xsl:template>
        <xsl:template name="teamStanding">
            <xsl:for-each select="teamStanding">
                <tr>
<td><xsl:value-of select="@rank"/></td>
<td><xsl:value-of select="@teamName"/></td>
<td><xsl:value-of select="@solved"/></td>
<td><xsl:value-of select="@points"/></td>
                <xsl:call-template name="problemSummaryInfo"/>
<!-- <teamStanding index="1" solved="8" problemsattempted="8" rank="1" score="1405" teamName="Warsaw University" timefirstsolved="13" timelastsolved="272" totalAttempts="19" userid="84" usersiteid="1"> -->
<td><xsl:value-of select="@totalAttempts"/>/<xsl:value-of select="@solved"/></td>
                </tr>
            </xsl:for-each>
        </xsl:template>
        <xsl:template name="problemSummaryInfo">
            <xsl:for-each select="problemSummaryInfo">
<!-- <problemSummaryInfo attempts="1" index="1" problemid="1" score="73" solutionTime="73"/> -->
<td>
<xsl:value-of select="@attempts"/>/<xsl:if test="@isSolved = 'false'">--</xsl:if>
<xsl:if test="@isSolved = 'true'"><xsl:value-of select="@solutionTime"/></xsl:if>
</td>
            </xsl:for-each>
        </xsl:template>
        <xsl:template name="problemTitle">
            <xsl:for-each select="/contestStandings/standingsHeader/problem">
<th>&#160;&#160;&#160;&#160;<strong><u><xsl:number format="A" value="@id"/></u></strong>&#160;&#160;&#160;&#160;</th>
            </xsl:for-each>
        </xsl:template>
        <xsl:template name="problemColor">
            <xsl:for-each select="/contestStandings/standingsHeader/colorList/colors[@siteNum = 1]/problem">
                <td><center><xsl:choose><xsl:when test="@color"> <xsl:value-of select="@color"/></xsl:when><xsl:otherwise>Color<xsl:value-of select="@id"/></xsl:otherwise></xsl:choose></center></td>
            </xsl:for-each>
        </xsl:template>
        <xsl:template name="rankRow">
                <tr><th><strong><u>Rank</u></strong></th>
<th><strong><u>Name</u></strong></th>
<th><strong><u>Solved</u></strong></th>
<th><strong><u>Time</u></strong></th>
                <xsl:call-template name="problemTitle"/>
<th>Total att/solv</th></tr>
        </xsl:template>
        <xsl:template name="colorRow">
<tr><td></td><td></td><td></td><td></td>
                <xsl:call-template name="problemColor"/>
</tr>
        </xsl:template>
</xsl:stylesheet>
