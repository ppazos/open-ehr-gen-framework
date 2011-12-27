<%@ page import="org.openehr.am.archetype.constraintmodel.*" %><%@ page import="com.thoughtworks.xstream.XStream" %><%@ page import="binding.CtrlTerminologia" %>
<%@ page import="util.FieldNames" %>

<g:set var="fields" value="${FieldNames.getInstance()}" />

<%--
in: cCodePhrase (${cCodePhrase.class}) (${cCodePhrase.rmTypeName}) (${archetype.archetypeId})<br/>
in: selectedValue si es edit viene con el valor ingresado antes, si no es null.
<b>${cCodePhrase.path()}</b>
CCodePhrase<br/>
<textarea style="width: 700px; height: 200px;">${new XStream().toXML(cCodePhrase)}</textarea>
--%>

<g:set var="control" value="${template.getField( archetype.archetypeId.value, cCodePhrase.path() )?.getControlByPath(cCodePhrase.path())}" />

<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>

<!-- armo lista de valores con textos -->
<g:set var="values" value="${[]}" />
<g:set var="codes" value="${[]}" />
<g:if test="${cCodePhrase.codeList != null}">

  <%-- Si es un codigo que referencia a una terminologia externa --%>
  <g:if test="${cCodePhrase.codeList.size()==1 && cCodePhrase.codeList[0].startsWith('ac')}">
    <%--
    ${archetype.ontology.constraintDefinitionsList.definitions.code} - 
    ${archetype.ontology.constraintDefinitionsList.definitions.text} <br/>
    
    // ojo cCodePhrase.terminologyId es org.openehr.rm.support.identification.TerminologyID
    // y yo implemente hce.core.support.identification.TerminologyID
    ${cCodePhrase.terminologyId.getClass()}
    --%>
    
    <%
    def ctrm = CtrlTerminologia.getInstance()
    values = ctrm.getNombreTerminos( cCodePhrase.terminologyId.name )
    codes = ctrm.getCodigoTerminos( cCodePhrase.terminologyId.name )
    %>
  </g:if>
  <g:else>
    <g:set var="codes" value="${cCodePhrase.codeList}" />
    <g:codeListTerms archetype="${archetype}" codeList="${codes}" locale="${locale}">
      <g:set var="values" value="${it.labels}" />
    </g:codeListTerms>
    
    <%--
    <g:each in="${codes}" var="code">
      <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(lang, code)}" />
      <g:if test="${!archetypeTerm}">
        El termino con codigo [${code}] no esta definido en el arquetipo ${archetype.archetypeId.value}, ver que el termino este definido para el lenguaje ${lang}.<br/>
      </g:if>
      <g:else>
        <% values << archetypeTerm.items.text %>
      </g:else>
    </g:each>
    --%>
    
  </g:else>    
   
</g:if>
<g:else>
  La lista de codigos no tiene elmentos...
</g:else>

<!-- le pongo el value al code para obtener el value en el show, porque asi se guarda en PathValores -->
<%
def values2 = [:]
if (values.size()>0)
{
  for (int i in 0..values.size()-1)
  {
    values2[codes[i] +'||'+ values[i]] = values[i]
  }
}
%>

<g:set var="path" value="${archetype.archetypeId.value +_refPath+ cCodePhrase.path()}" />

<g:if test="${control && control.type=='radioGroup'}">
  <g:each in="${values2}" var="entry">
    <label class="id_${entry.value}"><!-- necesita id por el CSS -->
      <input type="radio" value="${entry.key}" name="${fields.getField(path)}" />
      ${entry.value}
    </label>
  </g:each>
  <label class="id_nr"><!-- necesita id por el CSS -->
    <input type="radio" checked="true" value="" name="${fields.getField(path)}" />
    NR
  </label>
</g:if>
<g:else>
  <g:select from="${values2.entrySet().value}"
            keys="${values2.entrySet().key}"
            name="${fields.getField(path)}"
            noSelection="${['':'']}"
            value="${selectedValue}"/>
</g:else>

<%--
<br/><br/>
Params Value: ${params[archetype.archetypeId.value +_refPath+ cCodePhrase.path()]}<br/>
SelectedValue: ${selectedValue}<br/>
Path: ${archetype.archetypeId.value +_refPath+ cCodePhrase.path()}<br/><br/>
--%>
<% /*
<span class="ccode_phrase_selected_text_description">TODO: setear con la descripcion del valor seleccionado</span>
*/ %>

<!-- path: ${path} -->