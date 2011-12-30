<%@ page import="java.text.SimpleDateFormat" %><%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %><%@ page import="hce.core.common.directory.Folder" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <g:set var="startmsec" value="${System.currentTimeMillis()}" />
    <meta http-equiv="Content-Type" content="text/html;charset=ISO-8859-1" />

    <%-- No quiero paginas cacheadas --%>
    <%--
    <meta HTTP-EQUIV="Pragma" CONTENT="no-cache" />
    <meta HTTP-EQUIV="Expires" CONTENT="-1" />
    <!-- META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" /-->
    <meta HTTP-EQUIV="Cache-Control" content="no-cache, must-revalidate" />
    --%>
    <%-- en FF no funca --%>
    <meta http-equiv="Cache-Control" content="no-cache" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="0" /> 
    
    <g:javascript>
      // Para evitar el boton de volver del navegador.
      window.history.go(1);
    </g:javascript>
    
    <title><g:layoutTitle/> | Open-EHRGen | v${ApplicationHolder.application.metadata['app.version']}</title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'ehr_contenido_grande.css')}" />
    <g:layoutHead />
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
              <%-- no dejo cambiar el idioma si la accion es save http://code.google.com/p/open-ehr-sa/issues/detail?id=65 --%>
              <g:if test="${actionName=='save'}">
                 <a href="#">${it.locale.getDisplayName(session.locale)}</a>
              </g:if>
              <g:else>
                <a href="?sessionLang=${it.localeString}&templateId=${params.templateId}">${it.locale.getDisplayName(session.locale)}</a>
              </g:else>
            </li>
          </g:langSelector>
        </ul>
        <ul class="userBar">
          <li ${(['domain'].contains(controllerName))?'class="active"':''}>
            <g:link controller="domain" action="list"><g:message code="domain.action.list" /></g:link>
          </li>
          <li>
           <g:set var="folder" value="${Folder.findByPath(session.traumaContext.domainPath)}" />
           (${folder.name.value})
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
      <%-- El registro clinico ya tiene un flash para mostrar mensajes, saco este para que no muestre doble.
      <g:if test="${flash.message}">
        <div id="message" class="error">
          <g:message code="${flash.message}" args="${flash.args}" />
        </div>
      </g:if>
      --%>
      <table cellpadding="0" cellspacing="0">
        <tr>
          <td id="body_table" rowspan="2">
            <g:resumenEpisodio episodeId="${episodeId}" />
            <g:layoutBody />
          </td>
          <td>
            <div id="infoPaciente">
              <h2><g:message code="trauma.title.informacionPaciente" /></h2>
              <%-- A patient lo manda como modelo guiGenController.generarTemplate --%>
              <g:if test="${patient}">
                <g:render template="../demographic/Person" model="[person:patient]" />
                <g:canEditPatient patient="${patient}">
                  <g:link controller="demographic" action="edit" id="${patient.id}"><g:message code="demographic.action.completarDatos" /></g:link>
                </g:canEditPatient>
              </g:if>
              <g:else>
                <g:message code="trauma.layout.pacienteNoIdentificado.label" />:<br/>
                <g:link controller="demographic" action="admisionPaciente">
                  <g:message code="trauma.layout.identificarPaciente.action" />
                </g:link>
              </g:else>
            </div>
            <div id="menu">
              <ul>
                <li>
                  <g:link controller="records" action="list">
                    <g:message code="trauma.menu.list" />
                  </g:link>
                </li>
                <li ${((controllerName=='records'&&['show'].contains(actionName)) ? 'class="active"' : '')}>
                  <g:link controller="records" action="show" id="${episodeId}">
                    <g:message code="trauma.menu.show" />
                  </g:link>
                </li>
                
                <g:canFillClinicalRecord>
                  
                  <%--
                  TODO: desde lo estudios img hasta el registro clinico no puede ser
                        visto por un administrativo.
                  --%>
                  <g:if test="${( ['guiGen','records','ajaxApi'].contains(controllerName) && ['generarShow','generarTemplate','show','saveDiagnostico','showRecord'].contains(actionName) )}">
                    
                    <g:each in="${sections}" var="section">
                      <li ${(( template?.id?.startsWith(section) ) ? 'class="active"' : '')}>
                        <%-- allSubsections: ${allSubsections}<br/> --%>
                        <%-- se fija si el registro ya fue hecho --%>
                        <%
                        //def subsection = subsections.find{it.startsWith(section)}
                        def subsection = allSubsections[section][0]
                        if (!subsection) subsection = " " // para que no sea null o vacia en la llamada a g:hasContentItemForTemplate
                                                          // que espera no null y no vacio el templateId.
                        %>
                        <%-- subsection: ${subsection}<br/> --%>
                        <g:hasContentItemForTemplate episodeId="${episodeId}" templateId="${section+'-'+subsection}">
                          <g:if test="${it.hasItem}">
                            <g:link controller="guiGen" action="generarShow" id="${it.itemId}">
                              <g:message code="${'section.'+section}" /> (+) <%-- + es que se hizo algun registro en la seccion --%>
                            </g:link>
                          </g:if>
                          <g:else>
                            <g:link controller="records" action="registroClinico2" params="[section:section]">
                              <g:message code="${'section.'+section}" />
                            </g:link>
                          </g:else>
                        </g:hasContentItemForTemplate>
                      </li>
                    </g:each>
                  </g:if>
                  <li ${((controllerName=='records'&&['signRecord'].contains(actionName)) ? 'class="active"' : '')}>
                    <g:link controller="records" action="signRecord" id="${episodeId}">
                      <g:message code="registro.menu.close" />
                      <g:isSignedRecord episodeId="${episodeId}">(+)</g:isSignedRecord>
                    </g:link>
                  </li>
                </g:canFillClinicalRecord>
              </ul>
            </div>
          </td>
        </tr>
      </table>
    </div>
  </body>
</html>