<%@ page import="util.DateDifference" %><%@ page import="java.text.SimpleDateFormat" %>

<%-- in: person:Person --%>

<g:render template="../demographic/UIDBasedID" collection="${person.ids}" var="id" />

${person.primerNombre}
${person.segundoNombre}
${person.primerApellido}
${person.segundoApellido}
( ${person.sexo} )
<br/>

<%--
${person.fechaNacimiento.getClass()}
--%>

<%
if (person.fechaNacimiento)
{
  def myFormatter = new SimpleDateFormat( "yyyy-MM-dd" )
  print myFormatter.format(person.fechaNacimiento)
  
  print " ( " + DateDifference.numberOfYears(person.fechaNacimiento, new Date()) + " )"
}
%>
