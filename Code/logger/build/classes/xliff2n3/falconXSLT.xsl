<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:its="http://www.w3.org/2005/11/its"
    xmlns:itsx="http://itsx.uni.me/itsx.xsd"
    xmlns:dx="http://schema.interoperability-now.org">

    <xsl:output method="text" />
    <xsl:strip-space elements="*"/>
    <xsl:param name="xliffFileName" select="//xliff:file/@original"/>  <!-- Receives the XLIFF file name -->
    <xsl:param name="mid" select="''"/>
    <xsl:variable name="jobId" select="//xliff:phase[@job-id][1]/@job-id" />
    <xsl:variable name="creatorName" select="//xliff:phase/@contact-name" />
    <xsl:variable name="jobUri" select="concat('http://www.cngl.ie/jobs/', $jobId)" />
    <xsl:variable name="creatorUri" select="concat('http://www.cngl.ie/jobs/creator/', $creatorName)" />
    <xsl:variable name="sourceLang" select="//xliff:file/@source-language" />
    <xsl:variable name="targetLang" select="//xliff:file/@target-language" />

    <!-- Root -->
    <xsl:template match="/xliff:xliff">
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

	&lt;<xsl:value-of select="$jobUri" />> a gic:Project.
	&lt;<xsl:value-of select="$jobUri" />> a prov:Activity;
	&#09;gic:fileFormat "XLIFF 1.2";
	&#09;gic:activityId  "<xsl:value-of select="$jobId"/>";
	<xsl:if test="//xliff:pmui-data/@startdate != ''">&#09;prov:startedAtTime "<xsl:value-of select="//xliff:pmui-data/@startdate"/>";</xsl:if>
	<xsl:if test="//xliff:pmui-data/@deadline != ''">&#09;prov:endedAtTime "<xsl:value-of select="//xliff:pmui-data/@deadline"/>";</xsl:if>&#09;prov:wasAssociatedWith &lt;<xsl:value-of select="$creatorUri"/>>;
	&#09;gic:xliffSourceLang lang:<xsl:value-of select="//xliff:file/@source-language"/>;
	&#09;gic:xliffTargetLang lang:<xsl:value-of select="//xliff:file/@target-language"/>;
	<xsl:if test="//xliff:pmui-data/@pname != ''">&#09;gic:projectName  "<xsl:value-of select="//xliff:pmui-data/@pname"/>";</xsl:if>
	<xsl:if test="//xliff:pmui-data/@pdescription != ''">&#09;gic:projectDescription  "<xsl:value-of select="//xliff:pmui-data/@pdescription"/>";</xsl:if> &#09;gic:sourceContent "<xsl:value-of select="$xliffFileName" />".
        
	<xsl:apply-templates select="xliff:file" />
    </xsl:template>

	<xsl:template match="xliff:file">
        <xsl:variable name="fileUri" select="concat($jobUri, '/', @target-language)" />
        <xsl:for-each select="//dx:utx-glossary/dx:utx-term">
	&#09;&lt;<xsl:value-of select="concat($fileUri, '/', @dx:entry-id)" />> a gic:Terminology;
	&#09;gic:term "<xsl:value-of select="dx:utx-src"/>";
	&#09;gic:termType "<xsl:value-of select="dx:utx-src/@dx:pos"/>";
	&#09;gic:termTranslation "<xsl:value-of select="dx:utx-tgt"/>";
	&#09;gic:termStatus "<xsl:value-of select="dx:utx-tgt/@dx:status"/>";.
        
