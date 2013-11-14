<%@ page import="org.openehr.am.archetype.constraintmodel.*" %><%@ page import="binding.CtrlTerminologia" %><%@ page import="util.FieldNames" %>
<g:set var="fields" value="${FieldNames.getInstance()}" />
<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>
<g:set var="path" value="${archetype.archetypeId.value +_refPath+ constraintRef.path()}" />
       
<label class="${fields.getField(path)}"></label>

<!-- path: ${path} -->