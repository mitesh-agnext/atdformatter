<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cr="urn:crystal-reports:schemas:report-detail"
                exclude-result-prefixes="cr">
    <xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/cr:CrystalReport">
        <Envelope>
            <Body>
                <MessageParts>
                    <SalesOrder>
                        <SalesTable class="entity">
                            <CustAccount>
                                <xsl:value-of select="cr:Group/cr:GroupHeader/cr:Section/cr:Field[@FieldName='{INVENTRY.Description2}']/cr:Value"/>
                            </CustAccount>
                            <PurchOrderFormNum>PO</PurchOrderFormNum>
                            <!-- sale lines -->
                            <xsl:for-each select="cr:Group/cr:Group/cr:Details/cr:Section">
                                <SalesLine class="entity">
                                    <ItemId>
                                        <xsl:value-of select="cr:Field[@FieldName='{STATION.Comments}']/cr:Value"/>
                                    </ItemId>
                                    <SalesQty>
                                        <xsl:value-of select="cr:Field[@FieldName='{STATION.Quantity}']/cr:Value"/>
                                    </SalesQty>
                                </SalesLine>
                            </xsl:for-each>
                        </SalesTable>
                    </SalesOrder>
                </MessageParts>
            </Body>
        </Envelope>
    </xsl:template>

</xsl:stylesheet>
