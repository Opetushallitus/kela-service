/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.rajapinnat.kela;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class KelaGenerator {
    
    private static final Logger LOG = LoggerFactory.getLogger(KelaGenerator.class);

    @Autowired
    private WriteOPTILI optiliWriter;
    @Autowired
    private WriteOPTINI optiniWriter;
    @Autowired
    private WriteOPTIOL optiolWriter;
    @Autowired
    private WriteOPTIOP optiopWriter;
    @Autowired
    private WriteOPTITU optituWriter;
    @Autowired
    private WriteOPTIYH optiyhWriter;
    @Autowired
    private WriteOPTIYT optiytWriter;
    @Autowired
    private OrganisaatioContainer orgContainer;
    
    private String protocol;

    private String host;
    private String username;
    private String password;
    private String sourcePath;
    private String targetPath;
    private String dataTimeout;

    public void generateKelaFiles() {
        LOG.info("Fetching organisaatiot");
        long time = System.currentTimeMillis();
        long startTime = time;
        orgContainer.fetchOrgnaisaatiot();
        LOG.info("Fetch time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        
        LOG.info("Generating optili");
        time = System.currentTimeMillis();
        writeKelaFile(optiliWriter);
        LOG.info("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        LOG.info("Generating optini");
        writeKelaFile(optiniWriter);
        LOG.info("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        LOG.info("Generating optiol");
        writeKelaFile(optiolWriter);
        LOG.info("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        LOG.info("Generating optiop");
        writeKelaFile(optiopWriter);
        LOG.info("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        LOG.info("Generating optitu");
        writeKelaFile(optituWriter);
        LOG.info("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        LOG.info("Generating optiyh");
        writeKelaFile(optiyhWriter);
        LOG.info("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        LOG.info("Generating optiyt");
        writeKelaFile(optiytWriter);
        LOG.info("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        LOG.info("All files generated");
        LOG.info("Generation time: " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");
    }
    
    public void transferFiles() throws Exception {
        CamelContext context = new DefaultCamelContext();
        try {

            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                 from(String.format("%s%s", 
                                     "file:", 
                                     sourcePath)).to(String.format("%s%s%s%s%s%s%s%s%s%s", 
                                                                 protocol, 
                                                                 "://", 
                                                                 username, 
                                                                 "@", 
                                                                 host, 
                                                                 targetPath, 
                                                                 "?password=", 
                                                                 password,
                                                                 "&ftpClient.dataTimeout=",
                                                                 dataTimeout));
            }
            });
            context.start();
        
            Thread.sleep(10000);
        
        } finally {
            context.stop();
        }
    }
    
    private void writeKelaFile(AbstractOPTIWriter kelaWriter) {
        try {
            kelaWriter.writeFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        } 
    }
    
    @Value("${transferprotocol}")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Value("${transferhost}")
    public void setHost(String host) {
        this.host = host;
    }

    @Value("${transferuser}")
    public void setUsername(String username) {
        this.username = username;
    }

    @Value("${transferpassword}")
    public void setPassword(String password) {
        this.password = password;
    }

    @Value("${exportdir}")
    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @Value("${targetPath}")
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    @Value("${dataTimeout}")
    public void setDataTimeout(String dataTimeout) {
        this.dataTimeout = dataTimeout;
    }
    
    
    public String getDataTimeout() {
        return dataTimeout;
    }
    
    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }
    
    public static void main (String[] args) {
        final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/context/bundle-context.xml");
        KelaGenerator kelaGenerator = context.getBean(KelaGenerator.class);
        kelaGenerator.generateKelaFiles();
    }



}
