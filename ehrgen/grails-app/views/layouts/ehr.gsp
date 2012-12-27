<%@ page import="domain.Domain" %><%@ page import="workflow.Stage" %><%@ page import="java.text.SimpleDateFormat" %><%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <g:set var="startmsec" value="${System.currentTimeMillis()}" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

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
            <g:resumenEpisodio episodeId="${session.ehrSession?.episodioId}" />
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
                  <g:link controller="records" action="show" id="${session.ehrSession?.episodioId}">
                    <g:message code="trauma.menu.show" />
                  </g:link>
                </li>
                
                <%-- Menu de la derecha para acceder a las etapas de registro clinico --%>
                <g:canFillClinicalRecord>
                  
                  <%--
                  TODO: desde lo estudios img hasta el registro clinico no puede ser visto por un administrativo.
                  --%>
                  
                  <%-- Puede ser null sino hay un template --%>
                  <g:set var="currentStage" value="${workflow.getStage(template)}" />
                  
                  <g:if test="${( ['guiGen','records','ajaxApi'].contains(controllerName) && ['generarShow','generarTemplate','show','saveDiagnostico','showRecord','signRecord'].contains(actionName) )}">
                    <%-- nombres de stages dek workflow actual --%>
                    <g:each in="${workflow.stages}" var="stage">
                      
                      <%-- template puede ser null sino estoy en una seccion de registro --%>
                      <li ${( (currentStage?.name == stage.name) ? 'class="active"' : '')}>
                      
                        <%
                        def templateId = stage?.recordDefinitions?.getAt(0)?.templateId // allSubsections[stage.name][0]
                        if (!templateId) templateId = " " // para que no sea null o vacia en la llamada a g:hasContentItemForTemplate
                                                          // que espera no null y no vacio el templateId.
                        %>
                        
                        <%-- se fija si el registro ya fue hecho --%>
                        <%-- templateId: ${templateId}<br/> --%>
                        <g:hasContentItemForStage episodeId="${session.ehrSession?.episodioId}" stage="${stage}">

                          <g:if test="${it.hasItem}">
                            <g:link controller="guiGen" action="generarShow" id="${it.itemId}">
                              <g:message code="${stage.name}" /> (+) <%-- + es que se hizo algun registro en la seccion --%>
                            </g:link>
                          </g:if>
                          <g:else>
                            
                            <g:set var="stage" value="${Stage.findByNameAndOwner(stage.name, workflow)}" />
                            
                            <%-- Se verifica que el item tenga algun permiso para los templates contenidos en la seccion --%>
                            <g:hasDomainPermit domain="${domain}" templateIds="${stage.recordDefinitions.templateId}">
                              <g:link controller="records" action="registroClinico2" params="[section:stage.name]">
                                <g:message code="${stage.name}" />
                              </g:link>
                            </g:hasDomainPermit>
                            <g:dontHasDomainPermit><%-- Si no tiene permisos, se muestra el boton sin link --%>
                              <a href="javascript:alert('No tiene permisos para ingresar a esta seccion');" class="unavailable"><g:message code="${stage.name}" /></a>
                            </g:dontHasDomainPermit>
                            
                          </g:else>
                        </g:hasContentItemForStage>
                      </li>
                    </g:each>
                  </g:if>
                  <li ${((controllerName=='records'&&['signRecord'].contains(actionName)) ? 'class="active"' : '')}>
                    <g:link controller="records" action="signRecord" id="${session.ehrSession?.episodioId}">
                      <g:message code="registro.menu.close" />
                      <g:isSignedRecord episodeId="${session.ehrSession?.episodioId}">(+)</g:isSignedRecord>
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