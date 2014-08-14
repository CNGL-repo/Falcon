<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:its="http://www.w3.org/2005/11/its"
    xmlns:itsx="http://itsx.uni.me/itsx.xsd"
    xmlns:dx="http://schema.interoperability-now.org">

    <xsl:output method="text" />
    <xsl:strip-space elements="*"/>

    <!-- Root -->
    <xsl:template match="/globicdata/metadata">
        @prefix rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns>.
        @prefix its: &lt;http://www.cngl.ie/ontologies/#>.
        @prefix lang: &lt;http://http://www.cngl.ie/#lang>.
        @prefix AuthorText: &lt;http://www.cngl.ie/ontologies/#AuthorText/>.
        @prefix gic: &lt;http://www.cngl.ie/>.
        @prefix jobs: &lt;http://www.cngl.ie/jobs/>.
        @prefix logs: &lt;http://www.cngl.ie/logger/logs/>.
        @prefix prv: &lt;http://replace-with-provenance/>.
        @prefix prov:&lt;http://www.w3.org/ns/prov#>.
        @prefix dcterms:&lt;http://purl.org/dc/terms/>.
        @prefix str: &lt;http://nlp2rdf.lod2.eu/schema/string/> .
        @prefix its: &lt;http://www.w3.org/2005/11/its/rdf#>.
 
       &lt;<xsl:value-of select="concat('http://www.cngl.ie/sd/lbl/',substring-after(contenturl,'content/') )" />> a gic:LeanBackLearning; 
       gic:contentURL &lt;<xsl:value-of select="contenturl"/>>;
       gic:topics "<xsl:value-of select="topics"/>";
       gic:sourceLang "<xsl:value-of select="langentered"/>";
       gic:targetLang "<xsl:value-of select="outputlang"/>".

    </xsl:template>
</xsl:stylesheet>