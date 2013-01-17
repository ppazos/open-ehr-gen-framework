<g:set var="formatter" value="${new java.text.SimpleDateFormat('yyyy-MM-dd')}" />
<g:each in="${admissions}" status="i" var="admission">
  <%-- TODO: usar un template, ver views/demographic/_Person --%>
  <g:set var="person" value="${admission.patient}" />

  <div class="patient_data">
    <g:set var="domain" value="${admission.domain}" />
    
    <%-- La class se usa para seleccionar el form del domain en domain/list.gsp
         que se submitea por javascript al hacer click en el link aqui --%>
    
    <g:link url="[action:'list']" id="${person.id}" class="admission domain_${domain.id}">
    ${person.primerNombre}
    ${person.segundoNombre}
    ${person.primerApellido}
    ${person.segundoApellido}
    ( ${person.sexo} )
    <%
    if (person.fechaNacimiento)
    {
      print formatter.format(person.fechaNacimiento)
    }
    %>
    
    [${domain.name}]
    </g:link>
  </div>
</g:each>

<g:javascript>
  $(document).ready(function() {
  
    $('.patient_data a').click(function(evt) {
    
      evt.preventDefault();
      
      //alert(this.id);
      
      // Tengo que submitear el form del dominio correspondiente en domain/list.gsp
      // que es donde se incluye este template, pero agregandole la info del paciente.
      
      // Selecciona el form de selectDomain correspondiente al dominio de la admision
      // El form tiene class igual a domain_$domainId
      var form = $('form.' + $(this).attr('class').replace('admission ', '') );
      
      // Si voy para atras luego de seleccionar un paciente, en Forefox el DOM sigue
      // teniendo el input hidden. Si selecciono otro paciente van 2 ids al servidor.
      // Con esto remuevo los inputs que hayan, y aseguro que siempre va un solo id.
      $('input[name=patientId]', form).remove();
      
      form.append( '<input type="hidden" name="patientId" value="'+ this.id +'" />' );
      
      //console.log(form);
      
      // submit!
      form.submit();
    });
    
  });
</g:javascript>