<%@ page import="tablasMaestras.TipoIdentificador" %>
<html>
  <head>
    <meta name="layout" content="ehr-modal" />
    <title><g:message code="demographic.edit.title" /></title>
    <style>
      label {
        display: block;
      }
    </style>
  </head>
  <body>
    <h1><g:message code="demographic.edit.title" /></h1>
    
    <g:if test="${flash.message}">
      <div style="color:red;">
        <g:message code="${flash.message}" />
      </div>
    </g:if>
    
    <g:form action="edit" id="${patient.id}">
      <label for="identificador">
        <g:message code="persona.identificador" />
      </label>
      
      <g:each in="${patient.ids}" var="pid">
      
        <g:set var="codigo" value="${TipoIdentificador.findByCodigo(pid.root)}" />

        ${pid.extension} ${( (codigo) ? codigo.nombreCorto : pid.root )}<br/>
      </g:each>
      
      <%-- No dejo cambiar los identificadores porque deberia lanzar un proceso que verifique donde hay referencias a lso identificadores que se eliminador o cambiaron por correccion.
      <g:textField name="extension" value="${params.identificador}" />
      <g:select name="root" from="${tiposIds}" optionKey="codigo" optionValue="nombreCorto" />
      --%>
      
      <label for="primerNombre">
        <g:message code="persona.primerNombre" />
      </label>
      <g:textField name="primerNombre" value="${pn.primerNombre}" />
      
      <label for="segundoNombre">
        <g:message code="persona.segundoNombre" />
      </label>
      <g:textField name="segundoNombre" value="${pn.segundoNombre}" />
      
      <label for="primerApellido">
        <g:message code="persona.primerApellido" />
      </label>
      <g:textField name="primerApellido" value="${pn.primerApellido}" />
      
      <label for="segundoApellido">
        <g:message code="persona.segundoApellido" />
      </label>
      <g:textField name="segundoApellido" value="${pn.segundoApellido}" />
      
      <label for="fechaNacimiento">
        <g:message code="persona.fechaNacimiento" />
      </label>
      <g:datePicker name="fechaNacimiento" value="${(patient.fechaNacimiento)? patient.fechaNacimiento : 'none'}" precision="day" noSelection="['':'']" />
      <br/>
      
      <label for="sexo">
        <g:message code="persona.sexo" />
      </label>
      <g:select name="sexo" noSelection="['':'']" from="['M', 'F']" value="${patient.sexo}" />
      
      <br/>
      <br/>
      <g:submitButton name="doit" value="${message(code:'demographic.edit.save')}" />
      
    </g:form>
  </body>
</html>