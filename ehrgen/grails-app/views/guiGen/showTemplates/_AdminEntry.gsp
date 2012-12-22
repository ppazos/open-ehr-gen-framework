<%@ page import="archetype.ArchetypeManager" %>
<%--

in: rmNode (AdminEntry)

--%>
<g:set var="archetype" value="${ArchetypeManager.getInstance().getArchetype( rmNode.archetypeDetails.archetypeId )}" />

<div class="ADMIN_ENTRY">
  <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
  <%-- // FIXME: deberia escalar en locale como ArchetypeTagLib.findTerm --%>
  <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(session.locale.language, aomNode.nodeID)}" />
  <span class="label">
    ${archetypeTerm?.text}:
  </span>
  <span class="content">
    <%-- show --%>
      <%-- itemStructure especifico --%>
      <g:set var="templateName" value="${rmNode.data.getClassName()}" />
      <g:render template="../guiGen/showTemplates/${templateName}"
                model="[rmNode: rmNode.data, archetype: archetype, template: template]" />
  </span>
</div>