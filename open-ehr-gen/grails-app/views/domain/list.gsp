<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title><g:message code="domain.list.title" /></title>
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
        <g:link controller="person" action="list" class="list"><g:message code="domain.list.action.personas" /></g:link>
      </span>
      <span class="menuButton">
        <g:link controller="role" action="list" class="list"><g:message code="domain.list.action.roles" /></g:link>
      </span>
    </div>
    
    <div class="body">
      <h1><g:message code="domain.list.title" /></h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="list">
        <g:each in="${folders}" status="i" var="folder">
          <g:hasDomainPermit domain="${folder.name.definingCode.codeString}">
            <div class="domain">
              <g:link action="selectDomain" params="[path: folder.path]">
                <img src="${createLinkTo(dir: 'images', file: 'folder.png')}" /><br/>
                ${folder.name.value}
              </g:link>
            </div>
          </g:hasDomainPermit>
        </g:each>
      </div>
    </div>
  </body>
</html>