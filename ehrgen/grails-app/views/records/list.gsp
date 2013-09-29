<%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %><%@ page import="hce.core.common.change_control.Version" %><%@ page import="hce.core.composition.Composition" %><%@ page import="java.text.SimpleDateFormat" %><%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %>
<html>
  <head>
    <meta name="layout" content="ehr-modal" />
    <title><g:message code="episodio.list.title" /></title>
    <style>
      table #list {
        background-color: #ffffdd;
        width: 100%;
        font-size: 12px;
        border: 1px solid #000;
      }
      tr.odd { /* zebra */
        background-color: #ffffee;
      }
      #list tr:hover {
        background-color: #ddddff;
      }
      #list th {
        background-color: #ccccdd;
      }
      #list td {
        text-align: center;
        padding: 2px;
      }
    
      /* paginacion */
      .step, .currentStep, .nextLink, .prevLink {
        padding-right: 5px;
        padding-top: 7px;
        display: inline-block;
      }
      .currentStep {
        font-weight: bold;
      }
      #create_record {
        display: none;
      }
      li.active {
        font-weight: bold;
      }
      /* ============================>>> Achica imagen depickdate */
      img.ui-datepicker-trigger {
        height: 2em;
        vertical-align: top;
      }
      form.filter {
        display: inline;
      }
    </style>
    <link rel="stylesheet" href="${resource(dir:'css', file:'jquery-ui-1.9.2.datepicker.min.css')}" />
    
    <g:javascript library="jquery-1.8.2.min" />
    <g:javascript src="jquery.blockUI.js" />
    <g:javascript src="jquery-ui-1.9.2.datepicker.min.js" />
    <g:javascript>
    
      var showCreateRecord = function() {
      
        // Pido la vista al servidor
        $.ajax({
           url: '${createLink(action:"create")}',
           //data: data,
           success: function(data, textStatus, jqXHR) {
           
             $('#create_record').html( data );
           },
           dataType: 'html'
         });
      
        // Tamanios del area visible
        // $(window).height() es el alto total de la pagina (no sirve para centrar)
        var viewportHeight = window.innerHeight ? window.innerHeight : $(window).height();
        var viewportWidth = window.innerWidth ? window.innerWidth : $(window).width();
        
        $.blockUI({
          message: $('#create_record'),
          css: {
            width: '500px',
            height: '280px',
            left: (viewportWidth - 500) /2 + 'px',
            top:  (viewportHeight - 280) /2 + 'px',
            padding: '10px',
            textAlign: 'left'
          },
          onOverlayClick: $.unblockUI
        });
      };
    
      $(document).ready(function() {
      
        // TODO: cuando se lanza crear un registro,
        //       abrir create.gsp en la modal de blockUI
        $('a.create').click( function(evt) {
          
          evt.preventDefault();
          
          showCreateRecord();
        });
        
        /* ===================================================================================== 
         * Calendars para filtros de compositions.
         */
        $("input[name=fromDate]").datepicker({
           // Icono para mostrar el calendar 
           showOn: "button",
           buttonImage: "${resource(dir:'images', file:'calendar.gif')}",
           buttonImageOnly: true,
           buttonText: 'pick a date',
           // Formato
           dateFormat: '${ApplicationHolder.application.config.app.l10n.date_format.replace("yyyy","yy").replace("MM","mm")}', // poner yy hace salir yyyy y MM hace salir el nombre del mes
           // Menus para cambiar mes y anio 
           changeMonth: true,
           changeYear: true,
           // Fecha maxima es la que esta seleccionada en toDate si la hay
           onClose: function( selectedDate ) {
             $( "input[name=toDate]" ).datepicker( "option", "minDate", selectedDate );
           }
        });
        $("input[name=toDate]").datepicker({
           // Icono para mostrar el calendar 
           showOn: "button",
           buttonImage: "${resource(dir:'images', file:'calendar.gif')}",
           buttonImageOnly: true,
           buttonText: 'pick a date',
           // Formato
           dateFormat: '${ApplicationHolder.application.config.app.l10n.date_format.replace("yyyy","yy").replace("MM","mm")}', // poner yy hace salir yyyy y MM hace salir el nombre del mes
           // Menus para cambiar mes y anio 
           changeMonth: true,
           changeYear: true,
           // Fecha minima es la que esta seleccionada en fromDate si la hay
           onClose: function( selectedDate ) {
             $( "input[name=fromDate]" ).datepicker( "option", "maxDate", selectedDate );
           }
        });
        // =====================================================================================
        
      });      
    </g:javascript>
  </head>
  <body>
    <h1><g:message code="episodio.list.title" /></h1>
    
    <%-- si hay un paciente seleccionado, muestro sus datos
    Ver issue #22
    --%>
    <g:if test="${session.ehrSession.patientId}">
      <g:message code="records.list.currentPatient" />:
      <g:set var="patient" value="${demographic.party.Person.get(session.ehrSession.patientId)}" />
      <g:render template="../demographic/Person" model="[person:patient]" />
    </g:if>
    
    <ul class="top_actions">
      <li>
        <g:link action="create" class="create"><g:message code="records.list.action.crearEpisodio" /></g:link>
      </li>
      <%--
	  Si hay un paciente en session, se pueden ver sus registros para el dominio actual o para cualquier dominio.
	  FIX: aunque no tenga paciente, quiero filtrar por dominio actual o por todos los dominios.
	  --%>
      <%-- <g:if test="${session.ehrSession.patientId}"> --%>
        <li ${(!params.anyDomain)?'class="active"':''}>
          <g:link action="list" class="list"><g:message code="records.list.action.currentDomain" args="[domain.Domain.get(session.ehrSession.domainId).name]" /></g:link>
        </li>
        <li ${(params.anyDomain)?'class="active"':''}>
          <g:link action="list" class="list" params="[anyDomain:true]"><g:message code="records.list.action.anyDomain" /></g:link>
        </li>
      <%-- </g:if> --%>
      <li>
        <g:form action="list" class="filter">
          Filtros:
          <input type="text" name="fromDate" placeholder="from date" readonly="readonly" value="${params.fromDate}" />
	       <input type="text" name="toDate" placeholder="to date" readonly="readonly" value="${params.toDate}" />
          <g:if test="${params.anyDomain}">
            <input type="hidden" name="anyDomain" value="true" />
          </g:if>
          <input type="submit" name="doit" value="filtrar" />
        </g:form>
      </li>
    </ul>
    
    <div id="create_record">
      <%-- Si llamo aca ejecuta create, si hay un doit en los params, me crea una composition...
      <g:include action="create" />
      --%>
    </div>
    
    <table id="list">
      <tr>
        <%-- <th><g:message code="records.list.label.id" /></th> --%>
        <%-- Muestra el paciente solo si no hay un paciente seleccionado --%>
        <g:if test="${!session.ehrSession.patientId}">
          <th><g:message code="records.list.label.patient" /></th>
        </g:if>
        <%-- Si se muestran registros de varios dominios muestro el dominio de cada registro --%>
        <g:if test="${params.anyDomain}">
          <th><g:message code="records.list.label.domain" /></th>
        </g:if>
        <th><g:message code="records.list.label.responsible" /></th>
        <th><g:message code="records.list.label.startTime" /></th>
        <th><g:message code="records.list.label.endTime" /></th>
        <th><g:message code="records.list.label.observations" /></th>
        <th><g:message code="records.list.label.state" /></th>
        <th><g:message code="records.list.label.actions" /></th>
      </tr>
      <g:each in="${compositions}" var="composition" status="i">
        <tr ${((i%2==0)?'class="odd"':'')}>
          <%-- <td>${composition.id}</td> --%>
          <%-- Muestra el paciente solo si no hay un paciente seleccionado --%>
          <g:if test="${!session.ehrSession.patientId}">
            <td><g:showPatientFromComposition composition="${composition}" /></td>
          </g:if>
          <%-- Si se muestran registros de varios dominios muestro el dominio de cada registro --%>
          <g:if test="${params.anyDomain}">
            <td>${composition.padre.name}</td>
          </g:if>
          <td><g:showCompositionComposer composition="${composition}" /></td>
          <td>
            <g:format date="${composition.startTime}" />
            <%-- FIXME: si el registro esta cerrado, mostrar el total de tiempo --%>
            (${util.DateDifference.numberOfMinutes(composition.startTime, new Date())} min.)
          </td>
          <td><g:format date="${composition.endTime}" /></td>
          <td>
            <%-- OJO: Solo funciona si el otherContext es ItemSingle y el value del Element es DvText --%>
            ${composition.context.otherContext.item.value.value}
          </td>
          <td>
            <%--
            // El .toString es por esto:
	         // Exception Message: No signature of method:
	         // org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource.getMessage()
	         // is applicable for argument types: (org.codehaus.groovy.grails.web.util.StreamCharBuffer, null,
	         // org.codehaus.groovy.grails.web.util.StreamCharBuffer, java.util.Locale) values:
	         // [ehr.lifecycle.incomplete, null, ehr.lifecycle.incomplete, es]
            --%>
            <g:message code="${g.stateForComposition(episodeId:composition.id).toString()}" />
          </td>
          <td>
            <%--
            si muestro registros de varios dominios, solo deberia hacer show
            de los registro ya cerrados. records.show depende del dominio
            seleccionado en sesion, si se quiere ver un registro de un dominio
            distinto al seleccionado, va a mostrar la estructura del dominio
            seleccionado sin ningun dato. ver si mostrar un registro de otro
            dominio puede hacerse con algun parametro a show o si debe hacerse
            otra accion.
            --%>
            <g:link action="show" id="${composition.id}"><g:message code="records.list.action.show" /></g:link>
            <br />
            <g:if test="${(g.stateForComposition(episodeId:composition.id) == Version.STATE_SIGNED)}">
              <g:set var="version" value="${Version.findByData(composition)}"/>
              <g:set var="archivoCDA" value="${new File(ApplicationHolder.application.config.hce.rutaDirCDAs + '\\' + version.nombreArchCDA)}"/>
              
              <%-- Deshabilito temporalmente la creacion de CDA desde GUI. (v0.8)
              <g:if test="${!archivoCDA.exists()}">
                <g:link controller="cda" action="create" id="${composition.id}">Crear CDA</g:link>
              </g:if>
              <g:else>
                <g:message code="Documento Clinico Creado" /> <!-- TODO i18n -->
              </g:else>
              --%>
            </g:if>
          </td>
        </tr>
      </g:each>
    </table>
    
    <g:paginate next="Siguiente" prev="Previo"
                maxsteps="5" max="${params.max}"
                controller="records" action="list"
                total="${total}"
                params="${params}" />
    
    
    <%--
     Lista de instrucciones para algun rol del usuario logueado y el dominio seleccionado.
     / Por ahora se muestra aca
    
     Terminar esto para v0.9
    --%>
    <g:include action="listInstructions" />
    
  </body>
</html>