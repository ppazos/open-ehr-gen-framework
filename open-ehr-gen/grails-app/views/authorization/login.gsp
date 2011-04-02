<%@ page import="org.codehaus.groovy.grails.commons.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title><g:message code="auth.login.title" /></title>
    <g:javascript library="prototype/prototype" />
    <g:javascript>
      <!--
        Event.observe(window, 'load', function(event) {
        
          // Focus en input nombre de usuario
          $('user').focus();
          
        });
      -->
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

      #sheetbar {
        text-align:left;
      }

      #sheet {
        text-align:left;
        width:30em;
      }

      * {margin:0;padding:0}
      html,body{height:100%}
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
      #form1 table {
        width: 270px;
        margin: 20px;
        margin-bottom: 0px;
      }
      #form1 {
        border: 1px solid #000;
        padding: 20px 20px;
        margin-top: 5px;
        position: relative;
        text-align: center;
        left: -50%;
        /*background-color: #aaaaff;*/
      }
      p {margin:1em 0}
      input {
        position:relative;
      }
      td {
        text-align: right;
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

     <%--
     <div id="sheetbar">
       <ul>
         <li ${(session.locale.getLanguage()=='en')?'class="active"':''}>
           <a href="?sessionLang=en"><g:message code="common.lang.en" /></a>
         </li>
         <li ${(session.locale.getLanguage()=='es')?'class="active"':''}>
           <a href="?sessionLang=es"><g:message code="common.lang.es" /></a>
         </li>
       </ul>
     </div>
     --%>
     
    <div id="wrapper">
      <div id="outer">
        <div id="formwrap">
        
          <ul class="userBar">
            <g:langSelector>
              <li ${(session.locale.getLanguage()==it)?'class="active"':''}>
                <a href="?sessionLang=${it}&templateId=${params.templateId}"><g:message code="common.lang.${it}" /></a>
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
                <td>
                  <input type="submit" name="doit" value="${message(code:'auth.login.action.signin')}" />
                </td>
              </tr>
            </table>
            <%-- TODO: recordar clave
            <div align="center">
              <g:link action="forgotPassword"><g:message code="auth.login.action.forgotPass" /></g:link>
            </div>
            --%>
          </g:form>
        </div>
      </div>
    </div>
  </body>
</html>
