<h1>Nueva admisi&oacute;n</h1>

<g:form action="admissionCreate">
  
  Paciente:<br/>
  <%-- FIXME: hacer busqueda de pacientes --%>
  <select name="patientId">
    <g:each in="${patients}" var="patient">
      <option value="${patient.id}">${patient.primerNombre} ${patient.primerApellido}</option>
    </g:each>
  </select>
  <br/><br/>
  
  Medico:<br/>
  <%-- FIXME: hacer busqueda de medicos --%>
  <select name="physicianId">
    <g:each in="${physicians}" var="physician">
      <option value="${physician.id}">${physician.primerNombre} ${physician.primerApellido}</option>
    </g:each>
  </select>
  <br/><br/>
  
  Dominio:<br/>
  <%-- FIXME: hacer busqueda de medicos --%>
  <select name="domainId">
    <g:each in="${domains}" var="domain">
      <option value="${domain.id}">${domain.name}</option>
    </g:each>
  </select>
  <br/><br/>
  
  <input type="submit" name="doit" value="Guardar" />
  
</g:form>