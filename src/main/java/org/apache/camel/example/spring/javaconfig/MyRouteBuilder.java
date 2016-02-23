/*
* Copyright (c): cbb 2016
*/
package org.apache.camel.example.spring.javaconfig;

import java.io.File;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.idempotent.FileIdempotentRepository;

/**
 * Route zur Proxifizierung eines konfigurierbaren Webservice-Endpoints
 * mit Tracing-Funktion.
 * <br>
 * @author cbb
 */
public class MyRouteBuilder extends RouteBuilder {

    public static final String ENDPOINT_PROXY                   = "cxf:bean:endpointEAI?dataFormat=MESSAGE";
    public static final String ROUTE_ID_LOGGING                 = "ROUTE_ID_LOGGING";
    public static final String ENDPOINT_LOGGING                 = "direct:logging";
    public static final String ROUTE_ID_MESSENGING 		= "ROUTE_ID_MESSENGING";
    public static final String ENDPOINT_MESSENGING 		= "direct:MESSENGING";
    public static final String ROUTE_ID_ENTERING                = "ROUTE_ID_ENTERING";
    
    private String file_base;
    
    @Override
    public void configure() {
        
        configureExceptionRoute();
        configureProxyRoute();
        configureServiceRoute();
        configureLoggingRoute();
    }
    
    /**
     * Konfiguriert eine allgemeine Fehlerbehandlung für <br>
     * alle auf den Routen auftretenden Fehler.
     * */
    private void configureExceptionRoute() {
        onException(Throwable.class)
                
                .log("${exception.stacktrace}")
                
                // damit die Exception nach Retry weitergeworfen wird
                .handled(false)
                
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                
                .log(LoggingLevel.ERROR, this.getClass().getName(), "helloException!");
        
    }
    
    
    private void configureProxyRoute() {
        // <!-- cxf consumer mit MESSAGE format -->
        from("file:{{file.base}}/in?delay=5000&move=../done&moveFailed=../failed?idempotent=true").routeId(ROUTE_ID_ENTERING)
                
                .log(LoggingLevel.INFO, this.getClass().getName(), "ROUTE_ID_ENTERING")
                
                .idempotentConsumer(method(HashUtil.class, "hash"),
                    FileIdempotentRepository.fileIdempotentRepository(new File(file_base+"/repo")))
                    .skipDuplicate(false)
                    .filter(property(Exchange.DUPLICATE_MESSAGE).isEqualTo(true))
                        // filter out duplicate messages by sending them to someplace else and then stop
                        .log(LoggingLevel.WARN, this.getClass().getName(), "Duplicate")
                        .stop()
                    .end()
                
                .split(body(String.class).tokenize("\n"))
                
                .process(new Processor() {

            @Override
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setHeader(Exchange.FILE_NAME, exchange.getIn().getHeader(Exchange.FILE_NAME) + "_"+exchange.getIn().getHeader(Exchange.SPLIT_INDEX)+".txt");
            }
        })
//                .to("file:/tmp/tproc/out.lines?fileExist=TryRename&fileName=$simple{file:filename_nosuffix + _ + header.CamelSplitIndex}")
                .to("file:C:/tmp/tproc/out.lines?fileExist=TryRename")
//                .to("file:/tmp/tproc/out.lines")
                
        ;
        
    }

    private void configureServiceRoute(){
        
        from("file:/txt").routeId("ROUTE_ID_TXT")
                
                .log(LoggingLevel.INFO, this.getClass().getName(), "ROUTE_ID_TXT")

//                .convertBodyTo(Transfer.class)
                
//                .to("jpa:Transfers")
        ;

    }
    
    /*
    // Route für Timestamp-Protokollierung für z.B. Performance-Messung
    */
    private void configureLoggingRoute() {
        from(ENDPOINT_LOGGING).routeId(ROUTE_ID_LOGGING)
                
                .log(LoggingLevel.INFO, this.getClass().getName(), "ROUTE_ID_LOGGING")
                
                // Einfaches Logging-Protokoll erstellen
                .to("log:de.muenchen.eai.request?showBody=true&showStreams=true")
                // File 
//      	.to("file://C:tmp?fileName=${date:now:yyyyMMdd}-${in.header.type}-${date:now:hh.mm.ss.SSS}.txt&autoCreate=true")
                
                .log(LoggingLevel.INFO, this.getClass().getName(), "ROUTE_ID_LOGGING")
        ;
    }

    public String getFile_base() {
        return file_base;
    }

    public void setFile_base(String file_base) {
        this.file_base = file_base;
    }
    

    
}
