<%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="converters.DateConverter" %>
<%
  // Formateador para las fechas
  SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
%>
<html>
  <head>
    <meta name="layout" content="ehr-modal" />
    <title><g:message code="demographic.lista_candidatos.title" /></title>
    <style>
      table #list {
        background-color: #ffffdd;
        width: 100%;
        font-size: 12px;
        border: 1px solid #000;
      }
      #list th {
        background-color: #ccccdd;
      }
      #list td {
        text-align: center;
      }
      .feedback {
        padding: 10px;
        border: 1px solid #ddddaa;
        background-color: #ffffcc;
      }
    </style>
  </head>
  <body>
    <h1><g:message code="demographic.lista_candidatos.title" /></h1>
    
    <ul class="top_actions">
      <g:if test="${session.traumaContext.episodioId}">
        <li><g:link controller="records" action="show" id="${session.traumaContext.episodioId}" class="home"><g:message code="demographic.lista_candidatos.action.backToEpisode" /></g:link></li>
      </g:if>
      <li>
        <% // Pasarle los parametros para que no tenga que vea lo que ya ha ingresado en la busqueda.
        
        def search_params = [:]
        
        def bd = DateConverter.dateFromParams( params, 'fechaNacimiento_' ) // Si no vienen todos los datos, que sea null
        
        if (bd)
        {
          def bdstring = format.format(bd)
          search_params['fechaNacimiento'] = bdstring
        }
        
        if (params.identificador) search_params['identificador'] = params.identificador
        if (params.root) search_params['root'] = params.root
        if (params.personName.primerNombre) search_params['personName.primerNombre'] = params.personName.primerNombre
        if (params.personName.segundoNombre) search_params['personName.segundoNombre'] = params.personName.segundoNombre
        if (params.personName.primerApellido) search_params['personName.primerApellido'] = params.personName.primerApellido
        if (params.personName.segundoApellido) search_params['personName.segundoApellido'] = params.personName.segundoApellido
        %>

        <g:link action="admisionPaciente" class="back" params="${search_params}"><g:message code="demographic.lista_candidatos.action.admisionPaciente" /></g:link>
      </li>
      <%-- Solo se puede agregar un nuevo paciente si el repositorio es local --%>
      <g:if test="${ApplicationHolder.application.config.hce.patient_administration.serviceType.local}">
        <li><g:link action="agregarPaciente" class="create"><g:message code="demographic.lista_candidatos.action.agregarPaciente" /></g:link></li>
      </g:if>
    </ul>

    <g:if test="${flash.message}">
      <div style="color:red;">
        <g:message code="${flash.message}" />
      </div>
    </g:if>
    
    <table id="list">
      <tr>
        <th><g:message code="persona.identificadores" /></th>
        <th><g:message code="persona.primerNombre" /></th>
        <th><g:message code="persona.segundoNombre" /></th>
        <th><g:message code="persona.primerApellido" /></th>
        <th><g:message code="persona.segundoApellido" /></th>
        <th><g:message code="persona.fechaNacimiento" /></th>
        <th><g:message code="persona.sexo" /></th>
        <th><g:message code="demographic.lista_candidatos.label.acciones" /></th>
      </tr>
      <g:if test="${candidatos.size()==0}">
        <tr>
          <td colspan="8">
            <div class="feedback">
              <g:message code="demographic.lista_candidatos.noHayCandidatos" />
            </div>
          </td>
        </tr>
      </g:if>
      <g:else>
	      <g:each in="${candidatos}" var="persona">
	        <tr>
	          <td><g:render template="UIDBasedID" collection="${persona.ids}" var="id" /></td>
	          <g:set var="name" value="${persona.identities.find{ it.purpose == 'PersonName'} }" />
	          <td>${name?.primerNombre}</td>
	          <td>${name?.segundoNombre}</td>
	          <td>${name?.primerApellido}</td>
	          <td>${name?.segundoApellido}</td>
	          <%-- TODO: taglib --%>
	          <td><g:if test="${persona.fechaNacimiento}">${format.format(persona.fechaNacimiento)}</g:if></td>
	          <td>${persona.sexo}</td>
	          <td>
	            <!-- Si la persona esta en la base tiene id pero si no (p.e. consulta a imp reomoto),
	                 necesito un identificador como cedula. -->
	            <%-- Si se usa la cedula u otro identificador y no se guarda la persona en la base local.
	            <g:set var="id" value="${persona.ids.toArray()[0]}" />
	            <g:if test="${id}">
	              <g:link action="seleccionarPaciente" params="[root:id.root, extension:id.extension]">
	                <g:message code="demographic.lista_candidatos.action.seleccionarPaciente" />
	              </g:link>
	            </g:if>
	            <g:else>
	              El paciente no tiene ningun identificador.
	            </g:else>
	            --%>
	            
	            <%-- Guardando al paciente en la base local, aunque el IMP sea remoto --%>
	            <g:link action="seleccionarPaciente" id="${persona.id}">
	              <g:message code="demographic.lista_candidatos.action.seleccionarPaciente" />
	            </g:link>
	          </td>
	        </tr>
	      </g:each>
	    </g:else>
    </table>
  </body>
</html>