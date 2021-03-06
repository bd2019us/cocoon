<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:error="http://apache.org/cocoon/error/2.0">

  <xsl:template match="error:notify">
    <html>
      <head>
        <title>
          <xsl:value-of select="@type"/>:<xsl:value-of select="error:title"/>
        </title>
      </head>
      <body bgcolor="#ffffff">
        <table border="0" bgcolor="#000000" cellpadding="2" cellspacing="2">
          <tbody>
            <tr>
              <td bgcolor="#0086b2" colspan="2">
                <font color="#ffffff" face="arial,helvetica,sanserif" size="+2">
                  <xsl:value-of select="error:title"/>
                </font>
              </td>
            </tr>

            <tr>
              <td bgcolor="#0086b2" valign="top">
                <font color="#ffffff" face="arial,helvetica,sanserif" size="+1">
                  <xsl:value-of select="@type"/>
                </font>
              </td>
              <td bgcolor="#ffffff" >
                <xsl:apply-templates select="error:message"/>
              </td>
            </tr>

            <tr>
              <td bgcolor="#0086b2" valign="top" colspan="2">
                <font color="#ffffff" face="arial,helvetica,sanserif" size="+1">details</font>
              </td>
            </tr>

            <tr>
              <td bgcolor="#0086b2" valign="top">
                <font face="arial,helvetica,sanserif" color="#ffffff">from</font>
              </td>
              <td bgcolor="#ffffff">
                <font face="arial,helvetica,sanserif">
                  <xsl:value-of select="@sender"/>
                </font>
              </td>
            </tr>

            <tr>
              <td bgcolor="#0086b2" valign="top">
                <font face="arial,helvetica,sanserif" color="#ffffff">source</font>
              </td>
              <td bgcolor="#ffffff">
                <font face="arial,helvetica,sanserif">
                  <xsl:value-of select="error:source"/>
                </font>
              </td>
            </tr>

            <xsl:apply-templates select="error:description"/>

            <tr>
              <td bgcolor="#0086b2" valign="top" colspan="2">
                <font color="#ffffff" face="arial,helvetica,sanserif" size="+1">extra info</font>
              </td>
            </tr>

            <xsl:apply-templates select="error:extra"/>

          </tbody>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="error:description">
    <tr>
      <td bgcolor="#0086b2" valign="top">
        <font color="#ffffff" face="arial,helvetica,sanserif">description</font>
      </td>
      <td bgcolor="#ffffff">
        <font face="arial,helvetica,sanserif">
          <xsl:value-of select="."/>
        </font>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="error:message">
    <font face="arial,helvetica,sanserif">
      <xsl:value-of select="."/>
    </font>
  </xsl:template>

  <xsl:template match="error:extra">
    <tr>
      <td bgcolor="#0086b2" valign="top">
        <font color="#ffffff" face="arial,helvetica,sanserif">
          <xsl:value-of select="@description"/>
        </font>
      </td>
      <td bgcolor="#ffffff">
        <pre>
          <xsl:value-of select="."/>
        </pre>
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
<!-- vim: set et ts=2 sw=2: -->
