<%@ page import="org.codehaus.groovy.grails.commons.*" %><?xml version="1.0" ?>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<g:set var="startmsec" value="${System.currentTimeMillis()}"/>
  <head>
    <title><g:layoutTitle default="Helpi" /></title>

    <%-- choose the style skin --%>
    <g:set var="skin" value="${'default'}"/>
    <g:if test="${session.building?.getSetting('staff.skin')}">
      <g:set var="skin" value="${session.building.getSetting('staff.skin')}"/>
    </g:if>
    <g:if test="${session.user?.getSetting('staff.skin')}">
      <g:set var="skin" value="${session.user.getSetting('staff.skin')}"/>
    </g:if>
    <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'style/skins/'+skin+'/staff',file:'skin.css')}"></link>
    <g:if test="${session.building.getSetting('path.style')}">
      <link rel="stylesheet" type="text/css" href="${session.building.getSetting('path.style')}/skins/${skin}/staff/skin.css"></link>
    </g:if>

    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    
    <%-- No quiero paginas cacheadas --%>
    <META HTTP-EQUIV="Pragma" CONTENT="no-cache" />
    <META HTTP-EQUIV="Expires" CONTENT="-1" />
    <META HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE" />
    
    <%-- Pruebas handheld --%>
    <meta HTTP-Equiv="topcommandarea" content="show" />
    <meta HTTP-Equiv="ReloadButton" content="show" />
    
    <g:layoutHead />
    <g:javascript library="application" />
  </head>
  <body>
    <div class="center">  
      <g:layoutBody />    
    </div>
  </body>	
<!-- gsp time (msecs): ${System.currentTimeMillis()-startmsec} -->
</html>
