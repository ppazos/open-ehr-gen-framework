package util.terminology

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

/*
http://www.itserver.es/ITServer/rest/snomedcore/lang/en/suggestSNOMEDCTElements/cholec

<wordsIndices>
   <wordsIndex>
      <word>Cholecystogram</word>
   </wordsIndex>
   <wordsIndex>
      <word>Cholecystotomy</word>
   </wordsIndex>
   <wordsIndex>
      <word>Cholecystopexy</word>
   </wordsIndex>
   <wordsIndex>
      <word>Cholecalciferol</word>
   </wordsIndex>
   <wordsIndex>
      <word>Cholecystokinin</word>
   </wordsIndex>
</wordsIndices>


http://www.itserver.es/ITServer/rest/snomedcore/lang/en/searchInSnomed/termToSearch/Cholecystitis/numberOfElements/100

<sctDescriptionss>
   <description>
      <conceptid>76581006</conceptid>
      <descriptionid>127173011</descriptionid>
      <descriptionstatus>0</descriptionstatus>
      <descriptiontype>1</descriptiontype>
      <inititalcapitalstatus>0</inititalcapitalstatus>
      <languagecode>en</languagecode>
      <sourceName>Core</sourceName>
      <term>Cholecystitis</term>
   </description>
   <description>
      <conceptid>65275009</conceptid>
      <descriptionid>108454010</descriptionid>
      <descriptionstatus>0</descriptionstatus>
      <descriptiontype>1</descriptiontype>
      <inititalcapitalstatus>0</inititalcapitalstatus>
      <languagecode>en</languagecode>
      <sourceName>Core</sourceName>
      <term>Acute cholecystitis</term>
   </description>
   ...
</sctDescriptionss>
*/

class SnomedITServerAccess implements ITerminologyAccess {

   /**
    * Sugiere terminos conocidos a partir de un texto parcial.
    * El resultado puede tener codigos asociados o ser solo textos.
    */
   public List<Term> suggestTerms(String text)
   {
   }
   
   public List<Term> suggestTerms(String text, String language)
   {
      def res = []
      
      def http = new HTTPBuilder('http://www.itserver.es/ITServer/rest/snomedcore/lang/en/searchInSnomed/termToSearch/Cholecystitis/numberOfElements/100') // http://twitter.com')
      http.auth.basic 'ppazos', 'pablo1'
      http.request( GET, XML ) { req ->

         headers.'User-Agent' = 'Mozilla/5.0 Ubuntu/8.10 Firefox/3.0.4'

         // response handler for a success response code:
         response.success = { resp, xml ->
          
            // response class groovyx.net.http.HttpResponseDecorator
            
            //println req // org.apache.http.client.methods.HttpGet
            //println xstream.toXML(req)
            println resp.statusLine // Ok 200
            println xml.text() // html es java.io.InputStreamReader
            
            println xml.toXmlString()
            /*
            <?xml version='1.0'?>
            <sctDescriptionss>
               <description>
                  <conceptid>76581006</conceptid>
                  <descriptionid>127173011</descriptionid>
                  <descriptionstatus>0</descriptionstatus>
                  <descriptiontype>1</descriptiontype>
                  <inititalcapitalstatus>0</inititalcapitalstatus>
                  <languagecode>en</languagecode>
                  <sourceName>Core</sourceName>
                  <term>Cholecystitis</term>
               </description>
               ..
             ..
            */
            
            
      
            codigos.each { cie10 ->
            
               // Sino tiene codigo, es un agrupador, se usa subgrupo como codigo.
               if (cie10.codigo)
                  res << new Term(text: cie10.nombre, language:'es', code:cie10.codigo, terminologyId:'CIE10', qualifier:'final')
               else
                  res << new Term(text: cie10.nombre, language:'es', code:cie10.subgrupo, terminologyId:'CIE10', qualifier:'container')
            }
            
            
         }

         // handler for any failure status code:
         response.failure = { resp ->
            println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
         }
      }
      
      return res
   }

   /**
    * Busca terminos codificados, segun un texto, dentro de una terminologia, pudiendo restringir a buscar solo en un subset.
    */
   public List<Term> findCandidates( String text, String terminologyId, String subsetId )
   {
   }
   
   public List<Term> findCandidates( String text, String terminologyId, String subsetId, String language )
   {
   }
}