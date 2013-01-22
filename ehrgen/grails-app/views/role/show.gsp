<%@ page import="demographic.role.Role" %><%@ page import="domain.Domain" %>
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
      
      <div class="dialog list">
        <h2>Permisos de dominio</h2>
        <table>
          <tbody>
            <!--
            TODO: i18n de los textos hardcoded.
            TODO: al seleccionar "todas" para un controler, se deben apagar los checks de las demas acciones de ese controller.
            TODO: el id del template deberia traer el nombre y descripcion del template, que esta definido adentro del propio template.
            -->
            
            <g:each in="${domainPermits.domain.unique()}" var="domainId" status="i">
              <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                <td class="title">
                  ${message(code:Domain.get(domainId).name)}                
                </td>
              </tr>
              <g:each in="${domainPermits.findAll{ it.domain == domain }}" var="permit">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                  <g:set var="checked" value="${roleInstance.domainPermits.find{ p -> p.domain==permit.domain && p.templateId==permit.templateId }}" />
                  <td ${((checked)?'class="permit"':'')} title="${permit.templateId}">
                    ${permit.templateId} <g:if test="${permit.templateId == '*'}">(todas)</g:if>
                  </td>
                </tr>
              </g:each>
            </g:each>
          </tbody>
        </table>
        <%--
        <h2>Permisos de bajo nivel</h2>
        <table>
          <tbody>
            <!--
              TODO: i18n de los textos hardcoded.
              TODO: al seleccionar "todas" para un controler, se deben apagar los checks de las demas acciones de ese controller.
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
        --%>
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