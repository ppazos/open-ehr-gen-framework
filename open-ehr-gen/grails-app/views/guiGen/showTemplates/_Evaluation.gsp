<%@ page import="archetype_repository.ArchetypeManager" %>
<%@ page import="templates.TemplateManager" %>
<%--

in: rmNode (Evaluation)

--%>

<!-- Evaluation -->

<g:if test="${!template}">
  <g:set var="template"
         value="${TemplateManager.getInstance().getTemplate( rmNode.archetypeDetails.templateId )}" />
</g:if>

<g:set var="archetype"
       value="${ArchetypeManager.getInstance().getArchetype( rmNode.archetypeDetails.archetypeId )}" />

<div class="EVALUATION">
  <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
  
  <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(session.locale.language, aomNode.nodeID)}" />
  
  <span class="label">
    ${archetypeTerm?.text}:
  </span>
  <span class="content">
    <g:render template="../guiGen/showTemplates/ItemStructure"
              model="[rmNode: rmNode.data, archetype: archetype, template: template]" />
  </span>
</div>