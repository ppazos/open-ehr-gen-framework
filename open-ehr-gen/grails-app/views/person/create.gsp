<%@ page import="demographic.party.Person" %><%@ page import="demographic.role.Role" %><%@ page import="tablasMaestras.TipoIdentificador" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title><g:message code="person.create.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="nav">
      <%-- <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span> --%>
      <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
    </div>
    
    <div class="body">
      <h1><g:message code="person.create.label" args="[entityName]" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
      <g:hasErrors bean="${personInstance}">
        <div class="errors">
          <g:renderErrors bean="${personInstance}" as="list" />
        </div>
      </g:hasErrors>
      
      <g:form action="save" >
        <div class="dialog">
          <table>
            <tbody>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="ids"><g:message code="person.ids.label" default="Identificadores" /></label>
                </td>
                <td valign="top" class="value">
                  <g:textField name="extension" value="${params.extension}" />
                  <g:select name="root" from="${TipoIdentificador.list()}" value="${params.root}"
                            noSelection="${['':'Tipo de identificador...']}"
                            optionKey="codigo" optionValue="nombreCorto" />
                </td>
              </tr>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="primerNombre"><g:message code="person.primerNombre.label" default="Primer Nombre" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'primerNombre', 'errors')}">
                  <g:textField name="primerNombre" value="${personInstance?.primerNombre}" />
                </td>
              </tr>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="segundoNombre"><g:message code="person.segundoNombre.label" default="Segundo Nombre" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'segundoNombre', 'errors')}">
                  <g:textField name="segundoNombre" value="${personInstance?.segundoNombre}" />
                </td>
              </tr>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="primerApellido"><g:message code="person.primerApellido.label" default="Primer Apellido" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'primerApellido', 'errors')}">
                  <g:textField name="primerApellido" value="${personInstance?.primerApellido}" />
                </td>
              </tr>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="segundoApellido"><g:message code="person.segundoApellido.label" default="Segundo Apellido" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'segundoApellido', 'errors')}">
                  <g:textField name="segundoApellido" value="${personInstance?.segundoApellido}" />
                </td>
              </tr>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="fechaNacimiento"><g:message code="person.fechaNacimiento.label" default="Fecha Nacimiento" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'fechaNacimiento', 'errors')}">
                  <g:datePicker name="fechaNacimiento" precision="day" value="${personInstance?.fechaNacimiento}" default="none" noSelection="['': '']" />
                </td>
              </tr>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="sexo"><g:message code="person.sexo.label" default="Sexo" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'sexo', 'errors')}">
                  <g:select name="sexo" from="${personInstance.constraints.sexo.inList}" value="${personInstance?.sexo}" valueMessagePrefix="person.sexo" noSelection="['': '']" />
                </td>
              </tr>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="role_type"><g:message code="person.role_type.label" default="Rol" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'type', 'errors')}">
                  <g:select from="${Role.list().type}" name="role_type" value="${params.role_type}" noSelection="${['':'Rol...']}" />
                </td>
              </tr>
            
            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button"><g:submitButton name="create" class="save" value="${message(code: 'default.button.create.label', default: 'Create')}" /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
