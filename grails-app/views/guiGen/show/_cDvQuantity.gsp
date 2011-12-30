<%@ page import="org.openehr.am.archetype.constraintmodel.*" %>
<%@ page import="tablasMaestras.OpenEHRConcept" %><%@ page import="util.FieldNames" %>

<g:set var="fields" value="${FieldNames.getInstance()}" />

<%--
<b>${cDvQuantity.path()}</b>
--%>

<%-- TODO: ponerlo en una taglib --%>
<%-- no muestro la propiedad...
<g:if test="${cDvQuantity.property}">
  <%
    def concepts = OpenEHRConcept.withCriteria {
      eq('conceptId', cDvQuantity.property.codeString)
      eq('lang', lang)
    }
    if (concepts.size()==1)
      print concepts[0].rubric
    else
      print 'TODO: pedirselo al servicio de terminologia "' + cDvQuantity.property + '"'
  %>
</g:if>
--%>

<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>

<%-- FIXME: esto funciona para un par unidad-rango, si hubieran varias
            unidades el rango que se muestra deberia variar en funcion
            de la unidad seleccionada. --%>
            
<%-- FIXME: el rango para la magnitud depende de la unidad seleccionada, 
            y deberia cambiar al cambiar la unidad (si hay mas de una 
            unidad para seleccionar). --%>
            
<g:set var="interval" value="${null}" />
<g:each in="${cDvQuantity.list}" var="item">

  <%-- FIXME: ojo que el intervalo depende de la unidad que se seleccione, me gustaria 
              que al seleccionar una unidad u otra, mediante JS muestre el rango correspondiente, 
              si es que existe. --%>
  <g:if test="${item.magnitude != null}">
    <g:set var="interval" value="${item.magnitude}" />
  </g:if>
  <%-- units: ${item.units} --%>
</g:each>

<%-- muestra (min..max) para la magnitude, si hay restriccion --%>
<g:set var="lower" value="*" />
<g:set var="upper" value="*" />
<g:if test="${interval != null}">
  <g:if test="${interval.lower != null}">
    <g:set var="lower" value="${interval.lower}" />
  </g:if>
  <g:if test="${interval.upper != null}">
    <g:set var="upper" value="${interval.upper}" />
  </g:if>
</g:if>

(${lower}..${upper})
<!-- El valor se pone por javascript. http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=59 -->
<label class="${fields.getField(archetype.archetypeId.value +_refPath+ cDvQuantity.path()+'/magnitude')}" />
<label class="${fields.getField(archetype.archetypeId.value +_refPath+ cDvQuantity.path()+'/units')}" />