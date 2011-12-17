<%@ page import="org.openehr.am.archetype.constraintmodel.*" %><%@ page import="com.thoughtworks.xstream.XStream" %><%@ page import="binding.CtrlTerminologia" %><%@ page import="util.FieldNames" %>

<g:set var="fields" value="${FieldNames.getInstance()}" />
<%--
in: cCodePhrase (${cCodePhrase.class}) (${cCodePhrase.rmTypeName}) (${archetype.archetypeId})<br/>
in: selectedValue si es edit viene con el valor ingresado antes, si no es null.
<b>${cCodePhrase.path()}</b>
CCodePhrase<br/>
<textarea style="width: 700px; height: 200px;">${new XStream().toXML(cCodePhrase)}</textarea>
--%>
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
    <%
    def ctrm = CtrlTerminologia.getInstance()
    values = ctrm.getNombreTerminos( cCodePhrase.terminologyId.name )
    codes = ctrm.getCodigoTerminos( cCodePhrase.terminologyId.name )
    %>
  </g:if>
  <g:else>
    <g:set var="codes" value="${cCodePhrase.codeList}" />
    <g:each in="${codes}" var="code">
      <g:set var="archetypeTerm" value="${archetype.ontology.termDefinition(lang, code)}" />
      <g:if test="${!archetypeTerm}">
        El termino con codigo [${code}] no esta definido en el arquetipo ${archetype.archetypeId.value}, ver que el termino este definido para el lenguaje ${lang}.<br/>
      </g:if>
      <g:else>
        <% values << archetypeTerm.items.text %>
      </g:else>
    </g:each>
  </g:else>
</g:if>
<g:else>
  La lista de codigos no tiene elmentos...
</g:else>

<!-- TODO: donde se va a poner el valor del codePhrase -->
<!-- la otra class es la path para meterle el valor desde JS -->
<label class="${fields.getField(archetype.archetypeId.value +_refPath+ cCodePhrase.path())}"></label>
<!-- TODO: Si no se ingresa valor, desde js deberia mostrar NR -->
