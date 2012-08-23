package cache

import com.thoughtworks.xstream.XStream
import hce.core.composition.content.ContentItem

class PathValores {

    Map params
    String codedParams
    static transients = ['params']
    
    // link al rmNode que se bindea con las paths que se guardan en este PathValores
    ContentItem item
    
    // Nuevo para calcular codedValue
    def beforeInsert() {
       XStream xstream = new XStream()
       xstream.omitField(PathValores.class, "errors");
       codedParams = xstream.toXML(params)
    }
    def beforeUpdate() {
       XStream xstream = new XStream()
       xstream.omitField(PathValores.class, "errors");
       codedParams = xstream.toXML(params)
    }
    // Al reves
    def afterLoad() {
       XStream xstream = new XStream()
       xstream.omitField(PathValores.class, "errors");
       if (codedParams) params = xstream.fromXML(codedParams)
    }
    
    static mapping = {
    }

    static constraints = {
       codedParams (nullable: true, maxSize:10485760) // 1024*1024*10 // FIXME: si subo imagenes esto se puede quedar corto!
    }
}