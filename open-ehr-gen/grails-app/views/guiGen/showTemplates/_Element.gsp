<%@ page import="org.openehr.am.archetype.constraintmodel.*" %>

<%--

in: rmNode (Element)

--%>


<%-- Puede ser internal ref --%>
<g:set var="aomNode" value="${archetype.node(rmNode.path)}" />

<div class="ELEMENT">

<%--
  arhcID: ${rmNode.archetypeDetails.archetypeId},
  nodeID: ${rmNode.archetypeNodeId},
  id: ${rmNode.id}<br/><br/>
  ${rmNode.path}<br/>
--%>

  <%
    def isInternalRef = false
  %>
  <g:if test="${aomNode instanceof ArchetypeInternalRef}">
    <%-- ---- ArchInternalRef ----<br/> --%>
    <%
      isInternalRef = true
    %>
    <g:set var="aomNode2" value="${archetype.node( aomNode.targetPath+'/value' )}" /><%-- aomNode ahora es el nodo referenciado --%>
  </g:if>
  <g:else>
    <%-- Si no es arch_internal_ref voy a buscar un nivel mas el aomNode para mandarselo al template del element.value --%>
    <g:set var="aomNode" value="${archetype.node( rmNode.path+'/value' )}" />
  </g:else>

  <g:hasErrors bean="${rmNode}">
    <div class="error">
      <g:renderErrors bean="${rmNode}" as="list" />
    </div>
  </g:hasErrors>

  <%--
  ${rmNode.errors}<br/><br/>
  ${rmNode.value.errors}<br/><br/>
  --%>
  
  <span class="label">
    ${rmNode.name.value}
  </span>
  <span class="content">
    <g:set var="templateName" value="${rmNode.value.getClass().getSimpleName()}" />
    <%--<g:set var="templateName" value="${rmNode.value.getClassName()}" />--%>
    
    <%--
    EELEMENT REF PATH: ${((isInternalRef) ? "internal:"+aomNode.path() : 'no internal ref')}<br/>
    TemplateName: ${templateName}<br/>
    --%>
    
    <g:render template="../guiGen/showTemplates/${templateName}"
              model="[dataValue: rmNode.value,
              		  archetype: archetype,
              		  refPath: ((isInternalRef) ? aomNode.path() : ''),
              		  aomNode: ((isInternalRef) ? aomNode2 : aomNode),
            	      pathFromOwner: rmNode.path+'/value',
            	      template: template]" /> <%-- TODO: ver si no es mas facil si le pongo path a los primitives --%>
  </span>
</div>
