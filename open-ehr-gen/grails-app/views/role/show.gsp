<%@ page import="demographic.role.Role" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}" />
    <title><g:message code="default.show.label" args="[entityName]" /></title>
    <style>
      td.title {
        font-weight: bold;
      }
      td {
        vertical-align: middle;
      }
      /* Corrige CSS definido en el scaffolding */
      .dialog table {
        padding: 0px;
      }
      .permit {
        background-color: #fe9;
      }
    </style>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
      <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
    </div>
    <div class="body">
      <h1><g:message code="default.show.label" args="[entityName]" /> ${roleInstance?.type}</h1>
      
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
      <div class="dialog">
        <table>
          <tbody>
        
	          <!--
	          TODO: i18n de los textos hardcoded.
	          TODO: al seleccionar "todas" para un controler, se deben apagar los checks de las demas acciones de ese controller.
	          TODO: si el rol tiene un permit, mostrar el checkbox checked.
	          -->
	          
	          <g:each in="${permits.controller.unique()}" var="controller" status="i">
	            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	              <td class="title">
	                ${controller}                
	              </td>
	            </tr>
	            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	              <g:each in="${permits.findAll{ it.controller == controller }}" var="permit">
	              	<g:set var="checked" value="${roleInstance.permits.find{ p -> p.controller==permit.controller && p.action==permit.action }}" />
	                <td ${((checked)?'class="permit"':'')}>
	                  ${permit.action} <g:if test="${permit.action == '*'}">(todas)</g:if>
	                </td>
	              </g:each>
	            </tr>
	          </g:each>

          </tbody>
        </table>
      </div>
      <div class="buttons">
        <g:form>
          <g:hiddenField name="id" value="${roleInstance?.id}" />
          <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
          <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
        </g:form>
      </div>
    </div>
  </body>
</html>