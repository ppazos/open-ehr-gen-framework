<html>
  <head>
    <meta name="layout" content="ehr-modal" />
    <title><g:message code="demographic.agregar_paciente.title" /></title>
    <style>
      label {
        display: block;
      }
    </style>
  </head>
  <body>
    <h1><g:message code="demographic.agregar_paciente.title" /></h1>
    
    <g:if test="${flash.message}">
      <div style="color:red;">
        <g:message code="${flash.message}" />
      </div>
    </g:if>
    
    <g:form action="agregarPaciente">
      <label for="identificador">
        <g:message code="persona.identificador" />
      </label>
      <g:textField name="extension" value="${params.identificador}" />
      <g:select name="root" from="${tiposIds}" optionKey="codigo" optionValue="nombreCorto" />
      
      <label for="primerNombre">
        <g:message code="persona.primerNombre" />
      </label>
      <g:textField name="primerNombre" value="${params.primerNombre}" />
      
      <label for="segundoNombre">
        <g:message code="persona.segundoNombre" />
      </label>
      <g:textField name="segundoNombre" value="${params.segundoNombre}" />
      
      <label for="primerApellido">
        <g:message code="persona.primerApellido" />
      </label>
      <g:textField name="primerApellido" value="${params.primerApellido}" />
      
      <label for="segundoApellido">
        <g:message code="persona.segundoApellido" />
      </label>
      <g:textField name="segundoApellido" value="${params.segundoApellido}" />
      
      <label for="fechaNacimiento">
        <g:message code="persona.fechaNacimiento" />
      </label>
      <g:datePicker name="fechaNacimiento" value="none" precision="day" noSelection="['':'']" />
      <br/>
      
      <label for="sexo">
        <g:message code="persona.sexo" />
      </label>
      <g:select name="sexo" noSelection="['':'']" from="['M', 'F']" value="${params.sexo}" />
      <br/>
      <br/>
      
      <g:submitButton name="doit" value="${message(code:'demographic.agregar_paciente.agregar')}" />
      <g:link action="admisionPaciente"><g:message code="demographic.lista_candidatos.action.admisionPaciente" /></g:link>
    </g:form>
  </body>
</html>