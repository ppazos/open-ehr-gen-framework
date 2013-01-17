

    <h1>Instrucciones</h1>
    
    Listado de instrucciones filtrados por su rol.
    
    <ul>
      <li>Ver los indices de instruction que tienen el rol del usuario en sus instructionRoles</li>
      <li>Obtener los registros de instrucciones para esos arquetipos</li>
      <li>Ver el estado de cada instruccion (necesito crear una estructura para mantener los estados)</li>
      <li>Deberia poder realizar acciones dependiendo del estado actual, del ACTION archetype referenciado por la ACTIVITY y de las transiciones que marca el arquetipo de ACTION</li>
      <li>Al ejecutar una accion se deberia crear una nueva COMPOSITION y se deberia actualizar el estado de la instruction/activity en la estructura que mantiene los estados.</li>
    </ul>
    
    <g:set var="instructions" value="${instructionExecs}" />
    <table id="list">
      <tr>
        <th><g:message code="trauma.list.label.id" /></th>
        <th><g:message code="trauma.list.label.patient" /></th>
        <th><g:message code="trauma.list.label.responsible" /></th>
        <th><g:message code="trauma.list.label.startTime" /></th>
        <th><g:message code="trauma.list.label.observations" /></th>
        <th><g:message code="trauma.list.label.state" /></th>
        <th><g:message code="trauma.list.label.actions" /></th>
      </tr>
      <g:each in="${instructions}" var="instruction">
        <tr>
          <td>${instruction.id}</td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td>${instruction.state}</td>
          <td>consultar el arquetipo de action para ver que puedo hacer desde el estado actual de la instruction...</td>
        </tr>
      </g:each>
    </table>