<%@ page import="util.DateDifference" %><%@ page import="java.text.SimpleDateFormat" %>

<%-- in: person:Person --%>

<%-- Muestra todos los ids
<g:render template="../demographic/UIDBasedID" collection="${person.ids}" var="id" />
--%>
<%-- Solo muestra un id --%>
<g:render template="../demographic/UIDBasedID" model="[id:person.ids[0]]" />

<b>
${person.primerNombre}
${person.segundoNombre}
${person.primerApellido}
${person.segundoApellido}
( ${person.sexo} )
</b>

<%
if (person.fechaNacimiento)
{
  def myFormatter = new SimpleDateFormat( "yyyy-MM-dd" )
  print myFormatter.format(person.fechaNacimiento)
  
  print " ( " + DateDifference.numberOfYears(person.fechaNacimiento, new Date()) + " )"
}
%>
