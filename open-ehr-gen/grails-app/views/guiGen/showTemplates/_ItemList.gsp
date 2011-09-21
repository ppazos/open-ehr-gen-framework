<%--

in: rmNode (ItemList)

--%>
<%-- show --%>
<g:each in="${rmNode.items}" var="element">
  <g:render template="../guiGen/showTemplates/Element"
            model="[rmNode: element, pathFromParent: rmNode.path+'/items', archetype: archetype, template: template]" />
</g:each>