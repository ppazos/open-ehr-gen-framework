<%@ page import="hce.core.datastructure.itemstructure.representation.*" %>
<%--
in: rmNode (ItemTree)
in: template
in: archetype
--%>
<%-- show --%>
<g:each in="${rmNode.getItems()}" var="item"><%-- element o cluster --%>
  <%--
    esto tira Item_$$_javassist_165, no se porque, asi que lo hago con instanceof
    <g:set var="templateName" value="${item.getClass().getSimpleName()}" />
  --%>
  <g:set var="templateName" value="${item.getClassName()}" />
  <g:render template="../guiGen/showTemplates/${templateName}"
            model="[rmNode: item, pathFromParent: rmNode.path+'/items', archetype: archetype, template: template]" />
</g:each>