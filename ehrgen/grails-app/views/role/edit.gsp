<%@ page import="demographic.role.Role" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="layout" content="main" />
    <g:set var="entityName" value="${message(code: 'role.label', default: 'Role')}" />
    <title><g:message code="default.edit.label" args="[entityName]" /></title>
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
      /* Para corregir alineamiento de checkboxes a sus labels */
      input[type=checkbox] {
        width: 13px;
        height: 13px;
        padding: 0;
        margin:0;
        vertical-align: middle;
        position: relative;
        top: -1px;
        *overflow: hidden;
      }
    </style>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton"><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></span>
      <span class="menuButton"><g:link class="show" action="show" id="${roleInstance.id}"><g:message code="default.show.label" args="[entityName]" /></g:link></span>
    </div>
    <div class="body">
      <h1><g:message code="default.edit.label" args="[entityName]" /> ${roleInstance?.type}</h1>
      
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      
      <g:hasErrors bean="${roleInstance}">
        <div class="errors">
          <g:renderErrors bean="${roleInstance}" as="list" />
        </div>
      </g:hasErrors>
      
      <g:form method="post">
        <g:hiddenField name="id" value="${roleInstance?.id}" />
        <g:hiddenField name="version" value="${roleInstance?.version}" />
        <div class="dialog list">

          <h2>Permisos de dominio</h2>
          <table>
            <tbody>
              <!--
              TODO: i18n de los textos hardcoded.
              TODO: al seleccionar "todas" para un controler, se deben apagar los checks de las demas acciones de ese controller.
              TODO: el id del template deberia traer el nombre y descripcion del template, que esta definido adentro del propio template.
              -->
              
              <g:each in="${domainPermits.domain.unique()}" var="domain" status="i">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                  <td class="title">
                    ${domain.name}                
                  </td>
                </tr>
                <g:each in="${domainPermits.findAll{ it.domainId == domain.id }}" var="permit">
                  <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <g:set var="checked" value="${roleInstance.domainPermits.find{ p -> p.domain==permit.domain && p.templateId==permit.templateId }}" />
                    <td ${((checked)?'class="permit"':'')} title="${permit.templateId}">
                      <label>
                        <input type="checkbox" name="dpermits" value="${permit.domainId}__${permit.templateId}" ${((checked)?'checked="true"':'')} />
                        ${permit.templateId} <g:if test="${permit.templateId == '*'}">(todas)</g:if>
                      </label>
                    </td>
                  </tr>
                </g:each>
              </g:each>
            </tbody>
          </table>
        
		  <%-- No se van a usar mas por ahora...
          <h2>Permisos de bajo nivel</h2>
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
                    <td>
                      <label>
                        <g:set var="checked" value="${roleInstance.permits.find{ p -> p.controller==permit.controller && p.action==permit.action }}" />
                        <input type="checkbox" name="permits" value="${permit.controller}__${permit.action}" ${((checked)?'checked="true"':'')} />
                        ${permit.action} <g:if test="${permit.action == '*'}">(todas)</g:if>
                      </label>
                    </td>
                  </g:each>
                </tr>
              </g:each>

            </tbody>
          </table>
		  --%>
		  
        </div>
        <div class="buttons">
          <span class="button"><g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
          <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
        </div>
      </g:form>
    </div>
  </body>
</html>