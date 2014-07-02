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
import java.util.Arrays;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

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
    private WriteOPTITU optituWriter;
    @Autowired
    private WriteOPTIYH optiyhWriter;
    @Autowired
    private WriteOPTIYT optiytWriter;
    @Autowired
    private WriteOPTIOR optiorWriter;
    
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
        LOG.info("Fetching organisaatiot from index...");
        long time = System.currentTimeMillis();
        long startTime = time;
        orgContainer.fetchOrganisaatiot();
        LOG.info("Fetch time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        writeKelaFile(optiorWriter);
        writeKelaFile(optiliWriter);
        writeKelaFile(optiniWriter);
        writeKelaFile(optiolWriter);
        writeKelaFile(optituWriter); 
        writeKelaFile(optiyhWriter);
        writeKelaFile(optiytWriter);
        LOG.info("All files generated");
        LOG.info("Generation time: " + (System.currentTimeMillis() - startTime)/1000.0 + " seconds");
    }
    
    /**
     * Performs ftp transfer of generated kela-opti files.
     * @throws Exception
     */
    
	private String mkTargetUrl(String protocol, String username,
	String host, String targetPath, String password,
	String dataTimeout) {
		return String.format("%s%s%s%s%s%s%s%s%s%s", 
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
	}

    private String targetUrl;
    public void transferFiles() throws Exception {
        LOG.info("transferFiles: target url: " + mkTargetUrl(protocol, username, host, targetPath, "???", dataTimeout));
        targetUrl = mkTargetUrl(protocol, username, host, targetPath, password, dataTimeout);
        sendFile(optiorWriter);
        sendFile(optiliWriter);
        sendFile(optiniWriter);
        sendFile(optiolWriter);
        sendFile(optituWriter);
        sendFile(optiyhWriter);
        sendFile(optiytWriter);
        LOG.info("Files transferred");
    }
    
    private void sendFile(AbstractOPTIWriter writer) {
    	LOG.info("Sending: " + writer.getFileName()+"...");
    	producerTemplate.sendBodyAndHeader(targetUrl, new File(writer.getFileName()), Exchange.FILE_NAME, writer.getFileLocalName());
    	LOG.info("Done.");
    }
    
    private void writeKelaFile(AbstractOPTIWriter kelaWriter) {
        try {
            long time = System.currentTimeMillis();
            LOG.info(String.format("Generating file %s...",kelaWriter.getFileName()));
            kelaWriter.writeStream();
            LOG.info("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        } catch (Exception ex) {
        	LOG.error(ex.getMessage());
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
    	if (args.length>0) {
    		LOG.info("mode: " +Arrays.toString(args));
    	}
    	boolean sendonly = (args.length==1 && args[0].equalsIgnoreCase("-sendonly"));
    	boolean generateonly = (args.length==1 && args[0].equalsIgnoreCase("-generateonly"));

        final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/context/bundle-context.xml");
        KelaGenerator kelaGenerator = context.getBean(KelaGenerator.class);
        if (!sendonly) {
        	kelaGenerator.generateKelaFiles();
        }else{
        	LOG.info("skipped generate files");
        }
        if (!generateonly) {
        	try {
        		kelaGenerator.transferFiles();
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }else{
        	LOG.info("skipped transfer files");
        }
    }
}
