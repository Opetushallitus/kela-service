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

import java.io.File;
import java.util.Properties;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
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
    
    @Autowired
    ProducerTemplate producerTemplate;
    
    private String protocol;

    private String host;
    private String username;
    private String password;
    private String sourcePath;
    private String targetPath;
    private String dataTimeout;

    /**
     * Generates all KELA-OPTI transfer files currently implemented
     */
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
    
    /**
     * Performs ftp transfer of generated kela-opti files.
     * @throws Exception
     */
    public void transferFiles() throws Exception {
        LOG.info("transferFiles: ");
        String targetUrl = String.format("%s%s%s%s%s%s%s%s%s%s", 
                protocol, 
                "://", 
                username, 
                "@", 
                host, 
                targetPath, 
                "?password=", 
                password,
                "&ftpClient.dataTimeout=",
                dataTimeout + "&passiveMode=true");
        LOG.info("Target url: " + targetUrl);
        
        
        producerTemplate.sendBodyAndHeader(targetUrl, new File(optiliWriter.fileName), Exchange.FILE_NAME, this.optiliWriter.getFileLocalName());
        producerTemplate.sendBodyAndHeader(targetUrl, new File(optiniWriter.fileName), Exchange.FILE_NAME, this.optiniWriter.getFileLocalName());
        producerTemplate.sendBodyAndHeader(targetUrl, new File(optiolWriter.fileName), Exchange.FILE_NAME, this.optiolWriter.getFileLocalName());
        producerTemplate.sendBodyAndHeader(targetUrl, new File(optituWriter.fileName), Exchange.FILE_NAME, this.optituWriter.getFileLocalName());
        producerTemplate.sendBodyAndHeader(targetUrl, new File(optiopWriter.fileName), Exchange.FILE_NAME, this.optiopWriter.getFileLocalName());
        //LEFT OUT FOR NOW! producerTemplate.sendBodyAndHeader(targetUrl, new File(optiyhWriter.fileName), Exchange.FILE_NAME, this.optiyhWriter.getFileLocalName());
        producerTemplate.sendBodyAndHeader(targetUrl, new File(optiytWriter.fileName), Exchange.FILE_NAME, this.optiytWriter.getFileLocalName());
        LOG.info("Files transfered");
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
        Properties props = System.getProperties();
        props.put("socksProxyHost", "127.0.0.1");
        props.put("socksProxyPort", "9090") ;
        System.setProperties(props);
        final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/context/bundle-context.xml");
        KelaGenerator kelaGenerator = context.getBean(KelaGenerator.class);
        kelaGenerator.generateKelaFiles();
        try {
            kelaGenerator.transferFiles();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }



}
