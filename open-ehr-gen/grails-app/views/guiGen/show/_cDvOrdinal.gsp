<%@ page import="org.openehr.am.archetype.constraintmodel.*" %><%@ page import="util.FieldNames" %>

<g:set var="fields" value="${FieldNames.getInstance()}" />
<%--
<b>${cDvOrdinal.path()}</b>
--%>
<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>
<!-- armo lista de valores con textos -->
<g:set var="labels" value="${[]}" />
<g:each in="${cDvOrdinal.list.sort{ it.value }}" var="ordinal">
  <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(lang, ordinal.symbol.codeString)}" />
    <g:if test="${!archetypeTerm}">
    El termino con codigo [${ordinal.symbol.codeString}] no esta definido en el arquetipo, posiblemente el termino no esta definido para el lenguaje ${lang}.<br/>
  </g:if>
  <g:else>
    <% labels << archetypeTerm.items.text %>
  </g:else>
</g:each>
  
<!-- TODO: donde va el valor -->
<label class="${fields.getField(archetype.archetypeId.value +_refPath+ cDvOrdinal.path())}"></label>
