package util.terminology

/*
http://www.itserver.es/ITServer/rest/loinc/searchloinc?textToFind=glucos&fLoincNum=&fComponent=&fProperty=&fAspectTemp=&fSystem=&fScale=&fMethod=&fShortName=&selectType=1&language=en

<loincs>
   <loinc>
      <component>
      Glucose^1.5H post 0.05-0.15 U insulin/kg IV post 12H CFst
      </component>
      <exampleUnits>mg/dL</exampleUnits>
      <generatedYn/>
      <id>5471</id>
      <language>enx</language>
      <loincClass>CHAL</loincClass>
      <loincNum>1493-6</loincNum>
      <loincStatus>active</loincStatus>
      <longCommonName>
      Glucose [Mass/volume] in Serum or Plasma --1.5 hours post 0.05-0.15 U insulin/kg IV 12 hours fasting
      </longCommonName>
      <methodTyp/>
      <property>MCnc</property>
      <relatednames2>
      Glu; Gluc; Glucoseur; Insul; Humulin; HUM; IH7; Lente; NPH; Semilente; Ultralente; Sliding; 1.5h p U/kg Ins IV; p 12h fast; 1.5h p U/kg Ins IV; Mass concentration; Level; Point in time; Random; SerPl; SerPlas; SerP; Serum; SR; Plasma; Pl; Plsm; Quantitative; QNT; Quant; Quan; PST; After; PC; Fast; Calorie Fast; Fasting; 90 minutes; 1 1/2 hours; 90M; 1.5Hr; 90min; 90 min; 1 1/2 HR; CHEMISTRY.CHALLENGE TESTING; CHEMISTRY.CHALLENGE TESTING; GTT; Glu tol; Glucose tolerance
      </relatednames2>
      <scaleTyp>Qn</scaleTyp>
      <shortname>Glucose 1.5h p U/kg Ins IV SerPl-mCnc</shortname>
      <similarity>6.74</similarity>
      <system>Ser/Plas</system>
      <timeAspct>Pt</timeAspct>
   </loinc>
   ...
</loincs>
*/

class LoincITServerAccess implements ITerminologyAccess {

   /**
    * Sugiere terminos conocidos a partir de un texto parcial.
    * El resultado puede tener codigos asociados o ser solo textos.
    */
   public List<Term> suggestTerms(String text)
   {
   }
   
   public List<Term> suggestTerms(String text, String language)
   {
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