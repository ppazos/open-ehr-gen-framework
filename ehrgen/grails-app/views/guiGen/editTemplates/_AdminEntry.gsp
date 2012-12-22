<%@ page import="com.thoughtworks.xstream.XStream" %>
<%@ page import="archetype.ArchetypeManager" %>
<%--

in: rmNode (AdminEntry)

--%>
<g:set var="archetype" value="${ArchetypeManager.getInstance().getArchetype( rmNode.archetypeDetails.archetypeId )}" />
<div class="ADMIN_ENTRY">
  <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
  <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(session.locale.language, aomNode.nodeID)}" />
  <span class="label">
    ${archetypeTerm?.text}:
  </span>
  <span class="content">
    <%-- edit --%>
    <g:if test="${!rmNode.data}"><%-- Si no hay estructura RM para mostrar, voy por el AOM --%>
        <%-- Si pido /description que es correcto, no me da el nodo! --%>
        <g:set var="aomNode" value="${archetype.node( rmNode.path )}" />
        <%-- sacado de _cComplexObject --%>
        <g:render template="../guiGen/templates2/cAttribute"
                  var="cAttribute"
                  collection="${aomNode.attributes}"
                  model="[archetype: archetype, refPath: '', params: params]" />
    </g:if>
    <g:else><%-- Hay estructura RM para mostrar, no voy al AOM --%>
       <g:set var="templateName" value="${rmNode.data.getClassName()}" />
       <g:render template="../guiGen/editTemplates/${templateName}"
                 model="[rmNode: rmNode.data, archetype: archetype, template: template]" />
    </g:else>
  </span>
</div>