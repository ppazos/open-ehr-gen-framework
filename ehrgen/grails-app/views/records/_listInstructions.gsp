

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
        <th><g:message code="records.list.label.id" /></th>
        <th><g:message code="records.list.label.patient" /></th>
        <th><g:message code="records.list.label.responsible" /></th>
        <th><g:message code="records.list.label.startTime" /></th>
        <th><g:message code="records.list.label.observations" /></th>
        <th><g:message code="records.list.label.state" /></th>
        <th><g:message code="records.list.label.actions" /></th>
        <th><g:message code="trauma.list.label.path" /></th>
      </tr>
      <g:each in="${instructions}" var="instruction">
        <tr>
          <td>${instruction.id}</td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td>${instruction.state}</td>
          <td>
          ${session.locale}<br/>
          
          <!--
          consultar el arquetipo de action para ver que puedo hacer desde el estado actual de la instruction...
          -->
          
          <%--
          <g:each in="${transitions[instruction.id]}" var="next_state_code">
            <g:displayTerm code="${next_state_code}" locale="${session.locale}" terminologyId="openehr" />
            <br/>
          </g:each>
          --%>
          
          Deberia mostrar el careflow step en lugar del state, al usuario le interesa
          ejecutar una accion, no saber el estado tecnico de la activity luego de ejecutarla.
          
          <%-- Map <instExec.id , Maps<archId, List<careflow step>>> --%>
          <g:set var="careflow_step_data" value="${activityActions[instruction.id]}" />
          <g:each in="${careflow_step_data}" var="arch_id_careflow_steps">
            <g:each in="${arch_id_careflow_steps['careflow_steps']}" var="careflow_step">
              <%--
              <g:link action="recordAction" params="[archetypeId: arch_id_careflow_steps.archetype.archetypeId.value, instructionId: instruction.id, careflowStep: careflow_step]">
              --%>
              <g:link action="recordAction" params="[instructionExecId: instruction.id, careflowStep: careflow_step]">
                <g:displayTerm code="${careflow_step}" locale="${session.locale}" archetype="${arch_id_careflow_steps.archetype}" />
              </g:link>
              <br/>
            </g:each>
          </g:each>
          
          </td>
          <td>
          mostrar las acciones ejecutadas hasta el momento como una serie de pasos en un camino A &gt; B &gt; C
          </td>
        </tr>
      </g:each>
    </table>