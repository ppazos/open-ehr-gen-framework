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

<g:if test="${!rmNode}">
  <%-- EDIT, llamo al template de cObject --%>
  <g:set var="aomNode" value="${archetype.node(pathFromParent)}" />
  <g:render template="../guiGen/templates2/cObject"
            model="[cObject: aomNode, archetype: archetype]" />
</g:if>
<g:else>
  <%-- Puede ser internal ref --%>
  <%-- Esto es valido solo si viene rmNode --%>
  <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
  <%--
  Archetype: ${archetype.archetypeId.value}<br/>
  RM Node Path ${rmNode.path}</br>
  --%>
  <g:set var="elementValueRmType" value="ELEMENT_${aomNode?.attributes[0].children[0].rmTypeName}" />
  <div class="ELEMENT ${elementValueRmType}">
  <g:set var="isInternalRef" value="${false}" />
  <g:if test="${aomNode instanceof ArchetypeInternalRef}">
    <g:set var="isInternalRef" value="${true}" />
    <g:set var="aomChildNode" value="${archetype.node( aomNode.targetPath+'/value' )}" /><%-- aomNode ahora es el nodo referenciado --%>
  </g:if>
  <g:else>
    <%-- Si no es arch_internal_ref voy a buscar un nivel mas el aomNode para mandarselo al template del element.value --%>
    <g:set var="aomChildNode" value="${archetype.node( pathFromParent+'/value' )}" />
  </g:else>
  <%--
  pathFromParent: ${pathFromParent}<br/>
  aomChildnode: ${aomChildNode}<br/>
  --%>
  <span class="label">
    ${rmNode.name.value}
  </span>
  <span class="content">
    <g:set var="templateName" value="${rmNode.value.getClassName()}" />
    <%--
      ELEMENT REF PATH: ${((isInternalRef) ? "internal:"+aomNode.path() : 'no internal ref')}<br/>
      TemplateName: ${templateName}<br/>
    --%>
    <%-- TODO: ver si no es mas facil si le pongo path a los primitives --%>
    <g:render template="../guiGen/showTemplates/${templateName}"
              model="[dataValue: rmNode.value,
                      parent: rmNode,
                      archetype: archetype,
                      refPath: ((isInternalRef) ? aomNode.path() : ''),
                      aomNode: aomChildNode,
                      pathFromOwner: rmNode.path+'/value',
                      template: template]" />
    </span>
  </div>
</g:else>