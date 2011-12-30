<%--

Igual a la vista generarShow pero solo para la estructura del registro.

in: template
in: rmNode // este no se va a pasar porque se inyectan los valores por JS. TODO!!!

--%>

      <table class="container" cellpadding="0" cellspacing="3">
        <tr>
          <td colspan="2" id="content">
            <g:each in="${template.getArchetypesByZone('content')}" var="archRef">
              <g:if test="${index[archRef.id]}">
                <!-- FIXME: habria que arrancar del nodo que diga el template (p.e. esto es correcto si 
                            arranca de la raiz pero no si tiene un field con path distinta a "/"
                            http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=19
                -->
                <%-- Paths de los fields del archRef para los que se debe mostrar GUI
                 El cequeo podria ser una taglib
                 el chequeo para saber si mostrar el nodo se hace con: fieldPaths.find{ rmNode.path.startsWith(it.path)} != null
                --%>
                <g:set var="fieldPaths" value="${['/']}" />
                <g:if test="${archRef.fields?.size()>0}">
                  <g:set var="fieldPaths" value="${archRef.getFieldPaths()}" />
                </g:if>
                <g:set var="rmNode" value="${index[archRef.id]}" />
                <g:set var="templateName" value="${rmNode.getClassName()}" />
                <g:render template="../guiGen/showTemplates/${templateName}"
                          model="[rmNode:rmNode, fieldPaths:fieldPaths, archetype:archRef.getReferencedArchetype()]" />
              </g:if>
            </g:each>
          </td>
        </tr>
        <tr>
          <td id="left">
            <g:each in="${template.getArchetypesByZone('left')}" var="archRef">
              <g:if test="${index[archRef.id]}">
                <%-- Paths de los fields del archRef para los que se debe mostrar GUI
                 El cequeo podria ser una taglib
                 el chequeo para saber si mostrar el nodo se hace con: fieldPaths.find{ rmNode.path.startsWith(it.path)} != null
                --%>
                <g:set var="fieldPaths" value="${['/']}" />
                <g:if test="${archRef.fields?.size()>0}">
                  <g:set var="fieldPaths" value="${archRef.getFieldPaths()}" />
                </g:if>
                <g:set var="rmNode" value="${index[archRef.id]}" />
                <g:set var="templateName" value="${rmNode.getClassName()}" />
                <g:render template="../guiGen/showTemplates/${templateName}"
                          model="[rmNode:rmNode, fieldPaths:fieldPaths, archetype:archRef.getReferencedArchetype()]" />
              </g:if>
            </g:each>
          </td>
          <td id="right">
            <g:each in="${template.getArchetypesByZone('right')}" var="archRef">
              <g:if test="${index[archRef.id]}">
                <%-- Paths de los fields del archRef para los que se debe mostrar GUI
                 El cequeo podria ser una taglib
                 el chequeo para saber si mostrar el nodo se hace con: fieldPaths.find{ rmNode.path.startsWith(it.path)} != null
                --%>
                <g:set var="fieldPaths" value="${['/']}" />
                <g:if test="${archRef.fields?.size()>0}">
                  <g:set var="fieldPaths" value="${archRef.getFieldPaths()}" />
                </g:if>
                <g:set var="rmNode" value="${index[archRef.id]}" />
                <g:set var="templateName" value="${rmNode.getClassName()}" />
                <g:render template="../guiGen/showTemplates/${templateName}"
                          model="[rmNode:rmNode, fieldPaths:fieldPaths, archetype:archRef.getReferencedArchetype()]" />
              </g:if>
            </g:each>
          </td>
        </tr>
        <tr>
          <td colspan="2" id="bottom">
            <g:each in="${template.getArchetypesByZone('bottom')}" var="archRef">
              <g:if test="${index[archRef.id]}">
                <% /* Paths de los fields del archRef para los que se debe mostrar GUI
                 El cequeo podria ser una taglib
                 el chequeo para saber si mostrar el nodo se hace con: fieldPaths.find{ rmNode.path.startsWith(it.path)} != null
                */ %>
                <g:set var="fieldPaths" value="${['/']}" />
                <g:if test="${archRef.fields?.size()>0}">
                  <g:set var="fieldPaths" value="${archRef.getFieldPaths()}" />
                </g:if>
                <g:set var="rmNode" value="${index[archRef.id]}" />
                <g:set var="templateName" value="${rmNode.getClassName()}" />
                <g:render template="../guiGen/showTemplates/${templateName}"
                          model="[rmNode:rmNode, fieldPaths:fieldPaths, archetype:archRef.getReferencedArchetype()]" />
              </g:if>
            </g:each>
          </td>
        </tr>
      </table>