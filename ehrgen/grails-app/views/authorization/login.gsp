<%@ page import="org.codehaus.groovy.grails.commons.*" %><%@ page import="org.codehaus.groovy.grails.commons.ApplicationHolder" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title><g:message code="auth.login.title" /></title>
    <g:javascript library="jquery-1.8.2.min" />
    <g:javascript>
    $(document).ready(function() {
      $("#user").focus();
    });
    </g:javascript>
    <link rel="stylesheet" href="${createLinkTo(dir:'css', file:'ehr.css')}" />
    <style>
      table {      
        border: 0px;
      }
      th {
        background: none;
        text-align: right;
        vertical-align: middle;
      }      

      * {margin:0;padding:0}
      html,body{height:100%; background-color:#efefef;}
      #wrapper{
        height:100%;
        width:100%;
        display:table;
        vertical-align:middle;
      }
      #outer {
        display:table-cell;
        vertical-align:middle;
      }
      #formwrap {
        position:relative;
        left:50%;
        float:left;
      }
      #form1 {
        border: 1px solid #000;
        padding: 20px 20px;
        margin-top: 5px;
        position: relative;
        text-align: center;
        left: -50%;
        background-color: #fff;
        -moz-box-shadow:    2px 3px 5px 1px #ccc;
        -webkit-box-shadow: 2px 3px 5px 1px #ccc;
        box-shadow:         2px 3px 5px 1px #ccc;
        -webkit-border-radius: 5px;
        -webkit-border-top-left-radius: 0;
        -moz-border-radius: 5px;
        -moz-border-radius-topleft: 0;
        border-radius: 5px;
        border-top-left-radius: 0;
      }
      #form1 table {
        width: 270px;
        margin: 20px;
        margin-bottom: 0px;
      }
      ul.userBar {
        position: relative;
        left: -50%;
        top: 1px;
      }
      ul.userBar li.active {
        position: relative;
        z-index: 9999;
      }
      ul.userBar li a {
        border-bottom: 0px;
      }
    
      p {margin:1em 0}
      input {
        position:relative;
      }
      td {
        text-align: right;
      }
      input[type=submit] {
        margin-top: 3px;
        padding: 3px 10px;
      }
      .error {
        /* TODO: meter icono de error ! */
        border: 1px solid #f00;
        background-color: #f99;
        padding: 2px;
        margin-bottom: 3px;
      }
      .error ul {
        list-style:none;
        margin:0;
        padding:0;
      }
      img {
        border: 0px;
      }
      .version {
        margin-top: 5px;
        margin-bottom: 10px;
        text-align: right;
        width: 50%;
      }
    </style>
    <!--[if lt IE 8]>
      <style type="text/css">
        #formwrap {top:50%}
        #form1 {top:-50%;}
      </style>
     <![endif]-->
     <!--[if IE 7]>
       <style type="text/css">
        #wrapper{
         position:relative;
         overflow:hidden;
        }
      </style>
    <![endif]-->
  </head>
  <body>     
    <div id="wrapper">
      <div id="outer">
        <div id="formwrap">
        
          <ul class="userBar lang">
            <g:langSelector>
              <li ${(session.locale.toString()==it.localeString)?'class="active"':''}>
                <a href="?sessionLang=${it.localeString}&templateId=${params.templateId}">${it.locale.getDisplayName(session.locale)}</a>
              </li>
            </g:langSelector>
          </ul>
        
          <g:form url="[action:'login']" method="post" id="form1">
            <a href="http://code.google.com/p/open-ehr-gen-framework/" target="_blank"><img src="${resource(dir:'images', file:'ehr-gen_logo.png')}" alt="Open EHR-Gen Framework" /></a>
            <h3 align="center"><g:message code="auth.login.welcome2" /></h3>
            <g:if test="${flash.message}">
              <div class="error"><g:message code="${flash.message}" /></div>
            </g:if>
            <table>
              <tr>
                <th><g:message code="auth.login.label.userid" /></th>
                <td><input type="text" id="user" name="user" size="24" /></td>
              </tr>
              <tr>
                <th><g:message code="auth.login.label.password" /></th>
                <td><input type="password" name="pass" size="24" /></td>
              </tr>
              <tr>
                <th></th>
                <td><input type="submit" name="doit" value="${message(code:'auth.login.action.signin')}" /></td>
              </tr>
            </table>
            <%-- TODO: recordar clave y permitir registrar al usuario.
            <div align="center">
              <g:link action="forgotPassword"><g:message code="auth.login.action.forgotPass" /></g:link>
            </div>
            --%>
          </g:form>
          
          <div class="version">v${ApplicationHolder.application.metadata['app.version']}</div>
          
        </div>
      </div>
    </div>
  </body>
</html>