package gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
//import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class GuiManager {

   // Ruta debe ser independiente del SO
   // http://code.google.com/p/open-ehr-gen-framework/issues/detail?id=54
   private static String PS = System.getProperty("file.separator");
   
   /**
   * Directorio donde estan las guis generadas.
   * FIXME: deberia ser un parametro de la aplicacion en un .properties
   */
   private static String guiRepositoryPath = "." + PS + "grails-app" + PS + "views" + PS + "genViews" + PS;
   
   // Cache: templateId_mode => String (html de la gui generada)
   // mode puede ser show, create o edit
   private static Map<String, String> cache;
   
   
   // SINGLETON
   private static GuiManager instance = null;
    
   private GuiManager()
   {
      cache = new HashMap<String, String>();
   }
    
   public static GuiManager getInstance()
   {
      if (instance == null) instance = new GuiManager();
      return instance;
   }
   
   public boolean exists(String templateId, String mode, String locale)
   {
      File f = new File(guiRepositoryPath + templateId +"_"+ mode +"_"+ locale +".htm");
      return f.exists();
   }
   
   public String get(String templateId, String mode, String locale)
   {
      String value = cache.get(templateId+"_"+mode+"_"+locale);
      if (value == null)
      {
         //this.cache.put(templateId+"_"+mode, archivo.getText());
         BufferedReader in = null;
         value = "";
         try
         {
            FileReader archivo = new FileReader(guiRepositoryPath + templateId +"_"+ mode +"_"+ locale +".htm");
            //System.out.println("Archivo: "+ this.guiRepositoryPath + templateId +"_"+ mode + ".htm");
            
            // Implementacion mia
            //long start = System.currentTimeMillis();
            
            in = new BufferedReader(archivo, 32768); // Buffer de 32KB
            String buff;
            while ((buff = in.readLine()) != null)
            {
               value += buff;
            }
            
            //long end = System.currentTimeMillis();
            //System.out.println("tiempo lectura 1: " + (end-start));
            
            cache.put(templateId+"_"+mode, value);
            
            //System.out.println("No se encuentra gui "+ templateId +" en cache");
            //System.out.println("Carga value "+ value);
         }
         catch (Exception e)
         {
            System.out.println(e.getMessage());
         }
         finally
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
               System.out.println(e.getMessage());
            }
         }

         //println "GuiManager: gui " + templateId + " no esta en cache!";
      }
      
      return value;
   }
   
   public void add(String templateId, String mode, String gui)
   {
      cache.put(templateId+"_"+mode, gui);
   }
}