<html>
  <head>
    <meta name="layout" content="ehr-modal" />
    <title><g:message code="demographic.show.title" /></title>
    <style>
        table #list {
          background-color: #ffffdd;
          width: 100%;
          font-size: 12px;
          border: 1px solid #000;
        }
        #list th {
          background-color: #ccccdd;
        }
        #list td {
          text-align: center;
        }
      </style>
  </head>
  <body>
    <h1><g:message code="demographic.show.title" /></h1>
  
    <ul class="top_actions">
      <li>
        <g:link action="admisionPaciente" class="back"><g:message code="demographic.lista_candidatos.action.admisionPaciente" /></g:link>
      </li>
      <li>
        <g:link controller="records" action="create" params="[root:root, extension:extension]" class="create"><g:message code="demographic.show.action.createEpisode" /></g:link>
      </li>
      <%-- TODO: que otra accion sea seleccionar un episodio existente --%>
    </ul>
  
    <table id="list">
      <tr>
        <th><g:message code="persona.identificadores" /></th>
        <th><g:message code="persona.primerNombre" /></th>
        <th><g:message code="persona.segundoNombre" /></th>
        <th><g:message code="persona.primerApellido" /></th>
        <th><g:message code="persona.segundoApellido" /></th>
        <th><g:message code="persona.fechaNacimiento" /></th>
        <th><g:message code="persona.sexo" /></th>
      </tr>
      <tr>
        <td><g:render template="UIDBasedID" collection="${persona.ids}" var="id" /></td>
        <g:set var="name" value="${persona.identities.find{ it.purpose == 'PersonName'} }" />
        <td>${name?.primerNombre}</td>
        <td>${name?.segundoNombre}</td>
        <td>${name?.primerApellido}</td>
        <td>${name?.segundoApellido}</td>
        <td>${persona.fechaNacimiento}</td>
        <td>${persona.sexo}</td>
      </tr>
    </table>
  </body>
</html>