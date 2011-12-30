<%@ page import="demographic.role.RoleValidity" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'roleValidity.label', default: 'RoleValidity')}" />
    <title><g:message code="default.create.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton">
        <g:link class="show" controller="person" action="show" id="${params.id}"><g:message code="person.show.label" args="[entityName]" /></g:link>
      </span>
    </div>
    <div class="body">
      <h1><g:message code="default.create.label" args="[entityName]" /></h1>
      
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
      <g:hasErrors bean="${roleValidityInstance}">
        <div class="errors">
          <g:renderErrors bean="${roleValidityInstance}" as="list" />
        </div>
      </g:hasErrors>
      
      <g:form action="save" >
        <input type="hidden" name="performer.id" value="${params.id}"  />
        
        <div class="dialog">
          <table>
            <tbody>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="timeValidityFrom"><g:message code="roleValidity.timeValidityFrom.label" default="Time Validity From" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: roleValidityInstance, field: 'timeValidityFrom', 'errors')}">
                  <g:datePicker name="timeValidityFrom" precision="day" value="${roleValidityInstance?.timeValidityFrom}"  />
                </td>
              </tr>
              
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="timeValidityTo"><g:message code="roleValidity.timeValidityTo.label" default="Time Validity To" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: roleValidityInstance, field: 'timeValidityTo', 'errors')}">
                  <g:datePicker name="timeValidityTo" precision="day" value="${roleValidityInstance?.timeValidityTo}" default="none" noSelection="['': '']" />
                </td>
              </tr>
              
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="role"><g:message code="roleValidity.role.label" default="Role" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: roleValidityInstance, field: 'role', 'errors')}">
                  <%-- TODO: mostrar solo los roles que la persona NO tenga asignados --%>
                  <g:select name="role.id" from="${demographic.role.Role.list()}" optionKey="id" value="${roleValidityInstance?.role?.id}"  />
                </td>
              </tr>
            
              <tr class="prop">
                <td valign="top" class="name">
                  <label for="valid"><g:message code="roleValidity.valid.label" default="Valid" /></label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: roleValidityInstance, field: 'valid', 'errors')}">
                  <g:checkBox name="valid" value="${roleValidityInstance?.valid}" />
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
