<%=packageName%>
<html>
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
      <meta name="layout" content="main" />
      <title>Template list</title>
   </head>
   <body>
      <div class="nav">
         <span class="menuButton"><g:link controller="domain" action="list" class="list">Dominios</g:link></span>
         <span class="menuButton"><g:link action="reload">Reload all</g:link></span>
         <span class="menuButton"><g:link action="unloadAll">Unload all</g:link></span>
      </div>
      <div class="body">
         <h1>Template list</h1>
         <g:if test="${flash.message}">
           <div class="message">${flash.message}</div>
         </g:if>
         <div class="list">
            <table>
              <thead>
               <tr>
                 <th>ID</th>
                 <th>NAME</th>
                 <th>UTILIZADO</th>
                 <th>ACCIONES</th>
               </tr>
              </thead>
              <tbody>
              <g:each in="${templateMap.values()}" status="i" var="template">
               <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                 <td>${template.templateId}</td>
                 <td>${template.name}</td>
                 <td>${lastUseList[template.templateId]}</td>
                 <td>
                  <g:link class="delete" action="unload" id="${template.templateId}">[bajar]</g:link>
                 </td>
               </tr>
              </g:each>
              </tbody>
            </table>
         </div>
      </div>
   </body>
</html>
