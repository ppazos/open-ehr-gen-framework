<%@ page import="domain.Domain" %><%@ page import="java.text.SimpleDateFormat" %><%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <g:set var="startmsec" value="${System.currentTimeMillis()}" />
    <meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />
    
    <%-- No quiero paginas cacheadas --%>
    <%-- en FF no funca --%>
    <meta http-equiv="Cache-Control" content="no-cache" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="0" /> 
    
    <g:javascript>
      // Para evitar el boton de volver del navegador.
      window.history.go(1);
    </g:javascript>
    
    <title><g:layoutTitle/> | Open-EHRGen | v${ApplicationHolder.application.metadata['app.version']}</title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'ehr.css')}" />
    <g:layoutHead />
    
    <style>
      #body_table {
        background-color: #efefff;
        border: 1px solid #333399;
        width: 100%;
      }
      #body_td {
        width: auto;
      }
      #bottom_actions {
        /*text-align: right;*/
      }
    </style>
  </head>
  <body>
    <div id="user_bar">
      <b>Open-EHRGen</b> v${ApplicationHolder.application.metadata['app.version']} | 
      <g:datosUsuario />
      <span class="user_actions">
      
        <span class="currentDate">
          <g:format date="${new Date()}" />
        </span>
        
        <ul class="userBar lang">
          <g:langSelector>
            <li ${(session.locale.toString()==it.localeString)?'class="active"':''}>
              <a href="?sessionLang=${it.localeString}&templateId=${params.templateId}">${it.locale.getDisplayName(session.locale)}</a>
            </li>
          </g:langSelector>
        </ul>
        <ul class="userBar">
          <li ${(['domain'].contains(controllerName))?'class="active"':''}>
            <g:link controller="domain" action="list"><g:message code="domain.action.list" /></g:link>
          </li>
          <li>
           <g:set var="domain" value="${Domain.get(session.ehrSession.domainId)}" />
           (${domain.name})
          </li>
          <li ${(['records'].contains(controllerName))?'class="active"':''}>
            <g:link controller="records" action="list"><g:message code="records.action.list" /></g:link>
          </li>
          <li ${(controllerName=='demographic')?'class="active"':''}>
            <g:link controller="demographic" action="admisionPaciente"><g:message code="demographic.action.admisionPaciente" /></g:link>
          </li>
        </ul>
        
        <g:link controller="authorization" action="logout"><g:message code="authorization.action.logout" /></g:link>
        
      </span>
    </div>
  
    <div id="body">
      <g:if test="${flash.message}">
        <div id="message" class="error">
          <g:message code="${flash.message}" args="${flash.args}" />
        </div>
      </g:if>
      
      <table cellpadding="0" cellspacing="0" id="body_table">
        <tr>
          <td id="body_td" rowspan="2">
            <g:layoutBody />
          </td>
        </tr>
      </table>
    </div>
  </body>
</html>