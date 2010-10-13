
/*
 * 
 */

package org.opensih.webservices;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.2.3
 * Tue Dec 15 16:04:03 GMT 2009
 * Generated source version: 2.2.3
 * 
 */


@WebServiceClient(name = "ConsultaRepo", 
                  wsdlLocation = "http://192.168.118.28:8080/Rphm-ear-1.0-Rphm-ejb-1.0/ServicioRepositorio?wsdl",
                  targetNamespace = "http://webservices.opensih.org/") 
public class ConsultaRepo extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://webservices.opensih.org/", "ConsultaRepo");
    public final static QName OpenSIH_0020_0020Prototipo_0020WebService_0020PublisherPort = new QName("http://webservices.opensih.org/", "OpenSIH - Prototipo WebService PublisherPort");
    static {
        URL url = null;
        try {
            url = new URL("http://192.168.118.28:8080/Rphm-ear-1.0-Rphm-ejb-1.0/ServicioRepositorio?wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from http://192.168.118.28:8080/Rphm-ear-1.0-Rphm-ejb-1.0/ServicioRepositorio?wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public ConsultaRepo(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ConsultaRepo(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ConsultaRepo() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     * 
     * @return
     *     returns OpenSIH_0020_0020Prototipo_0020WebService_0020Publisher
     */
    @WebEndpoint(name = "OpenSIH - Prototipo WebService PublisherPort")
    public OpenSIH_0020_0020Prototipo_0020WebService_0020Publisher getOpenSIH_0020_0020Prototipo_0020WebService_0020PublisherPort() {
        return super.getPort(OpenSIH_0020_0020Prototipo_0020WebService_0020PublisherPort, OpenSIH_0020_0020Prototipo_0020WebService_0020Publisher.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns OpenSIH_0020_0020Prototipo_0020WebService_0020Publisher
     */
    @WebEndpoint(name = "OpenSIH - Prototipo WebService PublisherPort")
    public OpenSIH_0020_0020Prototipo_0020WebService_0020Publisher getOpenSIH_0020_0020Prototipo_0020WebService_0020PublisherPort(WebServiceFeature... features) {
        return super.getPort(OpenSIH_0020_0020Prototipo_0020WebService_0020PublisherPort, OpenSIH_0020_0020Prototipo_0020WebService_0020Publisher.class, features);
    }

}
