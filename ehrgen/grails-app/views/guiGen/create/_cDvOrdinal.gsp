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

<%-- armo lista de valores con textos
<g:each in="${cDvOrdinal.list.sort{ it.value }}" var="ordinal">
  
  <!-- FIXME: esto deberia sacarse de TagLib getTerm -->
  <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(lang, ordinal.symbol.codeString)}" />
  <g:if test="${!archetypeTerm}">
    El termino con codigo [${ordinal.symbol.codeString}] no esta definido en el arquetipo, posiblemente el termino no esta definido para el lenguaje ${lang}.<br/>
  </g:if>
  <g:else>
    <% labels << archetypeTerm.items.text %>
  </g:else>
  
  FIXME: obtener todos los codigos y hacer un taglib para obtener las traducciones para cada codigo, escalando en el locale.
  
</g:each>
--%>


<%-- Aca values tiene los codigos, es como codes en _cCodePhrase.gsp --%>
<g:set var="values" value="${cDvOrdinal.list.sort{ it.value }.symbol.codeString}" />

<g:codeListTerms archetype="${archetype}" codeList="${values}" locale="${locale}">
  <g:set var="labels" value="${it.labels}" />
</g:codeListTerms>


<!-- le pongo el value al code para obtener el value en el show, porque asi se guarda en PathValores -->
<%-- collectEntries disponible desde Groovy 1.7.9 y grails 1.3.7 tiene Groovy 1.7.8 
<g:set var="values" value="${values.collectEntries{ key, value -> [key +'||'+ value, value] }}" />
--%>

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