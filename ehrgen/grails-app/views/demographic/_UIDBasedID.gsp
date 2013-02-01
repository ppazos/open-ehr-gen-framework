<%-- FIXME: el nombreCorto deberia ser un codigo i18n --%>
<g:set var="codigo" value="${tablasMaestras.TipoIdentificador.findByCodigo(id.root)}" />
<%-- ${id.value} [${ ((codigo) ? codigo.nombreCorto : id.root) }]<br/> --%>
${id.extension} [${ ((codigo) ? codigo.nombreCorto : id.root) }]