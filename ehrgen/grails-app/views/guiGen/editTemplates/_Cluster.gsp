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
  <%-- edit --%>
    <span class="label">
      ${rmNode.name.value}
    </span>
    <span class="content">
	    <g:set var="aomNode" value="${archetype.node(rmNode.path)}" />
	    <%--
	    FIXME: solo con esto podria mostrar el lugar para editar, el problema es que no 
	    muestra valores ingresados ni errores en los nodos del RM creados en el bindeo,
	    pero la logica de GUI ya esta implementada.
	    <g:render template="../guiGen/templates2/cObject"
	              model="[cObject: aomNode,
	                      archetype: archetype,
	                      params: params]" />
	    --%>
	    <%--
	    Tengo que ver si para cara item del cluster definido por el AOM
	    tengo un valor en el RM, si no tengo, para ese nodo uso el AOM
	    para generar. Si no uso el RM con sus valores. Similar a ItemTree.
	    --%>
	    <%--
	    Recorro los CObject de attributes[0].
	    Attributes[0] es la restriccion sobre el atributo cluster.items
	    --%>
	    <g:each in="${aomNode.attributes[0].children}" var="children">
	      <%
	      //println "Children: "+children.getClass() + "<br/>"
	      // El cluster del RM tiene algun item con la path del AOM?
	      def rmItems = rmNode.items.findAll{ it.path == children.path() }
	      if (rmItems.size()==0) // No hay items RM para esa path, genero usando el AOM
	      {
	        // USO AOM
	        //print "_Cluster usa AOM"
	        print render(template:"../guiGen/templates2/cObject",
	                     model:[cObject: children,
	                            archetype: archetype,
	                            template: template,
	                            pathFromParent: children.path()])
	      }
	      else
	      {
	        // USO RM
	        //print "_Cluster usa RM<br/>"
	        rmItems.each { item ->
	          //print item.path + "<br/>"
	          def templateName = item.getClassName()
	          //print '_Cluster '+ templateName + "<br/>"
	          print render(template: "../guiGen/editTemplates/${templateName}",
	                       model: [rmNode: item,
	                               archetype: archetype,
	                               template: template,
	                               pathFromParent: item.path])
	        }
	      }
	      %>
	    </g:each>
    </span>
</div>