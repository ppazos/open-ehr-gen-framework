<%@ page import="org.openehr.am.archetype.constraintmodel.*" %><%@ page import="binding.CtrlTerminologia" %><%@ page import="util.FieldNames" %>
<g:set var="fields" value="${FieldNames.getInstance()}" />
<g:set var="control" value="${template.getField( archetype.archetypeId.value, constraintRef.path() )?.getControlByPath(constraintRef.path())}" />
<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>

<g:set var="path" value="${archetype.archetypeId.value +_refPath+ constraintRef.path()}" />

<!-- El nombre lo tiene en data-name, este campo no necesita nombre porque no se submitea -->
<input type="text" value="" data-name="${fields.getField(path)}"
       placeholder="Ingrese texto a buscar" class="find_in_terminology"
       data-bind="${constraintRef.reference}" data-archetypeid="${archetype.archetypeId.value}" />

<!-- path: ${path} -->