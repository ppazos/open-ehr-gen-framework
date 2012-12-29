<%@ page import="org.openehr.am.archetype.constraintmodel.*" %>
<%--
// in: cAttribute (${cAttribute.rmAttributeName}) (${cAttribute.class})<br/>
// in: refPath path del nodo arch internal ref que referencia al CObject duenio de este atributo.
--%>
<%
// refPath es nulo si no viene de un arch internal ref

def _refPath = ''
if (refPath) _refPath = refPath

%>

<%-- ATTR REFPATH: ${_refPath}<br/> --%>
  <g:render template="../guiGen/create/cObject"
            var="cObject"
            collection="${cAttribute.children}"
            model="[archetype: archetype,
                    archetypeService: archetypeService,
                    refPath: refPath,
                    params: params, lang: lang, locale: locale, template: template]" />