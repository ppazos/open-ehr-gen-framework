<%@ page import="tablasMaestras.TipoIdentificador" %>

<%-- FIXME: el nombreCorto deberia ser un codigo i18n --%>

<%
def codigo = TipoIdentificador.findByCodigo(id.root)
%>

<%-- ${id.value} [${ ((codigo) ? codigo.nombreCorto : id.root) }]<br/> --%>

${id.extension} [${ ((codigo) ? codigo.nombreCorto : id.root) }]<br/>