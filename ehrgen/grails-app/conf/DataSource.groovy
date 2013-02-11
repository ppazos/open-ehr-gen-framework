dataSource {
   pooled = true
   driverClassName = "org.hsqldb.jdbcDriver"
   username = "sa"
   password = ""
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
    //cache.provider_class='com.opensymphony.oscache.hibernate.OSCacheProvider'
}
// environment specific settings
environments {
   development {
      dataSource {
         
         // ====================================================================
         // Conexiones
         // ====================================================================
         
         // Configuracion para utilizar HSQL en memoria
         // Selecciona la base de datos 'devDB'
         // dbCreate = "create-drop" // one of 'create', 'create-drop','update'
         // url = "jdbc:hsqldb:mem:devDB"
         
         
         // Configuracion para utilizar MySQL
         // Selecciona la base de datos oehr_dev
         driverClassName = "com.mysql.jdbc.Driver"
         url = "jdbc:mysql://localhost:3306/oehr_dev?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8"
         username = "root"
         password = "vertrigo"
         
         
         // Configuracion para utilizar PostgreSQL
         // driverClassName = "org.postgresql.Driver"
         // url = "jdbc:postgresql://localhost:5432/ehrgen"
         // username = "postgres"
         // password = "postgres"
         // dialect = net.sf.hibernate.dialect.PostgreSQLDialect
         
         // ====================================================================
         // Otras configuraciones
         // ====================================================================
         
         // Reutilizar conecciones existentes con la base de datos 
         pooled = true
         
         // create-drop borra la base de datos y la genera de nuevo cada vez que se ejecuta la aplicacion
         // Ver mas opciones en la seccion 3.3: http://grails.org/doc/1.0/guide/3.%20Configuration.html
         dbCreate = "create-drop"
         
         // Poner en true para loguear consultas SQL
         loggingSql = false
      }
   }
   test {
      dataSource {
         
         // Configuracion para utilizar HSQL en memoria
         url = "jdbc:hsqldb:mem:testDb"
         
         dbCreate = "update"
      }
   }
   production {
      dataSource {
         
         // Configuracion para utilizar HSQL en archivo
         // Selecciona la base de datos prodDb
         //url = "jdbc:hsqldb:file:prodDb;shutdown=true"
         
         // Configuracion para utilizar MySQL
         // Selecciona la base de datos oehr_dev (por simplicidad "prod" utiliza la misma base que en "dev")
         driverClassName = "com.mysql.jdbc.Driver"
         url = "jdbc:mysql://localhost:3306/oehr_dev?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8"
         dbCreate = "update"
         username = "root"
         password = "vertrigo"
         
         // Reutilizar conecciones existentes con la base de datos
         pooled = true
         
         // update utiliza la base de datos existente actualizando sus datos en el bootstrap cada vez que se ejecuta la aplicacion
         // Ver mas opciones en la seccion 3.3: http://grails.org/doc/1.0/guide/3.%20Configuration.html
         dbCreate = "update"
      }
   }
}