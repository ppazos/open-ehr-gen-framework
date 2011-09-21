
<%@ page import="demographic.role.RoleValidity" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'roleValidity.label', default: 'RoleValidity')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
      <span class="menuButton"><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></span>
    </div>
    <div class="body">
      <h1><g:message code="default.list.label" args="[entityName]" /></h1>
      <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
      </g:if>
      <div class="list">
        <table>
          <thead>
            <tr>
              <g:sortableColumn property="id" title="${message(code: 'roleValidity.id.label', default: 'Id')}" />
              <g:sortableColumn property="timeValidityTo" title="${message(code: 'roleValidity.timeValidityTo.label', default: 'Time Validity To')}" />
              <th><g:message code="roleValidity.performer.label" default="Performer" /></th>
              <th><g:message code="roleValidity.role.label" default="Role" /></th>
              <g:sortableColumn property="timeValidityFrom" title="${message(code: 'roleValidity.timeValidityFrom.label', default: 'Time Validity From')}" />
              <g:sortableColumn property="valid" title="${message(code: 'roleValidity.valid.label', default: 'Valid')}" />
            </tr>
          </thead>
          <tbody>
          <g:each in="${roleValidityInstanceList}" status="i" var="roleValidityInstance">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
              <td><g:link action="show" id="${roleValidityInstance.id}">${fieldValue(bean: roleValidityInstance, field: "id")}</g:link></td>
              <td><g:formatDate date="${roleValidityInstance.timeValidityTo}" /></td>
              <td>${fieldValue(bean: roleValidityInstance, field: "performer")}</td>
              <td>${fieldValue(bean: roleValidityInstance, field: "role")}</td>
              <td><%-- TODO: i18n --%>
                <g:formatDate format="yyyy-MM-dd" date="${roleValidityInstance.timeValidityFrom}" />
              </td>
              <td><g:formatBoolean boolean="${roleValidityInstance.valid}" /></td>
            </tr>
          </g:each>
          </tbody>
        </table>
      </div>
      
      <div class="paginateButtons">
        <g:paginate total="${roleValidityInstanceTotal}" />
      </div>
    </div>
  </body>
</html>