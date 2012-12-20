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

<%-- Aca values tiene los codigos, es como codes en _cCodePhrase.gsp --%>
<g:set var="values" value="${cDvOrdinal.list.sort{ it.value }.symbol.codeString}" />

<g:codeListTerms archetype="${archetype}" codeList="${values}" locale="${locale}">
  <g:set var="labels" value="${it.labels}" />
</g:codeListTerms>

<%
def values2 = [:]
if (values.size()>0)
{
  for (int i in 0..values.size()-1)
  {
    values2[values[i] +'||'+ labels[i]] = labels[i]
  }
}
%>

<g:set var="path" value="${archetype.archetypeId.value +_refPath+ cDvOrdinal.path()}" />
<g:set var="control" value="${template.getField( archetype.archetypeId.value, cDvOrdinal.path() )?.getControlByPath(cDvOrdinal.path())}" />

<%-- TODO: seleccionar valor por defecto. --%>

<g:if test="${control && control.type=='radioGroup'}">
  <g:each in="${values2}" var="entry">
    <label class="${template.templateId}_${entry.key.split(/\|\|/)[0]}"><!-- necesita id por el CSS, ojo si es un nodo multiple, no puede repetirse el id-->
      <input type="radio" value="${entry.key}" name="${fields.getField(path)}" />
      ${entry.value}
    </label>
  </g:each>
  
  <!-- TODO: poner una opcion NR? como en cCodePhrase -->
  
</g:if>
<g:else>
  <!-- from: labels
       keys: values
  -->
  <g:select from="${values2.entrySet().value}"
            keys="${values2.entrySet().key}"
            name="${fields.getField(path)}"
            noSelection="${['':'']}" />
</g:else>

<% /*
<span class="ccode_phrase_selected_text_description">TODO: setear con la descripcion del valor seleccionado</span>
*/ %>

<!-- path: ${path} -->