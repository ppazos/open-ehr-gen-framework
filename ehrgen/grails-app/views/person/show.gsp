<%-- Copia de demographic.show modificada para mostrar personas en el backend --%><%@ page import="util.DateDifference" %><%@ page import="java.text.SimpleDateFormat" %><%@ page import="tablasMaestras.TipoIdentificador" %><%@ page import="java.util.TimeZone" %>
<html>
  <head>
    <meta name="layout" content="main" />
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
    <div class="nav">
      <span class="menuButton">
        <g:link action="list" class="list"><g:message code="person.list" /></g:link>
      </span>
      <span class="menuButton">
        <g:link class="create" controller="roleValidity" action="create" id="${person.id}"><g:message code="default.addRole.label" args="[entityName]" /></g:link>
      </span>
    </div>
    
    <div class="body">
      <h1><g:message code="demographic.show.title" /></h1>
      
      <div class="list">
        <h2><g:message code="demographic.show.personData" /></h2>
        <table>
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
            <td>
              <table>
                <g:each in="${person.ids}" status="j" var="id">
                  <tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
                    <td>
                      <g:set var="codigo" value="${TipoIdentificador.findByCodigo(id.root)}" />
                      ${id.extension} (${((codigo) ? codigo.nombreCorto : id.root)})
                    </td>                  
                  </tr>
                </g:each>
              </table>
            </td>
            <td>${person.primerNombre}</td>
            <td>${person.segundoNombre}</td>
            <td>${person.primerApellido}</td>
            <td>${person.segundoApellido}</td>
            <td>
              <%-- TODO: i18n --%>
              <g:formatDate format="yyyy-MM-dd" date="${person.fechaNacimiento}" />
              
              <% // muestra fecha de nacimiento y edad
                if (person.fechaNacimiento)
                {
                  print " ( " + DateDifference.numberOfYears(person.fechaNacimiento, new Date()) + " )"
                }
              %>
            </td>
            <td>${person.sexo}</td>
          </tr>
        </table>
        <div class="buttons">
          <g:form>
            <g:hiddenField name="id" value="${person.id}" />
            <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
            <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
          </g:form>
        </div>
        
        
        <h2><g:message code="demographic.show.personRoles" /></h2>
        <table>
          <thead>
            <tr>
              <th>${message(code: 'role.type.label', default: 'Type')}</th>
              <th>${message(code: 'role.timeValidityFrom.label', default: 'From')}</th>
              <th>${message(code: 'role.timeValidityTo.label', default: 'To')}</th>
              <th>${message(code: 'role.valid.label', default: 'Is valid')}</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <g:set var="dateFormatter" value="${new SimpleDateFormat('yyyy-MM-dd HH:mm')}" /><%-- FIXME: el formato depende del locale --%>
            <g:each in="${person.roles}" status="i" var="roleValidity">
              <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <td>${roleValidity.role.type}</td>
                <td>
                  <%-- TODO: i18n --%>
                  <g:formatDate format="yyyy-MM-dd" date="${roleValidity.timeValidityFrom}" />
                </td>
                <td>
                  <%-- TODO: i18n --%>
                  <g:formatDate format="yyyy-MM-dd" date="${roleValidity.timeValidityTo}" />
                </td>
                <td>${roleValidity.valid}</td>
                <td>
                  <g:link controller="roleValidity" action="edit" id="${roleValidity.id}">Modificar</g:link>
                  <g:link controller="roleValidity" action="delete" id="${roleValidity.id}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">Quitar</g:link>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </body>
</html>