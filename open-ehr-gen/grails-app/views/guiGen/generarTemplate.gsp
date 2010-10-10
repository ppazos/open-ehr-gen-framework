<?xml version="1.0" encoding="ISO-8859-1" ?>
<html>
  <head>
    <meta name="layout" content="ehr" />
    <style>
    #content, #left, #right, #bottom {
      vertical-align: top;
    }
    #left {
      width: 50%;
    }
    #right {
      width: 50%;
    }
    
    .OBSERVATION .label, .EVALUATION .label, .INSTRUCTION .label, .ACTION .label {
      font-size: 14px;
      font-weight: bold;
      background-color: #e0e0e0;
      padding: 3px;
      /* padding-left: 6px; */ /* Ahora todo alineado a la izq sin identacion */
    }

<%--
    .OBSERVATION, .EVALUATION {
      width: 50%;
      /* border: 1px solid #ffff00; */
      float: left;
      background-color: #ffdddd;
    }
    .INSTRUCTION, .ACTION {
      width: 50%;
      /* border: 1px solid #ffff00; */
      float: right;
      background-color: #ddddff;
    }
--%>

    .CLUSTER {
      /*margin-left: 10px;*/ /* Ahora todo alineado a la izq sin identacion */
      margin-bottom: 3px;
      font-weight: bold;
      background-color: #bbeeff;
      /*padding: 3px;*/
      border: 1px solid #9fcfff;
    }    
    .CLUSTER .label {
      padding: 3px;
      /*padding-left: 6px;*/ /* Ahora todo alineado a la izq sin identacion */
      background-color: #9fcfff;
      font-size: 13px;
    }
    .CLUSTER .content {
      padding: 3px;
      display: block;
    }
    
    .ELEMENT {
      /*margin-left: 10px;*/  /* Ahora todo alineado a la izq sin identacion */
      font-weight: normal;
      margin-bottom: 3px;
      /*padding: 3px;*/
      /*background-color: #ffaa99;
      border: 1px solid #ff3333;*/
      background-color: #ffff99;
      border: 1px solid #cccc00;
    }
    .ELEMENT .label {
      padding: 3px;
      /*padding-left: 6px;*/
      /*margin-right: 5px;*/
      background-color: #eeee99;
      font-size: 12px;
    }
    .ELEMENT .content {
      padding: 3px;
      display: block;
      overflow: auto;
    }
    .ELEMENT img {
      max-width: 385px;
    }

    .label {
      display: block;
      margin-bottom: 2px;
    }

    /*******************************************************/
    /* Para los boolean que el SI NO tenga el mismo largo. */
    label input {
      vertical-align: middle;
    }
    label span {
      /*background-color: #00ff00;*/ /* Para distinguir a que se aplica, lo muestra verde. */
      width: 30px;
      height: 22px;
      text-align: right;
      display: inline-block;
    }
    label:hover {
      background-color: #ddddff;
    }
    /* / Para los boolean que el SI NO tenga el mismo largo. */
    /*********************************************************/

    .active {
      /*font-weight: bold;*/
    }
    .multiple {
      text-align: right;
      padding: 5px;
    }
    .slot {
      border: 2px solid red;
      padding: 2px;
    }
    .archetype {
      /* background-color: #ddddff; */
      padding: 10px;
      border: 1px solid #dddd80;
    }
    .field {
      padding: 10px;
      border: 1px solid #80dddd;
    }
    
    
    /* --- NAVBAR ----------------------------- */ 
  #navbar
  {          
    width: 100%;
    /*padding-top: 5px;*/
    line-height: normal;
    font-size: 12px;
    margin-top: 0px;
    clear: both;
    /*background: #e0e0e0;*/
    text-align: left;
  }
  
  #navbar a {
     padding: 0px;
     text-decoration: none;
     color: #000;
     padding: 4px 10px 2px 10px;
     width: 100%;
  }
  
  #navbar ul {
     margin: 0;
     padding: 5px 0px 2px 20px;
     list-style: none;
     /*border-bottom: solid 1px #bbb;*/
  }
  
  #navbar li {
     margin-right: 1px;
     padding: 4px 0px 2px 0px;
     display: inline;
     color: #666;
     border: solid 1px #000000;
     border-bottom: 1px solid #000000;
     background-color: #efefef;
  }
  
  #navbar li.active {
      border: 1px solid #000000;
      border-bottom: 1px solid #ffffdd;
      /*font-weight: bold;*/
      background-color: #ffffdd;
  }
    </style>
  </head>
  <body>    
    <%-- ${archetypeList} --%>
    
    <%-- SUBMENU DE SECCIONES SI EXISTEn --%>
    <g:if test="${subsections.size()>1}">
      <div id="navbar">
        <ul>
          <g:each in="${subsections}" var="subsection">
            <li ${((template.id==subsection)?'class="active"':'')}>
	          <g:hasContentItemForTemplate episodeId="${episodeId}" templateId="${subsection}">
	            <g:if test="${it.hasItem}">
	              <g:link controller="guiGen" action="generarShow" id="${it.itemId}"><g:message code="${'section.'+subsection}" /> (*)</g:link>
	            </g:if>
	            <g:else>
		          <g:link controller="guiGen" action="generarTemplate" params="[templateId:subsection]">
		            <g:message code="${'section.'+subsection}" />
		          </g:link>
		        </g:else>
	          </g:hasContentItemForTemplate>
	        </li>
          </g:each>
        </ul>
      </div>
    </g:if>
    
    <g:form action="save" class="ehrform" method="post" enctype="multipart/form-data">
    
      <input type="hidden" name="templateId" value="${template.id}" />
	  <!-- TODO: sacar, es solo para test -->
      <%-- TemplateId: ${template.id}<br/> --%>
      <table class="container" cellpadding="0" cellspacing="3">
        <tr>
          <td colspan="2" id="content">
            <g:each in="${template.getArchetypesByZone('content')}" var="archRef">
              <g:each in="${archRef.getReferencedConstraints()}" var="node">
                <g:set var="strclass" value='${node.getClass().getSimpleName()}'/>
                <g:set var="templateName" value="${strclass[0].toLowerCase()+strclass.substring(1)}" />
                <g:render template="templates2/${templateName}"
                          model="[(templateName): node,
                                  archetype: archRef.getReferencedArchetype(),
                                  archetypeService: archetypeService,
                                  params: params]" />
              </g:each>
            </g:each>
          </td>
        </tr>
        <tr>
          <td id="left">
            <g:each in="${template.getArchetypesByZone('left')}" var="archRef">
              <g:each in="${archRef.getReferencedConstraints()}" var="node">
                <g:set var="strclass" value='${node.getClass().getSimpleName()}'/>
                <g:set var="templateName" value="${strclass[0].toLowerCase()+strclass.substring(1)}" />
                <g:render template="templates2/${templateName}"
                          model="[(templateName): node,
                                  archetype: archRef.getReferencedArchetype(),
                                  archetypeService: archetypeService,
                                  params: params]" />
              </g:each>
            </g:each>
          </td>
          <td id="right">
            <g:each in="${template.getArchetypesByZone('right')}" var="archRef">
              <g:each in="${archRef.getReferencedConstraints()}" var="node">
                <g:set var="strclass" value='${node.getClass().getSimpleName()}'/>
                <g:set var="templateName" value="${strclass[0].toLowerCase()+strclass.substring(1)}" />
                <g:render template="templates2/${templateName}"
                          model="[(templateName): node,
                                  archetype: archRef.getReferencedArchetype(),
                                  archetypeService: archetypeService,
                                  params: params]" />
              </g:each>
            </g:each>
          </td>
        </tr>
        <tr>
          <td colspan="2" id="bottom">
            <g:each in="${template.getArchetypesByZone('bottom')}" var="archRef">
              <g:each in="${archRef.getReferencedConstraints()}" var="node">
                <g:set var="strclass" value='${node.getClass().getSimpleName()}'/>
                <g:set var="templateName" value="${strclass[0].toLowerCase()+strclass.substring(1)}" />
                <g:render template="templates2/${templateName}"
                          model="[(templateName): node,
                                  archetype: archRef.getReferencedArchetype(),
                                  archetypeService: archetypeService,
                                  params: params]" />
              </g:each>
            </g:each>
          </td>
        </tr>
      </table>
      
      <br/>
      
      <div class="bottom_actions">
        <g:submitButton name="doit" value="Guardar" />
      </div>
    </g:form>
  </body>
</html>