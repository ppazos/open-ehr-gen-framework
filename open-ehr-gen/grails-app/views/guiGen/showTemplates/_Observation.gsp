<%@ page import="templates.TemplateManager" %>
<%@ page import="archetype_repository.ArchetypeManager" %>

<%--

in: rmNode (Observation)
in: template que define toda la estructura que cuelga de este rmNode.

--%>

<!-- Observation -->

<g:if test="${!template}">
  <g:set var="template"
         value="${TemplateManager.getInstance().getTemplate( rmNode.archetypeDetails.templateId )}" />
</g:if>

<%-- puede ser el mismo que me pasan como parametro, ahora es al dope que me lo pasen silo calculo de nuevo aqui. --%>
<g:set var="archetype"
       value="${ArchetypeManager.getInstance().getArchetype( rmNode.archetypeDetails.archetypeId )}" />



<div class="OBSERVATION">
  <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
  
  <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(session.locale.language, aomNode.nodeID)}" />
  
  <span class="label">
    ${archetypeTerm?.text}:
  </span>
  <span class="content">
    <g:render template="../guiGen/showTemplates/History"
              model="[rmNode: rmNode.data, archetype: archetype, template: template]" />
  </span>
</div>
