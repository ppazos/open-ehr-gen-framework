<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title><g:message code="template.create.title" /></title>
    <style>
      input[type=text] {
        width: 300px;
      }
      td.type {
        background-color: #ccccff;
        font-weight: bold;
      }
      td.select {
        text-align: center;
      }
      tr.slot {
        background-color: #f6f6f6;
      }
    </style>
    <g:javascript library="jquery-1.8.2.min" />
    <g:javascript library="jquery.tableFilter-1.0.0" />
    <g:javascript>
    $(document).ready(function() {
      
       $('input[name=archetype_filter]').tableFilter( $('#concepts'), 3 );
    });
    </g:javascript>
  </head>
  <body>
    <div class="nav">
      <%-- FIXME: acciones solo para administradores del sistema --%>
      <span class="menuButton">
        <g:link controller="domain" action="list" class="list"><g:message code="domain.list.title" /></g:link>
      </span>
    </div>
    <div class="body">
    
      <h1><g:message code="template.create.title" /></h1>
    
      <g:if test="${flash.message}">
        <div class="message"><g:message code="${flash.message}" /></div>
      </g:if>
     
      <%-- TODO: i18n --%>
	   <g:form url="[action:'createTemplate']">
	    
      Identificador:
      <br/>
      <input type="text" name="templateId" placeholder="triage_trauma" />
      <br/><br/>
      
      Nombre:
      <br/>
      <input type="text" name="name" placeholder="Evaluaciion de triage de trauma" />
      <br/><br/>
      
      Seleccion contenido ra&iacute;z:<br/><br/>
      
      Filtro por concepto: <input type="text" name="archetype_filter" id="archetype_filter" />
      
      <br/><br/>
      <g:set var="grouped" value="${archetypes.groupBy{it.type}}" />
      <table id="concepts">
        <tr>
          <th>type</th>
          <th>select</th>
          <th>name</th>
          <th>archetypeId</th>
        </tr>
        <g:each in="${grouped.keySet()}" var="type">
          <tr>
            <td colspan="4" class="type">${type}</td>
          </tr>
          <g:each in="${grouped[type]}" var="archetype">
            <tr>
              <td></td>
              <td class="select">
                <input type="radio" name="archetypeId" value="${archetype.archetypeId}" />
              </td>
              <td>${archetype.name}</td>
              <td>${archetype.archetypeId}</td>
            </tr>

              <g:each in="${archetype.slots}" var="slot_arch">
                <tr class="slot">
                  <td></td>
                  <td></td>
                  <td>|_ ${slot_arch.name}</td>
                  <td>${slot_arch.archetypeId}</td>
                </tr>
              </g:each>

          </g:each>
        </g:each>
      </table>
      <br/>
      
      <%--
      <g:each in="${grouped.keySet()}" var="type">
        <g:if test="${type == 'section'}">
          <g:each in="${grouped[type]}" var="archetype">
            ${archetype.name.padRight(60, ' .')} ${archetype.archetypeId}<br />
            <g:if test="${archetype.slots.size() > 0}">
              slots:<br/>
              <g:each in="${archetype.slots}" var="slot_arch">
                ${slot_arch.name.padRight(50, ' .')} ${slot_arch.archetypeId}<br/>
              </g:each>
              <hr/>        
            </g:if>
          </g:each>
        </g:if>
      </g:each>
      --%>
      
        <input type="submit" name="doit" value="Crear template" />
        <g:actionSubmit value="Cancelar" action="list" />
        
      </g:form>
    </div>
  </body>
</html>