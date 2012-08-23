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
        </style>
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />				
    </head>
    <body>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>	
        <div class="logo">
          <a href="http://code.google.com/p/open-ehr-gen-framework/" target="_blank"><img src="${resource(dir:'images', file:'ehr-gen_logo.png')}" alt="Open EHR-Gen Framework" /></a>
        </div>
        <g:layoutBody />		
    </body>	
</html>