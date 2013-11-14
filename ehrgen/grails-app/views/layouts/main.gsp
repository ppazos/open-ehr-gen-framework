<html>
  <head>
    <title><g:layoutTitle default="Grails" /></title>
    <style>
    .logo {
       padding: 5px;
       padding-bottom: 0px;
    }
    .logo img {
       border: 0px;
    }
	.user_info {
	  display: inline-block;
	  float: right;
	}
    div.wrapper {
      position:relative;
      height: 100%;
      width: 100%;
    }
    </style>
    <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
    <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
    <g:javascript library="application" />
    <g:javascript library="jquery" plugin="jquery" />
    <g:layoutHead />
    <r:layoutResources/>
  </head>
  <body>
    <!-- para hacer el height to fit the page height -->
    <div class="wrapper">
      <div id="spinner" class="spinner" style="display:none;">
        <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
      </div>	
      <div class="logo">
        <a href="http://code.google.com/p/open-ehr-gen-framework/" target="_blank"><img src="${resource(dir:'images', file:'ehr-gen_logo.png')}" alt="Open EHR-Gen Framework" /></a>
        <div class="user_info">
           ${session.ehrSession.userData}
          <g:link controller="authorization" action="logout"><g:message code="authorization.action.logout" /></g:link>
        </div>
      </div>
      <g:layoutBody />
      <r:layoutResources/>
    </div>
  </body>	
</html>