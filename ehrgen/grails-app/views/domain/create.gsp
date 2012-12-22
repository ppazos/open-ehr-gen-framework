<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title><g:message code="domain.create.title" /></title>
    <style>
    .domain {
        width: 120px;
        min-height: 110px;
        margin-right: 20px;
        margin-bottom: 15px;
        text-align: center;
        display: inline-block;
        position: relative;
        vertical-align: top; /* BASELINE CORRECCIÓN*/
        /*border: 1px solid black;*/
    }
    .domain img {
      border: 0px;
    }
    .list {
      padding: 20px;
    }
    </style>
  </head>
  <body>
    <div class="nav">
	  <span class="menuButton">
        <g:link controller="domain" action="list" class="list"><g:message code="domain.list.title" /></g:link>
      </span>
      <span class="menuButton">
        <g:link controller="person" action="list" class="list"><g:message code="domain.list.action.personas" /></g:link>
      </span>
      <span class="menuButton">
        <g:link controller="role" action="list" class="list"><g:message code="domain.list.action.roles" /></g:link>
      </span>
    </div>
    
    <div class="body">
      <h1><g:message code="domain.create.title" /></h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="create">
	  
	    <%-- TODO: i18n --%>
	    <g:form url="[action:'create']">
	    
		  Nombre del dominio <input type="text" name="value" />
		  <%--
		  <br/>
		  C&oacute;digo identificador del dominio <input type="text" name="codeString" />
		  --%>
		  <br/><br/>
		
		  <input type="submit" name="doit" value="Crear dominio" />
	    </g:form>
		
      </div>
    </div>
  </body>
</html>