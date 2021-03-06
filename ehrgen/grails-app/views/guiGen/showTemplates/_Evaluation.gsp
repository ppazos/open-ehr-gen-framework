<%@ page import="archetype.ArchetypeManager" %><%@ page import="templates.TemplateManager" %>
<%--
in: rmNode (Evaluation)
--%><!-- Evaluation show -->
<g:if test="${!template}">
  <g:set var="template" value="${TemplateManager.getInstance().getTemplate( rmNode.archetypeDetails.templateId )}" />
</g:if>
<g:set var="archetype" value="${ArchetypeManager.getInstance().getArchetype( rmNode.archetypeDetails.archetypeId )}" />

<%-- http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=19 --%>
<g:set var="generarGUI" value="${fieldPaths.find {rmNode.path.startsWith(it)} != null}" />
<g:if test="${generarGUI}">
  <div class="EVALUATION">
  <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
  <%-- // FIXME: deberia escalar en locale como ArchetypeTagLib.findTerm --%>
  <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(session.locale.language, aomNode.nodeID)}" />
  <span class="label">
    ${archetypeTerm?.text}:
  </span>
  <span class="content">
</g:if>

<%-- evaluation.data es itemStructure, llamo directamente al template del itemStructure especifico. --%>
<g:set var="templateName" value="${rmNode.data.getClassName()}" />
<g:render template="../guiGen/showTemplates/${templateName}"
          model="[rmNode: rmNode.data, archetype: archetype, template: template]" />

<g:if test="${generarGUI}">
    </span>
  </div>
</g:if>