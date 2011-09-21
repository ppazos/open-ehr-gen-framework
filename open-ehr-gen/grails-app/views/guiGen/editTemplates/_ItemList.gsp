<%--

in: rmNode (ItemList)

--%>
<%--
Idem ItemTree, pero aca intento hacer todo con tags,
estaria bueno luego hacer la comparacion para ver los tiempos...
--%>
<%-- edit --%>
  <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
  <g:each in="${aomNode.attributes[0].children}" var="children">
    <g:set var="rmItems" value="${rmNode.items.findAll{it.path == children.path()}}" />
    <g:if test="${rmItems.size()==0}">
      <g:render template="../guiGen/templates2/cObject"
                model="[cObject: children, archetype: archetype]" />
    </g:if>
    <g:else>
      <g:each in="${rmItems}" var="item">
        <%-- DEBERIAN SER TODOS ELEMENTS! --%>
        <g:set var="templateName" value="${item.getClassName()}" />
        <g:render template="../guiGen/editTemplates/${templateName}"
                  model="[rmNode:item, archetype:archetype, template: template]" />
      </g:each>
    </g:else>
  </g:each>