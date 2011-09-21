<%@ page import="hce.core.datastructure.itemstructure.representation.*" %><%@ page import="com.thoughtworks.xstream.XStream" %>
<%--
in: rmNode (ItemTree)
in: template
in: archetype
--%>
<%--
  Tengo que ver si para cada item del ItemTree, hay un nodo en el RM.
  Si el RM está, muestro ese RM,
  Si no está, genero los campos usando AOM.
--%>
<%-- edit --%>
<g:set var="aomNode" value="${archetype.node(rmNode.path)}" />

  <%--
  // deberia dar un CComplexObject con rmTypeName ITEM_TREE,
  // y deberia tener un solo attribute multiple para su atributo 'items'.
  // Los hijos del CAttribute items son los que recorro para ver si estan en le RM.
  def aomNode = archetype.node(rmNode.path)

  // java.util.Collections$UnmodifiableRandomAccessList 
  // println "CCO Attributes: "+aomNode.attributes.getClass() 
  --%>
  <g:each in="${aomNode.attributes[0].children}" var="children">
    <%
    //println "Children: "+children.getClass() + "<br/>"
    def rmItems = rmNode.items.findAll{ it.path == children.path() }
    if (rmItems.size()==0) // No hay items RM para esa path, genero usando el AOM
    {
      //println "No hay items RM para esa path, genero usando el AOM"
      //print "ItemTree AOM"
      print render(template: "../guiGen/templates2/cObject",
                   model: [cObject: children, archetype: archetype, template: template])
    }
    else // Hay items RM para ese AOM, genero usando el RM
    {
      //println "Hay items RM para ese AOM, genero usando el RM"
      //println "ItemTree RM<br/>"
      rmItems.each { item ->
        def templateName = item.getClassName()
          //print "templateName:"+templateName+"<br/>"
          //print '<textarea style="width: 700px; height: 200px;">' + new XStream().toXML(item) + '</textarea><br/>'
          //print '<textarea style="width: 700px; height: 200px;">' + new XStream().toXML(archetype.node(item.path)) + '</textarea><br/>'
        print render(template: "../guiGen/editTemplates/${templateName}",
                     model: [rmNode:item, archetype: archetype, template: template, pathFromParent: item.path])
      }
    }
    %>
  </g:each>