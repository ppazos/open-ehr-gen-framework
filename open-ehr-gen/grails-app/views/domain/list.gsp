<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main" />
    <title>Domain list</title>
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
    <%--
    <div class="nav">
      <g:link action="unloadAll">Unload all</g:link>
    </div>
    --%>
    <div class="body">
      <h1>Listado de dominios</h1>
      <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
      </g:if>
      <div class="list">
        <%--
        <g:each in="${domains}" status="i" var="domain">
          <div class="domain">
            <g:link action="selectDomain">
              <img src="${createLinkTo(dir: 'images', file: 'folder.png')}" /><br/>
              ${message(code: domain)}
            </g:link>
          </div>
        </g:each>
        --%>
        <g:each in="${folders}" status="i" var="folder">
          <div class="domain">
            <g:link action="selectDomain" params="[path: folder.path]">
              <img src="${createLinkTo(dir: 'images', file: 'folder.png')}" /><br/>
              ${folder.name.value}
            </g:link>
          </div>
        </g:each>
      </div>
    </div>
  </body>
</html>
