<%--

in: rmNode (Activity)

Activity<br/>

--%>
<%-- edit --%>
  <g:if test="${!rmNode.description}"><%-- Si no hay estructura RM para mostrar, voy por el AOM --%>
    <%-- sacado de _cComplexObject --%>
    <g:render template="../guiGen/templates2/cAttribute"
        var="cAttribute"
        collection="${aomNode.attributes}"
        model="[archetype: archetype, refPath: '', params: params]" />
  </g:if>
  <g:else><%-- Hay estructura RM para mostrar, no voy al AOM --%>
    <g:set var="templateName" value="${rmNode.description.getClassName()}" />
    <g:render template="../guiGen/editTemplates/${templateName}"
              model="[rmNode: rmNode.description, archetype: archetype, template: template]" />
  </g:else>