</xsl:for-each>
	
     <xsl:for-each select="//xliff:header/its:provenanceRecords"><xsl:variable name="precs" select="@xml:id" />
       <xsl:for-each select="its:provenanceRecord">&lt;<xsl:value-of select="@its:provRef"/>> a prov:Bundle;<xsl:if test="@its:tool!= ''">
       &#09;its:tool "<xsl:value-of select="@its:tool"/>";</xsl:if>
       <xsl:if test="@its:toolRef != ''"> &#09;its:toolRef "<xsl:value-of select="@its:toolRef"/>";</xsl:if>
       <xsl:if test="@its:org != ''">&#09;its:org <xsl:value-of select="@its:org"/>>;</xsl:if>
       <xsl:if test="@its:orgRef != ''">&#09;its:orgRef <xsl:value-of select="@its:orgRef"/>>;</xsl:if>
       <xsl:if test="@its:revTool != ''">&#09;its:revTool <xsl:value-of select="@its:revTool"/>>;</xsl:if>
       <xsl:if test="@its:revOrgRef != ''"> &#09;its:revOrgRef <xsl:value-of select="@its:revOrgRef"/>>;</xsl:if>
       <xsl:if test="@its:person != ''">&#09;its:person <xsl:value-of select="concat('http://www.cngl.ie/',@its:person)"/>>;</xsl:if>
       <xsl:if test="@its:revPerson != ''">&#09;its:revPerson <xsl:value-of select="concat('http://www.cngl.ie/',@its:revPerson)"/>>;</xsl:if>
       &#09;its:provRef <xsl:value-of select="concat('http://www.cngl.ie/logger/logs/',$precs)"/>>;.

       <xsl:if test="@its:person != ''"> &lt;<xsl:value-of select="concat('http://www.cngl.ie/',@its:person)"/>> a prov:Agent . </xsl:if>        
       <xsl:if test="@its:revPerson != ''"> &lt;<xsl:value-of select="concat('http://www.cngl.ie/',@its:revPerson)"/>> a prov:Agent . </xsl:if>      
       <xsl:if test="@its:org != ''"> &lt;<xsl:value-of select="@its:org"/>> a prov:Agent . </xsl:if>
       <xsl:if test="@its:orgRef != ''">&lt;<xsl:value-of select="@its:orgRef"/>> a prov:Agent . </xsl:if>
       <xsl:if test="@its:revTool != ''"> &lt;<xsl:value-of select="@its:revTool"/>> a prov:Agent . </xsl:if>
       <xsl:if test="@its:revOrgRef != ''">&lt;<xsl:value-of select="@its:revOrgRef"/>> a prov:Agent . </xsl:if>
       <xsl:if test="@its:tool!= ''"> &lt;<xsl:value-of select="@its:tool"/>> a prov:Agent .  </xsl:if>
       <xsl:if test="@its:toolRef != ''">&lt;<xsl:value-of select="@its:toolRef"/>> a prov:Agent . </xsl:if></xsl:for-each></xsl:for-each>

        <xsl:for-each select="//xliff:glossary/xliff:internal-file/itsx:glossary-entry">&lt;<xsl:value-of select="concat('http://cngl.ie/glossary/',$jobId,'/#',@id)"/>> a gic:Terminology;
        &#09;its:term "<xsl:value-of select="itsx:term"/>";
        &#09;its:translation "<xsl:value-of select="itsx:translation"/>";.</xsl:for-each>
        <xsl:apply-templates select="xliff:header/xliff:phase-group/xliff:phase"><xsl:with-param name="fileUri" select="$fileUri" /></xsl:apply-templates>
                &lt;<xsl:value-of select="$fileUri" />> a prov:Entity;
        &#09;gic:xliffSourceLang lang:<xsl:value-of select="$sourceLang"/>;
        &#09;gic:xliffTargetLang lang:<xsl:value-of select="$targetLang"/>;
        <!--&#09;rdf:XMLLiteral ngldatatypes:<xsl:value-of select="@datatype"/>;-->
        &#09;prov:wasMember &lt;<xsl:value-of select="concat($fileUri, '#collection')"/>>;
       <xsl:if  test="@category"> &#09;gic:domain "<xsl:value-of select="@category" />"</xsl:if>.
       &lt;<xsl:value-of select="$fileUri" />> a gic:InformationResource;.
        <xsl:if test="//xliff:count[@unit='segment']"><xsl:value-of select="$fileUri" />> its:segmentCount <xsl:value-of select="//xliff:count[@unit='segment']"/>;.</xsl:if>
        <xsl:if test="//xliff:count[@unit='word']">&#09;<xsl:value-of select="$fileUri" />> its:wordCount <xsl:value-of select="//xliff:count[@unit='word']" />;.</xsl:if>
        <xsl:if test="//xliff:count[@unit='character']">&#09;<xsl:value-of select="$fileUri" />> its:characterCount <xsl:value-of select="//xliff:count[@unit='character']" />;.</xsl:if>
        <xsl:apply-templates select="xliff:body/xliff:group/xliff:trans-unit"><xsl:with-param name="fileUri" select="$fileUri" /></xsl:apply-templates>
    </xsl:template>

       <xsl:template name="phase" match="xliff:phase"><xsl:param name="fileUri" />
        &lt;<xsl:value-of select="concat($fileUri, '/phases/', position())" />> a prov:Entity;
        &#09;its:phaseName "<xsl:value-of select="@phase-name" />";
        &#09;gic:componentId "<xsl:value-of select="@tool-id" />";
        &#09;its:phaseOrder "<xsl:value-of select="position()" />";
		&#09;prov:startedAtTime "<xsl:value-of select="@date" />".
        
		&lt;<xsl:value-of select="concat($fileUri, '/phases/', position())" />> a gic:ContentProcessingActivity;.
        &lt;<xsl:value-of select="concat($fileUri, '#collection')"  />> a prov:Collection;
        &#09;prov:hadMember &lt;<xsl:value-of select="concat($fileUri, '/phases/', position())" />>.
    </xsl:template>

    <xsl:template name="trans-unit" match="xliff:group/xliff:trans-unit">
        <xsl:param name="fileUri" />
        &lt;<xsl:value-of select="concat($fileUri, '/', @id)" />> a gic:Segment;.
        &lt;<xsl:value-of select="concat($fileUri, '/', @id)" />> a prov:Entity;
        &#09;gic:sourceSegment "<xsl:value-of select="normalize-space(xliff:source)" />";
        &#09;gic:sourceLang lang:<xsl:value-of select="$sourceLang" />;<xsl:for-each select="xliff:source/xliff:mrk">
        &#09;gic:termPointer  &lt;<xsl:value-of select="concat($fileUri, '/glossary/',@dx:term-id)" />>.</xsl:for-each>
        &#09;prov:startedAtTime  "<xsl:value-of select="@dx:modified-at"/>";
        &#09;gic:termOrigin  &lt;<xsl:value-of select="concat($fileUri, '/glossary/',@dx:origin)"/>>;     
        &#09;gic:termOriginShorttext "<xsl:value-of select="@dx:origin-shorttext"/>";<xsl:if test="@dx:repetition != ''">
        &#09;gic:repetition "<xsl:value-of select="@dx:repetition"/>"; </xsl:if>
			<xsl:if test="xliff:target != ''">
         &#09;gic:targetSegment "<xsl:value-of select="normalize-space(xliff:target)" />";
         &#09;gic:targetLang lang:<xsl:value-of select="$targetLang" />;</xsl:if>
         <xsl:if test="xliff:target/@state != ''">
         &#09;gic:state "<xsl:value-of select="xliff:target/@state" />";
         </xsl:if >
        <xsl:if test="xliff:target/@its:provenanceRecordsRef != ''">
        &#09;its:hasProvRecord &lt;<xsl:value-of select="concat('http://www.cngl.ie/logger/logs/',substring-after(xliff:target/@its:provenanceRecordsRef,'#'))"/>>;</xsl:if>.
        <xsl:variable name="ident" select="@id" /><xsl:if test="xliff:target/xliff:mrk!= ''"><xsl:for-each select="(xliff:target/xliff:mrk)">
        <xsl:if test="@its:mtconfidence != ''">&#09;its:mtconfidence "<xsl:value-of select="@its:mtconfidence" />";</xsl:if>
        <xsl:if test="@its:annotatorsRef != ''"><xsl:if test="contains(@its:annotatorsRef, 'text-analysis|')">&#09;its:taAnnotatorsRef "<xsl:value-of select="@its:annotatorsRef"/>";</xsl:if>
        <xsl:if test="contains(@its:annotatorsRef, 'mt-confidence|')">&#09;its:mtConfidenceAnnotatorsRef "<xsl:value-of select="@its:annotatorsRef"/>";</xsl:if>
        <xsl:if test="contains(@its:annotatorsRef, 'terminology|')">&#09;its:termAnnotatorsRef "<xsl:value-of select="@its:annotatorsRef"/>";</xsl:if></xsl:if>
        <xsl:if test="@its:provenanceRecordsRef != ''">&#09;its:hasProvRecord &lt;<xsl:value-of select="concat('http://www.cngl.ie/logger/logs/',substring-after(@its:provenanceRecordsRef,'#'))"/>>;</xsl:if>
        <xsl:if test="xliff:target/@its:provenanceRecordsRef != ''">&#09;its:hasProvRecord &lt;<xsl:value-of select="concat('http://www.cngl.ie/logger/logs/',substring-after(xliff:target/@its:provenanceRecordsRef,'#'))"/>>;</xsl:if>
        &#09;prov:wasDerivedFrom &lt;<xsl:value-of select="concat($fileUri, '/', $ident)" />>.</xsl:for-each>&#10;</xsl:if>&#10;
        
        &lt;<xsl:value-of select="concat($fileUri, '#collection')"  />> a prov:Collection;
        &#09;prov:hadMember &lt;<xsl:value-of select="concat($fileUri, '/', @id)" />>.
        
        <xsl:apply-templates select="xliff:alt-trans">
            <xsl:with-param name="transUnitUri" select="concat($fileUri, '/', @id)" />
        </xsl:apply-templates>
        <xsl:apply-templates select="xliff:seg-source">
            <xsl:with-param name="transUnitUri" select="concat($fileUri, '/', @id)" />
            <xsl:with-param name="mid" select="xliff:seg-source/xliff:mrk/@mid"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="alt-trans" match="xliff:alt-trans"><xsl:param name="transUnitUri" />
        &lt;<xsl:value-of select="concat($transUnitUri, '/alt-trans/', position())" />> a gic:Translation;.
       &lt;<xsl:value-of select="concat($transUnitUri, '/alt-trans/', position())" />> a prov:Entity;
        &#09;gic:sourceSegment "<xsl:value-of select="normalize-space(xliff:source)" />";
        &#09;gic:sourceLang lang:<xsl:value-of select="$sourceLang" />;
        &#09;gic:targetSegment "<xsl:value-of select="normalize-space(xliff:target)" />";
        &#09;gic:targetLang lang:<xsl:value-of select="$targetLang" />;
        &#09;gic:gloriousMatch "<xsl:value-of select="@dx:glorious-match" />";
	&#09;gic:termPenalty "<xsl:value-of select="@dx:match-penalty" />";
	&#09;prov:startedAtTime "<xsl:value-of select="@dx:modified-at" />";
	&#09;prov:generatedBy &lt;<xsl:value-of select="concat('http://cngl.ie/glossary/',@dx:modified-by)" />>;
	&#09;gic:termOriginShorttext "<xsl:value-of select="@dx:origin-shorttext" />";
	&#09;gic:termConfidence "<xsl:value-of select="@match-quality " />";
	&#09;gic:termOrigin lang:<xsl:value-of select="@dx:origin" />;
        <xsl:if test="xliff:target/@its:annotatorsRef != ''"><xsl:if test="contains(xliff:target/@its:annotatorsRef, 'text-analysis|')">
        &#09;its:taAnnotatorsRef "<xsl:value-of select="xliff:target/@its:annotatorsRef"/>";</xsl:if>
        <xsl:if test="contains(xliff:target/@its:annotatorsRef, 'mt-confidence|')">
        its:mtConfidenceAnnotatorsRef "<xsl:value-of select="xliff:target/@its:annotatorsRef"/>";</xsl:if>
        <xsl:if test="contains(xliff:target/@its:annotatorsRef, 'terminology|')">
        &#09;its:termAnnotatorsRef "<xsl:value-of select="xliff:target/@its:annotatorsRef"/>";</xsl:if></xsl:if>
        <xsl:if test="@its:annotatorsRef != ''"><xsl:if test="contains(@its:annotatorsRef, 'text-analysis|')"></xsl:if>
        <xsl:if test="contains(@its:annotatorsRef, 'mt-confidence|')">&#09;its:mtConfidenceAnnotatorsRef "<xsl:value-of select="@its:annotatorsRef"/>";</xsl:if>
        <xsl:if test="contains(@its:annotatorsRef, 'terminology|')">&#09;its:termAnnotatorsRef "<xsl:value-of select="@its:annotatorsRef"/>";</xsl:if></xsl:if>
        <xsl:if test="@origin != ''">&#09;gic:origin "<xsl:value-of select="@origin" />";</xsl:if>
        <xsl:if test="@mid != ''">&#09;its:mid "<xsl:value-of select="@mid" />";</xsl:if>
        <xsl:if test="@match-quality != ''">
	&#09;its:mtconfidence "<xsl:value-of select="@match-quality" />";</xsl:if>
        <xsl:if test="xliff:target/@its:mtconfidence != ''">&#09;its:mtconfidence "<xsl:value-of select="xliff:target/@its:mtconfidence" />";</xsl:if>
        <xsl:if test="xliff:target/@its:provenanceRecordsRef != ''">&#09;its:hasProvRecord <xsl:value-of select="concat('http://www.cngl.ie/logger/logs/',substring-after(xliff:target/@its:provenanceRecordsRef,'#'))"/>>;</xsl:if>
        <xsl:if test="@its:provenanceRecordsRef != ''">&#09;its:hasProvRecord &lt;<xsl:value-of select="concat('http://www.cngl.ie/logger/logs/',substring-after(@its:provenanceRecordsRef,'#'))"/>>;</xsl:if>.
        
    </xsl:template>

    <xsl:template name="seg-source" match="xliff:seg-source"><xsl:param name="transUnitUri" /><xsl:param name="mid" />
        <xsl:for-each select="xliff:mrk"><xsl:if test="@mid != ''"> <xsl:value-of select="concat($transUnitUri, '/', 'seg-source/',@mid)" />> a its:seg-source;<!--MAIN SEG MRK-->
        &#09;its:mrktext "<xsl:value-of select="normalize-space(.)"/>";
        &#09;its:mtype "<xsl:value-of select="@mtype" />";
        <xsl:if test="@its:termInfoRef != ''">&#09;its:termInfoRef <xsl:value-of select="concat('http://cngl.ie/glossary/',@its:termInfoRef)"/>>;</xsl:if>
        <xsl:if test="@its:taConfidence != ''">&#09;its:taConfidence "<xsl:value-of select="@its:taConfidence"/>";</xsl:if>
        <xsl:if test="@its:taClassRef != ''">&#09;its:taClassRef <xsl:value-of select="@its:taClassRef"/>>;</xsl:if>
        <xsl:if test="@its:taIdentRef!= ''">&#09;its:taIdentRef <xsl:value-of select="@its:taIdentRef"/>>;</xsl:if>
        <xsl:if test="@its:provenanceRecordsRef!= ''">&#09;its:hasProvRecord "<xsl:value-of select="@its:provenanceRecordsRef" />";</xsl:if>
        <xsl:if test="@its:provenanceRecordsRef!= ''">&#09;prov:hadDerivation <xsl:value-of select="concat('http://www.cngl.ie/logger/logs/',$jobId,'/',@its:provenanceRecordsRef)"/>>;</xsl:if>
        &#09;prov:wasDerivedFrom <xsl:value-of select="$transUnitUri"/>>;.</xsl:if>
        <xsl:variable name="x" select="@mid" />
        <xsl:for-each select="xliff:mrk"><!--MRK within main seg MRK--><xsl:value-of select="concat($transUnitUri, '/', 'seg-source/',$x,'/',position())" />> a its:mrk;
        &#09;its:mrktext "<xsl:value-of select="normalize-space(.)" />";
        &#09;its:mtype "<xsl:value-of select="@mtype" />";
        <xsl:if test="@its:termInfoRef != ''">&#09;its:termInfoRef <xsl:value-of select="concat('http://cngl.ie/glossary/',@its:termInfoRef)"/>>;</xsl:if>
        <xsl:if test="@its:taConfidence != ''">&#09;its:taConfidence "<xsl:value-of select="@its:taConfidence"/>";</xsl:if>
        <xsl:if test="@its:taClassRef != ''">&#09;its:taClassRef <xsl:value-of select="@its:taClassRef"/>>;</xsl:if>
        <xsl:if test="@its:taIdentRef!= ''">&#09;its:taIdentRef <xsl:value-of select="@its:taIdentRef"/>>;</xsl:if>
        <xsl:for-each select="xliff:mrk"><!--MRK within MRK-->&#09;&#09;its:mrktext "<xsl:value-of select="normalize-space(.)" />";
        &#09;its:mtype "<xsl:value-of select="@mtype"/>";
        <xsl:if test="@its:termInfoRef != ''">&#09;its:termInfoRef <xsl:value-of select="concat('http://cngl.ie/glossary/',@its:termInfoRef)"/>>;</xsl:if>
        <xsl:if test="@its:taConfidence != ''">&#09;its:taConfidence "<xsl:value-of select="@its:taConfidence"/>";</xsl:if>
        <xsl:if test="@its:taClassRef != ''">&#09;its:taClassRef <xsl:value-of select="@its:taClassRef"/>>;</xsl:if>
        <xsl:if test="@its:taIdentRef!= ''">&#09;its:taIdentRef <xsl:value-of select="@its:taIdentRef"/>>;</xsl:if></xsl:for-each> &#09;prov:wasDerivedFrom <xsl:value-of select="$transUnitUri"/>>;.</xsl:for-each></xsl:for-each>
    </xsl:template>
</xsl:stylesheet>