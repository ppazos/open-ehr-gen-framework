<%@ page import="org.openehr.am.archetype.constraintmodel.*" %>
<%--
in: rmNode (Element)
in: pathFromParent (String)
in: template
in: archetype
--%>
<g:hasErrors bean="${rmNode}">
  <div class="error">
    <g:renderErrors bean="${rmNode}" as="list" />
  </div>
</g:hasErrors>

<%-- errores del value del element --%></%-->
<g:hasErrors bean="${rmNode.value}">
  <div class="error">
    <g:renderErrors bean="${rmNode.value}" as="list" />
  </div>
</g:hasErrors>

<%-- para show siempre viene rmNode --%>

  <%-- Puede ser internal ref --%>
  <%-- Esto es valido solo si viene rmNode --%>
  <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
  <g:if test="${aomNode instanceof CComplexObject}">
    <g:set var="elementValueRmType" value=" ELEMENT_${aomNode?.attributes[0].children[0].rmTypeName}" />
  </g:if>
  <div class="ELEMENT${elementValueRmType}">
    <g:set var="isInternalRef" value="${false}" />
    <g:if test="${aomNode instanceof ArchetypeInternalRef}">
      <g:set var="isInternalRef" value="${true}" />
      <g:set var="aomChildNode" value="${archetype.node( aomNode.targetPath+'/value' )}" /><%-- aomNode ahora es el nodo referenciado --%>
    </g:if>
    <g:else>
      <%-- Si no es arch_internal_ref voy a buscar un nivel mas el aomNode para mandarselo al template del element.value --%>
      <g:set var="aomChildNode" value="${archetype.node( pathFromParent+'/value' )}" />
    </g:else>
    <span class="label">
      ${rmNode.name.value}
    </span>
    <span class="content">
      <%--
        ELEMENT REF PATH: ${((isInternalRef) ? "internal:"+aomNode.path() : 'no internal ref')}<br/>
        TemplateName: ${templateName}<br/>
      --%>
      <%-- TODO: ver si no es mas facil si le pongo path a los primitives --%>
      <%-- raro, si no empieza en minuscula no ve la taglib, se ve que no le gusta DvBoolean y le gusta aDvBoolean --%>
      <%-- mode es edit o show, aca es show --%>
      <g:set var="templateName" value="show${rmNode.value.getClassName()}" />
      <% println g."$templateName"( dataValue: rmNode.value,
	                      parent: rmNode,
	                      archetype: archetype,
	                      refPath: ((isInternalRef) ? aomNode.path() : ''),
	                      aomNode: aomChildNode,
	                      pathFromOwner: rmNode.path+'/value',
	                      template: template ) %>
    </span>
  </div>