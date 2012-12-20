<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><g:message code="archetype.upload.title" /></title>
    <style>
    </style>
  </head>
  <body>
    <h1><g:message code="archetype.upload.title" /></h1>
    
    <g:if test="${flash.message}">
      <div class="message">${flash.message}</div>
    </g:if>
    
    <g:form action="uploadArchetype" method="post" enctype="multipart/form-data">

      ADL archetype:
      <input type="file" name="adl" />
      <br/><br/>
         
      <div id="bottom_actions">
        <g:submitButton name="doit" value="${message(code:'archetype.upload.action')}" />
      </div>
    </g:form>
  </body>
</html>