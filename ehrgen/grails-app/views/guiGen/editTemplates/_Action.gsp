<%@ page import="com.thoughtworks.xstream.XStream" %>
<%@ page import="archetype.ArchetypeManager" %>
<%@ page import="templates.TemplateManager" %>
<%--

in: rmNode (Action)

--%>
<g:if test="${!template}">
  <g:set var="template" value="${TemplateManager.getInstance().getTemplate( rmNode.archetypeDetails.templateId )}" />
</g:if>
<g:set var="archetype" value="${ArchetypeManager.getInstance().getArchetype( rmNode.archetypeDetails.archetypeId )}" />
<g:set var="aomNode" value="${archetype.node(rmNode.path)}" />

<%-- http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=19 --%>
<g:set var="generarGUI" value="${fieldPaths.find{ rmNode.path.startsWith(it)} != null}" />
<g:if test="${generarGUI}">
  <div class="ACTION">
    <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(session.locale.language, aomNode.nodeID)}" />
    <span class="label">
      ${archetypeTerm?.text}:
    </span>
    <span class="content">
</g:if>

<%-- edit --%>

  <g:if test="${!rmNode.description}"><%-- Si no hay estructura RM para mostrar, voy por el AOM --%>
    <%-- sacado de _cComplexObject --%>
    <g:render template="../guiGen/templates2/cAttribute"
              var="cAttribute"
              collection="${aomNode.attributes}"
              model="[archetype:archetype, refPath:'', params:params]" />
  </g:if>
  <g:else><%-- Hay estructura RM para mostrar, no voy al AOM --%>
    <%-- usa el template del itemstructure concreto (single, list, tree o table) --%>
    <g:set var="templateName" value="${rmNode.description.getClassName()}" />
    <g:render template="../guiGen/editTemplates/${templateName}"
              model="[rmNode: rmNode.description, archetype: archetype, template: template]" />
  </g:else>

<g:if test="${generarGUI}">
    </span>
  </div>
</g:if>