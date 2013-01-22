<%@ page import="org.openehr.am.archetype.constraintmodel.*" %>
<%@ page import="util.FieldNames" %>

<g:set var="fields" value="${FieldNames.getInstance()}" />

<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>
<%-- PRIMITIVE REFPATH: ${_refPath}<br/>
Primitive: ${cPrimitiveObject.rmTypeName}<br/><br/>
--%>

<g:if test="${cPrimitiveObject.rmTypeName == 'DvBoolean'}">
  <%-- Si assumed value es null o false deschequea, si es true chequea. --%>
  <%-- Modelarlo como radio me genera un problema cuando hay mas de uno porquen
       no me deja seleccionar mas de uno a la vez, porque todos los radios tienen
       el mismo name.
  <label>
    <span><g:message code="label.boolean.true" locale="${new Locale(lang)}" /></span>
    <g:radio name="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}"
             value="label.boolean.true"
             checked="${cPrimitiveObject.item.hasAssumedValue() && cPrimitiveObject.item.assumedValue}" />
  </label>
  <br/>
  <label>
    <span><g:message code="label.boolean.false" locale="${new Locale(lang)}" /></span>
    <g:radio name="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}"
             value="label.boolean.false"
             checked="${cPrimitiveObject.item.hasAssumedValue() && !cPrimitiveObject.item.assumedValue}" />
  </label>
  --%>
  <%-- modelado como select --%>
  <select name="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}">
    <option value=""></option>
    <option value="label.boolean.true" ${((cPrimitiveObject.item.hasAssumedValue() && cPrimitiveObject.item.assumedValue || params[archetype.archetypeId.value +_refPath+ cPrimitiveObject.path()]=="label.boolean.true")?'selected="true"':'')}>
      <g:message code="label.boolean.true" locale="${new Locale(lang)}" />
    </option>
    <option value="label.boolean.false" ${((cPrimitiveObject.item.hasAssumedValue() && !cPrimitiveObject.item.assumedValue || params[archetype.archetypeId.value +_refPath+ cPrimitiveObject.path()]=="label.boolean.false")?'selected="true"':'')}>
      <g:message code="label.boolean.false" locale="${new Locale(lang)}" />
    </option>
  </select>
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
  <input type="text" name="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}" />
</g:if>

<g:if test="${cPrimitiveObject.rmTypeName == 'DvDate'}">
  <%-- TODO: considerar estos tipos de restricciones
  ${cPrimitiveObject.item.interval}
  ${cPrimitiveObject.item.list}
  --%>
  <%-- FIXME: ajustar patron para mostrar bien al ingreso de la fecha en espanol --%>
  <%-- como el control de entrada no es un input no necesito mostrar el formato de como se deberia ingresar --%>
  <%--(${cPrimitiveObject.item.pattern})--%>
  <g:datePicker name="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}" value="${new Date()}" precision="day" />
</g:if>

<g:if test="${cPrimitiveObject.rmTypeName == 'DvDateTime'}">
  <%-- TODO: considerar estos tipos de restricciones
  ${cPrimitiveObject.item.interval}
  ${cPrimitiveObject.item.list}
  --%>
  <%-- FIXME: ajustar patron para mostrar bien al ingreso de la fecha en espanol --%>
  <%-- como el control de entrada no es un input no necesito mostrar el formato de como se deberia ingresar --%>
  <%-- (${cPrimitiveObject.item.pattern}) --%>
  <g:datePicker name="${fields.getField(archetype.archetypeId.value +_refPath+ cPrimitiveObject.path())}" value="${new Date()}" precision="minute" />
</g:if>