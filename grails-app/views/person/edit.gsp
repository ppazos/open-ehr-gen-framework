<%@ page import="demographic.party.Person" %><%@ page import="demographic.role.Role" %><%@ page import="tablasMaestras.TipoIdentificador" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'person.label', default: 'Person')}" />
    <title><g:message code="default.edit.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><g:link class="list" action="list"><g:message code="person.list.label" args="[entityName]" /></g:link></span>
      <span class="menuButton"><g:link class="show" action="show" id="${params.id}"><g:message code="person.show.label" args="[entityName]" /></g:link></span>
    </div>
    
    <div class="body">
      <h1><g:message code="default.edit.label" args="[entityName]" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
      <g:hasErrors bean="${personInstance}">
        <div class="errors">
          <g:renderErrors bean="${personInstance}" as="list" />
        </div>
      </g:hasErrors>
      
      <g:form method="post" >
        <g:hiddenField name="id" value="${personInstance?.id}" />
        <g:hiddenField name="version" value="${personInstance?.version}" />
        <div class="dialog">
          <table>
            <tbody>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="ids"><g:message code="person.ids.label" default="Identificadores" /></label>
                </td>
                <td valign="top" class="value">
                  <table>
                    <g:each in="${personInstance.ids}" status="j" var="id">
	                  <tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
	                    <td>
	                      <g:set var="codigo" value="${TipoIdentificador.findByCodigo(id.root)}" />
	                      ${id.extension} (${((codigo) ? codigo.nombreCorto : id.root)})
	                    </td>                  
	                  </tr>
	                </g:each>
                  </table>
                  TODO: editar identificadores ...
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
                  <label for="contacts"><g:message code="person.contacts.label" default="Contacts" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: personInstance, field: 'contacts', 'errors')}">
                  TODO: edicion de contactos...
                  <!--
                  <g:select name="contacts" from="${demographic.contact.Contact.list()}" multiple="yes" optionKey="id" size="5" value="${personInstance?.contacts*.id}" />
                  -->
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="buttons">
          <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>
