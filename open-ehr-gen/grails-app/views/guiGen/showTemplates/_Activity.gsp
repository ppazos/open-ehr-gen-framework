<%--

in: rmNode (Activity)

Activity<br/>

--%>
<%-- show --%>
  <%-- itemStructure especifico --%>
    <g:set var="templateName" value="${rmNode.description.getClassName()}" />
    <g:render template="../guiGen/showTemplates/${templateName}"
              model="[rmNode: rmNode.description, archetype: archetype, template: template]" />