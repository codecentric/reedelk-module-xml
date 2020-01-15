<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <html>
            <body>
                <h2>XSLT transformation example</h2>
                <table border="1">
                    <tr bgcolor="grey">
                        <th>First Name</th>
                        <th>Surname</th>
                        <th>First line of Address</th>
                        <th>Second line of Address</th>
                        <th>City</th>
                        <th>Age</th>
                    </tr>
                    <xsl:for-each select="persons/person">
                        <tr>
                            <td><xsl:value-of select="name/firstName"/></td>
                            <td><xsl:value-of select="name/surname"/></td>
                            <td><xsl:value-of select="address/firstLine"/></td>
                            <td><xsl:value-of select="address/secondLine"/></td>
                            <td><xsl:value-of select="address/city"/></td>
                            <td><xsl:value-of select="age"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>