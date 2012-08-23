<%@ page import="com.thoughtworks.xstream.XStream" %>
<%--

in: rmNode (Cluster)
in: archetype

Seudocodigo: (es analogo para todos los objetos RM que tengan un atributo multiple como
              ITEM_TREE, ITEM_LIST, ITEM_TABLE, HISTORY, SECTION y COMPOSITION)

modo = edit?
{
    cc = nodo que restringe al cluster en el AOM
    para cada ci en cc.attributes del AOM,
      verificar que existe un item en cluster.items en el RM
      No hay item en el RM para ese item del AOM?
        Utilizo en AOM para generar los campos de edicion.
      Hay item en el RM para ese AOM?
        Utilizo el item RM para generar los campos de edicion.
}
modo = show? (si el modo no es edit es show)
{
    Utilizo el cluster y sus items en el RM para generar los campos de show,
    // si cae en este caso es que la estructura RM esta completa y no hubo
    // errores de validacion, por eso puedo usar solo el RM para generar la vista.
}

--%>
<div class="CLUSTER">
  <%--
  arhcID: ${rmNode.archetypeDetails.archetypeId},
  nodeID: ${rmNode.archetypeNodeId},
  id: ${rmNode.id}<br/><br/>
  --%>
  <%-- show --%>
    <span class="label">
      ${rmNode.name.value}
    </span>
    <span class="content">
      <g:each in="${rmNode.items}" var="item">
        <%-- element o cluster --%>
        <%-- esto tira Item_$$_javassist_165, no se porque, asi que lo hago con instanceof --%>
        <%-- <g:set var="templateName" value="${item.getClass().getSimpleName()}" /> --%>
        <g:set var="templateName" value="${item.getClassName()}" />
        <%-- Item: ${templateName}<br/> --%>
        <g:render template="../guiGen/showTemplates/${templateName}"
                  model="[rmNode: item, archetype: archetype, template: template, pathFromParent: item.path]" />
      </g:each>
    </span>
</div>