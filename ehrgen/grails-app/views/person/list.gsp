<%@ page import="util.DateDifference" %><%@ page import="java.text.SimpleDateFormat" %><%@ page import="tablasMaestras.TipoIdentificador" %><%@ page import="demographic.role.Role" %>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <meta name="layout" content="main" />
    <title>Person list</title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'ehr.css')}" />
    <style>
      td {
        vertical-align: middle;
      }
      .nav form {
        display: inline;
        padding: 0px;
        margin: 0px;
      }
      .nav select {
        padding: 0px;
        margin: 0px;
        font-size: 10px;
      }
      .person_id {
        font-weight: bold;
      }
    </style>
  </head>
  <body>
    <div class="nav">
      <span class="menuButton">
		<g:link controller="domain" class="home"><g:message code="default.home.label"/></g:link>
      </span>
      <span class="menuButton">
        <g:link action="create" class="create">Crear</g:link>
      </span>
      
      <span class="menuButton">
	    <g:form action="list">
	      todos menos... <input type="checkbox" name="neg" ${((params.neg)?'checked="true"':'')} onchange="javascript:this.parentNode.submit();" />
	      <g:select name="type" from="${Role.list().type}" value="${params.type}" noSelection="${['':'Rol...']}" onchange="javascript:this.parentNode.submit();" /> 
	    </g:form>
      </span>
      <!--
      TODO: busquedas
      TODO: crear personas
      -->
    </div>
    <div class="body">
      <h1>Person list</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="list">
        <table>
          <thead>
	        <tr>
	          <th>IDs</th>
	          <th>Identidad</th>
	          <th>Nacimiento</th>
	          <th>Roles</th>
	          <th>ACCIONES</th>
	        </tr>
          </thead>
          <tbody>
	        <g:each in="${persons}" status="i" var="person">
	          <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	            <td>
	              <table>
	                <g:each in="${person.ids}" status="j" var="id">
	                  <tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
	                  	<td>
	                  	  <span class="person_id">${id.extension}</span>
	                  	  <g:set var="codigo" value="${TipoIdentificador.findByCodigo(id.root)}" />
	                  	  (${((codigo) ? codigo.nombreCorto : id.root)})
	                  	</td>                  
	                  </tr>
	                </g:each>
	              </table>
	            </td>
	            <td>
	              ${person.primerNombre}
				  ${person.segundoNombre}
				  ${person.primerApellido}
				  ${person.segundoApellido}
	            </td>
	            <td>
	              <% // muestra fecha de nacimiento y edad
					if (person.fechaNacimiento)
					{
					  def myFormatter = new SimpleDateFormat( "yyyy-MM-dd" ) // FIXME: el formato depende del locale
					  print myFormatter.format(person.fechaNacimiento)
					  print " ( " + DateDifference.numberOfYears(person.fechaNacimiento, new Date()) + " )"
					}
				  %>            
	            </td>
	            <td>
	              <table>
	                <g:each in="${person.roles}" status="k" var="rval">
	                  <tr class="${(k % 2) == 0 ? 'odd' : 'even'}">
	                  	<td>${rval.role.type}</td>                  
	                  </tr>
	                </g:each>
	              </table>
	            </td>
	            <td>

			        <g:link action="show" class="show" id="${person.id}"><g:message code="person.show" /></g:link>

	            </td>
	          </tr>
	        </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </body>
</html>