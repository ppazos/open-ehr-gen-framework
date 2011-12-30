<%@ page import="org.openehr.am.archetype.constraintmodel.*" %><%@ page import="util.FieldNames" %>

<g:set var="fields" value="${FieldNames.getInstance()}" />

<%--
in: cPrimitiveObject (${cPrimitiveObject.class}) (${cPrimitiveObject.rmTypeName})<br/>

<b>${cPrimitiveObject.path()}</b><br/>
--%>

<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>

<g:if test="${cPrimitiveObject.rmTypeName == 'DvBoolean'}">
  <!-- TODO: meter el valor -->
  <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}"></label>
</g:if>

<g:if test="${cPrimitiveObject.rmTypeName == 'Integer'}">

  <%-- SI el RMTYPE es Integer, el primitiveobject.item es CInteger. --%>
  <g:if test="${cPrimitiveObject.item.list != null}">
    <%-- TODO --%>
  </g:if>
  
  <%-- FIXME: hacer un mostrador general de Interval con un template --%>
  <g:if test="${cPrimitiveObject.item.interval != null}">
(
    <g:if test="${cPrimitiveObject.item.interval.lower != null}">${cPrimitiveObject.item.interval.lower}</g:if>
    <g:else>*</g:else>
..
    <g:if test="${cPrimitiveObject.item.interval.upper != null}">${cPrimitiveObject.item.interval.upper}</g:if>
    <g:else>*</g:else>
)
  </g:if>
  ${cPrimitiveObject.item.assumedValue}
  <!-- TODO: meter el valor -->
  <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}"></label>
</g:if>

<g:if test="${cPrimitiveObject.rmTypeName == 'DvDate'}">
  <!-- TODO: meter el valor -->
  <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}"></label>
</g:if>

<g:if test="${cPrimitiveObject.rmTypeName == 'DvDateTime'}">
  <!-- TODO: meter el valor -->
  <label class="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}"></label>
</g:if>